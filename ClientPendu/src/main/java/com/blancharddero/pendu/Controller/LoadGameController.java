package com.blancharddero.pendu.Controller;

import com.blancharddero.pendu.Utils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.IOException;
import java.util.Random;

public class LoadGameController extends  Controller{
    String username;
    public  LoadGameController(String username){
        this.username=username;
        Platform.runLater(()->{
            games.getItems().add(createGamePane("A _ _ _ _",5,153));
            games.getItems().add(createGamePane("_ _ E _ T",2,0));
        });
    }
    private Pane createGamePane(String word,int lives,int seconds){
        Random random=new Random();
        Rectangle icon=new Rectangle(30,30,
                Color.rgb(random.nextInt(0,256),random.nextInt(0,256),random.nextInt(0,256)));
        icon.setLayoutX(10);
        icon.setLayoutY(10);

        Label wordLabel=new Label(word);
        wordLabel.setFont(Font.font("Arial", FontWeight.BOLD,16));
        wordLabel.setLayoutX(50);
        wordLabel.setLayoutY(16);

        Label livesLabel=new Label("Echecs tolérés restants: "+lives);
        livesLabel.setLayoutX(200);

        Label timerLabel=new Label(seconds==0?"Pas minuté":"Temps restant : "+seconds/60+" min "+seconds%60+" s");
        timerLabel.setLayoutX(200);
        timerLabel.setLayoutY(38);

        Pane pane=new Pane(icon,wordLabel,livesLabel,timerLabel);
        pane.setPrefHeight(50);
        return pane;
    }
    @FXML
    private ListView<Pane> games;
    @FXML
    private void back(ActionEvent e) throws IOException {
        Utils.previousScene(e,"menu-view.fxml",new MenuController(username),480,480);
    }
    @FXML
    private void load(){

    }
}
