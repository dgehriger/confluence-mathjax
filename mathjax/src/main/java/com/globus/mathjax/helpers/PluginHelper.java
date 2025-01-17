package com.globus.mathjax.helpers;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheLoader;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CacheSettingsBuilder;
import com.atlassian.plugin.spring.scanner.annotation.imports.ConfluenceImport;
import com.atlassian.renderer.v2.components.HtmlEscaper;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.globus.mathjax.rest.RestAdminConfigService;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;

// Helper functions for use throughout the plugin
@Component
public class PluginHelper {
  private static final double PNG_SCALING_FACTOR = 10.0;
  private final PluginSettingsFactory pluginSettingsFactory;
  private final TransactionTemplate transactionTemplate;
  private final Cache<String, String> svgCache;
  private final Cache<String, PngImageData> pngCache;
  private String m_url;
  private String m_serverUrl;
  private String m_inlineMathjaxStartIdentifier;
  private String m_inlineMathjaxEndIdentifier;
  private String m_blockMathjaxStartIdentifier;
  private String m_blockMathjaxEndIdentifier;
  private String m_mathjaxAsciiMathStartIdentifier;
  private String m_mathjaxAsciiMathEndIdentifier;
  private static String INLINE_MATHJAX_START_IDENTIFIER =
      "inlineMathjaxStartIdentifier";
  private static String INLINE_MATHJAX_END_IDENTIFIER =
      "inlineMathjaxEndIdentifier";
  private static String BLOCK_MATHJAX_START_IDENTIFIER =
      "blockMathjaxStartIdentifier";
  private static String BLOCK_MATHJAX_END_IDENTIFIER =
      "blockMathjaxEndIdentifier";
  private static String MATHJAX_ASCII_MATH_START_IDENTIFIER =
      "mathjaxAsciiMathStartIdentifier";
  private static String MATHJAX_ASCII_MATH_END_IDENTIFIER =
      "mathjaxAsciiMathEndIdentifier";

  private static final Logger log = LoggerFactory.getLogger(PluginHelper.class);

