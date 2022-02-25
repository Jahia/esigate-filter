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
import org.esigate.vars.VariablesResolver;

import java.io.IOException;
import java.io.Writer;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ESI Extension
 * Handles $(jahia.mode) / $(jahia.workspace) replacement
 * Rewrite outbound URLs based on visibleBaseURL ( replaces ResourceFixup )
 * Add default ESI tags
 */
public class JahiaEsiExtension implements Extension, IEventListener {
    private String defaultPageInclude;
    private String defaultFragmentReplace;
    private Pattern esiPattern = Pattern.compile("<esi:[^>]+>");
    private Pattern varPattern = Pattern.compile("\\$\\(jahia\\.[^)]+\\)");

    @Override
    public void init(Driver driver, Properties properties) {
        driver.getEventManager().register(EventManager.EVENT_RENDER_PRE, this);
        defaultPageInclude = properties.getProperty("defaultPageInclude");
        defaultFragmentReplace = properties.getProperty("defaultFragmentReplace");
    }

    @Override
    public boolean event(EventDefinition id, Event event) {
        RenderEvent renderEvent = (RenderEvent) event;
        final String baseUrl = renderEvent.getOriginalRequest().getBaseUrl().toString();
        final String requestUrl = renderEvent.getRemoteUrl();
        renderEvent.getRenderers().add(0, new Renderer() {
            @Override
            public void render(DriverRequest originalRequest, String src, Writer out) throws IOException, HttpErrorPage {
                final String mode = originalRequest.getOriginalRequest().getAttribute("jahia.mode");
                final String language = originalRequest.getOriginalRequest().getAttribute("jahia.language");
                if (!src.contains("<esi:") && defaultPageInclude != null) {
                    src = "<esi:include src=\"$(PROVIDER{default})/cms/$(jahia.mode)/$(jahia.language)" + defaultPageInclude + "\">" +
                            "<esi:replace fragment=\"" + defaultFragmentReplace + "\">" +
                            src +
                            "</esi:replace>" +
                            "</esi:include>";
                }

                StringBuffer sb1 = new StringBuffer();
                final Matcher esiMatcher = esiPattern.matcher(src);
                while (esiMatcher.find()) {
                    String esiTag = esiMatcher.group();
                    Matcher varMatcher = varPattern.matcher(esiTag);
                    StringBuffer sb2 = new StringBuffer();
                    while (varMatcher.find()) {
                        if (varMatcher.group().equals("$(jahia.mode)")) {
                            varMatcher.appendReplacement(sb2, mode);
                        } else if (varMatcher.group().equals("$(jahia.language)")) {
                            varMatcher.appendReplacement(sb2, language);
                        }
                    }
                    varMatcher.appendTail(sb2);
                    String replaced = VariablesResolver.replaceAllVariables(sb2.toString(), originalRequest);
                    esiMatcher.appendReplacement(sb1, Matcher.quoteReplacement(replaced));
                }
                esiMatcher.appendTail(sb1);
                src = sb1.toString();

                // if it's contain a esi:include tag it's an external app fragment, URLs need to be rewrite
                // if not it's include page fragment coming from main app, URLs have to stay the same
                String visibleBaseURL = originalRequest.getDriver().getConfiguration().getVisibleBaseURL(baseUrl);
                if (visibleBaseURL != null && !visibleBaseURL.equals(baseUrl) && src.contains("<esi:include")) {
                    visibleBaseURL = visibleBaseURL.replace("$(jahia.mode)", mode).replace("$(jahia.language)", language);
                    Properties p = new Properties();
                    p.setProperty(Parameters.VISIBLE_URL_BASE.getName(), visibleBaseURL);
                    UrlRewriter urlRewriter = new UrlRewriter(p);
                    out.write(urlRewriter.rewriteHtml(src, requestUrl, baseUrl).toString());
                } else {
                    out.write(src);
                }
            }
        });
        return true;
    }
}
