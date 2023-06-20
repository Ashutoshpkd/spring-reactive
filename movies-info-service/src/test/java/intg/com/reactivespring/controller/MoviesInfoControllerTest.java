package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.repository.MovieInfoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class MoviesInfoControllerTest {

    @Autowired
    MovieInfoRepository repository;

    @Autowired
    WebTestClient webTestClient;

    private static final String MOVIE_INFO_URL = "/api/v1/movie-info";

    @BeforeEach
    void setUp() {
        List<MovieInfo> movieinfos = List.of(new MovieInfo(null, "Test Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Test Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "The Test Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        repository.saveAll(movieinfos).blockLast();
    }

    @AfterEach
    void tearDown() {
        repository.deleteAll().block();
    }

    @Test
    void saveMovieInfo() {
        webTestClient.post()
                .uri(MOVIE_INFO_URL)
                .bodyValue(new MovieInfo(null, "Test Begins Now",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movie -> {
                    assertNotNull(movie.getResponseBody().getMovieInfoId());
                });
    }

    @Test
    void getAllMoviesInfo() {
        webTestClient.get()
                .uri(MOVIE_INFO_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .returnResult(MovieInfo.class)
                .consumeWith(movies -> {
                    var flux = movies.getResponseBody();
                    StepVerifier.create(flux)
                            .expectNextCount(3)
                            .verifyComplete();
                });
    }

    @Test
    void getMovieById() {
        webTestClient.get()
                .uri(MOVIE_INFO_URL + "/abc")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfo -> {
                    MovieInfo foundMovie = movieInfo.getResponseBody();
                    assertNotNull(foundMovie);
                    assertEquals("abc", foundMovie.getMovieInfoId());
                });
    }

    @Test
    void updateMovie() {
        MovieInfo updatedMovieInfo = new MovieInfo("abcd", "The Test Updates",
                2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));

        webTestClient.put()
                .uri(MOVIE_INFO_URL + "/{id}", "abc")
                .bodyValue(updatedMovieInfo)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(updatedMovie -> {
                    MovieInfo um = updatedMovie.getResponseBody();
                    assertNotNull(um);
                    assertEquals("abc", um.getMovieInfoId());
                    assertEquals("The Test Updates", um.getName());
                });
    }

    @Test
    void deleteMovie() {
        webTestClient.delete()
                .uri(MOVIE_INFO_URL + "/{id}", "abc")
                .exchange()
                .expectStatus()
                .isNoContent()
                .expectBody(Void.class);
    }

    @Test
    void getMovieById_NotFound() {
        webTestClient.get()
                .uri(MOVIE_INFO_URL + "/{id}", "notfound")
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void getMovieInfoByYear() {
        var uri = UriComponentsBuilder
                .fromUriString(MOVIE_INFO_URL)
                .queryParam("year", 2005)
                .buildAndExpand()
                .toUri();

        webTestClient.get()
                .uri(uri)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(1);


    }

    @Test
    void streamMovieInfo() {
        webTestClient.post()
                .uri(MOVIE_INFO_URL)
                .bodyValue(new MovieInfo(null, "Streaming test begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movie -> {
                    assertNotNull(movie.getResponseBody().getMovieInfoId());
                });

        var movieInfoFlux = webTestClient.get()
                .uri(MOVIE_INFO_URL + "/stream")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .returnResult(MovieInfo.class)
                .getResponseBody();

        StepVerifier.create(movieInfoFlux)
                .assertNext(movieInfo -> {
                    System.out.println("ASHUTOSH - " + movieInfo);
                    assertNotNull(movieInfo.getMovieInfoId());
                })
                .thenCancel()
                .verify();
    }
}