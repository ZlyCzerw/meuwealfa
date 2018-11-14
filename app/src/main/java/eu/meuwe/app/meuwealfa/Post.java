package eu.meuwe.app.meuwealfa;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Vector;

public class Post {
    private String uuid;
    private String user;
    private double latitude;
    private double longitude;
    private String text;
    private String title;
    private String imageUrl;
    private Date time;
    private List<Message> Messages;
    private List<String> tags;
    private int viewsCounter;


    public Post(String uuid, String user, double latitude, double longitude, String text, String imageUrl) {
        this.uuid = uuid;
        this.user = user;
        this.latitude = latitude;
        this.longitude = longitude;
        this.text = text;
        this.title = text.substring(0,Math.min(text.length(),12))+"...";
        this.imageUrl = imageUrl;
        this.time = GregorianCalendar.getInstance().getTime();
        this.Messages = new Vector<>();
        this.tags = new Vector<>();
        this.viewsCounter =0;
    }
    public Post(String uuid,String user, double latitude, double longitude, String text, String imageUrl,String title) {
        this(uuid,user,latitude,longitude,text,imageUrl);
        this.title = title;
        this.tags = new Vector<>();
   }

    public Post() {
        this("","none",0,0,"","","");
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

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return getTitle();
    }

    public int getViewsCounter() {
        return viewsCounter;
    }
    public void incrementViewsCounter(){
        viewsCounter++;
    }
}
