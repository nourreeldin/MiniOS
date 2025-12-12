package Scripts;

import UI.GUI;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Stack;

public class Scripts {
    public static final String RESET = "\033[0m";
    public static final String RED = "\033[0;31m";
    public static final String GREEN = "\033[0;32m";
    public static final String YELLOW = "\033[0;33m";
    public static final String BLUE = "\033[0;34m";
    public static final String PURPLE = "\033[0;35m";
    public static final String CYAN = "\033[0;36m";
    public static final String CYAN_BOLD = "\033[1;36m";
    public static final String WHITE_BOLD = "\033[1;37m";

    private Scripts() {}

    public static void clear() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (IOException | InterruptedException ex) {
            for (int i = 0; i < 50; i++) System.out.println();
        }
    }

    public static String getTimestampedCommand(String command) {
        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
        return "[" + time + "] " + command;
    }

    public static void showHistory(Stack<String> stack) {
        System.out.println(WHITE_BOLD + "\n--- EXECUTION HISTORY ---" + RESET);

        if (stack.isEmpty()) {
            System.out.println(RED + "   (History is empty)   " + RESET);
        } else {
            for (int i = stack.size() - 1; i >= 0; i--) {
                String entry = stack.get(i);
                System.out.println(CYAN + " | " + RESET + entry);
            }
        }
        System.out.println(WHITE_BOLD + "-------------------------" + RESET);
    }

    public static void openGUI(String s) {
        System.out.println(GREEN + ">> Launching GUI..." + RESET);
        javax.swing.SwingUtilities.invokeLater(() -> {
            GUI gui = new GUI();
            if(s.equals("from menu"))
                gui.setGUIAddress(s);
        });
    }

    public static void showCredits() {
        System.out.println(CYAN + "\n╔══════════════════════════════════════════╗" + RESET);
        System.out.println(CYAN + "║             MINI OS SIMULATOR            ║" + RESET);
        System.out.println(CYAN + "╠══════════════════════════════════════════╣" + RESET);
        System.out.println(WHITE_BOLD + "║  Team Members:                           ║" + RESET);
        System.out.println("║    Noureldin Hossam Mahmoud (20232773)   ║");
        System.out.println("║    Mohamed Sherif Ibrahim   (20233901)   ║");
        System.out.println("║    Omar Monzer Raafat       (20232401)   ║");
        System.out.println("║    Marco Fathy Isaac        (20233488)   ║");
        System.out.println("║    Hamza Sayed Zaki         (20232888)   ║");
        System.out.println(CYAN + "╠══════════════════════════════════════════╣" + RESET);
        System.out.println(CYAN_BOLD + "║             FUE - 2025/2026              ║" + RESET);
        System.out.println(CYAN + "╚══════════════════════════════════════════╝" + RESET);
    }

    public static void showHelp() {
        System.out.println(YELLOW + "\n╔═══════════════════════════════════════════════════════════╗" + RESET);
        System.out.println(YELLOW + "║                    AVAILABLE COMMANDS                     ║" + RESET);
        System.out.println(YELLOW + "╠═══════════════════════════════════════════════════════════╣" + RESET);
        System.out.println(WHITE_BOLD + "║  GENERAL COMMANDS:                                        ║" + RESET);
        System.out.println("║    help, ?        - Show this help menu                   ║");
        System.out.println("║    clear, cls     - Clear the screen                      ║");
        System.out.println("║    credits        - Show team credits                     ║");
        System.out.println("║    history        - Show command history                  ║");
        System.out.println("║    !!             - Repeat last command                   ║");
        System.out.println("║    exit           - Return to main menu                   ║");
        System.out.println(YELLOW + "╠═══════════════════════════════════════════════════════════╣" + RESET);
        System.out.println(WHITE_BOLD + "║  PROCESS COMMANDS:                                        ║" + RESET);
        System.out.println("║    input          - Interactive process input mode        ║");
        System.out.println("║    addprocess <AT> <BT>                                   ║");
        System.out.println("║                   - Add process (Arrival, Burst Time)     ║");
        System.out.println("║    listprocess    - Display all processes                 ║");
        System.out.println("║    clearprocess   - Clear all processes                   ║");
        System.out.println("║    setpriority    - Set priority of a process             ║");
        System.out.println(YELLOW + "╠═══════════════════════════════════════════════════════════╣" + RESET);
        System.out.println(WHITE_BOLD + "║  SCHEDULING COMMANDS:                                     ║" + RESET);
        System.out.println("║    schedule       - Open scheduling algorithms list       ║");
        System.out.println("║    settimequantum <Q> - Set time quantum for Round Robin  ║");
        System.out.println("║    runsjf         - Run Shortest Job First                ║");
        System.out.println("║    runprioritynp  - Run Priority Non-Preemptive           ║");
        System.out.println("║    runpriorityp   - Run Priority Preemptive               ║");
        System.out.println("║    runrr          - Run Round Robin                       ║");
        System.out.println(YELLOW + "╠═══════════════════════════════════════════════════════════╣" + RESET);
        System.out.println(WHITE_BOLD + "║  MEMORY MANAGEMENT COMMANDS:                              ║" + RESET);
        System.out.println("║    memory         - Open memory management menu           ║");
        System.out.println(YELLOW + "╠═══════════════════════════════════════════════════════════╣" + RESET);
        System.out.println(WHITE_BOLD + "║  GUI COMMAND:                                             ║" + RESET);
        System.out.println("║    open gui       - Launch graphical interface            ║");
        System.out.println(YELLOW + "╚═══════════════════════════════════════════════════════════╝" + RESET);
    }
}