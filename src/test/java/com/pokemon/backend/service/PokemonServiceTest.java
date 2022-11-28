package com.pokemon.backend.service;

import com.pokemon.backend.model.pokemon.Pokemon;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class PokemonServiceTest {
  @Autowired private PokemonService pokemonService;

  @Test
  @DisplayName("should return pokemon without description translated")
  public void shouldReturnPokemonWithoutDescriptionTranslated()
      throws URISyntaxException, ExecutionException, InterruptedException {
    // given
    String pokemonName = "pikachu";
    // when
    Pokemon actualPokemon = pokemonService.getPokemon(pokemonName, false);
    // then
    assertThat(actualPokemon.getName()).isEqualTo("pikachu");
    assertThat(actualPokemon.getHabitat()).isEqualTo("forest");
    assertThat(actualPokemon.getIsLegendary()).isEqualTo(false);
  }

  @Test
  @DisplayName("should throw not found exception when pokemon is not found from server")
  public void shouldThrowNotFoundExceptionWhenPokemonIsNotFoundFromServer() {
    // given
    String pokemonName = "invalid_pokemon";
    // then
    ResponseStatusException thrown =
        Assertions.assertThrows(
            ResponseStatusException.class, () -> pokemonService.getPokemon(pokemonName, false));
    assertThat(thrown.getStatus().value()).isEqualTo(404);
  }

  @Test
  @DisplayName("should return pokemon with description translated when habitat is cave")
  public void shouldReturnPokemonWithDescriptionTranslatedWhenHabitatIsCave()
      throws URISyntaxException, ExecutionException, InterruptedException {
    // given
    String pokemonName = "zubat";
    // when
    Pokemon actualPokemon = pokemonService.getPokemon(pokemonName, true);
    // then
    assertThat(actualPokemon.getName()).isEqualTo("zubat");
    assertThat(actualPokemon.getHabitat()).isEqualTo("cave");
    assertThat(actualPokemon.getIsLegendary()).isEqualTo(false);
  }

  @Test
  @DisplayName("should return pokemon with description translated when pokemon is legendary")
  public void shouldReturnPokemonWithDescriptionTranslatedWhenPokemonIsLegendary()
      throws URISyntaxException, ExecutionException, InterruptedException {
    // given
    String pokemonName = "mewtwo";
    // when
    Pokemon actualPokemon = pokemonService.getPokemon(pokemonName, true);
    // then
    assertThat(actualPokemon.getName()).isEqualTo("mewtwo");
    assertThat(actualPokemon.getHabitat()).isEqualTo("rare");
    assertThat(actualPokemon.getIsLegendary()).isEqualTo(true);
  }
}
