package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.dao.GenreDbStorage;

import java.util.Collection;
import java.util.Optional;

@Service
public class GenreService {

    private final GenreDbStorage genreStorage;

    @Autowired
    public GenreService(GenreDbStorage genreStorage) {
        this.genreStorage = genreStorage;
    }

    public Optional<Genre> getGenreById(int id) {
        return genreStorage.getGenreById(id);
    }

    public Collection<Genre> getGenres() {
        return genreStorage.getGenres();
    }
}
