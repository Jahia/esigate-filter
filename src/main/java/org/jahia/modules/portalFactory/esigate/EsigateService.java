package org.jahia.modules.portalFactory.esigate;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.apache.commons.lang.StringUtils;
import org.esigate.DriverFactory;
import org.jahia.api.Constants;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Properties;

/**
 * Created by kevan on 10/04/14.
 */
public class EsigateService implements InitializingBean {

    private static Logger logger = LoggerFactory.getLogger(EsigateService.class);

    JahiaTemplateManagerService jahiaTemplateManagerService;

    private EsigateSettings settings;

    public boolean isEnabled() {
        return settings.isServiceEnabled();
    }

    public void store(final EsigateSettings cfg) {
        try {
            // store esigate settings
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    store(cfg, session);
                    return Boolean.TRUE;
                }
            });
            load();
        } catch (RepositoryException e) {
            logger.error("Error storing esigate server settings into the repository.", e);
        }
    }

    public void reloadDefault() {
        EsigateSettings defaultSettings = new EsigateSettings();
        defaultSettings.setServiceEnabled(false);
        defaultSettings.setConfig(getDefaultConfig());
        store(defaultSettings);
    }

    protected void store(EsigateSettings cfg, JCRSessionWrapper session) throws RepositoryException {
        JCRNodeWrapper esigateNode = null;

        try {
            esigateNode = session.getNode("/settings/esigate-server");
        } catch (PathNotFoundException e) {
            if (session.nodeExists("/settings")) {
                esigateNode = session.getNode("/settings").addNode("esigate-server",
                        "jnt:esigateServerSettings");
            } else {
                esigateNode = session.getNode("/").addNode("settings", "jnt:globalSettings")
                        .addNode("esigate-server", "jnt:esigateServerSettings");
            }
        }

        esigateNode.setProperty("j:enabled", cfg.isServiceEnabled());
        try {
            Properties oldProperties = stringToProperties(esigateNode.getPropertyAsString("j:config"));
            Properties newProperties = stringToProperties(cfg.getConfig());
            if (!oldProperties.equals(newProperties)) {
                esigateNode.setProperty("j:config", cfg.getConfig());
                DriverFactory.configure(newProperties);
            }
        } catch (IOException e) {
            logger.error("Unable to transform string to properties", e);
        }

        session.save();
    }

    protected void load() {
        settings = new EsigateSettings();
        try {
            // read mail settings
            settings = JCRTemplate.getInstance().doExecuteWithSystemSession(null, Constants.EDIT_WORKSPACE, new JCRCallback<EsigateSettings>() {
                public EsigateSettings doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    EsigateSettings cfg = new EsigateSettings();

                    JCRNodeWrapper esigateNode = null;
                    try {
                        esigateNode = session.getNode("/settings/esigate-server");
                        cfg.setServiceEnabled(esigateNode.hasProperty("j:enabled")
                                && esigateNode.getProperty("j:enabled").getBoolean());
                        cfg.setConfig(esigateNode.getPropertyAsString("j:config"));
                    } catch (PathNotFoundException e) {
                        cfg.setServiceEnabled(false);
                        cfg.setConfig(getDefaultConfig());
                        store(cfg, session);

                    }

                    return cfg;
                }
            });
        } catch (RepositoryException e) {
            logger.error("Error reading Esigate settings from the repository."
                    + " Esigate will be disabled.", e);
        }
    }

    private String getDefaultConfig() {
        JahiaTemplatesPackage jahiaTemplatesPackage = getCurrentJahiaTemplatePackage();
        URL configUrl = jahiaTemplatesPackage.getBundle().getResource("jahia-esigate.properties");

        try {
            return Resources.toString(configUrl, Charsets.UTF_8);
        } catch (IOException e) {
            logger.error("Unable to load default esigate properties from esigate.properties file", e);
            return null;
        }
    }

    public static String propertiesToString(Properties properties) {
        StringWriter writer = new StringWriter();
        try {
            properties.store(writer, "");
        } catch (IOException e) {
            logger.error("Unable to transform properties to string", e);
        }
        return writer.getBuffer().toString();
    }

    public static Properties stringToProperties(String str) throws IOException {
        final Properties properties = new Properties();
        if (StringUtils.isNotEmpty(str)) {
            properties.load(new StringReader(str));
        }
        return properties;
    }

    private JahiaTemplatesPackage getCurrentJahiaTemplatePackage() {
        return jahiaTemplateManagerService.getTemplatePackageRegistry().lookupById("portal-factory_esigate-filter");
    }

    public EsigateSettings getSettings() {
        return settings;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        load();
    }

    public void setJahiaTemplateManagerService(JahiaTemplateManagerService jahiaTemplateManagerService) {
        this.jahiaTemplateManagerService = jahiaTemplateManagerService;
    }
}
