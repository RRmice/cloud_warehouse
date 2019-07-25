import io.netty.channel.ChannelInboundHandlerAdapter;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.ArrayList;

public class LoginController{
    @FXML
    TextField login;

    @FXML
    PasswordField password;

    @FXML
    VBox globParent;

    public MainController backController;

    public void auth(ActionEvent actionEvent){
        backController.Authorization(login.getText(), password.getText());
    }

    public void hideScene() {
        updateUI(() -> {
            globParent.getScene().getWindow().hide();
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
