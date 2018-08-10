package com.jjstudio.jjtank.model;

public enum StatusEnum {
    Connected(1),Disconnected(0);
    private int status;
     StatusEnum(int status){
        this.status = status;
    }
}
