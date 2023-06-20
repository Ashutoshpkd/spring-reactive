package com.reactivespring.router;

import com.reactivespring.handler.MovieReviewRouteHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class MovieRouter {

    @Autowired
    private MovieReviewRouteHandler handler;

    @Bean
    public RouterFunction<ServerResponse> helloWorld() {
        return route().nest(path("/api/v1/movie-review"), builder -> {
            builder.POST("", (request -> handler.addMovieReview(request)))
                    .GET("", (request -> handler.getAllReviews(request)))
                    .GET("/{id}", (request -> handler.getReview(request.pathVariable("id"))))
                    .DELETE("/{id}", (request -> handler.deleteReview(request.pathVariable("id"))))
                    .PUT("/{id}", (request -> handler.updateReview(request)));
                })
                .GET("/api/v1/hello-world", (request -> ServerResponse.ok().bodyValue("Hello-World!")
                ))
                .build();
    }
}
