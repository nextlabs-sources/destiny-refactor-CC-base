/*
 * Created on Jan 19, 2015
 * 
 * All sources, binaries and HTML pages (C) copyright 2014 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 *
 */
package com.nextlabs.evaluationconnector.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * AuthenticationFilter for the Evaluation Requests.
 * </p>
 *
 *
 * @author Amila Silva
 *
 */
public class AuthenticationFilter implements Filter {

	private static final Log log = LogFactory
			.getLog(AuthenticationFilter.class);

	@Override
	public void destroy() {
		if (log.isInfoEnabled())
			log.info("Authentication Filter destroyed");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		HttpServletResponse httpResponse = (HttpServletResponse) response;
		chain.doFilter(request, httpResponse);
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		if (log.isInfoEnabled())
			log.info("Authentication Filter initialized");

	}

}
