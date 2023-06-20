package com.reactivespring.client;

import com.reactivespring.domain.Review;
import com.reactivespring.exception.MoviesInfoClientException;
import com.reactivespring.exception.MoviesInfoServerException;
import com.reactivespring.exception.ReviewsClientException;
import com.reactivespring.util.RetryUtil;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Component
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class MovieReviewRestClient {

    @Autowired
    private WebClient webClient;

    @Value("${restClient.movieReviewUrl}")
    private String movieReviewUrl;

    public Flux<Review> getMovieReviews(String movieId) {
        log.info("Movie review url: {}", movieReviewUrl);
        var uri = UriComponentsBuilder.fromHttpUrl(movieReviewUrl)
                .queryParam("movieInfoId", movieId)
                .buildAndExpand()
                .toUriString();

        Function<ClientResponse, Mono<? extends Throwable>> fn = (clientResponse -> clientResponse.bodyToMono(String.class)
                .flatMap(message -> Mono.error(new MoviesInfoServerException("Server error in ReviewService: " + message))));

        log.info("Fetching reviews for movieId: {}, URL: {}", movieId, uri);

       return webClient.get()
                .uri(uri)
                .retrieve()
               .onStatus(HttpStatus::is4xxClientError, (clientResponse -> {
                   if (clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)) {
                       return Mono.empty();
                   }
                   return clientResponse.bodyToMono(String.class)
                           .flatMap(message -> Mono.error(new ReviewsClientException(message, clientResponse.statusCode().value())));
               }))
               .onStatus(HttpStatus::is5xxServerError, fn)
               .bodyToFlux(Review.class)
               .retryWhen(RetryUtil.retrySpec());
    }
}
