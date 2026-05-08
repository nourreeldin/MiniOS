package UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GUI {

    private JFrame mainFrame;
    private JDesktopPane desktop;
    private static final int WIDTH  = 1280;
    private static final int HEIGHT = 800;
    private String address = "";

    public GUI() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception e) { e.printStackTrace(); }
        initializeDesktop();
    }

    public void setGUIAddress(String address) { this.address = address; }

    private void initializeDesktop() {
        mainFrame = new JFrame("MiniOS");
        mainFrame.setSize(WIDTH, HEIGHT);
        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setResizable(false);
        mainFrame.setLayout(new BorderLayout());
        mainFrame.setIconImage(createAppIcon());

        mainFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) {
                System.out.println(Terminal.RED + "\n>> GUI Terminated." + Terminal.RESET);
                mainFrame.dispose();
                if (!mainFrame.isDisplayable() && address.equals("from menu"))
                    System.out.print("Select an option: ");
            }
        });

        desktop = new JDesktopPane() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, new Color(20, 20, 40),
                        getWidth(), getHeight(), new Color(60, 20, 60));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.setColor(new Color(255, 255, 255, 15));
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 60));
                String watermark = "MINI OS";
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(watermark)) / 2;
                int y = getHeight() / 2;
                g2d.drawString(watermark, x, y);
            }
        };

        JPopupMenu contextMenu = new JPopupMenu();
        JMenuItem itemInput   = new JMenuItem("📄 Input Processes");
        JMenuItem itemCpu     = new JMenuItem("⚙️ CPU Scheduling");
        JMenuItem itemStorage = new JMenuItem("🔮 Storage & Memory");
        JMenuItem itemExit    = new JMenuItem("❌ Exit System");

        itemInput.addActionListener(e   -> openInternalWindow("Process Input",   660, 540));
        itemCpu.addActionListener(e     -> openInternalWindow("CPU Scheduling",  900, 600));
        itemStorage.addActionListener(e -> openInternalWindow("Storage & Memory",1100, 700));
        itemExit.addActionListener(e    -> { System.out.println(Terminal.RED + "\n>> GUI Terminated." + Terminal.RESET); mainFrame.dispose(); });

        contextMenu.add(itemInput);
        contextMenu.add(itemCpu);
        contextMenu.add(itemStorage);
        contextMenu.addSeparator();
        contextMenu.add(itemExit);

        desktop.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e)  { showPopup(e); }
            public void mouseReleased(MouseEvent e) { showPopup(e); }
            private void showPopup(MouseEvent e) {
                if (e.isPopupTrigger()) contextMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        });

        mainFrame.add(desktop, BorderLayout.CENTER);
        mainFrame.add(createTaskbar(), BorderLayout.SOUTH);
        mainFrame.setVisible(true);
    }

    private Image createAppIcon() {
        BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint gp = new GradientPaint(0, 0, new Color(45, 10, 55), 64, 64, new Color(20, 20, 40));
        g2.setPaint(gp); g2.fillRoundRect(2, 2, 60, 60, 15, 15);
        g2.setColor(new Color(100, 149, 237)); g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(2, 2, 60, 60, 15, 15);
        g2.setColor(Color.WHITE); g2.setFont(new Font("Segoe UI", Font.BOLD, 40));
        FontMetrics fm = g2.getFontMetrics();
        String logo = "M";
        int x = (64 - fm.stringWidth(logo)) / 2;
        int y = ((64 - fm.getHeight()) / 2) + fm.getAscent() - 5;
        g2.drawString(logo, x, y);
        g2.dispose();
        return image;
    }

    private JPanel createTaskbar() {
        JPanel taskbar = new JPanel(new GridBagLayout());
        taskbar.setBackground(new Color(32, 32, 32));
        taskbar.setPreferredSize(new Dimension(WIDTH, 50));
        taskbar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(60, 60, 60)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(0, 5, 0, 5);

        JButton startBtn = createTaskbarButton("START", "Start", new Color(0, 120, 215), Color.WHITE);
        startBtn.addActionListener(e -> JOptionPane.showMessageDialog(mainFrame,
                "OS Simulator Project\nTeam Members\n" +
                        "Noureldin Hossam Mahmoud ~ 20232773\n" +
                        "Mohamed Sherif Ibrahim   ~ 20233901\n" +
                        "Marco Fathy Isaac        ~ 20233488\n" +
                        "Nada Abdulhamid Aiad     ~ 20234848"));
        gbc.gridx=0; gbc.weightx=0; gbc.anchor=GridBagConstraints.WEST;
        taskbar.add(startBtn, gbc);

        gbc.gridx=1; gbc.weightx=0.5; gbc.fill=GridBagConstraints.HORIZONTAL;
        taskbar.add(new JLabel(), gbc);

        gbc.weightx=0; gbc.fill=GridBagConstraints.NONE; gbc.anchor=GridBagConstraints.CENTER;
        JButton btnInput   = createTaskbarButton("INPUT",   "Input Processes",  null, new Color(200,200,200));
        JButton btnCpu     = createTaskbarButton("CPU",     "CPU Scheduling",   null, new Color(100,149,237));
        JButton btnStorage = createTaskbarButton("MEMORY",  "Storage & Memory", null, new Color(255,215,0));
        JButton btnExit    = createTaskbarButton("EXIT",    "Exit",             null, new Color(220,60,60));

        btnInput.addActionListener(e   -> openInternalWindow("Process Input",    660, 540));
        btnCpu.addActionListener(e     -> openInternalWindow("CPU Scheduling",   900, 600));
        btnStorage.addActionListener(e -> openInternalWindow("Storage & Memory", 1100, 700));
        btnExit.addActionListener(e    -> {
            System.out.println(Terminal.RED + "\n>> GUI Terminated." + Terminal.RESET);
            mainFrame.dispose();
        });

        gbc.gridx=2; taskbar.add(btnInput, gbc);
        gbc.gridx=3; taskbar.add(btnCpu, gbc);
        gbc.gridx=4; taskbar.add(btnStorage, gbc);
        gbc.gridx=5; taskbar.add(btnExit, gbc);

        gbc.gridx=6; gbc.weightx=0.5; gbc.fill=GridBagConstraints.HORIZONTAL;
        taskbar.add(new JLabel(), gbc);

        JLabel clockLabel = new JLabel();
        clockLabel.setForeground(Color.WHITE);
        clockLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        new Timer(1000, e -> clockLabel.setText(
                new SimpleDateFormat("HH:mm  dd/MM/yyyy").format(new Date()))).start();
        gbc.gridx=7; gbc.weightx=0; gbc.anchor=GridBagConstraints.EAST;
        gbc.insets=new Insets(0,5,0,15); taskbar.add(clockLabel, gbc);

        return taskbar;
    }

    private JButton createTaskbarButton(String iconType, String tooltip, Color hoverColor, Color iconColor) {
        JButton btn = new JButton() {
            private boolean isHovered = false;
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isHovered) {
                    g2.setColor(hoverColor != null ? hoverColor : new Color(255,255,255,30));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
                int iconSize = 24, x = (getWidth()-iconSize)/2, y = (getHeight()-iconSize)/2;
                g2.setColor(iconColor);
                drawIcon(g2, iconType, x, y, iconSize);
                g2.dispose();
            }
        };
        btn.setToolTipText(tooltip);
        Dimension fixedSize = new Dimension(50, 40);
        btn.setPreferredSize(fixedSize); btn.setMinimumSize(fixedSize); btn.setMaximumSize(fixedSize);
        btn.setBorderPainted(false); btn.setFocusPainted(false); btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { setHovered(btn, true); }
            public void mouseExited (java.awt.event.MouseEvent evt) { setHovered(btn, false); }
        });
        return btn;
    }

    private void setHovered(JButton btn, boolean val) {
        try {
            var f = btn.getClass().getDeclaredField("isHovered");
            f.setAccessible(true); f.set(btn, val);
        } catch (Exception ex) {}
        btn.repaint();
    }

    private void drawIcon(Graphics2D g2, String type, int x, int y, int size) {
        switch (type) {
            case "START":
                g2.fillRect(x,    y,    10, 10); g2.fillRect(x+12, y,    10, 10);
                g2.fillRect(x,    y+12, 10, 10); g2.fillRect(x+12, y+12, 10, 10);
                break;
            case "INPUT":
                g2.fillRoundRect(x+4, y+2, 16, 20, 2, 2);
                g2.setColor(Color.DARK_GRAY);
                g2.fillRect(x+7, y+6,  10, 2);
                g2.fillRect(x+7, y+10, 10, 2);
                g2.fillRect(x+7, y+14, 6,  2);
                break;
            case "CPU":
                g2.fillRect(x+2, y+2, 20, 20);
                g2.setColor(new Color(32,32,32));
                g2.fillRect(x+6, y+6, 12, 12);
                Color cpuC = new Color(100,149,237); g2.setColor(cpuC);
                g2.drawLine(x+2, y+6, x,    y+6);  g2.drawLine(x+2, y+10, x,    y+10);
                g2.drawLine(x+2, y+14, x,   y+14); g2.drawLine(x+22, y+6, x+24, y+6);
                g2.drawLine(x+22, y+10, x+24, y+10); g2.drawLine(x+22, y+14, x+24, y+14);
                break;
            case "MEMORY":
                g2.fillRoundRect(x+2, y+6, 20, 12, 2, 2);
                g2.setColor(new Color(32,32,32));
                g2.fillRect(x+5, y+9, 3, 6); g2.fillRect(x+10, y+9, 3, 6); g2.fillRect(x+15, y+9, 3, 6);
                break;
            case "EXIT":
                g2.setStroke(new BasicStroke(3));
                g2.drawLine(x+4, y+4, x+20, y+20); g2.drawLine(x+20, y+4, x+4, y+20);
                break;
        }
    }

    private void openInternalWindow(String title, int width, int height) {
        JInternalFrame iframe = new JInternalFrame(title, true, true, true, true);
        iframe.setSize(width, height);
        int x = (desktop.getWidth()  - width)  / 2;
        int y = (desktop.getHeight() - height) / 2;
        iframe.setLocation(x, y);
        iframe.setLayout(new BorderLayout());

        JPanel content;
        if (title.equals("Process Input")) {
            content = new ProcessInputPanel();
        } else if (title.equals("CPU Scheduling")) {
            content = new CPUSchedulingPanel();
        } else if (title.equals("Storage & Memory")) {
            content = new StoragePanel();
        } else {
            content = new JPanel();
            content.setBackground(new Color(245, 245, 245));
            content.add(new JLabel("<html><center><h1>" + title + "</h1></center></html>"));
        }

        iframe.add(content, BorderLayout.CENTER);
        iframe.setFrameIcon(null);
        iframe.setVisible(true);
        desktop.add(iframe);
        iframe.revalidate();
        iframe.repaint();
        try { iframe.setSelected(true); } catch (Exception e) {}
    }
}