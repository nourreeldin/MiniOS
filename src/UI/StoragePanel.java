package UI;

import Controller.DiskSchedulingHandler;
import Controller.IntegrationHandler;
import Controller.PageReplacementHandler;
import Model.Process;
import Model.ProcessList;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

public class StoragePanel extends JPanel {

    private static final Color BG      = new Color(245, 245, 245);
    private static final Color WHITE   = Color.WHITE;
    private static final Color BLUE    = new Color(100, 149, 237);
    private static final Color GREEN   = new Color(80, 180, 100);
    private static final Color ORANGE  = new Color(255, 165, 0);
    private static final Color RED     = new Color(220, 60, 60);

    private JComboBox<String> prProcessCombo, prAlgorithmCombo;
    private JTextField prRefStringField;
    private JTextArea  prResultArea;
    private JLabel     prStatsLabel;
    private DefaultTableModel prTableModel;
    private JTable     prTable;
    private RAMView    prRamView;
    private Timer      prTimer;
    private JSlider    prSlider;
    private int        prStep = 0;
    private PageReplacementHandler.Result prResult;

    private JTextField dsHeadField, dsQueueField;
    private JComboBox<String> dsAlgorithmCombo, dsDirectionCombo;
    private JTextArea  dsResultArea;
    private JLabel     dsStatsLabel;
    private DiskView   dsDiskView;
    private Timer      dsTimer;
    private JSlider    dsSlider;
    private int        dsStep = 0;
    private DiskSchedulingHandler.DiskResult dsResult;

    private JTextField coreRefField, coreHeadField;
    private JComboBox<String> corePageAlgCombo, coreDiskAlgCombo, coreDirCombo;
    private JTextArea  coreResultArea;
    private JLabel     coreStatsLabel;
    private DefaultTableModel pageMapModel;
    private JTable     pageMapTable;
    private CPUView    coreCpuView;
    private RAMView    coreRamView;
    private DiskView   coreDiskView;
    private Timer      coreTimer;
    private JSlider    coreSlider;
    private int        coreStep = 0;
    private IntegrationHandler.IntegrationResult coreResult;

    private JTextField metRefField, metHeadField;
    private JComboBox<String> metPageAlgCombo, metDiskAlgCombo, metDirCombo;
    private JLabel lblTotalFaults, lblFaultRate, lblTotalHeadMovement, lblAvgSeekTime;
    private DefaultTableModel compTableModel;
    private JTable compTable;

    public StoragePanel() {
        setLayout(new BorderLayout());
        setBackground(BG);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(BG);
        tabs.addTab("Page Replacement", buildPageReplacementTab());
        tabs.addTab("Disk Scheduling",  buildDiskSchedulingTab());
        tabs.addTab("Integration",      buildIntegrationTab());
        tabs.addTab("Metrics",          buildMetricsTab());

        add(tabs, BorderLayout.CENTER);
        refreshProcessCombos();
    }

    private JPanel buildPageReplacementTab() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(BG);

