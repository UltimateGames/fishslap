package com.greatmancode.fishslap;

import me.ampayne2.ultimategames.api.message.Message;

public enum FSMessage implements Message {
    KILLING_SPREE("KillingSpree", "&b%s is on a Killing Spree!"),
    RAMPAGE("Rampage", "&b%s is on a Rampage!"),
    DOMINATION("Domination", "&b%s is Dominating!"),
    UNSTOPPABLE("Unstoppable", "&b%s is Unstoppable!"),
    GOD("God", "&b%s is Godlike!"),
    SHUTDOWN("Shutdown", "&b%s was Shut Down!");

    private String message;
    private final String path;
    private final String defaultMessage;

    private FSMessage(String path, String defaultMessage) {
        this.path = path;
        this.defaultMessage = defaultMessage;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getDefault() {
        return defaultMessage;
    }

    @Override
    public String toString() {
        return message;
    }
}
