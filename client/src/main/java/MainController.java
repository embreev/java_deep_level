import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    ListView<String> filesListClient;

    @FXML
    ListView<String> filesListServer;

    boolean focus;

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
        filesListServer.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                focus = false;
            }
        });
        filesListServer.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    AbstractMessage am = Network.readObject();
                    if (am instanceof FileMessage) {
                        FileMessage fm = (FileMessage) am;
                        Files.write(Paths.get("client/client_storage/" + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
                        refreshLocalFilesList();
                        refreshRemoteFilesList();
                    }
                    if (am instanceof FileRequest) {
                        FileRequest fr = (FileRequest) am;
                        if (Files.exists(Paths.get("client/client_storage/" + fr.getFilename()))) {
                            FileMessage fm = new FileMessage(Paths.get("client/client_storage/" + fr.getFilename()));
                            Network.sendMsg(fm);
                        }
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
        refreshRemoteFilesList();
    }

    public void pressOnCopyBtn(ActionEvent actionEvent) {
        if (getFocusClient()) {
            System.out.println("Фокус на клиенте");
            for (Object o : getSelectedItem(filesListClient)) {
                System.out.println(filesListClient.getItems().get((int) o));
                try {
                    Network.sendMsg(new FileMessage(Paths.get("client/client_storage/" + filesListClient.getItems().get((int) o))));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                refreshRemoteFilesList();
            }
        } else {
            System.out.println("Фокус на сервере");
            for (Object o : getSelectedItem(filesListServer)) {
                Network.sendMsg(new Command("copy", filesListServer.getItems().get((int) o)));
            }
        }
    }

    public void pressOnDelBtn(ActionEvent actionEvent) {
        if (getFocusClient()) {
            System.out.println("Фокус на клиенте");
            for (Object o : getSelectedItem(filesListClient)) {
                String path = "client/client_storage/" + filesListClient.getItems().get((int) o);
                if (Files.exists(Paths.get(path))) {
                    try {
                        Files.delete(Paths.get(path));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    refreshLocalFilesList();
                    refreshRemoteFilesList();
                }
            }
        } else {
            System.out.println("Фокус на сервере");
            for (Object o : getSelectedItem(filesListServer)) {
                Network.sendMsg(new Command("del", filesListServer.getItems().get((int) o)));
                refreshLocalFilesList();
                refreshRemoteFilesList();
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
                Files.list(Paths.get("client/client_storage")).map(p -> p.getFileName().toString()).forEach(o -> filesListClient.getItems().add(o));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void refreshRemoteFilesList() {
        updateUI(() -> {
            try {
                filesListServer.getItems().clear();
//                Network.sendMsg(new Command("getFilesList"));
                Files.list(Paths.get("server/server_storage")).map(p -> p.getFileName().toString()).forEach(o -> filesListServer.getItems().add(o));
            } catch (IOException e) {
                e.printStackTrace();
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