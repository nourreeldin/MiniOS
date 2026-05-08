package Controller;

import DiskScheduling.*;
import Model.ProcessList;
import UI.Terminal;

import java.util.*;

public class DiskSchedulingHandler {

    public static class DiskResult {
        public List<Integer> seekSequence = new ArrayList<>();
        public int totalSeekDistance;
        public double avgSeekTime;
        public String algorithm;
        public int initialHead;
    }

    public static DiskResult run(String algorithm, int[] requests, int head, String direction, int diskSize) {
        DiskResult r = new DiskResult();
        r.algorithm   = algorithm;
        r.initialHead = head;

        List<Integer> seq;
        switch (algorithm.toUpperCase()) {
            case "FCFS":  seq = FCFS.run(requests, head);                       break;
            case "SSTF":  seq = SSTF.run(requests, head);                       break;
            case "SCAN":  seq = SCAN.run(requests, head, direction, diskSize);  break;
            case "CSCAN": seq = CSCAN.run(requests, head, direction, diskSize); break;
            case "LOOK":  seq = LOOK.run(requests, head, direction);            break;
            case "CLOOK": seq = CLOOK.run(requests, head, direction);           break;
            default:      seq = FCFS.run(requests, head);                       break;
        }
        r.seekSequence = seq;

        int total = 0;
        for (int i = 1; i < seq.size(); i++) total += Math.abs(seq.get(i) - seq.get(i - 1));
        r.totalSeekDistance = total;
        r.avgSeekTime = requests.length > 0 ? (double) total / requests.length : 0;
        return r;
    }

    public static void printResult(DiskResult r) {
        System.out.println(Terminal.CYAN + "\n=== DISK SCHEDULING: " + r.algorithm + " ===" + Terminal.RESET);
        System.out.print(Terminal.WHITE_BOLD + "Head Movement: " + Terminal.RESET);
        for (int i = 0; i < r.seekSequence.size(); i++) {
            System.out.print(r.seekSequence.get(i));
            if (i < r.seekSequence.size() - 1) System.out.print(" -> ");
        }
        System.out.println();
        System.out.printf(Terminal.GREEN + "Total Seek Distance: %d | Avg Seek Time: %.2f%n" + Terminal.RESET,
                r.totalSeekDistance, r.avgSeekTime);
    }

    public void showMenu(java.util.Scanner scanner) {
        ProcessList pl = ProcessList.getInstance();
        System.out.println(Terminal.CYAN + "\n=== DISK SCHEDULING ===" + Terminal.RESET);
        System.out.println("Disk size: 0 - " + (pl.getDiskSize() - 1));

        boolean keepGoing = true;
        while (keepGoing) {

            int head = -1;
            while (head < 0) {
                System.out.print("Enter initial head position (0-" + (pl.getDiskSize() - 1) + "): ");
                try {
                    head = Integer.parseInt(scanner.nextLine().trim());
                    if (head < 0 || head >= pl.getDiskSize()) {
                        System.out.println(Terminal.RED + "Head out of range 0-" + (pl.getDiskSize() - 1) + ". Try again." + Terminal.RESET);
                        head = -1;
                    }
                } catch (Exception e) {
                    System.out.println(Terminal.RED + "Invalid input. Enter a number." + Terminal.RESET);
                }
            }

            int[] requests = null;
            while (requests == null) {
                System.out.print("Enter request queue (space-separated, range 0-" + (pl.getDiskSize() - 1) + "): ");
                String reqStr = scanner.nextLine().trim();
                try {
                    String[] parts = reqStr.split("\\s+");
                    int[] tmp = new int[parts.length];
                    boolean valid = true;
                    for (int i = 0; i < parts.length; i++) {
                        tmp[i] = Integer.parseInt(parts[i]);
                        if (tmp[i] < 0 || tmp[i] >= pl.getDiskSize()) {
                            System.out.println(Terminal.RED + "Request " + tmp[i] + " out of disk range 0-"
                                    + (pl.getDiskSize() - 1) + ". Try again." + Terminal.RESET);
                            valid = false;
                            break;
                        }
                    }
                    if (valid) requests = tmp;
                } catch (Exception e) {
                    System.out.println(Terminal.RED + "Invalid request queue. Enter space-separated integers." + Terminal.RESET);
                }
            }

            String alg = null;
            while (alg == null) {
                System.out.println("Algorithms: 1.FCFS  2.SSTF  3.SCAN  4.CSCAN  5.LOOK  6.CLOOK");
                System.out.print("Choose: ");
                try {
                    int choice = Integer.parseInt(scanner.nextLine().trim());
                    String[] algs = {"FCFS", "SSTF", "SCAN", "CSCAN", "LOOK", "CLOOK"};
                    if (choice < 1 || choice > algs.length) throw new Exception();
                    alg = algs[choice - 1];
                } catch (Exception e) {
                    System.out.println(Terminal.RED + "Invalid choice. Enter 1-6." + Terminal.RESET);
                }
            }

            String direction = "right";
            if (alg.equals("SCAN") || alg.equals("CSCAN") || alg.equals("LOOK") || alg.equals("CLOOK")) {
                System.out.print("Direction (left/right): ");
                direction = scanner.nextLine().trim().toLowerCase();
                if (!direction.equals("left") && !direction.equals("right")) direction = "right";
            }

            DiskResult result = run(alg, requests, head, direction, pl.getDiskSize());
            printResult(result);

            System.out.print("\n(t)ry again or (b)ack to menu: ");
            String choice = scanner.nextLine().trim().toLowerCase();
            if (!choice.equals("t")) keepGoing = false;
            else System.out.println(Terminal.CYAN + "\n=== DISK SCHEDULING ===" + Terminal.RESET);
        }
    }
}