package com.reactivespring.client;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.exception.MoviesInfoClientException;
import com.reactivespring.exception.MoviesInfoServerException;
import com.reactivespring.util.RetryUtil;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.function.Function;

@Component
@NoArgsConstructor
@AllArgsConstructor
public class MoviesInfoRestClient {

    @Autowired
    private WebClient webClient;

    @Value("${restClient.moviesInfoUrl}")
    private String moviesInfoUrl;

    public Mono<MovieInfo> retrieveMovieInfo(String movieId){

        var url = moviesInfoUrl.concat("/{id}");

        Function<ClientResponse, Mono<? extends Throwable>> fn = (clientResponse -> clientResponse.bodyToMono(String.class)
                .flatMap(message -> Mono.error(new MoviesInfoServerException("Server error in MoviesInfoService: " + message))));

        return webClient
                .get()
                .uri(url, movieId)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, (clientResponse -> {
                    if (clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)) {
                        return Mono.error(new MoviesInfoClientException("Movie Info not found for id: " + movieId,
                                HttpStatus.NOT_FOUND.value()));
                    }
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(message -> Mono.error(new MoviesInfoClientException(message, clientResponse.statusCode().value())));
                }))
                .onStatus(HttpStatus::is5xxServerError, fn)
                .bodyToMono(MovieInfo.class)
                .retryWhen(RetryUtil.retrySpec())
                .log();

    }
}
