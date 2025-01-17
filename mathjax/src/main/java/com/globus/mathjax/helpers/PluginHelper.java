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
import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

// Helper functions for use throughout the plugin
@Component
public class PluginHelper {
  private static final double PNG_SCALING_FACTOR = 10.0;
  private final PluginSettingsFactory pluginSettingsFactory;
  private final TransactionTemplate transactionTemplate;
  private final Cache<String, String> svgCache;
  private final Cache<String, PngImageData> pngCache;
  private String urlEndPoint;
  private String serverUrl;
  private String inlineMathjaxStartIdentifier;
  private String inlineMathjaxEndIdentifier;
  private String blockMathjaxStartIdentifier;
  private String blockMathjaxEndIdentifier;
  private String mathjaxAsciiMathStartIdentifier;
  private String mathjaxAsciiMathEndIdentifier;
  private static final String INLINE_MATHJAX_START_IDENTIFIER = "inlineMathjaxStartIdentifier";
  private static final String INLINE_MATHJAX_END_IDENTIFIER = "inlineMathjaxEndIdentifier";
  private static final String BLOCK_MATHJAX_START_IDENTIFIER = "blockMathjaxStartIdentifier";
  private static final String BLOCK_MATHJAX_END_IDENTIFIER = "blockMathjaxEndIdentifier";
  private static final String MATHJAX_ASCII_MATH_START_IDENTIFIER = "mathjaxAsciiMathStartIdentifier";
  private static final String MATHJAX_ASCII_MATH_END_IDENTIFIER = "mathjaxAsciiMathEndIdentifier";

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
    this.pngCache = cacheManager.getCache(PluginHelper.class.getName() + ".pngBinaryCache",
        new MathJaxPngCacheLoader(),
        new CacheSettingsBuilder()
            .expireAfterAccess(3, TimeUnit.HOURS)
            .build());

    PluginSettings pluginSettings = this.pluginSettingsFactory.createGlobalSettings();
    UrlValidator urlValidator = new UrlValidator();

