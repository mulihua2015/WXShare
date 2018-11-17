package com.WXShare.service.Impl;

import com.WXShare.service.IWXService;
import com.alibaba.fastjson.JSONObject;
import lombok.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.UnsupportedEncodingException;

@Service
public class WXServiceImpl implements IWXService {

    //微信账号信息,注意替换
    private String appId="";
    private String appSecret="";
    private String accessTokeUrl="https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET";
    private String jsapiTicketUrl="https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=ACCESSTOKEN&type=jsapi";

    // 5分钟,可以根据需要来设置
    private Long time=300L;

    //处理缓存时间
    public static Map<String,Object> dataMap = new HashMap<String,Object>();

    @Override
    public Map<String, Object> getTicketData(String url) {
        String accessToken="";
        String jsapiTicket="";
        try{
            accessToken=getToken();
            jsapiTicket=getJsapiTicket(accessToken);
        }catch(Exception e)
        {
            System.out.println("系统异常:"+e.getMessage());
        }
        Map<String, Object> ret = new HashMap<String, Object>();
        String nonce_str = create_nonce_str();
        String timestamp = create_timestamp();
        String encyptStr;
        String signature = "";

        //注意这里参数名必须全部小写,且必须有序
        encyptStr = "jsapi_ticket=" + jsapiTicket +
                "&noncestr=" + nonce_str +
                "&timestamp=" + timestamp +
                "&url=" + url;
        System.out.println(encyptStr);
        try
        {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(encyptStr.getBytes("UTF-8"));
            signature = byteToHex(crypt.digest());
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        ret.put("url", url);
        ret.put("jsapi_ticket", jsapiTicket);
        ret.put("nonceStr", nonce_str);
        ret.put("timestamp", timestamp);
        ret.put("signature", signature);

        return ret;
    }

    public String getJsapiTicket(String accesstoken) throws Exception{
        String jsapiTicket="";
        Long nowDate = new Date().getTime();
        if (dataMap != null && dataMap.get("jsapiTicket") != null
                && dataMap.get("ticketTime") != null
                && (nowDate - Long.parseLong(String.valueOf(dataMap.get("ticketTime"))) < time * 1000)) {
            jsapiTicket = (String) dataMap.get("jsapiTicket");
        } else {
            String reqUrl=jsapiTicketUrl.replace("ACCESSTOKEN", accesstoken);
            System.out.println("jsapiReqUrl:"+reqUrl);
            JSONObject dataJson = JSONObject.parseObject(doGet(reqUrl));
            System.out.println("jsapiTicket:"+dataJson.toJSONString());
            dataMap.put("jsapiTicket", dataJson.getString("ticket"));
            dataMap.put("ticketTime", new Date().getTime() + "");
            System.out.println("time:"+new Date().getTime());
            jsapiTicket = (String) dataMap.get("jsapiTicket");
        }
        return jsapiTicket;
    }

    public String getToken() throws Exception {
        String accessToken = "";
        Long nowDate = new Date().getTime();
        if (dataMap != null && dataMap.get("accessToken") != null
                && dataMap.get("tokenTime") != null
                && (nowDate - Long.parseLong(String.valueOf(dataMap.get("tokenTime"))) < time * 1000)) {
            accessToken = (String) dataMap.get("accessToken");
        } else {
            String reqUrl=accessTokeUrl.replace("APPID", appId).replace("APPSECRET", appSecret);
            System.out.println("reqUrl:"+reqUrl);
            JSONObject dataJson = JSONObject.parseObject(doGet(reqUrl));
            System.out.println("accessToken:"+dataJson);
            dataMap.put("accessToken", dataJson.getString("access_token"));
            dataMap.put("tokenTime", new Date().getTime() + "");
            accessToken = (String) dataMap.get("accessToken");
        }
        return accessToken;
    }

    public String doGet(String url) throws Exception {
        URL localURL = new URL(url);
        URLConnection connection = localURL.openConnection();
        HttpURLConnection httpURLConnection = (HttpURLConnection) connection;
        httpURLConnection.setRequestProperty("Accept-Charset", "utf-8");
        httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader reader = null;
        StringBuffer resultBuffer = new StringBuffer();
        String tempLine = null;
        if (httpURLConnection.getResponseCode() >= 300) {
            throw new Exception("HTTP Request is not success, Response code is " + httpURLConnection.getResponseCode());
        }
        try {
            inputStream = httpURLConnection.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream);
            reader = new BufferedReader(inputStreamReader);

            while ((tempLine = reader.readLine()) != null) {
                resultBuffer.append(tempLine);
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return resultBuffer.toString();
    }
    private String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash)
        {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    private String create_nonce_str() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String create_timestamp() {
        return Long.toString(System.currentTimeMillis() / 1000);
    }
}
