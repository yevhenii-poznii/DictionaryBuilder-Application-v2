package com.kiskee.dictionarybuilder.web.controller.user;

import com.kiskee.dictionarybuilder.model.dto.user.profile.UpdateUserProfileDto;
import com.kiskee.dictionarybuilder.model.dto.user.profile.UserFullProfileDto;
import com.kiskee.dictionarybuilder.model.dto.user.profile.UserMiniProfileDto;
import com.kiskee.dictionarybuilder.model.dto.user.profile.UserProfileDto;
import com.kiskee.dictionarybuilder.service.user.profile.UserProfileService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user/profile")
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping("/me/mini")
    public UserMiniProfileDto getMiniProfile() {
        return userProfileService.getMiniProfile();
    }

    @GetMapping("/me")
    public UserFullProfileDto getFullProfile() {
        return userProfileService.getFullProfile();
    }

    @PutMapping("/me")
    public UserProfileDto updateProfile(@RequestBody @Valid UpdateUserProfileDto updateUserProfileDto) {
        return userProfileService.updateProfile(updateUserProfileDto);
    }

    @GetMapping("/{userId}")
    public UserMiniProfileDto getFullProfile(@PathVariable UUID userId) {
        // TODO implement
        return null;
    }
}
