package com.clipiq.websocket;

import com.clipiq.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class AnalysisMessageController {

    private final AnalysisService analysisService;

    @MessageMapping("/analyse")
    public void analyse(String uuid) {
        analysisService.process(uuid);
    }
}
