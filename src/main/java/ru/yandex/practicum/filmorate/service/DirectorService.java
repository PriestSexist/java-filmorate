package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class DirectorService {

    /** Поле слоя хранения режисеров */
    private final DirectorStorage directorStorage;


    /**
     * Метод получения всех режисеров
     * @return список режисеров
     */
    public List<Director> getDirectors() {
        //
    }

    /**
     * Метод получения режисера по уникальному идентификатору
     * @param id - идентификатор режисера
     * @return режисер
     */
    public Director getDirectorById(int id) {
        //
    }

    /**
     * Метод создания режисера
     * @param director - создаваемый режисер
     * @return сохраненный режисер
     */
    public Director createDirector(Director director) {
        //
    }

    /**
     * Метод обновления режисера
     * @param director - обновляемый режисер
     * @return обновленный режисер
     */
    public Director updateDirector(Director director) {
        //
    }

    /**
     * Метод удаления режисера
     * @param id - уникальный идентификатор режисера
     */
    public void removeDirector(int id) {
        //
    }
}
