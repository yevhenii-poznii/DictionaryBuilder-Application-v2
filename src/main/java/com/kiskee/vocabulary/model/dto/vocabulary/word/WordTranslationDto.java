package com.kiskee.vocabulary.model.dto.vocabulary.word;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class WordTranslationDto {

    private Long id;

    @Pattern(regexp = "^[а-щьюяєіїґА-ЩЬЮЯЄІЇҐ'\\-()\\s]+$",
            message = "Translation must contain only letters, hyphens, brackets, apostrophes and spaces")
    private String translation;

}
