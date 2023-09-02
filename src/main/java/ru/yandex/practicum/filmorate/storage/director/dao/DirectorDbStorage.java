package ru.yandex.practicum.filmorate.storage.director.dao;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        log.debug("Получение спика режисеров из базы.");

        String sqlQuery = "SELECT * FROM directors";

        return generatingListOfDirectors(sqlQuery);
    }

    @Override
    public Director getDirector(int id) {
        log.debug("Получение режисера под идентификатором {} из базы.", id);

        String sqlQuery = "SELECT * FROM directors WHERE directors.director_id = " + id;

        List<Director> directors = generatingListOfDirectors(sqlQuery);
        if (!directors.isEmpty()) {
            return directors.get(0);
        } else {
            throw new NotFoundException(String.format("Режисер с идентификатором %s не найден", id));
        }
    }

    /**
     * Метод генерации списка режисеров по ответу из базы данных
     * @param sqlQuery - запрос в базу
     * @return список режисеров
     */
    private List<Director> generatingListOfDirectors(String sqlQuery) {
        Map<Integer, Director> directors = new HashMap<>();

        jdbcTemplate.query(sqlQuery, rs -> {
            Integer directorId = rs.getInt("director_id");
            String directorName = rs.getString("name");

            if (!directors.containsKey(directorId)) {
                Director director = directorMapper.mapRow(rs, directorId);
                directors.put(directorId, director);
            }

            if (directorName != null) {
                directors.get(directorId).setName(rs.getString("name"));
            }
        });
        return new ArrayList<>(directors.values());
    }

    @Override
    public Director createDirector(Director director) {
        log.debug("Создание режисера.");

        String sqlQuery = "INSERT INTO directors (name) VALUES(?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"director_id"});
            stmt.setString(1, director.getName());
            return stmt;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            director.setId((Integer) keyHolder.getKey());
        }

        return getDirector(director.getId());
    }

    @Override
    public Director updateDirector(Director director) {
        log.debug("Обновление режисера.");

        String sqlQuery = "UPDATE directors SET name = ?, WHERE director_id = ?";
        jdbcTemplate.update(sqlQuery, director.getName(), director.getId());

        return getDirector(director.getId());
    }

    @Override
    public void removeDirector(Integer id) {
        log.debug("Удаление режисера.");

        String sqlQuery = "DELETE FROM directors WHERE director_id = ?";
        jdbcTemplate.update(sqlQuery, id);
    }

    @Override
    public boolean isDirectorPresent(Integer id) {
        log.debug("Проверка на наличие режисера по уникальному идентификатору.");

        String query = "SELECT COUNT (*) FROM directors WHERE director_id = ?";

        if (jdbcTemplate.queryForObject(query, Integer.class, id) == 0) {
            throw new NotFoundException(String.format("Режисер с идентификатором %s не найден", id));
        }
        return true;
    }
}
