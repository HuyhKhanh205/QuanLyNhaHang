package gui;

import dao.*;
import entity.*;
import socket.SocketClient;
import socket.SocketEvent;
import socket.SocketManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.table.TableColumn;

public class ManHinhGoiMonGUI extends JPanel {
    private Ban banHienTai;
    private HoaDon activeHoaDon;
    private HoaDonDAO hoaDonDAO_GoiMon;
    private final NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private final String maNVDangNhap;

    private DanhSachBanGUI parentDanhSachBanGUI_GoiMon;
    private MonAnDAO monAnDAO;
    private List<MonAn> dsMonAnFull;
    private List<MonAnItemPanel> dsMonAnPanel;
    private JPanel pnlMenuItemContainer;
    private JTextField txtTimKiem;
    private String currentCategoryFilter = "Tất cả";
    private JLabel statusColorBox;
    private DonDatMonDAO donDatMonDAO;
    private BanDAO banDAO;
    private ChiTietHoaDonDAO chiTietDAO;
    private KhachHangDAO khachHangDAO;
    private KhuyenMaiDAO maKhuyenMaiDAO;
    private DanhMucMonDAO danhMucMonDAO;
    private JPanel pnlCategoryFilter;

    private JLabel lblTenBanHeader;
    private JTable tblChiTietHoaDon;
    private DefaultTableModel modelChiTietHoaDon;
    private BillPanel billPanel;

    public ManHinhGoiMonGUI(DanhSachBanGUI parent) {
        this(parent, "NV_UNKNOWN");
    }

