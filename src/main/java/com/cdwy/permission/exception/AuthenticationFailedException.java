package com.cdwy.permission.exception;

public class AuthenticationFailedException extends RuntimeException {

    public AuthenticationFailedException() {
        super("用户名或密码错误");
    }
}
