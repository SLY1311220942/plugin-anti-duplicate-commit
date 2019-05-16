package com.sly.plugin.antiduplicatecommit.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 反重复提交属性类
 * 
 * @author sly
 * @time 2019年5月16日
 */
@ConfigurationProperties(prefix = "anti-duplicate-commit")
public class AntiDuplicateCommitProperties {
	/** token不存在提示语 */
	private String message;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
