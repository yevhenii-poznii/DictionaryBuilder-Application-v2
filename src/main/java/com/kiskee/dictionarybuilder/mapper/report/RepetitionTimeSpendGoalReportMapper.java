package com.kiskee.dictionarybuilder.mapper.report;

import com.kiskee.dictionarybuilder.model.dto.report.BaseReportRowDto;
import com.kiskee.dictionarybuilder.model.dto.report.goal.time.DictionaryRepetitionTimeSpendGoalReportDto;
import com.kiskee.dictionarybuilder.model.dto.report.goal.time.RepetitionTimeSpendGoalReportDto;
import com.kiskee.dictionarybuilder.model.entity.report.goal.time.DictionaryRepetitionTimeSpendGoalReport;
import com.kiskee.dictionarybuilder.model.entity.report.goal.time.RepetitionTimeSpendGoalReport;
import com.kiskee.dictionarybuilder.model.entity.report.goal.time.RepetitionTimeSpendGoalReportRow;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RepetitionTimeSpendGoalReportMapper {

    RepetitionTimeSpendGoalReportDto toDto(RepetitionTimeSpendGoalReport report);

    @Mapping(target = "reportPeriod", expression = "java(reportRow.getRowPeriod())")
    BaseReportRowDto toDto(RepetitionTimeSpendGoalReportRow reportRow);

    DictionaryRepetitionTimeSpendGoalReportDto toDto(DictionaryRepetitionTimeSpendGoalReport dictionaryReport);
}
