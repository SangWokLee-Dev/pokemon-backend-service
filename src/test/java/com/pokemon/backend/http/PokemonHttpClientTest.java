package com.pokemon.backend.http;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.pokemon.backend.config.JsonPathConfig;
import com.pokemon.backend.http.pokemon.PokemonHttpClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class PokemonHttpClientTest {

  @Autowired private PokemonHttpClient pokemonHttpClient;
  @Autowired private JsonPathConfig jsonPathConfig;

  @Test
  @DisplayName("should return pokemon")
  public void shouldReturnPokemon()
      throws URISyntaxException, ExecutionException, InterruptedException {
    // given
    String pokemonName = "pikachu";
    // when
    HttpResponse<String> actualPokemonResponse = pokemonHttpClient.getPokemon(pokemonName);
    // then
    int pokemonResponseStatusCode = actualPokemonResponse.statusCode();
    String pokemonResponseBody = actualPokemonResponse.body();
    DocumentContext pokemonContext =
        JsonPath.using(jsonPathConfig.getJsonPathConfig()).parse(pokemonResponseBody);
    String name = pokemonContext.read("$['name']");
    String habitat = pokemonContext.read("$['habitat']['name']");
    Boolean isLegendary = pokemonContext.read("$['is_legendary']");
    assertThat(pokemonResponseStatusCode).isEqualTo(200);
    assertThat(name).isEqualTo("pikachu");
    assertThat(habitat).isEqualTo("forest");
    assertThat(isLegendary).isEqualTo(false);
  }
}
