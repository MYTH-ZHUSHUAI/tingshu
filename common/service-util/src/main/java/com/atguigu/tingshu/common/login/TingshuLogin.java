package com.atguigu.tingshu.common.login;

/*
 *@auther:zhushuai
 *@verson 1.0
 *@2026/6/2 10:04
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TingshuLogin {

    boolean required() default true;
}
