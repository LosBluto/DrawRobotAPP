package com.example.myapplication.ui.http.protocol;

import com.google.gson.JsonObject;

public class CommonBaseJson<T>{

    private Integer code;
    private boolean success;
    private String message;
    private JsonObject data;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public boolean isSuccess() {
        return this.success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public JsonObject getData() {
        return this.data;
    }

    public void setData(JsonObject data) {
        this.data = data;
    }

}
