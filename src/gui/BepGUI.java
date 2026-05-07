package gui;

import dao.ChiTietHoaDonDAO;
import socket.SocketClient;
import socket.SocketEvent;
import socket.SocketManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BepGUI extends JPanel {

    private static final Color COLOR_HEADER_BG  = new Color(255, 140, 0);
    private static final Color COLOR_CARD_BG    = Color.WHITE;
    private static final Color COLOR_CARD_BORDER = new Color(255, 140, 0);
    private static final Color COLOR_BTN_XACNHAN = new Color(46, 160, 67);
    private static final Color COLOR_BTN_HOVER   = new Color(34, 130, 50);
    private static final Color COLOR_TEXT_BAN    = new Color(220, 80, 0);
    private static final Font  FONT_BAN_NAME     = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font  FONT_MON          = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font  FONT_BTN          = new Font("Segoe UI", Font.BOLD, 12);

    private final ChiTietHoaDonDAO chiTietDAO = new ChiTietHoaDonDAO();
    private JPanel cardContainer;
    private JLabel lblSoMon;
    private javax.swing.Timer timer;

    public BepGUI() {
        super(new BorderLayout(0, 0));
        setBackground(new Color(248, 248, 248));
        buildUI();
        dangKySocketEvents();
        timer = new javax.swing.Timer(30_000, e -> SwingUtilities.invokeLater(this::refreshCards));
        timer.start();
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                SwingUtilities.invokeLater(BepGUI.this::refreshCards);
            }
        });
    }

    private void buildUI() {
        add(buildHeader(), BorderLayout.NORTH);

        cardContainer = new JPanel(new WrapLayout(FlowLayout.LEFT, 15, 15));
        cardContainer.setBackground(new Color(248, 248, 248));

        JScrollPane scroll = new JScrollPane(cardContainer);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(new Color(248, 248, 248));
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setBackground(COLOR_HEADER_BG);
        header.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel title = new JLabel("Màn hình bếp");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        lblSoMon = new JLabel("0 món đang chờ");
        lblSoMon.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSoMon.setForeground(Color.WHITE);
        right.add(lblSoMon);

        JButton btnRefresh = new JButton("Làm mới");
        btnRefresh.setFont(FONT_BTN);
        btnRefresh.setBackground(Color.WHITE);
        btnRefresh.setForeground(COLOR_TEXT_BAN);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setBorder(new EmptyBorder(6, 14, 6, 14));
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> refreshCards());
        right.add(btnRefresh);

        header.add(right, BorderLayout.EAST);
        return header;
    }

    public void refreshCards() {
        List<Object[]> rows = chiTietDAO.getAllMonDangCho();

        Map<String, BanBepCard> cards = new LinkedHashMap<>();
        for (Object[] r : rows) {
            String maDon  = r[0].toString();
            String maBan  = r[1].toString();
            String tenBan = r[2].toString();
            String maMon  = r[3].toString();
            String tenMon = r[4].toString();
            int soLuong   = ((Number) r[5]).intValue();
            cards.computeIfAbsent(maBan, k -> new BanBepCard(tenBan))
                 .addMon(maDon, maMon, tenMon, soLuong);
        }

        cardContainer.removeAll();
        if (cards.isEmpty()) {
            JLabel empty = new JLabel("Không có món nào đang chờ");
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 15));
            empty.setForeground(Color.GRAY);
            cardContainer.add(empty);
            lblSoMon.setText("0 món đang chờ");
        } else {
            cards.values().forEach(cardContainer::add);
            lblSoMon.setText(rows.size() + " món đang chờ");
        }
        cardContainer.revalidate();
        cardContainer.repaint();
    }

    private void dangKySocketEvents() {
        SocketClient client = SocketManager.getClient();
        if (client == null) return;
        client.subscribe(SocketEvent.ORDER_CREATED,       msg -> SwingUtilities.invokeLater(this::refreshCards));
        client.subscribe(SocketEvent.ORDER_UPDATED,       msg -> SwingUtilities.invokeLater(this::refreshCards));
        client.subscribe(SocketEvent.HOA_DON_THANH_TOAN, msg -> SwingUtilities.invokeLater(this::refreshCards));
        client.subscribe(SocketEvent.KITCHEN_CONFIRM,     msg -> SwingUtilities.invokeLater(this::refreshCards));
    }

    // ── WrapLayout: FlowLayout wraps xuống dòng dựa theo chiều rộng container ─

    private static class WrapLayout extends FlowLayout {
        WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }

        @Override
        public Dimension preferredLayoutSize(Container target) {
            return layoutSize(target, true);
        }

        @Override
        public Dimension minimumLayoutSize(Container target) {
            return layoutSize(target, false);
        }

        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getWidth();
                if (targetWidth == 0) targetWidth = Integer.MAX_VALUE;
                int hgap = getHgap(), vgap = getVgap();
                Insets insets = target.getInsets();
                int maxWidth = targetWidth - insets.left - insets.right - hgap * 2;
                int width = 0, height = insets.top + vgap;
                int rowWidth = 0, rowHeight = 0;
                for (int i = 0; i < target.getComponentCount(); i++) {
                    Component c = target.getComponent(i);
                    if (!c.isVisible()) continue;
                    Dimension d = preferred ? c.getPreferredSize() : c.getMinimumSize();
                    if (rowWidth + d.width > maxWidth && rowWidth > 0) {
                        width = Math.max(width, rowWidth);
                        height += rowHeight + vgap;
                        rowWidth = 0;
                        rowHeight = 0;
                    }
                    if (rowWidth > 0) rowWidth += hgap;
                    rowWidth += d.width;
                    rowHeight = Math.max(rowHeight, d.height);
                }
                width = Math.max(width, rowWidth);
                height += rowHeight + vgap + insets.bottom;
                return new Dimension(width + insets.left + insets.right + hgap * 2, height);
            }
        }
    }

    // ── Card 1 bàn ─────────────────────────────────────────────────────────

    private class BanBepCard extends JPanel {
        private final JPanel monList;

        BanBepCard(String tenBan) {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(COLOR_CARD_BG);
            setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(COLOR_CARD_BORDER, 2, true),
                    new EmptyBorder(10, 12, 12, 12)));

            JLabel lblBan = new JLabel(tenBan);
            lblBan.setFont(FONT_BAN_NAME);
            lblBan.setForeground(COLOR_TEXT_BAN);
            lblBan.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(lblBan);

            JSeparator sep = new JSeparator();
            sep.setForeground(new Color(255, 200, 150));
            sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
            sep.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(Box.createRigidArea(new Dimension(0, 6)));
            add(sep);
            add(Box.createRigidArea(new Dimension(0, 4)));

            monList = new JPanel();
            monList.setLayout(new BoxLayout(monList, BoxLayout.Y_AXIS));
            monList.setOpaque(false);
            monList.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(monList);
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            d.width = Math.max(d.width, 240);
            return d;
        }

        void addMon(String maDon, String maMon, String tenMon, int soLuong) {
            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setOpaque(false);
            row.setBorder(new EmptyBorder(5, 0, 5, 0));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel lblMon = new JLabel("<html><b>x" + soLuong + "</b>  " + tenMon + "</html>");
            lblMon.setFont(FONT_MON);
            row.add(lblMon, BorderLayout.CENTER);

            JButton btnDaLen = new JButton("Đã lên");
            btnDaLen.setFont(FONT_BTN);
            btnDaLen.setBackground(COLOR_BTN_XACNHAN);
            btnDaLen.setForeground(Color.WHITE);
            btnDaLen.setFocusPainted(false);
            btnDaLen.setBorder(new EmptyBorder(4, 10, 4, 10));
            btnDaLen.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnDaLen.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) { btnDaLen.setBackground(COLOR_BTN_HOVER); }
                public void mouseExited(java.awt.event.MouseEvent e)  { btnDaLen.setBackground(COLOR_BTN_XACNHAN); }
            });
            btnDaLen.addActionListener(e -> xacNhan(maDon, maMon, btnDaLen));
            row.add(btnDaLen, BorderLayout.EAST);

            monList.add(row);
            JSeparator sep = new JSeparator();
            sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            sep.setAlignmentX(Component.LEFT_ALIGNMENT);
            monList.add(sep);
        }

        private void xacNhan(String maDon, String maMon, JButton btn) {
            btn.setEnabled(false);
            btn.setText("...");
            new SwingWorker<Boolean, Void>() {
                protected Boolean doInBackground() {
                    return chiTietDAO.xacNhanMon(maDon, maMon);
                }
                protected void done() {
                    try {
                        if (get()) {
                            SocketManager.sendEvent(SocketEvent.KITCHEN_CONFIRM,
                                    Map.of("maDon", maDon, "maMon", maMon));
                        } else {
                            btn.setEnabled(true);
                            btn.setText("Đã lên");
                            JOptionPane.showMessageDialog(BepGUI.this,
                                    "Lỗi xác nhận món!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        btn.setEnabled(true);
                        btn.setText("Đã lên");
                    }
                }
            }.execute();
        }
    }
}
