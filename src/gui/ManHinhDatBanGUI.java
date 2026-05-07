package gui; // Hoặc package của bạn

import dao.BanDAO;
import dao.DonDatMonDAO; // Sẽ cần sau
import dao.KhachHangDAO; // Sẽ cần sau
import entity.Ban;
import entity.TrangThaiBan; // Cần import này

import java.awt.event.*;

import javax.swing.SpinnerDateModel;
import java.util.Date;
import java.util.Calendar;
import java.time.ZoneId;
import java.time.LocalDate;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.time.LocalDateTime;
import java.awt.*;
import java.time.LocalTime; // Cho giờ
import java.time.format.DateTimeFormatter; // Cho giờ
import java.util.ArrayList;
import java.util.List;
import entity.DonDatMon; // Cần import DonDatMon
import entity.KhachHang; // Cần import KhachHang
import javax.swing.event.ListSelectionListener; // Có thể cần nếu muốn xử lý chọn item
import javax.swing.event.ListSelectionEvent;

public class ManHinhDatBanGUI extends JPanel {

    private BanDAO banDAO;
    private KhachHangDAO khachHangDAO;
    private DonDatMonDAO donDatMonDAO;
    private DanhSachBanGUI parentDanhSachBanGUI_DatBan;

    private JSpinner spinnerSoLuongKhach;
    private JSpinner dateSpinner;
    private JSpinner timeSpinner;
    private JTextField txtGhiChu;
    private JPanel pnlBanContainer;
    private List<Ban> dsTatCaBan;
    private List<Ban> dsBanDaChon = new ArrayList<>();
    private List<BanPanel> dsBanPanelHienThi = new ArrayList<>();
    private JTextField txtSDTKhach;
    private JTextField txtHoTenKhach;
    private JButton btnDatBan;
    private MainGUI mainGUI_DatBan;

    private JTextField txtTimKiemPhieuDat;
    private JList<DonDatMon> listPhieuDat;
    private DefaultListModel<DonDatMon> modelListPhieuDat;

    private static final Color COLOR_ACCENT_BLUE = new Color(56, 118, 243);
    private String maNVDangNhap = "";

    public ManHinhDatBanGUI(DanhSachBanGUI parent, MainGUI main) {
        this(parent, main, "");
    }

