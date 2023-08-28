package ru.yandex.practicum.filmorate.storage.mpa.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

@Slf4j
@Component
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Mpa> getMpaById(int id) {
        // Запрос на получение MPA по id
        String sqlQuery = "SELECT MPA_ID, NAME " +
                "FROM MPA " +
                "WHERE MPA_ID = ?";

        Mpa mpa;

        // Выполнение запроса
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sqlQuery, id);

        // Прохожусь по 1 строке результата запроса
        if (userRows.next()) {

            log.debug("Найден объект с id {}, и именем {}", userRows.getInt("MPA_ID"), userRows.getString("NAME"));

            // Создаю объект жанра из запроса
            mpa = createMpa(userRows);

            return Optional.of(mpa);
        }
        return Optional.empty();
    }

    @Override
    public Collection<Mpa> getMpas() {

        // Запрос на получение всех айдишников mpa фильмов
        String sqlQuery = "SELECT MPA_ID " +
                "FROM MPA  ";

        Mpa mpa;
        Collection<Mpa> mpas = new ArrayList<>();

        // Выполнение запроса
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sqlQuery);

        // Прохожусь по всему результату запроса
        while (userRows.next()) {
            log.debug("Найден объект с id {} ", userRows.getInt("MPA_ID"));

            // Достаю айдишник из запроса
            int mpaId = userRows.getInt("MPA_ID");
            // Если по нему получается достать фильм, то я добавляю его в коллекцию фильмов
            if (getMpaById(mpaId).isPresent()) {
                mpa = getMpaById(mpaId).get();
                mpas.add(mpa);
            }
        }

        return mpas;
    }

    private Mpa createMpa(SqlRowSet userRows) {
        return new Mpa(userRows.getInt("MPA_ID"),
                userRows.getString("NAME"));
    }
}
