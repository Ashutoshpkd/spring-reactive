package com.reactivespring.controller;

import com.reactivespring.client.MovieReviewRestClient;
import com.reactivespring.client.MoviesInfoRestClient;
import com.reactivespring.domain.Movie;
import com.reactivespring.domain.MovieInfo;
import com.reactivespring.domain.Review;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/movies")
@Slf4j
public class MoviesController {

    @Autowired
    private MoviesInfoRestClient infoRestClient;

    @Autowired
    private MovieReviewRestClient reviewRestClient;

    @GetMapping("/{id}")
    public Mono<Movie> getMovie(@PathVariable("id") String movieId) {
        log.info("Request started to get movie with id: {}", movieId);
//        return infoRestClient.retriveMovieInfo(movieId)
//                .flatMap(movieInfo -> {
//                    var reviewList = reviewRestClient.getMovieReviews(movieId)
//                            .collectList();
//
//                    return reviewList.map(reviews -> new Movie(movieInfo, reviews));
//                });
        return reviewRestClient.getMovieReviews(movieId)
                .collectList()
                .flatMap(reviewList -> infoRestClient.retrieveMovieInfo(movieId)
                        .map(movieInfo -> new Movie(movieInfo, reviewList)));
    }

    @GetMapping
    public Flux<Review> getReviews(@RequestParam("movieInfoId") String movieInfoId) {
        log.info("Getting review for movieInfoId: {}", movieInfoId);

        return reviewRestClient.getMovieReviews(movieInfoId);
    }

    @GetMapping("/info")
    public Mono<MovieInfo> getMovieInfo(@RequestParam("movieInfoId") String movieInfoId) {
        log.info("Getting review for movieInfoId: {}", movieInfoId);

        return infoRestClient.retrieveMovieInfo(movieInfoId);
    }
}
