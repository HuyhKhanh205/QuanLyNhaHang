package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {

    private static final Properties props = new Properties();

    static {
        // Ưu tiên file bên cạnh JAR, fallback về classpath
        java.io.File external = new java.io.File(
                System.getProperty("user.dir"), "config.properties");
        try {
            if (external.exists()) {
                try (InputStream in = new java.io.FileInputStream(external)) {
                    props.load(in);
                }
            } else {
                try (InputStream in = AppConfig.class.getClassLoader()
                        .getResourceAsStream("config.properties")) {
                    if (in != null) props.load(in);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Không thể đọc config.properties", e);
        }
    }

    public static String get(String key) {
        return props.getProperty(key, "");
    }

    public static int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(props.getProperty(key));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static String getDbUrl()       { return get("db.url"); }
    public static String getDbUser()      { return get("db.user"); }
    public static String getDbPassword()  { return get("db.password"); }
    public static String getSocketHost()  { return get("socket.host"); }
    public static int    getSocketPort()  { return getInt("socket.port", 9999); }
}
