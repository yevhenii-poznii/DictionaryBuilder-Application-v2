package com.kiskee.dictionarybuilder.model.dto.vocabulary.word;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class WordSaveUpdateRequest {

    @NotNull
    @Pattern(
            regexp = "^[\\p{L}\\-/'`(),!?\\s]+$",
            message =
                    "Word must contain only letters, hyphens, slashes, commas, brackets, spaces, single quotes, and backticks")
    private String word;

    @Valid
    @NotNull
    @Size(min = 1, message = "At least one translation is required")
    private List<WordTranslationDto> wordTranslations;

    private String wordHint;
}
