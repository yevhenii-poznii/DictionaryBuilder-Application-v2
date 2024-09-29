package com.kiskee.dictionarybuilder.service.share;

import com.kiskee.dictionarybuilder.exception.token.ExpiredTokenException;
import com.kiskee.dictionarybuilder.model.dto.share.ShareDictionaryRequest;
import com.kiskee.dictionarybuilder.model.dto.share.SharedDictionaryDto;
import com.kiskee.dictionarybuilder.model.dto.share.SharedDictionaryPage;
import com.kiskee.dictionarybuilder.model.dto.token.share.SharingTokenData;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.DictionaryDto;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.page.DictionaryPageRequestDto;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.page.DictionaryPageResponseDto;
import com.kiskee.dictionarybuilder.service.security.token.deserializer.TokenDeserializer;
import com.kiskee.dictionarybuilder.service.time.CurrentDateTimeService;
import com.kiskee.dictionarybuilder.service.token.share.SharingTokenService;
import com.kiskee.dictionarybuilder.service.vocabulary.AbstractDictionaryService;
import com.kiskee.dictionarybuilder.service.vocabulary.dictionary.DictionaryAccessValidator;
import com.kiskee.dictionarybuilder.service.vocabulary.dictionary.page.DictionaryPageLoaderFactory;
import com.kiskee.dictionarybuilder.util.IdentityUtil;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Getter
@Service
@RequiredArgsConstructor
public class ShareServiceImpl extends AbstractDictionaryService implements ShareService {

    private final SharingTokenService sharingTokenService;
    private final TokenDeserializer<String, SharingTokenData> tokenDeserializer;
    private final DictionaryAccessValidator dictionaryAccessValidator;
    private final DictionaryPageLoaderFactory dictionaryPageLoaderFactory;
    private final CurrentDateTimeService currentDateTimeService;

    @Override
    @Transactional(noRollbackFor = ExpiredTokenException.class)
    public SharedDictionaryPage getSharedDictionaryPage(
            String sharingToken, DictionaryPageRequestDto dictionaryPageRequest) {
        SharingTokenData sharingTokenData = tokenDeserializer.deserialize(sharingToken, SharingTokenData.class);
        if (currentDateTimeService.getCurrentInstant().isAfter(sharingTokenData.getExpiresAt())) {
            if (sharingTokenService.isNotInvalidated(sharingToken)) {
                sharingTokenService.invalidateToken(sharingToken);
            }
            throw new ExpiredTokenException("Token has expired");
        }
        DictionaryDto dictionaryDto = dictionaryAccessValidator.getDictionaryByIdAndUserId(
                sharingTokenData.getDictionaryId(), sharingTokenData.getUserId());
        DictionaryPageResponseDto dictionaryPage = load(sharingTokenData.getDictionaryId(), dictionaryPageRequest);
        return new SharedDictionaryPage(dictionaryDto.getDictionaryName(), dictionaryPage);
    }

    @Override
    @Transactional
    public SharedDictionaryDto shareDictionary(ShareDictionaryRequest request) {
        dictionaryAccessValidator.verifyUserHasDictionary(request.dictionaryId());
        UUID userId = IdentityUtil.getUserId();
        SharingTokenData sharingTokenData = new SharingTokenData(userId, request.dictionaryId(), request.shareToDate());
        String sharingToken = sharingTokenService.persistToken(sharingTokenData);
        return new SharedDictionaryDto(request.dictionaryId(), sharingToken, request.shareToDate());
    }
}
