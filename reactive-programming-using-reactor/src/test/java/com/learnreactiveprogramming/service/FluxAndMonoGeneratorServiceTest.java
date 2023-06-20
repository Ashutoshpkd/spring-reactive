package com.learnreactiveprogramming.service;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FluxAndMonoGeneratorServiceTest {
    FluxAndMonoGeneratorService fluxAndMonoGeneratorService = new FluxAndMonoGeneratorService();

    @Test
    void nameFlux() {
        var flux = fluxAndMonoGeneratorService.namesFlux();
        StepVerifier.create(flux)
                .expectNext("Ashutosh")
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void nameFlux_map() {
        var flux = fluxAndMonoGeneratorService.namesFlux_map();
        StepVerifier.create(flux)
                .expectNext("ASHUTOSH")
                .expectNext("CHARUL")
                .expectNext("EREN")
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void nameFlux_flatMap() {
        var flux = fluxAndMonoGeneratorService.nameFlux_flatMap("E");

        StepVerifier.create(flux)
                .expectNext("E", "R", "E", "N", "E", "R", "W", "I", "N")
                .verifyComplete();
    }

    @Test
    void nameFlux_flatMap_withDelay() {
        Flux<String> flux = fluxAndMonoGeneratorService.nameFlux_flatMap_withDelay("E");
        StepVerifier.create(flux)
                .expectNextCount(9)
                .verifyComplete();
    }

    @Test
    void nameMono_flatMap() {
        var flux = fluxAndMonoGeneratorService.nameMono_flatMap();
        StepVerifier.create(flux).expectNext(List.of("A", "S", "H", "U", "T", "O", "S", "H"))
                .verifyComplete();
    }

    @Test
    void nameFlux_flatMapMany() {
        var flux = fluxAndMonoGeneratorService.nameFlux_flatMapMany();
        StepVerifier.create(flux)
                .expectNext("A", "S", "H", "U", "T", "O", "S", "H")
                .verifyComplete();
    }

    @Test
    void nameFlux_switchIfEmpty() {
        var flux = fluxAndMonoGeneratorService.nameFlux_switchIfEmpty();
        StepVerifier.create(flux)
                .expectNext("D", "E", "F", "A", "U", "L", "T")
                .verifyComplete();
    }
}