package UI;

import java.util.Scanner;

import Controller.MemoryManagementHandler;
import Controller.CPUSchedulingHandler;
import Controller.ProcessInputHandler;
import Model.ProcessList;
import Scripts.Scripts;

public class Terminal {

    private Scanner scanner;
    private ProcessInputHandler inputHandler;
    private CPUSchedulingHandler schedulingHandler;
    private MemoryManagementHandler memoryHandler;
    public static final String RESET = "\033[0m";
    public static final String RED = "\033[0;31m";
    public static final String GREEN = "\033[0;32m";
    public static final String YELLOW = "\033[0;33m";
    public static final String BLUE = "\033[0;34m";
    public static final String PURPLE = "\033[0;35m";
    public static final String CYAN = "\033[0;36m";
    public static final String WHITE_BOLD = "\033[1;37m";
    public final ProcessList processList = ProcessList.getInstance();

    public Terminal() {
        this.scanner = new Scanner(System.in);
        this.inputHandler = new ProcessInputHandler(scanner);
        this.schedulingHandler = new CPUSchedulingHandler(scanner);
        this.memoryHandler = new MemoryManagementHandler(scanner);
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
        System.out.println("1. " + CYAN + "Input Processes" + RESET);
        System.out.println("2. " + PURPLE + "CPU Scheduling" + RESET);
        System.out.println("3. " + YELLOW + "Memory Management" + RESET);
        System.out.println("4. " + GREEN + "Open GUI" + RESET);
        System.out.println("5. " + BLUE  + "Use Commands (Shell Mode)" + RESET);
        System.out.println("6. " + RED + "Exit" + RESET);
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
                        System.out.println(CYAN + ">> Initialize Process Input..." + RESET);
                        inputHandler.inputProcessesInteractive();
                        break;
                    case 2:
                        schedulingHandler.showSchedulingMenu();
                        break;
                    case 3:
                        memoryHandler.showMemoryMenu();
                        break;
                    case 4:
                        Scripts.openGUI("from menu");
                        break;
                    case 5:
                        commandMode();
                        break;
                    case 6:
                        System.out.println(RED + "Exiting MiniOS. Goodbye!" + RESET);
                        exit = true;
                        break;
                    default:
                        System.out.println(RED + "Invalid option! Please enter a number between 1 and 6." + RESET);
                }
            } catch (NumberFormatException e) {
                System.out.println(RED + "Invalid input! Please enter a valid number." + RESET);
            }
        }
        scanner.close();
    }

    private void commandMode() {
        System.out.println(BLUE + "\n--- COMMAND MODE ACTIVATED ---" + RESET);
        System.out.println("Type 'help' for commands, 'exit' to return to menu.");
        boolean inCommandMode = true;
        java.util.Stack<String> historyStack = new java.util.Stack<>();
        String[] commandArray = new String[18];
        commandArray[0] = "history";
        commandArray[1] = "!!";
        commandArray[2] = "clear";
        commandArray[3] = "cls";
        commandArray[4] = "credits";
        commandArray[5] = "help";
        commandArray[6] = "?";
        commandArray[7] = "open gui";
        commandArray[8] = "addprocess";
        commandArray[9] = "listprocess";
        commandArray[10] = "clearprocess";
        commandArray[11] = "input";
        commandArray[12] = "setpriority";
        commandArray[13] = "settimequantum";
        commandArray[14] = "runsjf";
        commandArray[15] = "runprioritynp";
        commandArray[16] = "runpriorityp";
        commandArray[17] = "runrr";
        String lastCommand = "";

        while (inCommandMode) {
            System.out.print(WHITE_BOLD + "MiniOS> " + RESET);
            String cmdInput = scanner.nextLine().trim();

            for(int i = 0; i < commandArray.length; i++) {
                if(cmdInput.startsWith(commandArray[i]))
                    historyStack.push(Scripts.getTimestampedCommand(cmdInput));
            }

            if (cmdInput.equals("!!")) {
                if (lastCommand.isEmpty()) {
                    System.out.println(RED + "No previous command history!" + RESET);
                    continue;
                } else {
                    if(!lastCommand.equals("clear"))
                        System.out.println(GREEN + "Running: " + lastCommand + RESET);
                    cmdInput = lastCommand;
                }
            }

            String[] parts = cmdInput.split("\\s+");
            String command = parts[0].toLowerCase();

            switch (command) {
                case "clear":
                case "cls":
                    Scripts.clear();
                    lastCommand = "clear";
                    break;

                case "credits":
                    Scripts.showCredits();
                    lastCommand = "credits";
                    break;

                case "?":
                case "help":
                    lastCommand = "help";
                    Scripts.showHelp();
                    break;

                case "history":
                    Scripts.showHistory(historyStack);
                    lastCommand = "history";
                    break;

                case "input":
                    lastCommand = "input";
                    inputHandler.inputProcessesInteractive();
                    break;

                case "addprocess":
                    lastCommand = "addprocess";
                    if (parts.length >= 3) {
                        String[] args = new String[parts.length - 1];
                        System.arraycopy(parts, 1, args, 0, parts.length - 1);
                        inputHandler.addProcessCommand(args);
                    } else {
                        System.out.println(RED + "Usage: addprocess <arrival_time> <burst_time>" + RESET);
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
                        try {
                            int quantum = Integer.parseInt(parts[1]);
                            schedulingHandler.setTimeQuantum(quantum);
                        } catch (NumberFormatException ex) {
                            System.out.println(RED + "Error: Invalid time quantum value!" + RESET);
                        }
                    } else {
                        System.out.println(RED + "Usage: settimequantum <value>" + RESET);
                    }
                    break;

                case "runsjf":
                    lastCommand = "runsjf";
                    schedulingHandler.runSJFCommand();
                    break;

                case "runprioritynp":
                    lastCommand = "runprioritynp";
                    schedulingHandler.runPriorityNPCommand();
                    break;

                case "runpriorityp":
                    lastCommand = "runpriorityp";
                    schedulingHandler.runPriorityPCommand();
                    break;

                case "runrr":
                    lastCommand = "runrr";
                    schedulingHandler.runRRCommand();
                    break;

                case "schedule":
                    lastCommand = "schedule";
                    schedulingHandler.showSchedulingMenu();
                    break;

                case "memory":
                    lastCommand = "memory";
                    memoryHandler.showMemoryMenu();
                    break;

                case "open":
                    try {
                        if(parts[1].toLowerCase().equals("gui")) {
                            lastCommand = "open gui";
                            Scripts.openGUI("from command line");
                        } else {
                            System.out.println(RED + "Unknown command: '" + command + "'. Type 'help' for list." + RESET);
                        }
                    } catch (IndexOutOfBoundsException e) {
                        System.out.println(RED + "Unknown command: '" + command + "'. Type 'help' for list." + RESET);
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
                    System.out.println(RED + "Unknown command: '" + command + "'. Type 'help' for list." + RESET);
            }
        }
    }
}