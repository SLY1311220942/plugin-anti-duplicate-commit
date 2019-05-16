package com.sly.plugin.antiduplicatecommit.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.sly.plugin.antiduplicatecommit.configuration.AntiDuplicateCommitConfig;

/**
 * 开启反重复提交注解
 * @author sly
 * @time 2019年5月15日
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(AntiDuplicateCommitConfig.class)
public @interface EnableAntiDuplicateCommit {
	
}

