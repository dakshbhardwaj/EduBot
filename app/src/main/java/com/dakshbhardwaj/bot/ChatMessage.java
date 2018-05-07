package com.dakshbhardwaj.bot;

/**
 * Created by daksh on 5/1/18.
 */

public class ChatMessage {

    private String msgText;
    private String msgUser;

    public ChatMessage(String msgText, String msgUser) {
        this.msgText = msgText;
        this.msgUser = msgUser;
    }

    public ChatMessage()
    {

    }


    public String getMsgText() {
        return msgText;
    }

    public void setMsgText(String msgText) {
        this.msgText = msgText;
    }

    public String getMsgUser() {
        return msgUser;
    }

    public void setMsgUser(String msgUser) {
        this.msgUser = msgUser;
    }
}
