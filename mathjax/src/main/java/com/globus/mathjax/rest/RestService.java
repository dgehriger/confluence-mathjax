package com.globus.mathjax.rest;

import com.atlassian.confluence.plugin.services.VelocityHelperService;
import com.atlassian.confluence.setup.settings.SettingsManager;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

@Path("/")
public class RestService {
    private final SettingsManager settingsManager;
    private final VelocityHelperService velocityHelperService;

    public RestService(SettingsManager settingsManager,
                       VelocityHelperService velocityHelperService
    ) {
        this.settingsManager = settingsManager;
        this.velocityHelperService = velocityHelperService;
    }

    @GET
    @Path("help/mathjax-inline")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response mathJaxInlineHelp() {
        String title = "MathJax Inline Equation";
        String bodyTemplate = "/com/globus/mathjax/templates/mathjax-help.vm";

        return getMacroHelp(title, bodyTemplate);
    }

    @GET
    @Path("help/mathjax-block")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response mathJaxBlockHelp() {
        String title = "MathJax Block Equation";
        String bodyTemplate = "/com/globus/mathjax/templates/mathjax-help.vm";

        return getMacroHelp(title, bodyTemplate);
    }

    private Response getMacroHelp(String title, String bodyTemplate) {
        StringBuilder html = new StringBuilder();
        String headerTemplate = "/com/globus/mathjax/templates/help-header.vm";
        String footerTemplate = "/com/globus/mathjax/templates/help-footer.vm";
        String fossTemplate = "/com/globus/mathjax/templates/foss.vm";

        Map<String, Object> velocityContext = velocityHelperService.createDefaultVelocityContext();
        velocityContext.put("title", title);
        velocityContext.put("baseUrl", settingsManager.getGlobalSettings().getBaseUrl());

        html.append(velocityHelperService.getRenderedTemplate(headerTemplate, velocityContext));
        html.append(velocityHelperService.getRenderedTemplate(bodyTemplate, velocityContext));
        html.append(velocityHelperService.getRenderedTemplate(fossTemplate, velocityContext));
        html.append(velocityHelperService.getRenderedTemplate(footerTemplate, velocityContext));

        return Response.ok(new RestResponse(html.toString())).build();
    }
}
