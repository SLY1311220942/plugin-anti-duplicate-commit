package com.sly.plugin.antiduplicatecommit.aop;

import java.lang.reflect.Method;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.sly.plugin.antiduplicatecommit.annotation.AntiDuplicateCommit;

/**
 * 反重复提交AOP
 * 
 * @author sly
 * @time 2019年5月15日
 */
@Aspect
public class AntiDuplicateCommitAspect {
	@Around("@annotation(com.sly.plugin.antiduplicatecommit.annotation.AntiDuplicateCommit)")
	public Object around(ProceedingJoinPoint point) throws Throwable {

		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
				.getRequest();
		// 获取注解
		MethodSignature signature = (MethodSignature) point.getSignature();
		Method method = signature.getMethod();
		// 获取方法上的注解对象
		AntiDuplicateCommit antiDuplicateCommit = method.getAnnotation(AntiDuplicateCommit.class);

		// 获取注解参数
		String[] keys = antiDuplicateCommit.keys();
		if (antiDuplicateCommit.isCheckToken()) {
			for (String key : keys) {
				
			}
		}
		if (antiDuplicateCommit.isReturnToken()) {
			// 如果需要回传token,那么根据key值重新设置token
			for (String key : keys) {
				String uuid = UUID.randomUUID().toString();
				request.getSession().setAttribute(key, uuid);
				request.setAttribute(key, uuid);
			}
		}

		// 执行方法
		Object object = point.proceed();
		return object;
	}
}
