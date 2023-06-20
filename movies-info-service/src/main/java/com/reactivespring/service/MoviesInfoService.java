package com.reactivespring.service;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.repository.MovieInfoRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Service
@AllArgsConstructor
public class MoviesInfoService {

    private static final Logger logger = LoggerFactory.getLogger(MoviesInfoService.class);

    private MovieInfoRepository repository;

    public Mono<MovieInfo> saveMovieInfo(MovieInfo movie) {
        return repository.save(movie);
    }

    public Flux<MovieInfo> getAllMovies() {
        return repository.findAll();
    }

    public Mono<MovieInfo> getMovieById(String id) {
        logger.info("Getting movie info with id: {}", id);
        var movieInfo = repository.findById(id);
        logger.info("Movie Info: {}", movieInfo);

        return movieInfo;
    }

    public Mono<MovieInfo> updateMovie(MovieInfo newMovieInfo, String id) {
        var monoMovie = repository.findById(id);
//        var updatedMovie = monoMovie.flatMap(movieInfo -> {
//            movieInfo.setName(newMovieInfo.getName());
//            movieInfo.setYear(newMovieInfo.getYear());
//            movieInfo.setCast(newMovieInfo.getCast());
//            movieInfo.setRelease_date(newMovieInfo.getRelease_date());
//
//            return repository.save(movieInfo);
//        });
        Function<MovieInfo, Mono<MovieInfo>> updateMovie = movieInfo -> {
            movieInfo.setName(newMovieInfo.getName());
            movieInfo.setYear(newMovieInfo.getYear());
            movieInfo.setCast(newMovieInfo.getCast());
            movieInfo.setRelease_date(newMovieInfo.getRelease_date());

            return repository.save(movieInfo);
        };

        var updatedMovie = monoMovie.flatMap(updateMovie);

        return updatedMovie;
    }

    public Mono<Void> deleteMovieById(String id) {
        return repository.deleteById(id);
    }

    public Flux<MovieInfo> getMovieByYear(Integer year) {
        return repository.getMovieInfoByYear(year);
    }
}
