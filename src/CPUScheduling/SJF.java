package CPUScheduling;

import java.util.ArrayList;
import Model.Process;
import java.util.Comparator;

public class SJF {
    private static ArrayList<Process> processesList;
    private static ArrayList<GanttSegment> ganttChart;

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

    public static void apply(ArrayList<Process> list) {
        int time = 0;
        ganttChart = new ArrayList<>();
        ArrayList<Process> completed = new ArrayList<>();
        ArrayList<Process> processes = new ArrayList<>(list);

        for (Process p : processes)
            p.setCompletionTime(-1);

        processes.sort(Comparator.comparingInt(Process::getArrivalTime));

        while (completed.size() < processes.size()) {
            Process shortest = null;
            for (Process p : processes) {
                if (p.getArrivalTime() <= time && p.getCompletionTime() == -1) {
                    if (shortest == null || p.getBurstTime() < shortest.getBurstTime()) {
                        shortest = p;
                    }
                }
            }

            if (shortest == null) {
                time++;
                continue;
            }

            int startTime = time;
            time += shortest.getBurstTime();
            shortest.setCompletionTime(time);
            shortest.setTurnAroundTime(time - shortest.getArrivalTime());
            shortest.setWaitingTime(shortest.getTurnAroundTime() - shortest.getBurstTime());

            ganttChart.add(new GanttSegment(shortest.getPid(), startTime, time));
            completed.add(shortest);
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
        for(Process p: processesList) {
            total += p.getTurnAroundTime();
        }
        return total;
    }

    public static int getTotalWaitingTime() {
        int total = 0;
        for(Process p: processesList) {
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