package com.reactivespring.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
public class Review {

    @Id
    private String reviewId;

    @NotBlank(message = "Review: movieInfoId cannot be blank")
    private String movieInfoId;
    @NotBlank(message = "Review: comment cannot be blank")
    private String comment;
    @Min(value = 0L, message = "Review : rating is negative and please pass a non-negative value")
    private Double rating;
}
