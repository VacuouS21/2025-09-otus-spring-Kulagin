package ru.otus.hw.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Author;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе JDBC для работы с авторами")
@JdbcTest
@Import(JdbcAuthorRepository.class)
class JdbcAuthorRepositoryTest {

    @Autowired
    private JdbcAuthorRepository authorRepository;

    private List<Author> dbAuthors;

    @BeforeEach
    void setUp() {
        dbAuthors = getDbAuthors();
    }

    @DisplayName("должен загружать автора по id")
    @ParameterizedTest
    @MethodSource("getDbAuthors")
    void shouldReturnCorrectAuthorById(Author expectedAuthor) {
        var actualAuthor = authorRepository.findById(expectedAuthor.getId());
        assertThat(actualAuthor).isPresent()
                .get()
                .isEqualTo(expectedAuthor);
    }

    @DisplayName("должен загружать список всех авторов")
    @Test
    void shouldReturnCorrectAuthorsList() {
        var actualAuthors = authorRepository.findAll();
        var expectedAuthors = dbAuthors;

        assertThat(actualAuthors).containsExactlyElementsOf(expectedAuthors);
        actualAuthors.forEach(System.out::println);
    }

    @DisplayName("должен возвращать пустой Optional при поиске автора по несуществующему id")
    @Test
    void shouldReturnEmptyOptionalWhenAuthorNotFound() {
        var actualAuthor = authorRepository.findById(999L);
        assertThat(actualAuthor).isEmpty();
    }

    @DisplayName("должен корректно загружать автора с id = 1")
    @Test
    void shouldReturnFirstAuthor() {
        var expectedAuthor = new Author(1, "Author_1");
        var actualAuthor = authorRepository.findById(1L);

        assertThat(actualAuthor)
                .isPresent()
                .get()
                .isEqualTo(expectedAuthor);
    }

    @DisplayName("должен корректно загружать автора с id = 3")
    @Test
    void shouldReturnLastAuthor() {
        var expectedAuthor = new Author(3, "Author_3");
        var actualAuthor = authorRepository.findById(3L);

        assertThat(actualAuthor)
                .isPresent()
                .get()
                .isEqualTo(expectedAuthor);
    }

    @DisplayName("должен возвращать всех авторов в правильном порядке")
    @Test
    void shouldReturnAuthorsInCorrectOrder() {
        var actualAuthors = authorRepository.findAll();

        assertThat(actualAuthors)
                .hasSize(3)
                .extracting(Author::getId)
                .containsExactly(1L, 2L, 3L);
    }

    @DisplayName("должен возвращать авторов с корректными именами")
    @Test
    void shouldReturnAuthorsWithCorrectNames() {
        var actualAuthors = authorRepository.findAll();

        assertThat(actualAuthors)
                .extracting(Author::getFullName)
                .containsExactly("Author_1", "Author_2", "Author_3");
    }

    @DisplayName("findAll не должен возвращать null")
    @Test
    void shouldNotReturnNullInFindAll() {
        var actualAuthors = authorRepository.findAll();

        assertThat(actualAuthors)
                .isNotNull()
                .isNotEmpty();
    }

    @DisplayName("findById не должен возвращать null Optional")
    @Test
    void shouldNotReturnNullOptionalInFindById() {
        var actualAuthor = authorRepository.findById(1L);

        assertThat(actualAuthor).isNotNull();
    }

    private static List<Author> getDbAuthors() {
        return IntStream.range(1, 4).boxed()
                .map(id -> new Author(id, "Author_" + id))
                .toList();
    }
}
