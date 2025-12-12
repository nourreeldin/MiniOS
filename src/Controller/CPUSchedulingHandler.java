package Controller;

import CPUScheduling.*;
import Model.Process;
import Model.ProcessList;
import UI.Terminal;
import java.util.ArrayList;
import java.util.Scanner;

public class CPUSchedulingHandler {

    private final ProcessList processList;
    private final Scanner scanner;
    private int timeQuantum = 2;

    public CPUSchedulingHandler(Scanner scanner) {
        this.processList = ProcessList.getInstance();
        this.scanner = scanner;
    }

    public void showSchedulingMenu() {
        if (processList.getSize() == 0) {
            System.out.println(Terminal.RED + "Error: No processes available. Please add processes first." + Terminal.RESET);
            return;
        }

        boolean continueScheduling = true;
        while (continueScheduling) {
            System.out.println(Terminal.PURPLE + "\n=== CPU SCHEDULING ALGORITHMS ===" + Terminal.RESET);
            System.out.println("1. Shortest Job First (SJF)");
            System.out.println("2. Priority Non-Preemptive");
            System.out.println("3. Priority Preemptive");
            System.out.println("4. Round Robin");
            System.out.println("5. Back");

            System.out.print(Terminal.WHITE_BOLD + "Select algorithm: " + Terminal.RESET);
            String input = scanner.nextLine().trim();

            try {
                int choice = Integer.parseInt(input);
                switch (choice) {
                    case 1:
                        runSJF();
                        break;
                    case 2:
                        runPriorityNonPreemptive();
                        break;
                    case 3:
                        runPriorityPreemptive();
                        break;
                    case 4:
                        runRoundRobin();
                        break;
                    case 5:
                        continueScheduling = false;
                        break;
                    default:
                        System.out.println(Terminal.RED + "Invalid option!" + Terminal.RESET);
                }
            } catch (NumberFormatException e) {
                System.out.println(Terminal.RED + "Invalid input!" + Terminal.RESET);
            }
        }
    }

    public void runSJFCommand() {
        if (processList.getSize() == 0) {
            System.out.println(Terminal.RED + "Error: No processes available." + Terminal.RESET);
            return;
        }
        runSJF();
    }

    public void runPriorityNPCommand() {
        if (processList.getSize() == 0) {
            System.out.println(Terminal.RED + "Error: No processes available." + Terminal.RESET);
            return;
        }
        if (!validatePriorities()) return;
        runPriorityNonPreemptive();
    }

    public void runPriorityPCommand() {
        if (processList.getSize() == 0) {
            System.out.println(Terminal.RED + "Error: No processes available." + Terminal.RESET);
            return;
        }
        if (!validatePriorities()) return;
        runPriorityPreemptive();
    }

    public void runRRCommand() {
        if (processList.getSize() == 0) {
            System.out.println(Terminal.RED + "Error: No processes available." + Terminal.RESET);
            return;
        }
        ArrayList<Process> processes = new ArrayList<>(processList.getProcesses());
        RoundRobin.apply(processes, timeQuantum);
        displayResults("Round Robin (Q=" + timeQuantum + ")",
                RoundRobin.getProcessesList(),
                RoundRobin.getGanttChart(),
                RoundRobin.getAverageTurnAroundTime(),
                RoundRobin.getAverageWaitingTime(),
                RoundRobin.getCPUUtilization());
    }

    public void setTimeQuantum(int quantum) {
        if (quantum <= 0) {
            System.out.println(Terminal.RED + "Error: Time quantum must be positive!" + Terminal.RESET);
            return;
        }
        this.timeQuantum = quantum;
        System.out.println(Terminal.GREEN + "✓ Time quantum set to: " + quantum + Terminal.RESET);
    }

    private void runSJF() {
        System.out.println(Terminal.CYAN + "\n>> Running Shortest Job First (SJF)..." + Terminal.RESET);

        ArrayList<Process> processes = new ArrayList<>(processList.getProcesses());
        SJF.apply(processes);

        displayResults("Shortest Job First (SJF)",
                SJF.getProcessesList(),
                SJF.getGanttChart(),
                SJF.getAverageTurnAroundTime(),
                SJF.getAverageWaitingTime(),
                SJF.getCPUUtilization());
    }

