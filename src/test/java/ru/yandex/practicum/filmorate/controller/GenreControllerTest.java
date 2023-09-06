package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.storage.genre.dao.GenreDbStorage;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class GenreControllerTest {

    private final GenreDbStorage genreStorage;

    @Test
    public void testGetGenreById() {
        Optional<Genre> genreOptional = genreStorage.getGenreById(6);

        assertThat(genreOptional)
                .isPresent()
                .hasValueSatisfying(genre -> assertThat(genre).hasFieldOrPropertyWithValue("id", 6))
                .hasValueSatisfying(genre -> assertThat(genre).hasFieldOrPropertyWithValue("name", "Боевик"));

    }

    @Test
    public void testGetAllGenres() {
        List<Genre> genres = (List<Genre>) genreStorage.getGenres();

        Optional<Genre> genre1FromBd = Optional.of(genres.get(0));
        Optional<Genre> genre2FromBd = Optional.of(genres.get(1));
        Optional<Genre> genre3FromBd = Optional.of(genres.get(2));
        Optional<Genre> genre4FromBd = Optional.of(genres.get(3));
        Optional<Genre> genre5FromBd = Optional.of(genres.get(4));
        Optional<Genre> genre6FromBd = Optional.of(genres.get(5));

        assertThat(genre1FromBd)
                .isPresent()
                .hasValueSatisfying(genre -> assertThat(genre).hasFieldOrPropertyWithValue("id", 1))
                .hasValueSatisfying(genre -> assertThat(genre).hasFieldOrPropertyWithValue("name", "Комедия"));

        assertThat(genre2FromBd)
                .isPresent()
                .hasValueSatisfying(genre -> assertThat(genre).hasFieldOrPropertyWithValue("id", 2))
                .hasValueSatisfying(genre -> assertThat(genre).hasFieldOrPropertyWithValue("name", "Драма"));

        assertThat(genre3FromBd)
                .isPresent()
                .hasValueSatisfying(genre -> assertThat(genre).hasFieldOrPropertyWithValue("id", 3))
                .hasValueSatisfying(genre -> assertThat(genre).hasFieldOrPropertyWithValue("name", "Мультфильм"));

        assertThat(genre4FromBd)
                .isPresent()
                .hasValueSatisfying(genre -> assertThat(genre).hasFieldOrPropertyWithValue("id", 4))
                .hasValueSatisfying(genre -> assertThat(genre).hasFieldOrPropertyWithValue("name", "Триллер"));

        assertThat(genre5FromBd)
                .isPresent()
                .hasValueSatisfying(genre -> assertThat(genre).hasFieldOrPropertyWithValue("id", 5))
                .hasValueSatisfying(genre -> assertThat(genre).hasFieldOrPropertyWithValue("name", "Документальный"));

        assertThat(genre6FromBd)
                .isPresent()
                .hasValueSatisfying(genre -> assertThat(genre).hasFieldOrPropertyWithValue("id", 6))
                .hasValueSatisfying(genre -> assertThat(genre).hasFieldOrPropertyWithValue("name", "Боевик"));

    }
}
