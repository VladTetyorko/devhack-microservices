package com.vladte.devhack.common.service.view.impl;

import com.vladte.devhack.common.model.dto.AnswerDTO;
import com.vladte.devhack.common.model.mapper.AnswerMapper;
import com.vladte.devhack.common.service.domain.global.InterviewQuestionService;
import com.vladte.devhack.common.service.domain.personalized.AnswerService;
import com.vladte.devhack.common.service.domain.user.UserService;
import com.vladte.devhack.common.service.view.AnswerFormService;
import com.vladte.devhack.common.service.view.ModelBuilder;
import com.vladte.devhack.entities.global.InterviewQuestion;
import com.vladte.devhack.entities.personalized.Answer;
import com.vladte.devhack.entities.user.User;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of the AnswerFormService interface.
 * This class handles form-related operations for answers.
 */
@Service
public class AnswerFormServiceImpl implements AnswerFormService {

    private final AnswerService answerService;
    private final UserService userService;
    private final InterviewQuestionService questionService;
    private final AnswerMapper answerMapper;


    public AnswerFormServiceImpl(AnswerService answerService,
                                 UserService userService,
                                 InterviewQuestionService questionService,
                                 AnswerMapper answerMapper) {
        this.answerService = answerService;
        this.userService = userService;
        this.questionService = questionService;
        this.answerMapper = answerMapper;
    }

    @Override
    public void prepareNewAnswerForm(UUID questionId, Model model) {
        AnswerDTO answerDTO = new AnswerDTO();

        // Create a ModelBuilder
        ModelBuilder builder = ModelBuilder.of(model);

        // Add question if provided
        if (questionId != null) {
            Optional<InterviewQuestion> questionOpt = questionService.findById(questionId);
            questionOpt.ifPresent(question -> {
                answerDTO.setQuestionId(question.getId());
                answerDTO.setQuestionText(question.getQuestionText());
                builder.addAttribute("question", question);
            });
        }

        // Complete the model building
        builder.addAttribute("answer", answerDTO)
                .addAttribute("users", userService.findAll())
                .addAttribute("questions", questionService.findAll())
                .build();
    }

    @Override
    public AnswerDTO prepareEditAnswerForm(UUID id, Model model) {
        Optional<Answer> answerOpt = answerService.findById(id);
        if (answerOpt.isPresent()) {
            Answer answer = answerOpt.get();
            AnswerDTO answerDTO = answerMapper.toDTO(answer);

            ModelBuilder.of(model)
                    .addAttribute("answer", answerDTO)
                    .addAttribute("users", userService.findAll())
                    .addAttribute("questions", questionService.findAll())
                    .build();

            return answerDTO;
        }
        return null;
    }

    @Override
    public AnswerDTO saveAnswer(AnswerDTO answerDTO, UUID userId, UUID questionId) {
        Optional<User> userOpt = userService.findById(userId);
        Optional<InterviewQuestion> questionOpt = questionService.findById(questionId);

        if (userOpt.isPresent() && questionOpt.isPresent()) {
            User user = userOpt.get();
            InterviewQuestion question = questionOpt.get();

            Answer answer = answerMapper.toEntity(answerDTO);
            answer.setUser(user);
            answer.setQuestion(question);

            Answer savedAnswer = answerService.save(answer);
            return answerMapper.toDTO(savedAnswer);
        }
        return null;
    }

    @Override
    public void deleteAnswer(UUID id) {
        answerService.deleteById(id);
    }

    @Override
    public void checkAnswerWithAiAsync(UUID id) {
        answerService.checkAnswerWithAiAsync(id);
    }

    @Override
    public void setNewAnswerPageTitle(Model model) {
        ModelBuilder.of(model)
                .setPageTitle("Create New Answer")
                .build();
    }

    @Override
    public void setEditAnswerPageTitle(Model model) {
        ModelBuilder.of(model)
                .setPageTitle("Edit Answer")
                .build();
    }
}
