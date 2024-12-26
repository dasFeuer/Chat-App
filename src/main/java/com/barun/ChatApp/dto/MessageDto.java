package com.barun.ChatApp.dto;

public class MessageDto {
    private Long id;
    private String sender;
    private String receiver;
    private String content;
    private String action;

    public MessageDto() {
    }

    public MessageDto(String sender, String receiver, String content) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
    }

    public MessageDto(Long id, String sender, String receiver, String content, String action) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.action = action;
    }


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