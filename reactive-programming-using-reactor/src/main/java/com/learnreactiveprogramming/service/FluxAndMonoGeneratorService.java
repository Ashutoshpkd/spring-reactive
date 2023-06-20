package com.learnreactiveprogramming.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;

public class FluxAndMonoGeneratorService {

    public Flux<String> namesFlux() {
        return Flux.fromIterable(List.of("Ashutosh", "Yash", "Manjiri"));
    }

    public Flux<String> namesFlux_map() {
        return Flux.fromIterable(List.of("Ashutosh", "Charul", "Eren"))
                .map(String::toUpperCase);
    }

    public Mono<String> nameMono() {
        Stack<String> s = new Stack<>();
        s.push("Ashutosh");
        s.push("Manjiri");
        String res = s.toString();

        return Mono.just(res);
    }

    public Mono<List<String>> nameMono_filter() {
        return Mono.just(List.of("Ashutosh", "Saitama", "Eren", "Armin"))
                .flatMap(nameList -> Flux.fromIterable(nameList)
                        .filter(name -> name.startsWith("A"))
                        .map(fName -> fName.length() + "-" + fName)
                        .collectList()
                )
                .log();
    }

    public Flux<String> nameFlux_flatMap(String start) {
        return Flux.fromIterable(List.of("Eren", "Saitama", "Armin", "Erwin"))
                .filter(name -> name.startsWith(start))
                .flatMap(this::splitFlux)
                .map(String::toUpperCase)
                .log();
    }

    public Flux<String> splitFlux(String s) {
        var flux = Flux.fromArray(s.split(""));
        return flux;
    }

    public Flux<String> nameFlux_flatMap_withDelay(String start) {
        Integer delay = (new Random()).nextInt(1000);
        return Flux.fromIterable(List.of("Eren", "Saitama", "Armin", "Erwin"))
                .filter(name -> name.startsWith(start))
                .flatMap(this::splitFlux)
                .map(String::toUpperCase)
                .delayElements(Duration.ofMillis(Long.parseLong(Integer.toString(delay))))
                .log();
    }

//    public Mono<Role> getRole(String name) {
//        return Mono.just(Role.roles.get(name));
//    }
//
//    public Flux<String> nameFlux_practice_flatMap() {
//        String[] users = new String[] {"Ashutosh", "Yash", "Manjiri"};
//
//        Flux<String> userFlux = Flux.fromArray(users);
//
//        return userFlux.flatMap(user -> getRole(user))
//                .map(newRole -> {
//                    return newRole;
//                });
//    }

    public Mono<List<String>> nameMono_flatMap() {
        return Mono.just("Ashutosh")
                .map(String::toUpperCase)
                .flatMap(this::splitMono)
                .log();
    }

    public Mono<List<String>> splitMono(String s) {
        String[] cArr = s.split("");
        return Mono.just(List.of(cArr));
    }

    public Flux<String> nameFlux_flatMapMany() {
        return Mono.just("Ashutosh")
                .map(String::toUpperCase)
                .flatMapMany(this::splitFlux)
                .log();
    }

    public Flux<String> nameFlux_transform() {
        Function<Flux<String>, Flux<String>> transformFlux =
                nameList -> nameList
                .map(String::toUpperCase)
                .filter(name -> name.length() > 6);

        return Flux.fromIterable(List.of("Eren", "Erwin", "Armin"))
                .transform(transformFlux);
    }

    public Flux<String> nameFlux_switchIfEmpty() {
        Function<Flux<String>, Flux<String>> transformFlux = nameList ->
                nameList
                        .map(String::toUpperCase)
                        .filter(name -> name.length() > 6);

        var defaultFlux = Flux.just("Default")
                .transform(transformFlux)
                .flatMap(this::splitFlux);

        return Flux.fromIterable(List.of("Eren", "Erwin", "Armin"))
                .transform(transformFlux)
                .switchIfEmpty(defaultFlux);
    }

    public Flux<String> practice_flux(Set<String> arr) {
        return Mono.just("Armin")
                .map(name -> name.toUpperCase())
                .flatMapMany(name -> Flux.fromArray(name.split("")))
                .filter(letter -> !arr.contains(letter));
//                .log();
    }


    public static void main(String[] args) {
        Map<Integer, Integer> map = new HashMap<>();
        FluxAndMonoGeneratorService fluxAndMonoGeneratorService = new FluxAndMonoGeneratorService();

        fluxAndMonoGeneratorService.namesFlux().subscribe(name -> {
            System.out.println("Name is : " + name);
        });
        fluxAndMonoGeneratorService.namesFlux()
                .map(String::toUpperCase)
//                .collectMap(String::toUpperCase)
                .subscribe(name -> {
                    System.out.println("Transformed name : " + name);
                });

        fluxAndMonoGeneratorService.nameMono().subscribe(name  -> {
            System.out.println("Mono name is : " + name);
        });

        fluxAndMonoGeneratorService.namesFlux_map().subscribe(uName -> {
            System.out.println("FluxMap name : " + uName);
        });

        fluxAndMonoGeneratorService.nameMono_flatMap().subscribe(charList -> {
            System.out.println(charList);
        });

        fluxAndMonoGeneratorService.nameMono_filter()
                        .subscribe(listName -> {
                            listName.forEach(name -> {
                                System.out.println("Mono filter name : " + name);
                            });
                        });

        Set<String> arr = new HashSet<>();
        arr.add("A");

        fluxAndMonoGeneratorService.nameFlux_flatMap_withDelay("AS")
                        .subscribe(name -> {
                            System.out.println(name);
                        });

        fluxAndMonoGeneratorService.practice_flux(arr)
                        .subscribe(str -> {
                            System.out.println("Practice: " + str);
                        });

        System.out.println("End of code!");
    }
}

class Role {

    private String role;
    static Map<String, Role> roles = new HashMap<>();

    static {
        roles.put("Ashutosh", new Role("Admin"));
        roles.put("Yash", new Role("Peon"));
        roles.put("Manjiri", new Role("Boss"));
    }

    public Role(String role) {
        this.role = role;
    }
}
