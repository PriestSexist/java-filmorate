package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.DirectorService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
public class DirectorController {

    /** Поле сервисного слоя добавления режисеров */
    private final DirectorService directorService;

    @ResponseBody
    @GetMapping("/directors")
    public List<Director> getDirectors() {
        // GET /directors

        log.info("Вызван GET запрос на получение списка всех режисеров.");

        return directorService.getDirectors();
    }

    @ResponseBody
    @GetMapping("/directors/{id}")
    public Director getDirector(@PathVariable int id) {
        // GET /directors/{id}

        log.info("Вызван GET запрос для получения режисера по идентификатору.");
        log.debug("Передан идентификатор режисера {},", id);

        return directorService.getDirector(id);
    }

    @ResponseBody
    @PostMapping(value = "/directors")
    public Director createDirector(@Valid @RequestBody Director director) {
        // POST /directors

        log.info("Вызван POST запрос на добавление нового режисера.");
        log.debug("Новый режисер с именем {} и идентификатором {}",
                director.getName(), director.getId());

        return directorService.createDirector(director);
    }

    @PutMapping("/directors")
    public Director updateDirector(@RequestBody Director director) {
        // PUT /directors

        log.info("Вызван PUT запрос для обновления существующего режисера.");
        log.debug("Имя режисера {} и идентификатор {}", director.getName(), director.getId());

        return directorService.updateDirector(director);
    }

    @DeleteMapping("/directors/{id}")
    public void deleteDirector(@PathVariable int id) {
        // DELETE /directors/{id}

        log.debug("Вызван DELETE запрос на удаление режисера по идентификатору.");
        log.debug("Идентификатор режисера {}", id);

        directorService.removeDirector(id);
    }
}
