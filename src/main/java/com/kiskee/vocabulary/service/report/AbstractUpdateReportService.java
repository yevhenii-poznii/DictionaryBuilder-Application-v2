package com.kiskee.vocabulary.service.report;

import com.kiskee.vocabulary.model.dto.report.UpdateReportResult;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.retry.annotation.Recover;

@Slf4j
public abstract class AbstractUpdateReportService<E> {

    protected abstract JpaRepository<E, Long> getRepository();

    @Recover
    private CompletableFuture<UpdateReportResult> recoverUpdateReport(Exception exception) {
        log.error("Max attempts of retries has been run out with error", exception);

        String causedBy = String.format(
                "%s %s. Caused by: %s",
                exception.getClass().getSimpleName(), exception.getMessage(), exception.getCause());

        return CompletableFuture.completedFuture(new UpdateReportResult(Boolean.FALSE, causedBy));
    }
}
