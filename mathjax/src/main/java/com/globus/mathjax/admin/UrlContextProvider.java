package com.globus.mathjax.admin;

import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.ContextProvider;
import com.globus.mathjax.helpers.PluginHelper;

import java.util.Map;

public class UrlContextProvider implements ContextProvider {
    private final PluginHelper pluginHelper;
    private static String URL = "url";
    private static String SERVER_URL = "serverUrl";
    private static String INLINE_MATHJAX_START_IDENTIFIER = "inlineMathjaxStartIdentifier";
    private static String INLINE_MATHJAX_END_IDENTIFIER = "inlineMathjaxEndIdentifier";
    private static String BLOCK_MATHJAX_START_IDENTIFIER = "blockMathjaxStartIdentifier";
    private static String BLOCK_MATHJAX_END_IDENTIFIER = "blockMathjaxEndIdentifier";
    private static String MATHJAX_ASCII_MATH_START_IDENTIFIER = "mathjaxAsciiMathStartIdentifier";
    private static String MATHJAX_ASCII_MATH_END_IDENTIFIER = "mathjaxAsciiMathEndIdentifier";

    public UrlContextProvider(PluginHelper pluginHelper){
        this.pluginHelper = pluginHelper;
    }

    public void init(Map<String, String> params) throws PluginParseException {
    }

    public Map<String, Object> getContextMap(Map<String, Object> context) {
        context.put( URL, pluginHelper.getUrl() );
        context.put( SERVER_URL, pluginHelper.getServerUrl() );
        context.put( INLINE_MATHJAX_START_IDENTIFIER, pluginHelper.getInlineMathjaxStartIdentifier().replace("\\", "\\\\") );
        context.put( INLINE_MATHJAX_END_IDENTIFIER, pluginHelper.getInlineMathjaxEndIdentifier().replace("\\", "\\\\") );
        context.put( BLOCK_MATHJAX_START_IDENTIFIER, pluginHelper.getBlockMathjaxStartIdentifier().replace("\\", "\\\\") );
        context.put( BLOCK_MATHJAX_END_IDENTIFIER, pluginHelper.getBlockMathjaxEndIdentifier().replace("\\", "\\\\") );
        context.put( MATHJAX_ASCII_MATH_START_IDENTIFIER, pluginHelper.getMathjaxAsciiMathStartIdentifier().replace("\\", "\\\\") );
        context.put( MATHJAX_ASCII_MATH_END_IDENTIFIER, pluginHelper.getMathjaxAsciiMathEndIdentifier().replace("\\", "\\\\") );
        return context;
    }
}
