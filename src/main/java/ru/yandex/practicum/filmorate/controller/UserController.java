package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.user.EqualIdentifierException;
import ru.yandex.practicum.filmorate.exception.user.InvalidLoginException;
import ru.yandex.practicum.filmorate.exception.user.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping()
    public User postUser(@Valid @RequestBody User user) {

        if (user.getLogin().contains(" ")) {
            throw new InvalidLoginException("Invalid login");
        }

        return userService.postUser(user).get();
    }

    @PutMapping()
    public User putUser(@Valid @RequestBody User user) {

        if (user.getLogin().contains(" ")) {
            throw new InvalidLoginException("Invalid login");
        }

        Optional<User> optionalUser = userService.getUserById(user.getId());

        if (optionalUser.isEmpty()) {
            throw new UserNotFoundException("User not found");
        }

        return userService.putUser(user).get();
    }

    @GetMapping()
    public Collection<User> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable int id) {

        Optional<User> optionalUser = userService.getUserById(id);

        if (optionalUser.isEmpty()) {
            throw new UserNotFoundException("User not found");
        }

        return userService.getUserById(id).get();
    }

    @PutMapping("/{id}/friends/{friendId}")
    public User putUserFriend(@PathVariable int id, @PathVariable int friendId) {

        Optional<User> optionalUser = userService.getUserById(id);
        Optional<User> optionalFriend = userService.getUserById(friendId);

        if (optionalUser.isEmpty() || optionalFriend.isEmpty()) {
            throw new UserNotFoundException("One of the users not found");
        }

        if (id == friendId) {
            throw new EqualIdentifierException("Identifiers are equal");
        }

        return userService.putUserFriend(id, friendId).get();
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public User deleteUserFriend(@PathVariable int id, @PathVariable int friendId) {

        Optional<User> optionalUser = userService.getUserById(id);
        Optional<User> optionalFriend = userService.getUserById(friendId);

        if (optionalUser.isEmpty() || optionalFriend.isEmpty()) {
            throw new UserNotFoundException("One of the users not found");
        }

        if (id == friendId) {
            throw new EqualIdentifierException("Identifiers are equal");
        }

        return userService.deleteUserFriend(id, friendId).get();
    }

    @GetMapping("/{id}/friends")
    public Collection<User> getFriends(@PathVariable int id) {

        Optional<User> optionalUser = userService.getUserById(id);

        if (optionalUser.isEmpty()) {
            throw new UserNotFoundException("User not found");
        }

        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> getCommonFriends(@PathVariable int id, @PathVariable int otherId) {

        Optional<User> optionalUser = userService.getUserById(id);
        Optional<User> optionalOther = userService.getUserById(otherId);

        if (optionalUser.isEmpty() || optionalOther.isEmpty()) {
            throw new UserNotFoundException("One of the users not found");
        }

        if (id == otherId) {
            throw new EqualIdentifierException("Identifiers are equal");
        }

        return userService.getCommonFriends(id, otherId);
    }
}
