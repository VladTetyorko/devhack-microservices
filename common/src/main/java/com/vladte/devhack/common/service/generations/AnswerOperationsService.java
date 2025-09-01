package com.vladte.devhack.common.service.generations;

import com.vladte.devhack.domain.entities.personalized.Answer;
import org.springframework.scheduling.annotation.Async;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface AnswerOperationsService {

    /**
     * Check an answer using AI and update its score.
     *
     * @param answerId the ID of the answer to check
     * @return the updated answer with the AI score
     */
    Answer checkAnswerWithAi(UUID answerId);

    /**
     * Check an answer using AI and update its score asynchronously.
     *
     * @param answerId the ID of the answer to check
     * @return a CompletableFuture containing the updated answer with the AI score
     */
    @Async
    CompletableFuture<Answer> checkAnswerWithAiAsync(UUID answerId);

}
