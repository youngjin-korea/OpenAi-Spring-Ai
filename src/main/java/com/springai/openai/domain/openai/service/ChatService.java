package com.springai.openai.domain.openai.service;

import com.springai.openai.domain.openai.entity.ChatEntity;
import com.springai.openai.domain.openai.repository.ChatRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ChatService {

    private final ChatRepository chatRepository;

    @Transactional(readOnly = true)
    public List<ChatEntity> readAllChat (String userId) {
        return chatRepository.findByUserIdOrderByCreatedAtAsc(userId);
    }

}
