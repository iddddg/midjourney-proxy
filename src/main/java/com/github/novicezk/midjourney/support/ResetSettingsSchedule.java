package com.github.novicezk.midjourney.support;

import com.github.novicezk.midjourney.service.DiscordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResetSettingsSchedule {
    private final DiscordService discordService;

    @Scheduled(fixedRate = 10 * 60 * 1000L)
    public void checkSettings() {
        discordService.settings();
    }
}
