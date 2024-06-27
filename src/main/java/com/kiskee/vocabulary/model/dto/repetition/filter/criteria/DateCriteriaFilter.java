package com.kiskee.vocabulary.model.dto.repetition.filter.criteria;

import com.kiskee.vocabulary.model.dto.repetition.filter.CriteriaFilter;
import com.kiskee.vocabulary.model.dto.repetition.filter.DefaultCriteriaFilter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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
