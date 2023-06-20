package com.reactivespring.repository;

import com.reactivespring.domain.MovieInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@ActiveProfiles("test")
class MovieInfoRepositoryTest {

    @Autowired
    MovieInfoRepository repository;

    @BeforeEach
    void setup() {
        List<MovieInfo> movieinfos = List.of(new MovieInfo(null, "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        repository.saveAll(movieinfos).blockLast();
    }

    @AfterEach
    void teardown() {
        repository.deleteAll().block();
    }

    @Test
    void findAll() {
        var moviesFlux = repository.findAll();

        StepVerifier.create(moviesFlux)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void findById() {
        var movie = repository.findById("abc");

        StepVerifier.create(movie)
//                .expectNextCount(1)
                .assertNext(movieInfo -> {
                    assertEquals("Dark Knight Rises", movieInfo.getName());
                })
                .verifyComplete();
    }

    @Test
    void saveMovie() {
        MovieInfo movie = new MovieInfo(null, "Attack On Titans",
                2020, List.of("Eren Yeager", "Levi Ackerman"),
                LocalDate.of(2023, 03, 01));

        Mono<MovieInfo> savedMovie =  repository.save(movie);

        StepVerifier.create(savedMovie)
//                .expectNextCount(1)
                .assertNext(aot -> {
                    assertNotNull(aot.getMovieInfoId());
                    assertEquals("Attack On Titans", aot.getName());
                })
                .verifyComplete();
    }

    @Test
    void updateMovie() {
        MovieInfo movie = repository.findById("abc").block();
        movie.setName("Test change");

        repository.save(movie).block();

        Mono<MovieInfo> monoMovie = repository.findById("abc");
        StepVerifier.create(monoMovie)
                .assertNext(updatedMovie -> {
                    assertEquals("Test change", updatedMovie.getName());
                })
                .verifyComplete();
    }

    @Test
    void getMovieByYear() {
        Flux<MovieInfo> movieInfoFlux = repository.getMovieInfoByYear(2005);

        StepVerifier.create(movieInfoFlux)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void getMovieByName() {
        Mono<MovieInfo> movieInfoMono = repository.getMovieInfoByName("Batman Begins");
        StepVerifier.create(movieInfoMono)
//                .expectNextCount(1)
                .assertNext(movie -> {
                    assertNotNull(movie);
                    assertEquals("Batman Begins", movie.getName());
                })
                .verifyComplete();
    }
}