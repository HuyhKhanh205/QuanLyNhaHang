package socket;

public enum SocketEvent {
    TABLE_STATUS_CHANGED,   // maBan, trangThai
    ORDER_CREATED,          // maDon, maBan
    ORDER_UPDATED,          // maDon
    HOA_DON_THANH_TOAN,     // maHD, maBan
    MENU_UPDATED,           // (no data) — món ăn được thêm/sửa/xóa
    KITCHEN_CONFIRM,        // maDon, maMon — bếp xác nhận món đã lên
    PING
}
