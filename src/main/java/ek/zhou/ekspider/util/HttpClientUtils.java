package ek.zhou.ekspider.util;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

import java.nio.charset.Charset;

public class HttpClientUtils {
    public String getRedirectUri(String uri) {
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;
        HttpGet httpGet = null;
        try {
            HttpContext httpContext = new BasicHttpContext();
            RequestConfig defaultRequestConfig = RequestConfig.custom().setSocketTimeout(10000).build();
            httpclient = HttpClients.custom()
                    .setDefaultConnectionConfig(ConnectionConfig.custom().setCharset(Charset.forName("UTF-8")).build())
                    .build();
            httpGet = new HttpGet(uri);
            httpGet.setConfig(defaultRequestConfig);
            response = httpclient.execute(httpGet, httpContext);
            HttpHost currentHost = (HttpHost) httpContext
                    .getAttribute(ExecutionContext.HTTP_TARGET_HOST);
            HttpUriRequest req = (HttpUriRequest) httpContext
                    .getAttribute(ExecutionContext.HTTP_REQUEST);
            return (req.getURI().isAbsolute()) ? req.getURI()
                    .toString() : (currentHost.toURI() + req.getURI());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
