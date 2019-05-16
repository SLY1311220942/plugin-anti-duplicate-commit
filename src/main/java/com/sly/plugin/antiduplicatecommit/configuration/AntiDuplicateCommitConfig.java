package com.sly.plugin.antiduplicatecommit.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.sly.plugin.antiduplicatecommit.properties.AntiDuplicateCommitProperties;

/**
 * 反重复提交配置类
 * @author sly
 * @time 2019年5月16日
 */
@Configuration
@ComponentScan(basePackages = "com.sly.plugin.antiduplicatecommit.aop")
@EnableConfigurationProperties(AntiDuplicateCommitProperties.class)
public class AntiDuplicateCommitConfig {
	
}

