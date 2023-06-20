package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.service.MoviesInfoService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@WebFluxTest(controllers = MoviesInfoController.class)
@AutoConfigureWebTestClient
class MoviesInfoControllerUnitTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private MoviesInfoService serviceMock;

    private static final String MOVIE_INFO_URL = "/api/v1/movie-info";

    @Test
    void saveMovieInfo() {

        MovieInfo movie = new MovieInfo(null, "Test Begins",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        MovieInfo savedmovie = new MovieInfo("abcd", "Test Begins",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        when(serviceMock.saveMovieInfo(isA(MovieInfo.class))).thenReturn(Mono.just(savedmovie));

        webTestClient.post()
                .uri(MOVIE_INFO_URL)
                .bodyValue(movie)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(info -> {
                    assertNotNull(info.getResponseBody().getMovieInfoId());
                    assertEquals("Test Begins", info.getResponseBody().getName());
                });
    }

    @Test
    void getAllMoviesInfo() {
        List<MovieInfo> movieinfos = List.of(new MovieInfo(null, "Test Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Test Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "The Test Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        when(serviceMock.getAllMovies()).thenReturn(Flux.fromIterable(movieinfos));

        webTestClient.get()
                .uri(MOVIE_INFO_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void getMovieById() {
        MovieInfo info = new MovieInfo(null, "Test Begins",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        when(serviceMock.getMovieById(anyString())).thenReturn(Mono.just(info));

        webTestClient.get()
                .uri(MOVIE_INFO_URL + "/{id}", "random")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfo -> {
                    assertEquals("Test Begins", movieInfo.getResponseBody().getName());
                });
    }

    @Test
    void updateMovie() {
    }

    @Test
    void deleteMovie() {
        when(serviceMock.deleteMovieById(ArgumentMatchers.anyString())).thenReturn(Mono.justOrEmpty(null));

        webTestClient.delete()
                .uri(MOVIE_INFO_URL + "/{id}", "random")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Void.class);
    }
}