package com.qxj.springboot;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qxj.springboot.controller.model.TradeDataModel;
import com.qxj.springboot.dao.TradeDataDOMapper;
import com.qxj.springboot.dataobject.TradeDataDO;
import com.qxj.springboot.utils.CommonConstant;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.text.ParseException;

/**
 * @author YoSaukit
 * @date 2019/10/23 13:41
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Ignore
public class HttpUtil {
    private static RedisTemplate redisTemplate;

    @Autowired
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        HttpUtil.redisTemplate = redisTemplate;
    }
    @Autowired
    private TradeDataDOMapper tradeDataDOMapper;
    //创建http client
    private static CloseableHttpClient httpClient = createHttpsClient();
    private static final String ACCESS_TOKEN = "a3607ef5f33314c66916ecda6993a29b8cade5c23fb97f758c83c80c2889133e";
    @Test
    public void httpUtil() throws IOException, ParseException {
        String url = "https://api.wmcloud.com/data/v1/api/market/getSHSZBarRTIntraDay.json?ticker=&exchangeCD=&assetClass=E&startTime=09:30&endTime=10:00&unit=1";
        HttpGet httpGet = new HttpGet(url);
        //在header里加入 Bearer {token}，添加认证的token，并执行get请求获取json数据
        httpGet.addHeader("Authorization", "Bearer " + ACCESS_TOKEN);
        CloseableHttpResponse response = httpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        String body = EntityUtils.toString(entity);
        saveToRedis(body);
    }
    //创建http client
    public static CloseableHttpClient createHttpsClient() {
        X509TrustManager x509mgr = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] xcs, String string) {
            }
            @Override
            public void checkServerTrusted(X509Certificate[] xcs, String string) {
            }
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
        //因为java客户端要进行安全证书的认证，这里我们设置ALLOW_ALL_HOSTNAME_VERIFIER来跳过认证，否则将报错
        SSLConnectionSocketFactory sslsf = null;
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{x509mgr}, null);
            sslsf = new SSLConnectionSocketFactory(
                    sslContext,
                    SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return HttpClients.custom().setSSLSocketFactory(sslsf).build();
    }
    @SuppressWarnings("unchecked")
    public void saveToRedis(String entity) throws ParseException {
        JSONObject jsonObject = JSON.parseObject(entity);
        JSONArray list = jsonObject.getJSONArray("data");
        for (int i = 0; i < list.size(); i++) {
            JSONObject ob = list.getJSONObject(i);
            int unit = ob.getIntValue("unit");
            String ticker;
            if (ob.getString("exchangeCD").equals("XSHE"))ticker =  ob.getString("ticker")+".SZ";
            else if (ob.getString("exchangeCD").equals("XSHG"))ticker =  ob.getString("ticker")+".SH";
            else ticker =  ob.getString("ticker")+"."+ob.getString("exchangeCD");
            String shortNM = ob.getString("shortNM");
            JSONArray barBodys = ob.getJSONArray("barBodys");
            for (int j = 0; j < barBodys.size(); j++) {
                JSONObject bar = barBodys.getJSONObject(j);
                int totalVolume = bar.getIntValue("totalVolume");
                BigDecimal closePrice = bar.getBigDecimal("closePrice");
                BigDecimal openPrice = bar.getBigDecimal("openPrice");
                BigDecimal highPrice = bar.getBigDecimal("highPrice");
                BigDecimal lowPrice = bar.getBigDecimal("lowPrice");
                BigDecimal totalValue = bar.getBigDecimal("totalValue");
                BigDecimal vwap = totalVolume==0?closePrice:totalValue.divide(new BigDecimal(totalVolume),3,BigDecimal.ROUND_UP);
                TradeDataModel tradeDataModel = new TradeDataModel(unit,ticker,shortNM,convertToDate(bar.getString("barTime")),openPrice,closePrice,lowPrice,highPrice,totalValue,
                        totalVolume,vwap);
                Long time = Long.parseLong(convertToString(bar.getString("barTime")));
                tradeDataDOMapper.insert(convertFromModel(tradeDataModel));
                //zset存储，key：MIN1_000001.SZ score: 20191024093300 ,可以范围查询
                redisTemplate.opsForZSet().add(CommonConstant.MIN1_PREFIX+ticker,tradeDataModel,time);
                //redisTemplate.opsForValue().set(CommonConstant.MIN1_PREFIX+ticker+"_"+convertToString(bar.getString("barTime")),tradeDataDO);
            }


        }
    }
    public String convertToString(String barTime) {
        String day = LocalDate.now().toString();
        return (day+" "+barTime+":00").replaceAll("[[\\s-:punct:]]","");
    }
    public DateTime convertToDate(String barTime){
        String day = LocalDate.now(DateTimeZone.UTC).toString()+" "+barTime+":00";
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        return DateTime.parse(day, formatter);
    }
    public TradeDataDO convertFromModel(TradeDataModel tradeDataModel){
        if (tradeDataModel == null) return null;
        TradeDataDO tradeDataDO = new TradeDataDO();
        BeanUtils.copyProperties(tradeDataModel,tradeDataDO);
        tradeDataDO.setBarTime(tradeDataModel.getBarTime().toDate());
        return tradeDataDO;
    }






}
