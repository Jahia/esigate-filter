/*
 * Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
