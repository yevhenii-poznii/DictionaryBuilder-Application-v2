package com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.page;

import com.kiskee.dictionarybuilder.enums.vocabulary.filter.PageFilter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class DictionaryPageRequestDto {

    @PositiveOrZero
    private Integer page;

    @Min(20)
    @Max(100)
    private Integer size;

    private PageFilter filter;
}
