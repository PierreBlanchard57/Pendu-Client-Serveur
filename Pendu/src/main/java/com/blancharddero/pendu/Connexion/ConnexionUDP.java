package com.blancharddero.pendu.Connexion;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;

/*
Classe qui gère les communications UDP,côté client
 */
public class ConnexionUDP {
    public static final String SERVER_IP="127.0.0.1";//"192.168.78.186
    public static final int SERVER_PORT=9518;


    public static void setServerChatPort(int serverChatPort) {
        SERVER_CHAT_PORT = serverChatPort;
    }

    public static int SERVER_CHAT_PORT;
    private static DatagramSocket datagramSocket;

    public static DatagramSocket getDatagramSocketChat() {
        return datagramSocketChat;
    }

    public static void setDatagramSocketChat(DatagramSocket datagramSocketChat) {
        ConnexionUDP.datagramSocketChat = datagramSocketChat;
    }

    private static DatagramSocket datagramSocketChat;


    public static void sendMessage(String message) throws Exception {
        if( datagramSocket==null)datagramSocket=new DatagramSocket();
        datagramSocket.send(new DatagramPacket(message.getBytes(),message.length(), InetAddress.getByName(SERVER_IP),SERVER_PORT));
        System.out.println("Message sent!");
    }
    public static void receiveMessage(StringBuilder receiver) throws Exception {
        if( datagramSocket==null)datagramSocket=new DatagramSocket();
        datagramSocket.setSoTimeout(7000);
        byte buffer[]=new byte[32];
        datagramSocket.receive(new DatagramPacket(buffer,buffer.length));
        receiver.append(new String(buffer, StandardCharsets.UTF_8));
        System.out.println("Received message: "+receiver.toString());
    }
    public static void receiveMessage(StringBuilder receiver,int timeout) throws Exception {
        if( datagramSocket==null)datagramSocket=new DatagramSocket();
        datagramSocket.setSoTimeout(timeout);
        byte buffer[]=new byte[32];
        datagramSocket.receive(new DatagramPacket(buffer,buffer.length));
        receiver.append(new String(buffer, StandardCharsets.UTF_8));
        System.out.println("Received message: "+receiver.toString());
    }
    public static void resetConnexion(){datagramSocket=null;}

}
