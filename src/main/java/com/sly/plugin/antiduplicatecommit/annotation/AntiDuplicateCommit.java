package com.sly.plugin.antiduplicatecommit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 反重复提交注解
 * @author sly
 * @time 2019年5月15日
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AntiDuplicateCommit {
	/**
	 * token的key
	 * @return
	 * @author sly
	 * @time 2019年5月15日
	 */
	String[] keys();
	
	/**
	 * 是否返回token
	 * @return
	 * @author sly
	 * @time 2019年5月15日
	 */
	boolean isReturnToken() default false;
	
	/**
	 * 是否验证token
	 * @return
	 * @author sly
	 * @time 2019年5月15日
	 */
	boolean isCheckToken() default false;
}

