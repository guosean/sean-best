package com.sean.http;

import com.sean.exception.APIConnectionException;
import com.sean.exception.APIRequestException;
import com.sean.network.http.ApacheHttpClient;
import com.sean.network.http.ClientConfig;
import com.sean.network.http.NettyHttpClient;
import com.sean.network.http.ResponseWrapper;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class TestHttp {

    ClientConfig clientConfig;
    String url;

    @Before
    public void before(){
        clientConfig = ClientConfig.newBuilder().connectionMaxPerRoute(100).build();
        url = "http://localhost:8080/jpush/withSdk";

    }

    @Test
    public void testApache(){
        ApacheHttpClient httpClient = new ApacheHttpClient(clientConfig);
        String url = "http://localhost:8080/http/";
        Map<String, String> params = new HashMap<String, String>();
        params.put("name", "sean");
        String response = httpClient.sendPost(url, params);
        System.out.println(response);
        String jsonParam = "{\"title\":\"title\",\"token\":\"token\"}";
        String result = httpClient.sendPost("http://localhost:8080/jpush/withSdk",jsonParam);
        System.out.println(result);
        httpClient.sendPost(url,jsonParam,String.class);
    }

    @Test
    public void testNetty(){
        String authCode = "";

        String jsonParam = "{\"name\":\"sean\"}";
        String url = "http://localhost:8080/jpush/netty";

        NettyHttpClient nettyHttpClient = new NettyHttpClient(authCode,null,clientConfig);
        try {
            ResponseWrapper wrapper = nettyHttpClient.sendPost(url,jsonParam);
            System.out.println(wrapper.responseContent);
        } catch (APIConnectionException e) {
            e.printStackTrace();
        } catch (APIRequestException e) {
            e.printStackTrace();
        }
    }

}
