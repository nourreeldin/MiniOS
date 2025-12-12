package UI;

import MemoryManagement.Paging;
import MemoryManagement.Segmentation;
import Model.Process;
import Model.ProcessList;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MemoryManagementPanel extends JPanel {

    private JTabbedPane tabbedPane;
    private PagingPanel pagingPanel;
    private SegmentationPanel segmentationPanel;

    public MemoryManagementPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));

        tabbedPane = new JTabbedPane();
        pagingPanel = new PagingPanel();
        segmentationPanel = new SegmentationPanel();

        tabbedPane.addTab("Paging", pagingPanel);
        tabbedPane.addTab("Segmentation", segmentationPanel);

        add(tabbedPane, BorderLayout.CENTER);
    }


    private class PagingPanel extends JPanel {
        private JTextField txtTotalMemory, txtFrameSize;
        private JTextArea resultArea;
        private PagingVisualizer visualizer; // New Visual Component
        private Paging pagingSystem;
        private final ProcessList processList;

        public PagingPanel() {
            this.processList = ProcessList.getInstance();
            setLayout(new BorderLayout(10, 10));
            setBackground(new Color(245, 245, 245));
            setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            add(createSetupPanel(), BorderLayout.NORTH);
            add(createResultPanel(), BorderLayout.CENTER);
        }

        private JPanel createSetupPanel() {
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBackground(Color.WHITE);
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder("Paging Setup"),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));

            JPanel configPanel = new JPanel(new GridBagLayout());
            configPanel.setBackground(Color.WHITE);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            gbc.gridx = 0; gbc.gridy = 0;
            configPanel.add(new JLabel("Total Memory:"), gbc);
            gbc.gridx = 1;
            txtTotalMemory = new JTextField(10);
            configPanel.add(txtTotalMemory, gbc);

            gbc.gridx = 0; gbc.gridy = 1;
            configPanel.add(new JLabel("Frame Size:"), gbc);
            gbc.gridx = 1;
            txtFrameSize = new JTextField(10);
            configPanel.add(txtFrameSize, gbc);

            panel.add(configPanel, BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel(new FlowLayout());
            buttonPanel.setBackground(Color.WHITE);

            JButton btnRefresh = new JButton("Refresh Processes");
            styleButton(btnRefresh, new Color(158, 158, 158));
            btnRefresh.addActionListener(e -> refreshProcessList());
            buttonPanel.add(btnRefresh);

            JButton btnAllocate = new JButton("Allocate Memory");
            styleButton(btnAllocate, new Color(46, 125, 50));
            btnAllocate.addActionListener(e -> allocateMemory());
            buttonPanel.add(btnAllocate);

            JButton btnTranslate = new JButton("Translate Address");
            styleButton(btnTranslate, new Color(255, 152, 0));
            btnTranslate.addActionListener(e -> translateAddress());
            buttonPanel.add(btnTranslate);

            panel.add(buttonPanel, BorderLayout.SOUTH);
            return panel;
        }

        private JPanel createResultPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBackground(Color.WHITE);
            panel.setBorder(BorderFactory.createTitledBorder("Results & Memory Map"));

            resultArea = new JTextArea();
            resultArea.setEditable(false);
            resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            JScrollPane logScroll = new JScrollPane(resultArea);
            logScroll.setBorder(BorderFactory.createTitledBorder("Execution Log"));

            visualizer = new PagingVisualizer();
            JScrollPane visualScroll = new JScrollPane(visualizer);
            visualScroll.setBorder(BorderFactory.createTitledBorder("Visual Memory Map"));
            visualScroll.setPreferredSize(new Dimension(400, 250));

            JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, visualScroll, logScroll);
            splitPane.setResizeWeight(0.4); // 40% space for visualizer
            splitPane.setDividerLocation(250);

            panel.add(splitPane, BorderLayout.CENTER);
            return panel;
        }

        private void styleButton(JButton btn, Color color) {
            btn.setBackground(color);
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        }

        private void refreshProcessList() {
            JOptionPane.showMessageDialog(this, "Current processes in system: " + processList.getSize());
        }

        private void allocateMemory() {
            if (processList.getSize() == 0) {
                JOptionPane.showMessageDialog(this, "No processes available. Please add processes first!");
                return;
            }

            try {
                int totalMemory = Integer.parseInt(txtTotalMemory.getText().trim());
                int frameSize = Integer.parseInt(txtFrameSize.getText().trim());

                if (totalMemory <= 0 || frameSize <= 0 || totalMemory % frameSize != 0) {
                    JOptionPane.showMessageDialog(this, "Invalid memory or frame size!\nTotal memory must be divisible by frame size.");
                    return;
                }

                int choice = JOptionPane.showConfirmDialog(this,
                        "Set memory sizes for processes manually?\n(No = use default based on burst time)",
                        "Memory Size Configuration", JOptionPane.YES_NO_CANCEL_OPTION);

                if (choice == JOptionPane.CANCEL_OPTION) return;

                if (choice == JOptionPane.YES_OPTION) {
                    for (Process p : processList.getProcesses()) {
                        String input = JOptionPane.showInputDialog(this, "Enter memory size for Process " + p.getPid() + ":");
                        if (input == null) return;
                        p.setMemorySize(Integer.parseInt(input.trim()));
                    }
                } else {
                    for (Process p : processList.getProcesses()) {
                        p.setMemorySize(p.getBurstTime() * 10);
                    }
                }

                pagingSystem = new Paging(totalMemory, frameSize);
                StringBuilder result = new StringBuilder();

                for (Process p : processList.getProcesses()) {
                    Paging.ProcessInfo processInfo = new Paging.ProcessInfo(p.getPid(), p.getMemorySize(), frameSize);
                    if (pagingSystem.allocateProcess(processInfo)) {
                        result.append("✓ Process ").append(p.getPid()).append(" allocated (Size: ").append(p.getMemorySize()).append(")\n");
                    } else {
                        result.append("✗ Process ").append(p.getPid()).append(" - Not enough memory\n");
                    }
                }

                result.append("\n").append(generatePagingReport(pagingSystem));
                resultArea.setText(result.toString());
                resultArea.setCaretPosition(0);

                visualizer.updateData(pagingSystem);

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers!");
            }
        }

        private void translateAddress() {
            if (pagingSystem == null) {
                JOptionPane.showMessageDialog(this, "Please allocate memory first!");
                return;
            }
            JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
            JTextField txtPid = new JTextField(), txtPage = new JTextField(), txtOffset = new JTextField();
            panel.add(new JLabel("Process ID:")); panel.add(txtPid);
            panel.add(new JLabel("Page Number:")); panel.add(txtPage);
            panel.add(new JLabel("Offset:")); panel.add(txtOffset);

            if (JOptionPane.showConfirmDialog(this, panel, "Translate Address", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    int pid = Integer.parseInt(txtPid.getText().trim());
                    int page = Integer.parseInt(txtPage.getText().trim());
                    int offset = Integer.parseInt(txtOffset.getText().trim());

                    Integer physicalAddr = pagingSystem.translateAddress(pid, page, offset);
                    if (physicalAddr == null) JOptionPane.showMessageDialog(this, "Invalid logical address!");
                    else JOptionPane.showMessageDialog(this, "Physical Address: " + physicalAddr);
                } catch (Exception e) { JOptionPane.showMessageDialog(this, "Invalid Input!"); }
            }
        }

        private String generatePagingReport(Paging paging) {
            StringBuilder sb = new StringBuilder();
            sb.append("=== FRAME TABLE ===\n");
            sb.append(String.format("%-8s %-20s\n", "Frame", "Status"));
            sb.append("----------------------------\n");
            for (int i = 0; i < paging.getTotalFrames(); i++) {
                Paging.FrameInfo frame = paging.getFrameTable().get(i);
                String status = frame.isFree ? "Free" : "P" + frame.processId + " (Pg " + frame.pageNumber + ")";
                sb.append(String.format("%-8d %-20s\n", i, status));
            }
            sb.append("\n=== STATISTICS ===\n");
            sb.append("Free Frames: ").append(paging.getFreeFramesCount()).append(" / ").append(paging.getTotalFrames()).append("\n");
            return sb.toString();
        }

        class PagingVisualizer extends JPanel {
            private Paging data;
            private final int BLOCK_SIZE = 40;
            private final int GAP = 5;

            public void updateData(Paging paging) {
                this.data = paging;
                int totalFrames = paging.getTotalFrames();
                int cols = getWidth() > 0 ? getWidth() / (BLOCK_SIZE + GAP) : 10;
                int rows = (int) Math.ceil((double) totalFrames / cols);
                setPreferredSize(new Dimension(getWidth(), rows * (BLOCK_SIZE + GAP) + 20));
                revalidate();
                repaint();
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (data == null) {
                    g.drawString("Allocate memory to see visualization.", 20, 30);
                    return;
                }

                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int x = 10, y = 10;
                int panelWidth = getWidth();

                Map<Integer, Color> processColors = new HashMap<>();
                Random rand = new Random(123); // Fixed seed for consistent colors

                for (int i = 0; i < data.getTotalFrames(); i++) {
                    Paging.FrameInfo frame = data.getFrameTable().get(i);

                    if (frame.isFree) {
                        g2.setColor(new Color(220, 220, 220)); // Grey for free
                    } else {
                        processColors.putIfAbsent(frame.processId, new Color(rand.nextInt(150), rand.nextInt(150), rand.nextInt(200) + 50));
                        g2.setColor(processColors.get(frame.processId));
                    }

                    g2.fillRoundRect(x, y, BLOCK_SIZE, BLOCK_SIZE, 5, 5);

                    g2.setColor(Color.BLACK);
                    g2.drawRoundRect(x, y, BLOCK_SIZE, BLOCK_SIZE, 5, 5);

                    g2.setFont(new Font("Arial", Font.PLAIN, 9));
                    g2.drawString(String.valueOf(i), x + 2, y + 10);

                    if (!frame.isFree) {
                        g2.setColor(Color.WHITE);
                        g2.setFont(new Font("Arial", Font.BOLD, 10));
                        String txt = "P" + frame.processId;
                        int strW = g2.getFontMetrics().stringWidth(txt);
                        g2.drawString(txt, x + (BLOCK_SIZE - strW)/2, y + BLOCK_SIZE/2 + 4);
                    }

                    x += BLOCK_SIZE + GAP;
                    if (x + BLOCK_SIZE > panelWidth) {
                        x = 10;
                        y += BLOCK_SIZE + GAP;
                    }
                }
            }
        }
    }


    private class SegmentationPanel extends JPanel {
        private JTextField txtTotalMemory;
        private JTextArea resultArea;
        private SegmentationVisualizer visualizer;
        private Segmentation segmentationSystem;
        private final ProcessList processList;

        public SegmentationPanel() {
            this.processList = ProcessList.getInstance();
            setLayout(new BorderLayout(10, 10));
            setBackground(new Color(245, 245, 245));
            setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            add(createSetupPanel(), BorderLayout.NORTH);
            add(createResultPanel(), BorderLayout.CENTER);
        }

        private JPanel createSetupPanel() {
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBackground(Color.WHITE);
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder("Segmentation Setup"),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));

            JPanel configPanel = new JPanel(new GridBagLayout());
            configPanel.setBackground(Color.WHITE);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);

            gbc.gridx = 0; gbc.gridy = 0;
            configPanel.add(new JLabel("Total Memory:"), gbc);
            gbc.gridx = 1;
            txtTotalMemory = new JTextField(10);
            configPanel.add(txtTotalMemory, gbc);

            panel.add(configPanel, BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel(new FlowLayout());
            buttonPanel.setBackground(Color.WHITE);

            JButton btnRefresh = new JButton("Refresh Processes");
            styleButton(btnRefresh, new Color(158, 158, 158));
            btnRefresh.addActionListener(e -> refreshProcessList());
            buttonPanel.add(btnRefresh);

            JButton btnAllocate = new JButton("Allocate Memory");
            styleButton(btnAllocate, new Color(46, 125, 50));
            btnAllocate.addActionListener(e -> allocateMemory());
            buttonPanel.add(btnAllocate);

            JButton btnTranslate = new JButton("Translate Address");
            styleButton(btnTranslate, new Color(255, 152, 0));
            btnTranslate.addActionListener(e -> translateAddress());
            buttonPanel.add(btnTranslate);

            panel.add(buttonPanel, BorderLayout.SOUTH);
            return panel;
        }

        private void styleButton(JButton btn, Color color) {
            btn.setBackground(color);
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        }

        private JPanel createResultPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBackground(Color.WHITE);
            panel.setBorder(BorderFactory.createTitledBorder("Results & Memory Map"));

            resultArea = new JTextArea();
            resultArea.setEditable(false);
            resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            JScrollPane logScroll = new JScrollPane(resultArea);
            logScroll.setBorder(BorderFactory.createTitledBorder("Execution Log"));

            visualizer = new SegmentationVisualizer();
            JScrollPane visualScroll = new JScrollPane(visualizer);
            visualScroll.setBorder(BorderFactory.createTitledBorder("Visual Memory Map"));
            visualScroll.setPreferredSize(new Dimension(400, 150));
            visualScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

            JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, visualScroll, logScroll);
            splitPane.setResizeWeight(0.3);
            splitPane.setDividerLocation(150);

            panel.add(splitPane, BorderLayout.CENTER);
            return panel;
        }

        private void refreshProcessList() {
            JOptionPane.showMessageDialog(this, "Current processes in system: " + processList.getSize());
        }

        private void allocateMemory() {
            if (processList.getSize() == 0) {
                JOptionPane.showMessageDialog(this, "No processes available. Please add processes first!");
                return;
            }

            try {
                int totalMemory = Integer.parseInt(txtTotalMemory.getText().trim());
                if (totalMemory <= 0) {
                    JOptionPane.showMessageDialog(this, "Invalid memory size!");
                    return;
                }

                segmentationSystem = new Segmentation(totalMemory);
                StringBuilder result = new StringBuilder();

                for (Process p : processList.getProcesses()) {
                    String input = JOptionPane.showInputDialog(this, "Enter number of segments for Process " + p.getPid() + ":");
                    if (input == null) return;

                    int numSegments = Integer.parseInt(input.trim());
                    Segmentation.ProcessInfo processInfo = new Segmentation.ProcessInfo(p.getPid());

                    for (int j = 0; j < numSegments; j++) {
                        JPanel segPanel = new JPanel(new GridLayout(2, 2, 5, 5));
                        JTextField txtName = new JTextField("Code");
                        JTextField txtSize = new JTextField();
                        segPanel.add(new JLabel("Segment Name:")); segPanel.add(txtName);
                        segPanel.add(new JLabel("Size:")); segPanel.add(txtSize);

                        int segResult = JOptionPane.showConfirmDialog(this, segPanel,
                                "Process " + p.getPid() + " - Segment " + (j+1), JOptionPane.OK_CANCEL_OPTION);
                        if (segResult != JOptionPane.OK_OPTION) return;

                        processInfo.addSegment(txtName.getText(), Integer.parseInt(txtSize.getText().trim()));
                    }

                    if (segmentationSystem.allocateProcess(processInfo)) {
                        result.append("✓ Process ").append(p.getPid()).append(" allocated.\n");
                    } else {
                        result.append("✗ Process ").append(p.getPid()).append(" - Not enough memory.\n");
                    }
                }

                result.append("\n").append(generateSegmentationReport(segmentationSystem));
                resultArea.setText(result.toString());
                resultArea.setCaretPosition(0);

                visualizer.updateData(segmentationSystem);

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers!");
            }
        }

        private void translateAddress() {
            if (segmentationSystem == null) {
                JOptionPane.showMessageDialog(this, "Please allocate memory first!");
                return;
            }
            JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
            JTextField txtPid = new JTextField(), txtSegment = new JTextField(), txtOffset = new JTextField();
            panel.add(new JLabel("Process ID:")); panel.add(txtPid);
            panel.add(new JLabel("Segment No:")); panel.add(txtSegment);
            panel.add(new JLabel("Offset:")); panel.add(txtOffset);

            if (JOptionPane.showConfirmDialog(this, panel, "Translate Address", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    int pid = Integer.parseInt(txtPid.getText().trim());
                    int segment = Integer.parseInt(txtSegment.getText().trim());
                    int offset = Integer.parseInt(txtOffset.getText().trim());

                    Integer physicalAddr = segmentationSystem.translateAddress(pid, segment, offset);
                    if (physicalAddr == null) JOptionPane.showMessageDialog(this, "Invalid Address/Offset!");
                    else JOptionPane.showMessageDialog(this, "Physical Address: " + physicalAddr);
                } catch (Exception e) { JOptionPane.showMessageDialog(this, "Invalid Input!"); }
            }
        }

        private String generateSegmentationReport(Segmentation segmentation) {
            StringBuilder sb = new StringBuilder();
            for (int pid : segmentation.getProcessSegmentTables().keySet()) {
                Segmentation.SegmentTable st = segmentation.getProcessSegmentTables().get(pid);
                sb.append("=== Process ").append(pid).append(" ===\n");
                sb.append(String.format("%-4s %-10s %-8s %-8s\n", "#", "Name", "Base", "Limit"));
                sb.append("------------------------------------\n");
                for (int i = 0; i < st.segments.size(); i++) {
                    Segmentation.Segment seg = st.segments.get(i);
                    sb.append(String.format("%-4d %-10s %-8d %-8d\n", i, seg.name, seg.base, seg.limit));
                }
                sb.append("\n");
            }
            sb.append("Total Memory Used: ").append(segmentation.getUsedMemory()).append(" / ").append(segmentation.getTotalMemory());
            return sb.toString();
        }

        class SegmentationVisualizer extends JPanel {
            private Segmentation data;

            public void updateData(Segmentation seg) {
                this.data = seg;
                repaint();
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (data == null) {
                    g.drawString("Allocate memory to see visualization.", 20, 50);
                    return;
                }

                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth() - 40;
                int h = 60;
                int xStart = 20;
                int yStart = 40;

                g2.setColor(Color.LIGHT_GRAY);
                g2.fillRoundRect(xStart, yStart, w, h, 10, 10);
                g2.setColor(Color.BLACK);
                g2.drawRoundRect(xStart, yStart, w, h, 10, 10);

                g2.drawString("0", xStart, yStart + h + 15);
                String totalMemStr = String.valueOf(data.getTotalMemory());
                g2.drawString(totalMemStr, xStart + w - g2.getFontMetrics().stringWidth(totalMemStr), yStart + h + 15);

                double scale = (double) w / data.getTotalMemory();
                Random rand = new Random(456);

                Map<Integer, Segmentation.SegmentTable> tables = data.getProcessSegmentTables();
                for (Integer pid : tables.keySet()) {
                    Color pColor = new Color(rand.nextInt(150), rand.nextInt(150), rand.nextInt(200) + 50);
                    for (Segmentation.Segment seg : tables.get(pid).segments) {
                        int segX = xStart + (int) (seg.base * scale);
                        int segW = (int) (seg.limit * scale);
                        if (segW < 2) segW = 2;

                        g2.setColor(pColor);
                        g2.fillRect(segX, yStart, segW, h);

                        g2.setColor(Color.WHITE);
                        g2.drawRect(segX, yStart, segW, h);

                        if (segW > 20) {
                            g2.setFont(new Font("Arial", Font.PLAIN, 10));
                            g2.drawString("P"+pid, segX + 2, yStart + h/2);
                        }
                    }
                }
            }
        }
    }
}