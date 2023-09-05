package ru.yandex.practicum.filmorate.storage.director.dao;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Primary
@Repository
@AllArgsConstructor
public class DirectorDbStorage implements DirectorStorage {
    /** Поле с экземпляром сущности для обвязки над JDBC */
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Director> getDirectors() {
        log.debug("Получение спика режисеров из базы.");

        String sqlQuery = "select * from directors order by director_id";
        return jdbcTemplate.query(sqlQuery, this::buildDirector);
    }

    @Override
    public Director getDirector(int id) {
        log.debug("Получение режисера под идентификатором {} из базы.", id);

        SqlRowSet directorRows = jdbcTemplate
                .queryForRowSet("select * from directors where director_id = ?", id);
        if (directorRows.next()) {
            return Director.builder()
                    .id(directorRows.getInt("director_id"))
                    .name(directorRows.getString("name"))
                    .build();
        } else {
            throw new NotFoundException(String.format("Режиссера с id=%d не существует", id));
        }
    }

    private Director buildDirector(ResultSet rs, int rowNum) throws SQLException {
        return Director.builder()
                .id(rs.getInt("director_id"))
                .name(rs.getString("name"))
                .build();
    }

    @Override
    public Director createDirector(Director director) {
        log.debug("Создание режисера.");

        String sqlQuery = "insert into directors (name) values (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"director_id"});
            stmt.setString(1, director.getName());
            return stmt;
        }, keyHolder);
        if (keyHolder.getKey() != null) {
            int id = keyHolder.getKey().intValue();
            return getDirector(id);
        } else {
            throw new RuntimeException(String.format("Произошла ошибка во время создания режиссера %s", director));
        }
    }

    @Override
    public Director updateDirector(Director director) {
        log.debug("Обновление режисера.");

        if (isDirectorPresent(director.getId())) {
            String sqlQuery = "update directors set name = ? where director_id = ?";
            jdbcTemplate.update(sqlQuery, director.getName(), director.getId());
        }
        return getDirector(director.getId());
    }

    @Override
    public void removeDirector(Integer id) {
        log.debug("Удаление режисера.");

        if (isDirectorPresent(id)) {
            String sqlQuery = "delete from directors where director_id = ?";
            jdbcTemplate.update(sqlQuery, id);
        }
    }

    @Override
    public boolean isDirectorPresent(Integer id) {
        log.debug("Проверка на наличие режисера по уникальному идентификатору.");

        SqlRowSet directorRows = jdbcTemplate.queryForRowSet("select * from directors where director_id=?", id);
        if (directorRows.next()) {
            return true;
        } else {
            throw new NotFoundException(String.format("Режиссера с id=%d не существует", id));
        }
    }

}
