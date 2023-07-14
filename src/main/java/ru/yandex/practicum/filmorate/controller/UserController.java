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
import java.util.List;

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

        return userService.postUser(user);
    }

    @PutMapping()
    public User putUser(@Valid @RequestBody User user) {

        if (user.getLogin().contains(" ")) {
            throw new InvalidLoginException("Invalid login");
        }

        if (!userService.getUsers().containsKey(user.getId())) {
            throw new UserNotFoundException("User not found");
        }

        return userService.putUser(user);
    }

    @GetMapping()
    public Collection<User> getUsers() {
        return userService.getUsers().values();
    }

    @GetMapping("/{id}")
    public User getUsersById(@PathVariable int id) {
        if (!userService.getUsers().containsKey(id)) {
            throw new UserNotFoundException("User not found");
        }
        return userService.getUserById(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public User putUserFriend(@PathVariable int id, @PathVariable int friendId) {
        if (!userService.getUsers().containsKey(id) || !userService.getUsers().containsKey(friendId)) {
            throw new UserNotFoundException("One of the users not found");
        }
        if (id == friendId) {
            throw new EqualIdentifierException("Identifiers are equal");
        }

        return userService.putUserFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public User deleteUserFriend(@PathVariable int id, @PathVariable int friendId) {
        if (!userService.getUsers().containsKey(id) || !userService.getUsers().containsKey(friendId)) {
            throw new UserNotFoundException("One of the users not found");
        }
        if (id == friendId) {
            throw new EqualIdentifierException("Identifiers are equal");
        }

        return userService.deleteUserFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable int id) {
        if (!userService.getUsers().containsKey(id)) {
            throw new UserNotFoundException("User not found");
        }
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable int id, @PathVariable int otherId) {
        if (!userService.getUsers().containsKey(id) || !userService.getUsers().containsKey(otherId)) {
            throw new UserNotFoundException("One of the users not found");
        }
        if (id == otherId) {
            throw new EqualIdentifierException("Identifiers are equal");
        }

        return userService.getCommonFriends(id, otherId);
    }
}