  @Autowired
  public PluginHelper(PluginSettingsFactory pluginSettingsFactory,
                      TransactionTemplate transactionTemplate,
                      @ConfluenceImport CacheManager cacheManager) {
    this.pluginSettingsFactory = pluginSettingsFactory;
    this.transactionTemplate = transactionTemplate;
    this.svgCache = cacheManager.getCache(
        PluginHelper.class.getName() + ".svgCache", new MathJaxSvgCacheLoader(),
        new CacheSettingsBuilder()
            .expireAfterAccess(3, TimeUnit.HOURS)
            .build());
    this.pngCache =
        cacheManager.getCache(PluginHelper.class.getName() + ".pngBinaryCache",
                              new MathJaxPngCacheLoader(),
                              new CacheSettingsBuilder()
                                  .expireAfterAccess(3, TimeUnit.HOURS)
                                  .build());

    PluginSettings pluginSettings =
        this.pluginSettingsFactory.createGlobalSettings();
    UrlValidator urlValidator = new UrlValidator();
    String url;
    String serverUrl;
    String inlineMathjaxStartIdentifier;
    String inlineMathjaxEndIdentifier;
    String blockMathjaxStartIdentifier;
    String blockMathjaxEndIdentifier;
    String mathjaxAsciiMathStartIdentifier;
    String mathjaxAsciiMathEndIdentifier;

    try {
      url = (String)pluginSettings.get(
          RestAdminConfigService.Config.class.getName() + ".url");
      if (urlValidator.isValid(url)) {
        this.setUrl(url);
      } else {
        this.setUrl(Constants.DEFAULT_URL);
      }
    } catch (Exception exception) {
      try {
        this.setUrl(Constants.DEFAULT_URL);
      } catch (Exception exception2) {
        log.warn("Failed to save default url value to the plugin settings: " +
                 exception2.getMessage());
      }
    }

    try {
      serverUrl = (String)pluginSettings.get(
          RestAdminConfigService.Config.class.getName() + ".serverUrl");
      if (urlValidator.isValid(serverUrl)) {
        this.setServerUrl(serverUrl);
      } else {
        this.setServerUrl(Constants.DEFAULT_SERVER_URL);
      }
    } catch (Exception exception) {
      try {
        this.setServerUrl(Constants.DEFAULT_SERVER_URL);
      } catch (Exception exception2) {
        log.warn(
            "Failed to save default server url value to the plugin settings: " +
            exception2.getMessage());
      }
    }

    try {
      inlineMathjaxStartIdentifier = (String)pluginSettings.get(
          RestAdminConfigService.Config.class.getName() + "." +
          INLINE_MATHJAX_START_IDENTIFIER);
      if (inlineMathjaxStartIdentifier != null && !inlineMathjaxStartIdentifier.isBlank()) {
        this.setInlineMathjaxStartIdentifier(inlineMathjaxStartIdentifier);
      } else {
        this.setInlineMathjaxStartIdentifier(
            Constants.DEFAULT_INLINE_MATHJAX_START_IDENTIFIER);
      }
    } catch (Exception exception) {
      try {
        this.setInlineMathjaxStartIdentifier(
            Constants.DEFAULT_INLINE_MATHJAX_START_IDENTIFIER);
      } catch (Exception exception2) {
        log.warn(
            "Failed to save default inline start identifier value to the plugin settings: " +
            exception2.getMessage());
      }
    }

    try {
      inlineMathjaxEndIdentifier = (String)pluginSettings.get(
          RestAdminConfigService.Config.class.getName() + "." +
          INLINE_MATHJAX_END_IDENTIFIER);
      if (inlineMathjaxEndIdentifier != null && !inlineMathjaxEndIdentifier.isBlank()) {
        this.setInlineMathjaxEndIdentifier(inlineMathjaxEndIdentifier);
      } else {
        this.setInlineMathjaxEndIdentifier(
            Constants.DEFAULT_INLINE_MATHJAX_END_IDENTIFIER);
      }
    } catch (Exception exception) {
      try {
        this.setInlineMathjaxEndIdentifier(
            Constants.DEFAULT_INLINE_MATHJAX_END_IDENTIFIER);
      } catch (Exception exception2) {
        log.warn(
            "Failed to save default inline end identifier value to the plugin settings: " +
            exception2.getMessage());
      }
    }

    try {
      blockMathjaxStartIdentifier = (String)pluginSettings.get(
          RestAdminConfigService.Config.class.getName() + "." +
          BLOCK_MATHJAX_START_IDENTIFIER);
      if (blockMathjaxStartIdentifier != null && !blockMathjaxStartIdentifier.isBlank()) {
        this.setBlockMathjaxStartIdentifier(blockMathjaxStartIdentifier);
      } else {
        this.setBlockMathjaxStartIdentifier(
            Constants.DEFAULT_BLOCK_MATHJAX_START_IDENTIFIER);
      }
    } catch (Exception exception) {
      try {
        this.setBlockMathjaxStartIdentifier(
            Constants.DEFAULT_BLOCK_MATHJAX_START_IDENTIFIER);
      } catch (Exception exception2) {
        log.warn(
            "Failed to save default block start identifier value to the plugin settings: " +
            exception2.getMessage());
      }
    }

    try {
      blockMathjaxEndIdentifier = (String)pluginSettings.get(
          RestAdminConfigService.Config.class.getName() + "." +
          BLOCK_MATHJAX_END_IDENTIFIER);
      if (blockMathjaxEndIdentifier != null && !blockMathjaxEndIdentifier.isBlank()) {
        this.setBlockMathjaxEndIdentifier(blockMathjaxEndIdentifier);
      } else {
        this.setBlockMathjaxEndIdentifier(
            Constants.DEFAULT_BLOCK_MATHJAX_END_IDENTIFIER);
      }
    } catch (Exception exception) {
      try {
        this.setBlockMathjaxEndIdentifier(
            Constants.DEFAULT_BLOCK_MATHJAX_END_IDENTIFIER);
      } catch (Exception exception2) {
        log.warn(
            "Failed to save default block end identifier value to the plugin settings: " +
            exception2.getMessage());
      }
    }

    try {
      mathjaxAsciiMathStartIdentifier = (String)pluginSettings.get(
          RestAdminConfigService.Config.class.getName() + "." +
          MATHJAX_ASCII_MATH_START_IDENTIFIER);
      if (mathjaxAsciiMathStartIdentifier != null && !mathjaxAsciiMathStartIdentifier.isBlank()) {
        this.setMathjaxAsciiMathStartIdentifier(
            mathjaxAsciiMathStartIdentifier);
      } else {
        this.setMathjaxAsciiMathStartIdentifier(
            Constants.DEFAULT_MATHJAX_ASCII_MATH_START_IDENTIFIER);
      }
    } catch (Exception exception) {
      try {
        this.setMathjaxAsciiMathStartIdentifier(
            Constants.DEFAULT_MATHJAX_ASCII_MATH_START_IDENTIFIER);
      } catch (Exception exception2) {
        log.warn(
            "Failed to save default ascii math start identifier value to the plugin settings: " +
            exception2.getMessage());
      }
    }

    try {
      mathjaxAsciiMathEndIdentifier = (String)pluginSettings.get(
          RestAdminConfigService.Config.class.getName() + "." +
          MATHJAX_ASCII_MATH_END_IDENTIFIER);
      if (mathjaxAsciiMathEndIdentifier != null && !mathjaxAsciiMathEndIdentifier.isBlank()) {
        this.setMathjaxAsciiMathEndIdentifier(mathjaxAsciiMathEndIdentifier);
      } else {
        this.setMathjaxAsciiMathEndIdentifier(
            Constants.DEFAULT_MATHJAX_ASCII_MATH_END_IDENTIFIER);
      }
    } catch (Exception exception) {
      try {
        this.setMathjaxAsciiMathEndIdentifier(
            Constants.DEFAULT_MATHJAX_ASCII_MATH_END_IDENTIFIER);
      } catch (Exception exception2) {
        log.warn(
            "Failed to save default ascii math end identifier value to the plugin settings: " +
            exception2.getMessage());
      }
    }
  }

