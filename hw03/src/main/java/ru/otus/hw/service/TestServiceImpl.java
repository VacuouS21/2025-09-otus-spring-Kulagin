package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;
import ru.otus.hw.service.converters.QuestionStringConverter;

@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final LocalizedIOService ioService;

    private final QuestionDao questionDao;

    private final QuestionStringConverter questionStringConverter;

    @Override
    public TestResult executeTestFor(Student student) {
        ioService.printLine("");
        ioService.printFormattedLineLocalized("TestService.answer.the.questions", "%n");
        var questions = questionDao.findAll();
        var testResult = new TestResult(student);

        for (var question : questions) {
            String questionString = questionStringConverter.convertQuestionToString(question);
            int numChooseAnswer = ioService.readIntForRangeWithPrompt(1,
                    question.answers().size(),
                    questionString,
                    ioService.getMessage("TestService.invalid.answer.format"));
            var isAnswerValid = question.answers()
                    .get(numChooseAnswer - 1)
                    .isCorrect();
            testResult.applyAnswer(question, isAnswerValid);
        }
        return testResult;
    }

}
