import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainClient extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader fxmlLoaderAuth = new FXMLLoader(getClass().getResource("/auth.fxml"));
        Parent pAuth = fxmlLoaderAuth.load();
        primaryStage.setTitle("Auth");
        Scene sAuth = new Scene(pAuth);
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/main.fxml"));
        Parent pMain = fxmlLoader.load();
        primaryStage.setTitle("Client");
        Scene sMain = new Scene(pMain);
        if (isAuth(this)) {
            primaryStage.setScene(sMain);
        } else {
            primaryStage.setScene(sAuth);
        }
        primaryStage.show();
    }

    private boolean isAuth(MainClient mainClient) {
        return true;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
