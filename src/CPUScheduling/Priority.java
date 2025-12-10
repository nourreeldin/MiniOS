package CPUScheduling;

import Model.Process;
import java.util.ArrayList;
import java.util.Comparator;

public class Priority {
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

    public static void applyNonPreemptive(ArrayList<Process> list) {
        int time = 0;
        ganttChart = new ArrayList<>();
        ArrayList<Process> completed = new ArrayList<>();
        ArrayList<Process> processes = new ArrayList<>(list);

        for (Process p : processes)
            p.setCompletionTime(-1);

        processes.sort(Comparator.comparingInt(Process::getArrivalTime));

        while (completed.size() < processes.size()) {
            Process highestPriority = null;

            for (Process p : processes) {
                if (p.getArrivalTime() <= time && p.getCompletionTime() == -1) {
                    if (highestPriority == null || p.getPriority() < highestPriority.getPriority()) {
                        highestPriority = p;
                    }
                }
            }

            if (highestPriority == null) {
                time++;
                continue;
            }

            int startTime = time;
            time += highestPriority.getBurstTime();
            highestPriority.setCompletionTime(time);
            highestPriority.setTurnAroundTime(time - highestPriority.getArrivalTime());
            highestPriority.setWaitingTime(highestPriority.getTurnAroundTime() - highestPriority.getBurstTime());

            ganttChart.add(new GanttSegment(highestPriority.getPid(), startTime, time));
            completed.add(highestPriority);
        }

        processesList = processes;
    }

    public static void applyPreemptive(ArrayList<Process> list) {
        int time = 0;
        ganttChart = new ArrayList<>();
        ArrayList<Process> processes = new ArrayList<>(list);
        int[] remainingBurst = new int[processes.size()];

        for (int i = 0; i < processes.size(); i++) {
            remainingBurst[i] = processes.get(i).getBurstTime();
            processes.get(i).setCompletionTime(-1);
        }

        processes.sort(Comparator.comparingInt(Process::getArrivalTime));
        int completed = 0;
        int lastPid = -1;
        int segmentStart = 0;

        while (completed < processes.size()) {
            Process highestPriority = null;
            int highestIdx = -1;

            for (int i = 0; i < processes.size(); i++) {
                Process p = processes.get(i);
                if (p.getArrivalTime() <= time && remainingBurst[i] > 0) {
                    if (highestPriority == null || p.getPriority() < highestPriority.getPriority()) {
                        highestPriority = p;
                        highestIdx = i;
                    }
                }
            }

            if (highestPriority == null) {
                time++;
                continue;
            }

            if (lastPid != highestPriority.getPid()) {
                if (lastPid != -1) {
                    ganttChart.add(new GanttSegment(lastPid, segmentStart, time));
                }
                segmentStart = time;
                lastPid = highestPriority.getPid();
            }

            time++;
            remainingBurst[highestIdx]--;

            if (remainingBurst[highestIdx] == 0) {
                highestPriority.setCompletionTime(time);
                highestPriority.setTurnAroundTime(time - highestPriority.getArrivalTime());
                highestPriority.setWaitingTime(highestPriority.getTurnAroundTime() - highestPriority.getBurstTime());
                completed++;
            }
        }

        if (lastPid != -1) {
            ganttChart.add(new GanttSegment(lastPid, segmentStart, time));
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