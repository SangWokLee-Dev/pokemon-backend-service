package com.pokemon.backend.http.translation.constant;

public class TranslationUrl {
  private TranslationUrl() {}

  private static final String FUN_TRANSLATION_URL = "https://api.funtranslations.com/translate/";
  public static final String SHAKESPEARE_TRANSLATION_URL = FUN_TRANSLATION_URL + "shakespeare";
  public static final String YODA_TRANSLATION_URL = FUN_TRANSLATION_URL + "yoda";
}
