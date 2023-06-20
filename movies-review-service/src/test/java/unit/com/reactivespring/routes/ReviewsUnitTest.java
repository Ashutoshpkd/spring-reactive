package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.handler.MovieReviewRouteHandler;
import com.reactivespring.repository.MovieReviewRepository;
import com.reactivespring.router.MovieRouter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@WebFluxTest
@AutoConfigureWebTestClient
@ContextConfiguration(classes = {MovieRouter.class, MovieReviewRouteHandler.class})
public class ReviewsUnitTest {

    @MockBean
    private MovieReviewRepository repository;

    @Autowired
    private WebTestClient webTestClient;

    final static String REVIEW_URL = "/api/v1/movie-review";

    @Test
    void addReview()  {
        Review review = new Review("", "abc", "Test", 9.0);

        when(repository.save(isA(Review.class)))
                .thenReturn(Mono.just(new Review("test", "abc", "Test", 9.0)));

        webTestClient.post()
                .uri(REVIEW_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(Review.class)
                .consumeWith(movieReview -> {
                    assertNotNull(movieReview.getResponseBody().getReviewId());
                    assertEquals("test", movieReview.getResponseBody().getReviewId());
                });
    }

    @Test
    void getAllReviews() {
        List<Review> listReview = List.of(new Review("1", "abc", "Test_1", 9.0),
                new Review("2", "abc", "Test_2", 10.0));

        when(repository.findAll()).thenReturn(Flux.fromIterable(listReview));

        webTestClient.get()
                .uri(REVIEW_URL)
                .exchange()
                .expectBodyList(Review.class)
                .hasSize(2);
    }

    @Test
    void updateReview() {
        Review review = new Review("testId", "abc", "Test", 9.0);
        Review updatedReview = new Review("testId", "abc", "Test", 9.9);

        when(repository.findById(isA(String.class))).thenReturn(Mono.just(review));
        when(repository.save(isA(Review.class))).thenReturn(Mono.just(updatedReview));

        webTestClient.put()
                .uri(REVIEW_URL + "/{id}", "testId")
                .bodyValue(updatedReview)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Review.class)
                .consumeWith(ur ->  {
                    assertEquals(9.9, ur.getResponseBody().getRating());
                });
    }
}
