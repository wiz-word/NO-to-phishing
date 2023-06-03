package com.amplitudehack.antiscamchecker.data.model;

public enum GptModels {
    GPT_THREE_POINT_FIVE_TURBO("gpt-3.5-turbo"),
    GPT_FOUR("gpt-4"); //Not available to public API yet

    private final String name;

    GptModels(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
