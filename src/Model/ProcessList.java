package Model;

import java.util.ArrayList;

public class ProcessList {
    private final ArrayList<Process> processes;
    private static ProcessList INSTANCE = null;

    private int memorySize  = 16;   
    private int diskSize    = 200;  
    private int frameSize   = 4;    

    private ProcessList() {
        this.processes = new ArrayList<>();
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
        int blockStart = 0;
        for (Process existing : processes) blockStart += existing.getNumberOfPages();
        p.assignDiskBlocks(blockStart);
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
        int blockStart = 0;
        for (Process p : processes) {
            p.assignDiskBlocks(blockStart);
            blockStart += p.getNumberOfPages();
        }
    }

    public int getTotalPages() {
        int total = 0;
        for (Process p : processes) total += p.getNumberOfPages();
        return total;
    }
}