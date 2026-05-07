package socket;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * TCP client kết nối đến SocketServer.
 * GUI đăng ký listener theo từng event type.
 */
public class SocketClient {

    private static SocketClient instance;

    private final String host;
    private final int port;
    private PrintWriter out;
    private volatile boolean connected = false;

    private final Map<SocketEvent, List<Consumer<SocketMessage>>> listeners =
            new EnumMap<>(SocketEvent.class);

    private SocketClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static synchronized SocketClient getInstance() {
        return instance;
    }

    static synchronized SocketClient init(String host, int port) {
        if (instance == null) instance = new SocketClient(host, port);
        return instance;
    }

    public void connect() {
        Thread t = new Thread(() -> {
            int delay = 2000;
            while (!Thread.currentThread().isInterrupted()) {
                try (Socket socket = new Socket(host, port)) {
                    connected = true;
                    delay = 2000;
                    out = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String line;
                    while ((line = in.readLine()) != null) {
                        SocketMessage msg = SocketMessage.fromLine(line);
                        if (msg != null) notifyListeners(msg);
                    }
                } catch (Exception e) {
                    connected = false;
                }
                try {
                    Thread.sleep(delay);
                    delay = Math.min(delay * 2, 30_000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "SocketClient-Read");
        t.setDaemon(true);
        t.start();
    }

    public void sendEvent(SocketEvent event, Map<String, String> data) {
        if (out != null && connected) {
            try { out.println(new SocketMessage(event, data).toLine()); }
            catch (Exception e) { connected = false; }
        }
    }

    public void subscribe(SocketEvent event, Consumer<SocketMessage> listener) {
        listeners.computeIfAbsent(event, k -> new ArrayList<>()).add(listener);
    }

    private void notifyListeners(SocketMessage msg) {
        List<Consumer<SocketMessage>> list = listeners.get(msg.getEvent());
        if (list != null) {
            for (Consumer<SocketMessage> c : list) {
                try { c.accept(msg); } catch (Exception ignored) {}
            }
        }
    }

    public boolean isConnected() { return connected; }
}
