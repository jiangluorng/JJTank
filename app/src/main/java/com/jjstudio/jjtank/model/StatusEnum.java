package com.jjstudio.jjtank.model;

public enum StatusEnum {
    Connected(1),Disconnected(0),LastConnected(2);
    private int status;
     StatusEnum(int status){
        this.status = status;
    }

    public static StatusEnum fromValue(int status){
         if (status==1){
             return Connected;
         }else if (status==2){
             return Connected;
         }else {
             return Disconnected;
         }
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
