package ru.yandex.practicum.filmorate.storage.film.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Like;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    JdbcTemplate jdbcTemplate;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Film> postFilm(Film film) {

        // Запрос на добавление связи фильма и его жанра
        String sqlQueryForGenre = "INSERT INTO FILM_GENRE_CONNECTION(FILM_ID, GENRE_ID) " +
                "VALUES (?, ?)";

        // Мапа, которая содержит данные для вставки в таблицу films.
        // Ключ - название столбца, значение - значение
        HashMap<String, Object> values = new HashMap<>();
        values.put("NAME", film.getName());
        values.put("DESCRIPTION", film.getDescription());
        values.put("RELEASE_DATE", java.sql.Date.valueOf(film.getReleaseDate()));
        values.put("DURATION", film.getDuration());
        values.put("MPA_ID", film.getMpa().getId());

        // Объект для вставки значений в таблицу films через мапу
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("FILMS")
                .usingGeneratedKeyColumns("FILM_ID");

        // Айдишник фильма, который сгенерировался в бд
        int filmId = simpleJdbcInsert.executeAndReturnKey(values).intValue();

        // Вставляю жанры
        if (film.getGenres() != null) {
            log.debug("Начинаю заносить жанры в таблицу FILM_GENRE_CONNECTION");
            for (Genre genre : film.getGenres()) {
                jdbcTemplate.update(sqlQueryForGenre, filmId, genre.getId());
            }
        }

        // Не вставляю людей, которые поставили лайки, так как при post их список всегда пустой
        log.debug("Объект film с id {} занесён в таблицу FILMS", filmId);

        // Обращаюсь к бд, чтобы вернуть оттуда данные, которые туда занеслись (типа микро проверки)
        if (getFilmById(filmId).isPresent()) {
            log.debug("Объект film с id {} найден в бд", filmId);
            return getFilmById(filmId);
        }

        log.debug("Объект film с id {} не найден в бд", filmId);
        return Optional.empty();

    }

    @Override
    public Optional<Film> putFilm(Film film) {

        // Нахожу айди фильма. При изменении, он не меняется
        int filmId = film.getId();

        // Запрос на изменение фильма
        String sqlQueryUpdate = "UPDATE FILMS SET " +
                "NAME = ?, " +
                "RELEASE_DATE = ?, " +
                "DURATION = ?, " +
                "DESCRIPTION = ?, " +
                "MPA_ID = ? " +
                "WHERE FILM_ID = ?";

        // Оупшионал фильм из бд
        Optional<Film> optionalFilmInBd = getFilmById(filmId);

        // Проверка на наличие нужного фильма в бд
        if (optionalFilmInBd.isEmpty()) {
            log.debug("Объект film с id {} не найден в бд", filmId);
            return optionalFilmInBd;
        }

        // Сам фильм из бд
        Film filmInBd = optionalFilmInBd.get();

        // Проверяю, есть ли разница в лайках фильма из бд и в лайках фильма, который передали нам для замены
        if (!filmInBd.getLikes().containsAll(film.getLikes()) || !film.getLikes().containsAll(filmInBd.getLikes())) {

            // Запрос на удаление лайков
            String sqlQueryDropForLikes = "DELETE " +
                    "FROM LIKES " +
                    "WHERE FILM_ID = ?";

            // Запрос на добавление лайков
            String sqlQueryInsertForLikes = "INSERT INTO LIKES(FILM_ID, USER_ID) " +
                    "VALUES (?, ?)";

            // Если есть разница, то я удаляю всех людей из бд, которые лайкнули данный фильм
            jdbcTemplate.update(sqlQueryDropForLikes, filmId);

            // А потом добавляю новых людей, которые лайкнули фильм
            for (Like like : film.getLikes()) {
                jdbcTemplate.update(sqlQueryInsertForLikes, filmId, like.getUserId());
            }
        }

        // Проверяю, есть ли разница в жанрах фильма из бд и жанрах фильма, который передали нам для замены
        if (!filmInBd.getGenres().containsAll(film.getGenres()) || !film.getGenres().containsAll(filmInBd.getGenres())) {

            // Запрос на удаление жанров фильма
            String sqlQueryDropForFilmGenreConnection = "DELETE " +
                    "FROM FILM_GENRE_CONNECTION " +
                    "WHERE FILM_ID = ?";

            // Запрос на добавление жанров фильма
            String sqlQueryInsertForFilmGenreConnection = "INSERT INTO FILM_GENRE_CONNECTION(FILM_ID, GENRE_ID) " +
                    "VALUES (?, ?)";

            // Если есть разница, то я удаляю все жанры в бд, которые привязаны к данному фильму
            jdbcTemplate.update(sqlQueryDropForFilmGenreConnection, filmId);

            // А потом добавляю новые жанры
            for (Genre genre : film.getGenres()) {
                jdbcTemplate.update(sqlQueryInsertForFilmGenreConnection, filmId, genre.getId());
            }
        }

        // Запрос на изменение основных данных самого фильма
        jdbcTemplate.update(sqlQueryUpdate,
                film.getName(),
                java.sql.Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getDescription(),
                film.getMpa().getId(),
                film.getId());

        // Обращаюсь к бд, чтобы вернуть оттуда данные, которые туда занеслись (типа микро проверки)
        if (getFilmById(filmId).isPresent()) {
            log.debug("Объект film с id {} найден в бд", filmId);
            return getFilmById(filmId);
        }

        log.debug("Объект film с id {} не найден в бд", filmId);
        return Optional.empty();

    }

    @Override
    public Collection<Film> getFilms() {

        // Запрос на получение всех айдишников фильмов
        String sqlQuery = "SELECT f.FILM_ID " +
                "FROM FILMS AS F ";

        Film film;
        Collection<Film> films = new ArrayList<>();

        // Выполнение запроса
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sqlQuery);

        // Прохожусь по всему результату запроса
        while (userRows.next()) {
            log.debug("Найден объект с id {} ", userRows.getInt("FILM_ID"));

            // Достаю айдишник из запроса
            int filmId = userRows.getInt("FILM_ID");
            // Если по нему получается достать фильм, то я добавляю его в коллекцию фильмов
            if (getFilmById(filmId).isPresent()) {
                film = getFilmById(filmId).get();
                films.add(film);
            }
        }

        return films.stream().sorted(Comparator.comparingInt(Film::getId)).collect(Collectors.toList());

    }

    @Override
    public Optional<Film> getFilmById(int id) {

        // Запрос на получение фильма по id
        String sqlQuery = "SELECT F.FILM_ID, F.NAME, F.RELEASE_DATE, F.DURATION, F.DESCRIPTION, M.MPA_ID, M.NAME AS MNAME, LIKE_ID, L.USER_ID, G.GENRE_ID, G.NAME AS GNAME " +
                "FROM FILMS AS F " +
                "LEFT JOIN LIKES AS L on F.FILM_ID = L.FILM_ID " +
                "LEFT JOIN MPA AS M on F.MPA_ID = M.MPA_ID " +
                "LEFT JOIN FILM_GENRE_CONNECTION AS FGC on F.FILM_ID = fgc.FILM_ID " +
                "LEFT JOIN GENRES AS G on FGC.GENRE_ID = G.GENRE_ID " +
                "WHERE F.FILM_ID = ?";

        Film film;
        Genre genre;
        Like like;

        // Выполнение запроса
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sqlQuery, id);

        // Прохожусь по 1 строке результата запроса. Это делается, так как нам нужно как-то ОДИН раз собрать основную информацию о фильме
        if (userRows.next()) {

            log.debug("Найден объект с id {}, и именем {}", userRows.getInt("FILM_ID"), userRows.getString("NAME"));

            // Создаю объект фильма из запроса
            film = createFilm(userRows);

            // Отдельно создаю объекты для Like и Genre
            like = createLike(userRows);
            genre = createGenre(userRows);

            // Если лайк нашёлся и создался нормально, то я добавляю его фильму
            if (like != null) {
                film.getLikes().add(like);
            }

            // Если жанр нашёлся и создался нормально, то я добавляю его фильму
            if (genre != null) {
                film.getGenres().add(genre);
            }

            // Если больше строк нет, то возвращаю то, что есть
            if (userRows.isLast()) {
                return Optional.of(film);
            }

            log.debug("В таблице больше 1 строки, так что начинаю заполнять объект film дополнительной информацией");

            // Если больше 1 строки, то они отличаются только по Genres или Likes, так что меняю их
            while (userRows.next()) {

                // Отдельно создаю объекты для like и Genres и добавляю их в объект фильма
                like = createLike(userRows);
                genre = createGenre(userRows);

                // Если лайк нашёлся и создался нормально, то я добавляю его фильму
                if (like != null) {
                    film.getLikes().add(like);
                }

                // Если жанр нашёлся и создался нормально, то я добавляю его фильму
                if (genre != null) {
                    film.getGenres().add(genre);
                }

            }
            return Optional.of(film);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Film> putLikeToFilm(int filmId, int userId) {

        // Делаю запрос на количество связей film_id и user_id.
        // Если есть хоть 1 связь, значит пользователь добавлял лайк этому фильму
        // Если у нас больше 1 связи, значит где-то я ошибся, но этот случай я не обрабатывал
        String sqlQueryForCheck = "SELECT COUNT(LIKE_ID) AS COUNT " +
                "FROM LIKES " +
                "WHERE FILM_ID = ? AND USER_ID = ? ";

        // Исполняю запрос на проверку
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sqlQueryForCheck, filmId, userId);

        // Перехожу на ряд с результатом
        sqlRowSet.next();

        // Если результат не равняется 0 (пользователь уже поставил лайк фильму)
        if (sqlRowSet.getInt("COUNT") != 0) {
            log.debug("Пользователь пытается поставить лайк фильму, которому уже ставил лайк");
            // Если лайк от пользователя уже стоит, то я просто возвращаю объект
            return getFilmById(filmId);
        }

        // Мапа для добавления лайка фильму.
        // Ключ - название столбца, значение - значение
        HashMap<String, Object> values = new HashMap<>();
        values.put("FILM_ID", filmId);
        values.put("USER_ID", userId);

        // Объект для добавления
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("LIKES")
                .usingGeneratedKeyColumns("LIKE_ID");

        // Выполняю запрос и ставлю лайк фильму
        int id = simpleJdbcInsert.executeAndReturnKey(values).intValue();

        log.debug("Лайк film с id {} поставлен пользователем с id {} под следующим id: {}", filmId, userId, id);

        return getFilmById(filmId);
    }

    @Override
    public Optional<Film> deleteLikeFromFilm(int filmId, int userId) {

        // Делаю запрос на количество связей film_id и user_id.
        // Если есть более 1 связи или их вообще 0,
        // значит пользователь или не добавлял лайк этому фильму,
        // или я где-то опростоволосился и лайков 2+
        String sqlQueryForCheck = "SELECT COUNT(LIKE_ID) AS COUNT " +
                "FROM LIKES " +
                "WHERE FILM_ID = ? AND USER_ID = ? ";

        // Запрос на удаление лайка
        String sqlQueryForDelete = "DELETE " +
                "FROM LIKES " +
                "WHERE FILM_ID = ? AND USER_ID = ?";

        // Исполняю запрос на проверку
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sqlQueryForCheck, filmId, userId);

        // Перехожу на ряд с результатом
        sqlRowSet.next();
        // Если результат не равен 1, то количество лайков или больше 1,
        // что значит, что где-то в добавлении ошибка, или результат равен 0,
        // и тогда пользователь пытается убрать лайк фильму, хотя он его не ставил
        if (sqlRowSet.getInt("COUNT") != 1) {
            log.debug("Пользователь пытается убрать лайк фильму, которому не ставил лайк");
            return getFilmById(filmId);
        }

        // Исполняю запрос на удаление
        jdbcTemplate.update(sqlQueryForDelete, filmId, userId);

        log.debug("Убран лайк с film с id {} поставленный пользователем с id {} ", filmId, userId);

        return getFilmById(filmId);
    }

    @Override
    public Optional<Genre> getGenreById(int id) {
        // Запрос на получение жанра по id
        String sqlQuery = "SELECT GENRE_ID, NAME AS GNAME " +
                "FROM GENRES " +
                "WHERE GENRE_ID = ?";

        Genre genre;

        // Выполнение запроса
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sqlQuery, id);

        // Прохожусь по 1 строке результата запроса
        if (userRows.next()) {

            log.debug("Найден объект с id {}, и именем {}", userRows.getInt("GENRE_ID"), userRows.getString("GNAME"));

            // Создаю объект жанра из запроса
            genre = createGenre(userRows);
            if (genre != null) {
                return Optional.of(genre);
            }
            return Optional.empty();
        }
        return Optional.empty();
    }

    @Override
    public Collection<Genre> getGenres() {

        // Запрос на получение всех айдишников жанров фильмов
        String sqlQuery = "SELECT GENRE_ID " +
                "FROM GENRES  ";

        Genre genre;
        Collection<Genre> genres = new ArrayList<>();

        // Выполнение запроса
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sqlQuery);

        // Прохожусь по всему результату запроса
        while (userRows.next()) {
            log.debug("Найден объект с id {} ", userRows.getInt("GENRE_ID"));

            // Достаю айдишник из запроса
            int genreId = userRows.getInt("GENRE_ID");
            // Если по нему получается достать фильм, то я добавляю его в коллекцию фильмов
            if (getGenreById(genreId).isPresent()) {
                genre = getGenreById(genreId).get();
                genres.add(genre);
            }
        }

        return genres;
    }

    @Override
    public Optional<Mpa> getMpaById(int id) {
        // Запрос на получение MPA по id
        String sqlQuery = "SELECT MPA_ID, NAME AS MNAME " +
                "FROM MPA " +
                "WHERE MPA_ID = ?";

        Mpa mpa;

        // Выполнение запроса
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sqlQuery, id);

        // Прохожусь по 1 строке результата запроса
        if (userRows.next()) {

            log.debug("Найден объект с id {}, и именем {}", userRows.getInt("MPA_ID"), userRows.getString("MNAME"));

            // Создаю объект жанра из запроса
            mpa = createMpa(userRows);

            return Optional.of(mpa);
        }
        return Optional.empty();
    }

    @Override
    public Collection<Mpa> getMpas() {

        // Запрос на получение всех айдишников mpa фильмов
        String sqlQuery = "SELECT MPA_ID " +
                "FROM MPA  ";

        Mpa mpa;
        Collection<Mpa> mpas = new ArrayList<>();

        // Выполнение запроса
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sqlQuery);

        // Прохожусь по всему результату запроса
        while (userRows.next()) {
            log.debug("Найден объект с id {} ", userRows.getInt("MPA_ID"));

            // Достаю айдишник из запроса
            int mpaId = userRows.getInt("MPA_ID");
            // Если по нему получается достать фильм, то я добавляю его в коллекцию фильмов
            if (getMpaById(mpaId).isPresent()) {
                mpa = getMpaById(mpaId).get();
                mpas.add(mpa);
            }
        }

        return mpas;
    }

    private Like createLike(SqlRowSet userRows) {
        if (userRows.getInt("LIKE_ID") != 0) {
            return new Like(userRows.getInt("LIKE_ID"),
                    userRows.getInt("FILM_ID"),
                    userRows.getInt("USER_ID"));
        }
        return null;
    }

    private Genre createGenre(SqlRowSet userRows) {
        if (userRows.getInt("GENRE_ID") != 0) {
            return new Genre(userRows.getInt("GENRE_ID"),
                    userRows.getString("GNAME"));
        }
        return null;
    }

    private Film createFilm(SqlRowSet userRows) {
        return new Film(userRows.getInt("FILM_ID"),
                userRows.getString("NAME"),
                userRows.getString("DESCRIPTION"),
                new java.sql.Date(Objects.requireNonNull(userRows.getDate("RELEASE_DATE")).getTime()).toLocalDate(),
                userRows.getInt("DURATION"),
                createMpa(userRows));
    }

    private Mpa createMpa(SqlRowSet userRows) {
        return new Mpa(userRows.getInt("MPA_ID"),
                userRows.getString("MNAME"));
    }

}
