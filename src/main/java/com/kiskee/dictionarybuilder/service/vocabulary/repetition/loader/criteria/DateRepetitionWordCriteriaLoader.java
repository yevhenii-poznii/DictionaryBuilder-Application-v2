package com.kiskee.dictionarybuilder.service.vocabulary.repetition.loader.criteria;

import com.kiskee.dictionarybuilder.enums.vocabulary.filter.CriteriaFilterType;
import com.kiskee.dictionarybuilder.mapper.repetition.RepetitionWordMapper;
import com.kiskee.dictionarybuilder.model.dto.repetition.RepetitionStartFilterRequest;
import com.kiskee.dictionarybuilder.model.dto.repetition.filter.criteria.DateCriteriaFilter;
import com.kiskee.dictionarybuilder.model.entity.vocabulary.Word;
import com.kiskee.dictionarybuilder.repository.repetition.RepetitionWordRepository;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DateRepetitionWordCriteriaLoader extends AbstractRepetitionWordCriteriaLoader
        implements RepetitionWordCriteriaLoader {

    public DateRepetitionWordCriteriaLoader(RepetitionWordRepository repository, RepetitionWordMapper mapper) {
        super(repository, mapper);
    }

    @Override
    public CriteriaFilterType getFilter() {
        return CriteriaFilterType.BY_DATE;
    }

    @Override
    protected List<Word> loadRepetitionOnly(Long dictionaryId, RepetitionStartFilterRequest request) {
        DateCriteriaFilter.DateRange criteriaFilter =
                (DateCriteriaFilter.DateRange) request.getCriteriaFilter().getFilterValue();
        Instant from = criteriaFilter.getFrom().atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant to = criteriaFilter.getTo().atStartOfDay().plusDays(1).toInstant(ZoneOffset.UTC);

        return getRepository().findByDictionaryIdAndUseInRepetitionAndAddedAtBetween(dictionaryId, true, from, to);
    }

    @Override
    protected List<Word> loadNotRepetitionOnly(Long dictionaryId, RepetitionStartFilterRequest request) {
        DateCriteriaFilter.DateRange criteriaFilter =
                (DateCriteriaFilter.DateRange) request.getCriteriaFilter().getFilterValue();
        Instant from = criteriaFilter.getFrom().atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant to = criteriaFilter.getTo().atStartOfDay().plusDays(1).toInstant(ZoneOffset.UTC);

        return getRepository().findByDictionaryIdAndUseInRepetitionAndAddedAtBetween(dictionaryId, false, from, to);
    }

    @Override
    protected List<Word> loadAll(Long dictionaryId, RepetitionStartFilterRequest request) {
        DateCriteriaFilter.DateRange criteriaFilter =
                (DateCriteriaFilter.DateRange) request.getCriteriaFilter().getFilterValue();
        Instant from = criteriaFilter.getFrom().atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant to = criteriaFilter.getTo().atStartOfDay().plusDays(1).toInstant(ZoneOffset.UTC);

        return getRepository().findByDictionaryIdAndAddedAtBetween(dictionaryId, from, to);
    }
}
