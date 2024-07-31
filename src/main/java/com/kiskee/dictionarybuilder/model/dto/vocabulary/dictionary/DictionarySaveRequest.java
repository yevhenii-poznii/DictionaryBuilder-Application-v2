package com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class DictionarySaveRequest {

    @NotNull
    @Size(max = 50)
    @Pattern(
            regexp = "^[a-zA-Zа-яА-Я0-9]+([-_ ]?[a-zA-Zа-яА-Я0-9]+)*$",
            message = "Dictionary name can only contain alphanumeric characters, underscores, hyphens, and spaces "
                    + "within")
    private String dictionaryName;
}
