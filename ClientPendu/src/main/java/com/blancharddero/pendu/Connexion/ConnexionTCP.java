package com.blancharddero.pendu.Connexion;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/*
Classe qui gère les communications UDP,côté client
 */
public class ConnexionTCP {
    public static final String SERVER_IP="127.0.0.1";//"192.168.78.186
    public static final int SERVER_PORT=4570;

    public static Socket getSocket() {
        return socket;
    }

    private static Socket socket;
    private static ObjectOutputStream out;
    private static ObjectInputStream in;

    public static void establishConnexion() throws Exception {
        boolean connected = false;
        while (!connected) {
            try {
                socket = new Socket(SERVER_IP, SERVER_PORT);
                resetStreams(); // Réinstancier les flux
                connected = true;
                System.out.println("Connexion established!");
            } catch (Exception e) {
                System.out.println("Server not found! Trying again...");
            }
        }
    }

    private static void resetStreams() throws IOException {
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());
    }

    public static void sendMessage(Object message) throws Exception {
        if (socket == null) throw new Exception("Connexion must be established!");
        out.writeObject(message);
        out.flush();
        out.reset();
        System.out.println("Message sent: "+message);
    }

    public static void receiveMessage(Object receiver) throws Exception {
        if (socket == null) throw new Exception("Connexion must be established!");
        socket.setSoTimeout(7000);
        System.out.println("Waiting for response...");
        Object result = in.readObject();

        if (receiver instanceof StringBuilder && result instanceof String) {
            ((StringBuilder) receiver).append(result);
        } else if (receiver instanceof HashMap && result instanceof HashMap<?, ?>) {
            HashMap<String, Object> map = (HashMap<String, Object>) result;
            ((HashMap<String, Object>) receiver).putAll(map);
        } else {
            throw new IllegalArgumentException("The passed object cannot be parsed! :"
                    +result.getClass().getName()+" to" +receiver.getClass().getName());
        }

        System.out.println("Received message: " + receiver.toString());
    }
    public static void receiveMessage(Object receiver,int timeout) throws Exception {
        if (socket == null) throw new Exception("Connexion must be established!");
        socket.setSoTimeout(timeout);
        System.out.println("Waiting for response...");
        Object result = in.readObject();

        if (receiver instanceof StringBuilder && result instanceof String) {
            ((StringBuilder) receiver).append(result);
        } else if (receiver instanceof HashMap && result instanceof HashMap<?, ?>) {
            HashMap<String, Object> map = (HashMap<String, Object>) result;
            ((HashMap<String, Object>) receiver).putAll(map);
        } else {
            throw new IllegalArgumentException("The passed object cannot be parsed! :"
                    +result+" to" +receiver.getClass().getName());
        }

        System.out.println("Received message: " + receiver.toString());
    }


    public static void resetConnexion() throws IOException {
        socket.close();
        socket=null;
        System.out.println("TCP Connexion reset!");
    }
}
