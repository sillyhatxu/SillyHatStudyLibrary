package com.sillyhat.project.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.AfterReturningAdvice;

import java.lang.reflect.Method;

/**
 * <Description functions in a word>
 * <Detail description>
 * 
 * @author  Cookie Xu
 * @version  [Version NO, 2013-6-24]
 * @see  [Related classes/methods]
 * @since  [product/Modul version]
 */
public class MyAfterReturningAdvice implements AfterReturningAdvice{
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	public void afterReturning(Object returnvalue, Method method, Object[] params, Object obj) throws Throwable {
//		logger.info("MyAfterReturningAdvice");
	}
}

