package com.kiskee.dictionarybuilder.model.dto.vocabulary.word;

import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@EqualsAndHashCode(of = "translation")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WordTranslationDto {

    private Long id;

    @Pattern(
            regexp = "^[\\p{L}\\-/'`(),!?\\s]+$",
            message =
                    "Translation must contain only letters, hyphens, slashes, commas, brackets, spaces, single quotes, and backticks")
    private String translation;

    public WordTranslationDto(String translation) {
        this.translation = translation;
    }
}
