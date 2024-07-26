package com.kiskee.vocabulary.model.entity.report;

import java.util.Set;

public interface Report<RR extends ReportRow> {

    Set<RR> getReportRows();

    Report<RR> buildFrom(Set<RR> updatedReportRows);
}
