package com.kiskee.dictionarybuilder.service.vocabulary.search;

import com.kiskee.dictionarybuilder.repository.vocabulary.projections.WordProjection;
import java.util.List;

public interface SearchService {

    List<WordProjection> search(String searchWord);
}
