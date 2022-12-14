package com.pokemon.backend.http;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.pokemon.backend.config.JsonPathConfig;
import com.pokemon.backend.http.pokemon.PokemonHttpClient;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.pokemon.backend.config.CacheConfig.POKEMON_CACHE_NAME;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class PokemonHttpClientTest {
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
    cacheManager.getCache(POKEMON_CACHE_NAME).clear();
  }

  @Autowired private PokemonHttpClient pokemonHttpClient;
  @Autowired private JsonPathConfig jsonPathConfig;

  @Test
  @DisplayName("should return pokemon")
  public void shouldReturnPokemon()
      throws URISyntaxException, ExecutionException, InterruptedException, IOException {
    // given
    String pokemonName = "pikachu";
    // when
    String mockPokemonResponseBody =
        Files.readString(Path.of("src/test/resources/data/pikachu.json"));
    // when
    stubFor(
        get(urlEqualTo("/api/v2/pokemon-species/pikachu"))
            .willReturn(aResponse().withStatus(200).withBody(mockPokemonResponseBody)));
    // then
    HttpResponse<String> actualPokemonResponse = pokemonHttpClient.getPokemon(pokemonName);
    HttpResponse cachedActualPokemon =
        cacheManager.getCache(POKEMON_CACHE_NAME).get("pikachu", HttpResponse.class);
    int actualPokemonResponseStatusCode = actualPokemonResponse.statusCode();
    String actualPokemonResponseBody = actualPokemonResponse.body();
    DocumentContext actualPokemonContext =
        JsonPath.using(jsonPathConfig.getJsonPathConfig()).parse(actualPokemonResponseBody);
    String actualPokemonName = actualPokemonContext.read("$['name']");
    String actualPokemonHabitat = actualPokemonContext.read("$['habitat']['name']");
    Boolean actualPokemonIsLegendary = actualPokemonContext.read("$['is_legendary']");
    List<String> actualPokemonDescriptions =
        actualPokemonContext.read("$['flavor_text_entries'][*]['flavor_text']");
    assertThat(actualPokemonResponseStatusCode).isEqualTo(200);
    assertThat(actualPokemonName).isEqualTo("pikachu");
    assertThat(actualPokemonHabitat).isEqualTo("forest");
    assertThat(actualPokemonIsLegendary).isEqualTo(false);
    assertThat(actualPokemonDescriptions)
        .contains(
            "When several of these POKeMON gather,\ntheir electricity can build and cause\nlightning storms.");
    assertThat(cachedActualPokemon).isNotNull();
  }

  @Test
  @DisplayName("should throw not found exception when pokemon is not found from server")
  public void shouldThrowNotFoundExceptionWhenPokemonIsNotFoundFromServer()
      throws URISyntaxException, ExecutionException, InterruptedException {
    // given
    String pokemonName = "invalid_pokemon";
    // when
    stubFor(
        get(urlEqualTo("/api/v2/pokemon-species/invalid_pokemon"))
            .willReturn(aResponse().withStatus(404).withBody("Not Found")));
    // then
    HttpResponse<String> actualPokemonResponse = pokemonHttpClient.getPokemon(pokemonName);
    assertThat(actualPokemonResponse.statusCode()).isEqualTo(404);
    assertThat(actualPokemonResponse.body()).isEqualTo("Not Found");
  }
}
