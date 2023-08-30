package ru.yandex.practicum.filmorate.storage.director.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
@Component
public class DirectorMapper implements RowMapper<Director> {

    @Override
    public Director mapRow(ResultSet rs, int rowNum) throws SQLException {
        log.debug("Сборка обьекта Director по прочитанной из базы записи.");

        return Director.builder()
                .id(rs.getInt("director_id"))
                .name(rs.getString("name"))
                .build();
    }
}
