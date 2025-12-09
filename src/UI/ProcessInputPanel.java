package UI;

import Model.ProcessList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ProcessInputPanel extends JPanel {

    private final ProcessList processList;
    private JTextField txtArrivalTime;
    private JTextField txtBurstTime;
    private JTable processTable;
    private DefaultTableModel tableModel;
    private JLabel lblProcessCount;

    public ProcessInputPanel() {
        this.processList = ProcessList.getInstance();
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(245, 245, 245));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(createInputPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createControlPanel(), BorderLayout.SOUTH);

        updateTable();
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Add New Process"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Arrival Time
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Arrival Time:"), gbc);

        gbc.gridx = 1;
        txtArrivalTime = new JTextField(10);
        panel.add(txtArrivalTime, gbc);

        // Burst Time
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Burst Time:"), gbc);

        gbc.gridx = 1;
        txtBurstTime = new JTextField(10);
        panel.add(txtBurstTime, gbc);

        // Add Button
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        JButton btnAdd = new JButton("Add Process");
        btnAdd.setBackground(new Color(100, 149, 237));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFocusPainted(false);
        btnAdd.addActionListener(e -> addProcess());
        panel.add(btnAdd, gbc);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Process List"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        String[] columns = {"PID", "Arrival Time", "Burst Time"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        processTable = new JTable(tableModel);
        processTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        processTable.setRowHeight(25);
        processTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(processTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        panel.setBackground(new Color(245, 245, 245));

        lblProcessCount = new JLabel("Total Processes: 0");
        lblProcessCount.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(lblProcessCount);

        panel.add(Box.createHorizontalStrut(20));

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> updateTable());
        panel.add(btnRefresh);

        JButton btnClear = new JButton("Clear All");
        btnClear.setBackground(new Color(220, 60, 60));
        btnClear.setForeground(Color.WHITE);
        btnClear.setFocusPainted(false);
        btnClear.addActionListener(e -> clearProcesses());
        panel.add(btnClear);

        return panel;
    }

    private void addProcess() {
        try {
            String arrivalText = txtArrivalTime.getText().trim();
            String burstText = txtBurstTime.getText().trim();

            if (arrivalText.isEmpty() || burstText.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please enter both Arrival Time and Burst Time",
                        "Input Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            int arrivalTime = Integer.parseInt(arrivalText);
            int burstTime = Integer.parseInt(burstText);

            if (arrivalTime < 0) {
                JOptionPane.showMessageDialog(this,
                        "Arrival Time must be >= 0",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (burstTime <= 0) {
                JOptionPane.showMessageDialog(this,
                        "Burst Time must be > 0",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            processList.addProcess(arrivalTime, burstTime);

            txtArrivalTime.setText("");
            txtBurstTime.setText("");
            txtArrivalTime.requestFocus();

            updateTable();

            JOptionPane.showMessageDialog(this,
                    "Process added successfully!\nPID: " + (processList.getSize() - 1),
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Please enter valid integer values",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTable() {
        tableModel.setRowCount(0);

        for (int i = 0; i < processList.getSize(); i++) {
            var process = processList.getProcesses().get(i);
            tableModel.addRow(new Object[] {
                    process.getPid(),
                    process.getArrivalTime(),
                    process.getBurstTime()
            });
        }

        lblProcessCount.setText("Total Processes: " + processList.getSize());
    }

    private void clearProcesses() {
        if (processList.getSize() == 0) {
            JOptionPane.showMessageDialog(this,
                    "No processes to clear",
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int result = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to clear all " + processList.getSize() + " processes?",
                "Confirm Clear",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            processList.getProcesses().clear();
            updateTable();
            JOptionPane.showMessageDialog(this,
                    "All processes cleared successfully",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
}