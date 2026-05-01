package socket;

import java.util.HashMap;
import java.util.Map;

public class SocketMessage {

    private SocketEvent event;
    private Map<String, String> data;

    public SocketMessage() { this.data = new HashMap<>(); }

    public SocketMessage(SocketEvent event, Map<String, String> data) {
        this.event = event;
        this.data = data != null ? data : new HashMap<>();
    }

    public SocketEvent getEvent()           { return event; }
    public Map<String, String> getData()    { return data; }
    public String get(String key)           { return data.getOrDefault(key, ""); }

    /** Serialize to a single-line string: EVENT|key=val|key=val */
    public String toLine() {
        if (data.isEmpty()) return event.name();
        StringBuilder sb = new StringBuilder(event.name());
        for (Map.Entry<String, String> e : data.entrySet()) {
            sb.append('|').append(e.getKey()).append('=').append(e.getValue().replace("|","").replace("=",""));
        }
        return sb.toString();
    }

    /** Deserialize from a line produced by toLine() */
    public static SocketMessage fromLine(String line) {
        if (line == null || line.trim().isEmpty()) return null;
        String[] parts = line.trim().split("\\|");
        SocketEvent ev;
        try { ev = SocketEvent.valueOf(parts[0]); }
        catch (IllegalArgumentException e) { return null; }
        Map<String, String> data = new HashMap<>();
        for (int i = 1; i < parts.length; i++) {
            int eq = parts[i].indexOf('=');
            if (eq > 0) data.put(parts[i].substring(0, eq), parts[i].substring(eq + 1));
        }
        return new SocketMessage(ev, data);
    }

    @Override
    public String toString() { return toLine(); }
}
