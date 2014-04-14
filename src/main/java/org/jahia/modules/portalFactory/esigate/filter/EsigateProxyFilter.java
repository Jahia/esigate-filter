package org.jahia.modules.portalFactory.esigate.filter;

import org.esigate.DriverFactory;
import org.esigate.servlet.ProxyFilter;
import org.jahia.bin.filters.AbstractServletFilter;
import org.jahia.modules.portalFactory.esigate.EsigateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import java.io.IOException;

/**
 * Created by kevan on 24/03/14.
 */
public class EsigateProxyFilter extends AbstractServletFilter {
    private static Logger logger = LoggerFactory.getLogger(EsigateService.class);

    ProxyFilter proxyFilter;
    EsigateService esigateService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        proxyFilter = new ProxyFilter();
        try {
            DriverFactory.configure(EsigateService.stringToProperties(esigateService.getSettings().getConfig()));
        } catch (IOException e) {
            logger.error("Unable to transform string to properties", e);
        }
        proxyFilter.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (esigateService.isEnabled()){
            proxyFilter.doFilter(servletRequest, servletResponse, filterChain);
        }else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    @Override
    public void destroy() {
        if(proxyFilter != null){
            proxyFilter.destroy();
        }
    }

    public void setEsigateService(EsigateService esigateService) {
        this.esigateService = esigateService;
    }
}