        JPanel ctrl = new JPanel(new GridBagLayout());
        ctrl.setBackground(WHITE);
        ctrl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Page Replacement Parameters"),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5); g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx=0; g.gridy=0; ctrl.add(new JLabel("Process:"), g);
        g.gridx=1; prProcessCombo = new JComboBox<>(); ctrl.add(prProcessCombo, g);
        g.gridx=2; ctrl.add(new JLabel("Algorithm:"), g);
        g.gridx=3; prAlgorithmCombo = new JComboBox<>(new String[]{"FIFO","LRU","MRU","OPT","Clock"}); ctrl.add(prAlgorithmCombo, g);
        g.gridx=0; g.gridy=1; ctrl.add(new JLabel("Page Ref String:"), g);
        g.gridx=1; g.gridwidth=2; prRefStringField = new JTextField("7 0 1 2 0 3 0 4", 20); ctrl.add(prRefStringField, g);
        g.gridwidth=1; g.gridx=3;
        prStatsLabel = new JLabel(" "); prStatsLabel.setFont(new Font("Segoe UI", Font.BOLD, 11)); prStatsLabel.setForeground(new Color(60, 100, 180)); ctrl.add(prStatsLabel, g);
        g.gridx=0; g.gridy=2; g.gridwidth=4;
        JButton runBtn = makeButton("Calculate & Start Animation", BLUE);
        runBtn.addActionListener(e -> runPageReplacement());
        ctrl.add(runBtn, g);
        root.add(ctrl, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setBackground(BG);

        prRamView = new RAMView();
        JPanel visPanel = new JPanel(new BorderLayout());
        visPanel.setBackground(WHITE);
        visPanel.setBorder(BorderFactory.createTitledBorder("RAM Visualization"));
        visPanel.add(prRamView, BorderLayout.CENTER);
        prSlider = createSpeedSlider(e -> { if(prTimer != null) prTimer.setDelay(((JSlider)e.getSource()).getValue()); });
        visPanel.add(createPlaybackBar(
                e -> { if(prTimer != null) prTimer.start(); },
                e -> { if(prTimer != null) prTimer.stop(); },
                e -> { if(prTimer != null) { prTimer.stop(); prAnimateStep(); } },
                prSlider
        ), BorderLayout.SOUTH);
        centerPanel.add(visPanel, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerLocation(380); split.setResizeWeight(0.5); split.setBorder(null);

        JPanel tablePanel = new JPanel(new BorderLayout()); tablePanel.setBackground(WHITE);
        tablePanel.setBorder(BorderFactory.createTitledBorder("Step-by-Step"));
        prTableModel = new DefaultTableModel(new String[]{"Step","Page","Frames","Fault","Replaced"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        prTable = new JTable(prTableModel); prTable.setRowHeight(22); prTable.getTableHeader().setReorderingAllowed(false);
        tablePanel.add(new JScrollPane(prTable), BorderLayout.CENTER);
        split.setLeftComponent(tablePanel);

        JPanel logPanel = new JPanel(new BorderLayout()); logPanel.setBackground(WHITE);
        logPanel.setBorder(BorderFactory.createTitledBorder("Log"));
        prResultArea = new JTextArea(); prResultArea.setEditable(false); prResultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logPanel.add(new JScrollPane(prResultArea), BorderLayout.CENTER);
        split.setRightComponent(logPanel);
        centerPanel.add(split, BorderLayout.CENTER);
        root.add(centerPanel, BorderLayout.CENTER);
        return root;
    }

    private JPanel buildMetricsTab() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(BG);

        JPanel ctrl = new JPanel(new GridBagLayout());
        ctrl.setBackground(WHITE);
        ctrl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Metrics Parameters"),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5); g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx=0; g.gridy=0; ctrl.add(new JLabel("Page Ref (PID:page):"), g);
        g.gridx=1; metRefField = new JTextField("0:1 1:2 0:3 1:4 0:2", 20); ctrl.add(metRefField, g);
        g.gridx=2; ctrl.add(new JLabel("Initial Head:"), g);
        g.gridx=3; metHeadField = new JTextField("50", 5); ctrl.add(metHeadField, g);

        g.gridx=0; g.gridy=1; ctrl.add(new JLabel("Page Alg:"), g);
        g.gridx=1; metPageAlgCombo = new JComboBox<>(new String[]{"FIFO","LRU","OPT","Clock"}); ctrl.add(metPageAlgCombo, g);
        g.gridx=2; ctrl.add(new JLabel("Disk Alg:"), g);
        g.gridx=3; metDiskAlgCombo = new JComboBox<>(new String[]{"FCFS","SSTF","SCAN","CSCAN","LOOK","CLOOK"}); ctrl.add(metDiskAlgCombo, g);

        g.gridx=4; g.gridy=1; ctrl.add(new JLabel("Dir:"), g);
        g.gridx=5; metDirCombo = new JComboBox<>(new String[]{"Right","Left"}); ctrl.add(metDirCombo, g);

        g.gridx=0; g.gridy=2; g.gridwidth=6;
        JButton runBtn = makeButton("Generate Metrics", BLUE);
        runBtn.addActionListener(e -> generateMetricsUI());
        ctrl.add(runBtn, g);
        root.add(ctrl, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setBackground(BG);

        JPanel summaryPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        summaryPanel.setBackground(BG);

        JPanel memPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        memPanel.setBackground(WHITE);
        memPanel.setBorder(BorderFactory.createTitledBorder("Memory Metrics"));
        lblTotalFaults = new JLabel("Total Page Faults: -");
        lblFaultRate = new JLabel("Page Fault Rate: -");
        memPanel.add(lblTotalFaults); memPanel.add(lblFaultRate);

        JPanel diskPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        diskPanel.setBackground(WHITE);
        diskPanel.setBorder(BorderFactory.createTitledBorder("Disk Metrics"));
        lblTotalHeadMovement = new JLabel("Total Head Movement: -");
        lblAvgSeekTime = new JLabel("Average Seek Time: -");
        diskPanel.add(lblTotalHeadMovement); diskPanel.add(lblAvgSeekTime);

        summaryPanel.add(memPanel); summaryPanel.add(diskPanel);
        centerPanel.add(summaryPanel, BorderLayout.NORTH);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(WHITE);
        tablePanel.setBorder(BorderFactory.createTitledBorder("Comparison Table Example"));
        compTableModel = new DefaultTableModel(new String[]{"Algorithm", "Page Faults", "Disk Movement"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        compTable = new JTable(compTableModel);
        compTable.setRowHeight(25);
        tablePanel.add(new JScrollPane(compTable), BorderLayout.CENTER);
        centerPanel.add(tablePanel, BorderLayout.CENTER);

        root.add(centerPanel, BorderLayout.CENTER);
        return root;
    }

    private void generateMetricsUI() {
        String refStr = metRefField.getText().trim();
        if (refStr.isEmpty()) { showErr("Enter reference string."); return; }
        java.util.List<int[]> refs = new java.util.ArrayList<>();
        try {
            for (String tok : refStr.split("\\s+")) {
                String[] pp = tok.split(":");
                refs.add(new int[]{Integer.parseInt(pp[0]), Integer.parseInt(pp[1])});
            }
        } catch (Exception ex) { showErr("Invalid format. Use PID:page."); return; }

        int[] pages = refs.stream().mapToInt(x -> x[1]).toArray();
        int[] pids  = refs.stream().mapToInt(x -> x[0]).toArray();

        int head;
        try { head = Integer.parseInt(metHeadField.getText().trim()); }
        catch (Exception ex) { showErr("Invalid head."); return; }

        String pageAlg = (String) metPageAlgCombo.getSelectedItem();
        String diskAlg = (String) metDiskAlgCombo.getSelectedItem();
        String dir = ((String) metDirCombo.getSelectedItem()).toLowerCase();

        IntegrationHandler handler = new IntegrationHandler();
        IntegrationHandler.MetricsReport rep = handler.generateMetrics(pages, pids, pageAlg, diskAlg, dir, head);

        lblTotalFaults.setText("Total Page Faults: " + rep.totalPageFaults);
        lblFaultRate.setText(String.format("Page Fault Rate: %.2f%%", rep.pageFaultRate));
        lblTotalHeadMovement.setText("Total Head Movement: " + rep.totalDiskMovement);
        lblAvgSeekTime.setText(String.format("Average Seek Time: %.2f", rep.averageSeekTime));

        compTableModel.setRowCount(0);
        for (Object[] row : rep.comparisonData) {
            compTableModel.addRow(row);
        }
    }

    private void runPageReplacement() {
        if(prTimer != null) prTimer.stop();
        ProcessList pl = ProcessList.getInstance();
        String refStr = prRefStringField.getText().trim();
        if (refStr.isEmpty()) { showErr("Enter string."); return; }

        int[] pages;
        try {
            String[] parts = refStr.split("[,\\s]+"); pages = new int[parts.length];
            for (int i = 0; i < parts.length; i++) pages[i] = Integer.parseInt(parts[i].trim());
        } catch (Exception ex) { showErr("Invalid string."); return; }

        String procSel = (String) prProcessCombo.getSelectedItem();
        if (procSel == null || procSel.isEmpty()) { showErr("No process."); return; }
        int pid = Integer.parseInt(procSel.substring(1).split(" ")[0]);
        Process proc = pl.getProcess(pid);

        for (int p : pages) if (!proc.isValidPage(p)) { showErr("Page " + p + " invalid for P" + pid); return; }

        String alg = (String) prAlgorithmCombo.getSelectedItem();
        int frames = pl.getNumberOfFrames();
        PageReplacementHandler handler = new PageReplacementHandler();
        prResult = handler.simulate(pages, frames, alg);

        prTableModel.setRowCount(0); prResultArea.setText("");
        prStatsLabel.setText(String.format("Faults: %d / %d  (%.1f%%)", prResult.totalFaults, pages.length, prResult.faultRate));

        prStep = 0;
        prRamView.reset(frames, alg);
        int initialDelay = prSlider != null ? prSlider.getValue() : 1000;
        prTimer = new Timer(initialDelay, e -> prAnimateStep());
        prTimer.start();
    }

    private void prAnimateStep() {
        if (prResult == null || prStep >= prResult.steps.size()) {
            if(prTimer != null) prTimer.stop(); return;
        }
        PageReplacementHandler.StepResult s = prResult.steps.get(prStep);
        boolean isClock = "Clock".equalsIgnoreCase(prResult.algorithm);

        prRamView.updateState(s.frames, s.refBits, s.page, s.fault);
        StringBuilder fb = new StringBuilder();
        for (int i = 0; i < s.frames.length; i++) {
            if (s.frames[i] == -1) fb.append("-");
            else if (isClock && s.refBits != null) fb.append(s.frames[i]).append("/").append(s.refBits[i] ? "1" : "0");
            else fb.append(s.frames[i]);
            if (i < s.frames.length - 1) fb.append(", ");
        }
        String rep = s.replacedPage >= 0 ? String.valueOf(s.replacedPage) : "-";
        prTableModel.addRow(new Object[]{s.step, s.page, fb.toString(), s.fault ? "FAULT" : "HIT", rep});
        prTable.setRowSelectionInterval(prStep, prStep);
        prTable.scrollRectToVisible(prTable.getCellRect(prStep, 0, true));

        prResultArea.append(String.format("Step %-3d  Page %-3d  %s%n", s.step, s.page, s.fault ? "** FAULT **" : "  HIT"));
        prStep++;
    }

    private JPanel buildDiskSchedulingTab() {
        JPanel root = new JPanel(new BorderLayout(8, 8)); root.setBackground(BG);

        JPanel ctrl = new JPanel(new GridBagLayout()); ctrl.setBackground(WHITE);
        ctrl.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Disk Scheduling Parameters"), BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        GridBagConstraints g = new GridBagConstraints(); g.insets = new Insets(5, 5, 5, 5); g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx=0; g.gridy=0; ctrl.add(new JLabel("Initial Head:"), g);
        g.gridx=1; dsHeadField = new JTextField("53", 8); ctrl.add(dsHeadField, g);
        g.gridx=2; ctrl.add(new JLabel("Algorithm:"), g);
        g.gridx=3; dsAlgorithmCombo = new JComboBox<>(new String[]{"FCFS","SSTF","SCAN","CSCAN","LOOK","CLOOK"}); ctrl.add(dsAlgorithmCombo, g);
        g.gridx=0; g.gridy=1; ctrl.add(new JLabel("Request Queue:"), g);
        g.gridx=1; g.gridwidth=2; dsQueueField = new JTextField("98 183 37 122 14 124 65 67", 20); ctrl.add(dsQueueField, g);
        g.gridwidth=1; g.gridx=3; g.gridy=1;
        JPanel dirPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0)); dirPanel.setOpaque(false);
        dirPanel.add(new JLabel("Dir:")); dsDirectionCombo = new JComboBox<>(new String[]{"right","left"}); dirPanel.add(dsDirectionCombo);
        ctrl.add(dirPanel, g);
        dsAlgorithmCombo.addActionListener(e -> {
            String sel = (String) dsAlgorithmCombo.getSelectedItem();
            dsDirectionCombo.setEnabled(sel != null && (sel.contains("SCAN") || sel.contains("LOOK")));
        }); dsDirectionCombo.setEnabled(false);

        g.gridx=0; g.gridy=2; g.gridwidth=4;
        JButton runBtn = makeButton("Calculate & Start Animation", BLUE);
        runBtn.addActionListener(e -> runDiskScheduling());
        ctrl.add(runBtn, g);
        root.add(ctrl, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(8,8)); centerPanel.setBackground(BG);
        dsDiskView = new DiskView();
        JPanel visPanel = new JPanel(new BorderLayout()); visPanel.setBackground(WHITE);
        visPanel.setBorder(BorderFactory.createTitledBorder("Disk Head Animation"));
        visPanel.add(dsDiskView, BorderLayout.CENTER);
        dsSlider = createSpeedSlider(e -> { if(dsTimer != null) dsTimer.setDelay(((JSlider)e.getSource()).getValue()); });
        visPanel.add(createPlaybackBar(
                e -> { if(dsTimer != null) dsTimer.start(); },
                e -> { if(dsTimer != null) dsTimer.stop(); },
                e -> { if(dsTimer != null) { dsTimer.stop(); dsAnimateStep(); } },
                dsSlider
        ), BorderLayout.SOUTH);
        centerPanel.add(visPanel, BorderLayout.NORTH);

        JPanel logPanel = new JPanel(new BorderLayout()); logPanel.setBackground(WHITE);
        logPanel.setBorder(BorderFactory.createTitledBorder("Log"));
        dsStatsLabel = new JLabel(" "); dsStatsLabel.setFont(new Font("Segoe UI", Font.BOLD, 12)); dsStatsLabel.setForeground(BLUE);
        logPanel.add(dsStatsLabel, BorderLayout.NORTH);
        dsResultArea = new JTextArea(); dsResultArea.setEditable(false); dsResultArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        logPanel.add(new JScrollPane(dsResultArea), BorderLayout.CENTER);
        centerPanel.add(logPanel, BorderLayout.CENTER);

        root.add(centerPanel, BorderLayout.CENTER);
        return root;
    }

    private void runDiskScheduling() {
        if(dsTimer != null) dsTimer.stop();
        ProcessList pl = ProcessList.getInstance();
        int head;
        try { head = Integer.parseInt(dsHeadField.getText().trim()); } catch (Exception e) { showErr("Invalid head."); return; }
        if (head < 0 || head >= pl.getDiskSize()) { showErr("Head 0-" + (pl.getDiskSize()-1)); return; }

        int[] requests;
        try {
            String[] parts = dsQueueField.getText().trim().split("[,\\s]+"); requests = new int[parts.length];
            for (int i = 0; i < parts.length; i++) requests[i] = Integer.parseInt(parts[i].trim());
        } catch (Exception e) { showErr("Invalid queue."); return; }

        String alg = (String) dsAlgorithmCombo.getSelectedItem();
        String dir = (String) dsDirectionCombo.getSelectedItem(); if (dir == null) dir = "right";

        dsResult = DiskSchedulingHandler.run(alg, requests, head, dir, pl.getDiskSize());
        dsResultArea.setText("Algorithm: " + alg + "\nDisk Range: 0 - " + (pl.getDiskSize()-1) + "\nPath:\n");
        dsStatsLabel.setText(String.format(" Total Seek: %d | Avg Seek: %.2f", dsResult.totalSeekDistance, dsResult.avgSeekTime));

        dsStep = 0;
        dsDiskView.reset(pl.getDiskSize(), dsResult.seekSequence);
        int initialDelay = dsSlider != null ? dsSlider.getValue() : 500;
        dsTimer = new Timer(initialDelay, e -> dsAnimateStep());
        dsTimer.start();
    }

    private void dsAnimateStep() {
        if (dsResult == null || dsStep >= dsResult.seekSequence.size()) {
            if(dsTimer != null) dsTimer.stop(); return;
        }
        int pos = dsResult.seekSequence.get(dsStep);
        dsDiskView.stepTo(dsStep);
        dsResultArea.append(pos + (dsStep < dsResult.seekSequence.size() - 1 ? " -> " : "\n"));
        dsStep++;
    }

    private JPanel buildIntegrationTab() {
        JPanel root = new JPanel(new BorderLayout(8, 8)); root.setBackground(BG);

        JPanel ctrl = new JPanel(new GridBagLayout()); ctrl.setBackground(WHITE);
        ctrl.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Integration Parameters"), BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        GridBagConstraints g = new GridBagConstraints(); g.insets = new Insets(5, 5, 5, 5); g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx=0; g.gridy=0; ctrl.add(new JLabel("Ref (PID:page):"), g);
        g.gridx=1; g.gridwidth=3; coreRefField = new JTextField("0:0 0:1 1:0 0:2 1:1 0:3 1:2 0:0", 30); ctrl.add(coreRefField, g); g.gridwidth=1;
        g.gridx=0; g.gridy=1; ctrl.add(new JLabel("Page Alg:"), g);
        g.gridx=1; corePageAlgCombo = new JComboBox<>(new String[]{"FIFO","LRU","OPT","Clock"}); ctrl.add(corePageAlgCombo, g);
        g.gridx=2; ctrl.add(new JLabel("Disk Alg:"), g);
        g.gridx=3; coreDiskAlgCombo = new JComboBox<>(new String[]{"FCFS","SSTF","SCAN","CSCAN","LOOK","CLOOK"}); ctrl.add(coreDiskAlgCombo, g);
        g.gridx=0; g.gridy=2; ctrl.add(new JLabel("Dir:"), g);
        g.gridx=1; coreDirCombo = new JComboBox<>(new String[]{"right","left"}); ctrl.add(coreDirCombo, g);
        g.gridx=2; ctrl.add(new JLabel("Initial Head:"), g);
        g.gridx=3; coreHeadField = new JTextField("53", 8); ctrl.add(coreHeadField, g);
        g.gridx=0; g.gridy=3; g.gridwidth=4;
        JButton runBtn = makeButton("Calculate & Start Animation", GREEN);
        runBtn.addActionListener(e -> runIntegration()); ctrl.add(runBtn, g);
        root.add(ctrl, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(8,8)); centerPanel.setBackground(BG);
        JPanel dashPanel = new JPanel(new GridLayout(1, 3, 5, 0));
        dashPanel.setBackground(BG);
        coreCpuView = new CPUView();
        JPanel cPanel = new JPanel(new BorderLayout()); cPanel.setBackground(WHITE); cPanel.setBorder(BorderFactory.createTitledBorder("CPU")); cPanel.add(coreCpuView, BorderLayout.CENTER);
        coreRamView = new RAMView();
        JPanel rPanel = new JPanel(new BorderLayout()); rPanel.setBackground(WHITE); rPanel.setBorder(BorderFactory.createTitledBorder("RAM")); rPanel.add(coreRamView, BorderLayout.CENTER);
        coreDiskView = new DiskView();
        JPanel dPanel = new JPanel(new BorderLayout()); dPanel.setBackground(WHITE); dPanel.setBorder(BorderFactory.createTitledBorder("Disk")); dPanel.add(coreDiskView, BorderLayout.CENTER);
        dashPanel.add(cPanel); dashPanel.add(rPanel); dashPanel.add(dPanel);
        JPanel dashContainer = new JPanel(new BorderLayout());
        dashContainer.setBackground(BG);
        dashContainer.add(dashPanel, BorderLayout.CENTER);
        coreSlider = createSpeedSlider(e -> { if(coreTimer != null) coreTimer.setDelay(((JSlider)e.getSource()).getValue()); });
        dashContainer.add(createPlaybackBar(
                e -> { if(coreTimer != null) coreTimer.start(); },
                e -> { if(coreTimer != null) coreTimer.stop(); },
                e -> { if(coreTimer != null) { coreTimer.stop(); coreAnimateStep(); } },
                coreSlider
        ), BorderLayout.SOUTH);
        centerPanel.add(dashContainer, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT); split.setDividerLocation(380); split.setResizeWeight(0.5); split.setBorder(null);

        JPanel mapPanel = new JPanel(new BorderLayout()); mapPanel.setBackground(WHITE); mapPanel.setBorder(BorderFactory.createTitledBorder("Process Maps"));
        pageMapModel = new DefaultTableModel(new String[]{"PID","Page","Block","In RAM"}, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        pageMapTable = new JTable(pageMapModel); pageMapTable.setRowHeight(22);
        mapPanel.add(new JScrollPane(pageMapTable), BorderLayout.CENTER);
        coreStatsLabel = new JLabel(" "); coreStatsLabel.setFont(new Font("Segoe UI", Font.BOLD, 11)); coreStatsLabel.setForeground(BLUE); mapPanel.add(coreStatsLabel, BorderLayout.SOUTH);
        split.setLeftComponent(mapPanel);

        JPanel logPanel = new JPanel(new BorderLayout()); logPanel.setBackground(WHITE); logPanel.setBorder(BorderFactory.createTitledBorder("Log"));
        coreResultArea = new JTextArea(); coreResultArea.setEditable(false); coreResultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logPanel.add(new JScrollPane(coreResultArea), BorderLayout.CENTER);
        split.setRightComponent(logPanel);
        centerPanel.add(split, BorderLayout.CENTER);
        root.add(centerPanel, BorderLayout.CENTER);

        refreshPageMapTable();
        return root;
    }

    private void runIntegration() {
        if(coreTimer != null) coreTimer.stop();
        ProcessList pl = ProcessList.getInstance();
        if (pl.getSize() == 0) { showErr("Add processes."); return; }

        String refStr = coreRefField.getText().trim(); java.util.List<int[]> refs = new java.util.ArrayList<>();
        try {
            for (String tok : refStr.split("\\s+")) {
                String[] pp = tok.split(":"); refs.add(new int[]{Integer.parseInt(pp[0]), Integer.parseInt(pp[1])});
            }
        } catch (Exception e) { showErr("Format: PID:page"); return; }
        int[] pages = refs.stream().mapToInt(x -> x[1]).toArray(); int[] pids  = refs.stream().mapToInt(x -> x[0]).toArray();

        int head; try { head = Integer.parseInt(coreHeadField.getText().trim()); } catch (Exception e) { showErr("Invalid head."); return; }
        if (head < 0 || head >= pl.getDiskSize()) { showErr("Head range."); return; }

        String pageAlg = (String) corePageAlgCombo.getSelectedItem();
        String diskAlg = (String) coreDiskAlgCombo.getSelectedItem();
        String dir     = (String) coreDirCombo.getSelectedItem();

        IntegrationHandler handler = new IntegrationHandler();
        coreResult = handler.simulate(pages, pids, pageAlg, diskAlg, dir, head);

        coreResultArea.setText("");
        coreStatsLabel.setText(String.format("Faults: %d  |  Disk Movement: %d", coreResult.totalPageFaults, coreResult.totalDiskMovement));
        refreshPageMapTable();

        coreStep = 0;
        coreCpuView.reset();
        coreRamView.reset(pl.getNumberOfFrames(), pageAlg);
        coreDiskView.reset(pl.getDiskSize(), null); 
        int initialDelay = coreSlider != null ? coreSlider.getValue() : 1000;
        coreTimer = new Timer(initialDelay, e -> coreAnimateStep());
        coreTimer.start();
    }

    private void coreAnimateStep() {
        if (coreResult == null || coreStep >= coreResult.steps.size()) {
            if(coreTimer != null) coreTimer.stop(); return;
        }
        IntegrationHandler.IntegrationStep s = coreResult.steps.get(coreStep);
        coreCpuView.setPid(s.processId, !s.validPage);
        coreRamView.updateState(s.frames, s.clockRefBits, s.page, s.pageFault, s.targetFrame);
        if(s.diskHead != null && s.diskHead.length > 0) {
            java.util.List<Integer> movement = new java.util.ArrayList<>();
            for(int h : s.diskHead) movement.add(h);
            coreDiskView.reset(ProcessList.getInstance().getDiskSize(), movement);
            coreDiskView.stepTo(movement.size()-1);
        }

        for(int r=0; r<pageMapModel.getRowCount(); r++) {
            if(pageMapModel.getValueAt(r, 0).equals("P"+s.processId) && 
               pageMapModel.getValueAt(r, 1).toString().equals(String.valueOf(s.page))) {
                pageMapTable.setRowSelectionInterval(r, r);
                pageMapTable.scrollRectToVisible(pageMapTable.getCellRect(r, 0, true));
                boolean inRam = false;
                for(int f : s.frames) if(f == s.page) { inRam = true; break; } 
                pageMapModel.setValueAt(inRam ? "Yes" : "No", r, 3);
            }
        }

        coreResultArea.append(String.format("Step %d [P%d-Pg%d] %s%n", s.stepNumber, s.processId, s.page, s.message));
        coreStep++;
    }

    public void refreshProcessCombos() {
        ProcessList pl = ProcessList.getInstance();
        if (prProcessCombo != null) {
            prProcessCombo.removeAllItems();
            for (Process p : pl.getProcesses()) prProcessCombo.addItem("P" + p.getPid() + " (Pages: " + p.getNumberOfPages() + ")");
        }
        refreshPageMapTable();
    }

    public void refreshPageMapTable() {
        if (pageMapModel == null) return;
        pageMapModel.setRowCount(0);
        ProcessList pl = ProcessList.getInstance();
        for (Process p : pl.getProcesses()) {
            for (int pg = 0; pg < p.getNumberOfPages(); pg++) {
                int block = p.getDiskBlock(pg);
                pageMapModel.addRow(new Object[]{"P"+p.getPid(), pg, block, "No"});
            }
        }
    }

    public void refreshPerformance() {
        refreshPageMapTable();
        refreshProcessCombos();
    }

    private JButton makeButton(String text, Color bg) {
        JButton btn = new JButton(text); btn.setBackground(bg); btn.setForeground(WHITE); btn.setFocusPainted(false); btn.setFont(new Font("Segoe UI", Font.BOLD, 12)); return btn;
    }

    private JSlider createSpeedSlider(javax.swing.event.ChangeListener speed) {
        JSlider slider = new JSlider(100, 2000, 1000);
        slider.setInverted(true); 
        slider.addChangeListener(speed);
        slider.setBackground(WHITE);
        return slider;
    }

    private JPanel createPlaybackBar(ActionListener play, ActionListener pause, ActionListener step, JSlider slider) {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5)); bar.setBackground(WHITE);
        JButton btnPlay = new JButton("▶ Play"); btnPlay.addActionListener(play); bar.add(btnPlay);
        JButton btnPause = new JButton("⏸ Pause"); btnPause.addActionListener(pause); bar.add(btnPause);
        JButton btnStep = new JButton("⏭ Step"); btnStep.addActionListener(step); bar.add(btnStep);
        bar.add(new JLabel("Speed:"));
        bar.add(slider);
        return bar;
    }

    private void showErr(String msg) { JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE); }


    class RAMView extends JPanel {
        private int capacity = 0;
        private int[] frames;
        private boolean[] refBits;
        private int currentPage = -1;
        private boolean isFault = false;
        private String alg = "";
        private int targetFrameIndex = -1;

        public RAMView() { setPreferredSize(new Dimension(200, 80)); setBackground(WHITE); }

        public void reset(int capacity, String alg) {
            this.capacity = capacity; this.alg = alg;
            frames = new int[capacity]; for(int i=0; i<capacity; i++) frames[i] = -1;
            refBits = new boolean[capacity]; currentPage = -1; isFault = false; targetFrameIndex = -1;
            repaint();
        }

        public void updateState(int[] f, boolean[] rb, int page, boolean fault) {
            this.frames = f; this.refBits = rb; this.currentPage = page; this.isFault = fault; this.targetFrameIndex = -1;
            repaint();
        }

        public void updateState(int[] f, boolean[] rb, int page, boolean fault, int targetFrame) {
            this.frames = f; this.refBits = rb; this.currentPage = page; this.isFault = fault; this.targetFrameIndex = targetFrame;
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if(capacity == 0) { g.drawString("Run simulation...", 10, 20); return; }
            Graphics2D g2 = (Graphics2D) g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int boxW = 40, boxH = 40, gap = 10;
            int totalW = capacity * (boxW + gap) - gap;
            int startX = (getWidth() - totalW) / 2;
            if(startX < 10) startX = 10;
            int y = 20;

            for(int i=0; i<capacity; i++) {
                int x = startX + i * (boxW + gap);
                RoundRectangle2D rect = new RoundRectangle2D.Float(x, y, boxW, boxH, 8, 8);
                if (targetFrameIndex != -1) {
                    if (i == targetFrameIndex) {
                        g2.setColor(isFault ? RED : GREEN);
                        g2.setStroke(new BasicStroke(3));
                    } else {
                        g2.setColor(Color.LIGHT_GRAY);
                        g2.setStroke(new BasicStroke(1));
                    }
                } else {
                    if(frames != null && i < frames.length && frames[i] == currentPage) {
                        g2.setColor(isFault ? RED : GREEN);
                        g2.setStroke(new BasicStroke(3));
                    } else {
                        g2.setColor(Color.LIGHT_GRAY);
                        g2.setStroke(new BasicStroke(1));
                    }
                }
                g2.draw(rect);

                g2.setColor(Color.BLACK);
                g2.setFont(new Font("Arial", Font.BOLD, 14));
                String text = (frames == null || i >= frames.length || frames[i] == -1) ? "-" : String.valueOf(frames[i]);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(text, x + (boxW - fm.stringWidth(text))/2, y + boxH/2 + 5);

                if("Clock".equalsIgnoreCase(alg) && refBits != null && i < refBits.length) {
                    g2.setFont(new Font("Arial", Font.PLAIN, 10));
                    g2.setColor(BLUE);
                    g2.drawString("R:" + (refBits[i]?"1":"0"), x + 5, y - 5);
                }
            }
        }
    }

    class DiskView extends JPanel {
        private int diskSize = 0;
        private List<Integer> seq;
        private int currentIdx = -1;

        public DiskView() { setPreferredSize(new Dimension(200, 80)); setBackground(WHITE); }

        public void reset(int size, List<Integer> seq) {
            this.diskSize = size; this.seq = seq; this.currentIdx = -1; repaint();
        }

        public void stepTo(int idx) {
            this.currentIdx = idx; repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if(diskSize == 0 || seq == null || seq.isEmpty()) { g.drawString("Run simulation...", 10, 20); return; }
            Graphics2D g2 = (Graphics2D) g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int pad = 30;
            int w = getWidth() - pad*2;
            int y = getHeight() / 2;

            g2.setColor(Color.LIGHT_GRAY); g2.setStroke(new BasicStroke(2));
            g2.drawLine(pad, y, pad + w, y);
            g2.setFont(new Font("Arial", Font.PLAIN, 10)); g2.setColor(Color.GRAY);
            g2.drawString("0", pad, y + 20);
            g2.drawString(String.valueOf(diskSize-1), pad + w - 10, y + 20);

            if(currentIdx >= 0) {
                g2.setColor(BLUE); g2.setStroke(new BasicStroke(2));
                for(int i=0; i<currentIdx; i++) {
                    int x1 = pad + (int)((seq.get(i) / (double)(diskSize-1)) * w);
                    int x2 = pad + (int)((seq.get(i+1) / (double)(diskSize-1)) * w);
                    g2.drawLine(x1, y, x2, y);
                }
                int hx = pad + (int)((seq.get(currentIdx) / (double)(diskSize-1)) * w);
                g2.setColor(ORANGE);
                g2.fillOval(hx - 6, y - 6, 12, 12);
                g2.setColor(Color.BLACK);
                g2.drawString(String.valueOf(seq.get(currentIdx)), hx - 5, y - 10);
            }
        }
    }

    class CPUView extends JPanel {
        private int pid = -1;
        private boolean terminated = false;

        public CPUView() { setPreferredSize(new Dimension(100, 80)); setBackground(WHITE); }
        public void reset() { pid = -1; terminated = false; repaint(); }
        public void setPid(int pid, boolean term) { this.pid = pid; this.terminated = term; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int cx = getWidth()/2, cy = getHeight()/2;
            int r = 40;
            if(pid == -1) {
                g2.setColor(Color.LIGHT_GRAY); g2.fillRoundRect(cx-r, cy-r, r*2, r*2, 10, 10);
                g2.setColor(Color.BLACK); g2.drawString("IDLE", cx-15, cy+5);
            } else {
                g2.setColor(terminated ? RED : GREEN);
                g2.fillRoundRect(cx-r, cy-r, r*2, r*2, 10, 10);
                g2.setColor(WHITE); g2.setFont(new Font("Arial", Font.BOLD, 16));
                g2.drawString("P" + pid, cx-10, cy+5);
                if(terminated) {
                    g2.setFont(new Font("Arial", Font.PLAIN, 10));
                    g2.drawString("TERM", cx-14, cy+20);
                }
            }
        }
    }
}