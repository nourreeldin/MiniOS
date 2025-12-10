package UI;

import CPUScheduling.*;
import Model.Process;
import Model.ProcessList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class CPUSchedulingPanel extends JPanel {

    private final ProcessList processList;
    private JComboBox<String> algorithmSelector;
    private JSpinner quantumSpinner;
    private JLabel lblQuantum;
    private JTextArea resultArea;
    private JPanel ganttPanel;
    private JTable processTable;
    private DefaultTableModel tableModel;

    public CPUSchedulingPanel() {
        this.processList = ProcessList.getInstance();
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(245, 245, 245));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(createControlPanel(), BorderLayout.NORTH);
        add(createResultsPanel(), BorderLayout.CENTER);
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Algorithm Selection"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Algorithm:"), gbc);

        gbc.gridx = 1;
        String[] algorithms = {"Shortest Job First (SJF)", "Priority Non-Preemptive",
                "Priority Preemptive", "Round Robin"};
        algorithmSelector = new JComboBox<>(algorithms);
        algorithmSelector.addActionListener(e -> updateQuantumVisibility());
        panel.add(algorithmSelector, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        lblQuantum = new JLabel("Time Quantum:");
        lblQuantum.setVisible(false);
        panel.add(lblQuantum, gbc);

        gbc.gridx = 1;
        quantumSpinner = new JSpinner(new SpinnerNumberModel(2, 1, 100, 1));
        quantumSpinner.setVisible(false);
        panel.add(quantumSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        JButton btnSetPriority = new JButton("Set Priorities");
        btnSetPriority.setBackground(new Color(255, 165, 0));
        btnSetPriority.setForeground(Color.WHITE);
        btnSetPriority.setFocusPainted(false);
        btnSetPriority.addActionListener(e -> setPriorities());
        buttonPanel.add(btnSetPriority);

        JButton btnRun = new JButton("Run Algorithm");
        btnRun.setBackground(new Color(100, 149, 237));
        btnRun.setForeground(Color.WHITE);
        btnRun.setFocusPainted(false);
        btnRun.addActionListener(e -> runAlgorithm());
        buttonPanel.add(btnRun);

        panel.add(buttonPanel, gbc);

        return panel;
    }

    private JPanel createResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(245, 245, 245));

        // Gantt Chart Panel
        ganttPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(230, 230, 230));
                g.drawString("Gantt Chart will appear here after running algorithm", 20, 30);
            }
        };
        ganttPanel.setBackground(Color.WHITE);
        ganttPanel.setPreferredSize(new Dimension(0, 100));
        ganttPanel.setBorder(BorderFactory.createTitledBorder("Gantt Chart"));
        panel.add(ganttPanel, BorderLayout.NORTH);

        // Process Table
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createTitledBorder("Process Details"));

        String[] columns = {"PID", "Arrival", "Burst", "Priority", "Completion", "Turnaround", "Waiting"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        processTable = new JTable(tableModel);
        processTable.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(processTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        panel.add(tablePanel, BorderLayout.CENTER);

        // Statistics Panel
        resultArea = new JTextArea(4, 30);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        resultArea.setBackground(new Color(240, 255, 240));
        JScrollPane resultScroll = new JScrollPane(resultArea);
        resultScroll.setBorder(BorderFactory.createTitledBorder("Statistics"));
        panel.add(resultScroll, BorderLayout.SOUTH);

        return panel;
    }

    private void updateQuantumVisibility() {
        boolean isRoundRobin = algorithmSelector.getSelectedIndex() == 3;
        lblQuantum.setVisible(isRoundRobin);
        quantumSpinner.setVisible(isRoundRobin);
    }

    private void setPriorities() {
        if (processList.getSize() == 0) {
            JOptionPane.showMessageDialog(this,
                    "No processes available. Please add processes first.",
                    "No Processes",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JPanel panel = new JPanel(new GridLayout(processList.getSize(), 2, 10, 10));
        JSpinner[] spinners = new JSpinner[processList.getSize()];

        for (int i = 0; i < processList.getSize(); i++) {
            Process p = processList.getProcesses().get(i);
            panel.add(new JLabel("Process " + p.getPid() + " Priority:"));
            spinners[i] = new JSpinner(new SpinnerNumberModel(p.getPriority(), 0, 100, 1));
            panel.add(spinners[i]);
        }

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Set Process Priorities (Lower = Higher Priority)",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            for (int i = 0; i < processList.getSize(); i++) {
                Process p = processList.getProcesses().get(i);
                p.setPriority((Integer) spinners[i].getValue());
            }
            JOptionPane.showMessageDialog(this,
                    "Priorities updated successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void runAlgorithm() {
        if (processList.getSize() == 0) {
            JOptionPane.showMessageDialog(this,
                    "No processes available. Please add processes first.",
                    "No Processes",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int selectedIndex = algorithmSelector.getSelectedIndex();
        ArrayList<Process> processes = new ArrayList<>(processList.getProcesses());

        try {
            switch (selectedIndex) {
                case 0: // SJF
                    SJF.apply(processes);
                    displayResults("SJF", SJF.getProcessesList(), SJF.getGanttChart(),
                            SJF.getAverageTurnAroundTime(), SJF.getAverageWaitingTime(),
                            SJF.getCPUUtilization());
                    break;

                case 1: // Priority Non-Preemptive
                    if (!validatePriorities()) return;
                    Priority.applyNonPreemptive(processes);
                    displayResults("Priority Non-Preemptive", Priority.getProcessesList(),
                            Priority.getGanttChart(), Priority.getAverageTurnAroundTime(),
                            Priority.getAverageWaitingTime(), Priority.getCPUUtilization());
                    break;

                case 2: // Priority Preemptive
                    if (!validatePriorities()) return;
                    Priority.applyPreemptive(processes);
                    displayResults("Priority Preemptive", Priority.getProcessesList(),
                            Priority.getGanttChart(), Priority.getAverageTurnAroundTime(),
                            Priority.getAverageWaitingTime(), Priority.getCPUUtilization());
                    break;

                case 3: // Round Robin
                    int quantum = (Integer) quantumSpinner.getValue();
                    RoundRobin.apply(processes, quantum);
                    displayResults("Round Robin (Q=" + quantum + ")", RoundRobin.getProcessesList(),
                            RoundRobin.getGanttChart(), RoundRobin.getAverageTurnAroundTime(),
                            RoundRobin.getAverageWaitingTime(), RoundRobin.getCPUUtilization());
                    break;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error running algorithm: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean validatePriorities() {
        for (Process p : processList.getProcesses()) {
            if (p.getPriority() == 0) {
                JOptionPane.showMessageDialog(this,
                        "Please set priorities for all processes first!\nUse 'Set Priorities' button.",
                        "Priorities Required",
                        JOptionPane.WARNING_MESSAGE);
                return false;
            }
        }
        return true;
    }

    private void displayResults(String algorithmName, ArrayList<Process> processes,
                                ArrayList<?> ganttChart, double avgTAT, double avgWT, double cpuUtil) {
        // Update Gantt Chart
        ganttPanel.removeAll();
        ganttPanel.setLayout(new BorderLayout());
        ganttPanel.add(new GanttChartComponent(ganttChart), BorderLayout.CENTER);
        ganttPanel.revalidate();
        ganttPanel.repaint();

        // Update Process Table
        tableModel.setRowCount(0);
        for (Process p : processes) {
            tableModel.addRow(new Object[] {
                    p.getPid(),
                    p.getArrivalTime(),
                    p.getBurstTime(),
                    p.getPriority(),
                    p.getCompletionTime(),
                    p.getTurnAroundTime(),
                    p.getWaitingTime()
            });
        }

        // Update Statistics
        resultArea.setText(String.format(
                "Algorithm: %s\n" +
                        "Average Turnaround Time: %.2f\n" +
                        "Average Waiting Time: %.2f\n" +
                        "CPU Utilization: %.2f%%",
                algorithmName, avgTAT, avgWT, cpuUtil
        ));
    }

    private class GanttChartComponent extends JPanel {
        private final ArrayList<?> ganttChart;

        public GanttChartComponent(ArrayList<?> ganttChart) {
            this.ganttChart = ganttChart;
            setPreferredSize(new Dimension(0, 80));
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (ganttChart == null || ganttChart.isEmpty()) return;

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int startX = 50;
            int startY = 30;
            int height = 30;

            int totalTime = 0;
            Object lastSegment = ganttChart.get(ganttChart.size() - 1);
            if (lastSegment instanceof SJF.GanttSegment) {
                totalTime = ((SJF.GanttSegment) lastSegment).endTime;
            } else if (lastSegment instanceof Priority.GanttSegment) {
                totalTime = ((Priority.GanttSegment) lastSegment).endTime;
            } else if (lastSegment instanceof RoundRobin.GanttSegment) {
                totalTime = ((RoundRobin.GanttSegment) lastSegment).endTime;
            }

            int maxWidth = getWidth() - 100;
            double scale = (double) maxWidth / totalTime;

            Color[] colors = {
                    new Color(100, 149, 237), new Color(144, 238, 144),
                    new Color(255, 182, 193), new Color(255, 218, 185),
                    new Color(221, 160, 221), new Color(176, 224, 230)
            };

            for (Object obj : ganttChart) {
                int pid = -1, start = -1, end = -1;

                if (obj instanceof SJF.GanttSegment) {
                    SJF.GanttSegment seg = (SJF.GanttSegment) obj;
                    pid = seg.pid; start = seg.startTime; end = seg.endTime;
                } else if (obj instanceof Priority.GanttSegment) {
                    Priority.GanttSegment seg = (Priority.GanttSegment) obj;
                    pid = seg.pid; start = seg.startTime; end = seg.endTime;
                } else if (obj instanceof RoundRobin.GanttSegment) {
                    RoundRobin.GanttSegment seg = (RoundRobin.GanttSegment) obj;
                    pid = seg.pid; start = seg.startTime; end = seg.endTime;
                }

                int x = startX + (int)(start * scale);
                int width = (int)((end - start) * scale);

                g2.setColor(colors[pid % colors.length]);
                g2.fillRect(x, startY, width, height);

                g2.setColor(Color.BLACK);
                g2.drawRect(x, startY, width, height);

                g2.setFont(new Font("Arial", Font.BOLD, 12));
                String label = "P" + pid;
                FontMetrics fm = g2.getFontMetrics();
                int labelX = x + (width - fm.stringWidth(label)) / 2;
                int labelY = startY + (height + fm.getAscent()) / 2 - 2;
                g2.drawString(label, labelX, labelY);

                g2.setFont(new Font("Arial", Font.PLAIN, 10));
                g2.drawString(String.valueOf(start), x - 5, startY + height + 15);
            }

            g2.setFont(new Font("Arial", Font.PLAIN, 10));
            int lastX = startX + (int)(totalTime * scale);
            g2.drawString(String.valueOf(totalTime), lastX - 5, startY + height + 15);
        }
    }
}