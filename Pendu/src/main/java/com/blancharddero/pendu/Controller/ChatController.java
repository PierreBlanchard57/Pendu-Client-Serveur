package com.blancharddero.pendu.Controller;

import com.blancharddero.pendu.Connexion.ConnexionUDP;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ChatController extends Controller{
    @FXML
    private TextArea chat;
    @FXML
    private TextField message;
    @FXML
    private void send() throws Exception {
        String msg=username+" : "+message.getText();
        ConnexionUDP.getDatagramSocketChat().send(new DatagramPacket(msg.getBytes(),msg.length(),
                InetAddress.getByName(ConnexionUDP.SERVER_IP),ConnexionUDP.SERVER_CHAT_PORT));
    }
    public void setStopListeningThread(boolean stopListeningThread) {
        this.stopListeningThread = stopListeningThread;
    }

    private boolean stopListeningThread=false;
    private  String username;

    public ChatController(String username){
        this.username=username;

        Thread listeningThread=new Thread(() -> {
            while (!stopListeningThread){
                try {
                    byte buffer[]=new byte[1024];
                    ConnexionUDP.getDatagramSocketChat().receive(new DatagramPacket(buffer,buffer.length));
                    Platform.runLater(()-> chat.setText(chat.getText()+new String(buffer, StandardCharsets.UTF_8).trim()+"\r\n"));
                } catch (Exception e) {

                }
            }
        });
        listeningThread.setDaemon(true);
        listeningThread.start();
    }
}
