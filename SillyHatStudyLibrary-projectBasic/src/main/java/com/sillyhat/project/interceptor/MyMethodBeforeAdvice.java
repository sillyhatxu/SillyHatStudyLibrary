package com.sillyhat.project.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;

/**
 * <Description functions in a word> <Detail description>
 * 
 * @author Cookie Xu
 * @version [Version NO, 2013-6-24]
 * @see [Related classes/methods]
 * @since [product/Modul version]
 */
public class MyMethodBeforeAdvice implements MethodBeforeAdvice {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public void before(Method method, Object[] params, Object obj)
			throws Throwable {
		String checkMethod = method.getName();
		String[] vclass = obj.getClass().getName().split("\\.");
		String classMethod = vclass[vclass.length-1]+"."+checkMethod;
		logger.info(classMethod);
	}
}

