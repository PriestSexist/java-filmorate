package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;
import ru.yandex.practicum.filmorate.utility.EventOperation;
import ru.yandex.practicum.filmorate.utility.EventType;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventStorage eventDbStorage;

    public List<Event> getFeed(int userId) {
        return eventDbStorage.getFeed(userId);
    }

    public void createEvent(int userId, EventType eventType, EventOperation eventOperation, int entityId) {
        Event event = Event.builder()
                .timestamp(Instant.now().toEpochMilli())
                .userId(userId)
                .eventType(eventType)
                .operation(eventOperation)
                .entityId(entityId)
                .build();
        eventDbStorage.createEvent(event);
    }
}
