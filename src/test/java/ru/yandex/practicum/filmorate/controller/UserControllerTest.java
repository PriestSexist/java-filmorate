package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.yandex.practicum.filmorate.exception.user.EqualIdentifierException;
import ru.yandex.practicum.filmorate.exception.user.InvalidLoginException;
import ru.yandex.practicum.filmorate.exception.user.UserNotFoundException;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserController controller;

    @Autowired
    UserService userService;

    @Autowired
    UserStorage userStorage;

    @AfterEach
    public void reseter() {
        userStorage.clear();
    }

    @Test
    public void shouldReturnAllUsers() throws Exception {

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"vitekb650@gmail.com\", \"login\": \"PriestSexist\",\"name\": \"PriestSexist\",\"birthday\": \"2002-10-22\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"email\":\"vitekb650@gmail.com\",\"login\":\"PriestSexist\",\"name\":\"PriestSexist\",\"birthday\":\"2002-10-22\",\"friends\":[]}"));

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"satori0@gmail.com\", \"login\": \"Satori\",\"name\": \"Satori\",\"birthday\": \"1989-10-24\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":2,\"email\":\"satori0@gmail.com\",\"login\":\"Satori\",\"name\":\"Satori\",\"birthday\":\"1989-10-24\",\"friends\":[]}"));

        this.mockMvc.perform(get("http://localhost:8081/users"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("[{\"id\":1,\"email\":\"vitekb650@gmail.com\",\"login\":\"PriestSexist\",\"name\":\"PriestSexist\",\"birthday\":\"2002-10-22\",\"friends\":[]},{\"id\":2,\"email\":\"satori0@gmail.com\",\"login\":\"Satori\",\"name\":\"Satori\",\"birthday\":\"1989-10-24\",\"friends\":[]}]"));

    }

    @Test
    public void shouldPostUser() throws Exception {
        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"vitekb650@gmail.com\", \"login\": \"PriestSexist\",\"name\": \"PriestSexist\",\"birthday\": \"2002-10-22\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"email\":\"vitekb650@gmail.com\",\"login\":\"PriestSexist\",\"name\":\"PriestSexist\",\"birthday\":\"2002-10-22\",\"friends\":[]}"));
    }

    @Test
    public void shouldPostUserWithBlankName() throws Exception {
        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"vitekb650@gmail.com\", \"login\": \"PriestSexist\",\"name\": \" \",\"birthday\": \"2002-10-22\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"email\":\"vitekb650@gmail.com\",\"login\":\"PriestSexist\",\"name\":\"PriestSexist\",\"birthday\":\"2002-10-22\",\"friends\":[]}"));
    }

    @Test
    public void shouldNotPostUserWithInvalidEmail() throws Exception {
        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"vit@ekb650g@mail.com\", \"login\": \"PriestSexist\",\"name\": \"PriestSexist\",\"birthday\": \"2002-10-22\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException));
    }

    @Test
    public void shouldNotPostUserWithBlankEmail() throws Exception {
        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \" \", \"login\": \"PriestSexist\",\"name\": \"PriestSexist\",\"birthday\": \"2002-10-22\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException));
    }

    @Test
    public void shouldNotPostUserWithBlankLogin() throws Exception {
        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"vitekb650@gmail.com\", \"login\": \" \",\"name\": \"PriestSexist\",\"birthday\": \"2002-10-22\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException));
    }

    @Test
    public void shouldNotPostUserWithBirthdayFromFuture() throws Exception {
        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"vitekb650@gmail.com\", \"login\": \"PriestSexist\",\"name\": \"PriestSexist\",\"birthday\": \"2025-10-22\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException));
    }

    @Test
    public void shouldNotPostUserWithLoginWithSpaces() throws Exception {
        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"vitekb650@gmail.com\", \"login\": \"Priest Sexist\",\"name\": \"PriestSexist\",\"birthday\": \"2002-10-22\"}"))
                .andDo(print())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof InvalidLoginException));
    }

    @Test
    public void shouldPutUser() throws Exception {

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"vitekb650@gmail.com\", \"login\": \"PriestSexist\",\"name\": \"PriestSexist\",\"birthday\": \"2002-10-22\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"email\":\"vitekb650@gmail.com\",\"login\":\"PriestSexist\",\"name\":\"PriestSexist\",\"birthday\":\"2002-10-22\",\"friends\":[]}"));

        this.mockMvc.perform(put("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\", \"email\": \"vitekb650@gmail.com\", \"login\": \"PriestSexist\",\"name\": \"PriestSexist\",\"birthday\": \"2002-10-22\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"email\":\"vitekb650@gmail.com\",\"login\":\"PriestSexist\",\"name\":\"PriestSexist\",\"birthday\":\"2002-10-22\",\"friends\":[]}"));

    }

    @Test
    public void shouldNotPutUserWithInvalidEmail() throws Exception {

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"vitekb650@gmail.com\", \"login\": \"PriestSexist\",\"name\": \"PriestSexist\",\"birthday\": \"2002-10-22\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"email\":\"vitekb650@gmail.com\",\"login\":\"PriestSexist\",\"name\":\"PriestSexist\",\"birthday\":\"2002-10-22\",\"friends\":[]}"));

        this.mockMvc.perform(put("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\", \"email\": \"vit@ekb650g@mail.com\", \"login\": \"PriestSexist\",\"name\": \"PriestSexist\",\"birthday\": \"2002-10-22\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException));

    }

    @Test
    public void shouldNotPutUserWithBlankEmail() throws Exception {

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"vitekb650@gmail.com\", \"login\": \"PriestSexist\",\"name\": \"PriestSexist\",\"birthday\": \"2002-10-22\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"email\":\"vitekb650@gmail.com\",\"login\":\"PriestSexist\",\"name\":\"PriestSexist\",\"birthday\":\"2002-10-22\",\"friends\":[]}"));

        this.mockMvc.perform(put("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\", \"email\": \" \", \"login\": \"PriestSexist\",\"name\": \"PriestSexist\",\"birthday\": \"2002-10-22\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException));

    }

    @Test
    public void shouldNotPutUserWithBlankLogin() throws Exception {

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"vitekb650@gmail.com\", \"login\": \"PriestSexist\",\"name\": \"PriestSexist\",\"birthday\": \"2002-10-22\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"email\":\"vitekb650@gmail.com\",\"login\":\"PriestSexist\",\"name\":\"PriestSexist\",\"birthday\":\"2002-10-22\",\"friends\":[]}"));

        this.mockMvc.perform(put("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\", \"email\": \"vitekb650@gmail.com\", \"login\": \" \",\"name\": \"PriestSexist\",\"birthday\": \"2002-10-22\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException));

    }

    @Test
    public void shouldNotPutUserWithBirthdayFromFuture() throws Exception {

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"vitekb650@gmail.com\", \"login\": \"PriestSexist\",\"name\": \"PriestSexist\",\"birthday\": \"2002-10-22\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"email\":\"vitekb650@gmail.com\",\"login\":\"PriestSexist\",\"name\":\"PriestSexist\",\"birthday\":\"2002-10-22\",\"friends\":[]}"));

        this.mockMvc.perform(put("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\", \"email\": \"vitekb650@gmail.com\", \"login\": \"PriestSexist\",\"name\": \"PriestSexist\",\"birthday\": \"2025-10-22\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException));

    }

    @Test
    public void shouldNotPutUserWithLoginWithSpaces() throws Exception {

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"vitekb650@gmail.com\", \"login\": \"PriestSexist\",\"name\": \"PriestSexist\",\"birthday\": \"2002-10-22\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"email\":\"vitekb650@gmail.com\",\"login\":\"PriestSexist\",\"name\":\"PriestSexist\",\"birthday\":\"2002-10-22\",\"friends\":[]}"));

        this.mockMvc.perform(put("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\", \"email\": \"vitekb650@gmail.com\", \"login\": \"Priest Sexist\",\"name\": \"PriestSexist\",\"birthday\": \"2002-10-22\"}"))
                .andDo(print())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof InvalidLoginException));

    }

    @Test
    public void shouldGetUserById() throws Exception {

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"vitekb650@gmail.com\", \"login\": \"PriestSexist\",\"name\": \"PriestSexist\",\"birthday\": \"2002-10-22\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"email\":\"vitekb650@gmail.com\",\"login\":\"PriestSexist\",\"name\":\"PriestSexist\",\"birthday\":\"2002-10-22\",\"friends\":[]}"));

        this.mockMvc.perform(get("http://localhost:8081/users/1"))
                .andDo(print())
                .andExpect(content().string("{\"id\":1,\"email\":\"vitekb650@gmail.com\",\"login\":\"PriestSexist\",\"name\":\"PriestSexist\",\"birthday\":\"2002-10-22\",\"friends\":[]}"));

    }

    @Test
    public void shouldNotGetUserByInvalidId() throws Exception {

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"vitekb650@gmail.com\", \"login\": \"PriestSexist\",\"name\": \"PriestSexist\",\"birthday\": \"2002-10-22\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"email\":\"vitekb650@gmail.com\",\"login\":\"PriestSexist\",\"name\":\"PriestSexist\",\"birthday\":\"2002-10-22\",\"friends\":[]}"));

        this.mockMvc.perform(get("http://localhost:8081/users/-1"))
                .andDo(print())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserNotFoundException));

    }

    @Test
    public void shouldMake2UsersFriends() throws Exception {

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"vitekb650@gmail.com\", \"login\": \"PriestSexist\",\"name\": \"PriestSexist\",\"birthday\": \"2002-10-22\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"email\":\"vitekb650@gmail.com\",\"login\":\"PriestSexist\",\"name\":\"PriestSexist\",\"birthday\":\"2002-10-22\",\"friends\":[]}"));

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"satori0@gmail.com\", \"login\": \"Satori\",\"name\": \"Satori\",\"birthday\": \"1989-10-24\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":2,\"email\":\"satori0@gmail.com\",\"login\":\"Satori\",\"name\":\"Satori\",\"birthday\":\"1989-10-24\",\"friends\":[]}"));

        this.mockMvc.perform(put("http://localhost:8081/users/1/friends/2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"email\":\"vitekb650@gmail.com\",\"login\":\"PriestSexist\",\"name\":\"PriestSexist\",\"birthday\":\"2002-10-22\",\"friends\":[2]}"));

        assertEquals(1, userService.getUserById(2).getFriends().size());
        assertTrue(userService.getUserById(2).getFriends().contains(1));
    }

    @Test
    public void shouldNotMake2UsersFriendsWithInvalidUserId() throws Exception {

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"vitekb650@gmail.com\", \"login\": \"PriestSexist\",\"name\": \"PriestSexist\",\"birthday\": \"2002-10-22\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"email\":\"vitekb650@gmail.com\",\"login\":\"PriestSexist\",\"name\":\"PriestSexist\",\"birthday\":\"2002-10-22\",\"friends\":[]}"));

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"satori0@gmail.com\", \"login\": \"Satori\",\"name\": \"Satori\",\"birthday\": \"1989-10-24\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":2,\"email\":\"satori0@gmail.com\",\"login\":\"Satori\",\"name\":\"Satori\",\"birthday\":\"1989-10-24\",\"friends\":[]}"));

        this.mockMvc.perform(put("http://localhost:8081/users/-1/friends/2"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserNotFoundException));

    }

    @Test
    public void shouldNotMake2UsersFriendsWithInvalidFriendId() throws Exception {

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"vitekb650@gmail.com\", \"login\": \"PriestSexist\",\"name\": \"PriestSexist\",\"birthday\": \"2002-10-22\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"email\":\"vitekb650@gmail.com\",\"login\":\"PriestSexist\",\"name\":\"PriestSexist\",\"birthday\":\"2002-10-22\",\"friends\":[]}"));

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"satori0@gmail.com\", \"login\": \"Satori\",\"name\": \"Satori\",\"birthday\": \"1989-10-24\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":2,\"email\":\"satori0@gmail.com\",\"login\":\"Satori\",\"name\":\"Satori\",\"birthday\":\"1989-10-24\",\"friends\":[]}"));

        this.mockMvc.perform(put("http://localhost:8081/users/1/friends/-2"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserNotFoundException));

    }

    @Test
    public void shouldNotMake2UsersFriendsWithEqualId() throws Exception {

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"vitekb650@gmail.com\", \"login\": \"PriestSexist\",\"name\": \"PriestSexist\",\"birthday\": \"2002-10-22\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"email\":\"vitekb650@gmail.com\",\"login\":\"PriestSexist\",\"name\":\"PriestSexist\",\"birthday\":\"2002-10-22\",\"friends\":[]}"));

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"satori0@gmail.com\", \"login\": \"Satori\",\"name\": \"Satori\",\"birthday\": \"1989-10-24\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":2,\"email\":\"satori0@gmail.com\",\"login\":\"Satori\",\"name\":\"Satori\",\"birthday\":\"1989-10-24\",\"friends\":[]}"));

        this.mockMvc.perform(put("http://localhost:8081/users/1/friends/1"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof EqualIdentifierException));

    }

    @Test
    public void shouldUnmake2UsersFriends() throws Exception {

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"vitekb650@gmail.com\", \"login\": \"PriestSexist\",\"name\": \"PriestSexist\",\"birthday\": \"2002-10-22\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"email\":\"vitekb650@gmail.com\",\"login\":\"PriestSexist\",\"name\":\"PriestSexist\",\"birthday\":\"2002-10-22\",\"friends\":[]}"));

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"satori0@gmail.com\", \"login\": \"Satori\",\"name\": \"Satori\",\"birthday\": \"1989-10-24\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":2,\"email\":\"satori0@gmail.com\",\"login\":\"Satori\",\"name\":\"Satori\",\"birthday\":\"1989-10-24\",\"friends\":[]}"));

        this.mockMvc.perform(put("http://localhost:8081/users/1/friends/2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"email\":\"vitekb650@gmail.com\",\"login\":\"PriestSexist\",\"name\":\"PriestSexist\",\"birthday\":\"2002-10-22\",\"friends\":[2]}"));

        assertEquals(1, userService.getUserById(2).getFriends().size());
        assertTrue(userService.getUserById(2).getFriends().contains(1));

        this.mockMvc.perform(delete("http://localhost:8081/users/1/friends/2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"email\":\"vitekb650@gmail.com\",\"login\":\"PriestSexist\",\"name\":\"PriestSexist\",\"birthday\":\"2002-10-22\",\"friends\":[]}"));

        assertEquals(0, userService.getUserById(2).getFriends().size());

    }

    @Test
    public void shouldNotUnmake2UsersFriendsWithInvalidUserId() throws Exception {

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"vitekb650@gmail.com\", \"login\": \"PriestSexist\",\"name\": \"PriestSexist\",\"birthday\": \"2002-10-22\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"email\":\"vitekb650@gmail.com\",\"login\":\"PriestSexist\",\"name\":\"PriestSexist\",\"birthday\":\"2002-10-22\",\"friends\":[]}"));

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"satori0@gmail.com\", \"login\": \"Satori\",\"name\": \"Satori\",\"birthday\": \"1989-10-24\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":2,\"email\":\"satori0@gmail.com\",\"login\":\"Satori\",\"name\":\"Satori\",\"birthday\":\"1989-10-24\",\"friends\":[]}"));

        this.mockMvc.perform(put("http://localhost:8081/users/1/friends/2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"email\":\"vitekb650@gmail.com\",\"login\":\"PriestSexist\",\"name\":\"PriestSexist\",\"birthday\":\"2002-10-22\",\"friends\":[2]}"));

        assertEquals(1, userService.getUserById(2).getFriends().size());
        assertTrue(userService.getUserById(2).getFriends().contains(1));

        this.mockMvc.perform(delete("http://localhost:8081/users/-1/friends/2"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserNotFoundException));

        assertEquals(1, userService.getUserById(2).getFriends().size());
        assertTrue(userService.getUserById(2).getFriends().contains(1));

    }

    @Test
    public void shouldNotUnmake2UsersFriendsWithInvalidFriendId() throws Exception {

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"vitekb650@gmail.com\", \"login\": \"PriestSexist\",\"name\": \"PriestSexist\",\"birthday\": \"2002-10-22\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"email\":\"vitekb650@gmail.com\",\"login\":\"PriestSexist\",\"name\":\"PriestSexist\",\"birthday\":\"2002-10-22\",\"friends\":[]}"));

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"satori0@gmail.com\", \"login\": \"Satori\",\"name\": \"Satori\",\"birthday\": \"1989-10-24\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":2,\"email\":\"satori0@gmail.com\",\"login\":\"Satori\",\"name\":\"Satori\",\"birthday\":\"1989-10-24\",\"friends\":[]}"));

        this.mockMvc.perform(put("http://localhost:8081/users/1/friends/2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"email\":\"vitekb650@gmail.com\",\"login\":\"PriestSexist\",\"name\":\"PriestSexist\",\"birthday\":\"2002-10-22\",\"friends\":[2]}"));

        assertEquals(1, userService.getUserById(2).getFriends().size());
        assertTrue(userService.getUserById(2).getFriends().contains(1));

        this.mockMvc.perform(delete("http://localhost:8081/users/1/friends/-2"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserNotFoundException));

        assertEquals(1, userService.getUserById(2).getFriends().size());
        assertTrue(userService.getUserById(2).getFriends().contains(1));

    }

    @Test
    public void shouldNotUnmake2UsersFriendsWithEqualId() throws Exception {

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"vitekb650@gmail.com\", \"login\": \"PriestSexist\",\"name\": \"PriestSexist\",\"birthday\": \"2002-10-22\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"email\":\"vitekb650@gmail.com\",\"login\":\"PriestSexist\",\"name\":\"PriestSexist\",\"birthday\":\"2002-10-22\",\"friends\":[]}"));

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"satori0@gmail.com\", \"login\": \"Satori\",\"name\": \"Satori\",\"birthday\": \"1989-10-24\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":2,\"email\":\"satori0@gmail.com\",\"login\":\"Satori\",\"name\":\"Satori\",\"birthday\":\"1989-10-24\",\"friends\":[]}"));

        this.mockMvc.perform(put("http://localhost:8081/users/1/friends/2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"email\":\"vitekb650@gmail.com\",\"login\":\"PriestSexist\",\"name\":\"PriestSexist\",\"birthday\":\"2002-10-22\",\"friends\":[2]}"));

        assertEquals(1, userService.getUserById(2).getFriends().size());
        assertTrue(userService.getUserById(2).getFriends().contains(1));

        this.mockMvc.perform(put("http://localhost:8081/users/1/friends/1"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof EqualIdentifierException));

        assertEquals(1, userService.getUserById(2).getFriends().size());
        assertTrue(userService.getUserById(2).getFriends().contains(1));

    }

    @Test
    public void shouldGetAllFriends() throws Exception {

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"vitekb650@gmail.com\", \"login\": \"PriestSexist\",\"name\": \"PriestSexist\",\"birthday\": \"2002-10-22\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"email\":\"vitekb650@gmail.com\",\"login\":\"PriestSexist\",\"name\":\"PriestSexist\",\"birthday\":\"2002-10-22\",\"friends\":[]}"));

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"satori0@gmail.com\", \"login\": \"Satori\",\"name\": \"Satori\",\"birthday\": \"1989-10-24\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":2,\"email\":\"satori0@gmail.com\",\"login\":\"Satori\",\"name\":\"Satori\",\"birthday\":\"1989-10-24\",\"friends\":[]}"));

        this.mockMvc.perform(put("http://localhost:8081/users/1/friends/2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"email\":\"vitekb650@gmail.com\",\"login\":\"PriestSexist\",\"name\":\"PriestSexist\",\"birthday\":\"2002-10-22\",\"friends\":[2]}"));

        assertEquals(1, userService.getUserById(2).getFriends().size());
        assertTrue(userService.getUserById(2).getFriends().contains(1));

        this.mockMvc.perform(get("http://localhost:8081/users/1/friends"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("[{\"id\":2,\"email\":\"satori0@gmail.com\",\"login\":\"Satori\",\"name\":\"Satori\",\"birthday\":\"1989-10-24\",\"friends\":[1]}]"));

    }

    @Test
    public void shouldNotGetAllFriendsWithInvalidId() throws Exception {

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"vitekb650@gmail.com\", \"login\": \"PriestSexist\",\"name\": \"PriestSexist\",\"birthday\": \"2002-10-22\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"email\":\"vitekb650@gmail.com\",\"login\":\"PriestSexist\",\"name\":\"PriestSexist\",\"birthday\":\"2002-10-22\",\"friends\":[]}"));

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"satori0@gmail.com\", \"login\": \"Satori\",\"name\": \"Satori\",\"birthday\": \"1989-10-24\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":2,\"email\":\"satori0@gmail.com\",\"login\":\"Satori\",\"name\":\"Satori\",\"birthday\":\"1989-10-24\",\"friends\":[]}"));

        this.mockMvc.perform(put("http://localhost:8081/users/1/friends/2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"email\":\"vitekb650@gmail.com\",\"login\":\"PriestSexist\",\"name\":\"PriestSexist\",\"birthday\":\"2002-10-22\",\"friends\":[2]}"));

        assertEquals(1, userService.getUserById(2).getFriends().size());
        assertTrue(userService.getUserById(2).getFriends().contains(1));

        this.mockMvc.perform(get("http://localhost:8081/users/-1/friends"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserNotFoundException));
    }

    @Test
    public void shouldGetCommonFriends() throws Exception {

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"vitekb650@gmail.com\", \"login\": \"PriestSexist\",\"name\": \"PriestSexist\",\"birthday\": \"2002-10-22\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"email\":\"vitekb650@gmail.com\",\"login\":\"PriestSexist\",\"name\":\"PriestSexist\",\"birthday\":\"2002-10-22\",\"friends\":[]}"));

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"satori0@gmail.com\", \"login\": \"Satori\",\"name\": \"Satori\",\"birthday\": \"1989-10-24\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":2,\"email\":\"satori0@gmail.com\",\"login\":\"Satori\",\"name\":\"Satori\",\"birthday\":\"1989-10-24\",\"friends\":[]}"));

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"uralov@gmail.com\", \"login\": \"Uralov\",\"name\": \"Semen\",\"birthday\": \"1986-01-16\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":3,\"email\":\"uralov@gmail.com\",\"login\":\"Uralov\",\"name\":\"Semen\",\"birthday\":\"1986-01-16\",\"friends\":[]}"));

        this.mockMvc.perform(put("http://localhost:8081/users/1/friends/3"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"email\":\"vitekb650@gmail.com\",\"login\":\"PriestSexist\",\"name\":\"PriestSexist\",\"birthday\":\"2002-10-22\",\"friends\":[3]}"));

        assertEquals(1, userService.getUserById(3).getFriends().size());
        assertTrue(userService.getUserById(3).getFriends().contains(1));

        this.mockMvc.perform(put("http://localhost:8081/users/2/friends/3"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":2,\"email\":\"satori0@gmail.com\",\"login\":\"Satori\",\"name\":\"Satori\",\"birthday\":\"1989-10-24\",\"friends\":[3]}"));

        assertEquals(2, userService.getUserById(3).getFriends().size());
        assertTrue(userService.getUserById(3).getFriends().contains(1));
        assertTrue(userService.getUserById(3).getFriends().contains(2));

        this.mockMvc.perform((get("http://localhost:8081/users/1/friends/common/2")))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("[{\"id\":3,\"email\":\"uralov@gmail.com\",\"login\":\"Uralov\",\"name\":\"Semen\",\"birthday\":\"1986-01-16\",\"friends\":[1,2]}]"));

    }

    @Test
    public void shouldNotGetCommonFriendsWithInvalidUserId() throws Exception {

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"vitekb650@gmail.com\", \"login\": \"PriestSexist\",\"name\": \"PriestSexist\",\"birthday\": \"2002-10-22\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"email\":\"vitekb650@gmail.com\",\"login\":\"PriestSexist\",\"name\":\"PriestSexist\",\"birthday\":\"2002-10-22\",\"friends\":[]}"));

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"satori0@gmail.com\", \"login\": \"Satori\",\"name\": \"Satori\",\"birthday\": \"1989-10-24\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":2,\"email\":\"satori0@gmail.com\",\"login\":\"Satori\",\"name\":\"Satori\",\"birthday\":\"1989-10-24\",\"friends\":[]}"));

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"uralov@gmail.com\", \"login\": \"Uralov\",\"name\": \"Semen\",\"birthday\": \"1986-01-16\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":3,\"email\":\"uralov@gmail.com\",\"login\":\"Uralov\",\"name\":\"Semen\",\"birthday\":\"1986-01-16\",\"friends\":[]}"));

        this.mockMvc.perform(put("http://localhost:8081/users/1/friends/3"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"email\":\"vitekb650@gmail.com\",\"login\":\"PriestSexist\",\"name\":\"PriestSexist\",\"birthday\":\"2002-10-22\",\"friends\":[3]}"));

        assertEquals(1, userService.getUserById(3).getFriends().size());
        assertTrue(userService.getUserById(3).getFriends().contains(1));

        this.mockMvc.perform(put("http://localhost:8081/users/2/friends/3"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":2,\"email\":\"satori0@gmail.com\",\"login\":\"Satori\",\"name\":\"Satori\",\"birthday\":\"1989-10-24\",\"friends\":[3]}"));

        assertEquals(2, userService.getUserById(3).getFriends().size());
        assertTrue(userService.getUserById(3).getFriends().contains(1));
        assertTrue(userService.getUserById(3).getFriends().contains(2));

        this.mockMvc.perform((get("http://localhost:8081/users/-1/friends/common/2")))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserNotFoundException));


    }

    @Test
    public void shouldNotGetCommonFriendsWithInvalidOtherId() throws Exception {

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"vitekb650@gmail.com\", \"login\": \"PriestSexist\",\"name\": \"PriestSexist\",\"birthday\": \"2002-10-22\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"email\":\"vitekb650@gmail.com\",\"login\":\"PriestSexist\",\"name\":\"PriestSexist\",\"birthday\":\"2002-10-22\",\"friends\":[]}"));

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"satori0@gmail.com\", \"login\": \"Satori\",\"name\": \"Satori\",\"birthday\": \"1989-10-24\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":2,\"email\":\"satori0@gmail.com\",\"login\":\"Satori\",\"name\":\"Satori\",\"birthday\":\"1989-10-24\",\"friends\":[]}"));

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"uralov@gmail.com\", \"login\": \"Uralov\",\"name\": \"Semen\",\"birthday\": \"1986-01-16\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":3,\"email\":\"uralov@gmail.com\",\"login\":\"Uralov\",\"name\":\"Semen\",\"birthday\":\"1986-01-16\",\"friends\":[]}"));

        this.mockMvc.perform(put("http://localhost:8081/users/1/friends/3"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"email\":\"vitekb650@gmail.com\",\"login\":\"PriestSexist\",\"name\":\"PriestSexist\",\"birthday\":\"2002-10-22\",\"friends\":[3]}"));

        assertEquals(1, userService.getUserById(3).getFriends().size());
        assertTrue(userService.getUserById(3).getFriends().contains(1));

        this.mockMvc.perform(put("http://localhost:8081/users/2/friends/3"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":2,\"email\":\"satori0@gmail.com\",\"login\":\"Satori\",\"name\":\"Satori\",\"birthday\":\"1989-10-24\",\"friends\":[3]}"));

        assertEquals(2, userService.getUserById(3).getFriends().size());
        assertTrue(userService.getUserById(3).getFriends().contains(1));
        assertTrue(userService.getUserById(3).getFriends().contains(2));

        this.mockMvc.perform((get("http://localhost:8081/users/1/friends/common/-2")))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserNotFoundException));

    }

    @Test
    public void shouldNotGetcommonFriendsWithEqualId() throws Exception {

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"vitekb650@gmail.com\", \"login\": \"PriestSexist\",\"name\": \"PriestSexist\",\"birthday\": \"2002-10-22\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"email\":\"vitekb650@gmail.com\",\"login\":\"PriestSexist\",\"name\":\"PriestSexist\",\"birthday\":\"2002-10-22\",\"friends\":[]}"));

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"satori0@gmail.com\", \"login\": \"Satori\",\"name\": \"Satori\",\"birthday\": \"1989-10-24\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":2,\"email\":\"satori0@gmail.com\",\"login\":\"Satori\",\"name\":\"Satori\",\"birthday\":\"1989-10-24\",\"friends\":[]}"));

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"uralov@gmail.com\", \"login\": \"Uralov\",\"name\": \"Semen\",\"birthday\": \"1986-01-16\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":3,\"email\":\"uralov@gmail.com\",\"login\":\"Uralov\",\"name\":\"Semen\",\"birthday\":\"1986-01-16\",\"friends\":[]}"));

        this.mockMvc.perform(put("http://localhost:8081/users/1/friends/3"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"email\":\"vitekb650@gmail.com\",\"login\":\"PriestSexist\",\"name\":\"PriestSexist\",\"birthday\":\"2002-10-22\",\"friends\":[3]}"));

        assertEquals(1, userService.getUserById(3).getFriends().size());
        assertTrue(userService.getUserById(3).getFriends().contains(1));

        this.mockMvc.perform(put("http://localhost:8081/users/2/friends/3"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":2,\"email\":\"satori0@gmail.com\",\"login\":\"Satori\",\"name\":\"Satori\",\"birthday\":\"1989-10-24\",\"friends\":[3]}"));

        assertEquals(2, userService.getUserById(3).getFriends().size());
        assertTrue(userService.getUserById(3).getFriends().contains(1));
        assertTrue(userService.getUserById(3).getFriends().contains(2));

        this.mockMvc.perform((get("http://localhost:8081/users/1/friends/common/1")))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof EqualIdentifierException));

    }

}