package com.sean.network.http;

import org.apache.http.*;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 连接池提升效率
 * Created by guozhenbin on 2017/7/18.
 */
public class ApacheHttpClient {

    private static Logger LOG = LoggerFactory.getLogger(ApacheHttpClient.class);

    private static CloseableHttpClient _httpClient = null;
    private final static Object syncLock = new Object();
    private String _authCode;
    private HttpRequestRetryHandler requestRetryHandler;
    private ClientConfig config = ClientConfig.newBuilder().build();

    public ApacheHttpClient(String authCode, ClientConfig config) {
        this(config);
        _authCode = authCode;
    }

    public ApacheHttpClient(ClientConfig config) {
        this.config = config;
    }

    public ApacheHttpClient(ClientConfig config, HttpRequestRetryHandler requestRetryHandler) {
        this(config);
        this.requestRetryHandler = requestRetryHandler;
    }

    private void configHttpRequest(HttpRequestBase httpRequestBase) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(config.getConnectionRequestTimeout())
                .setConnectTimeout(config.getConnectionTimeout())
                .setSocketTimeout(config.getSocketTimeout())
                .build();

        httpRequestBase.setConfig(requestConfig);
    }

    public CloseableHttpClient getHttpClient(String url) {
        String hostname = url.split("/")[2];
        int port = 80;
        if (hostname.contains(":")) {
            String[] arr = hostname.split(":");
            hostname = arr[0];
            port = Integer.parseInt(arr[1]);
        }
        if (_httpClient == null) {
            synchronized (syncLock) {
                if (_httpClient == null) {
                    _httpClient = createHttpClient(config.getConnectionMaxTotal(), config.getConnectionMaxPerRoute(), config.getConnectionMaxRoute(), hostname, port);
                }
            }
        }
        return _httpClient;

    }

    public CloseableHttpClient createHttpClient(int maxTotal, int maxPerRoute, int maxRoute,
                                                String hostname, int port) {
        ConnectionSocketFactory plainsf = PlainConnectionSocketFactory
                .getSocketFactory();
        LayeredConnectionSocketFactory sslsf = SSLConnectionSocketFactory
                .getSocketFactory();
        Registry<ConnectionSocketFactory> registry = RegistryBuilder
                .<ConnectionSocketFactory>create().register("http", plainsf)
                .register("https", sslsf).build();
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(
                registry);
        // 将最大连接数增加
        cm.setMaxTotal(maxTotal);
        // 将每个路由基础的连接增加
        cm.setDefaultMaxPerRoute(maxPerRoute);
        HttpHost httpHost = new HttpHost(hostname, port);
        // 将目标主机的最大连接数增加
        cm.setMaxPerRoute(new HttpRoute(httpHost), maxRoute);

        // 请求重试处理
        if (null == requestRetryHandler) {
            requestRetryHandler = new HttpRequestRetryHandler() {
                public boolean retryRequest(IOException exception,
                                            int executionCount, HttpContext context) {
                    if (executionCount >= config.getMaxRetryTimes()) {
                        return false;
                    }
                    if (exception instanceof NoHttpResponseException) {// 如果服务器丢掉了连接，那么就重试
                        return true;
                    }
                    if (exception instanceof SSLHandshakeException) {// 不要重试SSL握手异常
                        return false;
                    }
                    if (exception instanceof InterruptedIOException) {// 超时
                        return false;
                    }
                    if (exception instanceof UnknownHostException) {// 目标服务器不可达
                        return false;
                    }
                    if (exception instanceof ConnectTimeoutException) {// 连接被拒绝
                        return false;
                    }
                    if (exception instanceof SSLException) {// SSL握手异常
                        return false;
                    }

                    HttpClientContext clientContext = HttpClientContext
                            .adapt(context);
                    HttpRequest request = clientContext.getRequest();
                    // 如果请求是幂等的，就再次尝试
                    if (!(request instanceof HttpEntityEnclosingRequest)) {
                        return true;
                    }
                    return false;
                }
            };
        }

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .setRetryHandler(requestRetryHandler).build();

        return httpClient;
    }

    public String sendGet(String url) {
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader(HttpHeaders.AUTHORIZATION, _authCode);

        return sendRequest(url, httpGet);
    }

    public String sendGet(String url, String content) {
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader(HttpHeaders.AUTHORIZATION, _authCode);

        return sendRequest(url, httpGet);
    }

    public String sendDelete(String url) {
        HttpDelete httpDelete = new HttpDelete(url);
        httpDelete.setHeader(HttpHeaders.AUTHORIZATION, _authCode);

        return sendRequest(url, httpDelete);
    }

    public String sendDelete(String url, String content) {
        HttpDelete httpDelete = new HttpDelete(url);
        httpDelete.setHeader(HttpHeaders.AUTHORIZATION, _authCode);

        return sendRequest(url, httpDelete);
    }

    public String sendPost(String url, String content) {
        String result = null;
        try {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader(HttpHeaders.AUTHORIZATION, _authCode);
            StringEntity params = new StringEntity(content);
            httpPost.setEntity(params);
            result = sendRequest(url, httpPost);
        } catch (IOException e) {
            LOG.error("sendPost error", e);
        }
        return result;
    }

    public String sendPost(String url, final Map<String, String> params) {
        String result = "";
        List<NameValuePair> httpParams = new ArrayList<NameValuePair>();
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                httpParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
        }
        HttpPost httpPost = new HttpPost(url);
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(httpParams, HTTP.UTF_8));
            result = sendRequest(url, httpPost);
        } catch (UnsupportedEncodingException e) {
            LOG.error("sendPost error", e);
        }

        return result;
    }

    public String sendPut(String url, String content) {
        String result = null;
        HttpPut httpPut = new HttpPut(url);
        httpPut.setHeader(HttpHeaders.AUTHORIZATION, _authCode);
        try {
            StringEntity params = new StringEntity(content);
            httpPut.setEntity(params);
            result = sendRequest(url, httpPut);
        } catch (UnsupportedEncodingException e) {
            LOG.error("sendPut error", e);
        }

        return result;
    }

    private String sendRequest(String url, final HttpRequestBase httpRequest) {
        String result = null;
        CloseableHttpResponse response = null;
        configHttpRequest(httpRequest);
        try {
            response = getHttpClient(url).execute(httpRequest, HttpClientContext.create());
            if (null != response) {
                result = processResponse(response);
            }
        } catch (IOException e) {
            LOG.error("http sendRequest error", e);
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                LOG.error("http sendRequest close error", e);
            }
        }
        return result;
    }

    public String processResponse(CloseableHttpResponse response) throws IOException {
        HttpEntity entity = response.getEntity();

        int status = response.getStatusLine().getStatusCode();
        String responseContent = EntityUtils.toString(entity, "utf-8");
        EntityUtils.consume(entity);
        if (status >= 200 && status < 300) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Succeed to get response OK - responseCode:" + status);
                LOG.debug("Response Content - " + responseContent);
            }
        } else if (status >= 300 && status < 400) {
            LOG.warn("Normal response but unexpected - responseCode:" + status + ", responseContent:" + responseContent);

        } else {
            LOG.warn("Got error response - responseCode:" + status + ", responseContent:" + responseContent);
        }

        return responseContent;
    }

    public static class ClientConfig {

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

    public static void main(String[] args) {
        ClientConfig config = ClientConfig.newBuilder().connectionMaxPerRoute(100).build();
        ApacheHttpClient httpClient = new ApacheHttpClient(config);
        String url = "http://localhost:8080/http/";
        Map<String, String> params = new HashMap<String, String>();
        params.put("name", "sean");
        String response = httpClient.sendPost(url, params);
        System.out.println(response);
        String jsonParam = "{\"title\":\"title\",\"token\":\"token\"}";
        String result = httpClient.sendPost("http://localhost:8080/jpush/withSdk",jsonParam);
        System.out.println(result);

    }

}
