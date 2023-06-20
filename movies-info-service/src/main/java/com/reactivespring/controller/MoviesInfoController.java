package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.service.MoviesInfoService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import javax.validation.Valid;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1")
public class MoviesInfoController {
    @Autowired
    private MoviesInfoService service;

    Sinks.Many<MovieInfo> movieInfoSinks = Sinks.many().replay().latest();

    private static final Logger logger = LoggerFactory.getLogger(MoviesInfoController.class);

    @GetMapping(value = "/movie-info/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<MovieInfo> streamMovie() {
        return movieInfoSinks.asFlux();
    }

    @PostMapping("/movie-info")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MovieInfo> saveMovieInfo(@RequestBody @Valid MovieInfo movie) {
        return service.saveMovieInfo(movie)
                .doOnNext(savedMovieInfo -> movieInfoSinks.tryEmitNext(savedMovieInfo));
    }

    @GetMapping("/movie-info")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Flux<MovieInfo> getAllMoviesInfo(@RequestParam(value = "year", required = false) Integer year) {
        if (Objects.isNull(year)) {
            return service.getAllMovies();
        }
        return service.getMovieByYear(year);
    }

    @GetMapping("/movie-info/{id}")
    public Mono<ResponseEntity<MovieInfo>> getMovieById(@PathVariable String id) {
        logger.info("Request started with id: {}", id);
        return service.getMovieById(id)
                .map(movieInfo -> ResponseEntity.ok().body(movieInfo))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @PutMapping("/movie-info/{id}")
    public Mono<MovieInfo> updateMovie(@RequestBody @Valid MovieInfo updatedMovieIfo, @PathVariable String id) {
        return service.updateMovie(updatedMovieIfo, id);
    }

    @DeleteMapping("/movie-info/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteMovie(@PathVariable String id) {
        return service.deleteMovieById(id);
    }
}
