package com.globus.mathjax.rest;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.globus.mathjax.helpers.PluginHelper;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.globus.mathjax.helpers.PluginHelper;

@Path("/admin/")
public class RestAdminConfigService {

    private final PluginSettingsFactory pluginSettingsFactory;
    private final TransactionTemplate transactionTemplate;
    private final UserManager userManager;
    private final PluginHelper pluginHelper;

    public RestAdminConfigService(PluginSettingsFactory pluginSettingsFactory,
                                  PluginHelper pluginHelper,
                                  TransactionTemplate transactionTemplate,
                                  UserManager userManager) {
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.pluginHelper = pluginHelper;
        this.transactionTemplate = transactionTemplate;
        this.userManager = userManager;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@Context HttpServletRequest request) {
        UserKey userkey = userManager.getRemoteUserKey(request);
        if (userkey == null || !userManager.isSystemAdmin(userkey)) {
            return Response.status(Status.UNAUTHORIZED).build();
        }

        return Response.ok(transactionTemplate.execute(new TransactionCallback() {
            public Object doInTransaction() {
                RestAdminConfigService.Config config = new RestAdminConfigService.Config();
                config.setUrl( pluginHelper.getUrl() );
                config.setServerUrl( pluginHelper.getServerUrl() );
                config.setInlineMathjaxStartIdentifier( pluginHelper.getInlineMathjaxStartIdentifier() );
                config.setInlineMathjaxEndIdentifier( pluginHelper.getInlineMathjaxEndIdentifier() );
                config.setBlockMathjaxStartIdentifier( pluginHelper.getBlockMathjaxStartIdentifier() );
                config.setBlockMathjaxEndIdentifier( pluginHelper.getBlockMathjaxEndIdentifier() );
                config.setMathjaxAsciiMathStartIdentifier( pluginHelper.getMathjaxAsciiMathStartIdentifier() );
                config.setMathjaxAsciiMathEndIdentifier( pluginHelper.getMathjaxAsciiMathEndIdentifier() );
                return config;
            }
        })).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response put(final Config config, @Context HttpServletRequest request) {
        UserKey userkey = userManager.getRemoteUserKey(request);
        if (userkey == null || !userManager.isSystemAdmin(userkey)) {
            return Response.status(Status.UNAUTHORIZED).build();
        }

        pluginHelper.setUrl(config.getUrl());
        pluginHelper.setServerUrl(config.getServerUrl());
        pluginHelper.setInlineMathjaxStartIdentifier(config.getInlineMathjaxStartIdentifier());
        pluginHelper.setInlineMathjaxEndIdentifier(config.getInlineMathjaxEndIdentifier());
        pluginHelper.setBlockMathjaxStartIdentifier(config.getBlockMathjaxStartIdentifier());
        pluginHelper.setBlockMathjaxEndIdentifier(config.getBlockMathjaxEndIdentifier());
        pluginHelper.setMathjaxAsciiMathStartIdentifier(config.getMathjaxAsciiMathStartIdentifier());
        pluginHelper.setMathjaxAsciiMathEndIdentifier(config.getMathjaxAsciiMathEndIdentifier());
        return Response.noContent().build();
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class Config {
        @XmlElement
        private String url;
        private String serverUrl;
        private String inlineMathjaxStartIdentifier;
        private String inlineMathjaxEndIdentifier;
        private String blockMathjaxStartIdentifier;
        private String blockMathjaxEndIdentifier;
        private String mathjaxAsciiMathStartIdentifier;
        private String mathjaxAsciiMathEndIdentifier;

        public String getUrl() {
            return url;
        }
        public void setUrl(String url) {
            this.url = url;
        }

        public String getServerUrl() {
            return serverUrl;
        }
        public void setServerUrl(String value) {
            this.serverUrl = value;
        }

        public String getInlineMathjaxStartIdentifier() {
            return inlineMathjaxStartIdentifier;
        }
        public void setInlineMathjaxStartIdentifier(String value) {
            this.inlineMathjaxStartIdentifier = value;
        }

        public String getInlineMathjaxEndIdentifier() {
            return inlineMathjaxEndIdentifier;
        }
        public void setInlineMathjaxEndIdentifier(String value) {
            this.inlineMathjaxEndIdentifier = value;
        }

        public String getBlockMathjaxStartIdentifier() {
            return blockMathjaxStartIdentifier;
        }
        public void setBlockMathjaxStartIdentifier(String value) {
            this.blockMathjaxStartIdentifier = value;
        }

        public String getBlockMathjaxEndIdentifier() {
            return blockMathjaxEndIdentifier;
        }
        public void setBlockMathjaxEndIdentifier(String value) {
            this.blockMathjaxEndIdentifier = value;
        }

        public String getMathjaxAsciiMathStartIdentifier() { return mathjaxAsciiMathStartIdentifier; }
        public void setMathjaxAsciiMathStartIdentifier(String value) { this.mathjaxAsciiMathStartIdentifier = value; }

        public String getMathjaxAsciiMathEndIdentifier() { return mathjaxAsciiMathEndIdentifier; }
        public void setMathjaxAsciiMathEndIdentifier(String value) { this.mathjaxAsciiMathEndIdentifier = value; }
    }
}
