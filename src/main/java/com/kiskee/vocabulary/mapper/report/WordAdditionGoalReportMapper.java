package com.kiskee.vocabulary.mapper.report;

import com.kiskee.vocabulary.model.dto.report.BaseReportRowDto;
import com.kiskee.vocabulary.model.dto.report.goal.word.DictionaryWordAdditionGoalReportDto;
import com.kiskee.vocabulary.model.dto.report.goal.word.WordAdditionGoalReportDto;
import com.kiskee.vocabulary.model.entity.report.goal.word.DictionaryWordAdditionGoalReport;
import com.kiskee.vocabulary.model.entity.report.goal.word.WordAdditionGoalReport;
import com.kiskee.vocabulary.model.entity.report.goal.word.WordAdditionGoalReportRow;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WordAdditionGoalReportMapper {

    WordAdditionGoalReportDto toDto(WordAdditionGoalReport report);

    @Mapping(target = "reportPeriod", expression = "java(reportRow.getRowPeriod())")
    BaseReportRowDto toDto(WordAdditionGoalReportRow reportRow);

    DictionaryWordAdditionGoalReportDto toDto(DictionaryWordAdditionGoalReport dictionaryReport);
}
