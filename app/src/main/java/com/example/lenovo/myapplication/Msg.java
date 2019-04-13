package com.example.lenovo.myapplication;

public class Msg {
    public String username; //发给谁的
    public String msgbody;
    public String date;
    public String fromOrTo;

    public Msg(String to, String msgbody, String date, String from) {
        this.username = to;
        this.msgbody = msgbody;
        this.date = date;
        this.fromOrTo = from;
    }
}
