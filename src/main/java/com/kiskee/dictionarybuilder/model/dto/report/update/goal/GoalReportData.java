package com.kiskee.dictionarybuilder.model.dto.report.update.goal;

import com.kiskee.dictionarybuilder.model.dto.report.update.ReportData;

public interface GoalReportData<V> extends ReportData {

    V getValue();

    V getGoal();
}