    public ManHinhGoiMonGUI(DanhSachBanGUI parent, String maNVDangNhap) {
        super(new BorderLayout());
        this.parentDanhSachBanGUI_GoiMon = parent;
        this.maNVDangNhap = maNVDangNhap;
        this.monAnDAO = new MonAnDAO();
        this.dsMonAnFull = new ArrayList<>();
        this.hoaDonDAO_GoiMon = new HoaDonDAO();
        this.donDatMonDAO = new DonDatMonDAO();
        this.banDAO = new BanDAO();
        this.dsMonAnPanel = new ArrayList<>();
        this.chiTietDAO = new ChiTietHoaDonDAO();
        this.khachHangDAO = new KhachHangDAO();
        this.maKhuyenMaiDAO = new KhuyenMaiDAO();
        this.danhMucMonDAO = new DanhMucMonDAO();

        buildUI();
        loadDataFromDB();
        xoaThongTinGoiMon();
        dangKySocketEvents();
    }
    private String phatSinhMaHD() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyy");
        String datePart = LocalDateTime.now().format(formatter);
        java.util.concurrent.ThreadLocalRandom current = java.util.concurrent.ThreadLocalRandom.current();
        int randomPart = current.nextInt(1000, 10000);
        return "HD" + datePart + randomPart;
    }
    private void moBanMoi(Ban ban) throws Exception {
        ban.setTrangThai(TrangThaiBan.DANG_PHUC_VU);
        ban.setGioMoBan(LocalDateTime.now());
        banDAO.updateBan(ban);

        DonDatMon ddm = new DonDatMon();
        ddm.setNgayKhoiTao(LocalDateTime.now());
        ddm.setMaNV(maNVDangNhap);
        ddm.setMaBan(ban.getMaBan());
        donDatMonDAO.themDonDatMon(ddm);

        HoaDon hd = new HoaDon(phatSinhMaHD(), LocalDateTime.now(), "Chưa thanh toán", "Tiền mặt", ddm.getMaDon(), maNVDangNhap, null);
        hd.setTongTienTuDB(0);
        hoaDonDAO_GoiMon.themHoaDon(hd);

        this.activeHoaDon = hd;
        statusColorBox.setBackground(ManHinhBanGUI.COLOR_STATUS_OCCUPIED);
    }
    public DanhSachBanGUI getParentDanhSachBanGUI() {
        return parentDanhSachBanGUI_GoiMon;
    }
    public boolean loadDuLieuBan(Ban banDuocChon) {
        System.out.println("loadDuLieuBan được gọi cho: " + banDuocChon.getTenBan() + " - Trạng thái: " + banDuocChon.getTrangThai());
        String maBanDich = donDatMonDAO.getMaBanDichCuaBanGhep(banDuocChon.getMaBan());
        Ban banThucSu = banDuocChon;
        if (maBanDich != null) {
            Ban banDich = banDAO.getBanByMa(maBanDich);
            if (banDich != null) {
                banThucSu = banDich;
            }
        }
        this.banHienTai = banThucSu;
        String tenHienThi = banDAO.getTenHienThiGhep(banThucSu.getMaBan());
        if (tenHienThi == null || tenHienThi.isEmpty()) {
            tenHienThi = banThucSu.getTenBan();
        }
        lblTenBanHeader.setText(tenHienThi + " -- " + banThucSu.getKhuVuc());
        if (billPanel != null) {
            billPanel.setCustomHeader(lblTenBanHeader.getText());
        }

        Color statusColor;
        switch (banThucSu.getTrangThai()) {
            case TRONG: statusColor = ManHinhBanGUI.COLOR_STATUS_FREE; break;
            case DA_DAT_TRUOC: statusColor = ManHinhBanGUI.COLOR_STATUS_RESERVED; break;
            case DANG_PHUC_VU: default: statusColor = ManHinhBanGUI.COLOR_STATUS_OCCUPIED; break;
        }
        statusColorBox.setBackground(statusColor);

        modelChiTietHoaDon.setRowCount(0);

        this.activeHoaDon = null;
        boolean requireBanRefresh = false;

        try {
            if (banThucSu.getTrangThai() == TrangThaiBan.DANG_PHUC_VU) {
                activeHoaDon = hoaDonDAO_GoiMon.getHoaDonChuaThanhToan(banThucSu.getMaBan());

                if (activeHoaDon == null) {
                    String[] options = {"Tạo hóa đơn mới", "Đặt lại bàn về Trống", "Hủy"};
                    int choice = JOptionPane.showOptionDialog(this,
                            "Bàn '" + banThucSu.getTenBan() + "' đang phục vụ nhưng không tìm thấy hóa đơn.\n" +
                            "Bạn muốn làm gì?",
                            "Dữ liệu không nhất quán", JOptionPane.DEFAULT_OPTION,
                            JOptionPane.WARNING_MESSAGE, null, options, options[0]);
                    if (choice == 0) {
                        moBanMoi(banThucSu);
                        requireBanRefresh = true;
                    } else if (choice == 1) {
                        banThucSu.setTrangThai(TrangThaiBan.TRONG);
                        banThucSu.setGioMoBan(null);
                        banDAO.updateBan(banThucSu);
                        if (parentDanhSachBanGUI_GoiMon != null) parentDanhSachBanGUI_GoiMon.refreshManHinhBan();
                        return false;
                    } else {
                        return false;
                    }
                }

                this.activeHoaDon = activeHoaDon;
                System.out.println("Đang tải hóa đơn: " + activeHoaDon.getMaHD());
            }

            else if (banThucSu.getTrangThai() == TrangThaiBan.TRONG) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Bạn có muốn mở bàn '" + banThucSu.getTenBan() + "' cho khách không?",
                        "Mở bàn mới", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    moBanMoi(banThucSu);
                    requireBanRefresh = true;
                } else {
                    return false;
                }
            }

            else if (banThucSu.getTrangThai() == TrangThaiBan.DA_DAT_TRUOC) {
                DonDatMon ddmPreview = donDatMonDAO.getDonDatMonDatTruoc(banThucSu.getMaBan());
                String msg = "Bàn '" + banThucSu.getTenBan() + "' đã được đặt trước.\nBạn có muốn nhận bàn này không?";

                if (ddmPreview != null) {
                    String tenKhach = "Khách";
                    if (ddmPreview.getMaKH() != null) {
                        entity.KhachHang kh = khachHangDAO.timTheoMaKH(ddmPreview.getMaKH());
                        if (kh != null) tenKhach = kh.getTenKH();
                    }
                    msg = "Bàn '" + banThucSu.getTenBan() + "' đặt bởi " + tenKhach + ".\nNhận bàn ngay?";
                }

                int confirm = JOptionPane.showConfirmDialog(this, msg, "Nhận bàn đặt", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    nhanBanDatTruoc(banThucSu);

                    requireBanRefresh = true;
                } else {
                    return false;
                }
            }

            if (this.activeHoaDon != null) {
                List<ChiTietHoaDon> dsChiTiet = chiTietDAO.getChiTietTheoMaDon(this.activeHoaDon.getMaDon());
                if (dsChiTiet != null) {
                    this.activeHoaDon.setDsChiTiet(dsChiTiet);
                    for (ChiTietHoaDon ct : dsChiTiet) {
                        Object[] rowData = { "X", ct.getMaMon(), ct.getTenMon(), ct.getSoluong(), ct.getDongia(), ct.getThanhtien() };
                        modelChiTietHoaDon.addRow(rowData);
                    }
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
            return false;
        } finally {
            updateBillPanelTotals();
        }

        if (requireBanRefresh && parentDanhSachBanGUI_GoiMon != null) {
            parentDanhSachBanGUI_GoiMon.refreshManHinhBan();
        }
        return true;
    }

    public void xoaThongTinGoiMon() {
        lblTenBanHeader.setText("Chưa chọn bàn");
        modelChiTietHoaDon.setRowCount(0);
        billPanel.clearBill();
        this.banHienTai = null;
        this.activeHoaDon = null;
        if (statusColorBox != null) {
            statusColorBox.setBackground(ManHinhBanGUI.COLOR_STATUS_FREE);
        }
    }

    private void addMonAnToOrder(MonAn monAn) {
        if (banHienTai == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bàn trước khi gọi món!", "Chưa chọn bàn", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maMon = monAn.getMaMonAn();
        String tenMon = monAn.getTenMon();
        float donGia = monAn.getDonGia();

        for (int i = 0; i < modelChiTietHoaDon.getRowCount(); i++) {
            String maMonTrongBang = (String) modelChiTietHoaDon.getValueAt(i, 1);
            if (maMon.equals(maMonTrongBang)) {
                int soLuongHienTai = (int) modelChiTietHoaDon.getValueAt(i, 3);
                int soLuongMoi = soLuongHienTai + 1;
                modelChiTietHoaDon.setValueAt(soLuongMoi, i, 3);
                float thanhTienMoi = soLuongMoi * donGia;
                modelChiTietHoaDon.setValueAt(thanhTienMoi, i, 5);
                updateBillPanelTotals();
                return;
            }
        }

        Object[] rowData = {
                "X",
                maMon,
                tenMon,
                Integer.valueOf(1),
                donGia,
                donGia
        };
        modelChiTietHoaDon.addRow(rowData);
        updateBillPanelTotals();
    }
    private List<ChiTietHoaDon> layChiTietTuTable() {
        List<ChiTietHoaDon> dsChiTiet = new ArrayList<>();
        HoaDon currentHD = getActiveHoaDon();

        String maDon = null;
        if (currentHD != null) {
            maDon = currentHD.getMaDon();
        }

        if (maDon == null) {
            System.err.println("layChiTietTuTable: Không tìm thấy maDon, không thể tạo List ChiTietHoaDon.");
            return dsChiTiet;
        }

        for (int i = 0; i < modelChiTietHoaDon.getRowCount(); i++) {
            try {
                String maMon = (String) modelChiTietHoaDon.getValueAt(i, 1);
                String tenMon = (String) modelChiTietHoaDon.getValueAt(i, 2);
                Integer soLuong = (Integer) modelChiTietHoaDon.getValueAt(i, 3);
                Float donGia = (Float) modelChiTietHoaDon.getValueAt(i, 4);

                if (maMon != null && tenMon != null && soLuong != null && donGia != null) {
                    ChiTietHoaDon ct = new ChiTietHoaDon(maDon, maMon, tenMon, soLuong.intValue(), donGia.floatValue());
                    dsChiTiet.add(ct);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Lỗi khi đọc dữ liệu từ JTable hàng " + i + ": " + e.getMessage());
            }
        }
        return dsChiTiet;
    }
    public void updateBillPanelTotals() {
        HoaDon currentHD = getActiveHoaDon();
        if (currentHD != null) {
            currentHD.setDsChiTiet(layChiTietTuTable());
            currentHD.tinhLaiGiamGiaVaTongTien(khachHangDAO, maKhuyenMaiDAO);
            int tongSoLuong = 0;
            if(currentHD.getDsChiTiet() != null){
                for(ChiTietHoaDon ct : currentHD.getDsChiTiet()) {
                    tongSoLuong += ct.getSoluong();
                }
            }

            billPanel.loadBillTotals(
                    (long) currentHD.getTongTien(),
                    (long) currentHD.getGiamGia(),
                    (long) currentHD.getTongThanhToan(),
                    tongSoLuong
            );
        } else {
            billPanel.clearBill();
        }
    }

    private void buildUI() {
        this.setBackground(Color.WHITE);
        this.setBorder(new EmptyBorder(10, 0, 10, 10));

        JPanel pnlLeft = createMenuPanel();

        JPanel pnlRight = createOrderPanel();

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                pnlLeft,
                pnlRight
        );
        splitPane.setDividerLocation(520);
        splitPane.setBorder(null);

        this.add(splitPane, BorderLayout.CENTER);
    }
    private void dangKySocketEvents() {
        SocketClient client = SocketManager.getClient();
        if (client == null) return;
        client.subscribe(SocketEvent.MENU_UPDATED, msg ->
                SwingUtilities.invokeLater(this::loadDataFromDB));
        client.subscribe(SocketEvent.ORDER_CREATED, msg ->
                SwingUtilities.invokeLater(this::reloadActiveOrder));
        client.subscribe(SocketEvent.ORDER_UPDATED, msg ->
                SwingUtilities.invokeLater(this::reloadActiveOrder));
    }

    private void reloadActiveOrder() {
        if (banHienTai == null || activeHoaDon == null) return;
        List<ChiTietHoaDon> dsChiTiet = chiTietDAO.getChiTietTheoMaDon(activeHoaDon.getMaDon());
        activeHoaDon.setDsChiTiet(dsChiTiet);
        modelChiTietHoaDon.setRowCount(0);
        if (dsChiTiet != null) {
            for (ChiTietHoaDon ct : dsChiTiet) {
                modelChiTietHoaDon.addRow(new Object[]{
                        "X", ct.getMaMon(), ct.getTenMon(),
                        ct.getSoluong(), ct.getDongia(), ct.getThanhtien()
                });
            }
        }
        updateBillPanelTotals();
    }

    private void loadDataFromDB() {
        reloadCategoryFilter();
        this.dsMonAnFull = monAnDAO.getMonAnDangKinhDoanh();

        pnlMenuItemContainer.removeAll();
        dsMonAnPanel.clear();

        if (dsMonAnFull.isEmpty()) {
            pnlMenuItemContainer.add(new JLabel("Không có món ăn nào trong CSDL."));
        } else {
            for (MonAn mon : dsMonAnFull) {
                MonAnItemPanel itemPanel = new MonAnItemPanel(mon);

                itemPanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            addMonAnToOrder(itemPanel.getMonAn());
                        }
                    }
                });

                dsMonAnPanel.add(itemPanel);
                pnlMenuItemContainer.add(itemPanel);
            }
        }

        filterMonAn();
        pnlMenuItemContainer.revalidate();
        pnlMenuItemContainer.repaint();
    }
    private void filterMonAn() {
        String tuKhoa = txtTimKiem.getText().trim().toLowerCase();
        for (MonAnItemPanel itemPanel : dsMonAnPanel) {
            MonAn mon = itemPanel.getMonAn();
            boolean show = true;

            if (!currentCategoryFilter.equals("Tất cả")) {
                if (mon.getMaDM() == null || !mon.getMaDM().equals(currentCategoryFilter)) {
                    show = false;
                }
            }

            if (show && !tuKhoa.isEmpty()) {
                if (!mon.getTenMon().toLowerCase().contains(tuKhoa)) {
                    show = false;
                }
            }

            itemPanel.setVisible(show);
        }
        pnlMenuItemContainer.revalidate();
        pnlMenuItemContainer.repaint();
    }

    private JPanel createMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);

        JPanel pnlFilter = new JPanel(new BorderLayout(0, 5));
        pnlFilter.setOpaque(false);
        pnlFilter.add(createCategoryFilterPanel(), BorderLayout.NORTH);
        pnlFilter.add(createSearchPanel(), BorderLayout.SOUTH);
        panel.add(pnlFilter, BorderLayout.NORTH);

        pnlMenuItemContainer = new VerticallyWrappingFlowPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        pnlMenuItemContainer.setBackground(Color.WHITE);
        pnlMenuItemContainer.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(pnlMenuItemContainer);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(new EmptyBorder(10, 5, 10, 10));
        panel.setBackground(Color.WHITE);

        panel.add(createOrderHeaderPanel(), BorderLayout.NORTH);

        this.billPanel = new BillPanel(this);
        panel.add(billPanel, BorderLayout.SOUTH);

        String[] cols = {"X", "Mã Món", "Tên món", "SL", "Đơn giá", "Thành tiền"};
        modelChiTietHoaDon = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0|| column == 3;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0: return String.class;
                    case 1: return String.class;
                    case 2: return String.class;
                    case 3: return Integer.class;
                    case 4: return Float.class;
                    case 5: return Float.class;
                    default: return Object.class;
                }
            }
        };
        tblChiTietHoaDon = new JTable(modelChiTietHoaDon);
        TableColumn columnX = tblChiTietHoaDon.getColumnModel().getColumn(0);
        columnX.setCellRenderer(new ButtonRenderer());
        columnX.setCellEditor(new ButtonEditor(new JCheckBox()));

        TableColumn columnSL = tblChiTietHoaDon.getColumnModel().getColumn(3);
        columnSL.setCellRenderer(new SpinnerRenderer());
        columnSL.setCellEditor(new SpinnerEditor());

        tblChiTietHoaDon.setRowHeight(30);

        TableColumn colMaMon = tblChiTietHoaDon.getColumnModel().getColumn(1);
        colMaMon.setMinWidth(0);
        colMaMon.setMaxWidth(0);
        colMaMon.setPreferredWidth(0);

        tblChiTietHoaDon.getColumnModel().getColumn(0).setPreferredWidth(30);
        tblChiTietHoaDon.getColumnModel().getColumn(2).setPreferredWidth(150);
        tblChiTietHoaDon.getColumnModel().getColumn(3).setPreferredWidth(50);
        tblChiTietHoaDon.getColumnModel().getColumn(4).setPreferredWidth(80);
        tblChiTietHoaDon.getColumnModel().getColumn(5).setPreferredWidth(90);

        DefaultTableCellRenderer currencyRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                if (value instanceof Number) {
                    value = nf.format(((Number) value).doubleValue());
                }
                setHorizontalAlignment(JLabel.RIGHT);
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        };
        tblChiTietHoaDon.getColumnModel().getColumn(4).setCellRenderer(currencyRenderer);
        tblChiTietHoaDon.getColumnModel().getColumn(5).setCellRenderer(currencyRenderer);
        JScrollPane scrollPane = new JScrollPane(tblChiTietHoaDon);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    public DefaultTableModel getModelChiTietHoaDon() {
        return modelChiTietHoaDon;
    }

    public Ban getBanHienTai() {
        return banHienTai;
    }
    public HoaDon getActiveHoaDon() {
        return activeHoaDon;
    }

    public String getMaNVDangNhap() {
        return maNVDangNhap;
    }

    private JPanel createCategoryFilterPanel() {
        pnlCategoryFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pnlCategoryFilter.setOpaque(false);
        // Nội dung sẽ được điền bởi reloadCategoryFilter() trong loadDataFromDB()
        return pnlCategoryFilter;
    }

    private void reloadCategoryFilter() {
        if (pnlCategoryFilter == null) return;
        pnlCategoryFilter.removeAll();
        ButtonGroup group = new ButtonGroup();

        ActionListener filterListener = e -> {
            currentCategoryFilter = e.getActionCommand();
            filterMonAn();
        };

        JToggleButton btnAll = createFilterButton("Tất cả", true);
        btnAll.setActionCommand("Tất cả");
        btnAll.addActionListener(filterListener);
        group.add(btnAll);
        pnlCategoryFilter.add(btnAll);

        for (DanhMucMon dm : danhMucMonDAO.getAllDanhMuc()) {
            JToggleButton btn = createFilterButton(dm.getTendm(), false);
            btn.setActionCommand(dm.getMadm());
            btn.addActionListener(filterListener);
            group.add(btn);
            pnlCategoryFilter.add(btn);
        }

        currentCategoryFilter = "Tất cả";
        pnlCategoryFilter.revalidate();
        pnlCategoryFilter.repaint();
    }
    private JToggleButton createFilterButton(String text, boolean selected) {
        JToggleButton button = new JToggleButton(text);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setBorder(new EmptyBorder(5, 15, 5, 15));
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        if (selected) {
            button.setBackground(BanPanel.COLOR_ACCENT_BLUE);
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(Color.WHITE);
            button.setForeground(Color.BLACK);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                    new EmptyBorder(4, 14, 4, 14)
            ));
        }
        button.addChangeListener(e -> {
            if (button.isSelected()) {
                button.setBackground(BanPanel.COLOR_ACCENT_BLUE);
                button.setForeground(Color.WHITE);
                button.setBorder(new EmptyBorder(5, 15, 5, 15));
            } else {
                button.setBackground(Color.WHITE);
                button.setForeground(Color.BLACK);
                button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                        new EmptyBorder(4, 14, 4, 14)
                ));
            }
        });
        button.setSelected(selected);
        return button;
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(5, 0, 0, 0));

        JLabel searchIcon = new JLabel("🔎");
        searchIcon.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 16));
        panel.add(searchIcon, BorderLayout.WEST);

        txtTimKiem = new JTextField();
        txtTimKiem.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtTimKiem.setPreferredSize(new Dimension(0, 35));

        txtTimKiem.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                filterMonAn();
            }
        });

        panel.add(txtTimKiem, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createOrderHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 10, 0));

        statusColorBox = new JLabel();
        statusColorBox.setPreferredSize(new Dimension(48, 48));
        statusColorBox.setBackground(ManHinhBanGUI.COLOR_STATUS_FREE);
        statusColorBox.setOpaque(true);

        lblTenBanHeader = new JLabel("Chưa chọn bàn");
        lblTenBanHeader.setFont(new Font("Segoe UI", Font.BOLD, 24));

        panel.add(statusColorBox, BorderLayout.WEST);
        panel.add(lblTenBanHeader, BorderLayout.CENTER);

        return panel;
    }
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setForeground(Color.RED);
            setBackground(Color.WHITE);
            setBorder(null);
            setText("X");
            setFont(new Font("Arial", Font.BOLD, 14));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;
        private int editingRow;
        private JTable table;


        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.setForeground(Color.RED);
            button.setBackground(Color.WHITE);
            button.setBorder(null);
            button.setFont(new Font("Arial", Font.BOLD, 14));
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.table = table;
            this.editingRow = row;
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed && table != null) {
                final DefaultTableModel finalModel = (DefaultTableModel) table.getModel();
                final int rowToRemove = editingRow;

                SwingUtilities.invokeLater(() -> {

                    if (rowToRemove >= 0 && rowToRemove < finalModel.getRowCount()) {
                        finalModel.removeRow(rowToRemove);
                        updateBillPanelTotals();
                    } else {
                        System.err.println("ButtonEditor (invokeLater): Lỗi index dòng khi xóa: " + rowToRemove);
                    }
                });
            }
            isPushed = false;
            editingRow = -1;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            boolean stopped = super.stopCellEditing();
            editingRow = -1;
            return stopped;
        }

        @Override
        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }

    class SpinnerRenderer extends JSpinner implements TableCellRenderer {
        public SpinnerRenderer() {
            super(new SpinnerNumberModel(1, 1, 100, 1));
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setBorder(null);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof Integer) {
                setValue(value);
            } else {
                setValue(1);
            }
            return this;
        }
    }

    class SpinnerEditor extends DefaultCellEditor {
        JSpinner spinner;
        JSpinner.DefaultEditor editor;
        JTextField textField;
        boolean valueSet;
        private int editingRow = -1;
        private JTable table;


        public SpinnerEditor() {
            super(new JTextField());

            spinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
            editor = ((JSpinner.DefaultEditor) spinner.getEditor());
            textField = editor.getTextField();
            textField.setHorizontalAlignment(JTextField.CENTER);
            textField.setBorder(null);
            spinner.setBorder(null);

            spinner.addChangeListener(e -> {
                if (table != null && editingRow != -1) {
                    DefaultTableModel model = (DefaultTableModel) table.getModel();

                    if (editingRow < model.getRowCount()) {
                        int currentQuantity = (Integer) spinner.getValue();
                        float donGia = (Float) model.getValueAt(editingRow, 4);
                        float thanhTienMoi = currentQuantity * donGia;

                        SwingUtilities.invokeLater(() -> {

                            if (editingRow < model.getRowCount()) {
                                model.setValueAt(thanhTienMoi, editingRow, 5);
                                updateBillPanelTotals();
                            }
                        });
                    }
                    fireEditingStopped();
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.table = table;
            this.editingRow = row;

            spinner.setValue(value);
            return spinner;
        }

        @Override
        public Object getCellEditorValue() {
            return spinner.getValue();
        }

        @Override
        public boolean stopCellEditing() {
            try {

                editor.commitEdit();
                spinner.commitEdit();
            } catch (java.text.ParseException e) {

                Object oldValue = spinner.getValue();
                spinner.setValue(oldValue);

            }
            editingRow = -1;
            return super.stopCellEditing();
        }
    }
    private void nhanBanDatTruoc(Ban banChinh) throws Exception {
        System.out.println("--- BẮT ĐẦU NHẬN BÀN " + banChinh.getTenBan() + " ---");

        DonDatMon ddm = donDatMonDAO.getDonDatMonDatTruoc(banChinh.getMaBan());
        if (ddm == null) throw new Exception("Không tìm thấy đơn đặt trước!");

        banChinh.setTrangThai(TrangThaiBan.DANG_PHUC_VU);
        banChinh.setGioMoBan(LocalDateTime.now());
        if (!banDAO.updateBan(banChinh)) throw new Exception("Lỗi update bàn chính!");

        String newMaHD = phatSinhMaHD();
        HoaDon hd = new HoaDon(newMaHD, LocalDateTime.now(), "Chưa thanh toán", "Tiền mặt", ddm.getMaDon(), ddm.getMaNV(), null);
        hd.setMaKH(ddm.getMaKH());
        hd.setTongTienTuDB(0);
        if (!hoaDonDAO_GoiMon.themHoaDon(hd)) throw new Exception("Lỗi tạo hóa đơn!");

        this.activeHoaDon = hd;
        statusColorBox.setBackground(ManHinhBanGUI.COLOR_STATUS_OCCUPIED);

        if (ddm.getMaKH() != null) {
            LocalDateTime timeCheck = ddm.getThoiGianDen() != null ? ddm.getThoiGianDen() : ddm.getNgayKhoiTao();

            List<String> dsMaBanLienQuan = donDatMonDAO.getMaBanCungDotDat(
                    ddm.getMaKH(),
                    timeCheck,
                    banChinh.getMaBan()
            );

            if (!dsMaBanLienQuan.isEmpty()) {
                List<Ban> dsBanPhu = new ArrayList<>();
                List<String> tenBanPhu = new ArrayList<>();

                for (String maBan : dsMaBanLienQuan) {
                    Ban b = banDAO.getBanByMa(maBan);

                    if (b != null) {
                        System.out.println("Check bàn " + b.getTenBan() + " -> Trạng thái: " + b.getTrangThai());

                        if (b.getTrangThai() == TrangThaiBan.DA_DAT_TRUOC) {
                            dsBanPhu.add(b);
                            tenBanPhu.add(b.getTenBan());
                        }
                    }
                }

                if (!dsBanPhu.isEmpty()) {
                    int confirm = JOptionPane.showConfirmDialog(this,
                            "Khách này có đặt thêm các bàn: " + String.join(", ", tenBanPhu) + ".\n" +
                                    "Bạn có muốn MỞ TẤT CẢ và GỘP CHUNG vào bàn này không?",
                            "Đồng bộ bàn đặt", JOptionPane.YES_NO_OPTION);

                    if (confirm == JOptionPane.YES_OPTION) {
                        boolean ketQua = banDAO.ghepBanLienKet(dsBanPhu, banChinh, maNVDangNhap);

                        if (ketQua) {
                            if (parentDanhSachBanGUI_GoiMon != null) parentDanhSachBanGUI_GoiMon.refreshManHinhBan();
                        } else {
                            JOptionPane.showMessageDialog(this, "Lỗi khi gộp bàn.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        }
    }
}