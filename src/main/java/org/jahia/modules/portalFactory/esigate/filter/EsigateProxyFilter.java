package org.jahia.modules.portalFactory.esigate.filter;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.message.BasicNameValuePair;
import org.esigate.Driver;
import org.esigate.DriverFactory;
import org.esigate.HttpErrorPage;
import org.esigate.http.IncomingRequest;
import org.esigate.impl.UriMapping;
import org.esigate.servlet.impl.DriverSelector;
import org.esigate.servlet.impl.RequestFactory;
import org.esigate.servlet.impl.RequestUrl;
import org.esigate.servlet.impl.ResponseSender;
import org.jahia.bin.filters.AbstractServletFilter;
import org.jahia.modules.portalFactory.esigate.EsigateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by kevan on 24/03/14.
 */
public class EsigateProxyFilter extends AbstractServletFilter {
    private static Logger logger = LoggerFactory.getLogger(EsigateService.class);

    private EsigateService esigateService;
    private RequestFactory requestFactory;
    private final DriverSelector driverSelector = new DriverSelector();
    private final ResponseSender responseSender = new ResponseSender();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        requestFactory = new RequestFactory(filterConfig.getServletContext());
        // Force esigate configuration parsing to trigger errors right away (if
        // any) and prevent delay on first call.
        try {
            DriverFactory.configure(EsigateService.stringToProperties(esigateService.getSettings().getConfig()));
        } catch (IOException e) {
            logger.error("Unable to transform string to properties", e);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        IncomingRequest incomingRequest = requestFactory.create(httpServletRequest, httpServletResponse, chain);

        if (request.getInputStream().available() == 0 && request.getContentLength() > 0) {
            // Recreate an input stream from parameters if it has been read already.
            List<NameValuePair> parameters = new ArrayList<NameValuePair>();
            for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
                for (String value : entry.getValue()) {
                    parameters.add(new BasicNameValuePair(entry.getKey(), value));
                }
            }
            String encoded = URLEncodedUtils.format(parameters, incomingRequest.getEntity().getContentEncoding().getValue());
            InputStreamEntity entity = new InputStreamEntity(new ByteArrayInputStream(encoded.getBytes()), encoded.length());
            entity.setContentEncoding(incomingRequest.getEntity().getContentEncoding().getValue());
            entity.setContentType(incomingRequest.getEntity().getContentType().getValue());
            incomingRequest.setEntity(entity);
        }

        Pair<Driver, UriMapping> dm = null;
        try {
            dm = driverSelector.selectProvider(httpServletRequest, false);
            String relUrl = RequestUrl.getRelativeUrl(httpServletRequest, dm.getRight(), false);
            logger.debug("Proxying {}", relUrl);
            String workspace = StringUtils.substringBefore(relUrl, "/");
            relUrl = StringUtils.substringAfter(relUrl, "/");
            String lang = StringUtils.substringBefore(relUrl, "/");
            relUrl = StringUtils.substringAfter(relUrl, "/");
            incomingRequest.setAttribute("jahia.language", lang);
            incomingRequest.setAttribute("jahia.workspace", workspace);
            CloseableHttpResponse driverResponse = dm.getLeft().proxy(relUrl, incomingRequest);
            responseSender.sendResponse(driverResponse, incomingRequest, httpServletResponse);
        } catch (HttpErrorPage e) {
            if (!httpServletResponse.isCommitted()) {
                responseSender.sendResponse(e.getHttpResponse(), incomingRequest, httpServletResponse);
            }
        }

    }

    @Override
    public void destroy() {

    }

    public void setEsigateService(EsigateService esigateService) {
        this.esigateService = esigateService;
    }
}