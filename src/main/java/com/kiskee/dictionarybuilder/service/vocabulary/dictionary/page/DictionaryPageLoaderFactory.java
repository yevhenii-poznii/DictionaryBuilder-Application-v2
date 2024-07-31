package com.kiskee.dictionarybuilder.service.vocabulary.dictionary.page;

import com.kiskee.dictionarybuilder.enums.vocabulary.PageFilter;
import com.kiskee.dictionarybuilder.service.vocabulary.word.page.DictionaryPageLoader;

public interface DictionaryPageLoaderFactory {

    DictionaryPageLoader getLoader(PageFilter filter);
}
