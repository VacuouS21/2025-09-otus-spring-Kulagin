package ru.otus.hw.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class TestTestServiceImpl {
    @Mock
    private IOService ioService;

    @Mock
    private QuestionDao questionDao;

    private TestServiceImpl testService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testService = new TestServiceImpl(ioService, questionDao);
    }

    @Test
    void executeTest_shouldPrintQuestionsAndAnswers() {
        List<Answer> answers = List.of(
                new Answer("Answer First", true),
                new Answer("Answer Second", false)
        );

        List<Question> questions = List.of(
                new Question("Question", answers)
        );

        when(questionDao.findAll()).thenReturn(questions);

        testService.executeTest();

        verify(ioService).printLine("");
        verify(ioService).printFormattedLine("Please answer the questions below%n");
        verify(ioService).printFormattedLine("Question");
        verify(ioService).printLine("1: Answer First");
        verify(ioService).printLine("2: Answer Second");
        verify(ioService).printFormattedLine("");
    }
}
