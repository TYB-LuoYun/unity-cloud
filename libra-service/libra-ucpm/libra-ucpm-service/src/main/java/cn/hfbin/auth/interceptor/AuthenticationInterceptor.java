package cn.hfbin.auth.interceptor;

import cn.hfbin.common.core.context.HeaderCode;
import cn.hfbin.common.core.exception.LibraException;
import cn.hfbin.common.token.AuthUtil;
import cn.hfbin.common.token.model.JwtUserInfo;
import cn.hfbin.common.utils.ServletUtil;
import cn.hfbin.log.MyRequestWrapper;
import cn.hfbin.ucpm.enums.UcPmExceptionCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;


/**
 * ClassName:AuthenticationInterceptor <br/>
 * Function:  <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2022/8/15 13:48 <br/>
 *
 * @author sheng.chen
 * @see
 * @since JDK 1.8
 */
@RefreshScope
public class AuthenticationInterceptor implements HandlerInterceptor {
    @Value("${secure.enable:true}")
    private boolean enable;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private AuthUtil authUtil;


    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private IgnoreUrlsConfig ignoreUrlsConfig;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //进入方法之前进行的操作
        if(!enable){
            return true;
        }
        // 白名单直接放行
        boolean isMatch = this.matchWhiteListUrl(request);
        if(isMatch){
            return true;
        }

        // 方法局部放行
        if(handler instanceof HandlerMethod){
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            PassToken passToken = method.getAnnotation(PassToken.class);
            if(passToken == null){
                passToken = handlerMethod.getBeanType().getAnnotation(PassToken.class);
            }
            if(passToken != null&&passToken.required()){
                return true;
            }
        }

        //获取token
        //如果不是映射到方法直接通过
        String token = request.getHeader(HeaderCode.TOKEN);
        if(StringUtils.isNotBlank(token)){
            JwtUserInfo authInfo = authUtil.getAuthInfo(token);
            Optional.ofNullable(authInfo).orElseThrow(() -> new LibraException(UcPmExceptionCode.TOKEN_INVALID));
            /**
             * 从redis中获取
             */
            Map<String, String> headers = new HashMap<>();
            headers.put(HeaderCode.ACCOUNT_ID, String.valueOf(authInfo.getAccountId()));
            headers.put(HeaderCode.IDENTITY_ID, String.valueOf(authInfo.getIdentityId()));
            headers.put(HeaderCode.DEPT_ID, String.valueOf(authInfo.getDeptId()));
            headers.put(HeaderCode.DEPT_CODE, authInfo.getDeptCode());
            headers.put(HeaderCode.USERNAME, authInfo.getUsername());
            headers.put(HeaderCode.DATA_SCOPE, String.valueOf(authInfo.getDeptCode()));
            ServletUtil.setHeaders((MyRequestWrapper) request,headers);
            return true;
        }else{
            throw  new LibraException(UcPmExceptionCode.TOKEN_INVALID);
        }


    }


    public static void main(String[] args){

        List<String> strings = new ArrayList<>();
        for(int i=0; i<1000;i++){
            strings.add("/"+i+"/system/**");
        }
        long start = System.currentTimeMillis();
        PathMatcher pathMatcher = new AntPathMatcher();
        for(String item : strings){
            System.out.println( pathMatcher.match(item, "/9999/system/get"));
        }
        long end = System.currentTimeMillis();
        System.out.println("结束");
        System.out.println((end-start)*1.0/1000 +"s");

    }

    private boolean matchWhiteListUrl(HttpServletRequest request) {
        //          白名单放行
        String uri = request.getRequestURI();
        PathMatcher pathMatcher = new AntPathMatcher();
        //白名单路径直接放行，这个可以在ResourceServerConfig中配置【其实这段代码配置了可不要】
        List<String> ignoreUrls = ignoreUrlsConfig.getUrls();
        for (String ignoreUrl : ignoreUrls) {
            if (pathMatcher.match(ignoreUrl, uri)) {
                return true;
            }
        }
        return false;
    }


    private void setAuthenticateFailMsg(HttpServletResponse response, String msg) throws IOException {
//        response.setContentType(SysConstants.APPLICATION_JSON_UTF8);
//        response.setStatus(HttpStatus.OK.value());
//        response.getWriter().write(mapper.writeValueAsString(Result.error(msg)));
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        //方法处理之后但是并未渲染视图的时候进行的操作
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //渲染视图之后进行的操作
    }
}