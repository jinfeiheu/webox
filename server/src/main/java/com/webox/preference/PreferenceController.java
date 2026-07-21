package com.webox.preference;

import com.webox.auth.AuthContext;
import com.webox.preference.dto.PreferenceView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/preferences")
public class PreferenceController {

    private final PreferenceService preferenceService;

    public PreferenceController(PreferenceService preferenceService) {
        this.preferenceService = preferenceService;
    }

    @GetMapping
    public PreferenceView get() {
        return preferenceService.get(AuthContext.require().id());
    }

    @PutMapping
    public PreferenceView put(@RequestBody PreferenceView request) {
        return preferenceService.put(AuthContext.require().id(), request);
    }
}
