package com.blancharddero.pendu.Controller;

import com.blancharddero.pendu.Connexion.ConnexionUDP;
import com.blancharddero.pendu.Loader;
import com.blancharddero.pendu.Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.DatagramSocket;
import java.util.HashMap;

public class CreateRoomController extends Controller {
    @FXML
    private TextField roomName,password;
    private String username;
    public  CreateRoomController(String username){
        this.username=username;
    }
    @FXML
    private void create(ActionEvent e) throws IOException, InterruptedException {
        Button button= (Button) e.getSource();
        Stage stage= (Stage) button.getScene().getWindow();
        //Utils.changeScene(stage,"room-view.fxml",new RoomController(roomName.getText(),username),480,480);
        HashMap<String,Object> message=new HashMap<>();
        message.put("mode","multi");
        message.put("action","create");
        message.put("roomName",roomName.getText());
        message.put("username",username);
        message.put("roomPassword",password.isDisable()?"":password.getText());
        //initialisation du socket de chat udp
        ConnexionUDP.setDatagramSocketChat(new DatagramSocket());
        message.put("port",ConnexionUDP.getDatagramSocketChat().getLocalPort());
        StringBuilder answer=new StringBuilder();
        Loader.doLoadingProcess(stage, message, answer, "TCP", () -> {
            try {
                Utils.changeScene(stage,"room-view.fxml",new RoomController(roomName.getText(),
                         username),480,480);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }
    @FXML
    private void togglePassword(){
        password.setDisable(!password.isDisable());
    }
    @FXML
    private void back(ActionEvent e) throws IOException {
        Utils.previousScene(e,"multi-view.fxml",new MultiController(username),480,480);
    }
}
