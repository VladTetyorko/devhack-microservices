package com.vladte.devhack.ai.service.api;

/**
 * Class containing prompt templates for AI services.
 * This class centralizes all prompt templates used in AI services to make them easier to maintain and update.
 */
public class AiPromptConstraints {

    /**
     * Template for generating interview questions based on a tag.
     * Parameters: %d (count), %s (difficulty), %s (tag)
     */
    public static final String GENERATE_QUESTIONS_TEMPLATE =
            "You are an expert technical interviewer creating questions for candidates. " +
                    "Generate exactly %d technical interview questions about %s at %s difficulty level. " +
                    "For difficulty levels: " +
                    "- Easy: Questions should test basic understanding and fundamental concepts. " +
                    "- Medium: Questions should require deeper knowledge and some problem-solving. " +
                    "- Hard: Questions should challenge advanced concepts and require complex problem-solving. " +
                    "Each question must be clear, specific, and directly related to %s. " +
                    "Format requirements: " +
                    "1. Output ONLY the questions with no introductions, explanations, or conclusions. " +
                    "2. Each question must start on a new line with 'Question: ' prefix. " +
                    "3. Questions should be self-contained and not reference each other. " +
                    "4. Do not number the questions.";

    /**
     * Template for checking an answer to an interview question and providing a score.
     * Parameters: %s (questionText), %s (answerText)
     */
    public static final String CHECK_ANSWER_TEMPLATE =
            "You are an expert technical evaluator with deep knowledge in software development and computer science. " +
                    "Evaluate the following answer to the given technical interview question using these scoring criteria: " +
                    "\n" +
                    "Scoring guidelines: " +
                    "- 0-20: Completely incorrect or irrelevant answer " +
                    "- 21-40: Major conceptual errors or significant omissions " +
                    "- 41-60: Partially correct with some errors or omissions " +
                    "- 61-80: Mostly correct with minor errors or omissions " +
                    "- 81-100: Completely correct and comprehensive answer " +
                    "\n" +
                    "Question: %s\n\n" +
                    "Answer: %s\n\n" +
                    "Based on the above criteria, assign a precise score from 0 to 100. " +
                    "OUTPUT INSTRUCTIONS: Return ONLY the numeric score as a single number without any text, explanation, or additional characters.";

    /**
     * Template for checking an answer to an interview question and providing a score and feedback.
     * Parameters: %s (questionText), %s (answerText)
     */
    public static final String CHECK_ANSWER_WITH_FEEDBACK_TEMPLATE =
            "You are an expert technical evaluator with deep knowledge in software development and computer science. " +
                    "Evaluate the following answer to the given technical interview question using these scoring criteria: " +
                    "\n" +
                    "Scoring guidelines: " +
                    "- 0-20: Completely incorrect or irrelevant answer " +
                    "- 21-40: Major conceptual errors or significant omissions " +
                    "- 41-60: Partially correct with some errors or omissions " +
                    "- 61-80: Mostly correct with minor errors or omissions " +
                    "- 81-100: Completely correct and comprehensive answer " +
                    "\n" +
                    "Question: %s\n\n" +
                    "Answer: %s\n\n" +
                    "Provide a comprehensive evaluation with the following structure: " +
                    "1. A precise score from 0-100 based on the guidelines above " +
                    "2. Key strengths of the answer (2-3 points) " +
                    "3. Areas for improvement (2-3 points) " +
                    "4. Specific suggestions to make the answer more complete and accurate " +
                    "5. Any technical corrections needed " +
                    "\n" +
                    "OUTPUT FORMAT (strictly follow this format): " +
                    "Score: [numeric score only] " +
                    "Feedback: " +
                    "- Strengths: [list key strengths as bullet points] " +
                    "- Areas for improvement: [list areas for improvement as bullet points] " +
                    "- Suggestions: [provide specific, actionable suggestions] " +
                    "- Technical corrections: [provide any necessary technical corrections]";

    // Private constructor to prevent instantiation
    private AiPromptConstraints() {
        // This class should not be instantiated
    }
}