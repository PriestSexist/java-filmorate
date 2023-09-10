package ru.yandex.practicum.filmorate.storage.user.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component()
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsertForUsers;
    private final SimpleJdbcInsert simpleJdbcInsertForFriendRequests;

    @Autowired
    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsertForUsers = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("USERS")
                .usingGeneratedKeyColumns("USER_ID");
        this.simpleJdbcInsertForFriendRequests = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("FRIEND_REQUEST")
                .usingGeneratedKeyColumns("FRIEND_REQUEST_ID");
    }

    @Override
    public Optional<User> postUser(User user) {

        // Мапа, которая содержит данные для вставки в таблицу users.
        // Ключ - название столбца, значение - значение
        HashMap<String, Object> values = new HashMap<>();
        values.put("EMAIL", user.getEmail());
        values.put("LOGIN", user.getLogin());
        values.put("NAME", user.getName());
        values.put("BIRTHDAY", java.sql.Date.valueOf(user.getBirthday()));

        // Айдишник пользователя, который сгенерировался в бд
        int userId = simpleJdbcInsertForUsers.executeAndReturnKey(values).intValue();

        // Не вставляю друзей и лайки, так как при post их список всегда пустой
        log.debug("Объект user с id {} занесён в таблицу users", userId);

        // Меняю айди на айди из бд, ведь это всё, чем отличаются запись в бд от объекта
        user.setId(userId);

        return Optional.of(user);
    }

    @Override
    public Optional<User> putUser(User user) {

        // Нахожу айди юзера. При изменении, он не меняется
        int userId = user.getId();

        // Запрос на изменение юзера
        String sqlQueryUpdate = "UPDATE USERS SET " +
                "EMAIL = ?, " +
                "LOGIN = ?, " +
                "NAME = ?, " +
                "BIRTHDAY = ? " +
                "WHERE USER_ID = ?";

        ArrayList<Object[]> likes = new ArrayList<>();
        ArrayList<Object[]> friendships = new ArrayList<>();

        // Запрос на изменение основных данных самого юзера
        int countOfUpdatedRows = jdbcTemplate.update(sqlQueryUpdate,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                java.sql.Date.valueOf(user.getBirthday()),
                userId);

        if (countOfUpdatedRows == 0) {
            return Optional.empty();
        }

        // Я решил убрать тут лишнюю проверку, так как если бы пользователя не было в бд,
        // то тогда бы countOfUpdatedRows было равно 0 и вернулся бы Optional.empty()
        // Сам юзер из БД
        User userInDb = getUserById(userId).get();

        // Проверяю, есть ли разница в лайках пользователе из бд и лайках пользователя, которого передали нам для замены
        if (!userInDb.getLikes().containsAll(user.getLikes())) {

            // Запрос на удаление лайков
            String sqlQueryDropForLikes = "DELETE " +
                    "FROM LIKES " +
                    "WHERE USER_ID = ?";

            // Запрос на добавление лайков
            String sqlQueryInsertForLikes = "INSERT INTO LIKES(FILM_ID, USER_ID) " +
                    "VALUES (?, ?)";

            // Если есть разница, то я удаляю все лайки от данного пользователя
            jdbcTemplate.update(sqlQueryDropForLikes, userId);

            // А потом добавляю новые лайки от данного пользователя
            for (Like like : user.getLikes()) {
                likes.add(new Object[]{like.getFilmId(), userId});
            }
            jdbcTemplate.batchUpdate(sqlQueryInsertForLikes, likes);
        }

        // Проверяю, есть ли разница в друзьях юзера из бд и друзьях юзера, которого передали нам для замены
        if (!userInDb.getFriendShips().containsAll(user.getFriendShips())) {

            // Запрос на удаление друзей
            String sqlQueryDropForFriends = "DELETE " +
                    "FROM FRIEND_REQUEST " +
                    "WHERE USER_ID = ?";

            // Запрос на добавление запросов в друзья и друзей
            String sqlQueryInsertForFriends = "INSERT INTO FRIEND_REQUEST(USER_ID, FRIEND_ID) " +
                    "VALUES (?, ?)";

            // Если есть разница, то я удаляю всех друзей из бд, которые привязаны к данному пользователю
            jdbcTemplate.update(sqlQueryDropForFriends, userId);

            // А потом добавляю новых друзей
            for (FriendShip friendShip : user.getFriendShips()) {
                friendships.add(new Object[]{userId, friendShip.getFriendId()});
            }
            jdbcTemplate.batchUpdate(sqlQueryInsertForFriends, friendships);
        }

        return Optional.of(user);
    }

    @Override
    public Collection<User> getUsers() {

        // Запрос на получение всех айдишников пользователей
        String sqlQueryForUsers = "SELECT U.USER_ID, U.EMAIL, U.LOGIN, U.NAME, U.BIRTHDAY, L.LIKE_ID, L.FILM_ID, FR.FRIEND_REQUEST_ID, FR.FRIEND_ID " +
                "FROM USERS AS U " +
                "LEFT JOIN LIKES AS L on U.USER_ID = L.USER_ID " +
                "LEFT JOIN FRIEND_REQUEST AS FR on U.USER_ID = FR.USER_ID ";

        User user;
        FriendShip friendShip;
        Like like;
        // Мапа айди пользователя, сам пользователь
        HashMap<Integer, User> users = new HashMap<>();

        // Выполнение запроса
        SqlRowSet rowsFromDb = jdbcTemplate.queryForRowSet(sqlQueryForUsers);

        // Прохожусь по всем строкам
        while (rowsFromDb.next()) {

            // Если ключ содержится в мапе пользователей
            if (users.containsKey(rowsFromDb.getInt("USER_ID"))) {

                //Достаю пользователя из мапы
                user = users.get(rowsFromDb.getInt("USER_ID"));

                // Отдельно создаю объекты для Like и FriendShip
                like = createLike(rowsFromDb);
                friendShip = createFriendRequest(rowsFromDb);

                // Если лайк нашёлся и создался нормально, то я добавляю его фильму от пользователя
                if (like != null) {
                    user.getLikes().add(like);
                }

                // Если дружба нашлась и создалась нормально, то я добавляю её пользователю
                if (friendShip != null) {
                    user.getFriendShips().add(friendShip);
                }

            } else {

                // Создаю объект пользователя из запроса
                user = createUser(rowsFromDb);

                // Отдельно создаю объекты для Like и FriendShip
                like = createLike(rowsFromDb);
                friendShip = createFriendRequest(rowsFromDb);

                // Если лайк нашёлся и создался нормально, то я добавляю его фильму от пользователя
                if (like != null) {
                    user.getLikes().add(like);
                }

                // Если дружба нашлась и создалась нормально, то я добавляю её пользователю
                if (friendShip != null) {
                    user.getFriendShips().add(friendShip);
                }
                users.put(user.getId(), user);


            }
        }
        return users.values().stream().sorted(Comparator.comparingInt(User::getId)).collect(Collectors.toList());
    }

    @Override
    public Optional<User> getUserById(int id) {

        // Запрос на получение пользователя по id
        String sqlQueryForOneUser = "SELECT U.USER_ID, U.EMAIL, U.LOGIN, U.NAME, U.BIRTHDAY, L.LIKE_ID, L.FILM_ID, FR.FRIEND_REQUEST_ID, FR.FRIEND_ID " +
                "FROM USERS AS U " +
                "LEFT JOIN LIKES AS L on U.USER_ID = L.USER_ID " +
                "LEFT JOIN FRIEND_REQUEST AS FR on U.USER_ID = FR.USER_ID " +
                "WHERE U.USER_ID = ?";

        User user;
        FriendShip friendShip;
        Like like;

        // Выполнение запроса
        SqlRowSet rowsForOneUser = jdbcTemplate.queryForRowSet(sqlQueryForOneUser, id);

        // Прохожусь по первой строке результата запроса
        if (rowsForOneUser.next()) {

            log.debug("Найден объект с id {}, и логином {}", rowsForOneUser.getInt("USER_ID"), rowsForOneUser.getString("LOGIN"));

            // Создаю объект пользователя из запроса
            user = createUser(rowsForOneUser);

            // Отдельно создаю объекты для Like и FriendShip
            like = createLike(rowsForOneUser);
            friendShip = createFriendRequest(rowsForOneUser);

            // Если лайк нашёлся и создался нормально, то я добавляю его фильму от пользователя
            if (like != null) {
                user.getLikes().add(like);
            }

            // Если дружба нашлась и создалась нормально, то я добавляю её пользователю
            if (friendShip != null) {
                user.getFriendShips().add(friendShip);
            }

            // Если больше строк нет, то возвращаю то, что есть
            if (rowsForOneUser.isLast()) {
                return Optional.of(user);
            }

            log.debug("В таблице больше 1 строки, так что начинаю заполнять объект user дополнительной информацией");

            // Если больше 1 строки, то они отличаются только по Like или FriendShip, так что меняю их
            while (rowsForOneUser.next()) {

                // Отдельно создаю объекты для Like и FriendShip
                like = createLike(rowsForOneUser);
                friendShip = createFriendRequest(rowsForOneUser);

                // Если лайк нашёлся и создался нормально, то я добавляю его фильму от пользователя
                if (like != null) {
                    user.getLikes().add(like);
                }

                // Если дружба нашлась и создалась нормально, то я добавляю её пользователю
                if (friendShip != null) {
                    user.getFriendShips().add(friendShip);
                }

            }
            return Optional.of(user);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> putUserFriend(int userId, int friendId) {

        // Мапа, которая содержит данные для вставки в таблицу users.
        // Ключ - название столбца, значение - значение
        HashMap<String, Object> values = new HashMap<>();
        values.put("USER_ID", userId);
        values.put("FRIEND_ID", friendId);

        try {
            int id = simpleJdbcInsertForFriendRequests.executeAndReturnKey(values).intValue();
            log.debug("Пользователи с id {} и id {} теперь друзья под следующим id: {}", userId, friendId, id);
            return getUserById(userId);
        } catch (DataIntegrityViolationException exception) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> deleteUserFriend(int userId, int friendId) {

        // Запрос на удаление дружбы
        String sqlQueryForDelete = "DELETE " +
                "FROM FRIEND_REQUEST " +
                "WHERE USER_ID = ? AND FRIEND_ID = ? ";

        int countOfUpdatedRows = jdbcTemplate.update(sqlQueryForDelete, userId, friendId);

        if (countOfUpdatedRows == 1) {
            log.debug("Пользователи с id {} и id {} больше не друзья", userId, friendId);
            return getUserById(userId);
        }

        log.debug("Пользователи с id {} и id {} не были друзьями", userId, friendId);
        return Optional.empty();
    }

    @Override
    public Collection<User> getFriends(int userId) {
        // Запрос на получение айдишников всех друзей
        String sqlQueryForFriendsId = "SELECT FRIEND_ID " +
                "FROM FRIEND_REQUEST " +
                "WHERE USER_ID = ? ";
        int id;
        Collection<User> friends = new ArrayList<>();

        // Выполнение запроса
        SqlRowSet sqlRowSetForFriendsId = jdbcTemplate.queryForRowSet(sqlQueryForFriendsId, userId);

        // прохожусь по всему результату запроса и, добавляю друзей в список, но если я не нашёл его, то не добавляю, а пропускаю эту итерацию
        while (sqlRowSetForFriendsId.next()) {
            id = sqlRowSetForFriendsId.getInt("FRIEND_ID");
            if (getUserById(id).isEmpty()) {
                continue;
            }
            friends.add(getUserById(id).get());
        }

        return friends;

    }

    @Override
    public Collection<User> getCommonFriends(int userId, int otherId) {
        Collection<User> myFriends = new HashSet<>(getFriends(userId));
        Collection<User> otherFriends = new HashSet<>(getFriends(otherId));

        // retainAll удаляет все неодинаковые элементы
        myFriends.retainAll(otherFriends);

        return myFriends;
    }

    @Override
    public HashMap<Integer, HashMap<Integer, Film>> getRecommendationFilms(int userId) {
        // Запрос на получение всех фильмов
        String sqlQueryForGettingFilms = "SELECT F.FILM_ID, F.NAME, F.RELEASE_DATE, F.DURATION, F.DESCRIPTION, M.MPA_ID, M.NAME AS MNAME, LIKE_ID, L.USER_ID, G.GENRE_ID, G.NAME AS GNAME " +
                "FROM LIKES AS L " +
                "LEFT JOIN FILMS AS F on L.FILM_ID = F.FILM_ID " +
                "LEFT JOIN MPA AS M on F.MPA_ID = M.MPA_ID " +
                "LEFT JOIN FILM_GENRE_CONNECTION AS FGC on F.FILM_ID = FGC.FILM_ID " +
                "LEFT JOIN GENRES AS G on FGC.GENRE_ID = G.GENRE_ID ";

        Film film;
        Genre genre;

        // Мапа айди пользователя, мапа лайкнутых фильмов, где ключ - айди фильма, а значение - фильм
        HashMap<Integer, HashMap<Integer, Film>> usersAndFilms = new HashMap<>();

        // Выполнение запроса
        SqlRowSet filmsFromDb = jdbcTemplate.queryForRowSet(sqlQueryForGettingFilms);

        // Прохожусь по всем строкам
        while (filmsFromDb.next()) {

            // Если ключ содержится в мапе фильмов
            if (usersAndFilms.containsKey(filmsFromDb.getInt("USER_ID")) && usersAndFilms.get(filmsFromDb.getInt("USER_ID")).containsKey(filmsFromDb.getInt("FILM_ID"))) {

                //Достаю фильм из мапы
                film = usersAndFilms.get(filmsFromDb.getInt("USER_ID")).get(filmsFromDb.getInt("FILM_ID"));

                // Отдельно создаю объект для Genre и добавляю его в объект фильма
                genre = createGenre(filmsFromDb);

                // Если жанр нашёлся и создался нормально, то я добавляю его фильму
                if (genre != null) {
                    film.getGenres().add(genre);
                }

                // Если ключа нет в мапе
            } else {

                // Создаю объект фильма из запроса
                film = createFilm(filmsFromDb);

                // Отдельно создаю объект для Genre
                genre = createGenre(filmsFromDb);

                // Если жанр нашёлся и создался нормально, то я добавляю его фильму
                if (genre != null) {
                    film.getGenres().add(genre);
                }

                // Если у мапы usersAndFilms ещё нет мапы с фильмами, то я создаю и заполняю. Если уже есть, то не создаю
                if (usersAndFilms.get(filmsFromDb.getInt("USER_ID")) == null) {
                    HashMap<Integer, Film> films = new HashMap<>();
                    films.put(film.getId(), film);
                    usersAndFilms.put(filmsFromDb.getInt("USER_ID"), films);
                    continue;
                }

                usersAndFilms.get(filmsFromDb.getInt("USER_ID")).put(film.getId(), film);


            }
        }
        // Возвращаю заполненную мапу айди пользователя, мапа лайкнутых фильмов, где ключ - айди фильма, а значение - фильм
        return usersAndFilms;
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

    private FriendShip createFriendRequest(SqlRowSet sqlRowSet) {
        if (sqlRowSet.getInt("FRIEND_REQUEST_ID") != 0) {
            return new FriendShip(sqlRowSet.getInt("USER_ID"),
                    sqlRowSet.getInt("FRIEND_ID"));
        }
        return null;
    }

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

    private User createUser(SqlRowSet sqlRowSet) {
        return new User(sqlRowSet.getInt("USER_ID"),
                sqlRowSet.getString("EMAIL"),
                sqlRowSet.getString("LOGIN"),
                sqlRowSet.getString("NAME"),
                new java.sql.Date(Objects.requireNonNull(sqlRowSet.getDate("BIRTHDAY")).getTime()).toLocalDate());
    }

    @Override
    public Optional<Integer> deleteUser(int userId) {
        final String sqlQuery = "DELETE FROM USERS WHERE USER_ID=?";

        int deletedRows = jdbcTemplate.update(sqlQuery, userId);
        if (deletedRows != 1) {
            return Optional.empty();
        }
        return Optional.of(userId);
    }

}
