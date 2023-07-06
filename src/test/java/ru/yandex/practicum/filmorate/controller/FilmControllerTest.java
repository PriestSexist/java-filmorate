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
class FilmControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    FilmController controller;

    @Test
    public void shouldReturnAllFilms() throws Exception {

        this.mockMvc.perform(get("http://localhost:8081/films"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void shouldPostFilm() throws Exception {

        this.mockMvc.perform(post("http://localhost:8081/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"ViktorB Live\", \"releaseDate\": \"2002-10-22\", \"duration\": 60, \"description\": \"ViktorB hates everyone\"}"))
                .andDo(print())
                .andExpect(status().isOk());
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
    public void shouldNotPostFilmWithInvalidReleaseDate() {

        Assertions.assertThrows(Exception.class, () -> this.mockMvc.perform(post("http://localhost:8081/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"ViktorB Live\", \"releaseDate\": \"1895-12-27\", \"duration\": 60, \"description\": \"ViktorB hates everyone\"}"))
                        .andDo(print()));
    }

    @Test
    public void shouldPutFilm() throws Exception {

        this.mockMvc.perform(post("http://localhost:8081/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"ViktorB Alive\", \"releaseDate\": \"2002-10-22\", \"duration\": 60, \"description\": \"ViktorB hates everyone\"}"))
                .andDo(print())
                .andExpect(status().isOk());

        this.mockMvc.perform(put("http://localhost:8081/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\", \"name\": \"Stas Live\", \"releaseDate\": \"1989-10-24\", \"duration\": 120, \"description\": \"Stas hates everyone\"}"))
                .andDo(print())
                .andExpect(status().isOk());

    }

    @Test
    public void shouldNotPutFilmWithBlankName() throws Exception {

        this.mockMvc.perform(post("http://localhost:8081/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"ViktorB Live\", \"releaseDate\": \"2002-10-22\", \"duration\": 60, \"description\": \"ViktorB hates everyone\"}"))
                .andDo(print())
                .andExpect(status().isOk());

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
                .andExpect(status().isOk());

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
                .andExpect(status().isOk());

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
                .andExpect(status().isOk());

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
                .andExpect(status().isOk());

        Assertions.assertThrows(Exception.class, () -> this.mockMvc.perform(put("http://localhost:8081/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"1\", \"name\": \"Stas Live\", \"releaseDate\": \"1895-12-27\", \"duration\": 120, \"description\": \"Stas hates everyone\"}"))
                        .andDo(print()));

    }

}