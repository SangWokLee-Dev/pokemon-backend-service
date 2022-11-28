package com.pokemon.backend.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableCaching
@RequiredArgsConstructor
@Log4j2
public class CacheConfig {
  public static final String POKEMON_CACHE_NAME = "pokemonCache";
  public static final String TRANSLATION_CACHE_NAME = "translationCache";

  @Value("${app.caches.pokemon.duration}")
  private Integer pokemonCacheDuration;

  @Value("${app.caches.pokemon.size}")
  private Integer pokemonCacheSize;

  @Value("${app.caches.translation.duration}")
  private Integer translationCacheDuration;

  @Value("${app.caches.translation.size}")
  private Integer translationCacheSize;

  @Bean
  public Ticker ticker() {
    return Ticker.systemTicker();
  }

  @Bean
  public CacheManager cacheManager(Ticker ticker) {
    SimpleCacheManager cacheManager = new SimpleCacheManager();
    Cache pokemonCache = buildPokemonCache(ticker);
    Cache translationCache = buildTranslationCache(ticker);
    List<Cache> caches = List.of(pokemonCache, translationCache);
    cacheManager.setCaches(caches);
    return cacheManager;
  }

  private Caffeine<Object, Object> buildCacheBuilder(
      Duration durationToExpire, Integer size, Ticker ticker) {
    return Caffeine.newBuilder()
        .expireAfterWrite(durationToExpire)
        .ticker(ticker)
        .maximumSize(size);
  }

  private CaffeineCache buildPokemonCache(Ticker ticker) {
    com.github.benmanes.caffeine.cache.Cache<Object, Object> cacheBuilder =
        buildCacheBuilder(Duration.ofSeconds(pokemonCacheDuration), pokemonCacheSize, ticker)
            .build();

    return new CaffeineCache(POKEMON_CACHE_NAME, cacheBuilder);
  }

  private CaffeineCache buildTranslationCache(Ticker ticker) {
    com.github.benmanes.caffeine.cache.Cache<Object, Object> cacheBuilder =
        buildCacheBuilder(
                Duration.ofSeconds(translationCacheDuration), translationCacheSize, ticker)
            .build();

    return new CaffeineCache(TRANSLATION_CACHE_NAME, cacheBuilder);
  }
}
