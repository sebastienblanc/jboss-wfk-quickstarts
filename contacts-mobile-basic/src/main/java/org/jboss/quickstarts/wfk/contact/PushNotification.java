package org.jboss.quickstarts.wfk.contact;

/**
 * Created by sebastien on 4/17/14.
 */
public class PushNotification {

    private String author;

    private String receiver;

    private String message;

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
