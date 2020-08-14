package ek.zhou.ekspider.spider;

import ek.zhou.ekspider.config.RedisConfig;
import ek.zhou.ekspider.util.RedisUtil;
import ek.zhou.ekspider.util.StoreUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.downloader.AbstractDownloader;
import us.codecraft.webmagic.downloader.HttpClientGenerator;
import us.codecraft.webmagic.downloader.HttpClientRequestContext;
import us.codecraft.webmagic.downloader.HttpUriRequestConverter;
import us.codecraft.webmagic.proxy.Proxy;
import us.codecraft.webmagic.proxy.ProxyProvider;
import us.codecraft.webmagic.selector.PlainText;
import us.codecraft.webmagic.utils.CharsetUtils;
import us.codecraft.webmagic.utils.HttpClientUtils;
import us.codecraft.webmagic.utils.HttpConstant;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;


/**
 * The http downloader based on HttpClient.
 *
 * @author code4crafter@gmail.com <br>
 * @since 0.1.0
 */
@ThreadSafe
@Component
public class MyDownloader extends AbstractDownloader {

    @Autowired
    RedisUtil redisUtil;
    private static final String keyPrifix= "ekspider:spiders:";
    private Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<String, CloseableHttpClient> httpClients = new HashMap<String, CloseableHttpClient>();

    private HttpClientGenerator httpClientGenerator = new HttpClientGenerator();

    private HttpUriRequestConverter httpUriRequestConverter = new HttpUriRequestConverter();

    private ProxyProvider proxyProvider;

    private boolean responseHeader = true;

    public void setHttpUriRequestConverter(HttpUriRequestConverter httpUriRequestConverter) {
        this.httpUriRequestConverter = httpUriRequestConverter;
    }

    public void setProxyProvider(ProxyProvider proxyProvider) {
        this.proxyProvider = proxyProvider;
    }

    public CloseableHttpClient getHttpClient(Site site) {
        if (site == null) {
            return httpClientGenerator.getClient(null);
        }
        String domain = site.getDomain();
        CloseableHttpClient httpClient = httpClients.get(domain);
        if (httpClient == null) {
            synchronized (this) {
                httpClient = httpClients.get(domain);
                if (httpClient == null) {
                    httpClient = httpClientGenerator.getClient(site);
                    httpClients.put(domain, httpClient);
                }

            }
        }
        return httpClient;
    }

    @Override
    public Page download(Request request, Task task) {
        if (task == null || task.getSite() == null) {
            throw new NullPointerException("task or site can not be null");
        }
        CloseableHttpResponse httpResponse = null;
        CloseableHttpClient httpClient = getHttpClient(task.getSite());
        Proxy proxy = proxyProvider != null ? proxyProvider.getProxy(task) : null;
        HttpClientRequestContext requestContext = httpUriRequestConverter.convert(request, task.getSite(), proxy);
        Page page = Page.fail();
        try {
            httpResponse = httpClient.execute(requestContext.getHttpUriRequest(), requestContext.getHttpClientContext());
            page = handleResponse(request, request.getCharset() != null ? request.getCharset() : task.getSite().getCharset(), httpResponse, task);
            onSuccess(request);
            logger.info("downloading page success {}", request.getUrl());
            return page;
        } catch (IOException e) {
            logger.warn("download page {} error", request.getUrl(),e);
            onError(request);
            return page;
        } finally {
            if (httpResponse != null) {
                //ensure the connection is released back to pool
                EntityUtils.consumeQuietly(httpResponse.getEntity());
            }
            if (proxyProvider != null && proxy != null) {
                proxyProvider.returnProxy(proxy, page, task);
            }
        }
    }

    @Override
    public void setThread(int thread) {
        httpClientGenerator.setPoolSize(thread);
    }

    protected Page handleResponse(Request request, String charset, HttpResponse httpResponse, Task task) throws IOException {
        Page page = new Page();
        if (httpResponse.getStatusLine().getStatusCode() != HttpConstant.StatusCode.CODE_200) {
            Object cycleTriedTimesObject = request.getExtra(Request.CYCLE_TRIED_TIMES);
            int cycleTriedTimes;
            if(null!=cycleTriedTimesObject){
                cycleTriedTimes = (Integer) cycleTriedTimesObject;
            }else{
                cycleTriedTimes = 1;
            }
            page.setDownloadSuccess(false);
            logger.warn("出错啦!code:"+httpResponse.getStatusLine().getStatusCode());
            logger.warn("出错url为:"+request.getUrl()+",出错次数为:"+cycleTriedTimes);
        }else{
            logger.info("get page:"+request.getUrl());
            byte[] bytes = IOUtils.toByteArray(httpResponse.getEntity().getContent());
            String contentType = httpResponse.getEntity().getContentType() == null ? "" : httpResponse.getEntity().getContentType().getValue();

            page.setBytes(bytes);
            if (!request.isBinaryContent()){
                if (charset == null) {
                    charset = getHtmlCharset(contentType, bytes);
                }
                page.setCharset(charset);
                page.setRawText(new String(bytes, charset));
            }
            page.setUrl(new PlainText(request.getUrl()));
            page.setRequest(request);
            page.setStatusCode(httpResponse.getStatusLine().getStatusCode());
            page.setDownloadSuccess(true);
            if (responseHeader) {
                page.setHeaders(HttpClientUtils.convertHeaders(httpResponse.getAllHeaders()));
            }
        }
        return page;
    }

    private String getHtmlCharset(String contentType, byte[] contentBytes) throws IOException {
        String charset = CharsetUtils.detectCharset(contentType, contentBytes);
        if (charset == null) {
            charset = Charset.defaultCharset().name();
            logger.warn("Charset autodetect failed, use {} as charset. Please specify charset in Site.setCharset()", Charset.defaultCharset());
        }
        return charset;
    }

    @Override
    protected void onError(Request request) {
        //记录tiaoout错误url
        String spiderName = (String) request.getExtras().get("spiderName");
        redisUtil.sSetAndTime(keyPrifix+spiderName+":errorUrl", RedisConfig.expireTime,request.getUrl());
        logger.warn(request.getUrl()+"   timeout!");


    }

    @Override
    protected void onSuccess(Request request) {

        String spiderName = (String) request.getExtras().get("spiderName");
        if(redisUtil.sHasKey(keyPrifix+spiderName+":errorUrl",request.getUrl())){
            redisUtil.setRemove(keyPrifix+spiderName+":errorUrl",request.getUrl());
        }

        redisUtil.sSetAndTime(keyPrifix+spiderName+":successUrl", RedisConfig.expireTime,request.getUrl());
        super.onSuccess(request);
    }
}
