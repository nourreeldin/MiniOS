package Controller;

import Model.ProcessList;
import PageReplacement.*;
import UI.Terminal;

import java.util.*;

public class PageReplacementHandler {

    public static class StepResult {
        public int step;
        public int page;
        public int[] frames;
        public boolean[] refBits;   
        public boolean fault;
        public int replacedPage; 
        public String algorithm;

        public StepResult(int step, int page, int[] frames, boolean[] refBits,
                          boolean fault, int replacedPage, String algorithm) {
            this.step        = step;
            this.page        = page;
            this.frames      = frames.clone();
            this.refBits     = refBits != null ? refBits.clone() : null;
            this.fault       = fault;
            this.replacedPage = replacedPage;
            this.algorithm   = algorithm;
        }
    }

    public static class Result {
        public List<StepResult> steps = new ArrayList<>();
        public int totalFaults;
        public double faultRate;
        public String algorithm;
        public int capacity;
        public int[] pages;
    }

    public Result simulate(int[] pages, int capacity, String algorithm) {
        Result result = new Result();
        result.algorithm = algorithm;
        result.capacity  = capacity;
        result.pages     = pages;

        List<int[]> raw;
        boolean isClock = algorithm.equalsIgnoreCase("CLOCK");

        switch (algorithm.toUpperCase()) {
            case "FIFO":  raw = FIFO.simulate(pages, capacity);  buildSteps(raw, capacity, "FIFO",  false, result); break;
            case "LRU":   raw = LRU.simulate(pages, capacity);   buildSteps(raw, capacity, "LRU",   false, result); break;
            case "MRU":   raw = MRU.simulate(pages, capacity);   buildSteps(raw, capacity, "MRU",   false, result); break;
            case "OPT":   raw = OPT.simulate(pages, capacity);   buildSteps(raw, capacity, "OPT",   false, result); break;
            case "CLOCK": raw = Clock.simulate(pages, capacity); buildSteps(raw, capacity, "Clock", true,  result); break;
            default:
                System.out.println(Terminal.RED + "Unknown algorithm: " + algorithm + Terminal.RESET);
                return result;
        }

        result.totalFaults = result.steps.stream().mapToInt(s -> s.fault ? 1 : 0).sum();
        result.faultRate   = pages.length > 0 ? (double) result.totalFaults / pages.length * 100 : 0;
        return result;
    }

    private void buildSteps(List<int[]> raw, int capacity, String alg, boolean hasBits, Result result) {
        int step = 1;
        for (int[] e : raw) {
            int page     = e[0];
            boolean fault = e[1] == 1;
            int replaced  = e[2];
            int[] frames  = Arrays.copyOfRange(e, 3, 3 + capacity);
            boolean[] refBits = null;
            if (hasBits) {
                refBits = new boolean[capacity];
                for (int i = 0; i < capacity; i++) refBits[i] = e[3 + capacity + i] == 1;
            }
            result.steps.add(new StepResult(step++, page, frames, refBits, fault, replaced, alg));
        }
    }

    public void printResult(Result result) {
        System.out.println(Terminal.CYAN + "\n=== PAGE REPLACEMENT: " + result.algorithm + " ===" + Terminal.RESET);
        System.out.printf(Terminal.WHITE_BOLD + "%-6s %-6s %-40s %-8s%n" + Terminal.RESET,
                "Step", "Page", "Frames", "Fault");
        System.out.println(Terminal.CYAN + "-".repeat(65) + Terminal.RESET);
        boolean isClock = result.algorithm.equalsIgnoreCase("Clock");
        for (StepResult s : result.steps) {
            StringBuilder fb = new StringBuilder("[");
            for (int i = 0; i < s.frames.length; i++) {
                if (s.frames[i] == -1) {
                    fb.append("-");
                } else if (isClock && s.refBits != null) {
                    fb.append(s.frames[i]).append("/").append(s.refBits[i] ? "1" : "0");
                } else {
                    fb.append(s.frames[i]);
                }
                if (i < s.frames.length - 1) fb.append(", ");
            }
            fb.append("]");
            String faultStr = s.fault
                    ? Terminal.RED   + "FAULT" + Terminal.RESET
                    : Terminal.GREEN + "HIT  " + Terminal.RESET;
            System.out.printf("%-6d %-6d %-40s %s%n", s.step, s.page, fb, faultStr);
        }
        System.out.println(Terminal.CYAN + "-".repeat(65) + Terminal.RESET);
        System.out.printf(Terminal.GREEN + "Total Page Faults: %d | Fault Rate: %.2f%%%n" + Terminal.RESET,
                result.totalFaults, result.faultRate);
    }

