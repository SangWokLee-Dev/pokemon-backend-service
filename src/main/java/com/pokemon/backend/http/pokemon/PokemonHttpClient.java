package com.pokemon.backend.http.pokemon;

import com.pokemon.backend.http.AbstractHttpClient;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutionException;

import static com.pokemon.backend.config.CacheConfig.POKEMON_CACHE_NAME;
import static com.pokemon.backend.http.pokemon.constant.PokemonUrl.POKEMON_SPECIES_URL;

@Service
@Log4j2
public class PokemonHttpClient {
  @Autowired private AbstractHttpClient abstractHttpClient;

  @Cacheable(cacheNames = POKEMON_CACHE_NAME, unless = "#result.statusCode() != 200")
  public HttpResponse<String> getPokemon(String name)
      throws URISyntaxException, ExecutionException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(new URI(POKEMON_SPECIES_URL + name))
            .version(HttpClient.Version.HTTP_2)
            .GET()
            .build();
    return abstractHttpClient.getResponse(request);
  }
}
