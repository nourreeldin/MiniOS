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
}
