package com.sigma.lib;

public class ApiResponse {
    private boolean ok;
    private String message;

    public ApiResponse(boolean ok, String message) {
        this.ok = ok;
        this.message = message;
    }

    public boolean isOk() {
        return ok;
    }

    public String getMessage() {
        return message;
    }
}