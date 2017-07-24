package com.sean.network.http;

public class ClientConfig {

    private String sslVersion;
    private int maxRetryTimes;
    private int readTimeout;
    private int connectionRequestTimeout;
    private int connectionTimeout;
    private int socketTimeout;
    private int connectionMaxTotal;
    private int connectionMaxRoute;
    private int connectionMaxPerRoute;


    private ClientConfig(Builder builder) {
        setSslVersion(builder.sslVersion);
        setMaxRetryTimes(builder.maxRetryTimes);
        setReadTimeout(builder.readTimeout);
        setConnectionRequestTimeout(builder.connectionRequestTimeout);
        setConnectionTimeout(builder.connectionTimeout);
        setSocketTimeout(builder.socketTimeout);
        setConnectionMaxTotal(builder.connectionMaxTotal);
        setConnectionMaxRoute(builder.connectionMaxRoute);
        setConnectionMaxPerRoute(builder.connectionMaxPerRoute);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getSslVersion() {
        return sslVersion;
    }

    public void setSslVersion(String sslVersion) {
        this.sslVersion = sslVersion;
    }

    public int getMaxRetryTimes() {
        return maxRetryTimes;
    }

    public void setMaxRetryTimes(int maxRetryTimes) {
        this.maxRetryTimes = maxRetryTimes;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getConnectionRequestTimeout() {
        return connectionRequestTimeout;
    }

    public void setConnectionRequestTimeout(int connectionRequestTimeout) {
        this.connectionRequestTimeout = connectionRequestTimeout;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public int getConnectionMaxTotal() {
        return connectionMaxTotal;
    }

    public void setConnectionMaxTotal(int connectionMaxTotal) {
        this.connectionMaxTotal = connectionMaxTotal;
    }

    public int getConnectionMaxRoute() {
        return connectionMaxRoute;
    }

    public void setConnectionMaxRoute(int connectionMaxRoute) {
        this.connectionMaxRoute = connectionMaxRoute;
    }

    public int getConnectionMaxPerRoute() {
        return connectionMaxPerRoute;
    }

    public void setConnectionMaxPerRoute(int connectionMaxPerRoute) {
        this.connectionMaxPerRoute = connectionMaxPerRoute;
    }

    public String getSSLVersion() {
        return this.sslVersion;
    }

    public static final class Builder {

        private String sslVersion = "TLS";
        private int maxRetryTimes = 3;
        private int readTimeout = 30 * 1000;
        private int connectionRequestTimeout = 10 * 1000;
        private int connectionTimeout = 5 * 1000;
        private int socketTimeout = 10 * 1000;
        private int connectionMaxTotal = 200;
        private int connectionMaxRoute = 100;
        private int connectionMaxPerRoute = 40;

        private Builder() {
        }

        public Builder sslVersion(String val) {
            sslVersion = val;
            return this;
        }

        public Builder maxRetryTimes(int val) {
            maxRetryTimes = val;
            return this;
        }

        public Builder readTimeout(int val) {
            readTimeout = val;
            return this;
        }

        public Builder connectionRequestTimeout(int val) {
            connectionRequestTimeout = val;
            return this;
        }

        public Builder connectionTimeout(int val) {
            connectionTimeout = val;
            return this;
        }

        public Builder socketTimeout(int val) {
            socketTimeout = val;
            return this;
        }

        public Builder connectionMaxTotal(int val) {
            connectionMaxTotal = val;
            return this;
        }

        public Builder connectionMaxRoute(int val) {
            connectionMaxRoute = val;
            return this;
        }

        public Builder connectionMaxPerRoute(int val) {
            connectionMaxPerRoute = val;
            return this;
        }

        public ClientConfig build() {
            return new ClientConfig(this);
        }
    }
}
