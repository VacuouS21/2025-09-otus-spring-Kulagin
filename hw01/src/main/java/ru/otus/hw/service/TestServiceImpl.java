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
        for (Question question : questions) {
            ioService.printFormattedLine(question.text());
            printAnswers(question.answers());
        }
    }

    private void printAnswers(List<Answer> answer) {
        for (int i = 0; i < answer.size(); i++) {
            int answerNum = i + 1;
            ioService.printLine(answerNum + ": " + answer.get(i).text());
        }
        ioService.printFormattedLine("");
    }
}
