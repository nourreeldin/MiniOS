package Model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class ProcessList {
    private final ArrayList<Process> processes;
    private static ProcessList INSTANCE = null;

    private int memorySize  = 16;   
    private int diskSize    = 200;  
    private int frameSize   = 4;    

    private final Set<Integer> usedDiskBlocks;
    private final Random rand;

    private ProcessList() {
        this.processes = new ArrayList<>();
        this.usedDiskBlocks = new HashSet<>();
        this.rand = new Random();
    }

    public static ProcessList getInstance() {
        if (INSTANCE == null) INSTANCE = new ProcessList();
        return INSTANCE;
    }

    public void addProcess(int arrivalTime, int burstTime) {
        int pid = processes.size();
        processes.add(new Process(arrivalTime, burstTime, pid));
    }

    public void addProcess(int arrivalTime, int burstTime, int numberOfPages) {
        int pid = processes.size();
        Process p = new Process(arrivalTime, burstTime, pid);
        p.setNumberOfPages(numberOfPages);
        assignRandomBlocks(p);
        processes.add(p);
    }

    public int getSize() { return processes.size(); }

    public ArrayList<Process> getProcesses() { return processes; }

    public Process getProcess(int pid) {
        for (Process p : processes) if (p.getPid() == pid) return p;
        return null;
    }

    public int getMemorySize()  { return memorySize; }
    public int getDiskSize()    { return diskSize; }
    public int getFrameSize()   { return frameSize; }

    public int getNumberOfFrames() {
        return (frameSize > 0) ? memorySize / frameSize : 1;
    }

    public void setMemorySize(int ms)  { this.memorySize = ms; }
    public void setDiskSize(int ds)    { this.diskSize   = ds; }
    public void setFrameSize(int fs)   { this.frameSize  = fs; }

    public void reassignDiskBlocks() {
        usedDiskBlocks.clear();
        for (Process p : processes) {
            assignRandomBlocks(p);
        }
    }

    private void assignRandomBlocks(Process p) {
        List<Integer> blocks = new ArrayList<>();
        while (blocks.size() < p.getNumberOfPages()) {
            if (usedDiskBlocks.size() >= diskSize) break; 
            int b = rand.nextInt(diskSize);
            if (!usedDiskBlocks.contains(b)) {
                usedDiskBlocks.add(b);
                blocks.add(b);
            }
        }
        p.assignDiskBlocks(blocks);
    }

    public void clearProcesses() {
        processes.clear();
        usedDiskBlocks.clear();
    }

    public int getTotalPages() {
        int total = 0;
        for (Process p : processes) total += p.getNumberOfPages();
        return total;
    }
}