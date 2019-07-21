import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ResourceBundle;

public class AuthController implements Initializable {
    @FXML
    TextField tfUsername;

    @FXML
    TextField tfPassword;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Network.start();
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    AbstractMessage am = Network.readObject();
                    if (am instanceof FileData) {
                        FileData fm = (FileData) am;
                        Files.write(Paths.get("client/client_storage/" + fm.getFileName()), fm.getData(), StandardOpenOption.CREATE);
                    }
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            } finally {
                Network.stop();
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public void pressOnAuthBtn(ActionEvent actionEvent) {
        if (tfUsername.getLength() > 0 && tfPassword.getLength() > 0) {
            Network.sendMsg(new AuthRequest(tfUsername.getText(), tfPassword.getText()));
            tfUsername.clear();
            tfPassword.clear();
        }
    }

    public static void updateUI(Runnable r) {
        if (Platform.isFxApplicationThread()) {
            r.run();
        } else {
            Platform.runLater(r);
        }
    }
}
