package Model;

public class Process {
    private int pid;
    private int arrivalTime;
    private int burstTime;
    private int priority;
    private int completionTime;
    private int turnAroundTime;
    private int waitingTime;
    private int memorySize;

    public Process(int arrivalTime, int burstTime, int pid) {
        this.pid = pid;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.memorySize = 0;
    }

    public int getPid() {
        return this.pid;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public int getBurstTime() {
        return burstTime;
    }

    public int getPriority() {
        return priority;
    }

    public int getCompletionTime() {
        return completionTime;
    }

    public int getTurnAroundTime() {
        return turnAroundTime;
    }

    public int getWaitingTime() {
        return waitingTime;
    }

    public int getMemorySize() {
        return memorySize;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setCompletionTime(int completionTime) {
        this.completionTime = completionTime;
    }

    public void setTurnAroundTime(int turnAroundTime) {
        this.turnAroundTime = turnAroundTime;
    }

    public void setWaitingTime(int waitingTime) {
        this.waitingTime = waitingTime;
    }

    public void setMemorySize(int memorySize) {
        this.memorySize = memorySize;
    }
}