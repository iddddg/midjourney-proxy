package com.github.novicezk.midjourney.controller;

import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.result.Message;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Api(tags = "配置维护")
@RestController
@RequestMapping("/config")
@AllArgsConstructor
public class ConfigController {
    private ProxyProperties properties;

    @ApiOperation(value = "查询所有配置")
    @GetMapping("/list")
    public Message<ProxyProperties> list() {
        return Message.success(this.properties);
    }

    @ApiOperation(value = "更新配置")
    @PostMapping("/update")
    public Message<ProxyProperties> update(@RequestBody ProxyProperties properties) {
        this.properties = properties;
        return Message.success(properties);
    }
}
