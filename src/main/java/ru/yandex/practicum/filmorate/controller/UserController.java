package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.InvalidIdentificatorException;
import ru.yandex.practicum.filmorate.exception.user.InvalidLoginException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.HashMap;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    HashMap<Integer, User> users = new HashMap<>();

    @PostMapping("/user")
    public HashMap<Integer, User> postUser(@Valid  @RequestBody User user) throws InvalidLoginException, InvalidIdentificatorException {
        if (!user.getLogin().contains(" ")){

            if (user.getName().isBlank()){
                user.setName(user.getLogin());
            }

            if (!users.containsKey(user.getId())) {
                users.put(user.getId(), user);
            } else {
                throw new InvalidIdentificatorException("This id already exists");
            }
        } else {
            throw new InvalidLoginException("Invalid login");
        }
        return users;
    }

    @PutMapping("/{oldId}")
    public HashMap<Integer, User> putUser(@Valid @RequestBody User user, @PathVariable int oldId) throws InvalidLoginException {
        if (!user.getLogin().contains(" ")) {

            if (user.getName().isBlank()){
                user.setName(user.getLogin());
            }

            users.remove(oldId);
            users.put(user.getId(), user);
        } else {
            throw new InvalidLoginException("Invalid login");
        }
        return users;
    }

    @GetMapping("/all")
    public HashMap<Integer, User> getUsers(){
        return users;
    }



}
