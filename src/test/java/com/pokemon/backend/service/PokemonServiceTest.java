package com.pokemon.backend.service;

import com.github.tomakehurst.wiremock.WireMockServer;
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
        get(urlEqualTo("/pokemon-species/pikachu"))
            .willReturn(aResponse().withStatus(200).withBody(mockPokemonResponseBody)));
    // then
    Pokemon actualPokemon = pokemonService.getPokemon(pokemonName, false);
    assertThat(actualPokemon.getName()).isEqualTo("pikachu");
    assertThat(actualPokemon.getHabitat()).isEqualTo("forest");
    assertThat(actualPokemon.getIsLegendary()).isEqualTo(false);
  }

  @Test
  @DisplayName("should throw not found exception when pokemon is not found from server")
  public void shouldThrowNotFoundExceptionWhenPokemonIsNotFoundFromServer() {
    // given
    String pokemonName = "invalid_pokemon";
    // when
    stubFor(
        get(urlEqualTo("/pokemon-species/invalid_pokemon"))
            .willReturn(aResponse().withStatus(404).withBody("Not Found")));
    // then
    ResponseStatusException thrown =
        Assertions.assertThrows(
            ResponseStatusException.class, () -> pokemonService.getPokemon(pokemonName, false));
    assertThat(thrown.getStatus().value()).isEqualTo(404);
  }

  @Test
  @DisplayName("should return pokemon with description translated when habitat is cave")
  public void shouldReturnPokemonWithDescriptionTranslatedWhenHabitatIsCave()
      throws URISyntaxException, ExecutionException, InterruptedException, IOException {
    // given
    String pokemonName = "diglett";
    String mockPokemonResponseBody =
        Files.readString(Path.of("src/test/resources/data/diglett.json"));
    // when
    stubFor(
        get(urlEqualTo("/pokemon-species/diglett"))
            .willReturn(aResponse().withStatus(200).withBody(mockPokemonResponseBody)));
    // then
    Pokemon actualPokemon = pokemonService.getPokemon(pokemonName, true);
    assertThat(actualPokemon.getName()).isEqualTo("diglett");
    assertThat(actualPokemon.getHabitat()).isEqualTo("cave");
    assertThat(actualPokemon.getIsLegendary()).isEqualTo(false);
  }

  @Test
  @DisplayName("should return pokemon with description translated when pokemon is legendary")
  public void shouldReturnPokemonWithDescriptionTranslatedWhenPokemonIsLegendary()
      throws URISyntaxException, ExecutionException, InterruptedException, IOException {
    // given
    String pokemonName = "mewtwo";
    String mockPokemonResponseBody =
        Files.readString(Path.of("src/test/resources/data/mewtwo.json"));
    // when
    stubFor(
        get(urlEqualTo("/pokemon-species/mewtwo"))
            .willReturn(aResponse().withStatus(200).withBody(mockPokemonResponseBody)));
    // then
    Pokemon actualPokemon = pokemonService.getPokemon(pokemonName, true);
    assertThat(actualPokemon.getName()).isEqualTo("mewtwo");
    assertThat(actualPokemon.getHabitat()).isEqualTo("rare");
    assertThat(actualPokemon.getIsLegendary()).isEqualTo(true);
  }
}
