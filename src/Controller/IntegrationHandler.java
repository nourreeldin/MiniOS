package Controller;

import Model.Process;
import Model.ProcessList;
import UI.Terminal;

import java.util.*;

public class IntegrationHandler {

    public static class IntegrationStep {
        public int stepNumber;
        public int page;
        public int processId;
        public boolean pageFault;
        public boolean validPage;         
        public int replacedPage;          
        public int diskBlock;             
        public int[] frames;              
        public boolean[] clockRefBits;    
        public int[] diskHead;            
        public int diskSeekDistance;
        public String message;
        public int terminatedPid;         
        public int newCpuPid;             
        public int targetFrame;           
    }

    public static class IntegrationResult {
        public List<IntegrationStep> steps = new ArrayList<>();
        public int totalPageFaults;
        public int totalDiskMovement;
        public String pageAlg;
        public String diskAlg;
        public String diskDirection;
    }

    public IntegrationResult simulate(
            int[] pageRefString, int[] processPids,
            String pageAlg, String diskAlg,
            String diskDirection, int initialHead) {

        ProcessList pl = ProcessList.getInstance();
        int capacity = pl.getNumberOfFrames();
        IntegrationResult result = new IntegrationResult();
        result.pageAlg = pageAlg;
        result.diskAlg = diskAlg;
        result.diskDirection = diskDirection;

        boolean isClock = pageAlg.equalsIgnoreCase("CLOCK");

        int[] frames = new int[capacity];
        int[] frameOwner = new int[capacity];
        Arrays.fill(frames, -1);
        Arrays.fill(frameOwner, -1);

        boolean[] clockRefBits = new boolean[capacity];
        int clockPointer = 0;

        Queue<Integer> fifoSlots = new LinkedList<>(); 

        Map<Integer, Integer> lastUsed = new HashMap<>(); 
        int timeStamp = 0;

        Set<Integer> terminated = new HashSet<>();
        int currentHead = initialHead;
        int totalDisk = 0;

        for (int i = 0; i < pageRefString.length; i++) {
            int page = pageRefString[i];
            int pid  = processPids[i];

            IntegrationStep step = new IntegrationStep();
            step.stepNumber  = i + 1;
            step.page        = page;
            step.processId   = pid;
            step.replacedPage = -1;
            step.terminatedPid = -1;
            step.newCpuPid    = -1;
            step.diskSeekDistance = 0;
            step.diskBlock    = -1;

            Process proc = pl.getProcess(pid);

            if (proc == null) {
                step.validPage    = false;
                step.pageFault    = false;
                step.targetFrame  = -1;
                step.message      = "P" + pid + " does not present!";
                step.frames       = frames.clone();
                step.clockRefBits = isClock ? clockRefBits.clone() : null;
                step.diskHead     = new int[]{currentHead};
                result.steps.add(step);
                continue;
            }

            if (terminated.contains(pid)) {
                step.validPage    = false;
                step.pageFault    = false;
                step.targetFrame  = -1;
                step.message      = "P" + pid + " does not present!";
                step.frames       = frames.clone();
                step.clockRefBits = isClock ? clockRefBits.clone() : null;
                step.diskHead     = new int[]{currentHead};
                result.steps.add(step);
                continue;
            }

            if (!proc.isValidPage(page)) {
                terminated.add(pid);
                step.validPage     = false;
                step.pageFault     = false;
                step.targetFrame   = -1;
                step.terminatedPid = pid;
                for (int f = 0; f < frames.length; f++) {
                    if (frameOwner[f] == pid) {
                        frames[f] = -1;
                        frameOwner[f] = -1;
                        clockRefBits[f] = false;
                    }
                }
                int nextPid = selectNextProcess(pl, terminated);
                step.newCpuPid = nextPid;
                step.message   = "P" + pid + " accessed invalid page " + page
                        + "! Process TERMINATED. Next CPU: P" + nextPid;
                step.frames       = frames.clone();
                step.clockRefBits = isClock ? clockRefBits.clone() : null;
                step.diskHead     = new int[]{currentHead};
                result.steps.add(step);
                continue;
            }

            step.validPage = true;

            int pageKey = pid * 10000 + page;
            int hitSlot = -1;
            for (int f = 0; f < frames.length; f++) {
                if (frames[f] == page && frameOwner[f] == pid) { hitSlot = f; break; }
            }

            if (hitSlot >= 0) {
                step.pageFault = false;
                step.targetFrame = hitSlot;
                lastUsed.put(pageKey, timeStamp++);
                if (isClock) clockRefBits[hitSlot] = true;  
                step.message      = "P" + pid + " page " + page + " -> HIT";
                step.diskHead     = new int[]{currentHead};
            } else {
                step.pageFault = true;
                result.totalPageFaults++;
                int diskBlock = proc.getDiskBlock(page);
                step.diskBlock = diskBlock;

                int dist = Math.abs(diskBlock - currentHead);
                totalDisk += dist;
                step.diskSeekDistance = dist;
                step.diskHead = new int[]{currentHead, diskBlock};
                currentHead = diskBlock;

                if (capacity == 0) {
                    step.targetFrame = -1;
                    step.message = "P" + pid + " page " + page + " -> FAULT | Capacity is 0, no frame loaded";
                } else {
                    int emptySlot = -1;
                    for (int f = 0; f < frames.length; f++) {
                        if (frames[f] == -1) { emptySlot = f; break; }
                    }

                    if (emptySlot >= 0) {
                        frames[emptySlot]    = page;
                        frameOwner[emptySlot] = pid;
                        clockRefBits[emptySlot] = false;
                        step.targetFrame = emptySlot;
                        if (pageAlg.equalsIgnoreCase("FIFO")) fifoSlots.add(emptySlot);
                        step.message = "P" + pid + " page " + page + " -> FAULT | Load from Disk Block "
                                + diskBlock + " -> Frame " + emptySlot;
                    } else {
                        int evictSlot = selectVictimFrame(pageAlg, frames, frameOwner, lastUsed,
                                pageRefString, processPids, i, capacity, fifoSlots, clockRefBits, clockPointer);

                        if (isClock) clockPointer = (evictSlot + 1) % capacity;

                        int evictedPage = frames[evictSlot];
                        int evictedPid  = frameOwner[evictSlot];
                        step.replacedPage = evictedPage;
                        lastUsed.remove(evictedPid * 10000 + evictedPage);

                        frames[evictSlot]    = page;
                        frameOwner[evictSlot] = pid;
                        clockRefBits[evictSlot] = false;
                        step.targetFrame = evictSlot;
                        if (pageAlg.equalsIgnoreCase("FIFO")) fifoSlots.add(evictSlot);

                        step.message = "P" + pid + " page " + page + " -> FAULT | Replace P"
                                + evictedPid + ":page" + evictedPage + " using " + pageAlg
                                + " | Disk Block " + diskBlock + " -> Frame " + evictSlot;
                    }
                }
                lastUsed.put(pageKey, timeStamp++);
            }

            step.frames       = frames.clone();
            step.clockRefBits = isClock ? clockRefBits.clone() : null;
            result.steps.add(step);
        }

        result.totalDiskMovement = totalDisk;
        return result;
    }

