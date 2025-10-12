package com.example.energy.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnergyResponse<T> {

    private int status;
    private String message;
    private T data;

    public EnergyResponse() {}

    public EnergyResponse(int status, String message, T data) {
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


}
