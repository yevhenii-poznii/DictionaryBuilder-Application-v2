package com.kiskee.vocabulary.service.report.goal.word.row;

import com.kiskee.vocabulary.model.dto.report.goal.WordAdditionData;
import com.kiskee.vocabulary.model.entity.report.word.WordAdditionGoalReportRow;

public interface WordAdditionGoalReportRowService {

    WordAdditionGoalReportRow buildRowFromScratch(WordAdditionData wordAdditionData);
}
