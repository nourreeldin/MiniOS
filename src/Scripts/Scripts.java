package Scripts;

import UI.GUI;
import java.io.IOException;
import java.util.Date;
import java.text.SimpleDateFormat;
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
    public static void listProcesses() {

    }
    public static void terminateProcess(int processId) {

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
            for (int i = stack.size() - 2; i >= 0; i--) {
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
        System.out.println(WHITE_BOLD + "--- CREDITS ---" + RESET);
        System.out.println(BLUE + "Noureldin Hossam Mahmoud" + WHITE_BOLD + " ~ " + BLUE + "20232773" + RESET);
        System.out.println(GREEN + "Mohamed Sherif Ibrahim" + WHITE_BOLD + " ~ " + GREEN + "20233901" + RESET);
        System.out.println(RED + "Omar Monzer Raafat" + WHITE_BOLD + " ~ " + RED + "20232401" + RESET);
        System.out.println(PURPLE + "Marco Fathy Isaac" + WHITE_BOLD + " ~ " + PURPLE + "20233488" + RESET);
        System.out.println(YELLOW + "Hamza Sayed Zaki" + WHITE_BOLD + " ~ " + YELLOW + "20232888" + RESET);
        System.out.println(CYAN_BOLD + "----- FUE -----" + RESET);
    }
    public static void showHelp() {
        System.out.println(YELLOW + "Available Commands:" + RESET);
        System.out.println(" - clear / cls : Clear the terminal screen");
        System.out.println(" - credits     : Show credits and team info");
        System.out.println(" - exit        : Return to main menu");
        System.out.println(" - history     : Show history of commands with dates");
        System.out.println(" - open gui    : Open the graphical user interface");
    }
}
