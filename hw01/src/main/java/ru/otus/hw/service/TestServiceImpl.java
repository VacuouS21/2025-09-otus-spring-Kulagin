package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;

import java.util.List;

@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final IOService ioService;

    private final QuestionDao questionDao;

    @Override
    public void executeTest() {
        ioService.printLine("");
        ioService.printFormattedLine("Please answer the questions below%n");
        List<Question> questions = questionDao.findAll();
        printAllQuestions(questions);
    }

    private void printAllQuestions(List<Question> questions) {
        for (Question question : questions) {
            ioService.printFormattedLine(convertQuestionToString(question));
        }
    }

    private String convertQuestionToString(Question question) {
        return question.text() + "\n"
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
