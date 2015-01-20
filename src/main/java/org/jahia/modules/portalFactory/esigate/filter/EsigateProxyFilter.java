/**
 * ==========================================================================================
 * =                            JAHIA'S ENTERPRISE DISTRIBUTION                             =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S ENTERPRISE DISTRIBUTIONS LICENSING - IMPORTANT INFORMATION
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group. All rights reserved.
 *
 *     This file is part of a Jahia's Enterprise Distribution.
 *
 *     Jahia's Enterprise Distributions must be used in accordance with the terms
 *     contained in the Jahia Solutions Group Terms & Conditions as well as
 *     the Jahia Sustainable Enterprise License (JSEL).
 *
 *     For questions regarding licensing, support, production usage...
 *     please contact our team at sales@jahia.com or go to http://www.jahia.com/license.
 *
 * ==========================================================================================
 *
 *
 *
 *
 */
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
import org.jahia.bin.Render;
import org.jahia.bin.filters.AbstractServletFilter;
import org.jahia.modules.portalFactory.esigate.EsigateService;
import org.jahia.utils.LanguageCodeConverters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

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
        if(!esigateService.isEnabled()){
            chain.doFilter(request, response);
        }else {
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;

            boolean extCall = httpServletRequest.getServletPath().startsWith("/ext");
            if (extCall && ((HttpServletRequest) request).getMethod().equals("POST")) {
                httpServletRequest = new HttpServletRequestWithGetMethod(httpServletRequest);
            }

            IncomingRequest incomingRequest = requestFactory.create(httpServletRequest, httpServletResponse, chain);

            if (extCall && request.getInputStream().available() == 0 && request.getContentLength() > 0) {
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

            try {
                DriverSelector.ProviderContext dm = driverSelector.selectProvider(httpServletRequest, false);
                String relUrl = dm.getRelUrl();
                logger.debug("Proxying {}", relUrl);
                if (extCall) {
                    String mode = StringUtils.substringBefore(relUrl, "/");
                    relUrl = StringUtils.substringAfter(relUrl, "/");
                    String lang = StringUtils.substringBefore(relUrl, "/");
                    relUrl = StringUtils.substringAfter(relUrl, "/");

                    if (!LanguageCodeConverters.LANGUAGE_PATTERN.matcher(lang).matches()) {
                        mode += "/" + lang;
                        lang = StringUtils.substringBefore(relUrl, "/");
                        relUrl = StringUtils.substringAfter(relUrl, "/");
                    }
                    incomingRequest.setAttribute("jahia.language", lang);
                    incomingRequest.setAttribute("jahia.mode", mode);
                }
                CloseableHttpResponse driverResponse = dm.getDriver().proxy(relUrl, incomingRequest);
                responseSender.sendResponse(driverResponse, incomingRequest, httpServletResponse);
            } catch (HttpErrorPage e) {
                if (!httpServletResponse.isCommitted()) {
                    responseSender.sendResponse(e.getHttpResponse(), incomingRequest, httpServletResponse);
                }
            }
        }
    }

    @Override
    public void destroy() {

    }

    public void setEsigateService(EsigateService esigateService) {
        this.esigateService = esigateService;
    }

    private static class HttpServletRequestWithGetMethod extends HttpServletRequestWrapper {
        public HttpServletRequestWithGetMethod(HttpServletRequest httpServletRequest) {
            super(httpServletRequest);
        }

        @Override
        public String getParameter(String name) {
            if (name.equals(Render.METHOD_TO_CALL)) {
                return "GET";
            }
            return super.getParameter(name);
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            Map<String, String[]> parameters = new HashMap<String,String[]>(super.getParameterMap());
            parameters.put(Render.METHOD_TO_CALL,new String[] {"GET"});
            return parameters;
        }

        @Override
        public Enumeration<String> getParameterNames() {
            return new Vector<String>(getParameterMap().keySet()).elements();
        }

        @Override
        public String[] getParameterValues(String name) {
            if (name.equals(Render.METHOD_TO_CALL)) {
                return new String[] {"GET"};
            }
            return super.getParameterValues(name);
        }
    }
}