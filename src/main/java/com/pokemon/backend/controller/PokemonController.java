package com.pokemon.backend.controller;

import com.pokemon.backend.model.pokemon.Pokemon;
import com.pokemon.backend.service.PokemonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

@Controller
@RequestMapping("/pokemon")
public class PokemonController {

  @Autowired private PokemonService pokemonService;

  @GetMapping("/{name}")
  public ResponseEntity<Pokemon> getPokemon(@PathVariable String name)
      throws URISyntaxException, ExecutionException, InterruptedException {
    Pokemon pokemon = pokemonService.getPokemon(name, false);
    return ResponseEntity.ok().body(pokemon);
  }

  @GetMapping("/translated/{name}")
  public ResponseEntity<Pokemon> getPokemonWithTranslatedDescription(@PathVariable String name)
      throws URISyntaxException, ExecutionException, InterruptedException {
    Pokemon pokemon = pokemonService.getPokemon(name, true);
    return ResponseEntity.ok().body(pokemon);
  }
}
