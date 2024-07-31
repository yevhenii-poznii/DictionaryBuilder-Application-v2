package com.kiskee.dictionarybuilder.model.dto.repetition.filter.criteria;

import com.kiskee.dictionarybuilder.model.dto.repetition.filter.CriteriaFilter;
import com.kiskee.dictionarybuilder.model.dto.repetition.filter.DefaultCriteriaFilter;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CountCriteriaFilter extends DefaultCriteriaFilter implements CriteriaFilter {

    @NotNull
    private Integer filterValue;
}
