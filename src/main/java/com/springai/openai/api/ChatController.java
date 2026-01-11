package com.springai.openai.api;

import com.springai.openai.domain.openai.service.OpenAiService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@Controller
public class ChatController {

    private final OpenAiService openAiService;

    @ResponseBody
    @PostMapping("/chat")
    public String chat(@RequestBody Map<String, String> body) {
        return openAiService.generate(body.get("text"));
    }

    @ResponseBody
    @PostMapping("/chat/stream")
    public Flux<String> streamChat (@RequestBody Map<String, String> body) {
        return openAiService.generateStream(body.get("text"));
    }

    @GetMapping("/")
    public String chatPage() {
        return "chat";
    }
}
