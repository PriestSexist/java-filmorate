package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

public interface DirectorStorage {
    /**
     * Метод для получения всех режисеров
     *
     * @return - список всех пользователей
     */
    List<Director> getDirectors();

    /**
     * Метод для получения всех режисеров
     *
     * @param id - идентификатор режисера
     * @return - сущность режисера
     */
    Director getDirector(int id);

    /**
     * Метод для создания нового режисера
     *
     * @param director - сущность нового режисера
     * @return сущность созданного режисера с уникальным идентификатором
     */
    Director createDirector(Director director);

    /**
     * Метод для обновления режисера
     *
     * @param director - сущность режисера
     * @return сущность измененного режисера
     */
    Director updateDirector(Director director);

    /**
     * Метод для удаления режисера
     *
     * @param id - уникальный идентификатор удаляемого режисера
     */
    void removeDirector(Integer id);

    /**
     * Метод для проверки на существование записи режисера
     *
     * @param id - уникальный идентификатор режисера
     */
    boolean isDirectorPresent(Integer id);
}
