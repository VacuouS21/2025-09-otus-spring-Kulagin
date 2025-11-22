package ru.otus.hw.service.converters;

import ru.otus.hw.domain.Question;

public interface QuestionStringConverter {
    String convertQuestionToString(Question question);
}
