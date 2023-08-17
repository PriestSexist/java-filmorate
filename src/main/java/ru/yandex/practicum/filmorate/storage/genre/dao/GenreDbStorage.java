package ru.yandex.practicum.filmorate.storage.genre.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

@Slf4j
@Component
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Genre> getGenreById(int id) {
        // Запрос на получение жанра по id
        String sqlQuery = "SELECT GENRE_ID, NAME " +
                "FROM GENRES " +
                "WHERE GENRE_ID = ?";

        Genre genre;

        // Выполнение запроса
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sqlQuery, id);

        // Прохожусь по 1 строке результата запроса
        if (userRows.next()) {

            log.debug("Найден объект с id {}, и именем {}", userRows.getInt("GENRE_ID"), userRows.getString("NAME"));

            // Создаю объект жанра из запроса
            genre = createGenre(userRows);
            if (genre != null) {
                return Optional.of(genre);
            }
            return Optional.empty();
        }
        return Optional.empty();
    }

    @Override
    public Collection<Genre> getGenres() {

        // Запрос на получение всех айдишников жанров фильмов
        String sqlQuery = "SELECT GENRE_ID " +
                "FROM GENRES  ";

        Genre genre;
        Collection<Genre> genres = new ArrayList<>();

        // Выполнение запроса
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sqlQuery);

        // Прохожусь по всему результату запроса
        while (userRows.next()) {
            log.debug("Найден объект с id {} ", userRows.getInt("GENRE_ID"));

            // Достаю айдишник из запроса
            int genreId = userRows.getInt("GENRE_ID");
            // Если по нему получается достать фильм, то я добавляю его в коллекцию фильмов
            if (getGenreById(genreId).isPresent()) {
                genre = getGenreById(genreId).get();
                genres.add(genre);
            }
        }

        return genres;
    }

    private Genre createGenre(SqlRowSet userRows) {
        if (userRows.getInt("GENRE_ID") != 0) {
            return new Genre(userRows.getInt("GENRE_ID"),
                    userRows.getString("NAME"));
        }
        return null;
    }
}
