package com.amplitudehack.antiscamchecker.data.model;

import com.amazonaws.services.textract.model.Block;

import java.util.List;

public class MainUIState {

    public enum State {
        TEXTRACT_SUCCESS,
        CALL_GPT_SUCCESS,
        ERROR
    }

    private final State state;
    private List<Block> blockList;
    private String concatenatedText;
    private String chatGptResponse;
    private String errMsg;

    private MainUIState(State state, List<Block> blockList, String concatenatedText) {
        this.state = state;
        this.blockList = blockList;
        this.concatenatedText = concatenatedText;
    }

    private MainUIState(State state, String chatGptResponse, String errMsg) {
        this.state = state;
        this.chatGptResponse = chatGptResponse;
        this.errMsg = errMsg;
    }

    private MainUIState(State state, String errMsg) {
        this.state = state;
        this.errMsg = errMsg;
    }

    public static MainUIState successCallTextract(List<Block> blockList, String concatenatedText) {
        return new MainUIState(State.TEXTRACT_SUCCESS, blockList, concatenatedText);
    }

    public static MainUIState successCallChatGpt(String chatGptResponse) {
        return new MainUIState(State.CALL_GPT_SUCCESS, chatGptResponse, null);
    }

    public static MainUIState error(String errMsg) {
        return new MainUIState(State.ERROR, errMsg);
    }

    public State getState() {
        return state;
    }

    public String getChatGptResponse() {
        return chatGptResponse;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public List<Block> getBlockList() {
        return blockList;
    }

    public String getConcatenatedText() {
        return concatenatedText;
    }
}


