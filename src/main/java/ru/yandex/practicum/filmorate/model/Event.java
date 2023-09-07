package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.utility.EventOperation;
import ru.yandex.practicum.filmorate.utility.EventType;

@Data
@Builder
@AllArgsConstructor
public class Event {
    private int eventId;
    private long timestamp;
    private int userId;
    private EventType eventType;
    private EventOperation operation;
    private int entityId;
}

