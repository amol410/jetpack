package com.dolphin.jetpack.presentation.util

import com.dolphin.jetpack.domain.model.Question
import com.dolphin.jetpack.domain.model.QuestionAnswer
import com.dolphin.jetpack.domain.model.Quiz

/**
 * Helper to resolve answer text from stored indices.
 */
object QuestionOptionResolver {
    /**
     * Build a map of questionIndex -> (correctText, selectedText) from quiz questions and stored answers.
     */
    fun resolveOptionTexts(quiz: Quiz, answers: List<QuestionAnswer>): Map<Int, Pair<String?, String?>> {
        val questionMap = quiz.questions.withIndex().associate { it.index to it.value }
        val resolved = mutableMapOf<Int, Pair<String?, String?>>()
        answers.forEach { answer ->
            val question: Question? = questionMap[answer.questionIndex]
            val correctText = question?.options?.getOrNull(answer.correctAnswer)
            val selectedText = question?.options?.getOrNull(answer.selectedAnswer)
            resolved[answer.questionIndex] = correctText to selectedText
        }
        return resolved
    }
}
