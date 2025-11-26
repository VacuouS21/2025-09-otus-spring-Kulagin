package ru.otus.hw.service.converters;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;

import java.util.List;

@AllArgsConstructor
@Component
public class QuestionStringConverterImpl implements QuestionStringConverter {

    @Override
    public String convertQuestionToString(Question question) {
        return question.text() + System.lineSeparator()
                + convertAnswersToString(question.answers());
    }

    private String convertAnswersToString(List<Answer> answers) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < answers.size(); i++) {
            int answerNum = i + 1;
            sb.append(answerNum)
                    .append(": ")
                    .append(answers.get(i).text())
                    .append("\n");
        }
        return sb.toString();
    }
}
