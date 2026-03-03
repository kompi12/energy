package com.example.energy.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnergyResponse<T> {

    private int status;
    private String message;
    private T data;
    private boolean success;

    public EnergyResponse() {}

    public EnergyResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public EnergyResponse(boolean success, int status, String message, T data) {
        this.success = success;
        this.status = status;
        this.message = message;
        this.data = data;
    }


    public static <T> EnergyResponse<T> success(T data) {
        return new EnergyResponse<>(200, "Success", data);
    }

    public static <T> EnergyResponse<T> success(String message, T data) {
        return new EnergyResponse<>(200, message, data);
    }

    public static <T> EnergyResponse<T> error(int status, String message) {
        return new EnergyResponse<>(status, message, null);
    }



    public boolean isSuccess() { return success; }
    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public T getData() { return data; }

    public void setSuccess(boolean success) { this.success = success; }
    public void setStatus(int status) { this.status = status; }
    public void setMessage(String message) { this.message = message; }
    public void setData(T data) { this.data = data; }

}
