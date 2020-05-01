/*
 * ==========================================================================================
 * =                            JAHIA'S ENTERPRISE DISTRIBUTION                             =
 * ==========================================================================================
 *
 *                                  http://www.jahia.com
 *
 * JAHIA'S ENTERPRISE DISTRIBUTIONS LICENSING - IMPORTANT INFORMATION
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group. All rights reserved.
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
 */
package org.jahia.modules.portalFactory.esigate.flow;

import org.hibernate.cache.ehcache.management.impl.BeanUtils;
import org.jahia.modules.portalFactory.esigate.EsigateService;
import org.jahia.modules.portalFactory.esigate.EsigateSettings;
import org.jahia.settings.SettingsBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by kevan on 10/04/14.
 */
public class EsigateFlowHandler implements Serializable {
    private static final long serialVersionUID = -7165546302210231402L;

    @Autowired
    private transient EsigateService esigateService;

    public EsigateSettings getSettings(HttpServletRequest request){
        EsigateSettings settings = esigateService.getSettings();
        if (settings.getConfig().isEmpty()) {
            reloadDefault(request);
            settings = esigateService.getSettings();
        }
        return settings;
    }
    
    public void saveSettings(EsigateSettings settings){
        esigateService.store(settings);
    }

    public void reloadDefault(HttpServletRequest request){
        esigateService.reloadDefault(request);
    }

    public EsigateProviderBean createProviderBean(HttpServletRequest request) {
        EsigateProviderBean bean = new EsigateProviderBean();
        bean.setOriginalConfig(esigateService.getSettings().getConfig());
        bean.setServerName(request.getServerName());
        bean.setServerPort(Integer.toString(request.getServerPort()));
        bean.setJahiaContext(SettingsBean.getInstance().getServletContext().getContextPath());
        return bean;
    }

    public String saveNewProvider(EsigateProviderBean bean) {
        final EsigateSettings settings = esigateService.getSettings();
        String config = settings.getConfig();
        StringBuilder newProviderConfig = new StringBuilder(esigateService.getNewProviderConfig());
        Pattern variables = Pattern.compile("\\$\\(([^)]+)\\)");
        Matcher m = variables.matcher(newProviderConfig);
        while (m.find()) {
            final Object beanProperty = BeanUtils.getBeanProperty(bean, m.group(1));
            if (beanProperty != null) {
                newProviderConfig.replace(m.start(), m.end(), beanProperty.toString());
                m = variables.matcher(newProviderConfig);
            }
        }
        newProviderConfig.insert(0,'\n');
        newProviderConfig.insert(0,config);
        settings.setConfig(newProviderConfig.toString());
        esigateService.store(settings);
        return "success";
    }

    public void setEsigateService(EsigateService esigateService) {
        this.esigateService = esigateService;
    }
}
