package com.amplitudehack.antiscamchecker.data.model;

import java.util.List;

public class ChatResponse {
    private String id;
    private String object;
    private long created;
    private List<ChatChoice> choices;
    private Usage usage;

    public String getId() {
        return id;
    }

    public String getObject() {
        return object;
    }

    public long getCreated() {
        return created;
    }

    public List<ChatChoice> getChoices() {
        return choices;
    }

    public Usage getUsage() {
        return usage;
    }

    public static class ChatChoice {
        private int index;
        private Message message;
        private String finish_reason;

        public int getIndex() {
            return index;
        }

        public Message getMessage() {
            return message;
        }

        public String getFinishReason() {
            return finish_reason;
        }
    }

    public static class Message {
        private String role;
        private String content;

        public String getRole() {
            return role;
        }

        public String getContent() {
            return content;
        }
    }

    public static class Usage {
        private int prompt_tokens;
        private int completion_tokens;
        private int total_tokens;

        public int getPromptTokens() {
            return prompt_tokens;
        }

        public int getCompletionTokens() {
            return completion_tokens;
        }

        public int getTotalTokens() {
            return total_tokens;
        }
    }

    @Override
    public String toString() {
        return "ChatResponse{" +
                "id='" + id + '\'' +
                ", object='" + object + '\'' +
                ", created=" + created +
                ", choices=" + choices +
                ", usage=" + usage +
                '}';
    }
}




