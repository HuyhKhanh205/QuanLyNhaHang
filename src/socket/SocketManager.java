package socket;

import config.AppConfig;

import java.net.ServerSocket;
import java.util.Map;

/**
 * Khởi động Socket layer khi app bắt đầu.
 * - Thử bind port → thành công: chạy SocketServer + kết nối SocketClient đến chính mình
 * - Port đã bị bind → chỉ kết nối SocketClient đến server đang chạy
 */
public class SocketManager {

    private static SocketServer server;
    private static SocketClient client;

    public static void initialize() {
        int port = AppConfig.getSocketPort();
        String host = AppConfig.getSocketHost();

        if (tryBindPort(port)) {
            // Máy này làm server
            server = new SocketServer(port);
            server.start();
        }

        // Tất cả máy đều kết nối như client (kể cả server)
        client = SocketClient.init(host, port);
        client.connect();
    }

    private static boolean tryBindPort(int port) {
        try (ServerSocket test = new ServerSocket(port)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Gửi event đến tất cả máy trạm (qua server).
     * Được gọi từ DAO sau khi thực hiện thay đổi DB.
     */
    public static void sendEvent(SocketEvent event, Map<String, String> data) {
        if (client != null) client.sendEvent(event, data);
    }

    public static SocketClient getClient()   { return client; }
    public static boolean isServer()         { return server != null; }

    public static void shutdown() {
        if (server != null) server.stop();
    }
}
