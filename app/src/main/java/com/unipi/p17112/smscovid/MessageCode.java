package com.unipi.p17112.smscovid;

public class MessageCode {
    int id;
    String subtitle;

    public MessageCode(int title, String subtitle){
        this.id = title;
        this.subtitle = subtitle;
    }
    public MessageCode(){

    }

    public int getId() {
        return id;
    }

    public String getSubtitle() {
        return subtitle;
    }
}
