import gui.TaiKhoanGUI;
import socket.SocketManager;
import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {
        // Khởi động Socket layer (server hoặc client tuỳ vào máy)
        try {
            SocketManager.initialize();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Tắt Socket khi app đóng
        Runtime.getRuntime().addShutdownHook(new Thread(SocketManager::shutdown));

        SwingUtilities.invokeLater(() -> {
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Color COLOR_INPUT_BORDER = new Color(150, 150, 150);
            Color COLOR_ACCENT_BLUE = new Color(56, 118, 243);

            UIManager.put("TextField.border", BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(COLOR_INPUT_BORDER),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)));
            UIManager.put("PasswordField.border", BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(COLOR_INPUT_BORDER),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)));
            UIManager.put("ComboBox.border", BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(COLOR_INPUT_BORDER),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)));
            UIManager.put("Button.background", COLOR_ACCENT_BLUE);
            UIManager.put("Button.foreground", Color.WHITE);
            UIManager.put("Button.font", new Font("Arial", Font.BOLD, 16));
            UIManager.put("Label.font", new Font("Arial", Font.PLAIN, 14));

            TaiKhoanGUI loginWindow = new TaiKhoanGUI();
            loginWindow.setVisible(true);
            loginWindow.setExtendedState(JFrame.MAXIMIZED_BOTH);
        });
    }
}
