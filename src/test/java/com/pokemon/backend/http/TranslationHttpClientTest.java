package com.pokemon.backend.http;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.pokemon.backend.config.JsonPathConfig;
import com.pokemon.backend.http.translation.TranslationHttpClient;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.pokemon.backend.config.CacheConfig.TRANSLATION_CACHE_NAME;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class TranslationHttpClientTest {
  @Autowired CacheManager cacheManager;
  static WireMockServer wireMockServer = new WireMockServer(options().port(8080));

  @BeforeAll
  public static void beforeAll() {
    wireMockServer.start();
  }

  @AfterAll
  public static void afterAll() {
    wireMockServer.stop();
  }

  @AfterEach
  public void afterEach() {
    wireMockServer.resetAll();
    cacheManager.getCache(TRANSLATION_CACHE_NAME).clear();
  }

  @Autowired private TranslationHttpClient translationHttpClient;
  @Autowired private JsonPathConfig jsonPathConfig;

  @Test
  @DisplayName("should return translated text with Shakespeare translation")
  public void shouldReturnTranslatedTextWithShakespeareTranslation()
      throws URISyntaxException, ExecutionException, InterruptedException, IOException {
    // given
    String text =
        "When several of these POKeMON gather, their electricity could build and cause lightning storms.";
    String mockTranslationResponseBody =
        Files.readString(
            Path.of("src/test/resources/data/pikachu_shakespeare_translated_description.json"));
    // when
    stubFor(
        post(urlEqualTo("/translate/shakespeare"))
            .withRequestBody(equalTo("text=" + text))
            .willReturn(aResponse().withStatus(200).withBody(mockTranslationResponseBody)));
    // then
    HttpResponse<String> actualTranslationResponse =
        translationHttpClient.getTranslatedText("/translate/shakespeare", text);
    HttpResponse actualCachedTranslationResponse =
        cacheManager
            .getCache(TRANSLATION_CACHE_NAME)
            .get(
                "/translate/shakespeare-When several of these POKeMON gather, their electricity could build and cause lightning storms.",
                HttpResponse.class);
    int actualTranslationResponseStatusCode = actualTranslationResponse.statusCode();
    String actualTranslationResponseBody = actualTranslationResponse.body();
    DocumentContext actualTranslationContext =
        JsonPath.using(jsonPathConfig.getJsonPathConfig()).parse(actualTranslationResponseBody);
    String actualTranslatedText = actualTranslationContext.read("$['contents']['translated']");
    assertThat(actualTranslationResponseStatusCode).isEqualTo(200);
    assertThat(actualTranslatedText)
        .isEqualTo(
            "At which hour several of these pokemon gather,  their electricity couldst buildeth and cause lightning storms.");
    assertThat(actualCachedTranslationResponse).isNotNull();
  }

  @Test
  @DisplayName("should return translated text with yoda translation")
  public void shouldReturnTranslatedTextWithYodaTranslation()
      throws URISyntaxException, ExecutionException, InterruptedException, IOException {
    // given
    String text =
        "Lives about one yard underground where it feeds on plant roots. It sometimes appears above ground.";
    String mockTranslationResponseBody =
        Files.readString(
            Path.of("src/test/resources/data/diglett_yoda_translated_description.json"));
    // when
    stubFor(
        post(urlEqualTo("/translate/yoda"))
            .withRequestBody(equalTo("text=" + text))
            .willReturn(aResponse().withStatus(200).withBody(mockTranslationResponseBody)));
    // then
    HttpResponse<String> actualTranslationResponse =
        translationHttpClient.getTranslatedText("/translate/yoda", text);
    int actualTranslationResponseStatusCode = actualTranslationResponse.statusCode();
    String actualTranslationResponseBody = actualTranslationResponse.body();
    DocumentContext actualTranslationContext =
        JsonPath.using(jsonPathConfig.getJsonPathConfig()).parse(actualTranslationResponseBody);
    String actualTranslatedText = actualTranslationContext.read("$['contents']['translated']");
    assertThat(actualTranslationResponseStatusCode).isEqualTo(200);
    assertThat(actualTranslatedText)
        .isEqualTo(
            "On plant roots,  lives about one yard underground where it feeds.Above ground,  it sometimes appears.");
  }
}
