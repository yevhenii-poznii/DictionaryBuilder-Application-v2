package com.kiskee.dictionarybuilder.service.share;

import com.kiskee.dictionarybuilder.model.dto.share.ShareDictionaryRequest;
import com.kiskee.dictionarybuilder.model.dto.share.SharedDictionaries;
import com.kiskee.dictionarybuilder.model.dto.share.SharedDictionaryDto;
import com.kiskee.dictionarybuilder.model.dto.share.SharedDictionaryPage;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.page.DictionaryPageRequestDto;

public interface ShareService {

    SharedDictionaryPage getSharedDictionaryPage(String sharingToken, DictionaryPageRequestDto dictionaryPageRequest);

    SharedDictionaryDto shareDictionary(ShareDictionaryRequest request);

    SharedDictionaries getSharedDictionaries();

    SharedDictionaries getSharedTokensByDictionary(Long dictionaryId);

    void revokeSharingToken(String sharingToken);

    void revokeAllSharingTokens();
}