    public ManHinhDatBanGUI(DanhSachBanGUI parent, MainGUI main, String maNVDangNhap) {
        this.parentDanhSachBanGUI_DatBan = parent;
        this.mainGUI_DatBan = main;
        this.maNVDangNhap = (maNVDangNhap != null && !maNVDangNhap.isEmpty()) ? maNVDangNhap : "";
        banDAO = new BanDAO();
        khachHangDAO = new KhachHangDAO();
        donDatMonDAO = new DonDatMonDAO();
        setLayout(new BorderLayout());
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(610);
        splitPane.setBorder(null);
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 0, 10, 10));


        JPanel pnlLeft = createLeftPanel_DatBan();
        JPanel pnlRight = createRightPanel();

        splitPane.setLeftComponent(pnlLeft);
        splitPane.setRightComponent(pnlRight);
        add(splitPane, BorderLayout.CENTER);

        taiDanhSachBanTrong();
        hienThiBanPhuHop();
        loadDanhSachDatTruoc();
    }

    private JPanel createLeftPanel_DatBan() {
        JPanel panel = new JPanel(new BorderLayout(10, 15));
        panel.setBackground(Color.WHITE);

        JPanel pnlInputNorth = createInputNorthPanel();
        panel.add(pnlInputNorth, BorderLayout.NORTH);

        JPanel listBanPanel = createListBanPanel_DatBan("Chọn Bàn Trống Phù Hợp");
        panel.add(listBanPanel, BorderLayout.CENTER);

        JPanel pnlInputSouth = createInputSouthPanel();
        panel.add(pnlInputSouth, BorderLayout.SOUTH);

        return panel;
    }
    private JPanel createListBanPanel_DatBan(String title) {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 5, 0, 5));

        JPanel headerPanel = new JPanel(new BorderLayout(0, 5));
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        headerPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel fakeFilterPanel = new JPanel();
        fakeFilterPanel.setOpaque(false);
        panel.add(headerPanel, BorderLayout.NORTH);

        pnlBanContainer = new VerticallyWrappingFlowPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        pnlBanContainer.setBackground(Color.WHITE);
        pnlBanContainer.setBorder(new EmptyBorder(5, 5, 5, 5));

        JScrollPane scrollPane = new JScrollPane(pnlBanContainer);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }
    private List<List<Ban>> timGoiYGhepBan(int soLuongKhach,List<Ban> sourceList) {
        List<List<Ban>> dsGoiY = new ArrayList<>();

        java.util.Map<String, List<Ban>> banTheoKhuVuc = new java.util.HashMap<>();
        for (Ban ban : sourceList) {
            banTheoKhuVuc.computeIfAbsent(ban.getKhuVuc(), k -> new ArrayList<>()).add(ban);
        }

        for (String khuVuc : banTheoKhuVuc.keySet()) {
            List<Ban> bansInZone = banTheoKhuVuc.get(khuVuc);

            int tongSucChuaKhuVuc = bansInZone.stream().mapToInt(Ban::getSoGhe).sum();

            if (tongSucChuaKhuVuc < soLuongKhach) {
                continue;
            }
            bansInZone.sort((b1, b2) -> Integer.compare(b2.getSoGhe(), b1.getSoGhe()));

            timToHopBan(bansInZone, soLuongKhach, 0, new ArrayList<>(), dsGoiY);
        }

        dsGoiY.sort((list1, list2) -> Integer.compare(list1.size(), list2.size()));

        if (dsGoiY.size() > 3) {
            return dsGoiY.subList(0, 3);
        }
        return dsGoiY;
    }
    private void timToHopBan(List<Ban> bans, int target, int index, List<Ban> current, List<List<Ban>> results) {
        if (results.size() >= 50) return;

        int currentSeats = current.stream().mapToInt(Ban::getSoGhe).sum();

        if (currentSeats >= target) {
            if (currentSeats - target <= 8) {
                results.add(new ArrayList<>(current));
            }
            return;
        }

        if (current.size() >= 10) {
            return;
        }
        for (int i = index; i < bans.size(); i++) {
            current.add(bans.get(i));
            timToHopBan(bans, target, i + 1, current, results);
            current.remove(current.size() - 1);
        }
    }
    private JPanel createInputNorthPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false); // Nền trong suốt
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0.33;
        JLabel lblSoLuong = new JLabel("Số lượng khách");
        lblSoLuong.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 0;
        panel.add(lblSoLuong, gbc);

        JLabel lblNgayDat = new JLabel("Ngày đặt:");
        lblNgayDat.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 1; panel.add(lblNgayDat, gbc);

        JLabel lblGioDat = new JLabel("Giờ đặt:");
        lblGioDat.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 2; panel.add(lblGioDat, gbc);

        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 5, 10, 5);

        spinnerSoLuongKhach = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
        spinnerSoLuongKhach.addChangeListener(e -> hienThiBanPhuHop());
        applySpinnerStyle(spinnerSoLuongKhach);
        gbc.gridx = 0;
        panel.add(spinnerSoLuongKhach, gbc);

        Date earliestDate = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
        SpinnerDateModel dateModel = new SpinnerDateModel(new Date(),
                earliestDate,
                null,
                Calendar.DAY_OF_MONTH);
        dateSpinner = new JSpinner(dateModel);
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "dd/MM/yyyy"));
        applySpinnerStyle(dateSpinner);
        gbc.gridx = 1; panel.add(dateSpinner, gbc);
        dateSpinner.addChangeListener(e -> hienThiBanPhuHop());

        SpinnerDateModel timeModel = new SpinnerDateModel();
        timeSpinner = new JSpinner(timeModel);
        timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, "HH:mm"));
        applySpinnerStyle(timeSpinner);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, 1);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        timeSpinner.setValue(cal.getTime());
        gbc.gridx = 2; panel.add(timeSpinner, gbc);
        timeSpinner.addChangeListener(e -> hienThiBanPhuHop());

        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 2, 5);
        JLabel lblGhiChu = new JLabel("Ghi chú:");
        lblGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(lblGhiChu, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 5, 10, 5);
        txtGhiChu = new JTextField();
        applyTextFieldStyle(txtGhiChu);
        panel.add(txtGhiChu, gbc);

        return panel;
    }


    private JPanel createInputSouthPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 5, 2, 10);

        JLabel lblSDT = new JLabel("SĐT khách:");
        lblSDT.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridy = 0; // Hàng label
        panel.add(lblSDT, gbc);

        txtSDTKhach = new JTextField();
        applyTextFieldStyle(txtSDTKhach);
        txtSDTKhach.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                timKhachHangTheoSDT();
            }
        });
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 5, 15, 10);
        panel.add(txtSDTKhach, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.5; // Chia đều không gian ngang
        gbc.insets = new Insets(0, 10, 2, 5);
        JLabel lblTen = new JLabel("Họ tên khách:");
        lblTen.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridy = 0;
        panel.add(lblTen, gbc);

        txtHoTenKhach = new JTextField();
        applyTextFieldStyle(txtHoTenKhach);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 10, 15, 5);
        panel.add(txtHoTenKhach, gbc);

        btnDatBan = new JButton("ĐẶT BÀN");
        btnDatBan.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnDatBan.setBackground(COLOR_ACCENT_BLUE); // Màu xanh dương
        btnDatBan.setForeground(Color.WHITE);
        btnDatBan.setFocusPainted(false);
        btnDatBan.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDatBan.addActionListener(e -> xuLyDatBan());
        btnDatBan.setBorder(BorderFactory.createCompoundBorder(
                btnDatBan.getBorder(),
                new EmptyBorder(10, 30, 10, 30)
        ));

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 5, 5, 5);
        panel.add(btnDatBan, gbc);

        return panel;
    }

    private void applyTextFieldStyle(JTextField tf) {
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)), // Viền xám nhạt
                new EmptyBorder(5, 8, 5, 8) // Padding bên trong
        ));
        tf.setPreferredSize(new Dimension(100, 35)); // Ưu tiên chiều cao
    }
    private void applySpinnerStyle(JSpinner spinner) {
        spinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField textField = ((JSpinner.DefaultEditor) editor).getTextField();
            textField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    new EmptyBorder(5, 8, 5, 8)
            ));
            textField.setBackground(Color.WHITE);
        }
        spinner.setPreferredSize(new Dimension(100, 35));
    }
    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 10, 15, 15));
        panel.setBackground(new Color(245, 245, 245));

        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setOpaque(false);
        searchPanel.setBorder(new EmptyBorder(0, 0, 5, 0));

        JLabel searchIcon = new JLabel("🔎");
        searchIcon.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 16));
        searchPanel.add(searchIcon, BorderLayout.WEST);

        final String placeholder = " Tìm kiếm bàn đặt SĐT/Tên khách...";
        txtTimKiemPhieuDat = new JTextField(placeholder);
        txtTimKiemPhieuDat.setForeground(Color.GRAY);
        applyTextFieldStyle(txtTimKiemPhieuDat);
        txtTimKiemPhieuDat.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtTimKiemPhieuDat.getText().equals(placeholder)) {
                    txtTimKiemPhieuDat.setText("");
                    txtTimKiemPhieuDat.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (txtTimKiemPhieuDat.getText().isEmpty()) {
                    txtTimKiemPhieuDat.setForeground(Color.GRAY);
                    txtTimKiemPhieuDat.setText(placeholder);
                }
            }
        });

        txtTimKiemPhieuDat.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                timKiemPhieuDat();
            }
        });
        searchPanel.add(txtTimKiemPhieuDat, BorderLayout.CENTER);
        panel.add(searchPanel, BorderLayout.NORTH);
        modelListPhieuDat = new DefaultListModel<>();
        listPhieuDat = new JList<>(modelListPhieuDat);
        listPhieuDat.setCellRenderer(new PhieuDatListRenderer());
        listPhieuDat.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listPhieuDat.setBackground(Color.WHITE);

        listPhieuDat.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = listPhieuDat.locationToIndex(e.getPoint());
                if (index != -1) {
                    DonDatMon ddm = modelListPhieuDat.getElementAt(index);
                    Rectangle itemBounds = listPhieuDat.getCellBounds(index, index);

                    Component rendererComp = listPhieuDat.getCellRenderer().getListCellRendererComponent(listPhieuDat, ddm, index, false, false);
                    Component mainPanelComp = null;
                    Component deleteBtnComp = null;

                    if (rendererComp instanceof JPanel) {
                        mainPanelComp = ((JPanel) rendererComp).getComponent(0);
                        if (mainPanelComp instanceof JPanel) {
                            deleteBtnComp = ((JPanel) mainPanelComp).getComponent(1);
                        }
                    }

                    if (deleteBtnComp instanceof JButton && mainPanelComp instanceof JPanel) {
                        JButton btnDelete = (JButton) deleteBtnComp;
                        JPanel itemMainPanel = (JPanel) mainPanelComp;
                        Insets borderInsets = new Insets(0,0,0,0);
                        Border border = itemMainPanel.getBorder();
                        if (border != null) {
                            borderInsets = border.getBorderInsets(itemMainPanel);
                        }

                        int btnX = itemBounds.x + itemBounds.width - btnDelete.getWidth()
                                - borderInsets.right
                                - ((BorderLayout)itemMainPanel.getLayout()).getHgap();
                        int btnY = itemBounds.y + (itemBounds.height - btnDelete.getHeight()) / 2;

                        Rectangle deleteButtonBounds = new Rectangle(btnX, btnY, btnDelete.getWidth(), btnDelete.getHeight());

                        if (deleteButtonBounds.contains(e.getPoint())) {
                            System.out.println("Clicked delete button for: " + ddm.getMaDon());
                            xuLyHuyDatBan(ddm, index);
                        }
                    }
                }
            }
        });

        JScrollPane scrollPaneList = new JScrollPane(listPhieuDat);
        scrollPaneList.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.add(scrollPaneList, BorderLayout.CENTER);

        return panel;
    }
    private void timKiemPhieuDat() {
        String query = txtTimKiemPhieuDat.getText().trim();
        final String placeholder = " Tìm kiếm bàn đặt SĐT/Tên khách...";

        modelListPhieuDat.clear();

        try {
            List<entity.DonDatMon> dsKetQua;

            if (query.isEmpty() || query.equals(placeholder)) {
                dsKetQua = donDatMonDAO.getAllDonDatMonChuaNhan();
            } else {
                dsKetQua = donDatMonDAO.timDonDatMonChuaNhan(query);
            }

            if (dsKetQua.isEmpty() && !(query.isEmpty() || query.equals(placeholder))) {
                modelListPhieuDat.addElement(null);
            } else if (dsKetQua.isEmpty() && (query.isEmpty() || query.equals(placeholder))) {
                modelListPhieuDat.addElement(null);
            }
            else {
                for (entity.DonDatMon ddm : dsKetQua) {
                    modelListPhieuDat.addElement(ddm);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tìm kiếm phiếu đặt: " + e.getMessage());
            modelListPhieuDat.clear();
            modelListPhieuDat.addElement(null);
        }
        listPhieuDat.setModel(modelListPhieuDat);
        listPhieuDat.repaint();
    }
    private void taiDanhSachBanTrong() {
        try {
            dsTatCaBan = banDAO.getAllBan();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hienThiBanPhuHop() {
        int soLuongKhach = (Integer) spinnerSoLuongKhach.getValue();
        LocalDateTime thoiGianDat = null;

        try {
            Date d = (Date) dateSpinner.getValue();
            Date t = (Date) timeSpinner.getValue();
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            Calendar timeCal = Calendar.getInstance();
            timeCal.setTime(t);
            cal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
            cal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
            thoiGianDat = cal.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        } catch (Exception e) { return; }

        LocalDateTime startCheck = thoiGianDat.minusHours(2);
        LocalDateTime endCheck = thoiGianDat.plusHours(2);

        List<String> maBanBan = donDatMonDAO.getMaBanDaDatTrongKhoang(startCheck, endCheck);

        boolean isBookingNow = java.time.Duration.between(LocalDateTime.now(), thoiGianDat).abs().toMinutes() < 30;

        pnlBanContainer.removeAll();
        dsBanPanelHienThi.clear();
        dsBanDaChon.clear();

        List<Ban> dsBanKhaDung = new ArrayList<>();
        if (dsTatCaBan != null) {
            for (Ban ban : dsTatCaBan) {
                boolean isBusy = false;

                if (maBanBan.contains(ban.getMaBan())) {
                    isBusy = true;
                }

                if (isBookingNow && ban.getTrangThai() == TrangThaiBan.DANG_PHUC_VU) {
                    isBusy = true;
                }

                if (!isBusy) {
                    dsBanKhaDung.add(ban);
                }
            }
        }

        boolean coBanDon = false;
        for (Ban ban : dsBanKhaDung) {
            if (ban.getSoGhe() >= soLuongKhach) {
                coBanDon = true;
                addBanPanelToView(ban);
            }
        }

        if (!coBanDon) {
            List<List<Ban>> dsGoiY = timGoiYGhepBan(soLuongKhach, dsBanKhaDung);

            if (!dsGoiY.isEmpty()) {
                JLabel lblGoiY = new JLabel("<html>Không có bàn đơn đủ chỗ. Gợi ý ghép bàn trống lúc " +
                        thoiGianDat.format(DateTimeFormatter.ofPattern("HH:mm")) + ":</html>");
                lblGoiY.setForeground(Color.BLUE);
                pnlBanContainer.add(lblGoiY);
                for (List<Ban> capBan : dsGoiY) {
                    createNutGhepBan(capBan);
                }
            } else {
                pnlBanContainer.add(new JLabel("Không có bàn trống phù hợp vào giờ này."));
            }
        }

        pnlBanContainer.revalidate();
        pnlBanContainer.repaint();
    }

    private void createNutGhepBan(List<Ban> groupBan) {
        int tongGhe = groupBan.stream().mapToInt(Ban::getSoGhe).sum();
        String khuVuc = groupBan.get(0).getKhuVuc();

        StringBuilder sb = new StringBuilder("<html><center>");
        sb.append("Ghép ").append(groupBan.size()).append(" bàn:<br><b>");
        for (int i = 0; i < groupBan.size(); i++) {
            sb.append(groupBan.get(i).getTenBan());
            if (i < groupBan.size() - 1) sb.append(", ");
            if ((i + 1) % 2 == 0 && i < groupBan.size() - 1) sb.append("<br>");
        }
        sb.append("</b><br><i>(Tổng ").append(tongGhe).append(" ghế)</i></center></html>");

        JToggleButton btnGhep = new JToggleButton();
        btnGhep.setLayout(new BorderLayout());

        btnGhep.setPreferredSize(new Dimension(180, 100));
        btnGhep.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGhep.setBackground(Color.WHITE);
        btnGhep.setForeground(Color.BLACK);

        JLabel lblIcon = new JLabel("🔗", SwingConstants.CENTER); // Icon liên kết
        lblIcon.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 24));

        JLabel lblInfo = new JLabel(sb.toString(), SwingConstants.CENTER);

        btnGhep.add(lblIcon, BorderLayout.WEST);
        btnGhep.add(lblInfo, BorderLayout.CENTER);

        btnGhep.addActionListener(e -> {
            dsBanDaChon.clear();
            dsBanDaChon.addAll(groupBan);

            for (BanPanel bp : dsBanPanelHienThi) bp.setSelected(false);

            Component[] comps = pnlBanContainer.getComponents();
            for (Component c : comps) {
                if (c instanceof JToggleButton && c != btnGhep) {
                    ((JToggleButton)c).setSelected(false);
                    c.setForeground(Color.BLACK);
                    c.setBackground(Color.WHITE);
                    updateLabelsColor((JToggleButton)c, Color.BLACK);
                }
            }

            if (btnGhep.isSelected()) {
                btnGhep.setBackground(new Color(56, 118, 243)); // Màu cam nhạt
                btnGhep.setForeground(Color.WHITE);
                updateLabelsColor(btnGhep, Color.WHITE);
            } else {
                dsBanDaChon.clear(); // Bỏ chọn
                btnGhep.setBackground(Color.WHITE);
                btnGhep.setForeground(Color.BLACK);
                updateLabelsColor(btnGhep, Color.BLACK);
            }
        });

        pnlBanContainer.add(btnGhep);
    }
    private void updateLabelsColor(JToggleButton button, Color color) {
        for (Component c : button.getComponents()) {
            if (c instanceof JLabel) {
                c.setForeground(color);
            }
        }
    }
    private void updateBanPanelSelection() {
        for (BanPanel panel : dsBanPanelHienThi) {
            boolean isSelected = false;
            for (Ban b : dsBanDaChon) {
                if (panel.getBan().equals(b)) {
                    isSelected = true;
                    break;
                }
            }
            panel.setSelected(isSelected);
        }
        if (!dsBanDaChon.isEmpty() && dsBanDaChon.size() == 1) {
            Component[] comps = pnlBanContainer.getComponents();
            for (Component c : comps) {
                if (c instanceof JToggleButton) {
                    ((JToggleButton)c).setSelected(false);
                    c.setBackground(Color.WHITE);
                }
            }
        }
    }

    private void timKhachHangTheoSDT() {
        String sdt = txtSDTKhach.getText().trim();
        if (sdt.isEmpty() || !sdt.matches("\\d{10}")) {
            txtHoTenKhach.setText("");
            return;
        }

        entity.KhachHang kh = khachHangDAO.timTheoSDT(sdt);
        if (kh != null) {
            txtHoTenKhach.setText(kh.getTenKH());
        } else {
            txtHoTenKhach.setText("");
        }
    }

    private void xuLyDatBan() {
        if (dsBanDaChon.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bàn!", "Chưa chọn bàn", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String sdt = txtSDTKhach.getText().trim();
        if (sdt.isEmpty() || !sdt.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(this, "Số điện thoại không hợp lệ!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
            txtSDTKhach.requestFocus();
            return;
        }
        String tenKH = txtHoTenKhach.getText().trim();
        if (tenKH.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên khách hàng!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
            txtHoTenKhach.requestFocus();
            return;
        }

        LocalDateTime thoiGianDat = null;
        try {
            Date selectedDate = (Date) dateSpinner.getValue();
            Date selectedTime = (Date) timeSpinner.getValue();
            Calendar dateCal = Calendar.getInstance();
            dateCal.setTime(selectedDate);
            Calendar timeCal = Calendar.getInstance();
            timeCal.setTime(selectedTime);
            dateCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
            dateCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
            dateCal.set(Calendar.SECOND, 0);
            thoiGianDat = dateCal.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

            if (thoiGianDat.isBefore(LocalDateTime.now())) {
                JOptionPane.showMessageDialog(this, "Thời gian đặt phải trong tương lai!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        entity.KhachHang kh = khachHangDAO.timTheoSDT(sdt);
        String maKHCanDung = null;

        if (kh == null) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Khách hàng mới (" + sdt + "). Thêm vào danh sách thành viên?",
                    "Khách mới", JOptionPane.YES_NO_CANCEL_OPTION);

            if (choice == JOptionPane.CANCEL_OPTION) return;

            kh = new entity.KhachHang();
            kh.setTenKH(tenKH);
            kh.setSdt(sdt);
            kh.setHangThanhVien(choice == JOptionPane.YES_OPTION ? entity.HangThanhVien.MEMBER : entity.HangThanhVien.NONE);
            kh.setGioitinh("Khác");
            kh.setNgaySinh(java.time.LocalDate.of(2000,1,1));
            kh.setNgayThamGia(java.time.LocalDate.now());

            if (khachHangDAO.themKhachHang(kh)) {
                entity.KhachHang khMoi = khachHangDAO.timTheoSDT(sdt);
                if (khMoi != null) {
                    maKHCanDung = khMoi.getMaKH();
                } else {
                    maKHCanDung = kh.getMaKH();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi thêm khách hàng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else {
            maKHCanDung = kh.getMaKH();
        }

        if (maKHCanDung == null) {
            JOptionPane.showMessageDialog(this, "Lỗi: Không lấy được Mã Khách Hàng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean tatCaThanhCong = true;
        boolean isGhepBan = dsBanDaChon.size() > 1;
        Ban banChinh = dsBanDaChon.get(0);

        for (Ban ban : dsBanDaChon) {
            entity.DonDatMon ddm = new entity.DonDatMon();
            ddm.setNgayKhoiTao(LocalDateTime.now());
            ddm.setThoiGianDen(thoiGianDat);
            ddm.setTrangThai("Chưa thanh toán");
            ddm.setMaNV(maNVDangNhap.isEmpty() ? "NV01002" : maNVDangNhap);
            ddm.setMaKH(maKHCanDung);
            ddm.setMaBan(ban.getMaBan());

            String ghiChuUser = txtGhiChu.getText().trim();
            if (isGhepBan) {
                if (ban.equals(banChinh)) {
                    ddm.setGhiChu(ghiChuUser + " (Đặt chính nhóm " + dsBanDaChon.size() + " bàn)");
                } else {
                    String noteHienThi = ghiChuUser + " (Đặt cùng " + banChinh.getTenBan() + ")";
                    String noteKyThuat = " LINKED:" + banChinh.getMaBan();

                    ddm.setGhiChu(noteHienThi + noteKyThuat);
                }
            } else {
                ddm.setGhiChu(ghiChuUser);
            }

            if (donDatMonDAO.themDonDatMon(ddm)) {
                long phutChenhLech = java.time.Duration.between(LocalDateTime.now(), thoiGianDat).toMinutes();
                if (phutChenhLech <= 120) {
                    if (ban.getTrangThai() != TrangThaiBan.DANG_PHUC_VU) {
                        ban.setTrangThai(TrangThaiBan.DA_DAT_TRUOC);
                        ban.setGioMoBan(thoiGianDat);
                        banDAO.updateBan(ban);
                    }
                }
            } else {
                tatCaThanhCong = false;
            }
        }

        if (tatCaThanhCong) {
            taiDanhSachBanTrong();
            hienThiBanPhuHop();
            loadDanhSachDatTruoc();

            spinnerSoLuongKhach.setValue(1);
            txtGhiChu.setText("");
            txtSDTKhach.setText("");
            txtHoTenKhach.setText("");
            dsBanDaChon.clear();

            if (parentDanhSachBanGUI_DatBan != null) {
                parentDanhSachBanGUI_DatBan.refreshManHinhBan();
            }
            JOptionPane.showMessageDialog(this, "Đặt bàn thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi lưu đơn!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadDanhSachDatTruoc() {
        modelListPhieuDat.clear(); // Xóa list cũ
        try {
            List<entity.DonDatMon> dsDatTruoc = donDatMonDAO.getAllDonDatMonChuaNhan();

            if (dsDatTruoc.isEmpty()) {
            } else {
                for (entity.DonDatMon ddm : dsDatTruoc) {
                    modelListPhieuDat.addElement(ddm);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tải danh sách đặt trước: " + e.getMessage());
        }
        listPhieuDat.setModel(modelListPhieuDat);
        listPhieuDat.repaint();
    }
    public void refreshData() {
        donDatMonDAO.tuDongHuyDonQuaGio();
        taiDanhSachBanTrong();
        hienThiBanPhuHop();
        loadDanhSachDatTruoc();
    }
    private void xuLyHuyDatBan(DonDatMon ddmToCancel, int index) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn hủy đặt bàn cho mã đơn '" + ddmToCancel.getMaDon() + "'?",
                "Xác nhận hủy đặt bàn",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean xoaDonOK = donDatMonDAO.xoaDonDatMon(ddmToCancel.getMaDon());

            if (xoaDonOK) {
                Ban banCanUpdate = banDAO.getBanByMa(ddmToCancel.getMaBan());
                if (banCanUpdate != null && banCanUpdate.getTrangThai() == TrangThaiBan.DA_DAT_TRUOC) {
                    banCanUpdate.setTrangThai(TrangThaiBan.TRONG);
                    banCanUpdate.setGioMoBan(null); // Reset giờ đặt
                    String tenHienTai = banCanUpdate.getTenBan();
                    String tenGoc = tenHienTai.replaceAll("\\s*\\(Ghép.*\\)", "").trim();
                    banCanUpdate.setTenBan(tenGoc);
                    boolean updateBanOK = banDAO.updateBan(banCanUpdate);
                    if (!updateBanOK) {
                        JOptionPane.showMessageDialog(this, "Hủy đơn thành công nhưng lỗi cập nhật lại trạng thái bàn!", "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    System.err.println("Không tìm thấy bàn " + ddmToCancel.getMaBan() + " hoặc trạng thái không phải DA_DAT_TRUOC để reset.");
                }

                modelListPhieuDat.removeElementAt(index);
                taiDanhSachBanTrong();
                hienThiBanPhuHop();
                if (parentDanhSachBanGUI_DatBan != null) {
                    parentDanhSachBanGUI_DatBan.refreshManHinhBan();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Hủy đặt bàn thất bại! Vui lòng thử lại.", "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class PhieuDatListRenderer implements ListCellRenderer<DonDatMon> {

        private final JPanel mainPanel;
        private final JPanel textPanel;
        private final JLabel lblLine1;
        private final JLabel lblLine2;
        private final JButton btnDelete;
        private final JSeparator separator;

        private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
        private final Font mainFont = new Font("Segoe UI", Font.BOLD, 14);
        private final Font subFont = new Font("Segoe UI", Font.PLAIN, 13);
        private final Color textColor = Color.DARK_GRAY;
        private final Color timeColor = Color.BLACK;
        private final Color separatorColor = new Color(220, 220, 220);

        public PhieuDatListRenderer() {
            mainPanel = new JPanel(new BorderLayout(10, 0));
            mainPanel.setBorder(new EmptyBorder(8, 10, 8, 10));

            textPanel = new JPanel();
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            textPanel.setOpaque(false);
            lblLine1 = new JLabel(" ");
            lblLine1.setFont(mainFont);
            lblLine1.setForeground(textColor);

            lblLine2 = new JLabel(" ");
            lblLine2.setFont(subFont);
            lblLine2.setForeground(timeColor);

            textPanel.add(lblLine1);
            textPanel.add(Box.createRigidArea(new Dimension(0, 3)));
            textPanel.add(lblLine2);

            btnDelete = new JButton("X");
            btnDelete.setFont(new Font("Arial", Font.BOLD, 16));
            btnDelete.setForeground(Color.WHITE);
            btnDelete.setBackground(new Color(239, 68, 68));
            btnDelete.setFocusPainted(false);
            btnDelete.setBorder(new EmptyBorder(5, 10, 5, 10));
            btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnDelete.setPreferredSize(new Dimension(40, 40));

            separator = new JSeparator(SwingConstants.HORIZONTAL);
            separator.setForeground(separatorColor);

            mainPanel.add(textPanel, BorderLayout.CENTER);
            mainPanel.add(btnDelete, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends DonDatMon> list, DonDatMon value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {

            if (value instanceof DonDatMon) {
                DonDatMon ddm = value;
                String tenBan = banDAO.getTenHienThiGhep(ddm.getMaBan());
                if (tenBan == null || tenBan.isEmpty()) tenBan = banDAO.getTenBanByMa(ddm.getMaBan());
                KhachHang kh = (ddm.getMaKH() != null) ? khachHangDAO.timTheoMaKH(ddm.getMaKH()) : null;
                String tenKH = (kh != null) ? kh.getTenKH() : "Vãng lai";
                String sdtKH = (kh != null) ? kh.getSdt() : "--";
                String gioDen = "N/A";
                if (ddm.getThoiGianDen() != null) {
                    gioDen = ddm.getThoiGianDen().format(timeFormatter);
                } else if (ddm.getNgayKhoiTao() != null) {
                    gioDen = ddm.getNgayKhoiTao().format(timeFormatter);
                }
                int soNguoi = 0;
                Ban banDat = banDAO.getBanByMa(ddm.getMaBan());
                if (banDat != null) soNguoi = banDat.getSoGhe();

                lblLine1.setText(String.format("%s (%s)", tenBan, sdtKH));
                lblLine2.setText(String.format("%s - %s ", gioDen, tenKH));
                btnDelete.setVisible(true);
            } else {
                String message;
                String currentSearchText = txtTimKiemPhieuDat.getText().trim();
                final String placeholder = " Tìm kiếm bàn đặt SĐT/Tên khách...";

                if (!currentSearchText.isEmpty() && !currentSearchText.equals(placeholder)) {
                    message = "Không tìm thấy kết quả phù hợp.";
                } else {
                    message = "Chưa có bàn nào được đặt trước.";
                }

                lblLine1.setText(message);
                lblLine1.setFont(subFont);
                lblLine1.setForeground(Color.GRAY);
                lblLine2.setText(" ");
                btnDelete.setVisible(false);
            }

            if (isSelected) {
                mainPanel.setBackground(list.getSelectionBackground());
                mainPanel.setForeground(list.getSelectionForeground());
                textPanel.setOpaque(true);
                textPanel.setBackground(list.getSelectionBackground());
                lblLine1.setForeground(Color.WHITE);
                lblLine2.setForeground(Color.WHITE);
            } else {
                mainPanel.setBackground(list.getBackground());
                mainPanel.setForeground(list.getForeground());
                textPanel.setOpaque(false);
                lblLine1.setForeground(textColor);
                lblLine2.setForeground(timeColor);
            }
            btnDelete.setBackground(mainPanel.getBackground());
            if (isSelected) btnDelete.setForeground(Color.DARK_GRAY); else btnDelete.setForeground(Color.RED);
            JPanel containerPanel = new JPanel(new BorderLayout());
            containerPanel.setBackground(list.getBackground());
            containerPanel.add(mainPanel, BorderLayout.CENTER);
            containerPanel.add(separator, BorderLayout.SOUTH);
            return containerPanel;
        }
    }
    private void addBanPanelToView(Ban ban) {
        BanPanel banPanel = new BanPanel(ban);
        banPanel.setBackground(BanPanel.COLOR_STATUS_FREE);

        dsBanPanelHienThi.add(banPanel);

        pnlBanContainer.add(banPanel);

        banPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    dsBanDaChon.clear();
                    dsBanDaChon.add(ban);

                    Component[] comps = pnlBanContainer.getComponents();
                    for (Component c : comps) {
                        if (c instanceof JToggleButton) {
                            ((JToggleButton)c).setSelected(false);
                            c.setBackground(Color.WHITE);
                            if (c instanceof JToggleButton) {
                                updateLabelsColor((JToggleButton)c, Color.BLACK);
                            }
                        }
                    }

                    updateBanPanelSelection();
                }
            }
        });
    }
}