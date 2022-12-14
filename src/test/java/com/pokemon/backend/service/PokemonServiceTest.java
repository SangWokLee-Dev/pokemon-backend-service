package com.pokemon.backend.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.pokemon.backend.model.pokemon.Pokemon;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class PokemonServiceTest {
  @Autowired private PokemonService pokemonService;
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
  }

  @Test
  public void shouldReturnPokemonWithoutDescriptionTranslated()
      throws URISyntaxException, IOException, ExecutionException, InterruptedException {
    // given
    String pokemonName = "pikachu";
    String mockPokemonResponseBody =
        Files.readString(Path.of("src/test/resources/data/pikachu.json"));
    // when
    stubFor(
        get(urlEqualTo("/api/v2/pokemon-species/pikachu"))
            .willReturn(aResponse().withStatus(200).withBody(mockPokemonResponseBody)));
    // then
    Pokemon actualPokemon = pokemonService.getPokemon(pokemonName, false);
    assertThat(actualPokemon.getName()).isEqualTo("pikachu");
    assertThat(actualPokemon.getHabitat()).isEqualTo("forest");
    assertThat(actualPokemon.getIsLegendary()).isEqualTo(false);
    assertThat(actualPokemon.getDescription())
        .isEqualTo(
            "When several of these POKeMON gather, their electricity can build and cause lightning storms.");
  }

  @Test
  @DisplayName(
      "should return pokemon without description translated when translation server returns error response")
  public void
      shouldReturnPokemonWithoutDescriptionTranslatedWhenTranslationServerReturnsErrorResponse()
          throws Exception {
    // given
    String pokemonName = "pikachu";
    String text =
        "When several of these POKeMON gather, their electricity can build and cause lightning storms.";
    String mockPokemonResponseBody =
        Files.readString(Path.of("src/test/resources/data/pikachu.json"));
    // when
    stubFor(
        post(urlEqualTo("/translate/shakespeare"))
            .withRequestBody(equalTo("text=" + text))
            .willReturn(aResponse().withStatus(429)));
    stubFor(
        WireMock.get(urlEqualTo("/api/v2/pokemon-species/pikachu"))
            .willReturn(aResponse().withStatus(200).withBody(mockPokemonResponseBody)));
    // then
    Pokemon actualPokemon = pokemonService.getPokemon(pokemonName, false);
    assertThat(actualPokemon.getName()).isEqualTo("pikachu");
    assertThat(actualPokemon.getHabitat()).isEqualTo("forest");
    assertThat(actualPokemon.getIsLegendary()).isEqualTo(false);
    assertThat(actualPokemon.getDescription())
        .isEqualTo(
            "When several of these POKeMON gather, their electricity can build and cause lightning storms.");
  }

  @Test
  @DisplayName("should return pokemon with description translated with shakespeare translation")
  public void shouldReturnPokemonWithDescriptionTranslatedWithShakespeareTranslation()
      throws Exception {
    // given
    String pokemonName = "pikachu";
    String text =
        "When several of these POKeMON gather, their electricity can build and cause lightning storms.";
    String mockPokemonResponseBody =
        Files.readString(Path.of("src/test/resources/data/pikachu.json"));
    String mockTranslationResponseBody =
        Files.readString(
            Path.of("src/test/resources/data/pikachu_shakespeare_translated_description.json"));
    // when
    stubFor(
        post(urlEqualTo("/translate/shakespeare"))
            .withRequestBody(equalTo("text=" + text))
            .willReturn(aResponse().withStatus(200).withBody(mockTranslationResponseBody)));
    stubFor(
        WireMock.get(urlEqualTo("/api/v2/pokemon-species/pikachu"))
            .willReturn(aResponse().withStatus(200).withBody(mockPokemonResponseBody)));
    // then
    Pokemon actualPokemon = pokemonService.getPokemon(pokemonName, true);
    assertThat(actualPokemon.getName()).isEqualTo("pikachu");
    assertThat(actualPokemon.getHabitat()).isEqualTo("forest");
    assertThat(actualPokemon.getIsLegendary()).isEqualTo(false);
    assertThat(actualPokemon.getDescription())
        .isEqualTo(
            "At which hour several of these pokemon gather,  their electricity couldst buildeth and cause lightning storms.");
  }

  @Test
  @DisplayName("should throw not found exception when pokemon is not found from server")
  public void shouldThrowNotFoundExceptionWhenPokemonIsNotFoundFromServer() {
    // given
    String pokemonName = "invalid_pokemon";
    // when
    stubFor(
        get(urlEqualTo("/api/v2/pokemon-species/invalid_pokemon"))
            .willReturn(aResponse().withStatus(404).withBody("Not Found")));
    // then
    ResponseStatusException thrown =
        Assertions.assertThrows(
            ResponseStatusException.class, () -> pokemonService.getPokemon(pokemonName, false));
    assertThat(thrown.getStatus().value()).isEqualTo(404);
  }

  @Test
  @DisplayName(
      "should return pokemon with description translated with yoda translation when habitat is cave")
  public void shouldReturnPokemonWithDescriptionTranslatedWithYodaTranslationWhenHabitatIsCave()
      throws URISyntaxException, ExecutionException, InterruptedException, IOException {
    // given
    String pokemonName = "diglett";
    String text =
        "Lives about one yard underground where it feeds on plant roots. It sometimes appears above ground.";
    String mockPokemonResponseBody =
        Files.readString(Path.of("src/test/resources/data/diglett.json"));
    String mockTranslationResponseBody =
        Files.readString(
            Path.of("src/test/resources/data/diglett_yoda_translated_description.json"));
    // when
    stubFor(
        post(urlEqualTo("/translate/yoda"))
            .withRequestBody(equalTo("text=" + text))
            .willReturn(aResponse().withStatus(200).withBody(mockTranslationResponseBody)));
    stubFor(
        get(urlEqualTo("/api/v2/pokemon-species/diglett"))
            .willReturn(aResponse().withStatus(200).withBody(mockPokemonResponseBody)));
    // then
    Pokemon actualPokemon = pokemonService.getPokemon(pokemonName, true);
    assertThat(actualPokemon.getName()).isEqualTo("diglett");
    assertThat(actualPokemon.getHabitat()).isEqualTo("cave");
    assertThat(actualPokemon.getIsLegendary()).isEqualTo(false);
    assertThat(actualPokemon.getDescription())
        .isEqualTo(
            "On plant roots,  lives about one yard underground where it feeds.Above ground,  it sometimes appears.");
  }

  @Test
  @DisplayName(
      "should return pokemon with description translated with yoda translation when pokemon is legendary")
  public void
      shouldReturnPokemonWithDescriptionTranslatedWithYodaTranslationWhenPokemonIsLegendary()
          throws URISyntaxException, ExecutionException, InterruptedException, IOException {
    // given
    String pokemonName = "mewtwo";
    String text =
        "It was created by a scientist after years of horrific gene splicing and DNA engineering experiments.";
    String mockPokemonResponseBody =
        Files.readString(Path.of("src/test/resources/data/mewtwo.json"));
    String mockTranslationResponseBody =
        Files.readString(
            Path.of("src/test/resources/data/mewtwo_yoda_translated_description.json"));
    // when
    stubFor(
        post(urlEqualTo("/translate/yoda"))
            .withRequestBody(equalTo("text=" + text))
            .willReturn(aResponse().withStatus(200).withBody(mockTranslationResponseBody)));
    stubFor(
        get(urlEqualTo("/api/v2/pokemon-species/mewtwo"))
            .willReturn(aResponse().withStatus(200).withBody(mockPokemonResponseBody)));
    // then
    Pokemon actualPokemon = pokemonService.getPokemon(pokemonName, true);
    assertThat(actualPokemon.getName()).isEqualTo("mewtwo");
    assertThat(actualPokemon.getHabitat()).isEqualTo("rare");
    assertThat(actualPokemon.getIsLegendary()).isEqualTo(true);
    assertThat(actualPokemon.getDescription())
        .isEqualTo(
            "Created by a scientist after years of horrific gene splicing and dna engineering experiments,  it was.");
  }
}
