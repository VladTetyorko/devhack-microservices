package com.vladte.devhack.common.service.websocket;

import com.vladte.devhack.domain.entities.global.InterviewQuestion;
import com.vladte.devhack.domain.model.mapper.global.InterviewQuestionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service for broadcasting interview question updates via WebSocket.
 */
@Service
@Slf4j
public class QuestionWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    private final InterviewQuestionMapper questionMapper;

    public QuestionWebSocketService(SimpMessagingTemplate messagingTemplate,
                                    InterviewQuestionMapper questionMapper) {
        this.messagingTemplate = messagingTemplate;
        this.questionMapper = questionMapper;
    }

    /**
     * Broadcast question creation event.
     *
     * @param question the created question
     */
    public void broadcastQuestionCreated(InterviewQuestion question) {
        log.debug("Broadcasting question created event for question ID: {}", question.getId());

        Map<String, Object> message = new HashMap<>();
        message.put("type", "CREATED");
        message.put("question", questionMapper.toDTO(question));
        message.put("timestamp", System.currentTimeMillis());

        messagingTemplate.convertAndSend("/topic/questions", message);
        log.debug("Question created event broadcasted successfully");
    }

    /**
     * Broadcast question update event.
     *
     * @param question the updated question
     */
    public void broadcastQuestionUpdated(InterviewQuestion question) {
        log.debug("Broadcasting question updated event for question ID: {}", question.getId());

        Map<String, Object> message = new HashMap<>();
        message.put("type", "UPDATED");
        message.put("question", questionMapper.toDTO(question));
        message.put("timestamp", System.currentTimeMillis());

        messagingTemplate.convertAndSend("/topic/questions", message);
        log.debug("Question updated event broadcasted successfully");
    }

    /**
     * Broadcast question deletion event.
     *
     * @param questionId the ID of the deleted question
     */
    public void broadcastQuestionDeleted(UUID questionId) {
        log.debug("Broadcasting question deleted event for question ID: {}", questionId);

        Map<String, Object> message = new HashMap<>();
        message.put("type", "DELETED");
        message.put("questionId", questionId);
        message.put("timestamp", System.currentTimeMillis());

        messagingTemplate.convertAndSend("/topic/questions", message);
        log.debug("Question deleted event broadcasted successfully");
    }

    /**
     * Broadcast bulk questions update event (e.g., after AI generation).
     *
     * @param count   the number of questions that were added/updated
     * @param tagName the tag name for which questions were generated (optional)
     */
    public void broadcastBulkQuestionsUpdate(int count, String tagName) {
        log.debug("Broadcasting bulk questions update event: {} questions, tag: {}", count, tagName);

        Map<String, Object> message = new HashMap<>();
        message.put("type", "BULK_UPDATE");
        message.put("count", count);
        message.put("tagName", tagName);
        message.put("timestamp", System.currentTimeMillis());

        messagingTemplate.convertAndSend("/topic/questions", message);
        log.debug("Bulk questions update event broadcasted successfully");
    }
}