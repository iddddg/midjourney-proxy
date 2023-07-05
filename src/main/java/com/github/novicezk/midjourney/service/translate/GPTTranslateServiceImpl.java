package com.github.novicezk.midjourney.service.translate;


import cn.hutool.core.text.CharSequenceUtil;
import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.service.TranslateService;
import com.unfbx.chatgpt.OpenAiClient;
import com.unfbx.chatgpt.entity.chat.ChatChoice;
import com.unfbx.chatgpt.entity.chat.ChatCompletion;
import com.unfbx.chatgpt.entity.chat.ChatCompletionResponse;
import com.unfbx.chatgpt.entity.chat.Message;
import com.unfbx.chatgpt.function.KeyRandomStrategy;
import com.unfbx.chatgpt.interceptor.OpenAILogger;
import com.unfbx.chatgpt.interceptor.OpenAiResponseInterceptor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class GPTTranslateServiceImpl implements TranslateService {
	private final OpenAiClient openAiClient;
	private final ProxyProperties.OpenaiConfig openaiConfig;

	public GPTTranslateServiceImpl(ProxyProperties properties) {
		this.openaiConfig = properties.getOpenai();
		if (CharSequenceUtil.isBlank(this.openaiConfig.getGptApiKey())) {
			throw new BeanDefinitionValidationException("mj-proxy.openai.gpt-api-key未配置");
		}
		HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new OpenAILogger());
		httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
		OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder()
				.addInterceptor(httpLoggingInterceptor)
				.addInterceptor(new OpenAiResponseInterceptor())
				.connectTimeout(10, TimeUnit.SECONDS)
				.writeTimeout(30, TimeUnit.SECONDS)
				.readTimeout(30, TimeUnit.SECONDS);
		if (CharSequenceUtil.isNotBlank(properties.getProxy().getHost())) {
			Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(properties.getProxy().getHost(), properties.getProxy().getPort()));
			okHttpBuilder.proxy(proxy);
		}
		OpenAiClient.Builder apiBuilder = OpenAiClient.builder()
				.apiKey(Collections.singletonList(this.openaiConfig.getGptApiKey()))
				.keyStrategy(new KeyRandomStrategy())
				.okHttpClient(okHttpBuilder.build());
		if (CharSequenceUtil.isNotBlank(this.openaiConfig.getGptApiUrl())) {
			apiBuilder.apiHost(this.openaiConfig.getGptApiUrl());
		}
		this.openAiClient = apiBuilder.build();
	}

	@Override
	public String translateToEnglish(String prompt) {
		if (!containsChinese(prompt)) {
			return prompt;
		}
		Message m1 = Message.builder().role(Message.Role.SYSTEM).content("从现在开始，你是一名中英翻译，你会根据我输入的中文内容，翻译成对应英文。请注意，你翻译后的内容主要服务于一个绘画AI，它只能理解具象的描述而非抽象的概念，同时根据你对绘画AI的理解，比如它可能的训练模型、自然语言处理方式等方面，进行翻译优化。由于我的描述可能会很散乱，不连贯，你需要综合考虑这些问题，然后对翻译后的英文内容再次优化或重组，从而使绘画AI更能清楚我在说什么。请严格按照此条规则进行翻译，也只输出翻译后的英文内容。 例如，我输入：一只想家的小狗。你不能输出：A homesick little dog.你必须输出：A small dog that misses home, with a sad look on its face and its tail tucked between its legs. It might be standing in front of a closed door or a gate, gazing longingly into the distance, as if hoping to catch a glimpse of its beloved home.").build();
		Message m2 = Message.builder().role(Message.Role.USER).content(prompt).build();
		ChatCompletion chatCompletion = ChatCompletion.builder()
				.messages(Arrays.asList(m1, m2))
				.model(this.openaiConfig.getModel())
				.temperature(this.openaiConfig.getTemperature())
				.maxTokens(this.openaiConfig.getMaxTokens())
				.build();
		ChatCompletionResponse chatCompletionResponse = this.openAiClient.chatCompletion(chatCompletion);
		try {
			List<ChatChoice> choices = chatCompletionResponse.getChoices();
			if (!choices.isEmpty()) {
				return choices.get(0).getMessage().getContent();
			}
		} catch (Exception e) {
			log.warn("调用chat-gpt接口翻译中文失败: {}", e.getMessage());
		}
		return prompt;
	}
}