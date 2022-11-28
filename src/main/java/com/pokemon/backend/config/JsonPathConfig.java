package com.pokemon.backend.config;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;

@org.springframework.context.annotation.Configuration
public class JsonPathConfig {
  private static final Configuration JSON_PATH_CONFIG =
      Configuration.builder().options(Option.SUPPRESS_EXCEPTIONS).build();

  public Configuration getJsonPathConfig() {
    return JSON_PATH_CONFIG;
  }
}
