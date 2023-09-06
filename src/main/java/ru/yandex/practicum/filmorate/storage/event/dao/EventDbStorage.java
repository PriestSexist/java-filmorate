package ru.yandex.practicum.filmorate.storage.event.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.utility.EventOperation;
import ru.yandex.practicum.filmorate.utility.EventType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;


@Slf4j
@Component
public class EventDbStorage implements EventStorage {
    private final JdbcTemplate jdbcTemplate;

    private final UserStorage userStorage;

    public EventDbStorage(JdbcTemplate jdbcTemplate, UserStorage userStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.userStorage = userStorage;
    }

    @Override
    public List<Event> getFeed(int userId) {
        if (userStorage.getUserById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь по " + userId + " id не найден.");
        }

        final String sqlQuery = "SELECT EVENT_ID, USER_ID, timestamp, EVENT_TYPE, OPERATION, ENTITY_ID " +
                "FROM FEED " +
                "WHERE USER_ID = ? ";
        return jdbcTemplate.query(sqlQuery, this::mapRow, userId);
    }

    @Override
    public void createEvent(Event event) {
        if (event.getEntityId() < 0) {
            throw new NotFoundException("entityId не может быть отрицательным.");
        }

        final String sqlQuery = "INSERT INTO FEED " +
                "(USER_ID, timestamp, EVENT_TYPE, OPERATION, ENTITY_ID) values (?, ?, ?, ?, ?)";

        jdbcTemplate.update(
                sqlQuery,
                event.getUserId(),
                event.getTimestamp(),
                event.getEventType().name(),
                event.getOperation().name(),
                event.getEntityId()
        );
    }

    private Event mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Event.builder()
                .eventId(rs.getInt("EVENT_ID"))
                .timestamp(rs.getLong("timestamp"))
                .userId(rs.getInt("USER_ID"))
                .eventType(EventType.valueOf(rs.getString("EVENT_TYPE")))
                .operation(EventOperation.valueOf(rs.getString("OPERATION")))
                .entityId(rs.getInt("ENTITY_ID"))
                .build();
    }
}
