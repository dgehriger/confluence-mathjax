package com.globus.mathjax.admin;

import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;

public class AdminUI extends HttpServlet {
    private final UserManager userManager;
    private final LoginUriProvider loginUriProvider;
    private final TemplateRenderer renderer;

    public AdminUI(UserManager userManager, LoginUriProvider loginUriProvider, TemplateRenderer renderer) {
        this.userManager = userManager;
        this.loginUriProvider = loginUriProvider;
        this.renderer = renderer;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        UserKey userkey = userManager.getRemoteUserKey();
        if (userkey == null || !userManager.isSystemAdmin(userkey)) {
            redirectToLogin(req, res);
            return;
        }

        res.setContentType("text/html;charset=utf-8");
        renderer.render("com/globus/mathjax/templates/admin.vm", res.getWriter());
    }

    private void redirectToLogin(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.sendRedirect(loginUriProvider.getLoginUri(getUri(req)).toASCIIString());
    }

    private URI getUri(HttpServletRequest req) {
        StringBuilder builder = new StringBuilder(req.getRequestURL());
        if (req.getQueryString() != null) {
            builder.append("?");
            builder.append(req.getQueryString());
        }

        return URI.create(builder.toString());
    }
}
