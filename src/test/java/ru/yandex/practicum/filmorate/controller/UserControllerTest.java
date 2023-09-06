package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.FriendShip;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.event.dao.EventDbStorage;
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
class UserControllerTest {

    private final UserDbStorage userStorage;

    private final EventDbStorage eventDbStorage;

    private final UserService userService;


    @Test
    public void testPostUser() {
        User userForPost = new User(10, "vitekb650@gmaill.com", "PriestSexist", "Viktor", LocalDate.of(2002, 10, 22));
        Optional<User> userOptional = userStorage.postUser(userForPost);

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("id", 1))
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("email", "vitekb650@gmaill.com"))
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("login", "PriestSexist"))
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("name", "Viktor"))
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("birthday", LocalDate.of(2002, 10, 22)));
    }

    @Test
    public void testPutUser() {
        User userForPost = new User(1, "vitekb650@gmaill.com", "PriestSexist", "Viktor", LocalDate.of(2002, 10, 22));
        User userForPut = new User(1, "satori@gmaill.com", "Satori", "Stas", LocalDate.of(1989, 10, 24));

        userStorage.postUser(userForPost);

        Optional<User> userOptional = userStorage.putUser(userForPut);

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("id", 1))
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("email", "satori@gmaill.com"))
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("login", "Satori"))
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("name", "Stas"))
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("birthday", LocalDate.of(1989, 10, 24)));

    }

    @Test
    public void testGetUsers() {
        User user1 = new User(1, "vitekb650@gmaill.com", "PriestSexist", "Viktor", LocalDate.of(2002, 10, 22));
        User user2 = new User(2, "satori@gmaill.com", "Satori", "Stas", LocalDate.of(1989, 10, 24));

        userStorage.postUser(user1);
        userStorage.postUser(user2);

        List<User> users = (List<User>) userStorage.getUsers();

        Optional<User> userOptional1 = Optional.of(users.get(0));
        Optional<User> userOptional2 = Optional.of(users.get(1));

        assertThat(userOptional1)
                .isPresent()
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("id", 1))
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("email", "vitekb650@gmaill.com"))
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("login", "PriestSexist"))
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("name", "Viktor"))
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("birthday", LocalDate.of(2002, 10, 22)));


        assertThat(userOptional2)
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("id", 2))
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("email", "satori@gmaill.com"))
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("login", "Satori"))
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("name", "Stas"))
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("birthday", LocalDate.of(1989, 10, 24)));

    }

    @Test
    public void testGetUserById() {
        User userForPost = new User(10, "vitekb650@gmaill.com", "PriestSexist", "Viktor", LocalDate.of(2002, 10, 22));
        userStorage.postUser(userForPost);

        Optional<User> userOptional = userStorage.getUserById(1);

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("id", 1))
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("email", "vitekb650@gmaill.com"))
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("login", "PriestSexist"))
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("name", "Viktor"))
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("birthday", LocalDate.of(2002, 10, 22)));

    }

    @Test
    public void testPutUserFriend() {
        User userForPost = new User(1, "vitekb650@gmaill.com", "PriestSexist", "Viktor", LocalDate.of(2002, 10, 22));
        User friend = new User(2, "satori@gmaill.com", "Satori", "Stas", LocalDate.of(1989, 10, 24));
        FriendShip friendShip = new FriendShip(0, userForPost.getId(), friend.getId());
        HashSet<FriendShip> friendShips = new HashSet<>();
        friendShips.add(friendShip);

        userStorage.postUser(userForPost);
        userStorage.postUser(friend);

        Optional<User> userOptional = userStorage.putUserFriend(userForPost.getId(), friend.getId());

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("id", 1))
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("email", "vitekb650@gmaill.com"))
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("login", "PriestSexist"))
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("name", "Viktor"))
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("birthday", LocalDate.of(2002, 10, 22)))
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("friendShips", friendShips));

    }

    @Test
    public void testDeleteUserFriend() {
        User userForPost = new User(1, "vitekb650@gmaill.com", "PriestSexist", "Viktor", LocalDate.of(2002, 10, 22));
        User friend = new User(2, "satori@gmaill.com", "Satori", "Stas", LocalDate.of(1989, 10, 24));
        HashSet<FriendShip> friendShips = new HashSet<>();

        userStorage.postUser(userForPost);
        userStorage.postUser(friend);
        userStorage.putUserFriend(userForPost.getId(), friend.getId());

        Optional<User> userOptional = userStorage.deleteUserFriend(userForPost.getId(), friend.getId());

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("id", 1))
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("email", "vitekb650@gmaill.com"))
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("login", "PriestSexist"))
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("name", "Viktor"))
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("birthday", LocalDate.of(2002, 10, 22)))
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("friendShips", friendShips));

    }

    @Test
    public void testGetFriends() {
        User userForPost = new User(1, "vitekb650@gmaill.com", "PriestSexist", "Viktor", LocalDate.of(2002, 10, 22));
        User friend1 = new User(2, "satori@gmaill.com", "Satori", "Stas", LocalDate.of(1989, 10, 24));
        User friend2 = new User(3, "Uretc@gmaill.com", "Uretc", "Uriy", LocalDate.of(2000, 9, 18));

        userStorage.postUser(userForPost);
        userStorage.postUser(friend1);
        userStorage.postUser(friend2);
        userStorage.putUserFriend(userForPost.getId(), friend1.getId());
        userStorage.putUserFriend(userForPost.getId(), friend2.getId());

        List<User> friends = (List<User>) userStorage.getFriends(userForPost.getId());

        Optional<User> userOptional1 = Optional.of(friends.get(0));
        Optional<User> userOptional2 = Optional.of(friends.get(1));

        assertThat(userOptional1)
                .isPresent()
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("id", 2))
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("email", "satori@gmaill.com"))
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("login", "Satori"))
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("name", "Stas"))
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("birthday", LocalDate.of(1989, 10, 24)));

        assertThat(userOptional2)
                .isPresent()
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("id", 3))
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("email", "Uretc@gmaill.com"))
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("login", "Uretc"))
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("name", "Uriy"))
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("birthday", LocalDate.of(2000, 9, 18)));

    }

    @Test
    public void testGetCommonFriends() {
        User userForPost1 = new User(1, "vitekb650@gmaill.com", "PriestSexist", "Viktor", LocalDate.of(2002, 10, 22));
        User userForPost2 = new User(2, "satori@gmaill.com", "Satori", "Stas", LocalDate.of(1989, 10, 24));
        User commonFriend = new User(3, "Uretc@gmaill.com", "Uretc", "Uriy", LocalDate.of(2000, 9, 18));

        userStorage.postUser(userForPost1);
        userStorage.postUser(userForPost2);
        userStorage.postUser(commonFriend);
        userStorage.putUserFriend(userForPost1.getId(), commonFriend.getId());
        userStorage.putUserFriend(userForPost2.getId(), commonFriend.getId());

        HashSet<User> friends = (HashSet<User>) userStorage.getCommonFriends(userForPost1.getId(), userForPost2.getId());

        Optional<User> userOptional = friends.stream().findFirst();

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("id", 3))
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("email", "Uretc@gmaill.com"))
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("login", "Uretc"))
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("name", "Uriy"))
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("birthday", LocalDate.of(2000, 9, 18)));

    }

    @Test
    public void testGetFeedUser() {
        User userForPost1 = new User(1, "vitekb650@gmaill.com", "PriestSexist", "Viktor", LocalDate.of(2002, 10, 22));
        User friend1 = new User(2, "satori@gmaill.com", "Satori", "Stas", LocalDate.of(1989, 10, 24));

        userService.postUser(userForPost1);
        userService.postUser(friend1);
        userService.putUserFriend(userForPost1.getId(), friend1.getId());
        userService.deleteUserFriend(userForPost1.getId(), friend1.getId());

        List<Event> feed = eventDbStorage.getFeed(userForPost1.getId());

        Assertions.assertEquals(feed.size(), 2);
    }
}
