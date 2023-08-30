package ru.yandex.practicum.filmorate.storage.director.dao;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.List;

@Slf4j
@Primary
@Repository
@AllArgsConstructor
public class DirectorDbStorage implements DirectorStorage {
    /** Поле с экземпляром сущности для обвязки над JDBC */
    private final JdbcTemplate jdbcTemplate;

    /** Поле с экземпляром сущности маппера строки на класс User */
    private final DirectorMapper directorMapper;

    @Override
    public List<Director> getDirectors() {
        //
    }

    @Override
    public Director createDirector(Director director) {
        //
    }

    @Override
    public Director updateDirector(Director director) {
        //
    }

    @Override
    public void removeDirector(Integer id) {
        //
    }

    @Override
    public boolean isDirectorPresent(Integer id) {
        //
    }

    @Override
    public Director getDirectorById(Integer id) {
        //
    }

    @Override
    public Film createNewFilm(Film film) {
        //
    }

    @Override
    public List<Film> getFilmByDirectorId(int directorId) {
        //
    }
}
