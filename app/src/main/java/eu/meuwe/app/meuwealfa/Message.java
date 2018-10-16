package eu.meuwe.app.meuwealfa;

import java.util.Date;

public class Message {
    private String Text;
    private String User;
    private long Time;

    public Message(String text, String user) {
        Text = text;
        User = user;

        Time = new Date().getTime();
    }

    public Message() {
        Text = "";
        User = "";
        Time = new Date().getTime();
    }

    public String getText() {
        return Text;
    }

    public void setText(String text) {
        Text = text;
    }

    public String getUser() {
        return User;
    }

    public void setUser(String user) {
        User = user;
    }

    public long getTime() {
        return Time;
    }

    public void setTime(long time) {
        Time = time;
    }
}