    private void runPriorityNonPreemptive() {
        if (!validatePriorities()) return;

        System.out.println(Terminal.CYAN + "\n>> Running Priority Non-Preemptive..." + Terminal.RESET);

        ArrayList<Process> processes = new ArrayList<>(processList.getProcesses());
        Priority.applyNonPreemptive(processes);

        displayResults("Priority Non-Preemptive",
                Priority.getProcessesList(),
                Priority.getGanttChart(),
                Priority.getAverageTurnAroundTime(),
                Priority.getAverageWaitingTime(),
                Priority.getCPUUtilization());
    }

    private void runPriorityPreemptive() {
        if (!validatePriorities()) return;

        System.out.println(Terminal.CYAN + "\n>> Running Priority Preemptive..." + Terminal.RESET);

        ArrayList<Process> processes = new ArrayList<>(processList.getProcesses());
        Priority.applyPreemptive(processes);

        displayResults("Priority Preemptive",
                Priority.getProcessesList(),
                Priority.getGanttChart(),
                Priority.getAverageTurnAroundTime(),
                Priority.getAverageWaitingTime(),
                Priority.getCPUUtilization());
    }

    private void runRoundRobin() {
        System.out.print(Terminal.WHITE_BOLD + "Enter Time Quantum: " + Terminal.RESET);
        String input = scanner.nextLine().trim();

        try {
            int quantum = Integer.parseInt(input);
            if (quantum <= 0) {
                System.out.println(Terminal.RED + "Error: Time quantum must be positive!" + Terminal.RESET);
                return;
            }

            System.out.println(Terminal.CYAN + "\n>> Running Round Robin (Quantum = " + quantum + ")..." + Terminal.RESET);

            ArrayList<Process> processes = new ArrayList<>(processList.getProcesses());
            RoundRobin.apply(processes, quantum);

            displayResults("Round Robin (Q=" + quantum + ")",
                    RoundRobin.getProcessesList(),
                    RoundRobin.getGanttChart(),
                    RoundRobin.getAverageTurnAroundTime(),
                    RoundRobin.getAverageWaitingTime(),
                    RoundRobin.getCPUUtilization());

        } catch (NumberFormatException e) {
            System.out.println(Terminal.RED + "Error: Invalid time quantum!" + Terminal.RESET);
        }
    }

    private boolean validatePriorities() {
        for (Process p : processList.getProcesses()) {
            if (p.getPriority() == 0) {
                System.out.println(Terminal.RED + "Error: Please set priorities for all processes first!" + Terminal.RESET);
                System.out.println(Terminal.YELLOW + "Hint: Use 'setpriority' command or GUI to set priorities" + Terminal.RESET);
                return false;
            }
        }
        return true;
    }

    private void displayResults(String algorithmName, ArrayList<Process> processes,
                                ArrayList<?> ganttChart, double avgTAT, double avgWT, double cpuUtil) {
        System.out.println(Terminal.GREEN + "\n========================================" + Terminal.RESET);
        System.out.println(Terminal.WHITE_BOLD + "      " + algorithmName + Terminal.RESET);
        System.out.println(Terminal.GREEN + "========================================" + Terminal.RESET);

        displayGanttChart(ganttChart);

        displayProcessTable(processes);

        System.out.println(Terminal.CYAN + "\n┌─────────────────────────────────────┐" + Terminal.RESET);
        System.out.println(Terminal.CYAN + "│         PERFORMANCE METRICS         │" + Terminal.RESET);
        System.out.println(Terminal.CYAN + "├─────────────────────────────────────┤" + Terminal.RESET);
        System.out.printf(Terminal.WHITE_BOLD + "│ " + Terminal.RESET + "Average Turnaround Time: " + Terminal.GREEN + "%-9.2f" + Terminal.RESET + "  │\n", avgTAT);
        System.out.printf(Terminal.WHITE_BOLD + "│ " + Terminal.RESET + "Average Waiting Time:    " + Terminal.GREEN + "%-9.2f" + Terminal.RESET + "  │\n", avgWT);
        System.out.printf(Terminal.WHITE_BOLD + "│ " + Terminal.RESET + "CPU Utilization:         " + Terminal.GREEN + "%-5.2f%%" + Terminal.RESET + "    │\n", cpuUtil);
        System.out.println(Terminal.CYAN + "└─────────────────────────────────────┘" + Terminal.RESET);
    }

