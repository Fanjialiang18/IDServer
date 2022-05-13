package com.clearlove3.idserver.domain;

/**
 * @author clearlove3
 * @date 2022/5/14 1:11
 */
public class Msg {
    private final int code;
    private final String msg;

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public Msg(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
