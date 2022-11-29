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

import static com.pokemon.backend.http.translation.constant.TranslationUrl.SHAKESPEARE_TRANSLATION_URL;
import static com.pokemon.backend.http.translation.constant.TranslationUrl.YODA_TRANSLATION_URL;

@Service
@Log4j2
public class TranslationService {
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
        ? getTranslatedText(description, getTranslationUrl(habitat, isLegendary))
        : description;
  }

  private String getTranslatedText(String text, String translationUrl) {
    log.info(
        "Received request to translate text: {} from translation url: {}", text, translationUrl);
    try {
      HttpResponse<String> translationResponse =
          translationHttpClient.getTranslatedText(translationUrl, text);
      int translationResponseStatusCode = translationResponse.statusCode();
      String translatedText =
          translationResponseStatusCode == HttpStatus.OK.value()
              ? getTranslatedText(translationResponse)
              : null;
      log.info(
          "Received translated text: {} from translation url: {} with original text: {}",
          translatedText,
          translationUrl,
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

  private String getTranslationUrl(String habitat, Boolean isLegendary) {
    return habitat != null && !habitat.isEmpty() && habitat.equals(Habitat.CAVE.name) || isLegendary
        ? YODA_TRANSLATION_URL
        : SHAKESPEARE_TRANSLATION_URL;
  }
}
