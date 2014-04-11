package org.jahia.modules.portalFactory.esigate;

import org.jahia.api.Constants;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

/**
 * Created by kevan on 10/04/14.
 */
public class EsigateService implements InitializingBean{

    private static Logger logger = LoggerFactory.getLogger(EsigateService.class);
    private EsigateSettings settings;

    public boolean isEnabled(){
        return settings.isServiceEnabled();
    }

    public void store(final EsigateSettings cfg){
        try {
            // store mail settings
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
                    } catch (PathNotFoundException e) {
                        cfg.setServiceEnabled(false);
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

    public EsigateSettings getSettings() {
        return settings;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        load();
    }
}
