package com.kiskee.dictionarybuilder.model.dto.repetition.filter.criteria;

import com.kiskee.dictionarybuilder.model.dto.repetition.filter.CriteriaFilter;
import com.kiskee.dictionarybuilder.model.dto.repetition.filter.DefaultCriteriaFilter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DateCriteriaFilter extends DefaultCriteriaFilter implements CriteriaFilter {

    @Valid
    @NotNull
    private DateRange filterValue;

    @Data
    @AllArgsConstructor
    public static class DateRange {
        @NotNull
        private LocalDate from;

        @NotNull
        private LocalDate to;
    }
}
