package CPUScheduling;
import Model.Process;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class RoundRobin {
    private static ArrayList<Process> processesList;
    private static ArrayList<GanttSegment> ganttChart;
    private static int timeQuantum;

    public static class GanttSegment {
        public int pid;
        public int startTime;
        public int endTime;

        public GanttSegment(int pid, int startTime, int endTime) {
            this.pid = pid;
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }

    public static void apply(ArrayList<Process> list, int quantum) {
        timeQuantum = quantum;
        int time = 0;
        ganttChart = new ArrayList<>();
        ArrayList<Process> processes = new ArrayList<>(list);
        int[] remainingBurst = new int[processes.size()];
        boolean[] inQueue = new boolean[processes.size()];

        for (int i = 0; i < processes.size(); i++) {
            remainingBurst[i] = processes.get(i).getBurstTime();
            processes.get(i).setCompletionTime(-1);
            inQueue[i] = false;
        }

        Queue<Integer> readyQueue = new LinkedList<>();
        int completed = 0;
        int idx = 0;

        for (int i = 0; i < processes.size(); i++) {
            if (processes.get(i).getArrivalTime() <= time) {
                readyQueue.offer(i);
                inQueue[i] = true;
            }
        }

        while (completed < processes.size()) {
            if (readyQueue.isEmpty()) {
                time++;
                for (int i = 0; i < processes.size(); i++) {
                    if (processes.get(i).getArrivalTime() <= time && !inQueue[i] && remainingBurst[i] > 0) {
                        readyQueue.offer(i);
                        inQueue[i] = true;
                    }
                }
                continue;
            }

            int currentIdx = readyQueue.poll();
            Process currentProcess = processes.get(currentIdx);
            inQueue[currentIdx] = false;

            int startTime = time;
            int executeTime = Math.min(remainingBurst[currentIdx], timeQuantum);
            time += executeTime;
            remainingBurst[currentIdx] -= executeTime;

            ganttChart.add(new GanttSegment(currentProcess.getPid(), startTime, time));

            for (int i = 0; i < processes.size(); i++) {
                if (processes.get(i).getArrivalTime() <= time && !inQueue[i] && remainingBurst[i] > 0 && i != currentIdx) {
                    readyQueue.offer(i);
                    inQueue[i] = true;
                }
            }

            if (remainingBurst[currentIdx] == 0) {
                currentProcess.setCompletionTime(time);
                currentProcess.setTurnAroundTime(time - currentProcess.getArrivalTime());
                currentProcess.setWaitingTime(currentProcess.getTurnAroundTime() - currentProcess.getBurstTime());
                completed++;
            } else {
                readyQueue.offer(currentIdx);
                inQueue[currentIdx] = true;
            }
        }

        processesList = processes;
    }

    public static ArrayList<Process> getProcessesList() {
        return processesList;
    }

    public static ArrayList<GanttSegment> getGanttChart() {
        return ganttChart;
    }


    public static int getTotalTurnAroundTime() {
        int total = 0;
        for (Process p : processesList) {
            total += p.getTurnAroundTime();
        }
        return total;
    }

    public static int getTotalWaitingTime() {
        int total = 0;
        for (Process p : processesList) {
            total += p.getWaitingTime();
        }
        return total;
    }

    public static double getAverageTurnAroundTime() {
        return (double) getTotalTurnAroundTime() / processesList.size();
    }

    public static double getAverageWaitingTime() {
        return (double) getTotalWaitingTime() / processesList.size();
    }

    public static double getCPUUtilization() {
        if (ganttChart.isEmpty()) return 0.0;
        int totalBurstTime = 0;
        for (Process p : processesList) {
            totalBurstTime += p.getBurstTime();
        }
        int totalTime = ganttChart.get(ganttChart.size() - 1).endTime;
        return (double) totalBurstTime / totalTime * 100;
    }
}