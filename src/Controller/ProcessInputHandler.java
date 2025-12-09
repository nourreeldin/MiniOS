package Controller;

import Model.ProcessList;
import UI.Terminal;
import java.util.Scanner;

public class ProcessInputHandler {

    private final ProcessList processList;
    private final Scanner scanner;

    public ProcessInputHandler(Scanner scanner) {
        this.processList = ProcessList.getInstance();
        this.scanner = scanner;
    }

    public void inputProcessesInteractive() {
        System.out.println(Terminal.CYAN + "\n=== PROCESS INPUT ===" + Terminal.RESET);
        System.out.println("Enter process details (Arrival Time and Burst Time)");
        System.out.println("Type 'done' when finished\n");

        int processCount = 1;
        while (true) {
            System.out.print(Terminal.WHITE_BOLD + "Process " + processCount + " >> " + Terminal.RESET);
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("done")) {
                break;
            }

            try {
                String[] parts = input.split("\\s+");
                if (parts.length != 2) {
                    System.out.println(Terminal.RED + "Error: Please enter exactly 2 values (Arrival Time, Burst Time)" + Terminal.RESET);
                    continue;
                }

                int arrivalTime = Integer.parseInt(parts[0]);
                int burstTime = Integer.parseInt(parts[1]);

                if (arrivalTime < 0 || burstTime <= 0) {
                    System.out.println(Terminal.RED + "Error: Arrival time must be >= 0 and Burst time must be > 0" + Terminal.RESET);
                    continue;
                }

                processList.addProcess(arrivalTime, burstTime);
                System.out.println(Terminal.GREEN + "✓ Process " + processCount + " added successfully (AT: " + arrivalTime + ", BT: " + burstTime + ")" + Terminal.RESET);
                processCount++;

            } catch (NumberFormatException e) {
                System.out.println(Terminal.RED + "Error: Invalid input. Please enter integers only." + Terminal.RESET);
            }
        }

        if (processList.getSize() > 0) {
            System.out.println(Terminal.GREEN + "\n✓ Total processes added: " + processList.getSize() + Terminal.RESET);
            displayProcessTable();
        } else {
            System.out.println(Terminal.YELLOW + "No processes were added." + Terminal.RESET);
        }
    }

    public void addProcessCommand(String[] args) {
        if (args.length != 2) {
            System.out.println(Terminal.RED + "Usage: addprocess <arrival_time> <burst_time>" + Terminal.RESET);
            return;
        }

        try {
            int arrivalTime = Integer.parseInt(args[0]);
            int burstTime = Integer.parseInt(args[1]);

            if (arrivalTime < 0 || burstTime <= 0) {
                System.out.println(Terminal.RED + "Error: Arrival time must be >= 0 and Burst time must be > 0" + Terminal.RESET);
                return;
            }

            processList.addProcess(arrivalTime, burstTime);
            int pid = processList.getSize() - 1;
            System.out.println(Terminal.GREEN + "✓ Process " + pid + " added (AT: " + arrivalTime + ", BT: " + burstTime + ")" + Terminal.RESET);

        } catch (NumberFormatException e) {
            System.out.println(Terminal.RED + "Error: Invalid input. Please enter integers only." + Terminal.RESET);
        }
    }

    public void listProcesses() {
        if (processList.getSize() == 0) {
            System.out.println(Terminal.YELLOW + "No processes in the system." + Terminal.RESET);
            return;
        }

        displayProcessTable();
    }

    public void clearProcesses() {
        processList.getProcesses().clear();
        System.out.println(Terminal.GREEN + "✓ All processes cleared." + Terminal.RESET);
    }

    private void displayProcessTable() {
        System.out.println(Terminal.CYAN + "\n┌─────────┬───────────────┬─────────────┐" + Terminal.RESET);
        System.out.println(Terminal.CYAN + "│   PID   │ Arrival Time  │ Burst Time  │" + Terminal.RESET);
        System.out.println(Terminal.CYAN + "├─────────┼───────────────┼─────────────┤" + Terminal.RESET);

        for (int i = 0; i < processList.getSize(); i++) {
            var process = processList.getProcesses().get(i);
            System.out.printf(Terminal.WHITE_BOLD + "│   %-5d │      %-8d │     %-7d │\n" + Terminal.RESET,
                    process.getPid(),
                    process.getArrivalTime(),
                    process.getBurstTime());
        }

        System.out.println(Terminal.CYAN + "└─────────┴───────────────┴─────────────┘" + Terminal.RESET);
        System.out.println(Terminal.GREEN + "Total Processes: " + processList.getSize() + Terminal.RESET);
    }
}