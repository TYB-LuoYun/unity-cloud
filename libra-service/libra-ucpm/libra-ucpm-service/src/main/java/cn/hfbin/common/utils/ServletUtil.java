package cn.hfbin.common.utils;


import cn.hfbin.log.MyRequestWrapper;
import com.alibaba.fastjson.JSON;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.MimeHeaders;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ftm
 * @date 2022/10/31 0031 17:55
 */
public class ServletUtil {


    public static MyRequestWrapper getWrapperRequest(HttpServletRequest request){
        // 获取请求body
        try {
            MyRequestWrapper myRequestWrapper = new MyRequestWrapper(request);
            return myRequestWrapper;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static  String getBody(HttpServletRequest request){
        // 获取请求body
        try {
            MyRequestWrapper myRequestWrapper = new MyRequestWrapper(request);
            return myRequestWrapper.getBody();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getFullUrl(HttpServletRequest req) {
        return req.getRequestURL()+ (StringUtils.isBlank(req.getQueryString())==true?"":"?"+req.getQueryString());
    }

    /**
     * 获取url的根地址
     * @param req
     * @return
     */
    public static String getRootUrl(HttpServletRequest req) {
        String s = req.getRequestURL().toString();
        Pattern p = Pattern.compile("([^:/])(/)");
        Matcher m = p.matcher(s);
        if (m.find()) {
            int start = m.start(2);
            return s.substring(0,start);
        }else{
            return null;
        }
    }

    /**
     * 获取String参数
     */
    public static String getParameter(String name)
    {
        return getRequest().getParameter(name);
    }

    /**
     * 获取String参数
     */
    public static String getParameter(String name, String defaultValue)
    {
        String parameter = getRequest().getParameter(name);
        if(parameter == null){
            return defaultValue;
        }
        return parameter;
    }

    /**
     * 获取Integer参数
     */
    public static Integer getParameterToInt(String name)
    {

        String parameter = getRequest().getParameter(name);
        if(parameter == null){
            return null;
        }
        return Integer.valueOf(parameter);
    }

    /**
     * 获取Integer参数
     */
    public static Integer getParameterToInt(String name, Integer defaultValue)
    {
        String parameter = getRequest().getParameter(name);
        if(parameter == null){
            return defaultValue;
        }
        return Integer.valueOf(parameter);
    }

    /**
     * 获取Boolean参数
     */
    public static Boolean getParameterToBool(String name)
    {

        String parameter = getRequest().getParameter(name);
        if(parameter == null){
            return null;
        }
        if(parameter=="1"||parameter.equals("true")){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 获取Boolean参数
     */
    public static Boolean getParameterToBool(String name, Boolean defaultValue)
    {
        String parameter = getRequest().getParameter(name);
        if(parameter == null){
            return defaultValue;
        }
        if(parameter=="1"||parameter.equals("true")){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 获取request
     */
    public static HttpServletRequest getRequest()
    {
        ServletRequestAttributes requestAttributes = getRequestAttributes();
        if(requestAttributes == null){
            return null;
        }
        return requestAttributes.getRequest();
    }

    /**
     * 获取response
     */
    public static HttpServletResponse getResponse()
    {
        return getRequestAttributes().getResponse();
    }

    /**
     * 获取session
     */
    public static HttpSession getSession()
    {
        return getRequest().getSession();
    }

    public static ServletRequestAttributes getRequestAttributes()
    {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        return (ServletRequestAttributes) attributes;
    }

    /**
     * 将字符串渲染到客户端
     *
     * @param response 渲染对象
     * @param string 待渲染的字符串
     */
    public static void renderString(HttpServletResponse response, String string)
    {
        try
        {
            response.setStatus(200);
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.getWriter().print(string);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }



    /**
     * 内容编码
     *
     * @param str 内容
     * @return 编码后的内容
     */
    public static String urlEncode(String str)
    {
        try
        {
            return URLEncoder.encode(str, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            return StringUtils.EMPTY;
        }
    }

    /**
     * 内容解码
     *
     * @param str 内容
     * @return 解码后的内容
     */
    public static String urlDecode(String str)
    {
        try
        {
            return URLDecoder.decode(str, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            return StringUtils.EMPTY;
        }
    }

    /**
     * 获取get/post参数
     * @return
     */
    public static Map<String, Object> getParams() {
        HttpServletRequest request = getRequest();
        return getParams(request);
    }


    public static Map<String,Object> getParams(HttpServletRequest request) {
        Map combineResultMap = new HashMap();
        Map<String,Object> map = new HashMap<>();
        Enumeration paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = (String) paramNames.nextElement();
            String[] paramValues = request.getParameterValues(paramName);
            if (paramValues.length == 1) {
                String paramValue = paramValues[0];
                if (paramValue.length() != 0) {
                    map.put(paramName, paramValue);
                }
            }
        }
        combineResultMap.putAll(map);
        if(!"POST".equals(request.getMethod())){
            return combineResultMap;
        }
        try {
            String reqBody = getBodyString(request);
            Map<String,Object> map2 = JSON.parseObject(reqBody, Map.class);
            if(map2!=null){
                combineResultMap.putAll(map2);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
        }

        return combineResultMap;
    }


    public static String getBodyString(final HttpServletRequest request) throws IOException {
        String contentType = request.getContentType();
        String bodyString = "";
        ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();
        if ( StringUtils.isNotBlank(contentType) && (contentType.contains("multipart/form-data") || contentType.contains("x-www-form-urlencoded"))) {
            Map<String, String[]> parameterMap = request.getParameterMap();
            for (Map.Entry<String, String[]> next : parameterMap.entrySet()) {
                String[] values = next.getValue();
                String value = null;
                if (values != null) {
                    if (values.length == 1) {
                        value = values[0];
                    } else {
                        value = Arrays.toString(values);
                    }
                }
                map.put(next.getKey(), value);
            }
            if (map !=null ) {
                bodyString = JSON.toJSONString(map);
            }
            return bodyString;
        } else {
            return IOUtils.toString(request.getInputStream());
        }
    }


    /**
     * 获取目标主机的ip
     * @return
     */
    public static String getCleintIp() {
        HttpServletRequest request = getRequest();
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip.contains("0:0:0:0:0:0:0:1") ? "127.0.0.1" : ip;
    }



    public static void setHeaders(HttpServletRequest request,Map<String, String> headerMap){
        Class<? extends HttpServletRequest> requestClass = request.getClass();
        try {
            Field request1 = requestClass.getDeclaredField("request");
            request1.setAccessible(true);
            Object o = request1.get(request);
            Field coyoteRequest = o.getClass().getDeclaredField("coyoteRequest");
            coyoteRequest.setAccessible(true);
            Object o1 = coyoteRequest.get(o);
            Field headers = o1.getClass().getDeclaredField("headers");
            headers.setAccessible(true);
            MimeHeaders o2 = (MimeHeaders)headers.get(o1);
            for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                o2.addValue(entry.getKey()).setString(entry.getValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void setHeaders(MyRequestWrapper request,Map<String, String> headerMap){
        for (Map.Entry<String, String> entry : headerMap.entrySet()) {
            request.addHeader(entry.getKey(), entry.getValue());
        }
    }



    private void setHeader2(HttpServletRequest request,String key,String value){
        Class<? extends HttpServletRequest> requestClass = request.getClass();
        try {
            Field request1 = requestClass.getDeclaredField("request");
//          如果自定义了request实现类的话需要自行修改
//            Field request1 = requestClass.getDeclaredField("orgRequest");

            request1.setAccessible(true);
            Object o = request1.get(request);
            Field coyoteRequest = o.getClass().getDeclaredField("exchange");
            coyoteRequest.setAccessible(true);
            Object o1 = coyoteRequest.get(o);
//            Field headers = o1.getClass().getDeclaredField("headers");
            Field headers = o1.getClass().getDeclaredField("requestHeaders");
            headers.setAccessible(true);
            MimeHeaders o2 = (MimeHeaders)headers.get(o1);
//            HeaderMap o2 = (HeaderMap)headers.get(o1);
            o2.addValue(key).setString(value);
//            o2.addFirst(new HttpString(key),value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

