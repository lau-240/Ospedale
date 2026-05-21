/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

/**
 *
 * @author Laura
 */

public class Response {
    private int status;
    private String message;
    private Object data;

    public Response(int status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public Response(int status, String message) {
        this.status = status;
        this.message = message;
        this.data = null;
    }

    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public Object getData() { return data; }

    public boolean isSuccess() { return status >= 200 && status < 300; }
}
