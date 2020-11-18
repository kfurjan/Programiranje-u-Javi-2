package hr.algebra.utils;

import javafx.scene.control.Alert;

/**
 *
 * @author efurkev
 */
public class MessageUtils {

    public static void showInfoMessage(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.show();
    }
}
