package UI;

import Model.ProcessList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ProcessInputPanel extends JPanel {

    private final ProcessList processList;
    private JTextField txtArrivalTime;
    private JTextField txtBurstTime;
    private JTextField txtPages;
    private JTextField txtMemorySize;
    private JTextField txtFrameSize;
    private JTextField txtDiskSize;
    private JTable processTable;
    private DefaultTableModel tableModel;
    private JLabel lblProcessCount;
    private JLabel lblSystemParams;

    public ProcessInputPanel() {
        this.processList = ProcessList.getInstance();
        setLayout(new BorderLayout(8, 8));
        setBackground(new Color(245, 245, 245));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel top = new JPanel(new GridLayout(1, 2, 8, 0));
        top.setOpaque(false);
        top.add(createInputPanel());
        top.add(createSystemParamsPanel());

        add(top, BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createControlPanel(), BorderLayout.SOUTH);

        updateTable();
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Add New Process"),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx=0; gbc.gridy=0; panel.add(new JLabel("Arrival Time:"), gbc);
        gbc.gridx=1; txtArrivalTime = new JTextField(8); panel.add(txtArrivalTime, gbc);

        gbc.gridx=0; gbc.gridy=1; panel.add(new JLabel("Burst Time:"), gbc);
        gbc.gridx=1; txtBurstTime = new JTextField(8); panel.add(txtBurstTime, gbc);

        gbc.gridx=0; gbc.gridy=2; panel.add(new JLabel("Number of Pages:"), gbc);
        gbc.gridx=1; txtPages = new JTextField("4", 8); panel.add(txtPages, gbc);

        gbc.gridx=0; gbc.gridy=3; gbc.gridwidth=2;
        JButton btnAdd = new JButton("Add Process");
        btnAdd.setBackground(new Color(100, 149, 237));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFocusPainted(false);
        btnAdd.addActionListener(e -> addProcess());
        panel.add(btnAdd, gbc);

        return panel;
    }

    private JPanel createSystemParamsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("System Parameters"),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx=0; gbc.gridy=0; panel.add(new JLabel("Memory Size (pages):"), gbc);
        gbc.gridx=1; txtMemorySize = new JTextField(String.valueOf(processList.getMemorySize()), 8);
        panel.add(txtMemorySize, gbc);

        gbc.gridx=0; gbc.gridy=1; panel.add(new JLabel("Frame Size (pages):"), gbc);
        gbc.gridx=1; txtFrameSize = new JTextField(String.valueOf(processList.getFrameSize()), 8);
        panel.add(txtFrameSize, gbc);

        gbc.gridx=0; gbc.gridy=2; panel.add(new JLabel("Disk Size (cylinders):"), gbc);
        gbc.gridx=1; txtDiskSize = new JTextField(String.valueOf(processList.getDiskSize()), 8);
        panel.add(txtDiskSize, gbc);

        gbc.gridx=0; gbc.gridy=3; gbc.gridwidth=2;
        JButton btnSave = new JButton("Save Parameters");
        btnSave.setBackground(new Color(80, 180, 100));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.addActionListener(e -> saveSystemParams());
        panel.add(btnSave, gbc);

        gbc.gridy=4;
        lblSystemParams = new JLabel(getParamsText());
        lblSystemParams.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblSystemParams.setForeground(new Color(60, 100, 180));
        panel.add(lblSystemParams, gbc);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Process List"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        String[] columns = {"PID", "Arrival Time", "Burst Time", "Pages", "Disk Blocks"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        processTable = new JTable(tableModel);
        processTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        processTable.setRowHeight(24);
        processTable.getTableHeader().setReorderingAllowed(false);
        panel.add(new JScrollPane(processTable), BorderLayout.CENTER);
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
            String at = txtArrivalTime.getText().trim();
            String bt = txtBurstTime.getText().trim();
            String pg = txtPages.getText().trim();

            if (at.isEmpty() || bt.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter Arrival Time and Burst Time.",
                        "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int arrivalTime = Integer.parseInt(at);
            int burstTime   = Integer.parseInt(bt);
            int pages       = pg.isEmpty() ? 4 : Integer.parseInt(pg);

            if (arrivalTime < 0) { showErr("Arrival Time must be >= 0"); return; }
            if (burstTime <= 0)  { showErr("Burst Time must be > 0");    return; }
            if (pages <= 0)      { showErr("Pages must be > 0");          return; }

            if (processList.getTotalPages() + pages > processList.getDiskSize()) {
                showErr("Process needs " + pages + " disk blocks but disk only has " + (processList.getDiskSize() - processList.getTotalPages()) + " free blocks left.\nIncrease disk size first.");
                return;
            }

            processList.addProcess(arrivalTime, burstTime, pages);

            txtArrivalTime.setText(""); txtBurstTime.setText("");
            txtPages.setText("4");
            txtArrivalTime.requestFocus();
            updateTable();

            notifyStoragePanel();

            JOptionPane.showMessageDialog(this,
                    "Process added! PID: " + (processList.getSize() - 1) + "  Pages: " + pages,
                    "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException ex) {
            showErr("Please enter valid integer values.");
        }
    }

    private void saveSystemParams() {
        try {
            int ms = Integer.parseInt(txtMemorySize.getText().trim());
            int fs = Integer.parseInt(txtFrameSize.getText().trim());
            int ds = Integer.parseInt(txtDiskSize.getText().trim());
            if (ms <= 0 || fs <= 0 || ds <= 0) { showErr("All values must be > 0"); return; }
            if (ds < processList.getTotalPages()) { 
                showErr("Disk size must not be less than the total pages in the system (" + processList.getTotalPages() + ")."); 
                return; 
            }
            processList.setMemorySize(ms);
            processList.setFrameSize(fs);
            processList.setDiskSize(ds);
            processList.reassignDiskBlocks();
            lblSystemParams.setText(getParamsText());
            updateTable();
            notifyStoragePanel();
            JOptionPane.showMessageDialog(this, "System parameters saved.\nFrames: " + processList.getNumberOfFrames(),
                    "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            showErr("Invalid values. Integers only.");
        }
    }

    private String getParamsText() {
        return String.format("Frames: %d | Disk: 0-%d",
                processList.getNumberOfFrames(), processList.getDiskSize() - 1);
    }

    private void updateTable() {
        tableModel.setRowCount(0);
        for (var process : processList.getProcesses()) {
            String blocks = process.getPageToBlockMap().values().toString();
            if (blocks.length() > 20) blocks = blocks.substring(0, 17) + "...";
            tableModel.addRow(new Object[]{
                    process.getPid(), process.getArrivalTime(), process.getBurstTime(),
                    process.getNumberOfPages(), blocks});
        }
        lblProcessCount.setText("Total Processes: " + processList.getSize());
        if (lblSystemParams != null) lblSystemParams.setText(getParamsText());
    }

    private void clearProcesses() {
        if (processList.getSize() == 0) {
            JOptionPane.showMessageDialog(this, "No processes to clear.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int result = JOptionPane.showConfirmDialog(this,
                "Clear all " + processList.getSize() + " processes?",
                "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (result == JOptionPane.YES_OPTION) {
            processList.clearProcesses();
            updateTable();
            notifyStoragePanel();
        }
    }

    private void notifyStoragePanel() {
        SwingUtilities.invokeLater(() -> {
            java.awt.Window w = SwingUtilities.getWindowAncestor(this);
            if (w instanceof JFrame) {
                notifyStoragePanels(((JFrame) w).getContentPane());
            }
        });
    }

    private void notifyStoragePanels(java.awt.Container c) {
        for (java.awt.Component comp : c.getComponents()) {
            if (comp instanceof StoragePanel) {
                ((StoragePanel) comp).refreshProcessCombos();
                ((StoragePanel) comp).refreshPerformance();
            } else if (comp instanceof java.awt.Container) {
                notifyStoragePanels((java.awt.Container) comp);
            }
        }
    }

    private void showErr(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Invalid Input", JOptionPane.ERROR_MESSAGE);
    }
}