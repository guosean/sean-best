package com.sean.network.http;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.Proxy;

public class HttpProxy {
    private static final Logger LOG = LoggerFactory.getLogger(HttpProxy.class);

    private String host;
    private int port;
    private String username;
    private String password;

    private boolean authenticationNeeded = false;

    public HttpProxy(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public HttpProxy(String host, int port, String username, String password) {
        this(host, port);

        Preconditions.checkArgument(! (null == username), "username should not be null");
        Preconditions.checkArgument(! (null == password), "password should not be null");

        this.username = username;
        this.password = password;
        authenticationNeeded = true;

        LOG.info("Http Proxy - host:" + host + ", port:" + port
                + ", username:" + username + ", password:" + password);
    }


    public Proxy getNetProxy() {
        return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
    }

    public boolean isAuthenticationNeeded() {
        return this.authenticationNeeded;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }
}
