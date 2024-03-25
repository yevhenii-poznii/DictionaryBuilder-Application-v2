package com.kiskee.vocabulary.model.dto.vocabulary.word;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class WordSaveRequest {

    @NotNull
    @Pattern(regexp = "^[a-zA-Z\\-()\\s]+$",
            message = "Word must contain only letters, hyphens, brackets and spaces")
    private String word;

    @Valid
    @NotNull
    @Size(min = 1, message = "At least one translation is required")
    private List<WordTranslationDto> wordTranslations;

    private String wordHint;

}
