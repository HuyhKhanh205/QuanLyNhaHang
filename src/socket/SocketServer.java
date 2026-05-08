package socket;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TCP server lắng nghe kết nối từ các máy trạm.
 * Mỗi message nhận được sẽ được broadcast đến tất cả clients (kể cả sender).
 */
public class SocketServer {

    private final int port;
    private final Set<PrintWriter> clients = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private volatile boolean running = false;

    public SocketServer(int port) { this.port = port; }

    public void start() {
        running = true;
        Thread serverThread = new Thread(() -> {
            try (ServerSocket ss = new ServerSocket(port)) {
                while (running) {
                    Socket client = ss.accept();
                    Thread clientThread = new Thread(() -> handleClient(client));
                    clientThread.setDaemon(true);
                    clientThread.start();
                }
            } catch (Exception e) {
                if (running) e.printStackTrace();
            }
        }, "SocketServer-Accept");
        serverThread.setDaemon(true);
        serverThread.start();
    }

    private void handleClient(Socket socket) {
        PrintWriter out = null;
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            clients.add(out);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                broadcast(line);
            }
        } catch (Exception ignored) {
        } finally {
            if (out != null) clients.remove(out);
            try { socket.close(); } catch (Exception ignored) {}
        }
    }

    public void broadcast(String line) {
        for (PrintWriter w : new ArrayList<>(clients)) {
            try { w.println(line); } catch (Exception ignored) {}
        }
    }

    public void stop() { running = false; }
}
