package com.pokemon.backend.service;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.pokemon.backend.config.JsonPathConfig;
import com.pokemon.backend.http.pokemon.PokemonHttpClient;
import com.pokemon.backend.model.language.Language;
import com.pokemon.backend.model.pokemon.Pokemon;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@Log4j2
public class PokemonService {
  @Autowired private PokemonHttpClient pokemonHttpClient;
  @Autowired private TranslationService translationService;
  @Autowired private JsonPathConfig jsonPathConfig;

  public Pokemon getPokemon(String pokemonName, Boolean isTranslated)
      throws URISyntaxException, ExecutionException, InterruptedException, ResponseStatusException {
    log.info(
        "Received request to get pokemon with name: {} and translation feature enabled status: {}",
        pokemonName,
        isTranslated);
    HttpResponse<String> pokemonResponse = pokemonHttpClient.getPokemon(pokemonName);
    int pokemonResponseStatusCode = pokemonResponse.statusCode();
    String pokemonResponseBody = pokemonResponse.body();
    if (pokemonResponseStatusCode != HttpStatus.OK.value()) {
      log.error(
          "Server responded with failure with response status code: {} and message: {} when requested to get pokemon with name: {}",
          pokemonResponseStatusCode,
          pokemonResponseBody,
          pokemonName);
      throw new ResponseStatusException(
          HttpStatus.valueOf(pokemonResponseStatusCode), pokemonResponseBody);
    }
    DocumentContext pokemonContext =
        JsonPath.using(jsonPathConfig.getJsonPathConfig()).parse(pokemonResponseBody);
    String name = pokemonContext.read("$['name']");
    String habitat = pokemonContext.read("$['habitat']['name']");
    Boolean isLegendary = pokemonContext.read("$['is_legendary']");
    String description = getDescription(isTranslated, pokemonContext, habitat, isLegendary);
    log.info(
        "Received pokemon with name: {}, habitat: {}, is legendary status: {}, and description: {}",
        name,
        habitat,
        isLegendary,
        description);
    return Pokemon.builder()
        .name(name)
        .isLegendary(isLegendary)
        .description(description)
        .habitat(habitat)
        .build();
  }

  private String getDescription(
      Boolean isTranslated, DocumentContext pokemonContext, String habitat, Boolean isLegendary) {
    List<String> englishDescriptions = getDescriptionsByLanguage(pokemonContext, Language.EN.code);
    String anyEnglishDescription = englishDescriptions.stream().findAny().orElse(null);
    return isTranslated
        ? translationService.getTranslatedPokemonDescription(
            anyEnglishDescription, habitat, isLegendary)
        : anyEnglishDescription;
  }

  private List<String> getDescriptionsByLanguage(DocumentContext pokemonContext, String language) {
    return pokemonContext.read(
        "$['flavor_text_entries'][?(@.language.name=='" + language + "')]['flavor_text']");
  }
}
