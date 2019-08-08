import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ResourceBundle;
import java.util.Set;

public class ClientController implements Initializable {

    @FXML
    TextField tfLogin;

    @FXML
    PasswordField pfPassword;

    @FXML
    HBox hbAuth;

    @FXML
    HBox hbFilesList;

    @FXML
    HBox hbManagePanel;

    @FXML
    ListView<String> filesListClient;

    @FXML
    ListView<String> filesListServer;

    boolean focus;

    private final String filesPath = "client/client_storage/";
    private Set<String> listServer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Network.start();

//        while (true) {
//            try {
//                AbstractMessage am = Network.readObject();
//                Command authAnswer = (Command) am;
//                if (authAnswer.getCommand().equals("auth_ok")) {
//                    hbAuth.setVisible(false);
//                    hbFilesList.setVisible(true);
//                    hbManagePanel.setVisible(true);
//                    break;
//                }
//                if (authAnswer.getCommand().equals("auth_err")) {
//                    clearAuthField();
//                    continue;
//                }
//            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

        filesListClient.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        filesListClient.requestFocus();
        filesListClient.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                focus = true;
            }
        });

        filesListServer.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        filesListServer.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                focus = false;
            }
        });

        Thread t = new Thread(() -> {
            try {
                while (true) {
                    AbstractMessage am = Network.readObject();
                    if (am instanceof Command) {
                        Command authAnswer = (Command) am;
                        if (authAnswer.getCommand().equals("auth_ok")) {
                            hbAuth.setVisible(false);
                            hbFilesList.setVisible(true);
                            hbManagePanel.setVisible(true);
                        }
                        if (authAnswer.getCommand().equals("auth_err")) {
                            clearAuthField();
                            continue;
                        }
                    }
                    if (am instanceof FileData) {
                        FileData fd = (FileData) am;
                        Files.write(Paths.get(filesPath + fd.getFileName()), fd.getData(), StandardOpenOption.CREATE);
                        refreshLocalFilesList();
                    }
                    if (am instanceof FilesList) {
                        FilesList flr = (FilesList) am;
                        listServer = flr.getFilesList();
                        refreshRemoteFilesList();
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

        refreshLocalFilesList();
        Network.sendMsg(new Command("list"));
    }

    public void pressOnAuthBtn(ActionEvent actionEvent) {
        System.out.println(tfLogin.getText() + " " + pfPassword.getText());
        Network.sendMsg(new AuthRequest(tfLogin.getText(), pfPassword.getText()));
        clearAuthField();
    }

    public void clearAuthField() {
        tfLogin.clear();
        pfPassword.clear();
    }

    public void pressOnCopyBtn(ActionEvent actionEvent) {
        if (getFocusClient()) {
            for (Object o : getSelectedItem(filesListClient)) {
                try {
                    Network.sendMsg(new FileData(Paths.get(filesPath + filesListClient.getItems().get((int) o))));
                    Network.sendMsg(new Command("list"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            for (Object o : getSelectedItem(filesListServer)) {
                Network.sendMsg(new Command("copy", filesListServer.getItems().get((int) o)));
            }
        }
    }

    public void pressOnDelBtn(ActionEvent actionEvent) {
        if (getFocusClient()) {
            for (Object o : getSelectedItem(filesListClient)) {
                String path = "client/client_storage/" + filesListClient.getItems().get((int) o);
                if (Files.exists(Paths.get(path))) {
                    try {
                        Files.delete(Paths.get(path));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    refreshLocalFilesList();
                }
            }
        } else {
            for (Object o : getSelectedItem(filesListServer)) {
                Network.sendMsg(new Command("del", filesListServer.getItems().get((int) o)));
            }
        }
    }

    public void pressOnMoveBtn(ActionEvent actionEvent) {
        pressOnCopyBtn(actionEvent);
        pressOnDelBtn(actionEvent);
    }

    private ObservableList getSelectedItem(ListView<String> lv) {
        ObservableList selectedIndices = lv.getSelectionModel().getSelectedIndices();
        return selectedIndices;
    }

    private boolean getFocusClient() {
        return focus;
    }

    public void refreshLocalFilesList() {
        updateUI(() -> {
            try {
                filesListClient.getItems().clear();
                Files.list(Paths.get(filesPath)).map(p -> p.getFileName().toString()).forEach(o -> filesListClient.getItems().add(o));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void refreshRemoteFilesList() {
        updateUI(() -> {
            filesListServer.getItems().clear();
            for (String s : listServer) {
                filesListServer.getItems().add(s);
            }
        });
    }

    public static void updateUI(Runnable r) {
        if (Platform.isFxApplicationThread()) {
            r.run();
        } else {
            Platform.runLater(r);
        }
    }
}
