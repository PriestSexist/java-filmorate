package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserController controller;

    @Test
    public void shouldReturnAllUsers() throws Exception {

        this.mockMvc.perform(get("http://localhost:8081/users"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void shouldPostUser() throws Exception {

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"vitekb650@gmail.com\", \"login\": \"PriestSexist\",\"name\": \"PriestSexist\",\"birthday\": \"2002-10-22\"}"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void shouldPostUserWithBlankName() throws Exception {

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"vitekb650@gmail.com\", \"login\": \"PriestSexist\",\"name\": \" \",\"birthday\": \"2002-10-22\"}"))
                .andDo(print())
                .andExpect(status().isOk());
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
    public void shouldNotPostUserWithLoginWithSpaces() {

        int flag;

        try {
            this.mockMvc.perform(post("http://localhost:8081/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\": \"vitekb650@gmail.com\", \"login\": \"Priest Sexist\",\"name\": \"PriestSexist\",\"birthday\": \"2002-10-22\"}"))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException));
            flag = 1;

        } catch (Exception exception) {
            flag = 0;
        }
        Assertions.assertEquals(0, flag);
    }

    @Test
    public void shouldPutUser() throws Exception {

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"vitekb650@gmail.com\", \"login\": \"PriestSexist\",\"name\": \"PriestSexist\",\"birthday\": \"2002-10-22\"}"))
                .andDo(print())
                .andExpect(status().isOk());

        this.mockMvc.perform(put("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"vitekb650@gmail.com\", \"login\": \"PriestSexist\",\"name\": \"PriestSexist\",\"birthday\": \"2002-10-22\"}"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void shouldNotPutUserWithInvalidEmail() throws Exception {

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"vitekb650@gmail.com\", \"login\": \"PriestSexist\",\"name\": \"PriestSexist\",\"birthday\": \"2002-10-22\"}"))
                .andDo(print())
                .andExpect(status().isOk());

        this.mockMvc.perform(put("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"vit@ekb650g@mail.com\", \"login\": \"PriestSexist\",\"name\": \"PriestSexist\",\"birthday\": \"2002-10-22\"}"))
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
                .andExpect(status().isOk());

        this.mockMvc.perform(put("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \" \", \"login\": \"PriestSexist\",\"name\": \"PriestSexist\",\"birthday\": \"2002-10-22\"}"))
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
                .andExpect(status().isOk());

        this.mockMvc.perform(put("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"vitekb650@gmail.com\", \"login\": \" \",\"name\": \"PriestSexist\",\"birthday\": \"2002-10-22\"}"))
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
                .andExpect(status().isOk());

        this.mockMvc.perform(put("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"vitekb650@gmail.com\", \"login\": \"PriestSexist\",\"name\": \"PriestSexist\",\"birthday\": \"2025-10-22\"}"))
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
                .andExpect(status().isOk());

        int flag;

        try {
            this.mockMvc.perform(put("http://localhost:8081/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\": \"vitekb650@gmail.com\", \"login\": \"Priest Sexist\",\"name\": \"PriestSexist\",\"birthday\": \"2002-10-22\"}"))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException));
            flag = 1;

        } catch (Exception exception) {
            flag = 0;
        }
        Assertions.assertEquals(0, flag);
    }
}