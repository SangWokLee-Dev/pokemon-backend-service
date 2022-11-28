package com.pokemon.backend.http;

import org.springframework.stereotype.Service;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class AbstractHttpClient {
  private static final ExecutorService executorService =
      Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

  public HttpResponse<String> getResponse(HttpRequest httprequest)
      throws ExecutionException, InterruptedException {
    CompletableFuture<HttpResponse<String>> response =
        HttpClient.newBuilder()
            .executor(executorService)
            .build()
            .sendAsync(httprequest, HttpResponse.BodyHandlers.ofString());
    return response.get();
  }
}
