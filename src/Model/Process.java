package Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Process {
    private int pid;
    private int arrivalTime;
    private int burstTime;
    private int priority;
    private int completionTime;
    private int turnAroundTime;
    private int waitingTime;
    private int memorySize;       
    private int numberOfPages;    

    private Map<Integer, Integer> pageToBlockMap;

    private Map<Integer, Integer> pageInFrame;

    private boolean terminated;

    public Process(int arrivalTime, int burstTime, int pid) {
        this.pid = pid;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.memorySize = 0;
        this.numberOfPages = 0;
        this.pageToBlockMap = new HashMap<>();
        this.pageInFrame = new HashMap<>();
        this.terminated = false;
    }

    public int getPid()             { return pid; }
    public int getArrivalTime()     { return arrivalTime; }
    public int getBurstTime()       { return burstTime; }
    public int getPriority()        { return priority; }
    public int getCompletionTime()  { return completionTime; }
    public int getTurnAroundTime()  { return turnAroundTime; }
    public int getWaitingTime()     { return waitingTime; }
    public int getMemorySize()      { return memorySize; }
    public int getNumberOfPages()   { return numberOfPages; }
    public boolean isTerminated()   { return terminated; }

    public Map<Integer, Integer> getPageToBlockMap() { return pageToBlockMap; }
    public Map<Integer, Integer> getPageInFrame()    { return pageInFrame; }

    public void setPriority(int priority)           { this.priority = priority; }
    public void setCompletionTime(int ct)           { this.completionTime = ct; }
    public void setTurnAroundTime(int tat)          { this.turnAroundTime = tat; }
    public void setWaitingTime(int wt)              { this.waitingTime = wt; }
    public void setMemorySize(int memorySize)       { this.memorySize = memorySize; }
    public void setNumberOfPages(int n)             { this.numberOfPages = n; }
    public void setTerminated(boolean t)            { this.terminated = t; }

    public void assignDiskBlocks(int blockStart) {
        pageToBlockMap.clear();
        for (int p = 0; p < numberOfPages; p++) {
            pageToBlockMap.put(p, blockStart + p);
        }
    }

    public int getDiskBlock(int page) {
        return pageToBlockMap.getOrDefault(page, -1);
    }

    public boolean isValidPage(int page) {
        return page >= 0 && page < numberOfPages;
    }

    @Override
    public String toString() {
        return String.format("P%d(AT=%d,BT=%d,Pages=%d)", pid, arrivalTime, burstTime, numberOfPages);
    }
}