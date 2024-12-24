package com.barun.ChatApp.dto;

public class MessageDto {
    private Long id;
    private String sender;
    private String receiver;
    private String content;
    private String action;

    // Default constructor
    public MessageDto() {
    }

    // Constructor for new messages
    public MessageDto(String sender, String receiver, String content) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
    }

    // Constructor for updates/deletes with ID
    public MessageDto(Long id, String sender, String receiver, String content, String action) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.action = action;
    }

    // All getters and setters...
    // (keep your existing getters and setters)


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}