    public void showMenu(java.util.Scanner scanner) {
        ProcessList pl = ProcessList.getInstance();
        System.out.println(Terminal.CYAN + "\n=== PAGE REPLACEMENT ===" + Terminal.RESET);

        if (pl.getSize() == 0) {
            System.out.println(Terminal.RED + "No processes. Please add processes first." + Terminal.RESET);
            return;
        }

        boolean keepGoing = true;
        while (keepGoing) {

            System.out.println("Processes:");
            for (int i = 0; i < pl.getSize(); i++) {
                var p = pl.getProcesses().get(i);
                System.out.printf("  P%d - Pages: %d%n", p.getPid(), p.getNumberOfPages());
            }

            int pid = -1;
            while (pid < 0) {
                System.out.print("Select process PID: ");
                try {
                    pid = Integer.parseInt(scanner.nextLine().trim());
                    if (pl.getProcess(pid) == null) {
                        System.out.println(Terminal.RED + "Process P" + pid + " not found. Try again." + Terminal.RESET);
                        pid = -1;
                    }
                } catch (Exception e) {
                    System.out.println(Terminal.RED + "Invalid PID. Enter a number." + Terminal.RESET);
                }
            }
            var proc = pl.getProcess(pid);

            int[] pages = null;
            while (pages == null) {
                System.out.print("Enter page reference string (space-separated, e.g. 7 0 1 2 0 3): ");
                String refStr = scanner.nextLine().trim();
                try {
                    String[] parts = refStr.split("\\s+");
                    int[] tmp = new int[parts.length];
                    for (int i = 0; i < parts.length; i++) tmp[i] = Integer.parseInt(parts[i]);

                    boolean valid = true;
                    for (int pg : tmp) {
                        if (!proc.isValidPage(pg)) {
                            System.out.println(Terminal.RED + "Page " + pg + " does not belong to P" + pid
                                    + " (valid: 0-" + (proc.getNumberOfPages() - 1) + "). Try again." + Terminal.RESET);
                            valid = false;
                            break;
                        }
                    }
                    if (valid) pages = tmp;
                } catch (Exception e) {
                    System.out.println(Terminal.RED + "Invalid page string. Enter space-separated integers." + Terminal.RESET);
                }
            }

            String alg = null;
            while (alg == null) {
                System.out.println("Algorithms: 1.FIFO  2.LRU  3.MRU  4.OPT  5.Clock");
                System.out.print("Choose: ");
                try {
                    int choice = Integer.parseInt(scanner.nextLine().trim());
                    String[] algs = {"FIFO", "LRU", "MRU", "OPT", "CLOCK"};
                    if (choice < 1 || choice > algs.length) throw new Exception();
                    alg = algs[choice - 1];
                } catch (Exception e) {
                    System.out.println(Terminal.RED + "Invalid choice. Enter 1-5." + Terminal.RESET);
                }
            }

            int frames = pl.getNumberOfFrames();
            System.out.println(Terminal.GREEN + "Frames available: " + frames
                    + " (MemorySize=" + pl.getMemorySize() + " / FrameSize=" + pl.getFrameSize() + ")" + Terminal.RESET);

            Result r = simulate(pages, frames, alg);
            printResult(r);

            System.out.print("\n(t)ry again or (b)ack to menu: ");
            String choice = scanner.nextLine().trim().toLowerCase();
            if (!choice.equals("t")) keepGoing = false;
            else System.out.println(Terminal.CYAN + "\n=== PAGE REPLACEMENT ===" + Terminal.RESET);
        }
    }
}