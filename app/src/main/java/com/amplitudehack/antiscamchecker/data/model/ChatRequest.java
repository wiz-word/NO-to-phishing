package com.amplitudehack.antiscamchecker.data.model;

import java.util.List;

public class ChatRequest {

    private final String model;
    private final List<Message> messages;

    public ChatRequest(String model, List<Message> messages) {
        this.model = model;
        this.messages = messages;
    }

    public String getModel() {
        return model;
    }

    public List<Message> getMessages() {
        return messages;
    }
}



