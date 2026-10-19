package com.blancharddero.pendu.Controller;

import com.blancharddero.pendu.Connexion.ConnexionTCP;
import com.blancharddero.pendu.Controller.Controller;
import com.blancharddero.pendu.Controller.GameController;
import com.blancharddero.pendu.Loader;
import com.blancharddero.pendu.Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class OwnerRoomController extends Controller {
    @FXML
    private Slider livesSlider,minutesSlider;
    @FXML
    private Label lives,minutes;
    @FXML
    private TextField word;
    @FXML
    private Button play;
    @FXML
    private void initialize(){
        Utils.bindSliderToLabel(livesSlider,lives);
        Utils.bindSliderToLabel(minutesSlider,minutes);
    }
    @FXML
    private void play(ActionEvent e) throws Exception {
        if(listView.getItems().size()>0){//on peut faire quelque chose si au moins un joueur et mot de longueur supérieur à 2
            if(word.getText().length()>=3){
                Button button= (Button) e.getSource();
                HashMap<String,Object> message=new HashMap<>();
                message.put("word",word.getText());
                message.put("length","("+word.getText().length()+","+word.getText().length()+")");
                message.put("lives",(int)livesSlider.getValue());
                message.put("timer",minutesSlider.isDisable()?0:(int)minutesSlider.getValue()*60);

                HashMap<String,Object> answer=new HashMap<>();
                ConnexionTCP.sendMessage(message);
            }else{
                Utils.displayInfoMessage(Utils.getSceneRoot(listView),
                        "Vous devez saisir un mot d'au moins 3 lettres pour pouvoir jouer!",
                        20,20,440,440);
            }
        }
        else{
            Utils.displayInfoMessage(Utils.getSceneRoot(listView),
                    "Vous devez attendre au moins 1 joueur pour pouvoir jouer!",20,20,440,440);
        }
    }
    @FXML
    private void toggleMinutes(){
        minutesSlider.setDisable(!minutesSlider.isDisable());
    }

    private ListView listView;
    public OwnerRoomController(ListView<String> listView){
        this.listView=listView;
    }
}