    private int selectVictimFrame(String alg, int[] frames, int[] frameOwner,
                                  Map<Integer, Integer> lastUsed,
                                  int[] pageRef, int[] pids, int currentIdx, int capacity,
                                  Queue<Integer> fifoSlots, boolean[] clockRefBits, int clockPointer) {
        switch (alg.toUpperCase()) {
            case "LRU": {
                int lruVal = Integer.MAX_VALUE, victim = 0;
                for (int f = 0; f < capacity; f++) {
                    int key = frameOwner[f] * 10000 + frames[f];
                    int lu  = lastUsed.getOrDefault(key, 0);
                    if (lu < lruVal) { lruVal = lu; victim = f; }
                }
                return victim;
            }
            case "OPT": {
                int farthest = -1, victim = 0;
                for (int f = 0; f < capacity; f++) {
                    int nextUse = Integer.MAX_VALUE;
                    for (int j = currentIdx + 1; j < pageRef.length; j++) {
                        if (pageRef[j] == frames[f] && pids[j] == frameOwner[f]) {
                            nextUse = j; break;
                        }
                    }
                    if (nextUse > farthest) { farthest = nextUse; victim = f; }
                }
                return victim;
            }
            case "CLOCK": {
                int ptr = clockPointer;
                while (true) {
                    if (!clockRefBits[ptr]) {
                        return ptr;
                    }
                    clockRefBits[ptr] = false;
                    ptr = (ptr + 1) % capacity;
                }
            }
            case "FIFO": {
                if (!fifoSlots.isEmpty()) return fifoSlots.poll();
                return 0;
            }
            default: 
                if (!fifoSlots.isEmpty()) return fifoSlots.poll();
                return 0;
        }
    }

    private int selectNextProcess(ProcessList pl, Set<Integer> terminated) {
        int bestPid = -1, minBurst = Integer.MAX_VALUE;
        for (Process p : pl.getProcesses()) {
            if (!terminated.contains(p.getPid()) && p.getBurstTime() < minBurst) {
                minBurst = p.getBurstTime();
                bestPid  = p.getPid();
            }
        }
        return bestPid;
    }

