package com.github.novicezk.midjourney;

import com.github.novicezk.midjourney.service.TranslateService;
import com.github.novicezk.midjourney.service.translate.GPTTranslateServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * MyTest
 *
 * @Author iddddg
 * @Date 17:23 2023/6/27
 * @Version 1.0
 **/
@SpringBootTest
public class MyTest {
    @Autowired
    private TranslateService translateService;

    @Test
    public void test() {
        String prompt = "一群猫猫狗狗";
        String promptEn = translateService.translateToEnglish(prompt);
        System.out.println(prompt + " => " + promptEn);
    }
}
