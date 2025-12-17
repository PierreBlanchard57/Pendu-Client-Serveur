package com.blancharddero.pendu.Controller;

import com.blancharddero.pendu.Connexion.ConnexionTCP;
import com.blancharddero.pendu.Loader;
import com.blancharddero.pendu.Menu;
import com.blancharddero.pendu.Utils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController extends Controller{
    @FXML
    private TextField login,password;
    @FXML
    private Label error;
    @FXML
    private void connect() throws IOException, InterruptedException {
        Scene scene=login.getScene();
        Stage stage=(Stage) scene.getWindow();
        StringBuilder stringBuilder = new StringBuilder();
        Loader.doLoadingProcess(stage, login.getText()+":"+password.getText(), stringBuilder,"UDP", ()-> {
                if(!stringBuilder.toString().trim().equals("OK")){
                    stage.setScene(scene);
                    error.setVisible(true);
                }
                else {
                    try {
                        Utils.changeScene(stage,"menu-view.fxml",new MenuController(login.getText()),480,480);
                        ConnexionTCP.establishConnexion();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        });
    }
}
