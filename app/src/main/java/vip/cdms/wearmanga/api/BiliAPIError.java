package vip.cdms.wearmanga.api;

import androidx.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

public class BiliAPIError extends Exception {
    private final int code;
    private final String message;

    public BiliAPIError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public String getMessage() {
        return message;
    }

    @NotNull
    @Override
    public String toString() {
        return this.getClass().getName() + ": code " + code + ", message " + message;
    }
}
