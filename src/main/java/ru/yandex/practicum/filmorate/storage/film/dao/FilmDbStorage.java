package ru.yandex.practicum.filmorate.storage.film.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsertForFilms;
    private final SimpleJdbcInsert simpleJdbcInsertForLikes;
    private final MpaStorage mpaStorage;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate, MpaStorage mpaStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsertForFilms = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("FILMS")
                .usingGeneratedKeyColumns("FILM_ID");
        this.simpleJdbcInsertForLikes = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("LIKES")
                .usingGeneratedKeyColumns("LIKE_ID");
        this.mpaStorage = mpaStorage;
    }

    @Override
    public Optional<Film> postFilm(Film film) {

        // Запрос на добавление связи фильма и его жанра
        String sqlQueryForGenre = "INSERT INTO FILM_GENRE_CONNECTION(FILM_ID, GENRE_ID) " +
                "VALUES (?, ?)";

        String sqlQueryForDirector = "INSERT INTO FILM_DIRECTORS(FILM_ID, DIRECTOR_ID) " +
                "VALUES (?, ?)";

        ArrayList<Object[]> genres = new ArrayList<>();
        ArrayList<Object[]> directors = new ArrayList<>();

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

        // Вставляю режисеров
        if (film.getDirectors() != null) {
            for (Director director : film.getDirectors()) {
                directors.add(new Object[]{filmId, director.getId()});
            }
            jdbcTemplate.batchUpdate(sqlQueryForDirector, directors);
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
        ArrayList<Object[]> directors = new ArrayList<>();

        // Запрос на изменение основных данных самого фильма
        int countOfUpdatedRows = jdbcTemplate.update(sqlQueryUpdate,
                film.getName(),
                java.sql.Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getDescription(),
                film.getMpa().getId(),
                film.getId());

        if (countOfUpdatedRows == 0) {
            return Optional.empty();
        }

        // Я решил убрать тут лишнюю проверку, так как если бы фильма не было в бд,
        // То тогда бы countOfUpdatedRows было равно 0 и вернулся бы Optional.empty()
        // Фильм из бд
        Film filmInDb = getFilmById(filmId).get();

        // Проверяю, есть ли разница в лайках фильма из бд и в лайках фильма, который передали нам для замены
        if (!filmInDb.getLikes().containsAll(film.getLikes()) || !film.getLikes().containsAll(filmInDb.getLikes())) {

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
        if (!filmInDb.getGenres().containsAll(film.getGenres()) || !film.getGenres().containsAll(filmInDb.getGenres())) {

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

        // Проверяю, есть ли разница в режиссёрах фильма из бд и режиссёрах фильма, который передали нам для замены
        if (!filmInDb.getDirectors().containsAll(film.getDirectors()) || !film.getDirectors().containsAll(filmInDb.getDirectors())) {

            // Запрос на удаление режиссёров фильма
            String sqlQueryDropForFilmGenreConnection = "DELETE " +
                    "FROM FILM_DIRECTORS " +
                    "WHERE FILM_ID = ?";

            // Запрос на добавление режиссёров фильма
            String sqlQueryInsertForFilmGenreConnection = "INSERT INTO FILM_DIRECTORS(FILM_ID, DIRECTOR_ID) " +
                    "VALUES (?, ?)";

            // Если есть разница, то я удаляю всех режиссёров в бд, которые привязаны к данному фильму
            jdbcTemplate.update(sqlQueryDropForFilmGenreConnection, filmId);

            // А потом добавляю новые жанры
            for (Director director : film.getDirectors()) {
                directors.add(new Object[]{filmId, director.getId()});
            }
            jdbcTemplate.batchUpdate(sqlQueryInsertForFilmGenreConnection, directors);
        }

        // Обращаюсь к бд, чтобы вернуть оттуда данные, которые туда занеслись (типа микро проверки)
        return Optional.of(film);

    }

    @Override
    public Collection<Film> getFilms() {

        // Запрос на получение всех фильмов
        String sqlQueryForGettingFilms = "SELECT F.FILM_ID, F.NAME, F.RELEASE_DATE, F.DURATION, F.DESCRIPTION, M.MPA_ID, M.NAME AS MNAME, LIKE_ID, L.USER_ID, G.GENRE_ID, G.NAME AS GNAME, D.DIRECTOR_ID, D.NAME AS DNAME " +
                "FROM FILMS AS F " +
                "LEFT JOIN LIKES AS L on F.FILM_ID = L.FILM_ID " +
                "LEFT JOIN MPA AS M on F.MPA_ID = M.MPA_ID " +
                "LEFT JOIN FILM_GENRE_CONNECTION AS FGC on F.FILM_ID = fgc.FILM_ID " +
                "LEFT JOIN GENRES AS G on FGC.GENRE_ID = G.GENRE_ID " +
                "LEFT JOIN FILM_DIRECTORS AS FD on FD.FILM_ID = F.FILM_ID " +
                "LEFT JOIN DIRECTORS AS D on FD.DIRECTOR_ID = d.director_id";

        Film film;
        Genre genre;
        Like like;
        Director director;

        // Мапа айди фильма, сам фильм
        HashMap<Integer, Film> films = new HashMap<>();

        // Выполнение запроса
        SqlRowSet filmsFromDb = jdbcTemplate.queryForRowSet(sqlQueryForGettingFilms);

        // Прохожусь по всем строкам
        while (filmsFromDb.next()) {

            // Если ключ содержится в мапе фильмов
            if (films.containsKey(filmsFromDb.getInt("FILM_ID"))) {

                // Достаю фильм из мапы
                film = films.get(filmsFromDb.getInt("FILM_ID"));

                // Отдельно создаю объекты для like и Genres и добавляю их в объект фильма
                like = createLike(filmsFromDb);
                genre = createGenre(filmsFromDb);
                director = createDirector(filmsFromDb);

                // Если лайк нашёлся и создался нормально, то я добавляю его фильму
                if (like != null) {
                    film.getLikes().add(like);
                }

                // Если жанр нашёлся и создался нормально, то я добавляю его фильму
                if (genre != null) {
                    if (!film.getGenres().contains(genre)) {
                        film.getGenres().add(genre);
                    }
                }

                if (director != null) {
                    film.getDirectors().add(director);
                }

                // Если ключа нет в мапе
            } else {

                // Создаю объект фильма из запроса
                film = createFilm(filmsFromDb);

                // Отдельно создаю объекты для Like и Genre
                like = createLike(filmsFromDb);
                genre = createGenre(filmsFromDb);
                director = createDirector(filmsFromDb);

                // Если лайк нашёлся и создался нормально, то я добавляю его фильму
                if (like != null) {
                    film.getLikes().add(like);
                }

                // Если жанр нашёлся и создался нормально, то я добавляю его фильму
                if (genre != null) {
                    if (!film.getGenres().contains(genre)) {
                        film.getGenres().add(genre);
                    }
                }

                if (director != null) {
                    film.getDirectors().add(director);
                }

                films.put(film.getId(), film);

            }
        }

        return films.values().stream().sorted(Comparator.comparingInt(Film::getId)).collect(Collectors.toList());
    }

    @Override
    public Optional<Film> getFilmById(int id) {

        // Запрос на получение фильма по id
        String sqlQueryForOneFilm = "SELECT F.FILM_ID, F.NAME, F.RELEASE_DATE, F.DURATION, " +
                "F.DESCRIPTION, M.MPA_ID, M.NAME AS MNAME, LIKE_ID, L.USER_ID, G.GENRE_ID, " +
                "G.NAME AS GNAME, D.DIRECTOR_ID, D.NAME AS DNAME " +
                "FROM FILMS AS F " +
                "LEFT JOIN LIKES AS L on F.FILM_ID = L.FILM_ID " +
                "LEFT JOIN MPA AS M on F.MPA_ID = M.MPA_ID " +
                "LEFT JOIN FILM_GENRE_CONNECTION AS FGC on F.FILM_ID = fgc.FILM_ID " +
                "LEFT JOIN GENRES AS G on FGC.GENRE_ID = G.GENRE_ID " +
                "LEFT JOIN FILM_DIRECTORS AS FD on FD.FILM_ID = F.FILM_ID " +
                "LEFT JOIN DIRECTORS AS D on FD.DIRECTOR_ID = d.director_id " +
                "WHERE F.FILM_ID = ?";

        Film film;
        Genre genre;
        Like like;
        Director director;

        // Выполнение запроса
        SqlRowSet rowsForOneFilm = jdbcTemplate.queryForRowSet(sqlQueryForOneFilm, id);

        // Прохожусь по 1 строке результата запроса. Это делается, так как нам нужно как-то ОДИН раз собрать основную информацию о фильме
        if (rowsForOneFilm.next()) {

            log.debug("Найден объект с id {}, и именем {}", rowsForOneFilm.getInt("FILM_ID"), rowsForOneFilm.getString("NAME"));

            // Создаю объект фильма из запроса
            film = createFilm(rowsForOneFilm);

            // Отдельно создаю объекты для Like и Genre
            like = createLike(rowsForOneFilm);
            genre = createGenre(rowsForOneFilm);
            director = createDirector(rowsForOneFilm);

            // Если лайк нашёлся и создался нормально, то я добавляю его фильму
            if (like != null) {
                film.getLikes().add(like);
            }

            // Если жанр нашёлся и создался нормально, то я добавляю его фильму
            if (genre != null && !film.getGenres().contains(genre)) {
                film.getGenres().add(genre);
            }

            if (director != null) {
                film.getDirectors().add(director);
            }

            // Если больше строк нет, то возвращаю то, что есть
            if (rowsForOneFilm.isLast()) {
                return Optional.of(film);
            }

            log.debug("В таблице больше 1 строки, так что начинаю заполнять объект film дополнительной информацией");

            // Если больше 1 строки, то они отличаются только по Genres или Likes, так что меняю их
            while (rowsForOneFilm.next()) {

                // Отдельно создаю объекты для like и Genres и добавляю их в объект фильма
                like = createLike(rowsForOneFilm);
                genre = createGenre(rowsForOneFilm);
                director = createDirector(rowsForOneFilm);

                // Если лайк нашёлся и создался нормально, то я добавляю его фильму
                if (like != null) {
                    film.getLikes().add(like);
                }

                // Если жанр нашёлся и создался нормально, то я добавляю его фильму
                if (genre != null) {
                    if (!film.getGenres().contains(genre)) {
                        film.getGenres().add(genre);
                    }
                }

                if (director != null) {
                    film.getDirectors().add(director);
                }

            }
            return Optional.of(film);
        }
        return Optional.empty();
    }

    @Override
    public List<Film> getPopularByGenre(int count, int genreId) {
        String sqlQuery = "SELECT * " +
                "FROM FILMS AS F " +
                "LEFT JOIN LIKES AS L on F.FILM_ID = L.FILM_ID " +
                "LEFT JOIN FILM_GENRE_CONNECTION AS FGC on F.FILM_ID = fgc.FILM_ID " +
                "WHERE FGC.GENRE_ID = ?" +
                "GROUP BY f.film_id ORDER BY COUNT(l.user_id) DESC LIMIT ?";
        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeFilm(rs), genreId, count);

    }

    @Override
    public List<Film> getPopularByYear(int count, int year) {
        String sqlQuery = "SELECT * " +
                "FROM FILMS AS F " +
                "LEFT JOIN LIKES AS L on F.FILM_ID = L.FILM_ID " +
                "WHERE EXTRACT(YEAR FROM f.release_date) = ? " +
                "GROUP BY f.film_id ORDER BY COUNT(l.user_id) DESC LIMIT ?";
        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeFilm(rs), year, count);
    }

    @Override
    public List<Film> getPopularByGenreByYear(int count, int genreId, int year) {
        String sqlQuery = "SELECT * FROM films " +
                "INNER JOIN FILM_GENRE_CONNECTION ON FILM_GENRE_CONNECTION.film_id = films.film_id " +
                "LEFT JOIN LIKES AS L on films.FILM_ID = L.FILM_ID " +
                "WHERE EXTRACT(YEAR FROM films.release_date) = ? AND FILM_GENRE_CONNECTION.genre_id = ?" +
                "GROUP BY films.film_id ORDER BY COUNT(l.user_id) DESC LIMIT ?";
        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeFilm(rs), year, genreId, count);
    }

    @Override
    public Optional<Film> putLikeToFilm(int filmId, int userId) {

        // Мапа для добавления лайка фильму.
        // Ключ - название столбца, значение - значение
        HashMap<String, Object> values = new HashMap<>();
        values.put("FILM_ID", filmId);
        values.put("USER_ID", userId);

        try {
            int id = simpleJdbcInsertForLikes.executeAndReturnKey(values).intValue();
            log.debug("Лайк film с id {} поставлен пользователем с id {} под следующим id: {}", filmId, userId, id);
            return getFilmById(filmId);
        } catch (DataIntegrityViolationException exception) {
            return Optional.empty();
        }

    }

    @Override
    public Optional<Film> deleteLikeFromFilm(int filmId, int userId) {
        // Запрос на удаление лайка
        String sqlQueryForDelete = "DELETE " +
                "FROM LIKES " +
                "WHERE FILM_ID = ? AND USER_ID = ?";

        // Выполняю запрос на удаление
        int countOfUpdatedRows = jdbcTemplate.update(sqlQueryForDelete, filmId, userId);

        if (countOfUpdatedRows == 1) {
            log.debug("Убран лайк с film с id {} поставленный пользователем с id {} ", filmId, userId);
            return getFilmById(filmId);
        }

        log.debug("У film с id {} не было лайка поставленного пользователем с id {} ", filmId, userId);
        return Optional.empty();
    }

    @Override
    public List<Film> searchByTitle(String query) {
        String sqlQuery = "SELECT FILMS.FILM_ID, FILMS.NAME, FILMS.DESCRIPTION, FILMS.RELEASE_DATE, FILMS.DURATION, FILMS.MPA_ID FROM films " +
                "LEFT JOIN likes ON films.film_id = likes.film_id " +
                "WHERE LOWER(films.name) LIKE LOWER(?) " +
                "GROUP BY films.film_id ";
        String searchQuery = "%" + query + "%";
        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeFilm(rs), searchQuery);
    }

    @Override
    public List<Film> searchByDirector(String query) {
        String sqlQuery = "SELECT FILMS.FILM_ID, FILMS.NAME, FILMS.DESCRIPTION, FILMS.RELEASE_DATE, FILMS.DURATION, FILMS.MPA_ID, DIRECTORS.NAME FROM films " +
                "LEFT JOIN likes ON films.film_id = likes.film_id " +
                "LEFT JOIN film_directors ON films.film_id = film_directors.film_id " +
                "LEFT JOIN directors ON film_directors.director_id = directors.director_id " +
                "WHERE LOWER(directors.name) LIKE LOWER(?) " +
                "GROUP BY films.film_id ";
        String searchQuery = "%" + query + "%";
        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeFilm(rs), searchQuery);
    }

    @Override
    public List<Film> searchByTitleByDirector(String query) {
        String sqlQuery = "SELECT FILMS.FILM_ID, FILMS.NAME, FILMS.DESCRIPTION, FILMS.RELEASE_DATE, FILMS.DURATION, FILMS.MPA_ID, DIRECTORS.NAME FROM films " +
                "LEFT JOIN likes ON films.film_id = likes.film_id " +
                "LEFT JOIN film_directors ON films.film_id = film_directors.film_id " +
                "LEFT JOIN directors ON film_directors.director_id = directors.director_id " +
                "WHERE LOWER(films.name) LIKE LOWER(?) OR LOWER(directors.name) LIKE LOWER(?)" +
                "GROUP BY films.film_id ";
        String searchQuery = "%" + query + "%";
        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeFilm(rs), searchQuery, searchQuery);
    }

    // --- начало блока (формирование film)  ---
    private Film makeFilm(ResultSet rs) throws SQLException {
        int id = rs.getInt("film_id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        LocalDate releaseDate = rs.getDate("release_date").toLocalDate();
        int duration = rs.getInt("duration");
        int mpaId = rs.getInt("mpa_id");
        Mpa mpa = mpaStorage.getMpaById(mpaId).get();
        ArrayList<Genre> genres = getFilmGenreByFilmId(id);
        HashSet<Like> likes = getLikes(id);
        List<Director> directors = getDirectorsByFilmId(id);
        Film film = Film.builder().name(name).description(description).releaseDate(releaseDate).duration(duration)
                .mpa(mpa).build();
        film.getGenres().addAll(genres);
        film.getLikes().addAll(likes);
        film.getDirectors().addAll(directors);
        film.setId(id);
        return film;
    }

    public List<Director> getDirectorsByFilmId(int filmId) {
        String sqlQuery = "SELECT * FROM directors WHERE director_id IN " +
                "(SELECT director_id FROM film_directors WHERE film_id = ?) ORDER BY directors.name DESC ";
        SqlRowSet directorsRows = jdbcTemplate.queryForRowSet(sqlQuery, filmId);
        List<Director> filmDirectors = new ArrayList<>();
        while (directorsRows.next()) {
            Director directorFilm = new Director(directorsRows.getInt("director_id"),
                    directorsRows.getString("name"));
            filmDirectors.add(directorFilm);
        }
        return filmDirectors;
    }

    public ArrayList<Genre> getFilmGenreByFilmId(int filmId) {
        String sqlQuery = "SELECT * FROM genres WHERE genre_id in " +
                "(SELECT genre_id FROM FILM_GENRE_CONNECTION WHERE film_id = ?) ORDER BY genre_id ";
        SqlRowSet genresRows = jdbcTemplate.queryForRowSet(sqlQuery, filmId);
        ArrayList<Genre> filmGenres = new ArrayList<>();
        while (genresRows.next()) {
            Genre genreFilm = new Genre(genresRows.getInt("genre_id"),
                    genresRows.getString("name"));
            filmGenres.add(genreFilm);
        }
        return filmGenres;
    }

    public HashSet<Like> getLikes(int filmId) {
        String sqlQuery = "SELECT * FROM likes WHERE film_id = ?";
        SqlRowSet likesRows = jdbcTemplate.queryForRowSet(sqlQuery, filmId);
        HashSet<Like> likes = new HashSet<>();
        while (likesRows.next()) {
            Like like = createLike(likesRows);
            likes.add(like);
        }
        return likes;
    }
    // --- конец блока ---

    private Like createLike(SqlRowSet sqlRowSet) {
        if (sqlRowSet.getInt("LIKE_ID") != 0) {
            return new Like(sqlRowSet.getInt("LIKE_ID"),
                    sqlRowSet.getInt("FILM_ID"),
                    sqlRowSet.getInt("USER_ID"));
        }
        return null;
    }

    private Genre createGenre(SqlRowSet sqlRowSet) {
        if (sqlRowSet.getInt("GENRE_ID") != 0) {
            return new Genre(sqlRowSet.getInt("GENRE_ID"),
                    sqlRowSet.getString("GNAME"));
        }
        return null;
    }

    private Director createDirector(SqlRowSet sqlRowSet) {
        if (sqlRowSet.getInt("DIRECTOR_ID") != 0) {
            return new Director(sqlRowSet.getInt("DIRECTOR_ID"),
                    sqlRowSet.getString("DNAME"));
        }
        return null;
    }

    private Film createFilm(SqlRowSet sqlRowSet) {
        return new Film(sqlRowSet.getInt("FILM_ID"),
                sqlRowSet.getString("NAME"),
                sqlRowSet.getString("DESCRIPTION"),
                new java.sql.Date(Objects.requireNonNull(sqlRowSet.getDate("RELEASE_DATE")).getTime()).toLocalDate(),
                sqlRowSet.getInt("DURATION"),
                createMpa(sqlRowSet));
    }

    private Mpa createMpa(SqlRowSet sqlRowSet) {
        return new Mpa(sqlRowSet.getInt("MPA_ID"),
                sqlRowSet.getString("MNAME"));
    }

    @Override
    public List<Integer> getFilmsIdByDirectorId(int directorId) {
        String sqlQuery = "select film_id from film_directors where director_id = ? order by film_id";
        return jdbcTemplate.queryForList(sqlQuery, Integer.class, directorId);
    }

    @Override
    public Optional<Integer> deleteFilm(int filmId) {
        final String sqlQuery = "DELETE FROM FILMS WHERE FILM_ID=?";

        int deletedRows = jdbcTemplate.update(sqlQuery, filmId);
        if (deletedRows != 1) {
            return Optional.empty();
        }
        return Optional.of(filmId);
    }

}
