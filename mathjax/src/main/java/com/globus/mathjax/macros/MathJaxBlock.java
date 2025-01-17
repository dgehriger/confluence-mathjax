package com.globus.mathjax.macros;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.content.render.xhtml.ConversionContextOutputType;
import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.confluence.plugin.services.VelocityHelperService;
import com.atlassian.confluence.xhtml.api.XhtmlContent;
import com.globus.mathjax.helpers.PluginHelper;
import java.util.Map;

public class MathJaxBlock implements Macro {
  public static final String EQUATION = "equation";
  public static final String BODY_AS_HTML = "bodyAsHtml";
  protected final VelocityHelperService velocityHelperService;
  protected final PluginHelper pluginHelper;

  public MathJaxBlock(PluginHelper pluginHelper,
      VelocityHelperService velocityHelperService) {
    this.pluginHelper = pluginHelper;
    this.velocityHelperService = velocityHelperService;
  }

  @Override
  public String execute(Map<String, String> parameters, String equation,
      ConversionContext context)
      throws MacroExecutionException {
    String html = "No equation provided";
    if (!equation.isEmpty()) {
      String outputType = context.getOutputType();
      if (ConversionContextOutputType.PREVIEW.value().equalsIgnoreCase(
          outputType) ||
          ConversionContextOutputType.DISPLAY.value().equalsIgnoreCase(
              outputType)
          ||
          "com.k15t.scroll.scroll-office".equals(
              context.getProperty("com.k15t.scroll.product.key"))
          ||
          "com.k15t.scroll.scroll-pdf".equals(
              context.getProperty("com.k15t.scroll.product.key"))) {
        html = pluginHelper.generateSvgHtml(equation, false);
      } else {
        html = pluginHelper.generatePngHtml(equation, false);
      }
    }

    String template = "/com/globus/mathjax/templates/mathjax-block.vm";
    Map<String, Object> velocityContext = velocityHelperService.createDefaultVelocityContext();

    velocityContext.put(BODY_AS_HTML, html);
    return velocityHelperService.getRenderedTemplate(template, velocityContext);
  }

  @Override
  public BodyType getBodyType() {
    return BodyType.PLAIN_TEXT;
  }

  @Override
  public OutputType getOutputType() {
    return OutputType.BLOCK;
  }
}