    private void displayGanttChart(ArrayList<?> ganttChart) {
        System.out.println(Terminal.YELLOW + "\n╔════════════════════════════════════════╗" + Terminal.RESET);
        System.out.println(Terminal.YELLOW + "║            GANTT CHART                 ║" + Terminal.RESET);
        System.out.println(Terminal.YELLOW + "╚════════════════════════════════════════╝" + Terminal.RESET);

        if (ganttChart.isEmpty()) {
            System.out.println(Terminal.RED + "No Gantt chart data available." + Terminal.RESET);
            return;
        }

        System.out.print("  ");
        for (Object obj : ganttChart) {
            System.out.print("┌────");
        }
        System.out.println("┐");

        System.out.print("  ");
        for (Object obj : ganttChart) {
            int pid = extractPid(obj);
            System.out.printf("│ P%-2d", pid);
        }
        System.out.println("│");

        System.out.print("  ");
        for (Object obj : ganttChart) {
            System.out.print("└────");
        }
        System.out.println("┘");

        System.out.print("  ");
        for (Object obj : ganttChart) {
            int start = extractStartTime(obj);
            System.out.printf("%-5d", start);
        }

        Object lastSegment = ganttChart.get(ganttChart.size() - 1);
        int endTime = extractEndTime(lastSegment);
        System.out.println(endTime);
    }

    private void displayProcessTable(ArrayList<Process> processes) {
        System.out.println(Terminal.CYAN + "\n┌─────┬───────────┬────────────┬─────────────┬──────────────┬──────────────┐" + Terminal.RESET);
        System.out.println(Terminal.CYAN + "│ PID │  Arrival  │ Burst Time │ Completion  │  Turnaround  │ Waiting Time │" + Terminal.RESET);
        System.out.println(Terminal.CYAN + "├─────┼───────────┼────────────┼─────────────┼──────────────┼──────────────┤" + Terminal.RESET);

        for (Process p : processes) {
            System.out.printf(Terminal.WHITE_BOLD + "│ %-3d │    %-6d │     %-6d │     %-7d │      %-7d │      %-7d │\n" + Terminal.RESET,
                    p.getPid(),
                    p.getArrivalTime(),
                    p.getBurstTime(),
                    p.getCompletionTime(),
                    p.getTurnAroundTime(),
                    p.getWaitingTime());
        }

        System.out.println(Terminal.CYAN + "└─────┴───────────┴────────────┴─────────────┴──────────────┴──────────────┘" + Terminal.RESET);
    }

    private int extractPid(Object obj) {
        if (obj instanceof SJF.GanttSegment) return ((SJF.GanttSegment) obj).pid;
        if (obj instanceof Priority.GanttSegment) return ((Priority.GanttSegment) obj).pid;
        if (obj instanceof RoundRobin.GanttSegment) return ((RoundRobin.GanttSegment) obj).pid;
        return -1;
    }

    private int extractStartTime(Object obj) {
        if (obj instanceof SJF.GanttSegment) return ((SJF.GanttSegment) obj).startTime;
        if (obj instanceof Priority.GanttSegment) return ((Priority.GanttSegment) obj).startTime;
        if (obj instanceof RoundRobin.GanttSegment) return ((RoundRobin.GanttSegment) obj).startTime;
        return -1;
    }

    private int extractEndTime(Object obj) {
        if (obj instanceof SJF.GanttSegment) return ((SJF.GanttSegment) obj).endTime;
        if (obj instanceof Priority.GanttSegment) return ((Priority.GanttSegment) obj).endTime;
        if (obj instanceof RoundRobin.GanttSegment) return ((RoundRobin.GanttSegment) obj).endTime;
        return -1;
    }

    public void setPrioritiesInteractive() {
        if (processList.getSize() == 0) {
            System.out.println(Terminal.RED + "Error: No processes available." + Terminal.RESET);
            return;
        }

        System.out.println(Terminal.CYAN + "\n=== SET PROCESS PRIORITIES ===" + Terminal.RESET);
        System.out.println(Terminal.YELLOW + "Note: Lower number = Higher priority" + Terminal.RESET);

        for (Process p : processList.getProcesses()) {
            System.out.print(Terminal.WHITE_BOLD + "Priority for Process " + p.getPid() + ": " + Terminal.RESET);
            String input = scanner.nextLine().trim();

            try {
                int priority = Integer.parseInt(input);
                if (priority < 0) {
                    System.out.println(Terminal.RED + "Error: Priority must be non-negative!" + Terminal.RESET);
                    return;
                }
                p.setPriority(priority);
            } catch (NumberFormatException e) {
                System.out.println(Terminal.RED + "Error: Invalid priority value!" + Terminal.RESET);
                return;
            }
        }

        System.out.println(Terminal.GREEN + "\n✓ Priorities set successfully!" + Terminal.RESET);
    }
}