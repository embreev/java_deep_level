import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Client extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/main.fxml"));
        Parent pMain = fxmlLoader.load();
        primaryStage.setTitle("Client");
        Scene sMain = new Scene(pMain);
        primaryStage.setScene(sMain);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        Network.sendMsg(new Command("disconnect"));
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
