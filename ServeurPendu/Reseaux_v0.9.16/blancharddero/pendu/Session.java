package com.blancharddero.pendu;

import java.util.HashMap;

import com.blancharddero.ClientHandler;

public abstract class Session {
    
    private ClientHandler host;

    public Session(ClientHandler user){
        this.host = user;
    }

    public abstract void startSession(HashMap<String, Object> parameters);

    public ClientHandler getHost(){
        return host;
    }

    public void removeHost(){
        host = null;
    }

}
