package com.kiskee.vocabulary.model.dto.report.update.goal;

import com.kiskee.vocabulary.model.dto.report.update.ReportData;

public interface GoalReportData<V> extends ReportData {

    V getValue();

    V getGoal();
}
