package com.atguigu.tingshu.common.login;

/*
 *@auther:zhushuai
 *@verson 1.0
 *@2026/6/2 10:07
 */

import com.atguigu.tingshu.common.constant.RedisConstant;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.common.result.ResultCodeEnum;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.model.user.UserInfo;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
public class TingshuLoginAspect {


    @Resource
    private RedisTemplate redisTemplate;


    @Around("execution(* com.atguigu.tingshu.*.api.*.*(..)) && @annotation(tingshuLogin)")
    public Object tingshuLogin(ProceedingJoinPoint pjp, TingshuLogin tingshuLogin) throws Throwable {


        try {
            ServletRequestAttributes servletRequestAttributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            HttpServletRequest request = servletRequestAttributes.getRequest();

            if (request == null) {
                throw new GuiguException(ResultCodeEnum.LOGIN_AUTH);
            }

            String token = request.getHeader("token");
            String loginKey = RedisConstant.USER_LOGIN_KEY_PREFIX + token;


            // 要求登录
            boolean required = tingshuLogin.required();
            if (required) {
                if (StringUtils.isEmpty(token)) {
                    throw new GuiguException(ResultCodeEnum.LOGIN_AUTH);
                }

                UserInfo userInfo = (UserInfo) redisTemplate.opsForValue().get(loginKey);

                if (userInfo == null) {
                    throw new GuiguException(ResultCodeEnum.LOGIN_AUTH);
                }
            }


            // 获取用户Id
            if (!StringUtils.isEmpty(token)) {
                //  获取缓存中用户数据
                UserInfo userInfo = (UserInfo) this.redisTemplate.opsForValue().get(loginKey);
                if (null != userInfo) {
                    //  存储用户Id
                    AuthContextHolder.setUserId(userInfo.getId());
                }
            }

            return pjp.proceed();

        } finally {
            // 删除上下文userId
            AuthContextHolder.removeUserId();
        }
    }


}
