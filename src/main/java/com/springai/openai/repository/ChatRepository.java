package com.springai.openai.repository;

import com.springai.openai.entity.ChatEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepository extends JpaRepository<ChatEntity, Long> {

    List<ChatEntity> findByUserIdOrderByCreatedAtAsc(String userId);
}
