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
import ru.yandex.practicum.filmorate.exception.film.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.film.InvalidReleaseDateException;
import ru.yandex.practicum.filmorate.exception.user.UserNotFoundException;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
class FilmControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    FilmController controller;

    @Autowired
    FilmService filmService;

    @Autowired
    UserService userService;

    @AfterEach
    public void reseter() {
        filmService.clear();
        userService.clear();
    }

    @Test
    public void shouldReturnAllFilms() throws Exception {

        this.mockMvc.perform(post("http://localhost:8081/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"ViktorB Live\", \"releaseDate\": \"2002-10-22\", \"duration\": 60, \"description\": \"ViktorB hates everyone\"}"))
                .andDo(print())
                .andExpect(status().isOk());

        this.mockMvc.perform(post("http://localhost:8081/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Stas Live\", \"releaseDate\": \"1989-10-24\", \"duration\": 120, \"description\": \"Stas hates everyone\"}"))
                .andDo(print())
                .andExpect(status().isOk());

        this.mockMvc.perform(get("http://localhost:8081/films"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("[{\"id\":1,\"name\":\"ViktorB Live\",\"releaseDate\":\"2002-10-22\",\"duration\":60,\"description\":\"ViktorB hates everyone\",\"peopleLiked\":[]},{\"id\":2,\"name\":\"Stas Live\",\"releaseDate\":\"1989-10-24\",\"duration\":120,\"description\":\"Stas hates everyone\",\"peopleLiked\":[]}]"));
    }

    @Test
    public void shouldPostFilm() throws Exception {

        this.mockMvc.perform(post("http://localhost:8081/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"ViktorB Live\", \"releaseDate\": \"2002-10-22\", \"duration\": 60, \"description\": \"ViktorB hates everyone\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"name\":\"ViktorB Live\",\"releaseDate\":\"2002-10-22\",\"duration\":60,\"description\":\"ViktorB hates everyone\",\"peopleLiked\":[]}"));

    }

    @Test
    public void shouldNotPostFilmWithBlankName() throws Exception {
        this.mockMvc.perform(post("http://localhost:8081/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \" \", \"releaseDate\": \"2002-10-22\", \"duration\": 60, \"description\": \"ViktorB hates everyone\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException));
    }

    @Test
    public void shouldNotPostFilmWithDuration0() throws Exception {
        this.mockMvc.perform(post("http://localhost:8081/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"ViktorB Live\", \"releaseDate\": \"2002-10-22\", \"duration\": 0, \"description\": \"ViktorB hates everyone\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException));
    }

    @Test
    public void shouldNotPostFilmWithNegativeDuration() throws Exception {
        this.mockMvc.perform(post("http://localhost:8081/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"ViktorB Live\", \"releaseDate\": \"2002-10-22\", \"duration\": -1, \"description\": \"ViktorB hates everyone\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException));
    }

    @Test
    public void shouldNotPostFilmWithLongDescription() throws Exception {
        this.mockMvc.perform(post("http://localhost:8081/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"ViktorB Live\", \"releaseDate\": \"2002-10-22\", \"duration\": 60, \"description\": \"ViktorB hates everyone. Even you. ViktorB hates everyone. Even you. ViktorB hates everyone. Even you. ViktorB hates everyone. Even you. ViktorB hates everyone. Even you. ViktorB hates everyone. Even you. \"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException));
    }

    @Test
    public void shouldNotPostFilmWithInvalidReleaseDate() throws Exception {

        this.mockMvc.perform(post("http://localhost:8081/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"ViktorB Live\", \"releaseDate\": \"1895-12-27\", \"duration\": 60, \"description\": \"ViktorB hates everyone\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof InvalidReleaseDateException));
    }

    @Test
    public void shouldPutFilm() throws Exception {

        this.mockMvc.perform(post("http://localhost:8081/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"ViktorB Live\", \"releaseDate\": \"2002-10-22\", \"duration\": 60, \"description\": \"ViktorB hates everyone\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"name\":\"ViktorB Live\",\"releaseDate\":\"2002-10-22\",\"duration\":60,\"description\":\"ViktorB hates everyone\",\"peopleLiked\":[]}"));

        this.mockMvc.perform(put("http://localhost:8081/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\", \"name\": \"Stas Live\", \"releaseDate\": \"1989-10-24\", \"duration\": 120, \"description\": \"Stas hates everyone\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"name\":\"Stas Live\",\"releaseDate\":\"1989-10-24\",\"duration\":120,\"description\":\"Stas hates everyone\",\"peopleLiked\":[]}"));
    }

    @Test
    public void shouldNotPutFilmWithBlankName() throws Exception {

        this.mockMvc.perform(post("http://localhost:8081/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"ViktorB Live\", \"releaseDate\": \"2002-10-22\", \"duration\": 60, \"description\": \"ViktorB hates everyone\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"name\":\"ViktorB Live\",\"releaseDate\":\"2002-10-22\",\"duration\":60,\"description\":\"ViktorB hates everyone\",\"peopleLiked\":[]}"));

        this.mockMvc.perform(put("http://localhost:8081/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\", \"name\": \" \", \"releaseDate\": \"1989-10-24\", \"duration\": 120, \"description\": \"Stas hates everyone\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException));
    }

    @Test
    public void shouldNotPutFilmWithDuration0() throws Exception {

        this.mockMvc.perform(post("http://localhost:8081/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"ViktorB Live\", \"releaseDate\": \"2002-10-22\", \"duration\": 60, \"description\": \"ViktorB hates everyone\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"name\":\"ViktorB Live\",\"releaseDate\":\"2002-10-22\",\"duration\":60,\"description\":\"ViktorB hates everyone\",\"peopleLiked\":[]}"));

        this.mockMvc.perform(put("http://localhost:8081/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\", \"name\": \"Stas Live\", \"releaseDate\": \"1989-10-24\", \"duration\": 0, \"description\": \"Stas hates everyone\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException));
    }

    @Test
    public void shouldNotPutFilmWithNegativeDuration() throws Exception {

        this.mockMvc.perform(post("http://localhost:8081/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"ViktorB Live\", \"releaseDate\": \"2002-10-22\", \"duration\": 60, \"description\": \"ViktorB hates everyone\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"name\":\"ViktorB Live\",\"releaseDate\":\"2002-10-22\",\"duration\":60,\"description\":\"ViktorB hates everyone\",\"peopleLiked\":[]}"));

        this.mockMvc.perform(put("http://localhost:8081/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\", \"name\": \"Stas Live\", \"releaseDate\": \"1989-10-24\", \"duration\": -1, \"description\": \"Stas hates everyone\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException));
    }

    @Test
    public void shouldNotPutFilmWithLongDescription() throws Exception {

        this.mockMvc.perform(post("http://localhost:8081/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"ViktorB Live\", \"releaseDate\": \"2002-10-22\", \"duration\": 60, \"description\": \"ViktorB hates everyone\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"name\":\"ViktorB Live\",\"releaseDate\":\"2002-10-22\",\"duration\":60,\"description\":\"ViktorB hates everyone\",\"peopleLiked\":[]}"));

        this.mockMvc.perform(put("http://localhost:8081/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\", \"name\": \"ViktorB Live\", \"releaseDate\": \"2002-10-22\", \"duration\": 60, \"description\": \"ViktorB hates everyone. Even you. ViktorB hates everyone. Even you. ViktorB hates everyone. Even you. ViktorB hates everyone. Even you. ViktorB hates everyone. Even you. ViktorB hates everyone. Even you. \"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException));
    }

    @Test
    public void shouldNotPutFilmWithInvalidReleaseDate() throws Exception {

        this.mockMvc.perform(post("http://localhost:8081/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"ViktorB Live\", \"releaseDate\": \"2002-10-22\", \"duration\": 60, \"description\": \"ViktorB hates everyone\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"name\":\"ViktorB Live\",\"releaseDate\":\"2002-10-22\",\"duration\":60,\"description\":\"ViktorB hates everyone\",\"peopleLiked\":[]}"));

        this.mockMvc.perform(put("http://localhost:8081/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\", \"name\": \"Stas Live\", \"releaseDate\": \"1895-12-27\", \"duration\": 120, \"description\": \"Stas hates everyone\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof InvalidReleaseDateException));
    }

    @Test
    public void shouldGetFilmById() throws Exception {

        this.mockMvc.perform(post("http://localhost:8081/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"ViktorB Live\", \"releaseDate\": \"2002-10-22\", \"duration\": 60, \"description\": \"ViktorB hates everyone\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"name\":\"ViktorB Live\",\"releaseDate\":\"2002-10-22\",\"duration\":60,\"description\":\"ViktorB hates everyone\",\"peopleLiked\":[]}"));

        this.mockMvc.perform(get("http://localhost:8081/films/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"name\":\"ViktorB Live\",\"releaseDate\":\"2002-10-22\",\"duration\":60,\"description\":\"ViktorB hates everyone\",\"peopleLiked\":[]}"));

    }

    @Test
    public void shouldNotGetFilmByInvalidId() throws Exception {

        this.mockMvc.perform(post("http://localhost:8081/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"ViktorB Live\", \"releaseDate\": \"2002-10-22\", \"duration\": 60, \"description\": \"ViktorB hates everyone\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"name\":\"ViktorB Live\",\"releaseDate\":\"2002-10-22\",\"duration\":60,\"description\":\"ViktorB hates everyone\",\"peopleLiked\":[]}"));

        this.mockMvc.perform(get("http://localhost:8081/films/-1"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof FilmNotFoundException));
    }

    @Test
    public void shouldPutLikeToFilm() throws Exception {

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"vitekb650@gmail.com\", \"login\": \"PriestSexist\",\"name\": \"PriestSexist\",\"birthday\": \"2002-10-22\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"email\":\"vitekb650@gmail.com\",\"login\":\"PriestSexist\",\"name\":\"PriestSexist\",\"birthday\":\"2002-10-22\",\"friends\":[]}"));

        this.mockMvc.perform(post("http://localhost:8081/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"ViktorB Live\", \"releaseDate\": \"2002-10-22\", \"duration\": 60, \"description\": \"ViktorB hates everyone\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"name\":\"ViktorB Live\",\"releaseDate\":\"2002-10-22\",\"duration\":60,\"description\":\"ViktorB hates everyone\",\"peopleLiked\":[]}"));

        this.mockMvc.perform(put("http://localhost:8081/films/1/like/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"name\":\"ViktorB Live\",\"releaseDate\":\"2002-10-22\",\"duration\":60,\"description\":\"ViktorB hates everyone\",\"peopleLiked\":[1]}"));
    }

    @Test
    public void shouldNotPutLikeToFilmWithInvalidUserId() throws Exception {

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"vitekb650@gmail.com\", \"login\": \"PriestSexist\",\"name\": \"PriestSexist\",\"birthday\": \"2002-10-22\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"email\":\"vitekb650@gmail.com\",\"login\":\"PriestSexist\",\"name\":\"PriestSexist\",\"birthday\":\"2002-10-22\",\"friends\":[]}"));

        this.mockMvc.perform(post("http://localhost:8081/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"ViktorB Live\", \"releaseDate\": \"2002-10-22\", \"duration\": 60, \"description\": \"ViktorB hates everyone\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"name\":\"ViktorB Live\",\"releaseDate\":\"2002-10-22\",\"duration\":60,\"description\":\"ViktorB hates everyone\",\"peopleLiked\":[]}"));

        this.mockMvc.perform(put("http://localhost:8081/films/1/like/-1"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserNotFoundException));
    }

    @Test
    public void shouldNotPutLikeToFilmWithInvalidFilmId() throws Exception {

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"vitekb650@gmail.com\", \"login\": \"PriestSexist\",\"name\": \"PriestSexist\",\"birthday\": \"2002-10-22\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"email\":\"vitekb650@gmail.com\",\"login\":\"PriestSexist\",\"name\":\"PriestSexist\",\"birthday\":\"2002-10-22\",\"friends\":[]}"));

        this.mockMvc.perform(post("http://localhost:8081/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"ViktorB Live\", \"releaseDate\": \"2002-10-22\", \"duration\": 60, \"description\": \"ViktorB hates everyone\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"name\":\"ViktorB Live\",\"releaseDate\":\"2002-10-22\",\"duration\":60,\"description\":\"ViktorB hates everyone\",\"peopleLiked\":[]}"));

        this.mockMvc.perform(put("http://localhost:8081/films/-1/like/1"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof FilmNotFoundException));
    }

    @Test
    public void shouldDeleteLikeToFilm() throws Exception {

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"vitekb650@gmail.com\", \"login\": \"PriestSexist\",\"name\": \"PriestSexist\",\"birthday\": \"2002-10-22\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"email\":\"vitekb650@gmail.com\",\"login\":\"PriestSexist\",\"name\":\"PriestSexist\",\"birthday\":\"2002-10-22\",\"friends\":[]}"));

        this.mockMvc.perform(post("http://localhost:8081/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"ViktorB Live\", \"releaseDate\": \"2002-10-22\", \"duration\": 60, \"description\": \"ViktorB hates everyone\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"name\":\"ViktorB Live\",\"releaseDate\":\"2002-10-22\",\"duration\":60,\"description\":\"ViktorB hates everyone\",\"peopleLiked\":[]}"));

        this.mockMvc.perform(put("http://localhost:8081/films/1/like/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"name\":\"ViktorB Live\",\"releaseDate\":\"2002-10-22\",\"duration\":60,\"description\":\"ViktorB hates everyone\",\"peopleLiked\":[1]}"));

        this.mockMvc.perform(delete("http://localhost:8081/films/1/like/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"name\":\"ViktorB Live\",\"releaseDate\":\"2002-10-22\",\"duration\":60,\"description\":\"ViktorB hates everyone\",\"peopleLiked\":[]}"));
    }

    @Test
    public void shouldNotDeleteLikeToFilmWithInvalidUserId() throws Exception {

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"vitekb650@gmail.com\", \"login\": \"PriestSexist\",\"name\": \"PriestSexist\",\"birthday\": \"2002-10-22\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"email\":\"vitekb650@gmail.com\",\"login\":\"PriestSexist\",\"name\":\"PriestSexist\",\"birthday\":\"2002-10-22\",\"friends\":[]}"));

        this.mockMvc.perform(post("http://localhost:8081/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"ViktorB Live\", \"releaseDate\": \"2002-10-22\", \"duration\": 60, \"description\": \"ViktorB hates everyone\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"name\":\"ViktorB Live\",\"releaseDate\":\"2002-10-22\",\"duration\":60,\"description\":\"ViktorB hates everyone\",\"peopleLiked\":[]}"));

        this.mockMvc.perform(put("http://localhost:8081/films/1/like/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"name\":\"ViktorB Live\",\"releaseDate\":\"2002-10-22\",\"duration\":60,\"description\":\"ViktorB hates everyone\",\"peopleLiked\":[1]}"));

        this.mockMvc.perform(delete("http://localhost:8081/films/1/like/-1"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserNotFoundException));
    }

    @Test
    public void shouldNotDeleteLikeToFilmWithInvalidFilmId() throws Exception {

        this.mockMvc.perform(post("http://localhost:8081/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"vitekb650@gmail.com\", \"login\": \"PriestSexist\",\"name\": \"PriestSexist\",\"birthday\": \"2002-10-22\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"email\":\"vitekb650@gmail.com\",\"login\":\"PriestSexist\",\"name\":\"PriestSexist\",\"birthday\":\"2002-10-22\",\"friends\":[]}"));

        this.mockMvc.perform(post("http://localhost:8081/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"ViktorB Live\", \"releaseDate\": \"2002-10-22\", \"duration\": 60, \"description\": \"ViktorB hates everyone\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"name\":\"ViktorB Live\",\"releaseDate\":\"2002-10-22\",\"duration\":60,\"description\":\"ViktorB hates everyone\",\"peopleLiked\":[]}"));

        this.mockMvc.perform(put("http://localhost:8081/films/1/like/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"name\":\"ViktorB Live\",\"releaseDate\":\"2002-10-22\",\"duration\":60,\"description\":\"ViktorB hates everyone\",\"peopleLiked\":[1]}"));

        this.mockMvc.perform(delete("http://localhost:8081/films/-1/like/1"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof FilmNotFoundException));
    }

    @Test
    public void shouldReturnTop3Films() throws Exception {

        //Два пользователя
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

        //3 фильма
        this.mockMvc.perform(post("http://localhost:8081/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"ViktorB Live 1. Pay and repent. Repent and pay!\", \"releaseDate\": \"2002-10-22\", \"duration\": 60, \"description\": \"ViktorB hates everyone\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"name\":\"ViktorB Live 1. Pay and repent. Repent and pay!\",\"releaseDate\":\"2002-10-22\",\"duration\":60,\"description\":\"ViktorB hates everyone\",\"peopleLiked\":[]}"));

        this.mockMvc.perform(post("http://localhost:8081/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"ViktorB Live 2. SCHOOL OF SOCIALISM!\", \"releaseDate\": \"2002-10-22\", \"duration\": 60, \"description\": \"ViktorB hates everyone\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":2,\"name\":\"ViktorB Live 2. SCHOOL OF SOCIALISM!\",\"releaseDate\":\"2002-10-22\",\"duration\":60,\"description\":\"ViktorB hates everyone\",\"peopleLiked\":[]}"));

        this.mockMvc.perform(post("http://localhost:8081/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"ViktorB Live 3. Law on foreign agents of the RF!\", \"releaseDate\": \"2002-10-22\", \"duration\": 60, \"description\": \"ViktorB hates everyone\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":3,\"name\":\"ViktorB Live 3. Law on foreign agents of the RF!\",\"releaseDate\":\"2002-10-22\",\"duration\":60,\"description\":\"ViktorB hates everyone\",\"peopleLiked\":[]}"));

        //ставят оценки
        this.mockMvc.perform(put("http://localhost:8081/films/1/like/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"name\":\"ViktorB Live 1. Pay and repent. Repent and pay!\",\"releaseDate\":\"2002-10-22\",\"duration\":60,\"description\":\"ViktorB hates everyone\",\"peopleLiked\":[1]}"));

        this.mockMvc.perform(put("http://localhost:8081/films/1/like/2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"name\":\"ViktorB Live 1. Pay and repent. Repent and pay!\",\"releaseDate\":\"2002-10-22\",\"duration\":60,\"description\":\"ViktorB hates everyone\",\"peopleLiked\":[1,2]}"));

        this.mockMvc.perform(put("http://localhost:8081/films/3/like/2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":3,\"name\":\"ViktorB Live 3. Law on foreign agents of the RF!\",\"releaseDate\":\"2002-10-22\",\"duration\":60,\"description\":\"ViktorB hates everyone\",\"peopleLiked\":[2]}"));

        //вывод популярного
        this.mockMvc.perform(get("http://localhost:8081/films/popular?count=3"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("[{\"id\":1,\"name\":\"ViktorB Live 1. Pay and repent. Repent and pay!\",\"releaseDate\":\"2002-10-22\",\"duration\":60,\"description\":\"ViktorB hates everyone\",\"peopleLiked\":[1,2]},{\"id\":3,\"name\":\"ViktorB Live 3. Law on foreign agents of the RF!\",\"releaseDate\":\"2002-10-22\",\"duration\":60,\"description\":\"ViktorB hates everyone\",\"peopleLiked\":[2]},{\"id\":2,\"name\":\"ViktorB Live 2. SCHOOL OF SOCIALISM!\",\"releaseDate\":\"2002-10-22\",\"duration\":60,\"description\":\"ViktorB hates everyone\",\"peopleLiked\":[]}]"));

    }

    @Test
    public void shouldReturnTop4FilmsButWithoutCount() throws Exception {

        //Два пользователя
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

        //4 фильма
        this.mockMvc.perform(post("http://localhost:8081/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"ViktorB Live 1. Pay and repent. Repent and pay!\", \"releaseDate\": \"2002-10-22\", \"duration\": 60, \"description\": \"ViktorB hates everyone\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"name\":\"ViktorB Live 1. Pay and repent. Repent and pay!\",\"releaseDate\":\"2002-10-22\",\"duration\":60,\"description\":\"ViktorB hates everyone\",\"peopleLiked\":[]}"));

        this.mockMvc.perform(post("http://localhost:8081/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"ViktorB Live 2. SCHOOL OF SOCIALISM!\", \"releaseDate\": \"2002-10-22\", \"duration\": 60, \"description\": \"ViktorB hates everyone\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":2,\"name\":\"ViktorB Live 2. SCHOOL OF SOCIALISM!\",\"releaseDate\":\"2002-10-22\",\"duration\":60,\"description\":\"ViktorB hates everyone\",\"peopleLiked\":[]}"));

        this.mockMvc.perform(post("http://localhost:8081/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"ViktorB Live 3. Law on foreign agents of the RF!\", \"releaseDate\": \"2002-10-22\", \"duration\": 60, \"description\": \"ViktorB hates everyone\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":3,\"name\":\"ViktorB Live 3. Law on foreign agents of the RF!\",\"releaseDate\":\"2002-10-22\",\"duration\":60,\"description\":\"ViktorB hates everyone\",\"peopleLiked\":[]}"));

        this.mockMvc.perform(post("http://localhost:8081/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"ViktorB Live 4. Lokimin is not going on tour in Russia!\", \"releaseDate\": \"2002-10-22\", \"duration\": 60, \"description\": \"ViktorB hates everyone\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":4,\"name\":\"ViktorB Live 4. Lokimin is not going on tour in Russia!\",\"releaseDate\":\"2002-10-22\",\"duration\":60,\"description\":\"ViktorB hates everyone\",\"peopleLiked\":[]}"));

        //ставят оценки
        this.mockMvc.perform(put("http://localhost:8081/films/1/like/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"name\":\"ViktorB Live 1. Pay and repent. Repent and pay!\",\"releaseDate\":\"2002-10-22\",\"duration\":60,\"description\":\"ViktorB hates everyone\",\"peopleLiked\":[1]}"));

        this.mockMvc.perform(put("http://localhost:8081/films/1/like/2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":1,\"name\":\"ViktorB Live 1. Pay and repent. Repent and pay!\",\"releaseDate\":\"2002-10-22\",\"duration\":60,\"description\":\"ViktorB hates everyone\",\"peopleLiked\":[1,2]}"));

        this.mockMvc.perform(put("http://localhost:8081/films/3/like/2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":3,\"name\":\"ViktorB Live 3. Law on foreign agents of the RF!\",\"releaseDate\":\"2002-10-22\",\"duration\":60,\"description\":\"ViktorB hates everyone\",\"peopleLiked\":[2]}"));

        this.mockMvc.perform(put("http://localhost:8081/films/4/like/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":4,\"name\":\"ViktorB Live 4. Lokimin is not going on tour in Russia!\",\"releaseDate\":\"2002-10-22\",\"duration\":60,\"description\":\"ViktorB hates everyone\",\"peopleLiked\":[1]}"));

        this.mockMvc.perform(put("http://localhost:8081/films/4/like/2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("{\"id\":4,\"name\":\"ViktorB Live 4. Lokimin is not going on tour in Russia!\",\"releaseDate\":\"2002-10-22\",\"duration\":60,\"description\":\"ViktorB hates everyone\",\"peopleLiked\":[1,2]}"));

        //вывод популярного
        this.mockMvc.perform(get("http://localhost:8081/films/popular"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("[{\"id\":1,\"name\":\"ViktorB Live 1. Pay and repent. Repent and pay!\",\"releaseDate\":\"2002-10-22\",\"duration\":60,\"description\":\"ViktorB hates everyone\",\"peopleLiked\":[1,2]},{\"id\":4,\"name\":\"ViktorB Live 4. Lokimin is not going on tour in Russia!\",\"releaseDate\":\"2002-10-22\",\"duration\":60,\"description\":\"ViktorB hates everyone\",\"peopleLiked\":[1,2]},{\"id\":3,\"name\":\"ViktorB Live 3. Law on foreign agents of the RF!\",\"releaseDate\":\"2002-10-22\",\"duration\":60,\"description\":\"ViktorB hates everyone\",\"peopleLiked\":[2]},{\"id\":2,\"name\":\"ViktorB Live 2. SCHOOL OF SOCIALISM!\",\"releaseDate\":\"2002-10-22\",\"duration\":60,\"description\":\"ViktorB hates everyone\",\"peopleLiked\":[]}]"));

    }

}