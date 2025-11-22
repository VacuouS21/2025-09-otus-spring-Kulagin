package ru.otus.hw.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.exceptions.QuestionReadException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

class TestCSVQuestionDao {

    @Mock
    private TestFileNameProvider fileNameProvider;

    private CsvQuestionDao csvQuestionDao;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        csvQuestionDao = new CsvQuestionDao(fileNameProvider);
    }

    @Test
    void testShouldThrowQuestionReadExceptionWhenFileNotFound() {
        String nonExistentFile = "non-existent.csv";
        when(fileNameProvider.getTestFileName()).thenReturn(nonExistentFile);

        assertThatThrownBy(() -> csvQuestionDao.findAll())
                .isInstanceOf(QuestionReadException.class)
                .hasMessageContaining("Failed to read file: " + nonExistentFile)
                .hasCauseInstanceOf(Exception.class);
    }

    @Test
    void testShouldCreateCsvQuestionDaoInstance() {
        String nonExistentFile = "questions.csv";
        when(fileNameProvider.getTestFileName()).thenReturn(nonExistentFile);
        assertThat(csvQuestionDao).isNotNull();
        assertThat(csvQuestionDao).isInstanceOf(CsvQuestionDao.class);
    }

}