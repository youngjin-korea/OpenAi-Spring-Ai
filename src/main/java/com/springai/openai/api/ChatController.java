package com.springai.openai.api;

import com.springai.openai.domain.openai.service.ChatService;
import com.springai.openai.domain.openai.service.OpenAiService;
import com.springai.openai.entity.ChatEntity;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@Controller
public class ChatController {

    private final OpenAiService openAiService;
    private final ChatService chatService;

    @ResponseBody
    @PostMapping("/chat/history/{userid}")
    public List<ChatEntity> getChatHistory (@PathVariable("userid") String userId) {
        return chatService.readAllChat(userId);
    }

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
