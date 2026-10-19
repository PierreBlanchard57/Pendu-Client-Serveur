package com.blancharddero.pendu;

import com.blancharddero.pendu.Connexion.ConnexionTCP;
import com.blancharddero.pendu.Controller.Controller;
import javafx.animation.ScaleTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class Utils {
    private  Utils() {}//impossibilité d'instancier la classe car elle est utilitaire uniquement

    /**
     * Obtient la stage à partir d'un noeud appartenant à cette scene
     * @param node
     * @return
     */
    public static Stage getStage(Node node){
        return (Stage)node.getScene().getWindow();
    }

    /**
     * Obtient la scene à partir d'un noeud appartenant à cette scene
     * @param node
     * @return
     */
    public static Scene getScene(Node node){
        return node.getScene();
    }

    /**
     * Obtient le noeud racine de la scene à partir d'un noeud appartenant à cette scene
     * @param node
     * @return
     */
    public static Pane getSceneRoot(Node node){
        return (Pane) node.getScene().getRoot();
    }

    /**
     * Change la scene courante.
     * @param stage La stage dont on va changer la scene
     * @param resource Le nom du fichier FXML de la scene à charger
     * @param controller Le controleur associé à la scene à charger
     * @param width La largeur de la scene à charger
     * @param height La hauteur de la scene à charger
     * @throws IOException
     */
    public static void changeScene(Stage stage, String resource, Controller controller,int width,int height) throws IOException {
        FXMLLoader fxmlLoader=new FXMLLoader(Menu.class.getResource(resource));
        fxmlLoader.setController(controller);
        Scene scene = new Scene(fxmlLoader.load(), width, height);
        stage.setScene(scene);
    }

    /**
     * Est utilisé dans les evenements boutons retour pour afficher la scène précedente
     * @param e Evenement pour recuperer la scene
     * @param resource Nom du fichier FXML de la scene
     * @param controller Controleur associé à la scene
     * @param width Largeur de la scene
     * @param height Hauteur de la scene
     * @throws IOException
     */
    public static void previousScene(ActionEvent e,String resource,Controller controller,int width,int height) throws IOException {
        Button button= (Button) e.getSource();
        changeScene(getStage(button),resource,controller,width,height);
    }

    /**
     * Permet de lier la valeur d'un slider à un label pour un affichage réactif.
     * @param slider Slider dont la valeur va etre traduite
     * @param label Label affichant la valeur du label
     */
    public static void bindSliderToLabel(Slider slider, Label label){
        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            label.setText(String.valueOf(newValue.intValue()));
            double colorRatio = newValue.doubleValue() / 26;
            label.setTextFill(Color.rgb(0, (int) (255 * colorRatio), (int) (64 + 191 * colorRatio)));
        });
    }
    public static void forceDisconnexion(Stage stage) throws IOException {
        ConnexionTCP.resetConnexion();
        FXMLLoader fxmlLoader=new FXMLLoader(Menu.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 480, 480);
        stage.setScene(scene);
    }
    public static void displayInfoMessage(Pane sceneRoot,String message,int x,int y,int width,int height){
        Pane pane=new Pane();
        pane.setBackground(new Background(new BackgroundFill(Color.rgb(255,255,255,0.95), CornerRadii.EMPTY, Insets.EMPTY)));
        pane.setLayoutX(x);
        pane.setLayoutY(y);
        pane.setPrefWidth(width);
        pane.setPrefHeight(height);

        Label label=new Label(message);
        label.setLayoutX(width/2-2*message.length());
        label.setLayoutY(height/3);

        Button button=new Button("OK");
        button.setOnMouseClicked(event -> {
            sceneRoot.getChildren().remove(pane);
        });
        button.setLayoutX(width/2);
        button.setLayoutY((2*height)/3);
        pane.getChildren().addAll(label,button);
        sceneRoot.getChildren().add(pane);
        ScaleTransition scaleTransition=new ScaleTransition(Duration.millis(500));
        scaleTransition.setNode(pane);
        scaleTransition.setFromX(0);
        scaleTransition.setFromY(0);
        scaleTransition.setToX(1);
        scaleTransition.setToY(1);
        scaleTransition.play();
    }
}
