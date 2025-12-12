package Controller;

import MemoryManagement.Paging;
import MemoryManagement.Segmentation;
import Model.Process;
import Model.ProcessList;
import UI.Terminal;
import java.util.Scanner;

public class MemoryManagementHandler {

    private final Scanner scanner;
    private final ProcessList processList;
    private Paging pagingSystem;
    private Segmentation segmentationSystem;

    public MemoryManagementHandler(Scanner scanner) {
        this.scanner = scanner;
        this.processList = ProcessList.getInstance();
    }

    public void showMemoryMenu() {
        boolean continueMemory = true;
        while (continueMemory) {
            System.out.println(Terminal.YELLOW + "\n=== MEMORY MANAGEMENT ===" + Terminal.RESET);
            System.out.println("1. Paging");
            System.out.println("2. Segmentation");
            System.out.println("3. Back");

            System.out.print(Terminal.WHITE_BOLD + "Select option: " + Terminal.RESET);
            String input = scanner.nextLine().trim();

            try {
                int choice = Integer.parseInt(input);
                switch (choice) {
                    case 1:
                        handlePaging();
                        break;
                    case 2:
                        handleSegmentation();
                        break;
                    case 3:
                        continueMemory = false;
                        break;
                    default:
                        System.out.println(Terminal.RED + "Invalid option!" + Terminal.RESET);
                }
            } catch (NumberFormatException e) {
                System.out.println(Terminal.RED + "Invalid input!" + Terminal.RESET);
            }
        }
    }

