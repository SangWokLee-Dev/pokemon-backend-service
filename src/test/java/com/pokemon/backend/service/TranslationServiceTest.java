package com.pokemon.backend.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class TranslationServiceTest {
  @Autowired private TranslationService translationService;
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
    wireMockServer.resetScenarios();
  }

  @Test
  @DisplayName("should return translated text with Shakespeare translation")
  public void shouldReturnTranslatedTextWithShakespeareTranslation() throws IOException {
    // given
    String text =
        "When several of these POKéMON gather, their electricity could build and cause lightning storms.";
    String mockTranslationResponseBody =
        Files.readString(
            Path.of("src/test/resources/data/pikachu_shakespeare_translated_description.json"));
    // when
    stubFor(
        post(urlEqualTo("/translate/shakespeare"))
            .withRequestBody(equalTo("text=" + text))
            .willReturn(aResponse().withStatus(200).withBody(mockTranslationResponseBody)));
    // then
    String actualTranslationResponse =
        translationService.getTranslatedPokemonDescription(text, "forest", false);
    assertThat(actualTranslationResponse)
        .isEqualTo(
            "At which hour several of these pokemon gather,  their electricity couldst buildeth and cause lightning storms.");
  }

  @Test
  @DisplayName("should return translated text with yoda translation when habitat is cave")
  public void shouldReturnTranslatedTextWithYodaTranslationWhenHabitatIsCave() throws IOException {
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
    String actualTranslationResponse =
        translationService.getTranslatedPokemonDescription(text, "cave", false);
    assertThat(actualTranslationResponse)
        .isEqualTo(
            "On plant roots,  lives about one yard underground where it feeds.Above ground,  it sometimes appears.");
  }

  @Test
  @DisplayName("should return translated text with yoda translation when pokemon is legendary")
  public void shouldReturnTranslatedTextWithYodaTranslationWhenPokemonIsLegendary()
      throws IOException {
    // given
    String text =
        "It was created by a scientist after years of horrific gene splicing and DNA engineering experiments.";
    String mockTranslationResponseBody =
        Files.readString(
            Path.of("src/test/resources/data/mewtwo_yoda_translated_description.json"));
    // when
    stubFor(
        post(urlEqualTo("/translate/yoda"))
            .withRequestBody(equalTo("text=" + text))
            .willReturn(aResponse().withStatus(200).withBody(mockTranslationResponseBody)));
    // then
    String actualTranslationResponse =
        translationService.getTranslatedPokemonDescription(text, "rare", true);
    assertThat(actualTranslationResponse)
        .isEqualTo(
            "Created by a scientist after years of horrific gene splicing and dna engineering experiments,  it was.");
  }

  @Test
  @DisplayName("should return original text when translation response status code is not ok")
  public void shouldReturnOriginalTextWhenTranslationResponseStatusCodeIsNotOk() {
    // given
    String text =
        "A legendary bird POKeMON that is said to appear from clouds while dropping enormous lightning bolts.";
    // when
    stubFor(
        post(urlEqualTo("/translate/yoda"))
            .withRequestBody(equalTo("text=" + text))
            .willReturn(aResponse().withStatus(429)));
    // then
    String actualTranslationResponse =
        translationService.getTranslatedPokemonDescription(text, "rare", true);
    assertThat(actualTranslationResponse)
        .isEqualTo(
            "A legendary bird POKeMON that is said to appear from clouds while dropping enormous lightning bolts.");
  }

  @Test
  @DisplayName("should return original text when translation response is not in valid format")
  public void shouldReturnOriginalTextWhenTranslationResponseIsNotInValidFormat() {
    // given
    String text =
        "Generations of kings were attended by these Pokémon, which used their spectral power to manipulate and control people and Pokémon.";
    // when
    stubFor(
        post(urlEqualTo("/translate/yoda"))
            .withRequestBody(equalTo("text=" + text))
            .willReturn(aResponse().withStatus(200).withBody("invalid_response_body")));
    // then
    String actualTranslationResponse =
        translationService.getTranslatedPokemonDescription(text, "rare", true);
    assertThat(actualTranslationResponse)
        .isEqualTo(
            "Generations of kings were attended by these Pokémon, which used their spectral power to manipulate and control people and Pokémon.");
  }
}
