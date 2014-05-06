package org.jahia.modules.portalFactory.esigate.flow;

import org.jahia.modules.portalFactory.esigate.EsigateService;
import org.jahia.modules.portalFactory.esigate.EsigateSettings;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

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

    public void setEsigateService(EsigateService esigateService) {
        this.esigateService = esigateService;
    }
}
