package com.kiskee.vocabulary.model.dto.report.goal;

import com.kiskee.vocabulary.model.dto.report.ReportData;

public interface GoalReportData<V> extends ReportData {

    V getValue();

    V getGoal();
}
