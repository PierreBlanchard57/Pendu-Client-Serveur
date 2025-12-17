package com.blancharddero;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * This class uses java Socket server to connection tentatives and the creation of Sessions to play the game of Hangman
 */
public class ServerPendu {
    
    // Sockets declaration
    private static ServerSocket server;
    private static DatagramSocket connectionSocket;

    // Buffer pour la connexion UDP
    final static int taille = 1024; 
    final static byte buffer[] = new byte[taille];

    // Server parameters
    private static Map<String, Long> authorizedUsers = new HashMap<>(); // The user connected via UDP and authorized for TCP connection for a limited time
    private static final int TIMEOUT = 5000; // How long the server wait before timout for TCP connection.
    private static final int MAX_THREADS = 10; // Maximum number of threads on the server at the same time
    private static ExecutorService threadPool;
    private static ArrayList<ClientHandler> active_users = new ArrayList<>();
    

    public static void main(String args[]) {
        // socket server port on which it will listen
        int portTCP = 4570;
        int portUDP = 9518;

        try {
            // The ThreadPool to manage the number of threads active at the same time
            threadPool = Executors.newFixedThreadPool(MAX_THREADS);

            // Sockets creation
            server = new ServerSocket(portTCP);
            connectionSocket = new DatagramSocket(portUDP);
        
            // keep listening indefinitely until program terminates
            // create a thread for each client connected
            while(true){
                System.out.println("---\n\nWaiting for the client connection attempt...");

                // Waiting Connection via UDP
                DatagramPacket data = new DatagramPacket(buffer,buffer.length);
                connectionSocket.receive(data);

                System.out.println("\n\nClient trying to connect");

                // Starts a thread to handle the connexion 
                threadPool.execute(() -> handleUDPRequest(connectionSocket, server, data));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("---\n\nSHUTTING DOWN SERVER\n\n---");
            threadPool.shutdownNow();
            // Closing Sockets
            if (connectionSocket != null && !connectionSocket.isClosed()) {
                connectionSocket.close();
            }
            if (server != null && !server.isClosed()) {
                try {
                    server.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Closing the ThreadPool
            threadPool.shutdown();
            try {
                if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException ex) {
                threadPool.shutdownNow();
            }
        }
    }

    /**
     * Used to handle one tentatives of UDP connection
     * @param connectionSocket The Datagram Socket
     * @param server The Socket of the Server
     * @param data The DatagramPacket
     */
    private static void handleUDPRequest(DatagramSocket connectionSocket, ServerSocket server, DatagramPacket data) {
        try {
            // Getting client's message and IP
            String clientMessage = new String(data.getData(), 0, data.getLength());
            String clientIP = data.getAddress().getHostAddress();

            // Verification of client's credentials
            if (ManipulationFichiers.connectionJoueur(clientMessage)) {
                // Send "OK" to client
                data.setData("OK".getBytes());
                connectionSocket.send(data);

                // User with this IP can connect to the server via TCP for a limited time
                authorizedUsers.put(clientIP, System.currentTimeMillis());
                System.out.println("\n\nUser from IP " + clientIP + " is authorized to connect via TCP.");

                // Handle the TCP connection and create a new thread to manage the succesful connection
                handleTCPConnection(clientIP, server, clientMessage.split(":")[0]);
            } else {
                // Send "NO" to client
                System.out.println("\n\n" + clientIP + " failed to connect.");
                data.setData("NO".getBytes());
                connectionSocket.send(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Used to handle the TCP connection and create a new thread for managing it.
     * Wait for some time before a timeout
     * @param clientIP The client's IP used for information in the prints
     * @param server The socket of the server
     */
    private static void handleTCPConnection(String clientIP, ServerSocket server, String userName) {
        try {
            // Imposer un délai d'attente pour la connexion TCP
            server.setSoTimeout(TIMEOUT);
            System.out.println("\n\nWaiting for TCP connection from " + clientIP);

            // Waiting TCP connection
            Socket socket = server.accept();
            String socketIP = socket.getInetAddress().getHostAddress();

            // Vérifier si l'adresse IP de la connexion TCP correspond à celle d'un des utilisateur autorisé via UDP
            if (authorizedUsers.containsKey(socketIP) && (System.currentTimeMillis() - authorizedUsers.get(socketIP)) < TIMEOUT) {
                // Créer un thread pour le client connecté
                //Thread client = new Thread(new clientServerConnection(socket));
                ClientHandler ch = new ClientHandler(socket, userName);
                active_users.add(ch);
                Thread client = new Thread(ch);
                client.start();
                System.out.println("\n\nTCP connection accepted from " + socketIP);
            } else {
                System.out.println("\n\nUnauthorized TCP connection attempt from " + socketIP);
                socket.close(); // Fermer la connexion si non autorisée
            }

            authorizedUsers.remove(socketIP); // Retirer l'utilisateur de la liste autorisée
        } catch (SocketTimeoutException e) {
            System.out.println("\n\nTCP connection timeout for " + clientIP + "\n\n---");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
