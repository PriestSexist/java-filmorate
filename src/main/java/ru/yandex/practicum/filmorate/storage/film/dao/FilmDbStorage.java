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
@Component
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsertForFilms;
    private final SimpleJdbcInsert simpleJdbcInsertForLikes;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsertForFilms = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("FILMS")
                .usingGeneratedKeyColumns("FILM_ID");
        this.simpleJdbcInsertForLikes = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("LIKES")
                .usingGeneratedKeyColumns("LIKE_ID");
    }

    @Override
    public Optional<Film> postFilm(Film film) {

        // Запрос на добавление связи фильма и его жанра
        String sqlQueryForGenre = "INSERT INTO FILM_GENRE_CONNECTION(FILM_ID, GENRE_ID) " +
                "VALUES (?, ?)";

        ArrayList<Object[]> genres = new ArrayList<>();

        // Мапа, которая содержит данные для вставки в таблицу films.
        // Ключ - название столбца, значение - значение
        HashMap<String, Object> values = new HashMap<>();
        values.put("NAME", film.getName());
        values.put("DESCRIPTION", film.getDescription());
        values.put("RELEASE_DATE", java.sql.Date.valueOf(film.getReleaseDate()));
        values.put("DURATION", film.getDuration());
        values.put("MPA_ID", film.getMpa().getId());

        // Айдишник фильма, который сгенерировался в бд
        int filmId = simpleJdbcInsertForFilms.executeAndReturnKey(values).intValue();

        // Запись в бд и объект отличаются по данным только айдишником. Так что мы можем просто изменить айдишник и вернуть тот же самый объект
        film.setId(filmId);

        // Вставляю жанры
        if (film.getGenres() != null) {
            log.debug("Начинаю заносить жанры в таблицу FILM_GENRE_CONNECTION");
            for (Genre genre : film.getGenres()) {
                genres.add(new Object[]{filmId, genre.getId()});
            }
            jdbcTemplate.batchUpdate(sqlQueryForGenre, genres);
        }

        // Не вставляю людей, которые поставили лайки, так как при post их список всегда пустой
        log.debug("Объект film с id {} занесён в таблицу FILMS", filmId);

        return Optional.of(film);

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

        ArrayList<Object[]> likes = new ArrayList<>();
        ArrayList<Object[]> genres = new ArrayList<>();

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
                likes.add(new Object[]{filmId, like.getUserId()});
            }
            jdbcTemplate.batchUpdate(sqlQueryInsertForLikes, likes);
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
                genres.add(new Object[]{filmId, genre.getId()});
            }
            jdbcTemplate.batchUpdate(sqlQueryInsertForFilmGenreConnection, genres);
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
        return Optional.of(film);

    }

    @Override
    public Collection<Film> getFilms() {

        // Запрос на получение всех фильмов
        String sqlQuery = "SELECT F.FILM_ID, F.NAME, F.RELEASE_DATE, F.DURATION, F.DESCRIPTION, M.MPA_ID, M.NAME AS MNAME, LIKE_ID, L.USER_ID, G.GENRE_ID, G.NAME AS GNAME " +
                "FROM FILMS AS F " +
                "LEFT JOIN LIKES AS L on F.FILM_ID = L.FILM_ID " +
                "LEFT JOIN MPA AS M on F.MPA_ID = M.MPA_ID " +
                "LEFT JOIN FILM_GENRE_CONNECTION AS FGC on F.FILM_ID = fgc.FILM_ID " +
                "LEFT JOIN GENRES AS G on FGC.GENRE_ID = G.GENRE_ID ";

        int prevFilmId;
        Film film;
        Genre genre;
        Like like;
        Collection<Film> films = new ArrayList<>();

        // Выполнение запроса
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sqlQuery);

        // Делаю из 1 строки объект
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

            // Если это была первая и последняя строка, то вайл не запустится и надо добавлять тут
            if (userRows.isLast()) {
                films.add(film);
                return films.stream().sorted(Comparator.comparingInt(Film::getId)).collect(Collectors.toList());
            }

            // Если ещё есть какие-то строки, то я
            // Запоминаю айди предыдущего фильма. Он нужен, чтобы мы могли различать, где строка с новым фильмом,
            // А где строка которая отличается только по лайкам и жанрам
            prevFilmId = userRows.getInt("FILM_ID");

            // Если больше 1 строки, то либо это 1 фильм и строки отличаются только по Genres или Likes, сл-но меняю их.
            // Либо это другой фильм
            while (userRows.next()) {

                if (prevFilmId != userRows.getInt("FILM_ID")) {

                    // Добавляю фильм в лист
                    films.add(film);

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

                    prevFilmId = userRows.getInt("FILM_ID");

                } else {

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

                // Если это была последняя строка, то следующей итерации не будет и надо добавлять сейчас
                if (userRows.isLast()) {
                    films.add(film);
                }
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

        // Выполняю запрос и ставлю лайк фильму
        int id = simpleJdbcInsertForLikes.executeAndReturnKey(values).intValue();

        log.debug("Лайк film с id {} поставлен пользователем с id {} под следующим id: {}", filmId, userId, id);

        return getFilmById(filmId);
    }

    @Override
    public Optional<Film> deleteLikeFromFilm(int filmId, int userId) {

        // Запрос на удаление лайка
        String sqlQueryForDelete = "DELETE " +
                "FROM LIKES " +
                "WHERE FILM_ID = ? AND USER_ID = ?";

        // Выполняю запрос на удаление
        jdbcTemplate.update(sqlQueryForDelete, filmId, userId);

        log.debug("Убран лайк с film с id {} поставленный пользователем с id {} ", filmId, userId);

        return getFilmById(filmId);
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
