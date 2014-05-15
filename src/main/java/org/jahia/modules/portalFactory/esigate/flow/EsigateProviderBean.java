package org.jahia.modules.portalFactory.esigate.flow;

import org.apache.commons.lang3.StringUtils;
import org.jahia.settings.SettingsBean;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.binding.validation.ValidationContext;

import javax.servlet.ServletContext;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

/**
 * Bean for new provider creation
 */
public class EsigateProviderBean implements Serializable {
    private String originalConfig;

    private String providerKey;
    private String remoteUrlBase;
    private String extensions = "org.jahia.modules.portalFactory.esigate.filter.JahiaEsiExtension,org.esigate.extension.Esi";
    private String defaultPageInclude = "";
    private String defaultFragmentReplace = "";

    private String context;


    private String serverName;
    private String serverPort;
    private String jahiaContext;

    public String getOriginalConfig() {
        return originalConfig;
    }

    public void setOriginalConfig(String originalConfig) {
        this.originalConfig = originalConfig;
    }

    public String getProviderKey() {
        return providerKey;
    }

    public void setProviderKey(String providerKey) {
        this.providerKey = providerKey;
    }

    public String getRemoteUrlBase() {
        return remoteUrlBase;
    }

    public void setRemoteUrlBase(String remoteUrlBase) {
        this.remoteUrlBase = remoteUrlBase;
    }

    public String getExtensions() {
        return extensions;
    }

    public void setExtensions(String extensions) {
        this.extensions = extensions;
    }

    public String getDefaultPageInclude() {
        return defaultPageInclude;
    }

    public void setDefaultPageInclude(String defaultPageInclude) {
        this.defaultPageInclude = defaultPageInclude;
    }

    public String getDefaultFragmentReplace() {
        return defaultFragmentReplace;
    }

    public void setDefaultFragmentReplace(String defaultFragmentReplace) {
        this.defaultFragmentReplace = defaultFragmentReplace;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
        if (!StringUtils.isEmpty(context) && !extensions.contains("org.esigate.servlet.ServletExtension")) {
            extensions = "org.esigate.servlet.ServletExtension," + extensions;
        } else if (StringUtils.isEmpty(context) && extensions.contains("org.esigate.servlet.ServletExtension")) {
            extensions = extensions.replace("org.esigate.servlet.ServletExtension,","");
        }
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getServerPort() {
        return serverPort;
    }

    public void setServerPort(String serverPort) {
        this.serverPort = serverPort;
    }

    public String getJahiaContext() {
        return jahiaContext;
    }

    public void setJahiaContext(String jahiaContext) {
        this.jahiaContext = jahiaContext;
    }

    public void validateViewAddProvider(ValidationContext validationContext)  {
        MessageContext messages = validationContext.getMessageContext();

        if (providerKey.isEmpty()) {
            messages.addMessage(new MessageBuilder().error().source("providerKey").
                    code("serverSettings.esigateSettings.providerKey.notEmpty").build());
        } else if (!Pattern.compile("[A-Za-z0-9]+").matcher(providerKey).matches()) {
            messages.addMessage(new MessageBuilder().error().source("providerKey").
                    code("serverSettings.esigateSettings.providerKey.lettersOnly").build());
        } else if (originalConfig.contains(providerKey + ".remoteUrlBase=")) {
            messages.addMessage(new MessageBuilder().error().source("providerKey").
                    code("serverSettings.esigateSettings.providerKey.alreadyExists").build());
        }
        if (remoteUrlBase.isEmpty()) {
            messages.addMessage(new MessageBuilder().error().source("remoteUrlBase").
                    code("serverSettings.esigateSettings.remoteUrlBase.notEmpty").build());
        } else if (!remoteUrlBase.endsWith("/")) {
            messages.addMessage(new MessageBuilder().error().source("remoteUrlBase").
                    code("serverSettings.esigateSettings.remoteUrlBase.endsWithSlash").build());
        } else {
            try {
                new URL(remoteUrlBase);
            } catch (MalformedURLException e) {
                messages.addMessage(new MessageBuilder().error().source("remoteUrlBase").
                        code("serverSettings.esigateSettings.remoteUrlBase.malformed").build());
            }
        }
        if (!context.isEmpty()) {
            ServletContext crossContext = SettingsBean.getInstance().getServletContext().getContext(context);
            if (crossContext == null || crossContext == SettingsBean.getInstance().getServletContext()) {
                messages.addMessage(new MessageBuilder().error().source("context").
                        code("serverSettings.esigateSettings.crossContext.invalid").build());
            }
        }

    }
}
