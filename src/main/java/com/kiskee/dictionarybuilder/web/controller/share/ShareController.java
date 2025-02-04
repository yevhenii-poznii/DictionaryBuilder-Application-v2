package com.kiskee.dictionarybuilder.web.controller.share;

import com.kiskee.dictionarybuilder.model.dto.share.ShareDictionaryRequest;
import com.kiskee.dictionarybuilder.model.dto.share.SharedDictionaries;
import com.kiskee.dictionarybuilder.model.dto.share.SharedDictionaryDto;
import com.kiskee.dictionarybuilder.model.dto.share.SharedDictionaryPage;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.page.DictionaryPageRequestDto;
import com.kiskee.dictionarybuilder.service.share.ShareService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/share")
public class ShareController {

    private final ShareService shareService;

    @GetMapping("/{sharingToken}")
    public SharedDictionaryPage getSharedDictionaryPage(
            @PathVariable String sharingToken, @ModelAttribute @Valid DictionaryPageRequestDto dictionaryPageRequest) {
        return shareService.getSharedDictionaryPage(sharingToken, dictionaryPageRequest);
    }

    @PostMapping
    public SharedDictionaryDto shareDictionary(@RequestBody @Valid ShareDictionaryRequest request) {
        return shareService.shareDictionary(request);
    }

    @GetMapping("/dictionaries")
    public SharedDictionaries getSharedDictionaries() {
        return shareService.getSharedDictionaries();
    }

    @GetMapping("/dictionaries/{dictionaryId}")
    public SharedDictionaries getSharedTokensByDictionary(@PathVariable Long dictionaryId) {
        return shareService.getSharedTokensByDictionary(dictionaryId);
    }

    @PatchMapping("/{sharingToken}/revoke")
    public void revokeSharingToken(@PathVariable String sharingToken) {
        shareService.revokeSharingToken(sharingToken);
    }

    @PatchMapping("/revoke")
    public void revokeAllSharingTokens() {
        shareService.revokeAllSharingTokens();
    }

    @GetMapping("/{sharingToken}/repetition")
    public void getRepetition(@PathVariable String sharingToken) {
        System.out.println("Protected method");
    }
}
