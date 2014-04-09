package org.jahia.modules.portalFactory.esigate.filter;

import org.esigate.servlet.ProxyFilter;
import org.jahia.bin.filters.AbstractServletFilter;

import javax.servlet.*;
import java.io.IOException;

/**
 * Created by kevan on 24/03/14.
 */
public class EsigateProxyFilter extends AbstractServletFilter {
    ProxyFilter proxyFilter;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        proxyFilter = new ProxyFilter();
        proxyFilter.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        proxyFilter.doFilter(servletRequest, servletResponse, filterChain);
    }

    @Override
    public void destroy() {
        if(proxyFilter != null){
            proxyFilter.destroy();
        }
    }
}