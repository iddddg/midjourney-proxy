package com.github.novicezk.midjourney.wss.handle;

import com.github.novicezk.midjourney.enums.MessageType;
import com.github.novicezk.midjourney.service.DiscordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * describe消息处理.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SettingsMessageHandler extends MessageHandler {
    private static final String CONTENT_REGEX = "^Adjust your settings here\\. Current suffix: `(.*)`$";
    private final DiscordService discordService;
    private final List<String> LABEL_LIST = List.of("MJ version 5.2", "Stylize med", "Public mode");

    @Override
    public void handle(MessageType messageType, DataObject message) {
        String content = getMessageContent(message);
        Boolean settingsMessage = parse(content);
        if (!settingsMessage) {
            return;
        }
        String messageId = message.hasKey("id") ? message.getString("id") : "1125973175096836126";
        // 关闭Remix
        Boolean checkRemixMode = this.checkRemixMode(message);
        if (checkRemixMode == null) {
            log.error("获取当前Remix mode异常");
        } else if (!checkRemixMode) {
            this.discordService.preferRemix();
        }
        // 重置
        Boolean checkSettings = this.checkSettings(message);
        if (checkSettings == null) {
            log.error("获取当前设置值异常");
        } else if (!checkSettings) {
            this.discordService.resetSettings(messageId);
        }
    }

    @Override
    public void handle(MessageType messageType, Message message) {

    }

    private Boolean parse(String content) {
        Matcher matcher = Pattern.compile(CONTENT_REGEX).matcher(content);
        return matcher.matches();
    }

    private Boolean checkSettings(DataObject message) {
        DataObject mjVersion52;
        DataObject stylizeMed;
        DataObject publicMode;
        try {
            mjVersion52 = message.getArray("components").getObject(1).getArray("components").getObject(1);
            stylizeMed = message.getArray("components").getObject(2).getArray("components").getObject(1);
            publicMode = message.getArray("components").getObject(3).getArray("components").getObject(0);
        } catch (Exception e) {
            return null;
        }
        List<DataObject> list = List.of(mjVersion52, stylizeMed, publicMode);
        Boolean result = true;
        for (DataObject dataObject : list) {
            String label = dataObject.hasKey("label") ? dataObject.getString("label") : "";
            Boolean status = dataObject.hasKey("style") && dataObject.getInt("style") == 3;
            if (!LABEL_LIST.contains(label)) {
                result = null;
                break;
            }
            result = result && status;
        }
        return result;
    }

    private Boolean checkRemixMode(DataObject message) {
        DataObject remixMode;
        try {
            remixMode = message.getArray("components").getObject(3).getArray("components").getObject(1);
        } catch (Exception e) {
            return null;
        }
        String label = remixMode.hasKey("label") ? remixMode.getString("label") : "";
        Boolean status = remixMode.hasKey("style") && remixMode.getInt("style") == 2;
        if (!"Remix mode".equals(label)) {
            return null;
        }
        return status;
    }
}
