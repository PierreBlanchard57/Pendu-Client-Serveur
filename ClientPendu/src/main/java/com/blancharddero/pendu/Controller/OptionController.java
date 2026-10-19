package com.blancharddero.pendu.Controller;

import com.blancharddero.pendu.Loader;
import com.blancharddero.pendu.Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.controlsfx.control.RangeSlider;

import java.io.IOException;
import java.util.HashMap;

public class OptionController extends Controller{
    private String username;
    private int preparedFrom,preparedTo,preparedLives,preparedMinutes;
    public OptionController(String username){//constructeur de base
        this.username=username;
    }
    public OptionController(String username,int from,int to,int lives,int minutes){//constructeur lorsqu'on rejoue une partie solo
        this.username=username;
        preparedFrom=from;
        preparedTo=to;
        preparedLives=lives;
        preparedMinutes=minutes;
    }
    @FXML
    private Button play;
    @FXML
    private RadioButton radioRandom,radioDefine;
    @FXML
    private CheckBox timed;
    @FXML
    private Label livesLabel,minutesLabel;
    @FXML
    private Slider lives,minutes;
    @FXML
    private RangeSlider length;
    @FXML
    private void initialize(){
        Utils.bindSliderToLabel(lives,livesLabel);
        Utils.bindSliderToLabel(minutes,minutesLabel);
        ToggleGroup toggleGroup=new ToggleGroup();
        radioRandom.setToggleGroup(toggleGroup);
        radioDefine.setToggleGroup(toggleGroup);
        if(preparedFrom==0)radioRandom.setSelected(true);
        else {
            radioDefine.setSelected(true);
            length.setDisable(false);
        }
        length.setHighValue(preparedTo);
        length.setLowValue(preparedFrom);
        lives.setValue(preparedLives);
        if(preparedMinutes==0){
            timed.setSelected(false);
            minutes.setDisable(true);
        }
        else {
            timed.setSelected(true);
            minutes.setDisable(false);
        }
        minutes.setValue(preparedMinutes);
    }
    @FXML
    protected  void onPlay() throws Exception {
        HashMap<String,Object> answer=new HashMap<>();
        Stage stage=(Stage) play.getScene().getWindow();

        HashMap<String,Object> data=new HashMap<>();
        data.put("length",length.isDisable()?"random":(int)length.getLowValue()+","+(int)length.getHighValue());
        data.put("lives",(int)lives.getValue());
        data.put("timer",minutes.isDisable()?0:(int)minutes.getValue()*60);
        data.put("username",username);
        data.put("mode","single");

        Loader.doLoadingProcess(stage, data, answer,"TCP", () -> {
            try {
                Utils.changeScene(stage,"game-view.fxml",new GameController((String) answer.get("word"),
                        length.isDisable()?0:(int)length.getLowValue(),
                        length.isDisable()?0:(int)length.getHighValue(),
                        (int)lives.getValue(),
                        (Integer) answer.get("timer"),username),480,480);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
    @FXML
    private void back(ActionEvent e) throws IOException {
        Utils.previousScene(e,"menu-view.fxml",new MenuController(username),480,480);
    }
    @FXML
    private void toggleRandom(){
        length.setDisable(!length.isDisable());
    }
    @FXML
    private void toggleMinutes(){
        minutes.setDisable(!minutes.isDisable());
    }

}
