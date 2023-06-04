package com.amplitudehack.antiscamchecker.data.model;

import com.amazonaws.services.textract.model.Block;

import java.util.List;

public class MainUIState {

    public enum State {
        TEXTRACT_SUCCESS,
        GPT_SAFE,
        GPT_DETECTED,
        SAGEMAKER_SAFE,
        SAGEMAKER_DETECTED,
        FRAUD_FREEZE_SAFE,
        FRAUD_FREEZE_DETECTED,
        DISPOSABLE_EMAILER_SAFE,
        DISPOSABLE_EMAILER_DETECTED,
        OOP_SCAM_SAFE,
        OOP_SCAM_DETECTED,
        ERROR
    }

    private final State state;
    private List<Block> blockList;
    private String concatenatedText;
    private String chatGptResponse;
    private String errMsg;

    public MainUIState(State state) {
        this.state = state;
    }

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

    public static MainUIState gptSafe(){
        return new MainUIState(State.GPT_SAFE);
    }

    public static MainUIState gptDetected(){
        return new MainUIState(State.GPT_DETECTED);
    }

    public static MainUIState sagemakerSafe(){
        return new MainUIState(State.SAGEMAKER_SAFE);
    }

    public static MainUIState sagemakerDetected(){
        return new MainUIState(State.SAGEMAKER_DETECTED);
    }

    public static MainUIState fraudFreezeSafe(){
        return new MainUIState(State.FRAUD_FREEZE_SAFE);
    }

    public static MainUIState fraudFreezeDetected(){
        return new MainUIState(State.FRAUD_FREEZE_DETECTED);
    }

    public static MainUIState disposableEmailerSafe(){
        return new MainUIState(State.DISPOSABLE_EMAILER_SAFE);
    }

    public static MainUIState disposableEmailerDetected(){
        return new MainUIState(State.DISPOSABLE_EMAILER_DETECTED);
    }

    public static MainUIState oopScamSafe(){
        return new MainUIState(State.OOP_SCAM_SAFE);
    }

    public static MainUIState oopScamDetected(){
        return new MainUIState(State.OOP_SCAM_DETECTED);
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

    public String getExtractedText() {
        return concatenatedText;
    }
}


