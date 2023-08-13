package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.film.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.dao.UserDbStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class FilmControllerTest {

    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    @Test
    public void testPostFilm() {
        Mpa mpa = new Mpa(5, "NC-17");
        Genre genre = new Genre(6, "Боевик");
        Film filmForPost = new Film(10, "Viktor B Live", "Viktor B hates everyone even you.", LocalDate.of(2002, 10, 22), 60, mpa);
        HashSet<Genre> genres = new HashSet<>();

        filmForPost.getGenres().add(genre);
        genres.add(genre);

        Optional<Film> filmOptional = filmStorage.postFilm(filmForPost);

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film -> assertThat(film).hasFieldOrPropertyWithValue("id", 1))
                .hasValueSatisfying(film -> assertThat(film).hasFieldOrPropertyWithValue("name", "Viktor B Live"))
                .hasValueSatisfying(film -> assertThat(film).hasFieldOrPropertyWithValue("description", "Viktor B hates everyone even you."))
                .hasValueSatisfying(film -> assertThat(film).hasFieldOrPropertyWithValue("releaseDate", LocalDate.of(2002, 10, 22)))
                .hasValueSatisfying(film -> assertThat(film).hasFieldOrPropertyWithValue("duration", 60))
                .hasValueSatisfying(film -> assertThat(film).hasFieldOrPropertyWithValue("mpa", mpa))
                .hasValueSatisfying(film -> assertThat(film).hasFieldOrPropertyWithValue("genres", genres));
    }

    @Test
    public void testPutFilm() {
        Mpa mpa = new Mpa(5, "NC-17");
        Genre genre = new Genre(6, "Боевик");
        Film filmForPost = new Film(1, "Viktor B Live", "Viktor B hates everyone even you.", LocalDate.of(2002, 10, 22), 60, mpa);
        Film filmForPut = new Film(1, "Stas Live", "Stas B hates everyone even you.", LocalDate.of(1989, 10, 24), 120, mpa);

        HashSet<Genre> genres = new HashSet<>();

        filmForPost.getGenres().add(genre);
        filmForPut.getGenres().add(genre);
        genres.add(genre);

        filmStorage.postFilm(filmForPost);

        Optional<Film> filmOptional = filmStorage.putFilm(filmForPut);

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film -> assertThat(film).hasFieldOrPropertyWithValue("id", 1))
                .hasValueSatisfying(film -> assertThat(film).hasFieldOrPropertyWithValue("name", "Stas Live"))
                .hasValueSatisfying(film -> assertThat(film).hasFieldOrPropertyWithValue("description", "Stas B hates everyone even you."))
                .hasValueSatisfying(film -> assertThat(film).hasFieldOrPropertyWithValue("releaseDate", LocalDate.of(1989, 10, 24)))
                .hasValueSatisfying(film -> assertThat(film).hasFieldOrPropertyWithValue("duration", 120))
                .hasValueSatisfying(film -> assertThat(film).hasFieldOrPropertyWithValue("mpa", mpa))
                .hasValueSatisfying(film -> assertThat(film).hasFieldOrPropertyWithValue("genres", genres));

    }

    @Test
    public void testGetFilms() {
        Mpa mpa = new Mpa(5, "NC-17");
        Genre genre = new Genre(6, "Боевик");
        Film filmForPost1 = new Film(1, "Viktor B Live", "Viktor B hates everyone even you.", LocalDate.of(2002, 10, 22), 60, mpa);
        Film filmForPost2 = new Film(2, "Stas Live", "Stas B hates everyone even you.", LocalDate.of(1989, 10, 24), 120, mpa);

        HashSet<Genre> genres = new HashSet<>();

        filmForPost1.getGenres().add(genre);
        filmForPost2.getGenres().add(genre);
        genres.add(genre);

        filmStorage.postFilm(filmForPost1);
        filmStorage.postFilm(filmForPost2);

        List<Film> films = (List<Film>) filmStorage.getFilms();

        Optional<Film> filmOptional1 = Optional.of(films.get(0));
        Optional<Film> filmOptional2 = Optional.of(films.get(1));

        assertThat(filmOptional1)
                .isPresent()
                .hasValueSatisfying(filmFromBd -> assertThat(filmFromBd).hasFieldOrPropertyWithValue("id", 1))
                .hasValueSatisfying(filmFromBd -> assertThat(filmFromBd).hasFieldOrPropertyWithValue("name", "Viktor B Live"))
                .hasValueSatisfying(filmFromBd -> assertThat(filmFromBd).hasFieldOrPropertyWithValue("description", "Viktor B hates everyone even you."))
                .hasValueSatisfying(filmFromBd -> assertThat(filmFromBd).hasFieldOrPropertyWithValue("releaseDate", LocalDate.of(2002, 10, 22)))
                .hasValueSatisfying(filmFromBd -> assertThat(filmFromBd).hasFieldOrPropertyWithValue("duration", 60))
                .hasValueSatisfying(filmFromBd -> assertThat(filmFromBd).hasFieldOrPropertyWithValue("mpa", mpa))
                .hasValueSatisfying(filmFromBd -> assertThat(filmFromBd).hasFieldOrPropertyWithValue("genres", genres));


        assertThat(filmOptional2)
                .isPresent()
                .hasValueSatisfying(film -> assertThat(film).hasFieldOrPropertyWithValue("id", 2))
                .hasValueSatisfying(film -> assertThat(film).hasFieldOrPropertyWithValue("name", "Stas Live"))
                .hasValueSatisfying(film -> assertThat(film).hasFieldOrPropertyWithValue("description", "Stas B hates everyone even you."))
                .hasValueSatisfying(film -> assertThat(film).hasFieldOrPropertyWithValue("releaseDate", LocalDate.of(1989, 10, 24)))
                .hasValueSatisfying(film -> assertThat(film).hasFieldOrPropertyWithValue("duration", 120))
                .hasValueSatisfying(film -> assertThat(film).hasFieldOrPropertyWithValue("mpa", mpa))
                .hasValueSatisfying(film -> assertThat(film).hasFieldOrPropertyWithValue("genres", genres));
    }

    @Test
    public void testGetFilmById() {
        Mpa mpa = new Mpa(5, "NC-17");
        Genre genre = new Genre(6, "Боевик");
        Film filmForPost = new Film(1, "Viktor B Live", "Viktor B hates everyone even you.", LocalDate.of(2002, 10, 22), 60, mpa);
        HashSet<Genre> genres = new HashSet<>();

        filmForPost.getGenres().add(genre);
        genres.add(genre);

        filmStorage.postFilm(filmForPost);

        Optional<Film> filmOptional = filmStorage.getFilmById(filmForPost.getId());

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film -> assertThat(film).hasFieldOrPropertyWithValue("id", 1))
                .hasValueSatisfying(film -> assertThat(film).hasFieldOrPropertyWithValue("name", "Viktor B Live"))
                .hasValueSatisfying(film -> assertThat(film).hasFieldOrPropertyWithValue("description", "Viktor B hates everyone even you."))
                .hasValueSatisfying(film -> assertThat(film).hasFieldOrPropertyWithValue("releaseDate", LocalDate.of(2002, 10, 22)))
                .hasValueSatisfying(film -> assertThat(film).hasFieldOrPropertyWithValue("duration", 60))
                .hasValueSatisfying(film -> assertThat(film).hasFieldOrPropertyWithValue("mpa", mpa))
                .hasValueSatisfying(film -> assertThat(film).hasFieldOrPropertyWithValue("genres", genres));

    }

    @Test
    public void testPutLikeToFilm() {
        User userForPost = new User(1, "vitekb650@gmaill.com", "PriestSexist", "Viktor", LocalDate.of(2002, 10, 22));
        userStorage.postUser(userForPost);

        Mpa mpa = new Mpa(5, "NC-17");
        Genre genre = new Genre(6, "Боевик");
        Like like = new Like(1, 1, 1);
        Film filmForPost = new Film(1, "Viktor B Live", "Viktor B hates everyone even you.", LocalDate.of(2002, 10, 22), 60, mpa);
        HashSet<Genre> genres = new HashSet<>();
        HashSet<Like> likes = new HashSet<>();

        filmForPost.getGenres().add(genre);
        genres.add(genre);
        likes.add(like);

        filmStorage.postFilm(filmForPost);

        Optional<Film> filmOptional = filmStorage.putLikeToFilm(filmForPost.getId(), userForPost.getId());

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film -> assertThat(film).hasFieldOrPropertyWithValue("id", 1))
                .hasValueSatisfying(film -> assertThat(film).hasFieldOrPropertyWithValue("name", "Viktor B Live"))
                .hasValueSatisfying(film -> assertThat(film).hasFieldOrPropertyWithValue("description", "Viktor B hates everyone even you."))
                .hasValueSatisfying(film -> assertThat(film).hasFieldOrPropertyWithValue("releaseDate", LocalDate.of(2002, 10, 22)))
                .hasValueSatisfying(film -> assertThat(film).hasFieldOrPropertyWithValue("duration", 60))
                .hasValueSatisfying(film -> assertThat(film).hasFieldOrPropertyWithValue("mpa", mpa))
                .hasValueSatisfying(film -> assertThat(film).hasFieldOrPropertyWithValue("genres", genres))
                .hasValueSatisfying(film -> assertThat(film).hasFieldOrPropertyWithValue("likes", likes));

    }

    @Test
    public void testDeleteLikeToFilm() {
        User userForPost = new User(1, "vitekb650@gmaill.com", "PriestSexist", "Viktor", LocalDate.of(2002, 10, 22));
        userStorage.postUser(userForPost);

        Mpa mpa = new Mpa(5, "NC-17");
        Genre genre = new Genre(6, "Боевик");
        Film filmForPost = new Film(1, "Viktor B Live", "Viktor B hates everyone even you.", LocalDate.of(2002, 10, 22), 60, mpa);
        HashSet<Genre> genres = new HashSet<>();
        HashSet<Like> likes = new HashSet<>();

        filmForPost.getGenres().add(genre);
        genres.add(genre);

        filmStorage.postFilm(filmForPost);
        filmStorage.putLikeToFilm(filmForPost.getId(), userForPost.getId());

        Optional<Film> filmOptional = filmStorage.deleteLikeFromFilm(filmForPost.getId(), userForPost.getId());

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film -> assertThat(film).hasFieldOrPropertyWithValue("id", 1))
                .hasValueSatisfying(film -> assertThat(film).hasFieldOrPropertyWithValue("name", "Viktor B Live"))
                .hasValueSatisfying(film -> assertThat(film).hasFieldOrPropertyWithValue("description", "Viktor B hates everyone even you."))
                .hasValueSatisfying(film -> assertThat(film).hasFieldOrPropertyWithValue("releaseDate", LocalDate.of(2002, 10, 22)))
                .hasValueSatisfying(film -> assertThat(film).hasFieldOrPropertyWithValue("duration", 60))
                .hasValueSatisfying(film -> assertThat(film).hasFieldOrPropertyWithValue("mpa", mpa))
                .hasValueSatisfying(film -> assertThat(film).hasFieldOrPropertyWithValue("genres", genres))
                .hasValueSatisfying(film -> assertThat(film).hasFieldOrPropertyWithValue("likes", likes));

    }

    @Test
    public void testGetGenreById() {
        Optional<Genre> genreOptional = filmStorage.getGenreById(6);

        assertThat(genreOptional)
                .isPresent()
                .hasValueSatisfying(genre -> assertThat(genre).hasFieldOrPropertyWithValue("id", 6))
                .hasValueSatisfying(genre -> assertThat(genre).hasFieldOrPropertyWithValue("name", "Боевик"));

    }

    @Test
    public void testGetAllGenres() {
        List<Genre> genres = (List<Genre>) filmStorage.getGenres();

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

    @Test
    public void testGetMpaById() {
        Optional<Mpa> mpaOptional = filmStorage.getMpaById(1);

        assertThat(mpaOptional)
                .isPresent()
                .hasValueSatisfying(mpa -> assertThat(mpa).hasFieldOrPropertyWithValue("id", 1))
                .hasValueSatisfying(mpa -> assertThat(mpa).hasFieldOrPropertyWithValue("name", "G"));

    }

    @Test
    public void testGetAllMpas() {
        List<Mpa> mpas = (List<Mpa>) filmStorage.getMpas();

        Optional<Mpa> mpa1FromBd = Optional.of(mpas.get(0));
        Optional<Mpa> mpa2FromBd = Optional.of(mpas.get(1));
        Optional<Mpa> mpa3FromBd = Optional.of(mpas.get(2));
        Optional<Mpa> mpa4FromBd = Optional.of(mpas.get(3));
        Optional<Mpa> mpa5FromBd = Optional.of(mpas.get(4));

        assertThat(mpa1FromBd)
                .isPresent()
                .hasValueSatisfying(mpa -> assertThat(mpa).hasFieldOrPropertyWithValue("id", 1))
                .hasValueSatisfying(mpa -> assertThat(mpa).hasFieldOrPropertyWithValue("name", "G"));

        assertThat(mpa2FromBd)
                .isPresent()
                .hasValueSatisfying(mpa -> assertThat(mpa).hasFieldOrPropertyWithValue("id", 2))
                .hasValueSatisfying(mpa -> assertThat(mpa).hasFieldOrPropertyWithValue("name", "PG"));

        assertThat(mpa3FromBd)
                .isPresent()
                .hasValueSatisfying(mpa -> assertThat(mpa).hasFieldOrPropertyWithValue("id", 3))
                .hasValueSatisfying(mpa -> assertThat(mpa).hasFieldOrPropertyWithValue("name", "PG-13"));

        assertThat(mpa4FromBd)
                .isPresent()
                .hasValueSatisfying(mpa -> assertThat(mpa).hasFieldOrPropertyWithValue("id", 4))
                .hasValueSatisfying(mpa -> assertThat(mpa).hasFieldOrPropertyWithValue("name", "R"));

        assertThat(mpa5FromBd)
                .isPresent()
                .hasValueSatisfying(mpa -> assertThat(mpa).hasFieldOrPropertyWithValue("id", 5))
                .hasValueSatisfying(mpa -> assertThat(mpa).hasFieldOrPropertyWithValue("name", "NC-17"));

    }


}