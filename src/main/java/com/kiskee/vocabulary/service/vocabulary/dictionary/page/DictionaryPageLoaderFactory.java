package com.kiskee.vocabulary.service.vocabulary.dictionary.page;

import com.kiskee.vocabulary.enums.vocabulary.PageFilter;
import com.kiskee.vocabulary.service.vocabulary.word.page.DictionaryPageLoader;

public interface DictionaryPageLoaderFactory {

    DictionaryPageLoader getLoader(PageFilter filter);

}
