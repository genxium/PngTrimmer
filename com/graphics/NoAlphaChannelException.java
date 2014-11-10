package com.graphics;

public class NoAlphaChannelException extends Exception {
    public NoAlphaChannelException() {
        super("Unable to crop images containing no alpha channel");
    }
}
