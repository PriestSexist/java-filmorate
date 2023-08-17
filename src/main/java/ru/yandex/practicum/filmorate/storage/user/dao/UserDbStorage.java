package ru.yandex.practicum.filmorate.storage.user.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.FriendShip;
import ru.yandex.practicum.filmorate.model.Like;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component("userDbStorage")
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


        // Оупшионал юзер из бд
        Optional<User> optionalUserInBd = getUserById(userId);

        // Проверка на наличие нужного юзера в бд
        if (optionalUserInBd.isEmpty()) {
            log.debug("Объект user с id {} не найден в бд", userId);
            return optionalUserInBd;
        }

        // Сам юзер из БД
        User userInBd = optionalUserInBd.get();

        // Проверяю, есть ли разница в лайках пользователе из бд и лайках пользователя, которого передали нам для замены
        if (!userInBd.getLikes().containsAll(user.getLikes())) {

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
        if (!userInBd.getFriendShips().containsAll(user.getFriendShips())) {

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

        // Запрос на изменение основных данных самого юзера
        jdbcTemplate.update(sqlQueryUpdate,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                java.sql.Date.valueOf(user.getBirthday()),
                userId);

        return Optional.of(user);
    }

    @Override
    public Collection<User> getUsers() {

        // Запрос на получение всех айдишников пользователей
        String sqlQuery = "SELECT U.USER_ID, U.EMAIL, U.LOGIN, U.NAME, U.BIRTHDAY, L.LIKE_ID, L.FILM_ID, FR.FRIEND_REQUEST_ID, FR.FRIEND_ID " +
                "FROM USERS AS U " +
                "LEFT JOIN LIKES AS L on U.USER_ID = L.USER_ID " +
                "LEFT JOIN FRIEND_REQUEST AS FR on U.USER_ID = FR.USER_ID ";

        int prevUserId;
        User user;
        FriendShip friendShip;
        Like like;
        Collection<User> users = new ArrayList<>();

        // Выполнение запроса
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sqlQuery);

        if (userRows.next()) {

            log.debug("Найден объект с id {}, и логином {}", userRows.getInt("USER_ID"), userRows.getString("LOGIN"));

            // Создаю объект пользователя из запроса
            user = createUser(userRows);

            // Отдельно создаю объекты для Like и FriendShip
            like = createLike(userRows);
            friendShip = createFriendRequest(userRows);

            // Если лайк нашёлся и создался нормально, то я добавляю его фильму от пользователя
            if (like != null) {
                user.getLikes().add(like);
            }

            // Если дружба нашлась и создалась нормально, то я добавляю её пользователю
            if (friendShip != null) {
                user.getFriendShips().add(friendShip);
            }

            // Если больше строк нет, то возвращаю то, что есть
            if (userRows.isLast()) {
                users.add(user);
                return users.stream().sorted(Comparator.comparingInt(User::getId)).collect(Collectors.toList());
            }

            // Если ещё есть какие-то строки,
            // То я запоминаю айди предыдущего фильма. Он нужен, чтобы мы могли различать, где строка с новым фильмом,
            // А где строка которая отличается только по лайкам и жанрам
            prevUserId = userRows.getInt("USER_ID");

            // Если больше 1 строки, то, либо это 1 пользователь и строки отличаются только по Friendship или Likes,
            // Либо это другой пользователь
            while (userRows.next()) {

                if (prevUserId != userRows.getInt("USER_ID")) {

                    // Добавляю пользователя в лист
                    users.add(user);

                    log.debug("Найден объект с id {}, и именем {}", userRows.getInt("USER_ID"), userRows.getString("NAME"));

                    // Создаю объект пользователя из запроса
                    user = createUser(userRows);

                    // Отдельно создаю объекты для Like и FriendShip
                    like = createLike(userRows);
                    friendShip = createFriendRequest(userRows);

                    // Если лайк нашёлся и создался нормально, то я добавляю его фильму от пользователя
                    if (like != null) {
                        user.getLikes().add(like);
                    }

                    // Если дружба нашлась и создалась нормально, то я добавляю её пользователю
                    if (friendShip != null) {
                        user.getFriendShips().add(friendShip);
                    }

                    prevUserId = userRows.getInt("FILM_ID");

                } else {

                    // Отдельно создаю объекты для Like и FriendShip
                    like = createLike(userRows);
                    friendShip = createFriendRequest(userRows);

                    // Если лайк нашёлся и создался нормально, то я добавляю его фильму от пользователя
                    if (like != null) {
                        user.getLikes().add(like);
                    }

                    // Если дружба нашлась и создалась нормально, то я добавляю её пользователю
                    if (friendShip != null) {
                        user.getFriendShips().add(friendShip);
                    }
                }
                // Если это была последняя строка, то следующей итерации не будет и надо добавлять сейчас
                if (userRows.isLast()) {
                    users.add(user);
                }
            }
        }
        return users.stream().sorted(Comparator.comparingInt(User::getId)).collect(Collectors.toList());
    }

    @Override
    public Optional<User> getUserById(int id) {

        // Запрос на получение пользователя по id
        String sqlQuery = "SELECT U.USER_ID, U.EMAIL, U.LOGIN, U.NAME, U.BIRTHDAY, L.LIKE_ID, L.FILM_ID, FR.FRIEND_REQUEST_ID, FR.FRIEND_ID " +
                "FROM USERS AS U " +
                "LEFT JOIN LIKES AS L on U.USER_ID = L.USER_ID " +
                "LEFT JOIN FRIEND_REQUEST AS FR on U.USER_ID = FR.USER_ID " +
                "WHERE U.USER_ID = ?";

        User user;
        FriendShip friendShip;
        Like like;

        // Выполнение запроса
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sqlQuery, id);

        // Прохожусь по первой строке результата запроса
        if (userRows.next()) {

            log.debug("Найден объект с id {}, и логином {}", userRows.getInt("USER_ID"), userRows.getString("LOGIN"));

            // Создаю объект пользователя из запроса
            user = createUser(userRows);

            // Отдельно создаю объекты для Like и FriendShip
            like = createLike(userRows);
            friendShip = createFriendRequest(userRows);

            // Если лайк нашёлся и создался нормально, то я добавляю его фильму от пользователя
            if (like != null) {
                user.getLikes().add(like);
            }

            // Если дружба нашлась и создалась нормально, то я добавляю её пользователю
            if (friendShip != null) {
                user.getFriendShips().add(friendShip);
            }

            // Если больше строк нет, то возвращаю то, что есть
            if (userRows.isLast()) {
                return Optional.of(user);
            }

            log.debug("В таблице больше 1 строки, так что начинаю заполнять объект user дополнительной информацией");

            // Если больше 1 строки, то они отличаются только по Like или FriendShip, так что меняю их
            while (userRows.next()) {

                // Отдельно создаю объекты для Like и FriendShip
                like = createLike(userRows);
                friendShip = createFriendRequest(userRows);

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

        // Запрос на проверку уже существования дружбы, если count = 1
        String sqlQueryCheckForFriendRequest = "SELECT COUNT(*) AS COUNT " +
                "FROM FRIEND_REQUEST " +
                "WHERE USER_ID = ? AND FRIEND_ID = ? ";


        // Мапа, которая содержит данные для вставки в таблицу users.
        // Ключ - название столбца, значение - значение
        HashMap<String, Object> values = new HashMap<>();
        values.put("USER_ID", userId);
        values.put("FRIEND_ID", friendId);

        // Выполнение запроса на проверку дружбы
        SqlRowSet sqlRowSetCheckForFriendRequest = jdbcTemplate.queryForRowSet(sqlQueryCheckForFriendRequest, userId, friendId);

        // Перехожу на следующую строку
        sqlRowSetCheckForFriendRequest.next();

        // Если count = 0, тогда они не друзья и я делаю их друзьями
        if (sqlRowSetCheckForFriendRequest.getInt("COUNT") == 0) {
            Number id = simpleJdbcInsertForFriendRequests.executeAndReturnKey(values);
            System.out.println(id);
        }

        return getUserById(userId);
    }

    @Override
    public Optional<User> deleteUserFriend(int userId, int friendId) {

        // Запрос на удаление дружбы
        String sqlQueryDeleteForFriendRequest = "DELETE " +
                "FROM FRIEND_REQUEST " +
                "WHERE USER_ID = ? AND FRIEND_ID = ? ";

        jdbcTemplate.update(sqlQueryDeleteForFriendRequest, userId, friendId);

        return getUserById(userId);
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

    private FriendShip createFriendRequest(SqlRowSet userRows) {
        if (userRows.getInt("FRIEND_REQUEST_ID") != 0) {
            return new FriendShip(userRows.getInt("USER_ID"),
                    userRows.getInt("FRIEND_ID"));
        }
        return null;
    }

    private Like createLike(SqlRowSet userRows) {
        if (userRows.getInt("LIKE_ID") != 0) {
            return new Like(userRows.getInt("LIKE_ID"),
                    userRows.getInt("FILM_ID"),
                    userRows.getInt("USER_ID"));
        }
        return null;
    }

    private User createUser(SqlRowSet userRows) {
        return new User(userRows.getInt("USER_ID"),
                userRows.getString("EMAIL"),
                userRows.getString("LOGIN"),
                userRows.getString("NAME"),
                new java.sql.Date(Objects.requireNonNull(userRows.getDate("BIRTHDAY")).getTime()).toLocalDate());
    }

}
