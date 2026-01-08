package ru.otus.hw.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе JDBC для работы с жанрами")
@JdbcTest
@Import(JdbcGenreRepository.class)
class JdbcGenreRepositoryTest {

    @Autowired
    private JdbcGenreRepository genreRepository;

    private List<Genre> dbGenres;

    @BeforeEach
    void setUp() {
        dbGenres = getDbGenres();
    }

    @DisplayName("должен загружать список всех жанров")
    @Test
    void shouldReturnCorrectGenresList() {
        var actualGenres = genreRepository.findAll();
        var expectedGenres = dbGenres;

        assertThat(actualGenres).containsExactlyElementsOf(expectedGenres);
        actualGenres.forEach(System.out::println);
    }

    @DisplayName("должен загружать жанры по списку ids")
    @Test
    void shouldReturnCorrectGenresByIds() {
        var expectedGenres = List.of(dbGenres.get(0), dbGenres.get(2), dbGenres.get(4));
        var actualGenres = genreRepository.findAllByIds(Set.of(1L, 3L, 5L));

        assertThat(actualGenres)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(expectedGenres);
    }

    @DisplayName("должен загружать жанры связанные с книгой 1")
    @Test
    void shouldReturnGenresForBook1() {
        var expectedGenres = List.of(dbGenres.get(0), dbGenres.get(1));
        var actualGenres = genreRepository.findAllByIds(Set.of(1L, 2L));

        assertThat(actualGenres)
                .hasSize(2)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(expectedGenres);
    }

    @DisplayName("должен возвращать пустой список при поиске по несуществующим ids")
    @Test
    void shouldReturnEmptyListWhenIdsNotFound() {
        var actualGenres = genreRepository.findAllByIds(Set.of(999L, 1000L));
        assertThat(actualGenres).isEmpty();
    }

    @DisplayName("должен возвращать пустой список при передаче пустого Set")
    @Test
    void shouldReturnEmptyListWhenIdsSetIsEmpty() {
        var actualGenres = genreRepository.findAllByIds(Set.of());
        assertThat(actualGenres).isEmpty();
    }

    @DisplayName("должен загружать все жанры при передаче всех ids")
    @Test
    void shouldReturnAllGenresWhenAllIdsProvided() {
        var actualGenres = genreRepository.findAllByIds(Set.of(1L, 2L, 3L, 4L, 5L, 6L));
        var expectedGenres = dbGenres;

        assertThat(actualGenres)
                .hasSize(6)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(expectedGenres);
    }

    @DisplayName("должен загружать только существующие жанры из смешанного набора ids")
    @Test
    void shouldReturnOnlyExistingGenresFromMixedIds() {
        var expectedGenres = List.of(dbGenres.get(0), dbGenres.get(1));
        var actualGenres = genreRepository.findAllByIds(Set.of(1L, 2L, 999L, 1000L));

        assertThat(actualGenres)
                .hasSize(2)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(expectedGenres);
    }

    @DisplayName("должен возвращать жанры с корректными именами")
    @Test
    void shouldReturnGenresWithCorrectNames() {
        var actualGenres = genreRepository.findAll();

        assertThat(actualGenres)
                .extracting(Genre::getName)
                .containsExactly("Genre_1", "Genre_2", "Genre_3", "Genre_4", "Genre_5", "Genre_6");
    }

    @DisplayName("findAll не должен возвращать null")
    @Test
    void shouldNotReturnNullInFindAll() {
        var actualGenres = genreRepository.findAll();

        assertThat(actualGenres)
                .isNotNull()
                .isNotEmpty();
    }

    @DisplayName("findAllByIds не должен возвращать null")
    @Test
    void shouldNotReturnNullInFindAllByIds() {
        var actualGenres = genreRepository.findAllByIds(Set.of(1L));

        assertThat(actualGenres).isNotNull();
    }

    private static List<Genre> getDbGenres() {
        return IntStream.range(1, 7).boxed()
                .map(id -> new Genre(id, "Genre_" + id))
                .toList();
    }
}
