package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.InvalidIdentificatorException;
import ru.yandex.practicum.filmorate.exception.user.InvalidLoginException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private static final AtomicInteger count = new AtomicInteger(0);
    HashMap<Integer, User> users = new HashMap<>();

    @PostMapping()
    public User postUser(@Valid @RequestBody User user) throws InvalidLoginException {
        if (!user.getLogin().contains(" ")) {

            if (user.getName() == null || user.getName().isBlank()) {
                user.setName(user.getLogin());
            }
            user.setId(count.incrementAndGet());
            users.put(user.getId(), user);

        } else {
            throw new InvalidLoginException("Invalid login");
        }
        return user;
    }

    @PutMapping()
    public User putUser(@Valid @RequestBody User user) throws InvalidLoginException, InvalidIdentificatorException {
        if (!user.getLogin().contains(" ")) {

            if (user.getName().isBlank()) {
                user.setName(user.getLogin());
            }

            if (users.containsKey(user.getId())) {
                users.replace(user.getId(), user);
            } else {
                throw new InvalidIdentificatorException("Invalid id");
            }
        } else {
            throw new InvalidLoginException("Invalid login");
        }
        return user;
    }

    @GetMapping()
    public ArrayList<User> getUsers() {
        return new ArrayList<>(users.values());
    }



}
