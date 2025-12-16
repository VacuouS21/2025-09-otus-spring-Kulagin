package ru.otus.hw.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JdbcBookRepository implements BookRepository {

    private final NamedParameterJdbcOperations namedParameterJdbcOperations;
    private final GenreRepository genreRepository;

    @Override
    public Optional<Book> findById(long id) {
        String sql = "SELECT b.id, b.title, b.author_id, a.full_name as author_name, g.id as genre_id, g.name as genre_name FROM books b JOIN authors a ON b.author_id = a.id LEFT JOIN books_genres bg ON b.id = bg.book_id LEFT JOIN genres g ON bg.genre_id = g.id WHERE b.id = :id ";

        Map<String, Object> params = Collections.singletonMap("id", id);
        Book book = namedParameterJdbcOperations.query(sql, params, new BookResultSetExtractor());

        return Optional.ofNullable(book);
    }

    @Override
    public List<Book> findAll() {
        var genres = genreRepository.findAll();
        var books = getAllBooksWithoutGenres();
        var relations = getAllGenreRelations();
        mergeBooksInfo(books, genres, relations);
        return books;
    }

    @Override
    public Book save(Book book) {
        if (book.getId() == 0) {
            return insert(book);
        }
        return update(book);
    }

    @Override
    public void deleteById(long id) {
        Map<String, Object> params = Collections.singletonMap("id", id);
        namedParameterJdbcOperations.update("DELETE FROM books_genres WHERE book_id = :id", params);
        namedParameterJdbcOperations.update("DELETE FROM books WHERE id = :id", params);
    }

    private List<Book> getAllBooksWithoutGenres() {
        String sql = "SELECT b.id, b.title, b.author_id, a.full_name as author_name FROM books b JOIN authors a ON b.author_id = a.id";

        return namedParameterJdbcOperations.query(sql, new BookRowMapper());
    }

    private List<BookGenreRelation> getAllGenreRelations() {
        return namedParameterJdbcOperations.query(
                "SELECT book_id, genre_id FROM books_genres",
                (rs, rowNum) -> new BookGenreRelation(
                        rs.getLong("book_id"),
                        rs.getLong("genre_id")
                )
        );
    }

    private void mergeBooksInfo(List<Book> booksWithoutGenres, List<Genre> genres,
                                List<BookGenreRelation> relations) {
        Map<Long, Genre> genreMap = genres.stream()
                .collect(Collectors.toMap(Genre::getId, genre -> genre));

        Map<Long, List<Long>> bookGenreMap = relations.stream()
                .collect(Collectors.groupingBy(
                        BookGenreRelation::bookId,
                        Collectors.mapping(BookGenreRelation::genreId, Collectors.toList())
                ));

        booksWithoutGenres.forEach(book -> {
            List<Long> genreIds = bookGenreMap.getOrDefault(book.getId(), Collections.emptyList());
            List<Genre> bookGenres = genreIds.stream()
                    .map(genreMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            book.setGenres(bookGenres);
        });
    }

    private Book insert(Book book) {

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("title", book.getTitle())
                .addValue("author_id", book.getAuthor().getId());

        var keyHolder = new GeneratedKeyHolder();

        namedParameterJdbcOperations.update(
                "INSERT INTO books (title, author_id) VALUES (:title, :author_id)",
                params,
                keyHolder
        );

        book.setId(keyHolder.getKeyAs(Long.class));

        batchInsertGenresRelationsFor(book);
        return book;
    }

    private Book update(Book book) {
        Map<String, Object> params = Map.of("id", book.getId(), "title", book.getTitle(), "author_id", book.getAuthor().getId());

        int updatedRows = namedParameterJdbcOperations.update(
                "UPDATE books SET title = :title, author_id = :author_id WHERE id = :id",
                params
        );

        if (updatedRows == 0) {
            throw new EntityNotFoundException("Book with id " + book.getId() + " not found");
        }

        removeGenresRelationsFor(book);
        batchInsertGenresRelationsFor(book);

        return book;
    }

    private void batchInsertGenresRelationsFor(Book book) {
        if (book.getGenres() == null || book.getGenres().isEmpty()) {
            return;
        }

        List<Map<String, Long>> batchParams = book.getGenres().stream()
                .map(genre -> Map.of("book_id", book.getId(), "genre_id", genre.getId()))
                .toList();

        namedParameterJdbcOperations.batchUpdate(
                "INSERT INTO books_genres (book_id, genre_id) VALUES (:book_id, :genre_id)",
                batchParams.toArray(new Map[batchParams.size()])
        );
    }

    private void removeGenresRelationsFor(Book book) {
        Map<String, Object> params = Collections.singletonMap("book_id", book.getId());
        namedParameterJdbcOperations.update(
                "DELETE FROM books_genres WHERE book_id = :book_id",
                params
        );
    }

    private static class BookRowMapper implements RowMapper<Book> {

        @Override
        public Book mapRow(ResultSet rs, int rowNum) throws SQLException {
            long id = rs.getLong("id");
            String title = rs.getString("title");
            long authorId = rs.getLong("author_id");
            String authorName = rs.getString("author_name");
            Author author = new Author(authorId, authorName);
            return new Book(id, title, author, new ArrayList<>());
        }
    }

    @SuppressWarnings("ClassCanBeRecord")
    @RequiredArgsConstructor
    private static class BookResultSetExtractor implements ResultSetExtractor<Book> {

        @Override
        public Book extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (!rs.next()) {
                return null;
            }

            long bookId = rs.getLong("id");
            String title = rs.getString("title");
            long authorId = rs.getLong("author_id");
            String authorName = rs.getString("author_name");
            Author author = new Author(authorId, authorName);

            List<Genre> genres = new ArrayList<>();

            do {
                long genreId = rs.getLong("genre_id");
                if (!rs.wasNull()) {
                    String genreName = rs.getString("genre_name");
                    genres.add(new Genre(genreId, genreName));
                }
            } while (rs.next());

            return new Book(bookId, title, author, genres);
        }
    }

    private record BookGenreRelation(long bookId, long genreId) {
    }
}
