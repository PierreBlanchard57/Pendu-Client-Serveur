package com.blancharddero.pendu;

import java.io.IOException;

public interface GameState {
    void handleMessage(Object message) throws IOException, ClassNotFoundException;
}