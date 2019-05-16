package com.sly.plugin.antiduplicatecommit.aop;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.sly.plugin.antiduplicatecommit.annotation.AntiDuplicateCommit;
import com.sly.plugin.antiduplicatecommit.properties.AntiDuplicateCommitProperties;

/**
 * 反重复提交AOP
 * 
 * @author sly
 * @time 2019年5月15日
 */
@Aspect
public class AntiDuplicateCommitAspect {
	private static final Logger LOGGER = LoggerFactory.getLogger(AntiDuplicateCommitAspect.class);

	private AntiDuplicateCommitProperties antiDuplicateCommitProperties;

	public void setAntiDuplicateCommitProperties(AntiDuplicateCommitProperties antiDuplicateCommitProperties) {
		this.antiDuplicateCommitProperties = antiDuplicateCommitProperties;
	}

	@Around("@annotation(com.sly.plugin.antiduplicatecommit.annotation.AntiDuplicateCommit)")
	public Object around(ProceedingJoinPoint point) throws Throwable {

		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
				.getRequest();

		// 判断请求类型
		String header = request.getHeader("X-Requested-With");
		boolean isAjax = "XMLHttpRequest".equals(header) ? true : false;

		// 获取注解
		MethodSignature signature = (MethodSignature) point.getSignature();
		Method method = signature.getMethod();
		// 获取方法上的注解对象
		AntiDuplicateCommit antiDuplicateCommit = method.getAnnotation(AntiDuplicateCommit.class);

		// 返回结果map
		Map<String, Object> result = new HashMap<>(16);

		// 是否验证通过
		boolean isCheckPassed = true;

		// 获取注解参数
		String[] keys = antiDuplicateCommit.keys();
		if (antiDuplicateCommit.isCheckToken()) {
			for (String key : keys) {
				String existToken = (String) request.getSession().getAttribute(key);
				String token = request.getParameter(key);
				if (StringUtils.isBlank(existToken) || !existToken.equals(token)) {
					result.put("message", antiDuplicateCommitProperties.getMessage());
					isCheckPassed = false;
					break;
				}
			}
		}

		// 回写token
		if (antiDuplicateCommit.isReturnToken()) {
			// 如果需要回传token,那么根据key值重新设置token
			for (String key : keys) {
				String uuid = UUID.randomUUID().toString();
				request.getSession().setAttribute(key, uuid);
				request.setAttribute(key, uuid);
				// 为返回结果设置token,便于ajax请求重新设置页面token
				result.put(key, uuid);
			}
		}

		if (!isCheckPassed) {
			return result;
		}

		// 执行方法
		try {
			Object ret = point.proceed();
			if (isAjax && antiDuplicateCommit.isReturnToken()) {
				// ajax请求需要给返回结果设置新token
				@SuppressWarnings("unchecked")
				Map<String, Object> originResultMap = (Map<String, Object>) ret;
				for (String key : keys) {
					originResultMap.put(key, request.getSession().getAttribute(key));
				}
				return originResultMap;
			}
			return ret;
		} catch (Exception e) {
			LOGGER.error("原始异常:" + ExceptionUtils.getStackTrace(e));
			return result;
		}

	}
}
