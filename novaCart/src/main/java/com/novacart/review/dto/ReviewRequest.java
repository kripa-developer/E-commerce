package com.novacart.review.dto;

import jakarta.validation.constraints.*;

public record ReviewRequest(
        @NotNull @Min(1) @Max(5) Integer rating,
        @NotBlank @Size(max = 200) String title,
        @Size(max = 2000) String body
) {}
