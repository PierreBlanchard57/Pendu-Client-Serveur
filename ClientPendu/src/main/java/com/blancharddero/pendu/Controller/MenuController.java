package com.blancharddero.pendu.Controller;

import com.blancharddero.pendu.Menu;
import com.blancharddero.pendu.Utils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class MenuController extends Controller{
    private String username;
    public MenuController(String username){
        this.username =username;
    }
    public  MenuController(String username,String errorMessage){
        this.username =username;
        Platform.runLater(()->Utils.displayInfoMessage(Utils.getSceneRoot(quit),errorMessage,60,60,360,360));
    }
    @FXML
    private Button quit,newGame,loadGame;
    @FXML
    private Label name;
    @FXML
    private void initialize(){
        name.setText("Bonjour "+ username +" !");
        Platform.runLater(() -> name.setLayoutX((480-name.getWidth())/2));
    }

    @FXML
    protected void onQuit(){
        Stage stage=(Stage)(quit.getScene().getWindow());
        stage.close();
    }
    @FXML
    protected void newGame() throws IOException {
        Utils.changeScene(Utils.getStage(newGame),"option-view.fxml",new OptionController(username),480,480);
    }
    @FXML
    protected void multiplayer() throws IOException {
        Utils.changeScene(Utils.getStage(newGame),"multi-view.fxml",new MultiController(username),480,480);
    }
    @FXML
    protected void loadGame() throws IOException {
        Utils.changeScene(Utils.getStage(newGame),"loadgame-view.fxml",new LoadGameController(username),480,480);
    }
}