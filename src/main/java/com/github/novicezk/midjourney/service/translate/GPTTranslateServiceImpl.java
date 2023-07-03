package com.github.novicezk.midjourney.service.translate;


import cn.hutool.core.text.CharSequenceUtil;
import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.service.TranslateService;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;

import java.util.List;

@Slf4j
public class GPTTranslateServiceImpl implements TranslateService {
    private final OpenAiService openAiService;
    private final ProxyProperties.OpenaiConfig openaiConfig;

    public GPTTranslateServiceImpl(ProxyProperties.OpenaiConfig openaiConfig) {
        if (CharSequenceUtil.isBlank(openaiConfig.getGptApiKey())) {
            throw new BeanDefinitionValidationException("mj-proxy.openai.gpt-api-key未配置");
        }
        if (CharSequenceUtil.isBlank(openaiConfig.getBaseUrl()) || !openaiConfig.getBaseUrl().startsWith("http")) {
            throw new BeanDefinitionValidationException("mj-proxy.openai.base_url配置错误");
        }
        this.openaiConfig = openaiConfig;
        this.openAiService = new OpenAiService(openaiConfig.getGptApiKey(), openaiConfig.getBaseUrl(), openaiConfig.getTimeout());
    }

    @Override
    public String translateToEnglish(String prompt) {
        if (!containsChinese(prompt)) {
            return prompt;
        }
        ChatMessage m1 = new ChatMessage("system", "从现在开始，你是一名中英翻译，你会根据我输入的中文内容，翻译成对应英文。请注意，你翻译后的内容主要服务于一个绘画AI，它只能理解具象的描述而非抽象的概念，同时根据你对绘画AI的理解，比如它可能的训练模型、自然语言处理方式等方面，进行翻译优化。由于我的描述可能会很散乱，不连贯，你需要综合考虑这些问题，然后对翻译后的英文内容再次优化或重组，从而使绘画AI更能清楚我在说什么。请严格按照此条规则进行翻译，也只输出翻译后的英文内容。 例如，我输入：一只想家的小狗。你不能输出：A homesick little dog.你必须输出：A small dog that misses home, with a sad look on its face and its tail tucked between its legs. It might be standing in front of a closed door or a gate, gazing longingly into the distance, as if hoping to catch a glimpse of its beloved home.");
        ChatMessage m2 = new ChatMessage("user", prompt);
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(this.openaiConfig.getModel())
                .temperature(this.openaiConfig.getTemperature())
                .maxTokens(this.openaiConfig.getMaxTokens())
                .messages(List.of(m1, m2))
                .build();
        try {
            List<ChatCompletionChoice> choices = this.openAiService.createChatCompletion(request).getChoices();
            if (!choices.isEmpty()) {
                return choices.get(0).getMessage().getContent();
            }
        } catch (Exception e) {
            log.warn("调用chat-gpt接口翻译中文失败: {}", e.getMessage());
        }
        return prompt;
    }
}