package com.tjtechy.system;

public class Result {
    private String message;
    private boolean flag;
    private Object data;
    private Integer code;

    public Result(){

    }
    public Result(String message, boolean flag, Integer code) {
        this.message = message;
        this.flag = flag;
        this.code = code;
    }
    public Result(String message, boolean flag, Object data, Integer code) {
        this.message = message;
        this.flag = flag;
        this.data = data;
        this.code = code;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isFlag() {
        return flag;
    }
    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public Object getData() {
        return data;
    }
    public void setData(Object data) {
        this.data = data;
    }

    public Integer getCode() {
        return code;
    }
    public void setCode(Integer code) {
        this.code = code;
    }
}
