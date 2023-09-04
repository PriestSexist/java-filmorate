package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class DirectorService {

    /**
     * Поле слоя хранения режисеров
     */
    private final DirectorStorage directorStorage;


    /**
     * Метод получения всех режисеров
     *
     * @return список режисеров
     */
    public List<Director> getDirectors() {
        return directorStorage.getDirectors();
    }

    /**
     * Метод получения режисера по уникальному идентификатору
     *
     * @param id - идентификатор режисера
     * @return режисер
     */
    public Director getDirector(int id) {
        if (directorStorage.isDirectorPresent(id)) {
            return directorStorage.getDirector(id);
        } else {
            throw new NotFoundException(String.format("Режисер с идентификатором %s не найден", id));
        }
    }

    /**
     * Метод создания режисера
     *
     * @param director - создаваемый режисер
     * @return сохраненный режисер
     */
    public Director createDirector(Director director) {
        return directorStorage.createDirector(director);
    }

    /**
     * Метод обновления режисера
     *
     * @param director - обновляемый режисер
     * @return обновленный режисер
     */
    public Director updateDirector(Director director) {
        int id = director.getId();

        if (!directorStorage.isDirectorPresent(id)) {
            throw new NotFoundException(String.format("Режисер с идентификатором %s не найден", id));
        }

        return directorStorage.updateDirector(director);
    }

    /**
     * Метод удаления режисера
     *
     * @param id - уникальный идентификатор режисера
     */
    public void removeDirector(int id) {
        if (directorStorage.isDirectorPresent(id)) {
            directorStorage.removeDirector(id);
        }
    }
}
