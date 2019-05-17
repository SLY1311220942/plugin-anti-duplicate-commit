# plugin-anti-duplicate-commit
防止重复提交插件
## 使用范围
用于springboot项目controller上

## 使用方法
### 1. 引入jar包
使用maven方式，你得发布到你的仓库或者其它能够使用maven引用到的方式。
```xml
<dependency>
	<groupId>com.sly</groupId>
	<artifactId>plugin-anti-duplicate-commit</artifactId>
	<version>1.0</version>
</dependency>
```

### 2. 配置属性文件
```yml
anti-duplicate-commit: 
  message: 页面超时请刷新重试!
```

### 3. 启用防止重复提交
在项目启动类上添加如下注解
> @EnableAntiDuplicateCommit

### 4. 在需要验证的方法上添加验证注解
```java
@AntiDuplicateCommit(keys = { DemoToken.DEMO_ADD_TOKEN }, isCheckToken = false, isReturnToken = true)
```

```java
@RequestMapping("/toAdd")
@AntiDuplicateCommit(keys = { DemoToken.DEMO_ADD_TOKEN }, isCheckToken = false, isReturnToken = true)
public String toAdd(HttpServletRequest request, HttpServletResponse response) {
	return "/pages/add.html";
}

@ResponseBody
@RequestMapping("/demoAddSubmit")
@AntiDuplicateCommit(keys = { DemoToken.DEMO_ADD_TOKEN }, isCheckToken = true, isReturnToken = false)
public Object demoAddSubmit(HttpServletRequest request, HttpServletResponse response) {
	Map<String, Object> result = new HashMap<>(16);
	try {
		System.out.println("我是新增业务方法,我执行了!");
		result.put("status", 200);
		result.put("message", "新增成功!");
	} catch (Exception e) {
		LOGGER.error(ExceptionUtils.getStackTrace(e));
		result.put("status", 400);
		result.put("message", "新增失败!");
	}
	return result;
}
```

> keys：token的key,可以是多个。

> isCheckToken：是否验证token。

> isReturnToken：是否向页面或返回对象中返回新的token。

## 注意事项
### 1. 返回对象
使用该插件返回对象必须统一。
> 例如demo中返回数据对象都是map。跳转页面时返回都是String，值采用request域返回。

### 2. 提交数据
> 提交数据时token的字段必须与注解上的字段一致，不然切面验证时无法取到值。

```html
<!DOCTYPE html>
<html>

	<head>
		<meta charset="UTF-8">
		<title>新增</title>
	</head>
	<script>
		var webRoot = '[[${#httpServletRequest.getContextPath()}]]';
	</script>
	<body>
		<input type="hidden" id="DEMO_ADD_TOKEN" name="DEMO_ADD_TOKEN" th:value="${DEMO_ADD_TOKEN}" />
		<button onclick="add();">新增</button>
	</body>
	<script th:src="@{/resource/common.js}"></script>
	<script th:src="@{/resource/jquery.min.js}"></script>
	<script>
		function add(){
			$.ajax({
				type: "post",
				url: webRoot + "/demo/demoAddSubmit",
				dataType: "json",
				data: {
					DEMO_ADD_TOKEN: $.trim($("#DEMO_ADD_TOKEN").val())
				},
				success: function(data) {
					if(data.status == 200) {
						alert(data.message);
					} else {
						alert(data.message);
					}
				}
			});
		}
	</script>
</html>

```

### 3. 如何在我的项目使用
这个demo使用的都是map进行返回数据封装，跳转页面都是String。如果需要适合自己的项目，需要对切面进行修改。这个项目并不是一个产品，只是一个技术的验证而已，可以看到有些地方并不是那么尽善尽美，最终使用时还是要和具体的规范结合。

AntiDuplicateCommitAspect.java 根据需要自己改吧，毕竟每个项目都有自己的规范，有些东西没法用一套代码适应所有。当然提供一个接口让用户自己实现似乎也可以，不过使用起来会麻烦一些。再说了一个公司或者一个项目这些东西都没有统一起来，那只能说明管理者的失职，以及参与者的不专业。

```java
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
				String token = request.getParameter(key);
				String existToken = (String) request.getSession().getAttribute(key);
				//移除旧token 避免被重复使用
				request.getSession().removeAttribute(key);
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
```

## 使用demo
https://github.com/SLY1311220942/demo-antiduplicatecommit
