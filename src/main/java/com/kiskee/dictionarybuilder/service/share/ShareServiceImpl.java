package com.kiskee.dictionarybuilder.service.share;

import com.kiskee.dictionarybuilder.enums.vocabulary.filter.PageFilter;
import com.kiskee.dictionarybuilder.exception.ResourceNotFoundException;
import com.kiskee.dictionarybuilder.exception.token.InvalidTokenException;
import com.kiskee.dictionarybuilder.model.dto.share.ShareDictionaryRequest;
import com.kiskee.dictionarybuilder.model.dto.share.SharedDictionaries;
import com.kiskee.dictionarybuilder.model.dto.share.SharedDictionaryDto;
import com.kiskee.dictionarybuilder.model.dto.share.SharedDictionaryPage;
import com.kiskee.dictionarybuilder.model.dto.token.share.SharingTokenData;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.DictionaryDto;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.page.DictionaryPageRequestDto;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.page.DictionaryPageResponseDto;
import com.kiskee.dictionarybuilder.service.security.token.deserializer.TokenDeserializationHandler;
import com.kiskee.dictionarybuilder.service.token.share.SharingTokenIssuer;
import com.kiskee.dictionarybuilder.service.vocabulary.AbstractDictionaryService;
import com.kiskee.dictionarybuilder.service.vocabulary.dictionary.DictionaryAccessValidator;
import com.kiskee.dictionarybuilder.service.vocabulary.loader.factory.WordLoaderFactory;
import com.kiskee.dictionarybuilder.service.vocabulary.word.page.DictionaryPageLoader;
import com.kiskee.dictionarybuilder.util.IdentityUtil;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public class ShareServiceImpl extends AbstractDictionaryService implements ShareService {

    private final SharingTokenIssuer sharingTokenIssuer;
    private final TokenDeserializationHandler<SharingTokenData> tokenDeserializationHandler;
    private final DictionaryAccessValidator dictionaryAccessValidator;
    private final WordLoaderFactory<PageFilter, DictionaryPageLoader> dictionaryPageLoaderFactory;

    @Override
    @Transactional(noRollbackFor = InvalidTokenException.class)
    public SharedDictionaryPage getSharedDictionaryPage(
            String sharingToken, DictionaryPageRequestDto dictionaryPageRequest) {
        SharingTokenData sharingTokenData =
                tokenDeserializationHandler.deserializeToken(sharingToken, SharingTokenData.class);
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
        String sharingToken = sharingTokenIssuer.persistToken(sharingTokenData);
        log.info("Sharing token {} created for dictionary {} by user {}", sharingToken, request.dictionaryId(), userId);
        return new SharedDictionaryDto(request.dictionaryId(), sharingToken, request.shareToDate());
    }

    @Override
    @Transactional
    public SharedDictionaries getSharedDictionaries() {
        return getFilteredSharedDictionaries(tokenData -> true);
    }

    @Override
    @Transactional
    public SharedDictionaries getSharedTokensByDictionary(Long dictionaryId) {
        return getFilteredSharedDictionaries(
                tokenData -> tokenData.sharingTokenData().getDictionaryId().equals(dictionaryId));
    }

    @Override
    public void revokeSharingToken(String sharingToken) {
        revokeTokens(() -> sharingTokenIssuer.invalidateTokenByUserId(IdentityUtil.getUserId(), sharingToken));
    }

    @Override
    public void revokeAllSharingTokens() {
        revokeTokens(() -> sharingTokenIssuer.invalidateAllTokensByUserId(IdentityUtil.getUserId()));
    }

    private SharedDictionaries getFilteredSharedDictionaries(Predicate<ValidTokenData> filter) {
        UUID userId = IdentityUtil.getUserId();
        List<String> validSharingTokens = sharingTokenIssuer.getValidSharingTokens(userId);
        if (validSharingTokens.isEmpty()) {
            throw new ResourceNotFoundException("No shared dictionaries found");
        }
        List<SharedDictionaryDto> sharedDictionaries = validSharingTokens.stream()
                .map(this::wrapValidToken)
                .flatMap(Optional::stream)
                .filter(filter)
                .map(this::toSharedDictionaryDto)
                .toList();
        return new SharedDictionaries(sharedDictionaries);
    }

    private Optional<ValidTokenData> wrapValidToken(String token) {
        try {
            SharingTokenData tokenData = tokenDeserializationHandler.deserializeToken(token, SharingTokenData.class);
            return Optional.of(new ValidTokenData(token, tokenData));
        } catch (Exception e) {
            log.info("Error deserializing token [{}]", token, e);
            return Optional.empty();
        }
    }

    private SharedDictionaryDto toSharedDictionaryDto(ValidTokenData validTokenData) {
        SharingTokenData tokenData = validTokenData.sharingTokenData();
        return new SharedDictionaryDto(tokenData.getDictionaryId(), validTokenData.token(), tokenData.getExpiresAt());
    }

    private void revokeTokens(Supplier<Boolean> revokeAction) {
        boolean invalidated = revokeAction.get();
        if (!invalidated) {
            throw new ResourceNotFoundException("Token not found or already revoked");
        }
    }

    private record ValidTokenData(String token, SharingTokenData sharingTokenData) {}
}