  public String getUrl() { return this.m_url; }
  public void setUrl(final String url) {
    this.m_url = url;
    transactionTemplate.execute(new TransactionCallback() {
      public Object doInTransaction() {
        PluginSettings pluginSettings =
            pluginSettingsFactory.createGlobalSettings();
        pluginSettings.put(
            RestAdminConfigService.Config.class.getName() + ".url", url);
        return null;
      }
    });
  }

  public String getServerUrl() { return this.m_serverUrl; }
  public void setServerUrl(final String serverUrl) {
    this.m_serverUrl = serverUrl;
    transactionTemplate.execute(new TransactionCallback() {
      public Object doInTransaction() {
        PluginSettings pluginSettings =
            pluginSettingsFactory.createGlobalSettings();
        pluginSettings.put(RestAdminConfigService.Config.class.getName() +
                               ".serverUrl",
                           serverUrl);
        return null;
      }
    });
  }

  public String getInlineMathjaxStartIdentifier() {
    return this.m_inlineMathjaxStartIdentifier;
  }
  public void setInlineMathjaxStartIdentifier(final String value) {
    if (value != null && !value.isBlank()) {
      this.m_inlineMathjaxStartIdentifier = value;
      transactionTemplate.execute(new TransactionCallback() {
        public Object doInTransaction() {
          PluginSettings pluginSettings =
              pluginSettingsFactory.createGlobalSettings();
          pluginSettings.put(RestAdminConfigService.Config.class.getName() +
                                 "." + INLINE_MATHJAX_START_IDENTIFIER,
                             value);
          return null;
        }
      });
    }
  }

  public String getInlineMathjaxEndIdentifier() {
    return this.m_inlineMathjaxEndIdentifier;
  }

  public void setInlineMathjaxEndIdentifier(final String value) {
    if (value != null && !value.isBlank()) {
      this.m_inlineMathjaxEndIdentifier = value;
      transactionTemplate.execute(new TransactionCallback() {
        public Object doInTransaction() {
          PluginSettings pluginSettings =
              pluginSettingsFactory.createGlobalSettings();
          pluginSettings.put(RestAdminConfigService.Config.class.getName() +
                                 "." + INLINE_MATHJAX_END_IDENTIFIER,
                             value);
          return null;
        }
      });
    }
  }

  public String getBlockMathjaxStartIdentifier() {
    return this.m_blockMathjaxStartIdentifier;
  }
  public void setBlockMathjaxStartIdentifier(final String value) {
    if (value != null && !value.isBlank()) {
      this.m_blockMathjaxStartIdentifier = value;
      transactionTemplate.execute(new TransactionCallback() {
        public Object doInTransaction() {
          PluginSettings pluginSettings =
              pluginSettingsFactory.createGlobalSettings();
          pluginSettings.put(RestAdminConfigService.Config.class.getName() +
                                 "." + BLOCK_MATHJAX_START_IDENTIFIER,
                             value);
          return null;
        }
      });
    }
  }

  public String getBlockMathjaxEndIdentifier() {
    return this.m_blockMathjaxEndIdentifier;
  }
  public void setBlockMathjaxEndIdentifier(final String value) {
    if (value != null && !value.isBlank()) {
      this.m_blockMathjaxEndIdentifier = value;
      transactionTemplate.execute(new TransactionCallback() {
        public Object doInTransaction() {
          PluginSettings pluginSettings =
              pluginSettingsFactory.createGlobalSettings();
          pluginSettings.put(RestAdminConfigService.Config.class.getName() +
                                 "." + BLOCK_MATHJAX_END_IDENTIFIER,
                             value);
          return null;
        }
      });
    }
  }

  public String getMathjaxAsciiMathStartIdentifier() {
    return this.m_mathjaxAsciiMathStartIdentifier;
  }
  public void setMathjaxAsciiMathStartIdentifier(final String value) {
    if (value != null && !value.isBlank()) {
      this.m_mathjaxAsciiMathStartIdentifier = value;
      transactionTemplate.execute(new TransactionCallback() {
        public Object doInTransaction() {
          PluginSettings pluginSettings =
              pluginSettingsFactory.createGlobalSettings();
          pluginSettings.put(RestAdminConfigService.Config.class.getName() +
                                 "." + MATHJAX_ASCII_MATH_START_IDENTIFIER,
                             value);
          return null;
        }
      });
    }
  }

