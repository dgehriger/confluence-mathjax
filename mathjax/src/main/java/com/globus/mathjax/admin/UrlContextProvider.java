package com.globus.mathjax.admin;

import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.ContextProvider;
import com.globus.mathjax.helpers.PluginHelper;

import java.util.Map;

public class UrlContextProvider implements ContextProvider {
    private final PluginHelper pluginHelper;
    private static final String URL = "url";
    private static final String SERVER_URL = "serverUrl";
    private static final String INLINE_MATHJAX_START_IDENTIFIER = "inlineMathjaxStartIdentifier";
    private static final String INLINE_MATHJAX_END_IDENTIFIER = "inlineMathjaxEndIdentifier";
    private static final String BLOCK_MATHJAX_START_IDENTIFIER = "blockMathjaxStartIdentifier";
    private static final String BLOCK_MATHJAX_END_IDENTIFIER = "blockMathjaxEndIdentifier";
    private static final String MATHJAX_ASCII_MATH_START_IDENTIFIER = "mathjaxAsciiMathStartIdentifier";
    private static final String MATHJAX_ASCII_MATH_END_IDENTIFIER = "mathjaxAsciiMathEndIdentifier";

    public UrlContextProvider(PluginHelper pluginHelper) {
        this.pluginHelper = pluginHelper;
    }

    public void init(Map<String, String> params) throws PluginParseException {
        // This method is intentionally left empty because no initialization is
        // required.
    }

    public Map<String, Object> getContextMap(Map<String, Object> context) {
        context.put(URL, pluginHelper.getUrl());
        context.put(SERVER_URL, pluginHelper.getServerUrl());
        context.put(INLINE_MATHJAX_START_IDENTIFIER,
                pluginHelper.getInlineMathjaxStartIdentifier().replace("\\", "\\\\"));
        context.put(INLINE_MATHJAX_END_IDENTIFIER, pluginHelper.getInlineMathjaxEndIdentifier().replace("\\", "\\\\"));
        context.put(BLOCK_MATHJAX_START_IDENTIFIER,
                pluginHelper.getBlockMathjaxStartIdentifier().replace("\\", "\\\\"));
        context.put(BLOCK_MATHJAX_END_IDENTIFIER, pluginHelper.getBlockMathjaxEndIdentifier().replace("\\", "\\\\"));
        context.put(MATHJAX_ASCII_MATH_START_IDENTIFIER,
                pluginHelper.getMathjaxAsciiMathStartIdentifier().replace("\\", "\\\\"));
        context.put(MATHJAX_ASCII_MATH_END_IDENTIFIER,
                pluginHelper.getMathjaxAsciiMathEndIdentifier().replace("\\", "\\\\"));
        return context;
    }
}
