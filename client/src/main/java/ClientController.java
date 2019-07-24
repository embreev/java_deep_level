import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ResourceBundle;
import java.util.Set;

public class ClientController implements Initializable {

    @FXML
    ListView<String> filesListClient;

    @FXML
    ListView<String> filesListServer;

    @FXML
    TextField tfUsername;

    @FXML
    TextField tfPassword;

    @FXML
    HBox authPanel;

    @FXML
    Label authMessage;

    @FXML
    HBox clientPanel;

    @FXML
    HBox serverPanel;

    boolean focus;

    private final String filesPath = "client/client_storage/";
    private Set<String> listServer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Network.start();
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

        Thread tMain = new Thread(() -> {
            try {
                while (true) {
                    AbstractMessage am = Network.readObject();
                    if (am instanceof Command) {
                        Command cmd = (Command) am;
                        if (cmd.getCommand().equals("authorized")) {
                            System.out.println("AUTH");
                            authPanel.setVisible(false);
                            authMessage.setVisible(false);
                            clientPanel.setVisible(true);
                            serverPanel.setVisible(true);
                        }
                        if (cmd.getCommand().equals("unauthorized")) {
                            authMessage.setText("Incorrect login or password!");
                            authMessage.setVisible(true);
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
        tMain.setDaemon(true);
        tMain.start();

        refreshLocalFilesList();
//        Network.sendMsg(new Command("list"));
    }

    public void pressOnAuthBtn(ActionEvent actionEvent) {
        if (tfUsername.getLength() > 0 && tfPassword.getLength() > 0) {
            Network.sendMsg(new AuthRequest(tfUsername.getText(), tfPassword.getText()));
            tfUsername.clear();
            tfPassword.clear();
        } else {
            authMessage.setText("User or Password is empty");
            authMessage.setVisible(true);
        }
    }

    public void pressOnCopyBtn(ActionEvent actionEvent) {
        if (getFocusClient()) {
            System.out.println(filesListClient.getItems());
            for (Object o : getSelectedItem(filesListClient)) {
                System.out.println(filesListClient.getItems().get((int) o));
                try {
                    Network.sendMsg(new FileData(Paths.get(filesPath + filesListClient.getItems().get((int) o))));
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

    public void pressOnRefreshBtn(ActionEvent actionEvent) {
        refreshLocalFilesList();
        refreshRemoteFilesList();
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
            System.out.println(listServer);
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
