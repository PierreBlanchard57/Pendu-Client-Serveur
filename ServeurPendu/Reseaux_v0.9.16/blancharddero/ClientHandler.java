package com.blancharddero;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.blancharddero.pendu.GameState;
import com.blancharddero.pendu.MainMenuState;

public class ClientHandler implements Runnable {
    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private volatile boolean running = true;
    private String username;
    private String roomName;
    private GameState currentState;
    private int UDPport = 0;

    public ClientHandler(Socket socket, String user) {
        this.socket = socket;
        username = user;
        currentState = new MainMenuState(this);
    }

    @Override
    public void run() {
        try {
            // Initialize streams
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());

            // Main communication loop
            while (running && !socket.isClosed()) {
                try {
                    Object input = ois.readObject(); // Read client data
                    currentState.handleMessage(input); // Process client input
                } catch (EOFException e) {
                    System.out.println("Client closed connection.");
                    break;
                } catch (IOException e) {
                    System.out.println("Connection lost with client.");
                    break;
                } catch (ClassNotFoundException e) {
                    System.out.println("Invalid object received.");
                }
            }
        } catch (IOException e) {
            System.out.println("Error initializing client streams.");
        } finally {
            disconnectClient(); // Clean up resources
        }
    }

    public void setCurrentState(GameState newState) {
        this.currentState = newState;
    }

    public void sendMessage(Object message){
        if (!socket.isClosed() && oos != null)
            try {
                oos.writeObject(message);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println(username + ": Error while writing object");
                running = false;
            }
    }

    public String getUsername(){
        return username;
    }

    public void setUDPport(int port){
        this.UDPport = port;
    }

    public int getUDPport(){
        return UDPport;
    }

    public boolean isDisconnected(){
        return  socket == null || socket.isClosed();
    }

    private void disconnectClient() {
        running = false;
        if(roomName != null) MainMenuState.removeRoom(roomName);
        try {
            if (ois != null) ois.close();
            if (oos != null) oos.close();
            if (socket != null && !socket.isClosed()) socket.close();
            System.out.println("Client disconnected and resources cleaned up.");
        } catch (IOException e) {
            System.out.println("Error closing client resources.");
        }
    }

    public void setRoomName(String string) {
        this.roomName = string;
    }

    public String getRoomName(){
        return roomName;
    }

    public Socket getSocket(){
        return socket;
    }

}