package com.kiskee.vocabulary.model.dto.vocabulary.word;

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
public class WordUpdateRequest {

    @NotNull
    @Pattern(regexp = "^[a-zA-Z\\-()\\s]+$", message = "Word must contain only letters, hyphens, brackets and spaces")
    private String word;

    //    @NotNull
    //    private Boolean useInRepetition;

    @Valid
    @NotNull
    @Size(min = 1, message = "At least one translation is required")
    private List<WordTranslationDto> wordTranslations;

    private String wordHint;
}
