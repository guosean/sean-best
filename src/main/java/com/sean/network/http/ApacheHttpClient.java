package com.sean.network.http;

import com.google.common.base.Preconditions;
import com.sean.util.JsonMapper;
import org.apache.commons.lang3.StringUtils;
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
            if(StringUtils.isNoneBlank(_authCode)){
                httpPost.setHeader(HttpHeaders.AUTHORIZATION, _authCode);
            }
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

    public <T,R> R sendPost(String url, T input, Class<R> responseType) {
        Preconditions.checkArgument(StringUtils.isNotBlank(url),"url cannot be blank");
        Preconditions.checkNotNull(input,"input cannot be null");
        Preconditions.checkNotNull(responseType,"response type cannot be null");

        String params = mapper.toJson(input);
        String response = sendPost(url,params);

        if(StringUtils.isBlank(response)){
            return null;
        }

        return mapper.fromJson(response,responseType);
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

    static JsonMapper mapper = JsonMapper.buildNormal();

}
