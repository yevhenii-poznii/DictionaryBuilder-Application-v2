package com.kiskee.vocabulary.mapper.report;

import com.kiskee.vocabulary.model.dto.report.BaseReportRowDto;
import com.kiskee.vocabulary.model.dto.report.progress.DictionaryRepetitionStatisticReportDto;
import com.kiskee.vocabulary.model.dto.report.progress.RepetitionStatisticReportDto;
import com.kiskee.vocabulary.model.entity.report.progress.repetition.DictionaryRepetitionStatisticReport;
import com.kiskee.vocabulary.model.entity.report.progress.repetition.RepetitionStatisticReport;
import com.kiskee.vocabulary.model.entity.report.progress.repetition.RepetitionStatisticReportRow;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RepetitionStatisticReportMapper {

    RepetitionStatisticReportDto toDto(RepetitionStatisticReport report);

    @Mapping(target = "reportPeriod", expression = "java(reportRow.getRowPeriod())")
    BaseReportRowDto toDto(RepetitionStatisticReportRow reportRow);

    DictionaryRepetitionStatisticReportDto toDto(DictionaryRepetitionStatisticReport dictionaryReport);
}
