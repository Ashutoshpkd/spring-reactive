package com.reactivespring.handler;

import com.reactivespring.domain.Review;
import com.reactivespring.exception.ReviewDataException;
import com.reactivespring.exception.ReviewNotFoundException;
import com.reactivespring.repository.MovieReviewRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
@Slf4j
public class MovieReviewRouteHandler {

    @Autowired
    private Validator validator;

    private MovieReviewRepository repository;

    public Mono<ServerResponse> addMovieReview(ServerRequest request) {
        return request.bodyToMono(Review.class)
                .doOnNext(this::validate)
                .flatMap(movieReview  -> repository.save(movieReview))
                .flatMap(movieReview -> ServerResponse.status(HttpStatus.CREATED).bodyValue(movieReview));
    }

    private void validate(Review review) {
        var constraintValidator = validator.validate(review);
        log.info("ConstraintViolations : {}", constraintValidator);

        if (constraintValidator.size() > 0) {
            var errorMessage = constraintValidator
                    .stream()
                    .sorted()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));

            throw new ReviewDataException(errorMessage);
        }
    }

    public Mono<ServerResponse> getAllReviews(ServerRequest request) {

        var movieInfoId = request.queryParam("movieInfoId");

        if(movieInfoId.isPresent()){
            var reviewsFlux = repository.findMovieReviewByMovieInfoId(movieInfoId.get());
            return buildReviewsResponse(reviewsFlux);
        } else {
            var reviewsFlux = repository.findAll();
            return buildReviewsResponse(reviewsFlux);
        }
    }

    private Mono<ServerResponse> buildReviewsResponse(Flux<Review> reviewsFlux) {
        return ServerResponse.ok().body(reviewsFlux, Review.class);
    }

    public Mono<ServerResponse> updateReview(ServerRequest request) {
        String id = request.pathVariable("id");
        var existingReview = repository.findById(id)
                .switchIfEmpty(Mono.error(new ReviewNotFoundException("Review not found with id: " + id)));

        return existingReview.flatMap(review ->
            request.bodyToMono(Review.class)
                    .map(newReview -> {
                        review.setComment(newReview.getComment());
                        review.setMovieInfoId(newReview.getMovieInfoId());
                        review.setRating(newReview.getRating());

                        return review;
                    })
                    .flatMap(repository::save)
                    .flatMap(savedReview -> ServerResponse.ok().bodyValue(savedReview))
        );
    }

    public Mono<ServerResponse> getReview(String id) {
        return repository.findById(id)
                .flatMap(review -> ServerResponse.ok().bodyValue(review));
    }

    public Mono<ServerResponse> deleteReview(String id) {
        return repository.deleteById(id)
                .flatMap(review -> ServerResponse.status(HttpStatus.NO_CONTENT).build());
    }
}
