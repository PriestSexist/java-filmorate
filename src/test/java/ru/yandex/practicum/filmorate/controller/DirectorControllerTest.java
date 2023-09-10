package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.yandex.practicum.filmorate.model.Director;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class DirectorControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    DirectorController directorController;
    ObjectMapper mapper = new ObjectMapper();

    @Test
    public void getEmptyDirectors() throws Exception {
        mockMvc.perform(get("/directors")).andExpect(status().isOk());
    }

    @Test
    public void addDirector() throws Exception {
        Director director = Director.builder()
                .id(1)
                .name("Name")
                .build();

        String json = mapper.writeValueAsString(director);

        MvcResult result = mockMvc
                .perform(post("/directors").content(json).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(json, result.getResponse().getContentAsString());
    }

    @Test
    public void addDirectorEmptyName() throws Exception {
        Director director = Director.builder()
                .id(1)
                .build();

        String json = mapper.writeValueAsString(director);

        mockMvc.perform(post("/directors")
                .content(json).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
    }

    @Test
    public void addDirectorBlankName() throws Exception {
        Director director = Director.builder()
                .id(1)
                .name("")
                .build();

        String json = mapper.writeValueAsString(director);

        mockMvc.perform(post("/directors")
                .content(json).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
    }

    @Test
    public void getDirector() throws Exception {
        Director director = Director.builder()
                .id(1)
                .name("Name")
                .build();

        String json = mapper.writeValueAsString(director);

        mockMvc.perform(post("/directors")
                .content(json).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

        MvcResult result = mockMvc.perform(get("/directors/1"))
                .andExpect(status().isOk()).andReturn();

        assertEquals(json, result.getResponse().getContentAsString());
    }

    @Test
    public void getNotFoundDirector() throws Exception {
        Director director = Director.builder()
                .id(1)
                .name("Name")
                .build();

        String json = mapper.writeValueAsString(director);

        mockMvc.perform(get("/directors/2")).andExpect(status().isNotFound());
    }

    @Test
    public void updateDirector() throws Exception {
        Director director = Director.builder()
                .id(1)
                .name("Name")
                .build();

        String json = mapper.writeValueAsString(director);

        mockMvc.perform(post("/directors")
                .content(json).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

        Director director2 = Director.builder()
                .id(1)
                .name("Name2")
                .build();

        json = mapper.writeValueAsString(director2);
        mockMvc.perform(put("/directors")
                .content(json).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

        MvcResult result = mockMvc.perform(get("/directors/1"))
                .andExpect(status().isOk()).andReturn();

        assertEquals(json, result.getResponse().getContentAsString());
    }

    @Test
    public void updateNotFoundDirector() throws Exception {
        Director director = Director.builder()
                .id(1)
                .name("Name")
                .build();

        String json = mapper.writeValueAsString(director);

        mockMvc.perform(post("/directors")
                .content(json).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

        Director director2 = Director.builder()
                .id(2)
                .name("Name2")
                .build();

        json = mapper.writeValueAsString(director2);

        mockMvc.perform(put("/directors")
                .content(json).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound());
    }

    @Test
    public void deleteNormalDirector() throws Exception {
        Director director = Director.builder()
                .id(1)
                .name("Name")
                .build();

        String json = mapper.writeValueAsString(director);

        mockMvc.perform(post("/directors")
                .content(json).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

        mockMvc.perform(delete("/directors/1")).andExpect(status().isOk());

        mockMvc.perform(get("/directors/1")).andExpect(status().isNotFound());
    }

    @Test
    public void deleteNotFoundDirector() throws Exception {
        Director director = Director.builder()
                .id(1)
                .name("Name")
                .build();

        String json = mapper.writeValueAsString(director);

        mockMvc.perform(post("/directors")
                .content(json).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

        mockMvc.perform(delete("/directors/2")).andExpect(status().isNotFound());
    }
}