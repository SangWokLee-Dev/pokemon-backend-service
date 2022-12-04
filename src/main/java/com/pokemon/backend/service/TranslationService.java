package com.pokemon.backend.service;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.pokemon.backend.config.JsonPathConfig;
import com.pokemon.backend.http.translation.TranslationHttpClient;
import com.pokemon.backend.model.pokemon.Habitat;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutionException;

@Service
@Log4j2
public class TranslationService {
  private static final String YODA_TRANSLATION_URL_PATH = "/translate/yoda";
  private static final String SHAKESPEARE_TRANSLATION_URL_PATH = "/translate/shakespeare";
  @Autowired private TranslationHttpClient translationHttpClient;
  @Autowired private JsonPathConfig jsonPathConfig;

  public String getTranslatedPokemonDescription(
      String description, String habitat, Boolean isLegendary) {
    log.info(
        "Received request to translate pokemon description: {} with habitat: {}, and is legendary status: {}",
        description,
        habitat,
        isLegendary);
    return description != null && !description.isEmpty()
        ? getTranslatedText(description, getTranslationUrlPath(habitat, isLegendary))
        : description;
  }

  private String getTranslatedText(String text, String translationUrlPath) {
    log.info(
        "Received request to translate text: {} from translation url path: {}",
        text,
        translationUrlPath);
    try {
      HttpResponse<String> translationResponse =
          translationHttpClient.getTranslatedText(translationUrlPath, text);
      int translationResponseStatusCode = translationResponse.statusCode();
      String translatedText =
          translationResponseStatusCode == HttpStatus.OK.value()
              ? getTranslatedText(translationResponse)
              : null;
      log.info(
          "Received translated text: {} from translation url path: {} with original text: {}",
          translatedText,
          translationUrlPath,
          text);
      text = translatedText != null && !translatedText.isEmpty() ? translatedText : text;
    } catch (URISyntaxException | ExecutionException | InterruptedException exception) {
      log.error("Failed to translate text: " + text + " due to: ", exception);
    }
    return text;
  }

  private String getTranslatedText(HttpResponse<String> translationResponse) {
    String translationResponseBody = translationResponse.body();
    DocumentContext translationContext =
        JsonPath.using(jsonPathConfig.getJsonPathConfig()).parse(translationResponseBody);
    return translationContext.read("$['contents']['translated']");
  }

  private String getTranslationUrlPath(String habitat, Boolean isLegendary) {
    return habitat != null && !habitat.isEmpty() && habitat.equals(Habitat.CAVE.name) || isLegendary
        ? YODA_TRANSLATION_URL_PATH
        : SHAKESPEARE_TRANSLATION_URL_PATH;
  }
}
