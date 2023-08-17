package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.dao.MpaDbStorage;

import java.util.Collection;
import java.util.Optional;

@Service
public class MpaService {

    private final MpaDbStorage mpaDbStorage;

    @Autowired
    public MpaService(MpaDbStorage mpaDbStorage) {
        this.mpaDbStorage = mpaDbStorage;
    }

    public Optional<Mpa> getMpaById(int id) {
        return mpaDbStorage.getMpaById(id);
    }

    public Collection<Mpa> getMpas() {
        return mpaDbStorage.getMpas();
    }
}
