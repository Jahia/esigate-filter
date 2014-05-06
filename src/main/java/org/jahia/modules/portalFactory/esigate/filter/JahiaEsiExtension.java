package org.jahia.modules.portalFactory.esigate.filter;

import org.esigate.Driver;
import org.esigate.HttpErrorPage;
import org.esigate.Parameters;
import org.esigate.Renderer;
import org.esigate.events.Event;
import org.esigate.events.EventDefinition;
import org.esigate.events.EventManager;
import org.esigate.events.IEventListener;
import org.esigate.events.impl.RenderEvent;
import org.esigate.extension.Extension;
import org.esigate.impl.DriverRequest;
import org.esigate.impl.UrlRewriter;

import java.io.IOException;
import java.io.Writer;
import java.util.Properties;

/**
 * ESI Extension
 * Handles $(jahia.workspace) / $(jahia.workspace) replacement
 * Rewrite outbound URLs based on visibleBaseURL ( replaces ResourceFixup )
 * Add default ESI tags
 */
public class JahiaEsiExtension implements Extension, IEventListener {
    private UrlRewriter urlRewriter;
    @Override
    public void init(Driver driver, Properties properties) {
        driver.getEventManager().register(EventManager.EVENT_RENDER_PRE, this);
      }

    @Override
    public boolean event(EventDefinition id, Event event) {
        RenderEvent renderEvent = (RenderEvent) event;
        final String baseUrl = renderEvent.getOriginalRequest().getBaseUrl().toString();
        final String requestUrl = renderEvent.getRemoteUrl();
        renderEvent.getRenderers().add(new Renderer() {
            @Override
            public void render(DriverRequest originalRequest, String src, Writer out) throws IOException, HttpErrorPage {
                final String workspace = originalRequest.getOriginalRequest().getAttribute("jahia.workspace");
                final String language = originalRequest.getOriginalRequest().getAttribute("jahia.language");

                if (!src.contains("<esi:")) {
                    src = "<esi:include src=\"$(PROVIDER{default})/cms/$(jahia.workspace)/$(jahia.language)/sites/mySite/home.html\">" +
                            "<esi:replace fragment=\"col1\">" +
                            src +
                            "</esi:replace>" +
                            "</esi:include>";
                }
                if (src.contains("$(jahia.workspace)")) {
                    src = src.replace("$(jahia.workspace)", workspace);
                }
                if (src.contains("$(jahia.language)")) {
                    src = src.replace("$(jahia.language)", language);
                }
                String visibleBaseURL = originalRequest.getDriver().getConfiguration().getVisibleBaseURL(baseUrl);
                if (visibleBaseURL != null && !visibleBaseURL.equals(baseUrl)) {
                    visibleBaseURL = visibleBaseURL.replace("$(jahia.workspace)", workspace).replace("$(jahia.language)", language);
                    Properties p = new Properties();
                    p.setProperty(Parameters.VISIBLE_URL_BASE.getName(), visibleBaseURL);
                    UrlRewriter urlRewriter = new UrlRewriter(p);
                    out.write(urlRewriter.rewriteHtml(src, requestUrl, baseUrl).toString());
                } else {
                    out.write(src);
                }
            }
        });

        System.out.println(event.getClass());
        return true;
    }
}
