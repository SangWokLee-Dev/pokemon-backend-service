package com.pokemon.backend.http.translation;

import com.pokemon.backend.http.AbstractHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutionException;

import static com.pokemon.backend.config.CacheConfig.TRANSLATION_CACHE_NAME;

@Service
public class TranslationHttpClient {
  @Value("${app.base.url.translation}")
  private String translationBaseUrl;

  @Autowired private AbstractHttpClient abstractHttpClient;

  @Cacheable(cacheNames = TRANSLATION_CACHE_NAME, unless = "#result.statusCode() != 200")
  public HttpResponse<String> getTranslatedText(String url, String text)
      throws URISyntaxException, ExecutionException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(new URI(translationBaseUrl + url))
            .version(HttpClient.Version.HTTP_2)
            .POST(HttpRequest.BodyPublishers.ofString("text=" + text))
            .headers(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .build();
    return abstractHttpClient.getResponse(request);
  }
}
