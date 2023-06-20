package com.reactivespring.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.reactivespring.domain.Movie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@AutoConfigureWireMock(port = 8084)
@TestPropertySource(
        properties = {
                "restClient.moviesInfoUrl=http://localhost:8084/api/v1/movie-info",
                "restClient.movieReviewUrl=http://localhost:8084/api/v1/movie-review"
        }
)
public class MoviesControllerIntgTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void retriveMovieById() {
        String movieId = "abc";

        stubFor(get(urlEqualTo("/api/v1/movie-info/" + movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movieinfo.json")));

        stubFor(get(urlEqualTo("/api/v1/movie-review?movieInfoId="+movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("reviews.json")));

        webTestClient.get()
                .uri("/api/v1/movies/"+movieId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Movie.class)
                .consumeWith(movieEntity -> {
                    var movie = movieEntity.getResponseBody();
                    assert Objects.requireNonNull(movie).getReviewList().size() == 2;
                });
    }

    @Test
    public void retriveMovies_404_MovieInfo() {
        String movieId = "abc";

        stubFor(get(urlEqualTo("/api/v1/movie-info/" + movieId))
                .willReturn(aResponse().withStatus(404)));

        stubFor(get(urlEqualTo("/api/v1/movie-review?movieInfoId=abc"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("reviews.json")));

        webTestClient.get()
                .uri("/api/v1/movies/" + movieId)
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody(String.class)
                .consumeWith(message -> {
                    assert message.getResponseBody().equals("Movie Info not found for id: abc");
                });
    }

    @Test
    public void retriveMovies_404_MovieReviews() {
        String movieId = "abc";

        stubFor(get(urlEqualTo("/api/v1/movie-info/" + movieId))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("movieinfo.json")));

        stubFor(get(urlEqualTo("/api/v1/movie-review?movieInfoId=abc"))
                .willReturn(aResponse().withStatus(404)));

        webTestClient.get()
                .uri("/api/v1/movies/" + movieId)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Movie.class)
                .consumeWith(movie -> {
                    assert Objects.requireNonNull(movie).getResponseBody().getReviewList().size() == 0;
                });
    }

    @Test
    public void retriveMovies_500_MovieReview() {
        String movieId = "abc";

        stubFor(get(urlEqualTo("/api/v1/movie-info/" + movieId))
                .willReturn(aResponse().withStatus(500)
                        .withBody("Movie Info service unavailable")));

        stubFor(get(urlEqualTo("/api/v1/movie-review?movieInfoId=abc"))
                .willReturn(
                        aResponse().withStatus(500)
                                .withBody("Movie reviews service unavailable")
                ));

        webTestClient.get()
                .uri("/api/v1/movies/" + movieId)
                .exchange()
                .expectStatus()
                .is5xxServerError();

        WireMock.verify(4, getRequestedFor(urlEqualTo("/api/v1/movie-review?movieInfoId=" + movieId)));
    }

    @Test
    public void retriveMovies_500_Movieinfo() {
        String movieId = "abc";

        stubFor(get(urlEqualTo("/api/v1/movie-info/" + movieId))
                .willReturn(aResponse().withStatus(500)
                        .withBody("Movie Info service unavailable")));

        stubFor(get(urlEqualTo("/api/v1/movie-review?movieInfoId=abc"))
                .willReturn(
                        aResponse().withHeader("Content-Type", "application/json")
                                .withBodyFile("reviews.json")
                ));

        webTestClient.get()
                .uri("/api/v1/movies/" + movieId)
                .exchange()
                .expectStatus()
                .is5xxServerError();

        WireMock.verify(4, getRequestedFor(urlEqualTo("/api/v1/movie-info/" + movieId)));
    }
}