  public String getMathjaxAsciiMathEndIdentifier() {
    return this.m_mathjaxAsciiMathEndIdentifier;
  }

  public void setMathjaxAsciiMathEndIdentifier(final String value) {
    if (value != null && !value.isBlank()) {
      this.m_mathjaxAsciiMathEndIdentifier = value;
      transactionTemplate.execute(new TransactionCallback() {
        public Object doInTransaction() {
          PluginSettings pluginSettings =
              pluginSettingsFactory.createGlobalSettings();
          pluginSettings.put(RestAdminConfigService.Config.class.getName() +
                                 "." + MATHJAX_ASCII_MATH_END_IDENTIFIER,
                             value);
          return null;
        }
      });
    }
  }

  public String generateSvgHtml(String equation, boolean isInline) {
    String svg = null;
    try {
      String encodedEquation =
          URLEncoder.encode(equation, StandardCharsets.UTF_8);
      svg = this.svgCache.get(encodedEquation);
    } catch (Exception e) {
    }

    if (svg != null) {
      if (isInline) {
        return String.format("<span>%s</span>", svg);
      } else {
        return String.format("<center><div>%s</div></center>", svg);
      }
    } else {
      String escapedEquation = HtmlEscaper.escapeAll(equation, true);
      String startIdentifier = isInline ? this.getInlineMathjaxStartIdentifier()
                                        : this.getBlockMathjaxStartIdentifier();
      String endIdentifier = isInline ? this.getInlineMathjaxEndIdentifier()
                                      : this.getBlockMathjaxEndIdentifier();
      return startIdentifier + escapedEquation + endIdentifier;
    }
  }

  private class MathJaxSvgCacheLoader implements CacheLoader<String, String> {
    @Override
    public String load(@Nonnull String encodedEquation) {
      try {
        String serverUrl = PluginHelper.this.getServerUrl() + "/svg";

        HttpRequest request =
            HttpRequest.newBuilder()
                .uri(URI.create(serverUrl))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("mathjax=" +
                                                          encodedEquation))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response =
            client.send(request, HttpResponse.BodyHandlers.ofString());
        String rawSvg = response.body();
        return rawSvg;
      } catch (IOException | InterruptedException e) {
        return null;
      }
    }
  }

  public String generatePngHtml(String equation, boolean isInline) {
    PngImageData image = null;
    try {
      String encodedEquation =
          URLEncoder.encode(equation, StandardCharsets.UTF_8);
      image = this.pngCache.get(encodedEquation);
    } catch (Exception e) {
    }

    if (image != null) {
      // Convert the byte array to a Base64 string
      String imageBase64 = Base64.getEncoder().encodeToString(image.data);
      int width = (int)(image.width / this.PNG_SCALING_FACTOR);
      int height = (int)(image.height / this.PNG_SCALING_FACTOR);
      if (isInline) {
        return String.format(
            "<span><img width='%d' height='%d' src='data:image/png;base64,%s' /></span>",
            width, height, imageBase64);
      } else {
        return String.format(
            "<center><img width='%d' height='%d' src='data:image/png;base64,%s' /></center>",
            width, height, imageBase64);
      }
    } else {
      String escapedEquation = HtmlEscaper.escapeAll(equation, true);
      String startIdentifier = isInline ? this.getInlineMathjaxStartIdentifier()
                                        : this.getBlockMathjaxStartIdentifier();
      String endIdentifier = isInline ? this.getInlineMathjaxEndIdentifier()
                                      : this.getBlockMathjaxEndIdentifier();
      return startIdentifier + escapedEquation + endIdentifier;
    }
  }

  private class MathJaxPngCacheLoader
      implements CacheLoader<String, PngImageData> {
    @Override
    public PngImageData load(@Nonnull String encodedEquation) {
      try {
        String serverUrl = PluginHelper.this.getServerUrl() + "/png";

        HttpRequest request =
            HttpRequest.newBuilder()
                .uri(URI.create(serverUrl))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(
                    String.format("scaling=%f&mathjax=%s", PNG_SCALING_FACTOR,
                                  encodedEquation)))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<byte[]> response =
            client.send(request, HttpResponse.BodyHandlers.ofByteArray());

        byte[] responseBody = response.body();
        return new PngImageData(responseBody);

      } catch (IOException | InterruptedException e) {
        return null;
      }
    }
  }

  private class PngImageData {
    final public byte[] data;
    final public int width;
    final public int height;

    public PngImageData(byte[] data) {
      this.data = data;

      ByteBuffer buffer = ByteBuffer.wrap(data, 16, 8);
      this.width = buffer.getInt();
      this.height = buffer.getInt();
    }
  }
}