    try {
      String url = (String) pluginSettings.get(
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
      String url = (String) pluginSettings.get(
          RestAdminConfigService.Config.class.getName() + ".serverUrl");
      if (urlValidator.isValid(url)) {
        this.setServerUrl(url);
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
      String id = (String) pluginSettings.get(
          RestAdminConfigService.Config.class.getName() + "." +
              INLINE_MATHJAX_START_IDENTIFIER);
      if (id != null && !id.isBlank()) {
        this.setInlineMathjaxStartIdentifier(id);
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
      String id = (String) pluginSettings.get(
          RestAdminConfigService.Config.class.getName() + "." +
              INLINE_MATHJAX_END_IDENTIFIER);
      if (id != null && !id.isBlank()) {
        this.setInlineMathjaxEndIdentifier(id);
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
      String id = (String) pluginSettings.get(
          RestAdminConfigService.Config.class.getName() + "." +
              BLOCK_MATHJAX_START_IDENTIFIER);
      if (id != null && !id.isBlank()) {
        this.setBlockMathjaxStartIdentifier(id);
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
      String id = (String) pluginSettings.get(
          RestAdminConfigService.Config.class.getName() + "." +
              BLOCK_MATHJAX_END_IDENTIFIER);
      if (id != null && !id.isBlank()) {
        this.setBlockMathjaxEndIdentifier(id);
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
      String id = (String) pluginSettings.get(
          RestAdminConfigService.Config.class.getName() + "." +
              MATHJAX_ASCII_MATH_START_IDENTIFIER);
      if (id != null && !id.isBlank()) {
        this.setMathjaxAsciiMathStartIdentifier(
            id);
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
      String id = (String) pluginSettings.get(
          RestAdminConfigService.Config.class.getName() + "." +
              MATHJAX_ASCII_MATH_END_IDENTIFIER);
      if (id != null && !id.isBlank()) {
        this.setMathjaxAsciiMathEndIdentifier(id);
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

  public String getUrl() {
    return this.urlEndPoint;
  }

  public void setUrl(final String url) {
    this.urlEndPoint = url;
    transactionTemplate.execute(new TransactionCallback() {
      public Object doInTransaction() {
        PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
        pluginSettings.put(
            RestAdminConfigService.Config.class.getName() + ".url", url);
        return null;
      }
    });
  }

  public String getServerUrl() {
    return this.serverUrl;
  }

  public void setServerUrl(final String serverUrl) {
    this.serverUrl = serverUrl;
    transactionTemplate.execute(new TransactionCallback() {
      public Object doInTransaction() {
        PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
        pluginSettings.put(RestAdminConfigService.Config.class.getName() +
            ".serverUrl",
            serverUrl);
        return null;
      }
    });
  }

  public String getInlineMathjaxStartIdentifier() {
    return this.inlineMathjaxStartIdentifier;
  }

  public void setInlineMathjaxStartIdentifier(final String value) {
    if (value != null && !value.isBlank()) {
      this.inlineMathjaxStartIdentifier = value;
      transactionTemplate.execute(new TransactionCallback() {
        public Object doInTransaction() {
          PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
          pluginSettings.put(RestAdminConfigService.Config.class.getName() +
              "." + INLINE_MATHJAX_START_IDENTIFIER,
              value);
          return null;
        }
      });
    }
  }

  public String getInlineMathjaxEndIdentifier() {
    return this.inlineMathjaxEndIdentifier;
  }

  public void setInlineMathjaxEndIdentifier(final String value) {
    if (value != null && !value.isBlank()) {
      this.inlineMathjaxEndIdentifier = value;
      transactionTemplate.execute(new TransactionCallback() {
        public Object doInTransaction() {
          PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
          pluginSettings.put(RestAdminConfigService.Config.class.getName() +
              "." + INLINE_MATHJAX_END_IDENTIFIER,
              value);
          return null;
        }
      });
    }
  }

  public String getBlockMathjaxStartIdentifier() {
    return this.blockMathjaxStartIdentifier;
  }

  public void setBlockMathjaxStartIdentifier(final String value) {
    if (value != null && !value.isBlank()) {
      this.blockMathjaxStartIdentifier = value;
      transactionTemplate.execute(new TransactionCallback() {
        public Object doInTransaction() {
          PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
          pluginSettings.put(RestAdminConfigService.Config.class.getName() +
              "." + BLOCK_MATHJAX_START_IDENTIFIER,
              value);
          return null;
        }
      });
    }
  }

  public String getBlockMathjaxEndIdentifier() {
    return this.blockMathjaxEndIdentifier;
  }

  public void setBlockMathjaxEndIdentifier(final String value) {
    if (value != null && !value.isBlank()) {
      this.blockMathjaxEndIdentifier = value;
      transactionTemplate.execute(new TransactionCallback() {
        public Object doInTransaction() {
          PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
          pluginSettings.put(RestAdminConfigService.Config.class.getName() +
              "." + BLOCK_MATHJAX_END_IDENTIFIER,
              value);
          return null;
        }
      });
    }
  }

  public String getMathjaxAsciiMathStartIdentifier() {
    return this.mathjaxAsciiMathStartIdentifier;
  }

  public void setMathjaxAsciiMathStartIdentifier(final String value) {
    if (value != null && !value.isBlank()) {
      this.mathjaxAsciiMathStartIdentifier = value;
      transactionTemplate.execute(new TransactionCallback() {
        public Object doInTransaction() {
          PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
          pluginSettings.put(RestAdminConfigService.Config.class.getName() +
              "." + MATHJAX_ASCII_MATH_START_IDENTIFIER,
              value);
          return null;
        }
      });
    }
  }

  public String getMathjaxAsciiMathEndIdentifier() {
    return this.mathjaxAsciiMathEndIdentifier;
  }

  public void setMathjaxAsciiMathEndIdentifier(final String value) {
    if (value != null && !value.isBlank()) {
      this.mathjaxAsciiMathEndIdentifier = value;
      transactionTemplate.execute(new TransactionCallback() {
        public Object doInTransaction() {
          PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
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
      String encodedEquation = URLEncoder.encode(equation, StandardCharsets.UTF_8);
      svg = this.svgCache.get(encodedEquation);
    } catch (Exception e) {
      log.error("Error generating SVG HTML", e);
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
    public String load(String encodedEquation) {
      try {
        String url = PluginHelper.this.getServerUrl() + "/svg";

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString("mathjax=" +
                encodedEquation))
            .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
      } catch (IOException e) {
        return null;
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return null;
      }
    }
  }

  public String generatePngHtml(String equation, boolean isInline) {
    PngImageData image = null;
    try {
      String encodedEquation = URLEncoder.encode(equation, StandardCharsets.UTF_8);
      image = this.pngCache.get(encodedEquation);
    } catch (Exception e) {
      log.error("Error generating PNG HTML", e);
    }

    if (image != null) {
      // Convert the byte array to a Base64 string
      String imageBase64 = Base64.getEncoder().encodeToString(image.data);
      int width = (int) (image.width / PluginHelper.PNG_SCALING_FACTOR);
      int height = (int) (image.height / PluginHelper.PNG_SCALING_FACTOR);
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
    public PngImageData load(String encodedEquation) {
      try {
        String url = PluginHelper.this.getServerUrl() + "/png";

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(
                String.format("scaling=%f&mathjax=%s", PNG_SCALING_FACTOR,
                    encodedEquation)))
            .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

        byte[] responseBody = response.body();
        return new PngImageData(responseBody);

      } catch (IOException e) {
        return null;
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return null;
      }
    }
  }

  private class PngImageData {
    public final byte[] data;
    public final int width;
    public final int height;

    public PngImageData(byte[] data) {
      this.data = data;

      ByteBuffer buffer = ByteBuffer.wrap(data, 16, 8);
      this.width = buffer.getInt();
      this.height = buffer.getInt();
    }
  }
}
