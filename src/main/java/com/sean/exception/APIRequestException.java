package com.sean.exception;

import com.sean.network.http.ResponseWrapper;

public class APIRequestException extends Exception{

    private ResponseWrapper wrapper;

    public APIRequestException(ResponseWrapper wrapper){
        this.wrapper = wrapper;
    }

}