    public void printResult(IntegrationResult result) {
        boolean isClock = result.pageAlg.equalsIgnoreCase("CLOCK");
        System.out.println(Terminal.CYAN + "\n=== INTEGRATION SIMULATION ===" + Terminal.RESET);
        System.out.printf(Terminal.WHITE_BOLD + "%-4s %-4s %-5s %-8s %-35s %s%n" + Terminal.RESET,
                "Step", "PID", "Page", "Fault", "Frames", "Message");
        System.out.println(Terminal.CYAN + "-".repeat(90) + Terminal.RESET);

        for (IntegrationStep s : result.steps) {
            StringBuilder fb = new StringBuilder("[");
            for (int i = 0; i < s.frames.length; i++) {
                if (s.frames[i] == -1) {
                    fb.append("-");
                } else if (isClock && s.clockRefBits != null) {
                    fb.append(s.frames[i]).append("/").append(s.clockRefBits[i] ? "1" : "0");
                } else {
                    fb.append(s.frames[i]);
                }
                if (i < s.frames.length - 1) fb.append(",");
            }
            fb.append("]");
            String faultStr = s.pageFault
                    ? Terminal.RED    + "FAULT" + Terminal.RESET
                    : !s.validPage
                    ? Terminal.PURPLE + "TERM " + Terminal.RESET
                    : Terminal.GREEN  + "HIT  " + Terminal.RESET;
            System.out.printf("%-4d %-4d %-5d %-8s %-35s %s%n",
                    s.stepNumber, s.processId, s.page, faultStr, fb, s.message);
        }
        System.out.println(Terminal.CYAN + "-".repeat(90) + Terminal.RESET);
        System.out.printf(Terminal.GREEN + "Total Page Faults: %d | Total Disk Movement: %d%n" + Terminal.RESET,
                result.totalPageFaults, result.totalDiskMovement);
    }

    public void showMenu(java.util.Scanner scanner) {
        ProcessList pl = ProcessList.getInstance();
        System.out.println(Terminal.CYAN + "\n=== INTEGRATED SIMULATION ===" + Terminal.RESET);
        if (pl.getSize() == 0) {
            System.out.println(Terminal.RED + "No processes. Please add processes first." + Terminal.RESET);
            return;
        }

        System.out.print("Enter page reference string as 'PID:page PID:page ...' (e.g. 0:3 1:1 0:5): ");
        String refStr = scanner.nextLine().trim();
        List<int[]> refs = new ArrayList<>();
        try {
            for (String tok : refStr.split("\\s+")) {
                String[] pp = tok.split(":");
                refs.add(new int[]{Integer.parseInt(pp[0]), Integer.parseInt(pp[1])});
            }
        } catch (Exception e) {
            System.out.println(Terminal.RED + "Invalid format. Use PID:page pairs." + Terminal.RESET);
            return;
        }
        int[] pages = refs.stream().mapToInt(x -> x[1]).toArray();
        int[] pids  = refs.stream().mapToInt(x -> x[0]).toArray();

        System.out.println("Page alg: 1.FIFO  2.LRU  3.OPT  4.Clock");
        System.out.print("Choose: ");
        String pageAlg;
        try {
            int c = Integer.parseInt(scanner.nextLine().trim());
            pageAlg = new String[]{"FIFO", "LRU", "OPT", "CLOCK"}[c - 1];
        } catch (Exception e) { System.out.println(Terminal.RED + "Invalid." + Terminal.RESET); return; }

        System.out.println("Disk alg: 1.FCFS  2.SSTF  3.SCAN  4.CSCAN  5.LOOK  6.CLOOK");
        System.out.print("Choose: ");
        String diskAlg;
        try {
            int c = Integer.parseInt(scanner.nextLine().trim());
            diskAlg = new String[]{"FCFS", "SSTF", "SCAN", "CSCAN", "LOOK", "CLOOK"}[c - 1];
        } catch (Exception e) { System.out.println(Terminal.RED + "Invalid." + Terminal.RESET); return; }

        String dir = "right";
        if (!diskAlg.equals("FCFS") && !diskAlg.equals("SSTF")) {
            System.out.print("Direction (left/right): ");
            dir = scanner.nextLine().trim().toLowerCase();
            if (!dir.equals("left") && !dir.equals("right")) dir = "right";
        }

        System.out.print("Initial disk head position (0-" + (pl.getDiskSize() - 1) + "): ");
        int head;
        try { head = Integer.parseInt(scanner.nextLine().trim()); }
        catch (Exception e) { System.out.println(Terminal.RED + "Invalid." + Terminal.RESET); return; }

        IntegrationResult result = simulate(pages, pids, pageAlg, diskAlg, dir, head);
        printResult(result);
    }
}