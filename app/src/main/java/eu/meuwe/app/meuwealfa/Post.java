package eu.meuwe.app.meuwealfa;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class Post {
    private String user;
    private double latitude;
    private double longitude;
    private String text;
    private String imageUrl;
    private Date time;
    private List<Message> Messages;

    public Post(String user, double latitude, double longitude, String text, String imageUrl) {
        this.user = user;
        this.latitude = latitude;
        this.longitude = longitude;
        this.text = text;
        this.imageUrl = imageUrl;
        this.time = GregorianCalendar.getInstance().getTime();
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public List<Message> getMessages() {
        return Messages;
    }

    public void setMessages(List<Message> messages) {
        Messages = messages;
    }

    public void addMessage (Message newMessage)
    {
        Messages.add(newMessage);
    }
    public void addMessage (String text, String user)
    {
        Message newMessage = new Message (text,user);
        Messages.add(newMessage);
    }
}
