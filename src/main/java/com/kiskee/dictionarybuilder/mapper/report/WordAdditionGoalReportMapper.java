package com.kiskee.dictionarybuilder.mapper.report;

import com.kiskee.dictionarybuilder.model.dto.report.BaseReportRowDto;
import com.kiskee.dictionarybuilder.model.dto.report.goal.word.DictionaryWordAdditionGoalReportDto;
import com.kiskee.dictionarybuilder.model.dto.report.goal.word.WordAdditionGoalReportDto;
import com.kiskee.dictionarybuilder.model.entity.report.goal.word.DictionaryWordAdditionGoalReport;
import com.kiskee.dictionarybuilder.model.entity.report.goal.word.WordAdditionGoalReport;
import com.kiskee.dictionarybuilder.model.entity.report.goal.word.WordAdditionGoalReportRow;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WordAdditionGoalReportMapper {

    WordAdditionGoalReportDto toDto(WordAdditionGoalReport report);

    @Mapping(target = "reportPeriod", expression = "java(reportRow.getRowPeriod())")
    BaseReportRowDto toDto(WordAdditionGoalReportRow reportRow);

    DictionaryWordAdditionGoalReportDto toDto(DictionaryWordAdditionGoalReport dictionaryReport);
}
