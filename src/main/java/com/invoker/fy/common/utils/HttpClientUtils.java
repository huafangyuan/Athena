package com.alitrip.traveluac.util;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;

import com.alibaba.fastjson.JSON;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description: HttpClient 工具类  基于HttpClient 4.x
 *
 * @author huafangyuan
 * @date 2017/5/8
 */
public class HttpClientUtils {

    public static final String MIME_JSON = "application/json";

    public static final String MIME_XML = "application/xml";

    private static final int MAX_CONNECTIONS = 50;

    private static final int DEFAULT_CONNECT_TIMEOUT = 3000;

    private static final int DEFAULT_SOCKET_TIMEOUT = 15000;

    private final static String DEFAULT_ENCODING = "UTF-8";

    private static HttpClient client = null;

    private static final Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);

    static {
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                //信任所有
                @Override
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            }).build();
            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext);
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE).register("https", sslConnectionSocketFactory)
                .build();
            PoolingHttpClientConnectionManager httpConnectionManager = new PoolingHttpClientConnectionManager(
                socketFactoryRegistry);
            httpConnectionManager.setDefaultMaxPerRoute(MAX_CONNECTIONS);
            httpConnectionManager.setMaxTotal(MAX_CONNECTIONS * 2);
            client = HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory).setConnectionManager(
                httpConnectionManager)
                .build();
        } catch (Exception e) {
            logger.error("Init http client error!", e);
        }
    }

    /**
     * 发送一个 GET 请求，响应编码格式UTF-8
     *
     * @param url 请求地址
     * @return
     * @throws Exception
     */
    public static String doGet(String url) throws Exception {

        return executeGet(url, DEFAULT_ENCODING);

    }

    /**
     * 发送一个 GET 请求
     *
     * @param url     请求地址
     * @param charset 响应编码格式，默认UTF-8
     * @return
     * @throws Exception
     */
    public static String executeGet(String url, String charset) throws Exception {
        logger.warn("requestUrl=" + url + ";charset=" + charset);
        long startTime = System.currentTimeMillis();
        HttpGet method = new HttpGet(url);
        String result = null;
        try {
            RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(DEFAULT_CONNECT_TIMEOUT)
                .setSocketTimeout(DEFAULT_SOCKET_TIMEOUT).build();
            method.setConfig(requestConfig);
            result = client.execute(method, new ResponseHandlerImpl(charset));
        } finally {
            method.releaseConnection();
        }
        logger.warn("响应用时" + (System.currentTimeMillis() - startTime) + " requestURL:" + url + " result:" + result);
        return result;
    }

    /**
     * 发送一个 POST 请求，响应编码格式为UTF-8
     *
     * @param url     请求地址
     * @param headers 请求头部
     * @param params  请求参数
     * @return
     * @throws Exception
     */
    public static String doPost(String url, Map<String, Object> headers, Map<String, Object> params) throws Exception {

        return executePost(url, headers, params, DEFAULT_ENCODING);

    }

    /**
     * 发送一个 POST 请求
     *
     * @param url     请求地址
     * @param headers 请求头部
     * @param params  请求参数
     * @param charset 响应编码格式，默认UTF-8
     * @return
     * @throws Exception
     */
    public static String executePost(String url, Map<String, Object> headers, Map<String, Object> params,
                                     String charset) throws Exception {
        logger.warn(
            "requestUrl=" + url + ";header=" + JSON.toJSONString(headers) + ";params=" + JSON.toJSONString(params)
                + ";charset=" + charset);
        long startTime = System.currentTimeMillis();
        HttpPost method = new HttpPost(url);
        String result = null;
        try {
            RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(DEFAULT_CONNECT_TIMEOUT)
                .setSocketTimeout(DEFAULT_SOCKET_TIMEOUT).build();
            method.setConfig(requestConfig);
            //设置header
            initHeader(headers, method);
            //设置请求参数
            List<NameValuePair> nvps = Lists.newArrayList();
            if (MapUtils.isNotEmpty(params)) {
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if (null != key && null != value) {
                        nvps.add(new BasicNameValuePair(key, value.toString()));
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(nvps)) {
                method.setEntity(new UrlEncodedFormEntity(nvps, DEFAULT_ENCODING));
            }
            result = client.execute(method, new ResponseHandlerImpl(charset));
        } finally {
            method.releaseConnection();
        }
        logger.warn("响应用时" + (System.currentTimeMillis() - startTime) + " requestURL:" + url + " result:" + result);
        return result;
    }

    /**
     * 发送一个 POST 请求，CONTENT_TYPE = "application/json"，响应编码格式为UTF-8
     *
     * @param url         请求地址
     * @param headers     请求头部
     * @param requestBody 请求参数
     * @return
     * @throws Exception
     */
    public static String doPost(String url, Map<String, Object> headers, String requestBody) throws Exception {

        return executePost(url, headers, requestBody, HttpClientUtils.MIME_JSON, DEFAULT_ENCODING);
    }

    /**
     * 发送一个 XML POST 请求，CONTENT_TYPE = "application/xml"，响应编码格式为UTF-8
     *
     * @param url         请求地址
     * @param headers     请求头部
     * @param requestBody 请求参数
     * @return
     * @throws Exception
     */
    public static String doPostXml(String url, Map<String, Object> headers, String requestBody) throws Exception {

        return executePost(url, headers, requestBody, HttpClientUtils.MIME_XML, DEFAULT_ENCODING);
    }

    /**
     * 发送一个 POST 请求
     *
     * @param url         请求地址
     * @param headers     请求头部
     * @param requestBody 请求参数
     * @param mimeType    表单提交编码格式
     * @param charset     响应编码格式，默认UTF-8
     * @return
     * @throws Exception
     */
    public static String executePost(String url, Map<String, Object> headers, String requestBody, String mimeType,
                                     String charset) throws Exception {
        logger.warn(
            "requestUrl=" + url + ";headers=" + JSON.toJSONString(headers) + ";params=" + requestBody + ";charset="
                + charset);
        long startTime = System.currentTimeMillis();
        HttpPost method = new HttpPost(url);
        String result = null;
        try {
            RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(DEFAULT_CONNECT_TIMEOUT)
                .setSocketTimeout(DEFAULT_SOCKET_TIMEOUT).build();
            method.setConfig(requestConfig);
            //设置header
            initHeader(headers, method);
            StringEntity stringEntity = new StringEntity(requestBody, ContentType.create(
                mimeType, "UTF-8"));
            method.setEntity(stringEntity);
            result = client.execute(method, new ResponseHandlerImpl(charset));
        } finally {
            method.releaseConnection();
        }
        logger.warn("响应用时" + (System.currentTimeMillis() - startTime) + " requestURL:" + url + " result:" + result);
        return result;
    }

    /**
     * 设置请求头部
     *
     * @param headers
     * @param method
     */
    private static void initHeader(Map<String, Object> headers, HttpPost method) {
        if (MapUtils.isNotEmpty(headers)) {
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (null != key && null != value) {
                    method.addHeader(key, value.toString());
                }
            }
        }
    }

    private static class ResponseHandlerImpl implements ResponseHandler<String> {

        private String charset;

        private static final int STATUS_CODE = 300;

        public ResponseHandlerImpl(String charset) {

            this.charset = charset;
        }

        @Override
        public String handleResponse(HttpResponse httpResponse) throws IOException {
            StatusLine statusLine = httpResponse.getStatusLine();
            if (statusLine.getStatusCode() >= STATUS_CODE) {
                logger.error("response Error code" + statusLine.getStatusCode());
                throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
            }
            HttpEntity entity = httpResponse.getEntity();
            if (null != entity) {
                if (StringUtils.isNotEmpty(charset)) {
                    return EntityUtils.toString(entity, charset);
                } else {
                    return EntityUtils.toString(entity, DEFAULT_ENCODING);
                }
            } else {
                return "";
            }
        }

        public String getCharset() {
            return charset;
        }

        public void setCharset(String charset) {
            this.charset = charset;
        }
    }

}
