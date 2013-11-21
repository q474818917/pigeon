/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.process.filter;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import com.dianping.pigeon.component.invocation.InvocationResponse;
import com.dianping.pigeon.monitor.LoggerLoader;
import com.dianping.pigeon.remoting.common.config.RemotingConfigurer;
import com.dianping.pigeon.remoting.common.filter.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.component.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.component.async.ServiceFutureFactory;
import com.dianping.pigeon.remoting.invoker.component.context.InvokerContext;

/**
 * 
 * 
 * @author jianhuihuang
 * @version $Id: GatewayInvokeFilter.java, v 0.1 2013-6-20 下午9:50:16
 *          jianhuihuang Exp $
 */
public class GatewayInvokeFilter extends InvocationInvokeFilter {

	private static final Logger logger = LoggerLoader.getLogger(GatewayInvokeFilter.class);
	private static AtomicLong requests = new AtomicLong(0);

	@Override
	public InvocationResponse invoke(ServiceInvocationHandler handler, InvokerContext invocationContext)
			throws Throwable {
		InvokerConfig invokerConfig = invocationContext.getInvokerConfig();
		if (logger.isDebugEnabled()) {
			logger.debug("GatewayInvokeFilter invoke config:" + invokerConfig);
		}
		int maxRequests = invokerConfig.getMaxRequests();
		try {
			if (maxRequests > 0) {
				if (requests.incrementAndGet() > maxRequests) {
					throw new RuntimeException("request refused, max requests limit reached:" + maxRequests);
				}
			}
			try {
				return handler.handle(invocationContext);
			} catch (Throwable e) {
				if (Constants.CALL_FUTURE.equalsIgnoreCase(invokerConfig.getCallMethod())) {
					ServiceFutureFactory.remove();
				}
				throw e;
			}
		} finally {
			if (maxRequests > 0) {
				requests.decrementAndGet();
			}
		}
	}

}