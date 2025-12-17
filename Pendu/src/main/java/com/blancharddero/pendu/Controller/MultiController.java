package com.blancharddero.pendu.Controller;

import com.blancharddero.pendu.Loader;
import com.blancharddero.pendu.Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MultiController extends Controller{
    private String username;
    public MultiController(String username){
        this.username=username;

    }
    @FXML
    private void create(ActionEvent e) throws IOException {
        Utils.changeScene(Utils.getStage((Node) e.getSource()),
                "createroom-view.fxml",new CreateRoomController(username),480,480);
    }
    @FXML
    private void back(ActionEvent e) throws IOException {
        Utils.previousScene(e,"menu-view.fxml",new MenuController(username),480,480);
    }
    @FXML
    private void join(ActionEvent e) throws IOException, InterruptedException {
        HashMap<String,Object> message=new HashMap<>();
        message.put("mode","multi");
        message.put("action","join");
        message.put("username",username);

        HashMap<String, Map<String,Object>> answer=new HashMap<>();
        Stage stage=Utils.getStage((Node) e.getSource());
        Loader.doLoadingProcess(stage,message,answer,"TCP", () -> {
            try {
                Utils.changeScene(stage,"joinroom-view.fxml",new JoinRoomController(username,answer),480,480);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

    }
}
