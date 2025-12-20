package ru.otus.hw.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.exceptions.QuestionReadException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {CsvQuestionDao.class})
class CsvQuestionDaoTest {

    @MockitoBean
    private TestFileNameProvider fileNameProvider;

    @Autowired
    private CsvQuestionDao csvQuestionDao;

    @BeforeEach
    void setUp() {
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