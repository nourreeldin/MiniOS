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
        System.out.println("Enter process details: Arrival Time, Burst Time, Number of Pages");
        System.out.println("Type 'done' when finished\n");

        int processCount = processList.getSize() + 1;
        while (true) {
            System.out.print(Terminal.WHITE_BOLD + "Process " + processCount + " >> " + Terminal.RESET);
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("done")) break;

            try {
                String[] parts = input.split("\\s+");
                if (parts.length < 2 || parts.length > 3) {
                    System.out.println(Terminal.RED + "Error: Enter 2 or 3 values (AT BT [Pages])" + Terminal.RESET);
                    continue;
                }
                int arrivalTime = Integer.parseInt(parts[0]);
                int burstTime   = Integer.parseInt(parts[1]);
                int pages       = parts.length == 3 ? Integer.parseInt(parts[2]) : 4;

                if (arrivalTime < 0 || burstTime <= 0 || pages <= 0) {
                    System.out.println(Terminal.RED + "Error: AT>=0, BT>0, Pages>0" + Terminal.RESET);
                    continue;
                }

                if (processList.getTotalPages() + pages > processList.getDiskSize()) {
                    System.out.println(Terminal.RED + "Error: Process needs " + pages + " disk blocks "
                            + " but disk only has " + (processList.getDiskSize() - processList.getTotalPages()) + " free blocks left." + Terminal.RESET);
                    System.out.println(Terminal.YELLOW + "Reduce the number of pages or increase disk size (sysparams)." + Terminal.RESET);
                    continue;
                }

                processList.addProcess(arrivalTime, burstTime, pages);
                int pid = processList.getSize() - 1;
                String blocksStr = processList.getProcess(pid).getPageToBlockMap().values().toString();
                System.out.printf(Terminal.GREEN + "✓ P%d added (AT:%d BT:%d Pages:%d DiskBlocks:%s)%n" + Terminal.RESET,
                        pid, arrivalTime, burstTime, pages, blocksStr);
                processCount++;

            } catch (NumberFormatException e) {
                System.out.println(Terminal.RED + "Error: Invalid input. Integers only." + Terminal.RESET);
            }
        }

        if (processList.getSize() > 0) {
            System.out.println(Terminal.GREEN + "\n✓ Total processes: " + processList.getSize() + Terminal.RESET);
            displayProcessTable();
        } else {
            System.out.println(Terminal.YELLOW + "No processes were added." + Terminal.RESET);
        }
    }

    public void setSystemParameters() {
        System.out.println(Terminal.CYAN + "\n=== SYSTEM PARAMETERS ===" + Terminal.RESET);
        ProcessList pl = ProcessList.getInstance();

        int memorySize = pl.getMemorySize();
        System.out.print("Memory size (total pages, current=" + pl.getMemorySize() + "): ");
        try {
            String in = scanner.nextLine().trim();
            if (!in.isEmpty()) memorySize = Integer.parseInt(in);
        } catch (NumberFormatException e) {
            System.out.println(Terminal.RED + "Invalid, keeping current." + Terminal.RESET);
        }

        int frameSize = pl.getFrameSize();
        System.out.print("Frame size (pages per frame, current=" + pl.getFrameSize() + "): ");
        try {
            String in = scanner.nextLine().trim();
            if (!in.isEmpty()) frameSize = Integer.parseInt(in);
        } catch (NumberFormatException e) {
            System.out.println(Terminal.RED + "Invalid, keeping current." + Terminal.RESET);
        }

        if (frameSize <= 0) {
            System.out.println(Terminal.RED + "Error: Frame size must be > 0. Keeping current values." + Terminal.RESET);
        } else if (memorySize % frameSize != 0) {
            System.out.println(Terminal.RED + "Error: Memory size (" + memorySize + ") is not divisible by frame size ("
                    + frameSize + "). " + memorySize + " mod " + frameSize + " = " + (memorySize % frameSize) + "." + Terminal.RESET);
            System.out.println(Terminal.YELLOW + "Parameters NOT applied. Please retry with a valid combination." + Terminal.RESET);
            return;
        } else {
            pl.setMemorySize(memorySize);
            pl.setFrameSize(frameSize);
        }

        System.out.print("Disk size (cylinders, current=" + pl.getDiskSize() + "): ");
        try {
            String in = scanner.nextLine().trim();
            if (!in.isEmpty()) {
                int ds = Integer.parseInt(in);
                if (ds <= 0) {
                    System.out.println(Terminal.RED + "Invalid disk size, keeping current." + Terminal.RESET);
                } else if (ds < pl.getTotalPages()) {
                    System.out.println(Terminal.RED + "Error: Disk size cannot be less than total pages currently in the system (" + pl.getTotalPages() + ")." + Terminal.RESET);
                } else {
                    pl.setDiskSize(ds);
                }
            }
        } catch (NumberFormatException e) {
            System.out.println(Terminal.RED + "Invalid, keeping current." + Terminal.RESET);
        }

        System.out.printf(Terminal.GREEN + "✓ Memory: %d pages | Frames: %d | Frame size: %d | Disk: %d cylinders%n" + Terminal.RESET,
                pl.getMemorySize(), pl.getNumberOfFrames(), pl.getFrameSize(), pl.getDiskSize());
    }

    public void addProcessCommand(String[] args) {
        if (args.length < 2) {
            System.out.println(Terminal.RED + "Usage: addprocess <AT> <BT> [Pages]" + Terminal.RESET);
            return;
        }
        try {
            int at    = Integer.parseInt(args[0]);
            int bt    = Integer.parseInt(args[1]);
            int pages = args.length >= 3 ? Integer.parseInt(args[2]) : 4;

            if (at < 0 || bt <= 0 || pages <= 0) {
                System.out.println(Terminal.RED + "Error: AT>=0, BT>0, Pages>0" + Terminal.RESET);
                return;
            }

            if (processList.getTotalPages() + pages > processList.getDiskSize()) {
                System.out.println(Terminal.RED + "Error: Process needs " + pages + " disk blocks "
                        + " but disk only has " + (processList.getDiskSize() - processList.getTotalPages()) + " free blocks left." + Terminal.RESET);
                return;
            }

            processList.addProcess(at, bt, pages);
            int pid = processList.getSize() - 1;
            String blocksStr = processList.getProcess(pid).getPageToBlockMap().values().toString();
            System.out.printf(Terminal.GREEN + "✓ P%d added (AT:%d BT:%d Pages:%d DiskBlocks:%s)%n" + Terminal.RESET,
                    pid, at, bt, pages, blocksStr);
        } catch (NumberFormatException e) {
            System.out.println(Terminal.RED + "Error: Integers only." + Terminal.RESET);
        }
    }

    public void listProcesses() {
        if (processList.getSize() == 0) {
            System.out.println(Terminal.YELLOW + "No processes." + Terminal.RESET);
            return;
        }
        displayProcessTable();
    }

    public void clearProcesses() {
        processList.clearProcesses();
        System.out.println(Terminal.GREEN + "✓ All processes cleared." + Terminal.RESET);
    }

    private void displayProcessTable() {
        System.out.println(Terminal.CYAN + "\n┌────┬──────┬─────┬───────┬─────────────┐" + Terminal.RESET);
        System.out.println(Terminal.CYAN + "│PID │  AT  │  BT │ Pages │ Disk Blocks │" + Terminal.RESET);
        System.out.println(Terminal.CYAN + "├────┼──────┼─────┼───────┼─────────────┤" + Terminal.RESET);
        for (var p : processList.getProcesses()) {
            String blocks = p.getPageToBlockMap().values().toString();
            if (blocks.length() > 12) blocks = blocks.substring(0, 9) + "...";
            System.out.printf(Terminal.WHITE_BOLD + "│%-4d│  %-4d│  %-3d│  %-5d│ %-12s│%n" + Terminal.RESET,
                    p.getPid(), p.getArrivalTime(), p.getBurstTime(), p.getNumberOfPages(), blocks);
        }
        System.out.println(Terminal.CYAN + "└────┴──────┴─────┴───────┴─────────────┘" + Terminal.RESET);
        System.out.printf(Terminal.GREEN + "Total: %d processes | Frames: %d | Disk: 0-%d%n" + Terminal.RESET,
                processList.getSize(), processList.getNumberOfFrames(), processList.getDiskSize() - 1);
    }
}