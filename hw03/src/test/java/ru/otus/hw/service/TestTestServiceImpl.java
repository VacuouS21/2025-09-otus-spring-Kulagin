package ru.otus.hw.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;
import ru.otus.hw.service.converters.QuestionStringConverter;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TestTestServiceImpl {

    @Mock
    private LocalizedIOService localizedIOService;

    @Mock
    private QuestionDao questionDao;

    @Mock
    private QuestionStringConverter questionStringConverter;

    private TestServiceImpl testService;

    private Student student;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testService = new TestServiceImpl(localizedIOService, questionDao, questionStringConverter);
        student = new Student("Konst", "Kul");
    }

    @Test
    void executeTestForShouldReturnTestResult() {
        List<Answer> answers = List.of(
                new Answer("Answer First", true),
                new Answer("Answer Second", false)
        );

        List<Question> questions = List.of(
                new Question("Question 1", answers),
                new Question("Question 2", answers)
        );

        when(questionDao.findAll()).thenReturn(questions);
        when(questionStringConverter.convertQuestionToString(questions.get(0)))
                .thenReturn("Formatted Question 1");
        when(questionStringConverter.convertQuestionToString(questions.get(1)))
                .thenReturn("Formatted Question 2");

        when(localizedIOService.readIntForRangeWithPrompt(1, 2, "Formatted Question 1", localizedIOService.getMessage("TestService.invalid.answer.format")))
                .thenReturn(1);
        when(localizedIOService.readIntForRangeWithPrompt(1, 2, "Formatted Question 2", localizedIOService.getMessage("TestService.invalid.answer.format")))
                .thenReturn(1);

        TestResult testResult = testService.executeTestFor(student);

        assertThat(testResult).isNotNull();
        assertThat(testResult.getStudent()).isEqualTo(student);
        assertThat(testResult.getAnsweredQuestions()).hasSize(2);
        assertThat(testResult.getRightAnswersCount()).isEqualTo(2);


        verify(questionStringConverter).convertQuestionToString(questions.get(0));
        verify(questionStringConverter).convertQuestionToString(questions.get(1));
    }

    @Test
    void executeTestForShouldApplyCorrectAnswers() {
        List<Answer> answers = List.of(
                new Answer("Correct Answer", true),
                new Answer("Wrong Answer", false)
        );

        Question question = new Question("Test Question", answers);
        List<Question> questions = List.of(question);

        when(questionDao.findAll()).thenReturn(questions);
        when(questionStringConverter.convertQuestionToString(question))
                .thenReturn("Formatted Test Question");

        when(localizedIOService.readIntForRangeWithPrompt(1, 2, "Formatted Test Question", localizedIOService.getMessage("TestService.invalid.answer.format")))
                .thenReturn(1);

        TestResult testResult = testService.executeTestFor(student);

        assertThat(testResult.getRightAnswersCount()).isEqualTo(1);
    }

    @Test
    void executeTestForShouldApplyWrongAnswers() {
        List<Answer> answers = List.of(
                new Answer("Correct Answer", true),
                new Answer("Wrong Answer", false)
        );

        Question question = new Question("Test Question", answers);
        List<Question> questions = List.of(question);

        when(questionDao.findAll()).thenReturn(questions);
        when(questionStringConverter.convertQuestionToString(question))
                .thenReturn("Formatted Test Question");

        when(localizedIOService.readIntForRangeWithPrompt(1, 2, "Formatted Test Question", localizedIOService.getMessage("TestService.invalid.answer.format")))
                .thenReturn(2);

        TestResult testResult = testService.executeTestFor(student);

        assertThat(testResult.getRightAnswersCount()).isZero();
    }

    @Test
    void executeTestForShouldHandleMultipleQuestions() {
        List<Answer> answers1 = List.of(
                new Answer("Q1 Correct", true),
                new Answer("Q1 Wrong", false)
        );

        List<Answer> answers2 = List.of(
                new Answer("Q2 Wrong", false),
                new Answer("Q2 Correct", true)
        );

        List<Question> questions = List.of(
                new Question("Question 1", answers1),
                new Question("Question 2", answers2)
        );

        when(questionDao.findAll()).thenReturn(questions);
        when(questionStringConverter.convertQuestionToString(questions.get(0)))
                .thenReturn("Formatted Question 1");
        when(questionStringConverter.convertQuestionToString(questions.get(1)))
                .thenReturn("Formatted Question 2");

        when(localizedIOService.readIntForRangeWithPrompt(1, 2, "Formatted Question 1", localizedIOService.getMessage("TestService.invalid.answer.format")))
                .thenReturn(1);
        when(localizedIOService.readIntForRangeWithPrompt(1, 2, "Formatted Question 2", localizedIOService.getMessage("TestService.invalid.answer.format")))
                .thenReturn(2);

        TestResult testResult = testService.executeTestFor(student);

        assertThat(testResult.getRightAnswersCount()).isEqualTo(2);
        assertThat(testResult.getAnsweredQuestions()).hasSize(2);
    }

    @Test
    void executeTestForShouldHandleEmptyQuestionsList() {
        when(questionDao.findAll()).thenReturn(List.of());

        TestResult testResult = testService.executeTestFor(student);

        assertThat(testResult).isNotNull();
        assertThat(testResult.getRightAnswersCount()).isZero();
        assertThat(testResult.getAnsweredQuestions()).isEmpty();

    }
}