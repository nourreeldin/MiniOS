package Model;
import java.util.ArrayList;

public class ProcessList {
    private final ArrayList<Process> processes;
    private static ProcessList INSTANCE = null;
    private ProcessList() {
        this.processes = new ArrayList<>();
    }

    public static ProcessList getInstance() {
        if(INSTANCE == null)
            INSTANCE = new ProcessList();
        return INSTANCE;
    }

    public void addProcess(int arrivalTime, int burstTime) {
        int pid = processes.size();
        this.processes.add(new Process(arrivalTime, burstTime, pid));
    }

    public int getSize() {
        return processes.size();
    }

    public ArrayList<Process> getProcesses() {
        return processes;
    }

    public int getTotalTurnAroundTime() {
        int total = 0;
        for(Process p: processes) {
            total += p.getTurnAroundTime();
        }
        return total;
    }

    public int getTotalWaitingTime() {
        int total = 0;
        for(Process p: processes) {
            total += p.getWaitingTime();
        }
        return total;
    }

    public double getAverageTurnAroundTime() {
        double size = processes.size();
        return getTotalTurnAroundTime() / size;
    }

    public double getAverageTurnWaitingTime() {
        double size = processes.size();
        return getTotalWaitingTime() / size;
    }
}
