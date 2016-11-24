/*
 * Copyright 2016 Focus Technology, Co., Ltd. All rights reserved.
 */
package com.invoker.fy.common.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.springframework.util.StringUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

/**
 * HttpClient 工具类
 * @author huafangyuan
 * @date 2016/11/24
 */
public class HttpClientUtils {

	public static final Log logger = LogFactory.getLog(HttpClientUtils.class);

	private static class HttpClientHolder {
		private static HttpClientUtils httpClientUtils = new HttpClientUtils();
	}

	public static HttpClientUtils getInstance() {
		return HttpClientHolder.httpClientUtils;
	}

	// 私密连接工厂
	private static SSLConnectionSocketFactory	socketFactory;

	// 重写验证方法，取消检测SSL
	private static TrustManager					manager	= new X509TrustManager() {
															@Override
															public void checkClientTrusted(
																	X509Certificate[] x509Certificates, String s)
																	throws CertificateException {

															}

															@Override
															public void checkServerTrusted(
																	X509Certificate[] x509Certificates, String s)
																	throws CertificateException {

															}

															@Override
															public X509Certificate[] getAcceptedIssuers() {
																return null;
															}
														};

	/**
	 * http get
	 * @param url 请求地址
	 * @param cookie cookie
	 * @param refer 跳转地址
	 * @param charset 响应编码格式
	 * @return 响应对象
	 */
	public static String executeGet(String url, String cookie, String refer, String charset) {
		CloseableHttpClient httpClient = createHttpClient();
		if (null != httpClient) {
			try {
				HttpGet httpget = new HttpGet(new URI(url));
				if (null != cookie) {
					httpget.setHeader("Cookie", cookie);
				}
				if (null != refer) {
					httpget.setHeader("Referer", refer);
				}
				String html = httpClient.execute(httpget, new ResponseHandlerImpl(charset));
				return html;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					httpClient.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return "";
	}

	/**
	 * http post
	 * @param url 请求地址
	 * @param values 请求参数
	 * @param cookie cookie
	 * @param refer 跳转地址
	 * @param charset 响应编码格式
	 * @return 响应对象
	 */
	public static String executePost(String url, List<NameValuePair> values, String cookie, String refer,
			String charset) {
		CloseableHttpClient httpClient = createHttpClient();
		if (null != httpClient) {
			try {
				HttpPost httppost = new HttpPost(new URI(url));
				if (null != cookie) {
					httppost.setHeader("Cookie", cookie);
				}
				if (null != refer) {
					httppost.setHeader("Referer", refer);
				}
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(values, Consts.UTF_8);
				httppost.setEntity(entity);
				String html = httpClient.execute(httppost, new ResponseHandlerImpl(charset));
				return html;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					httpClient.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return "";
	}

	/**
	 * xml http post
	 * @param url 请求地址
	 * @param args 请求参数
	 * @param charset 响应编码格式
	 * @return 响应对象
	 */
	public static String executePostXml(String url, String args, String charset) {
		CloseableHttpClient httpClient = createHttpClient();
		if (null != httpClient) {
			try {
				HttpPost httppost = new HttpPost(new URI(url));
				StringEntity xml = new StringEntity(args, "GBK");
				httppost.setEntity(xml);
				httppost.setHeader("Content-Type", "application/xml;charset=GBK");
				String html = httpClient.execute(httppost, new ResponseHandlerImpl(charset));
				return html;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					httpClient.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return "";

	}

	private static class ResponseHandlerImpl implements ResponseHandler<String> {

		private String charset;

		public ResponseHandlerImpl(String charset) {
			this.charset = charset;
		}

		@Override
		public String handleResponse(HttpResponse httpResponse) throws IOException {
			StatusLine statusLine = httpResponse.getStatusLine();
			if (statusLine.getStatusCode() >= 300) {
				throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
			}
			HttpEntity entity = httpResponse.getEntity();
			if (null != entity) {
				if (!StringUtils.isEmpty(charset)) {
					return EntityUtils.toString(entity, charset);
				} else {
					return EntityUtils.toString(entity);
				}
			} else {
				return "";
			}
		}
	}

	public static CloseableHttpClient createHttpClient() {
		try {
			enableSSL();
			RequestConfig defaultRequestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD_STRICT)
					.setExpectContinueEnabled(true).setSocketTimeout(180 * 1000).setConnectTimeout(180 * 1000)
					.setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM, AuthSchemes.DIGEST))
					.setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC)).build();
			Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create()
					.register("http", PlainConnectionSocketFactory.INSTANCE).register("https", socketFactory).build();
			PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(
					socketFactoryRegistry);
			// 最大连接数
			connectionManager.setMaxTotal(10);
			// 每个路由基础连接
			connectionManager.setDefaultMaxPerRoute(2);
			// 请求重试处理
			HttpRequestRetryHandler httpRequestRetryHandler = new HttpRequestRetryHandler() {
				@Override
				public boolean retryRequest(IOException exception, int executionCount, HttpContext httpContext) {
					if (executionCount >= 3) {// 如果已经重试了3次，就放弃
						logger.info("如果已经重试了3次，就放弃");
						return false;
					}
					if (exception instanceof NoHttpResponseException) {// 如果丢掉服务器，就重试
						logger.info("如果丢掉服务器，就重试");
						return true;
					}
					if (exception instanceof SSLHandshakeException) {// 不要重试SSL握手异常
						logger.info("不要重试SSL握手异常");
						return false;
					}
					if (exception instanceof InterruptedIOException) {// 超时
						logger.info("超时");
						return false;
					}
					if (exception instanceof UnknownHostException) {// 目标服务器不可达
						logger.info("目标服务器不可达");
						return false;
					}
					if (exception instanceof ConnectTimeoutException) {// 连接被拒绝
						logger.info(" 连接被拒绝");
						return false;
					}
					if (exception instanceof SSLException) {// ssl握手异常
						logger.info("ssl握手异常");
						return false;
					}
					HttpClientContext clientContext = HttpClientContext.adapt(httpContext);
					HttpRequest request = clientContext.getRequest();
					// 如果请求是幂等的，就再次尝试
					if (!(request instanceof HttpEntityEnclosingRequest)) {
						return true;
					}
					return false;
				}
			};
			CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(connectionManager)
					.setRetryHandler(httpRequestRetryHandler).setDefaultRequestConfig(defaultRequestConfig).build();
			return httpClient;
		} catch (Exception e) {
			logger.info("获取httpClient失败" + e.getMessage());
			return null;
		}
	}

	/**
	 * 调用SSL
	 */
	private static void enableSSL() {
		try {
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, new TrustManager[] { manager }, null);
			socketFactory = new SSLConnectionSocketFactory(context, NoopHostnameVerifier.INSTANCE);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}

	}

}
