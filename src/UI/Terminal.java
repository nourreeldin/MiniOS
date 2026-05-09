package UI;

import java.util.Scanner;

import Controller.PageReplacementHandler;
import Controller.DiskSchedulingHandler;
import Controller.IntegrationHandler;
import Controller.CPUSchedulingHandler;
import Controller.ProcessInputHandler;
import Model.ProcessList;
import Scripts.Scripts;

public class Terminal {

    private Scanner scanner;
    private ProcessInputHandler     inputHandler;
    private CPUSchedulingHandler    schedulingHandler;
    private PageReplacementHandler  pageHandler;
    private DiskSchedulingHandler   diskHandler;
    private IntegrationHandler      integrationHandler;

    public static final String RESET      = "\033[0m";
    public static final String RED        = "\033[0;31m";
    public static final String GREEN      = "\033[0;32m";
    public static final String YELLOW     = "\033[0;33m";
    public static final String BLUE       = "\033[0;34m";
    public static final String PURPLE     = "\033[0;35m";
    public static final String CYAN       = "\033[0;36m";
    public static final String WHITE_BOLD = "\033[1;37m";

    public final ProcessList processList = ProcessList.getInstance();

    public Terminal() {
        this.scanner            = new Scanner(System.in);
        this.inputHandler       = new ProcessInputHandler(scanner);
        this.schedulingHandler  = new CPUSchedulingHandler(scanner);
        this.pageHandler        = new PageReplacementHandler();
        this.diskHandler        = new DiskSchedulingHandler();
        this.integrationHandler = new IntegrationHandler();
        printWelcomeScreen();
        startMenu();
    }

    private void printWelcomeScreen() {
        Scripts.clear();
        System.out.println(RED    + "  __  __ _       _  ____   _____ " + RESET);
        System.out.println(YELLOW + " |  \\/  (_)     (_)/ __ \\ / ____|" + RESET);
        System.out.println(GREEN  + " | \\  / |_ _ __  _| |  | | (___  " + RESET);
        System.out.println(CYAN   + " | |\\/| | | '_ \\| | |  | |\\___ \\ " + RESET);
        System.out.println(BLUE   + " | |  | | | | | | | |__| |____) |" + RESET);
        System.out.println(PURPLE + " |_|  |_|_|_| |_|_|\\____/|_____/ " + RESET);
        System.out.println("\n" + GREEN + "Welcome to the MiniOS Simulator." + RESET);
        System.out.println("Press " + WHITE_BOLD + "Enter" + RESET + " to initialize the system...");
        scanner.nextLine();
    }

    public void showOptions() {
        System.out.println(WHITE_BOLD + "--- MAIN MENU ---" + RESET);
        System.out.println("1. " + CYAN   + "Input Processes & System Setup" + RESET);
        System.out.println("2. " + PURPLE + "CPU Scheduling" + RESET);
        System.out.println("3. " + YELLOW + "Page Replacement" + RESET);
        System.out.println("4. " + BLUE   + "Disk Scheduling" + RESET);
        System.out.println("5. " + GREEN  + "Core Integration (Memory + Disk)" + RESET);
        System.out.println("6. " + CYAN   + "Open GUI" + RESET);
        System.out.println("7. " + BLUE   + "Use Commands (Shell Mode)" + RESET);
        System.out.println("8. " + RED    + "Exit" + RESET);
    }

    public void startMenu() {
        boolean exit = false;
        showOptions();
        while (!exit) {
            System.out.print("Select an option: ");
            String input = scanner.nextLine();
            try {
                int choice = Integer.parseInt(input);
                switch (choice) {
                    case 1:
                        System.out.println(CYAN + ">> Process Input & System Setup" + RESET);
                        inputHandler.inputProcessesInteractive();
                        System.out.println(CYAN + "\n>> Set System Parameters?" + RESET);
                        System.out.print("Configure memory/disk settings? (y/n): ");
                        if (scanner.nextLine().trim().equalsIgnoreCase("y"))
                            inputHandler.setSystemParameters();
                        showOptions();
                        break;
                    case 2:
                        schedulingHandler.showSchedulingMenu();
                        showOptions();
                        break;
                    case 3:
                        pageHandler.showMenu(scanner);
                        showOptions();
                        break;
                    case 4:
                        diskHandler.showMenu(scanner);
                        showOptions();
                        break;
                    case 5:
                        integrationHandler.showMenu(scanner);
                        showOptions();
                        break;
                    case 6:
                        Scripts.openGUI("from menu");
                        showOptions();
                        break;
                    case 7:
                        commandMode();
                        showOptions();
                        break;
                    case 8:
                        System.out.println(RED + "Exiting MiniOS. Goodbye!" + RESET);
                        exit = true;
                        break;
                    default:
                        System.out.println(RED + "Invalid option! Please enter 1-8." + RESET);
                        showOptions();
                }
            } catch (NumberFormatException e) {
                System.out.println(RED + "Invalid input!" + RESET);
                showOptions();
            }
        }
        scanner.close();
    }

