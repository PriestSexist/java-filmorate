package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Service
public class UserService {

    private final UserStorage userDbStorage;


    @Autowired
    public UserService(UserStorage userDbStorage) {   // !!!
        this.userDbStorage = userDbStorage;

    }

    public Optional<User> postUser(User user) {

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        return userDbStorage.postUser(user);
    }

    public Optional<User> putUser(User user) {

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        return userDbStorage.putUser(user);
    }

    public Collection<User> getUsers() {
        return userDbStorage.getUsers();
    }

    public Optional<User> getUserById(int id) {
        return userDbStorage.getUserById(id);
    }

    public Optional<User> putUserFriend(int id, int friendId) {
        return userDbStorage.putUserFriend(id, friendId);
    }

    public Optional<User> deleteUserFriend(int id, int friendId) {
        return userDbStorage.deleteUserFriend(id, friendId);
    }

    public Collection<User> getFriends(int id) {
        return userDbStorage.getFriends(id);
    }

    public Collection<User> getCommonFriends(int userId, int otherId) {
        return userDbStorage.getCommonFriends(userId, otherId);
    }

    public Collection<Film> getRecommendationFilms(int userId) {

        // мапа айди пользователя, мапа лайкнутых фильмов, где ключ - айди фильма, а значение - фильм
        HashMap<Integer, HashMap<Integer, Film>> usersAndFilms = userDbStorage.getRecommendationFilms(userId);

        // Айди пользователей, фильмы которых я буду рекомендовать
        Collection<Integer> idsUsersForRecommendation = new ArrayList<>();

        // Фильмы, которые я буду рекомендовать
        Collection<Film> filmsForRecommendation = new HashSet<>();

        // Лайкнутые фильмы пользователя, который сделал запрос.
        HashMap<Integer, Film> userFilms = usersAndFilms.get(userId);

        // Когда мы проходимся по пользователям, мы проверяем, на сколько они совпали с юзером
        int howManyMatched = 0;

        // Прохожусь по всем ключам пользователей
        for (Integer otherIds : usersAndFilms.keySet()) {
            // Фильмы проверяемого пользователя, которого мы проверяем
            HashMap<Integer, Film> films = new HashMap<>(usersAndFilms.get(otherIds));

            // Если подборка фильмов юзера равна подборке фильмов проверяемого пользователя
            if (films.keySet().equals(userFilms.keySet())) {
                continue;
            }

            // Оставляю только фильмы, которые есть у юзера
            films.values().retainAll(userFilms.values());

            // Если количество совпавших в прошлый раз равно количеству совпавших сейчас, то я просто добавляю
            if (films.size() == howManyMatched) {
                idsUsersForRecommendation.add(otherIds);
                continue;
            }

            // Если количество совпавших в прошлый раз больше количества совпавших сейчас,
            // то я меняю количество совпавших в прошлый раз, удаляю всех, кого я рекомендовал и заполняю заного
            if (films.size() > howManyMatched) {
                howManyMatched = films.size();
                idsUsersForRecommendation.clear();
                idsUsersForRecommendation.add(otherIds);
            }
        }

        // Я нашёл все нужные ID пользователей и добавляю их фильмы
        for (Integer id : idsUsersForRecommendation) {
            filmsForRecommendation.addAll(usersAndFilms.get(id).values());
        }
        // Удаляю все фильмы, которые лайкал пользователь, из результата
        if (userFilms != null) {
            filmsForRecommendation.removeAll(userFilms.values());
        }
        return filmsForRecommendation;
    }

    public void deleteUser(int userId) {
        userDbStorage.deleteUser(userId);
    }
}
