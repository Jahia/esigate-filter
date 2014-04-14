package org.jahia.modules.portalFactory.esigate;

import java.io.Serializable;

/**
 * Created by kevan on 10/04/14.
 */
public class EsigateSettings implements Serializable{
    private static final long serialVersionUID = -1705739194553241923L;

    private boolean serviceEnabled;
    private String config;

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public boolean isServiceEnabled() {
        return serviceEnabled;
    }

    public void setServiceEnabled(boolean serviceEnabled) {
        this.serviceEnabled = serviceEnabled;
    }
}
