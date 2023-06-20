package com.reactivespring.controller;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.print.attribute.standard.Media;
import java.time.Duration;
import java.util.Random;
import java.util.function.Supplier;

@RestController
public class FluxAndMonoController {

    @GetMapping("/flux")
    public Flux<Integer> flux() {
        return Flux.just(1,2,3).log();
    }

    @GetMapping("/mono")
    public Mono<String> mono() {
        return Mono.just("hello-world").log();
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Long> stream() {
        Supplier<Integer> supp = () -> {
            Random random = new Random();
            return random.nextInt(10);
        };

        return Flux.interval(Duration.ofMillis(1000));
    }
}