    private void handlePaging() {
        if (processList.getSize() == 0) {
            System.out.println(Terminal.RED + "Error: No processes available. Please add processes first." + Terminal.RESET);
            return;
        }

        System.out.println(Terminal.CYAN + "\n>> Paging System Setup" + Terminal.RESET);

        try {
            System.out.print(Terminal.WHITE_BOLD + "Enter Total Physical Memory Size: " + Terminal.RESET);
            int totalMemory = Integer.parseInt(scanner.nextLine().trim());

            System.out.print(Terminal.WHITE_BOLD + "Enter Frame Size: " + Terminal.RESET);
            int frameSize = Integer.parseInt(scanner.nextLine().trim());

            if (totalMemory <= 0 || frameSize <= 0 || totalMemory % frameSize != 0) {
                System.out.println(Terminal.RED + "Error: Invalid memory or frame size!" + Terminal.RESET);
                return;
            }

            pagingSystem = new Paging(totalMemory, frameSize);

            System.out.print(Terminal.WHITE_BOLD + "Set memory sizes for processes? (y/n): " + Terminal.RESET);
            boolean setMemorySizes = scanner.nextLine().trim().equalsIgnoreCase("y");

            if (setMemorySizes) {
                System.out.println(Terminal.CYAN + "\n>> Enter memory size for each process:" + Terminal.RESET);
                for (Process p : processList.getProcesses()) {
                    System.out.print(Terminal.WHITE_BOLD + "Process " + p.getPid() + " memory size: " + Terminal.RESET);
                    int size = Integer.parseInt(scanner.nextLine().trim());
                    if (size <= 0) {
                        System.out.println(Terminal.RED + "Error: Memory size must be positive!" + Terminal.RESET);
                        return;
                    }
                    p.setMemorySize(size);
                }
            } else {
                System.out.println(Terminal.YELLOW + "Using default memory sizes (burst_time * 10)" + Terminal.RESET);
                for (Process p : processList.getProcesses()) {
                    p.setMemorySize(p.getBurstTime() * 10);
                }
            }

            System.out.println(Terminal.CYAN + "\n>> Allocating memory for processes..." + Terminal.RESET);
            for (Process p : processList.getProcesses()) {
                Paging.ProcessInfo processInfo = new Paging.ProcessInfo(p.getPid(), p.getMemorySize(), frameSize);

                if (!pagingSystem.allocateProcess(processInfo)) {
                    System.out.println(Terminal.RED + "X Process " + p.getPid() + " - Not enough memory" + Terminal.RESET);
                } else {
                    System.out.println(Terminal.GREEN + "> Process " + p.getPid() + " allocated successfully (Size: " + p.getMemorySize() + ")" + Terminal.RESET);
                }
            }

            displayPagingResults(pagingSystem);

            System.out.print(Terminal.WHITE_BOLD + "\nTranslate address? (y/n): " + Terminal.RESET);
            if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                translatePagingAddress(pagingSystem);
            }

        } catch (NumberFormatException e) {
            System.out.println(Terminal.RED + "Error: Invalid input!" + Terminal.RESET);
        }
    }

    private void handleSegmentation() {
        if (processList.getSize() == 0) {
            System.out.println(Terminal.RED + "Error: No processes available. Please add processes first." + Terminal.RESET);
            return;
        }

        System.out.println(Terminal.CYAN + "\n>> Segmentation System Setup" + Terminal.RESET);

        try {
            System.out.print(Terminal.WHITE_BOLD + "Enter Total Physical Memory Size: " + Terminal.RESET);
            int totalMemory = Integer.parseInt(scanner.nextLine().trim());

            if (totalMemory <= 0) {
                System.out.println(Terminal.RED + "Error: Invalid memory size!" + Terminal.RESET);
                return;
            }

            segmentationSystem = new Segmentation(totalMemory);

            System.out.println(Terminal.CYAN + "\n>> Define segments for each process:" + Terminal.RESET);

            for (Process p : processList.getProcesses()) {
                System.out.println(Terminal.YELLOW + "\nProcess " + p.getPid() + ":" + Terminal.RESET);
                System.out.print(Terminal.WHITE_BOLD + "  Number of Segments: " + Terminal.RESET);
                int numSegments = Integer.parseInt(scanner.nextLine().trim());

                if (numSegments <= 0) {
                    System.out.println(Terminal.RED + "Error: Number of segments must be positive!" + Terminal.RESET);
                    return;
                }

                Segmentation.ProcessInfo processInfo = new Segmentation.ProcessInfo(p.getPid());

                for (int j = 0; j < numSegments; j++) {
                    System.out.println(Terminal.CYAN + "    Segment " + j + ":" + Terminal.RESET);
                    System.out.print(Terminal.WHITE_BOLD + "      Name: " + Terminal.RESET);
                    String name = scanner.nextLine().trim();

                    System.out.print(Terminal.WHITE_BOLD + "      Size: " + Terminal.RESET);
                    int size = Integer.parseInt(scanner.nextLine().trim());

                    if (size <= 0) {
                        System.out.println(Terminal.RED + "Error: Segment size must be positive!" + Terminal.RESET);
                        return;
                    }

                    processInfo.addSegment(name, size);
                }

                if (!segmentationSystem.allocateProcess(processInfo)) {
                    System.out.println(Terminal.RED + "X Process " + p.getPid() + " - Not enough memory" + Terminal.RESET);
                } else {
                    System.out.println(Terminal.GREEN + "> Process " + p.getPid() + " allocated successfully" + Terminal.RESET);
                }
            }

            displaySegmentationResults(segmentationSystem);

            System.out.print(Terminal.WHITE_BOLD + "\nTranslate address? (y/n): " + Terminal.RESET);
            if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                translateSegmentationAddress(segmentationSystem);
            }

        } catch (NumberFormatException e) {
            System.out.println(Terminal.RED + "Error: Invalid input!" + Terminal.RESET);
        }
    }

    private void displayPagingResults(Paging paging) {
        System.out.println(Terminal.GREEN + "\n========================================" + Terminal.RESET);
        System.out.println(Terminal.WHITE_BOLD + "      PAGING - MEMORY LAYOUT" + Terminal.RESET);
        System.out.println(Terminal.GREEN + "========================================" + Terminal.RESET);

        System.out.println(Terminal.YELLOW + "\n======= FRAME TABLE =======" + Terminal.RESET);

        System.out.println(Terminal.CYAN + "+----------+--------------------------+" + Terminal.RESET);
        System.out.println(Terminal.CYAN + "|  Frame   |         Status           |" + Terminal.RESET);
        System.out.println(Terminal.CYAN + "+----------+--------------------------+" + Terminal.RESET);

        for (int i = 0; i < paging.getTotalFrames(); i++) {
            Paging.FrameInfo frame = paging.getFrameTable().get(i);
            String status;
            if (frame.isFree) {
                status = "Free";
            } else {
                status = "P" + frame.processId + " Page " + frame.pageNumber;
            }
            System.out.printf(Terminal.WHITE_BOLD + "|   %-5d  |  %-23s |\n" + Terminal.RESET, i, status);
        }

        System.out.println(Terminal.CYAN + "+----------+--------------------------+" + Terminal.RESET);

        for (int pid : paging.getProcessPageTables().keySet()) {
            Paging.PageTable pageTable = paging.getProcessPageTables().get(pid);

            System.out.println(Terminal.YELLOW + "\n======= Process " + pid + " - Page Table =======" + Terminal.RESET);

            System.out.println(Terminal.CYAN + "+-----------+-----------+" + Terminal.RESET);
            System.out.println(Terminal.CYAN + "|   Page    |   Frame   |" + Terminal.RESET);
            System.out.println(Terminal.CYAN + "+-----------+-----------+" + Terminal.RESET);

            for (int page : pageTable.pageToFrame.keySet()) {
                int frame = pageTable.pageToFrame.get(page);
                System.out.printf(Terminal.WHITE_BOLD + "|    %-5d  |    %-5d  |\n" + Terminal.RESET, page, frame);
            }

            System.out.println(Terminal.CYAN + "+-----------+-----------+" + Terminal.RESET);
        }

        System.out.println(Terminal.CYAN + "\n+--------------------------------------+" + Terminal.RESET);
        System.out.println(Terminal.CYAN + "|         MEMORY STATISTICS            |" + Terminal.RESET);
        System.out.println(Terminal.CYAN + "+--------------------------------------+" + Terminal.RESET);
        System.out.printf(Terminal.WHITE_BOLD + "| " + Terminal.RESET + "Total Memory:    " + Terminal.GREEN + "%-15d" + Terminal.RESET + "     |\n", paging.getTotalMemory());
        System.out.printf(Terminal.WHITE_BOLD + "| " + Terminal.RESET + "Frame Size:      " + Terminal.GREEN + "%-15d" + Terminal.RESET + "     |\n", paging.getFrameSize());
        System.out.printf(Terminal.WHITE_BOLD + "| " + Terminal.RESET + "Total Frames:    " + Terminal.GREEN + "%-15d" + Terminal.RESET + "     |\n", paging.getTotalFrames());
        System.out.printf(Terminal.WHITE_BOLD + "| " + Terminal.RESET + "Free Frames:     " + Terminal.GREEN + "%-15d" + Terminal.RESET + "     |\n", paging.getFreeFramesCount());
        System.out.println(Terminal.CYAN + "+--------------------------------------+" + Terminal.RESET);
    }

    private void displaySegmentationResults(Segmentation segmentation) {
        System.out.println(Terminal.GREEN + "\n========================================" + Terminal.RESET);
        System.out.println(Terminal.WHITE_BOLD + "   SEGMENTATION - MEMORY LAYOUT" + Terminal.RESET);
        System.out.println(Terminal.GREEN + "========================================" + Terminal.RESET);

        for (int pid : segmentation.getProcessSegmentTables().keySet()) {
            Segmentation.SegmentTable segTable = segmentation.getProcessSegmentTables().get(pid);

            System.out.println(Terminal.YELLOW + "\n======= Process " + pid + " - Segment Table =======" + Terminal.RESET);

            System.out.println(Terminal.CYAN + "+----------+--------------+-----------+-----------+" + Terminal.RESET);
            System.out.println(Terminal.CYAN + "| Segment  |     Name     |   Base    |   Limit   |" + Terminal.RESET);
            System.out.println(Terminal.CYAN + "+----------+--------------+-----------+-----------+" + Terminal.RESET);

            for (int i = 0; i < segTable.segments.size(); i++) {
                Segmentation.Segment seg = segTable.segments.get(i);
                System.out.printf(Terminal.WHITE_BOLD + "|    %-4d  |  %-11s |   %-6d  |   %-6d  |\n" + Terminal.RESET,
                        i, seg.name, seg.base, seg.limit);
            }

            System.out.println(Terminal.CYAN + "+----------+--------------+-----------+-----------+" + Terminal.RESET);
        }

        System.out.println(Terminal.CYAN + "\n+--------------------------------------+" + Terminal.RESET);
        System.out.println(Terminal.CYAN + "|         MEMORY STATISTICS            |" + Terminal.RESET);
        System.out.println(Terminal.CYAN + "+--------------------------------------+" + Terminal.RESET);
        System.out.printf(Terminal.WHITE_BOLD + "| " + Terminal.RESET + "Total Memory:    " + Terminal.GREEN + "%-15d" + Terminal.RESET + "     |\n", segmentation.getTotalMemory());
        System.out.printf(Terminal.WHITE_BOLD + "| " + Terminal.RESET + "Used Memory:     " + Terminal.GREEN + "%-15d" + Terminal.RESET + "     |\n", segmentation.getUsedMemory());
        System.out.printf(Terminal.WHITE_BOLD + "| " + Terminal.RESET + "Free Memory:     " + Terminal.GREEN + "%-15d" + Terminal.RESET + "     |\n", segmentation.getFreeMemory());
        System.out.println(Terminal.CYAN + "+--------------------------------------+" + Terminal.RESET);
    }

    private void translatePagingAddress(Paging paging) {
        try {
            System.out.print(Terminal.WHITE_BOLD + "Enter Process ID: " + Terminal.RESET);
            int pid = Integer.parseInt(scanner.nextLine().trim());

            System.out.print(Terminal.WHITE_BOLD + "Enter Page Number: " + Terminal.RESET);
            int pageNumber = Integer.parseInt(scanner.nextLine().trim());

            System.out.print(Terminal.WHITE_BOLD + "Enter Offset: " + Terminal.RESET);
            int offset = Integer.parseInt(scanner.nextLine().trim());

            Integer physicalAddress = paging.translateAddress(pid, pageNumber, offset);

            if (physicalAddress == null) {
                System.out.println(Terminal.RED + "Error: Invalid logical address!" + Terminal.RESET);
            } else {
                System.out.println(Terminal.GREEN + "\n> Address Translation:" + Terminal.RESET);
                System.out.println(Terminal.CYAN + "  Logical Address:  (Page=" + pageNumber + ", Offset=" + offset + ")" + Terminal.RESET);
                System.out.println(Terminal.GREEN + "  Physical Address: " + physicalAddress + Terminal.RESET);
            }
        } catch (NumberFormatException e) {
            System.out.println(Terminal.RED + "Error: Invalid input!" + Terminal.RESET);
        }
    }

    private void translateSegmentationAddress(Segmentation segmentation) {
        try {
            System.out.print(Terminal.WHITE_BOLD + "Enter Process ID: " + Terminal.RESET);
            int pid = Integer.parseInt(scanner.nextLine().trim());

            System.out.print(Terminal.WHITE_BOLD + "Enter Segment Number: " + Terminal.RESET);
            int segmentNumber = Integer.parseInt(scanner.nextLine().trim());

            System.out.print(Terminal.WHITE_BOLD + "Enter Offset: " + Terminal.RESET);
            int offset = Integer.parseInt(scanner.nextLine().trim());

            Integer physicalAddress = segmentation.translateAddress(pid, segmentNumber, offset);

            if (physicalAddress == null) {
                System.out.println(Terminal.RED + "Error: Invalid logical address or offset exceeds limit!" + Terminal.RESET);
            } else {
                System.out.println(Terminal.GREEN + "\n> Address Translation:" + Terminal.RESET);
                System.out.println(Terminal.CYAN + "  Logical Address:  (Segment=" + segmentNumber + ", Offset=" + offset + ")" + Terminal.RESET);
                System.out.println(Terminal.GREEN + "  Physical Address: " + physicalAddress + Terminal.RESET);
            }
        } catch (NumberFormatException e) {
            System.out.println(Terminal.RED + "Error: Invalid input!" + Terminal.RESET);
        }
    }
}