    private void commandMode() {
        System.out.println(BLUE + "\n--- COMMAND MODE ---" + RESET);
        System.out.println("Type 'help' for commands, 'exit' to return.");
        boolean inCommandMode = true;
        java.util.Stack<String> historyStack = new java.util.Stack<>();
        String lastCommand = "";

        while (inCommandMode) {
            System.out.print(WHITE_BOLD + "MiniOS> " + RESET);
            String cmdInput = scanner.nextLine().trim();

            String[] knownCmds = {
                    "history","!!","clear","cls","credits","help","?","open gui",
                    "addprocess","listprocess","clearprocess","input","sysparams",
                    "setpriority","settimequantum","runsjf","runprioritynp","runpriorityp","runrr",
                    "pagereplace","diskschedule","integrate","performance","schedule","memory","metrics"
            };
            for (String kc : knownCmds)
                if (cmdInput.startsWith(kc)) { historyStack.push(Scripts.getTimestampedCommand(cmdInput)); break; }

            if (cmdInput.equals("!!")) {
                if (lastCommand.isEmpty()) { System.out.println(RED + "No previous command!" + RESET); continue; }
                if (!lastCommand.equals("clear")) System.out.println(GREEN + "Running: " + lastCommand + RESET);
                cmdInput = lastCommand;
            }

            String[] parts   = cmdInput.split("\\s+");
            String   command = parts[0].toLowerCase();

            switch (command) {
                case "clear": case "cls":
                    Scripts.clear(); lastCommand = "clear"; break;

                case "credits":
                    Scripts.showCredits(); lastCommand = "credits"; break;

                case "?": case "help":
                    Scripts.showHelp(); lastCommand = "help"; break;

                case "history":
                    Scripts.showHistory(historyStack); lastCommand = "history"; break;

                case "input":
                    lastCommand = "input";
                    inputHandler.inputProcessesInteractive();
                    break;

                case "sysparams":
                    lastCommand = "sysparams";
                    inputHandler.setSystemParameters();
                    break;

                case "addprocess":
                    lastCommand = "addprocess";
                    if (parts.length >= 3) {
                        String[] args = new String[parts.length - 1];
                        System.arraycopy(parts, 1, args, 0, parts.length - 1);
                        inputHandler.addProcessCommand(args);
                    } else {
                        System.out.println(RED + "Usage: addprocess <AT> <BT> [Pages]" + RESET);
                    }
                    break;

                case "listprocess":
                    lastCommand = "listprocess";
                    inputHandler.listProcesses();
                    break;

                case "clearprocess":
                    lastCommand = "clearprocess";
                    inputHandler.clearProcesses();
                    break;

                case "setpriority":
                    lastCommand = "setpriority";
                    schedulingHandler.setPrioritiesInteractive();
                    break;

                case "settimequantum":
                    lastCommand = "settimequantum";
                    if (parts.length >= 2) {
                        try { schedulingHandler.setTimeQuantum(Integer.parseInt(parts[1])); }
                        catch (NumberFormatException ex) { System.out.println(RED + "Invalid quantum!" + RESET); }
                    } else System.out.println(RED + "Usage: settimequantum <value>" + RESET);
                    break;

                case "runsjf":        lastCommand="runsjf";        schedulingHandler.runSJFCommand();        break;
                case "runprioritynp": lastCommand="runprioritynp"; schedulingHandler.runPriorityNPCommand(); break;
                case "runpriorityp":  lastCommand="runpriorityp";  schedulingHandler.runPriorityPCommand();  break;
                case "runrr":         lastCommand="runrr";         schedulingHandler.runRRCommand();         break;

                case "pagereplace":
                    lastCommand = "pagereplace";
                    pageHandler.showMenu(scanner);
                    break;

                case "diskschedule":
                    lastCommand = "diskschedule";
                    diskHandler.showMenu(scanner);
                    break;

                case "integrate":
                    lastCommand = "integrate";
                    integrationHandler.showMenu(scanner);
                    break;

                case "metrics":
                    lastCommand = "metrics";
                    integrationHandler.showMetrics(scanner);
                    break;

                case "performance":
                    lastCommand = "performance";
                    showPerformanceSummary();
                    break;

                case "schedule":
                    lastCommand = "schedule";
                    schedulingHandler.showSchedulingMenu();
                    break;

                case "memory":
                    lastCommand = "memory";
                    System.out.println(YELLOW + "Use 'pagereplace' or 'diskschedule' or 'integrate'" + RESET);
                    break;

                case "open":
                    try {
                        if (parts[1].equalsIgnoreCase("gui")) {
                            lastCommand = "open gui";
                            Scripts.openGUI("from command line");
                        } else System.out.println(RED + "Unknown: 'open " + parts[1] + "'" + RESET);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        System.out.println(RED + "Usage: open gui" + RESET);
                    }
                    break;

                case "exit":
                    inCommandMode = false;
                    System.out.println(BLUE + "Returning to Main Menu..." + RESET);
                    showOptions();
                    break;

                case "":
                    break;

                default:
                    System.out.println(RED + "Unknown command: '" + command + "'. Type 'help'." + RESET);
            }
        }
    }

    private void showPerformanceSummary() {
        ProcessList pl = ProcessList.getInstance();
        System.out.println(CYAN + "\n=== SYSTEM SUMMARY ===" + RESET);
        System.out.printf("Memory: %d pages | Frames: %d | Frame size: %d | Disk: 0-%d%n",
                pl.getMemorySize(), pl.getNumberOfFrames(), pl.getFrameSize(), pl.getDiskSize()-1);
        System.out.println("Processes: " + pl.getSize());
        inputHandler.listProcesses();
        System.out.println(YELLOW + "Run simulations via 'pagereplace', 'diskschedule', 'integrate' to see metrics." + RESET);
    }
}