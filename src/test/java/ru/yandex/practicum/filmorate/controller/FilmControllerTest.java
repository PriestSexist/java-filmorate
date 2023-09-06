package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.event.dao.EventDbStorage;
import ru.yandex.practicum.filmorate.storage.director.dao.DirectorDbStorage;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.dao.UserDbStorage;

import java.time.LocalDate;
import java.util.ArrayList;
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
    private final FilmService filmService;
    private final UserDbStorage userStorage;
    private final DirectorDbStorage directorStorage;

    private final UserService userService;

    private final EventDbStorage eventDbStorage;

    @Test
    public void testPostFilm() {
        Mpa mpa = new Mpa(5, "NC-17");
        Genre genre = new Genre(6, "Боевик");
        Film filmForPost = new Film(10, "Viktor B Live", "Viktor B hates everyone even you.", LocalDate.of(2002, 10, 22), 60, mpa);
        ArrayList<Genre> genres = new ArrayList<>();

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

        ArrayList<Genre> genres = new ArrayList<>();

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

        ArrayList<Genre> genres = new ArrayList<>();

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
        ArrayList<Genre> genres = new ArrayList<>();

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
        ArrayList<Genre> genres = new ArrayList<>();
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
        ArrayList<Genre> genres = new ArrayList<>();
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
    public void testGetFeedFilm() {
        User userForPost = new User(1, "vitekb650@gmaill.com", "PriestSexist", "Viktor", LocalDate.of(2002, 10, 22));

        Mpa mpa = new Mpa(5, "NC-17");
        Genre genre = new Genre(6, "Боевик");
        Like like = new Like(1, 1, 1);
        Film filmForPost = new Film(1, "Viktor B Live", "Viktor B hates everyone even you.", LocalDate.of(2002, 10, 22), 60, mpa);
        ArrayList<Genre> genres = new ArrayList<>();
        HashSet<Like> likes = new HashSet<>();

        filmForPost.getGenres().add(genre);
        genres.add(genre);
        likes.add(like);

        filmService.postFilm(filmForPost);
        userService.postUser(userForPost);

        filmService.putLikeToFilm(filmForPost.getId(), userForPost.getId());
        filmService.deleteLikeToFilm(filmForPost.getId(), userForPost.getId());

        List<Event> feed = eventDbStorage.getFeed(filmForPost.getId());

        Assertions.assertEquals(feed.size(), 2);
    }

    @Test
    void testGetCommonFilms() {
        User user = new User(1, "vitekb650@gmaill.com", "PriestSexist", "Viktor", LocalDate.of(2002, 10, 22));
        User friend = new User(2, "satori@gmaill.com", "Satori", "Stas", LocalDate.of(1989, 10, 24));

        userStorage.postUser(user);
        userStorage.postUser(friend);

        Mpa mpa = new Mpa(5, "NC-17");
        Genre genre = new Genre(6, "Боевик");
        Film filmForPost = new Film(1, "Viktor B Live", "Viktor B hates everyone even you.", LocalDate.of(2002, 10, 22), 60, mpa);
        ArrayList<Genre> genres = new ArrayList<>();

        filmForPost.getGenres().add(genre);
        genres.add(genre);

        filmStorage.postFilm(filmForPost);
        filmStorage.putLikeToFilm(filmForPost.getId(), user.getId());
        filmStorage.putLikeToFilm(filmForPost.getId(), friend.getId());

        List<Film> films = (List<Film>) filmService.getCommonFilms(user.getId(), friend.getId());

        Optional<Film> filmOptional = films.stream().findFirst();

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
    public void testGetTopFilms() {
        Film film1 = Film.builder()
                .id(1)
                .name("movie1")
                .description("movie1")
                .releaseDate(LocalDate.of(2001, 01, 01))
                .duration(121)
                .mpa(new Mpa(5, "NC-17"))
                .build();
        film1.getGenres().add(new Genre(6, "Боевик"));
        film1.getDirectors().add(directorStorage.createDirector(new Director(1, "Директор1")));
        filmStorage.postFilm(film1);
        filmStorage.putLikeToFilm(1, 1);


        Film film2 = Film.builder()
                .id(2)
                .name("movie2")
                .description("movie2")
                .releaseDate(LocalDate.of(2002, 02, 02))
                .duration(122)
                .mpa(new Mpa(3, "PG-13"))
                .build();
        film2.getGenres().add(new Genre(1, "Комедия"));
        film2.getDirectors().add(directorStorage.createDirector(new Director(2, "movieДиректор2")));
        filmStorage.postFilm(film2);

        Film film3 = Film.builder()
                .id(3)
                .name("movie3")
                .description("movie3")
                .releaseDate(LocalDate.of(2003, 03, 03))
                .duration(123)
                .mpa(new Mpa(1, "G"))
                .build();
        film3.getGenres().add(new Genre(6, "Боевик"));
        film3.getDirectors().add(directorStorage.createDirector(new Director(3, "Директор3")));
        filmStorage.postFilm(film3);

        User user = User.builder()
                .id(1)
                .email("one@yandex.ru")
                .login("one")
                .name("One")
                .birthday(LocalDate.of(2001, 01, 01))
                .build();
        userStorage.postUser(user);

        List<Film> topFilmsByGenreByYear = filmStorage.getPopularByGenreByYear(3, 6, 2003);
        assertThat(topFilmsByGenreByYear)
                .isNotEmpty()
                .hasSize(1)
                .containsExactly(filmStorage.getFilmById(film3.getId()).get());

        List<Film> topFilmsByGenre = filmStorage.getPopularByGenre(3, 6);
        assertThat(topFilmsByGenre)
                .isNotEmpty()
                .hasSize(2);

        List<Film> topFilmsByYear = filmStorage.getPopularByYear(3, 2001);
        assertThat(topFilmsByYear)
                .isNotEmpty()
                .hasSize(1)
                .containsExactly(filmStorage.getFilmById(film1.getId()).get());

    }


    @Test
    public void testSearchByTitleByDirector() {
        Film film1 = Film.builder()
                .id(1)
                .name("movie1")
                .description("movie1")
                .releaseDate(LocalDate.of(2001, 01, 01))
                .duration(121)
                .mpa(new Mpa(5, "NC-17"))
                .build();
        film1.getGenres().add(new Genre(6, "Боевик"));
        film1.getDirectors().add(directorStorage.createDirector(new Director(1, "Директор1")));
        filmStorage.postFilm(film1);
        filmStorage.putLikeToFilm(1, 1);

        Film film2 = Film.builder()
                .id(2)
                .name("movie2")
                .description("movie2")
                .releaseDate(LocalDate.of(2002, 02, 02))
                .duration(122)
                .mpa(new Mpa(3, "PG-13"))
                .build();
        film2.getGenres().add(new Genre(1, "Комедия"));
        film2.getDirectors().add(directorStorage.createDirector(new Director(2, "movieДиректор2")));
        filmStorage.postFilm(film2);

        Film film3 = Film.builder()
                .id(3)
                .name("cinema3")
                .description("cinema3")
                .releaseDate(LocalDate.of(2003, 03, 03))
                .duration(123)
                .mpa(new Mpa(1, "G"))
                .build();
        film3.getGenres().add(new Genre(6, "Боевик"));
        film3.getDirectors().add(directorStorage.createDirector(new Director(3, "Директор3")));
        filmStorage.postFilm(film3);

        User user = User.builder()
                .id(1)
                .email("one@yandex.ru")
                .login("one")
                .name("One")
                .birthday(LocalDate.of(2001, 01, 01))
                .build();
        userStorage.postUser(user);

        List<Film> searchByTitle = filmStorage.searchByTitle("MOv");
        List<Film> assertList1 = new ArrayList<>();
        assertList1.add(filmStorage.getFilmById(film1.getId()).get());
        assertList1.add(filmStorage.getFilmById(film2.getId()).get());

        assertThat(searchByTitle)
                .isNotEmpty()
                .hasSize(2)
                .isEqualTo(assertList1);


        List<Film> searchByDirector = filmStorage.searchByDirector("Ректор3");
        List<Film> assertList2 = new ArrayList<>();
        assertList2.add(filmStorage.getFilmById(film3.getId()).get());

        assertThat(searchByDirector)
                .isNotEmpty()
                .hasSize(1)
                .isEqualTo(assertList2);

        List<Film> searchByTitleByDirector = filmStorage.searchByTitleByDirector("viE");
        List<Film> assertList3 = new ArrayList<>();
        assertList3.add(filmStorage.getFilmById(film1.getId()).get());
        assertList3.add(filmStorage.getFilmById(film2.getId()).get());

        assertThat(searchByTitleByDirector)
                .isNotEmpty()
                .hasSize(2)
                .isEqualTo(assertList3);

    }

}