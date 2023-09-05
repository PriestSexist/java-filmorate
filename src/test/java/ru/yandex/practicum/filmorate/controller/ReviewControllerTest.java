package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.film.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.review.dao.ReviewDbStorage;
import ru.yandex.practicum.filmorate.storage.user.dao.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ReviewControllerTest {

    private final ReviewDbStorage reviewStorage;
    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;

    private Film getTestFilm() {
        Mpa mpa = new Mpa(5, "NC-17");
        Genre genre = new Genre(6, "Боевик");
        Film film = Film.builder()
                .name("Test film")
                .description("Test test")
                .releaseDate(LocalDate.of(2002, 10, 22))
                .mpa(mpa)
                .build();
        film.getGenres().add(genre);
        return film;
    }

    private User getTestUser() {
       return User.builder()
               .name("Test user")
               .email("test@mail.ru")
               .login("Test login")
               .birthday(LocalDate.of(2000, 10, 8))
               .build();
    }

    private Review getTestReview() {
       return Review.builder()
               .content("Bad film")
               .isPositive(false)
               .filmId(1)
               .userId(1)
               .build();
    }

    private Review getTestReviewToUpdate() {
        return Review.builder()
                .reviewId(1)
                .content("Not very bad film")
                .isPositive(true)
                .filmId(2)
                .userId(2)
                .useful(10)
                .build();
    }

    @Test
    void testPostReview() {
        userStorage.postUser(getTestUser());
        filmStorage.postFilm(getTestFilm());

        Optional<Review> reviewOptional = reviewStorage.postReview(getTestReview());

        assertThat(reviewOptional)
                .isPresent()
                .hasValueSatisfying(review -> assertThat(review).hasFieldOrPropertyWithValue("reviewId", 1))
                .hasValueSatisfying(review -> assertThat(review).hasFieldOrPropertyWithValue("content", "Bad film"))
                .hasValueSatisfying(review -> assertThat(review).hasFieldOrPropertyWithValue("isPositive", false))
                .hasValueSatisfying(review -> assertThat(review).hasFieldOrPropertyWithValue("filmId",1))
                .hasValueSatisfying(review -> assertThat(review).hasFieldOrPropertyWithValue("userId", 1))
                .hasValueSatisfying(review -> assertThat(review).hasFieldOrPropertyWithValue("useful", 0));
    }

    @Test
    void testUpdateReview() {
        userStorage.postUser(getTestUser());
        filmStorage.postFilm(getTestFilm());

        reviewStorage.postReview(getTestReview());
        Optional<Review> updatedReview = reviewStorage.updateReview(getTestReviewToUpdate());

        assertThat(updatedReview)
                .isPresent()
                .hasValueSatisfying(review -> assertThat(review).hasFieldOrPropertyWithValue("reviewId", 1))
                .hasValueSatisfying(review -> assertThat(review).hasFieldOrPropertyWithValue("content", "Not very bad film"))
                .hasValueSatisfying(review -> assertThat(review).hasFieldOrPropertyWithValue("isPositive", true))
                .hasValueSatisfying(review -> assertThat(review).hasFieldOrPropertyWithValue("filmId",1))
                .hasValueSatisfying(review -> assertThat(review).hasFieldOrPropertyWithValue("userId", 1))
                .hasValueSatisfying(review -> assertThat(review).hasFieldOrPropertyWithValue("useful", 0));

    }

    @Test
    void testGetReviewById() {
        userStorage.postUser(getTestUser());
        filmStorage.postFilm(getTestFilm());

        Optional<Review> reviewOptional = reviewStorage.postReview(getTestReview());
        Optional<Review> savedReview = reviewStorage.getReviewById(reviewOptional.get().getReviewId());

        assertThat(savedReview)
                .isPresent()
                .hasValueSatisfying(review -> assertThat(review).hasFieldOrPropertyWithValue("reviewId", 1))
                .hasValueSatisfying(review -> assertThat(review).hasFieldOrPropertyWithValue("content", "Bad film"))
                .hasValueSatisfying(review -> assertThat(review).hasFieldOrPropertyWithValue("isPositive", false))
                .hasValueSatisfying(review -> assertThat(review).hasFieldOrPropertyWithValue("filmId",1))
                .hasValueSatisfying(review -> assertThat(review).hasFieldOrPropertyWithValue("userId", 1))
                .hasValueSatisfying(review -> assertThat(review).hasFieldOrPropertyWithValue("useful", 0));

    }

    @Test
    void testGetAllReviewsToFilm1Count1() {
        userStorage.postUser(getTestUser());
        filmStorage.postFilm(getTestFilm());

        reviewStorage.postReview(getTestReview());

        List<Review> reviewsToFilm = reviewStorage.getReviewsByFilmId(1, 1);
        Optional<Review> reviewOptional = Optional.of(reviewsToFilm.get(0));

        assertThat(reviewOptional)
                .isPresent()
                .hasValueSatisfying(review -> assertThat(review).hasFieldOrPropertyWithValue("reviewId", 1))
                .hasValueSatisfying(review -> assertThat(review).hasFieldOrPropertyWithValue("content", "Bad film"))
                .hasValueSatisfying(review -> assertThat(review).hasFieldOrPropertyWithValue("isPositive", false))
                .hasValueSatisfying(review -> assertThat(review).hasFieldOrPropertyWithValue("filmId",1))
                .hasValueSatisfying(review -> assertThat(review).hasFieldOrPropertyWithValue("userId", 1))
                .hasValueSatisfying(review -> assertThat(review).hasFieldOrPropertyWithValue("useful", 0));
    }

    @Test
    void testDeleteReview() {
        userStorage.postUser(getTestUser());
        filmStorage.postFilm(getTestFilm());

        reviewStorage.postReview(getTestReview());
        reviewStorage.deleteReview(1);

        List<Review> reviewsToFilm = reviewStorage.getAllReviews(1);
        assertTrue(reviewsToFilm.isEmpty());
    }

    @Test
    void testPutLikeToReview() {
        userStorage.postUser(getTestUser());
        filmStorage.postFilm(getTestFilm());

        reviewStorage.postReview(getTestReview());
        reviewStorage.putLikeToReview(1, 1);
        Optional<Review> reviewOptional = reviewStorage.getReviewById(1);

        assertThat(reviewOptional)
                .isPresent()
                .hasValueSatisfying(review -> assertThat(review).hasFieldOrPropertyWithValue("reviewId", 1))
                .hasValueSatisfying(review -> assertThat(review).hasFieldOrPropertyWithValue("content", "Bad film"))
                .hasValueSatisfying(review -> assertThat(review).hasFieldOrPropertyWithValue("isPositive", false))
                .hasValueSatisfying(review -> assertThat(review).hasFieldOrPropertyWithValue("filmId",1))
                .hasValueSatisfying(review -> assertThat(review).hasFieldOrPropertyWithValue("userId", 1))
                .hasValueSatisfying(review -> assertThat(review).hasFieldOrPropertyWithValue("useful", 1));

        reviewStorage.deleteLikeFromReview(1, 1);
        Optional<Review> reviewOptional2 = reviewStorage.getReviewById(1);


        assertThat(reviewOptional2)
                .isPresent()
                .hasValueSatisfying(review -> assertThat(review).hasFieldOrPropertyWithValue("reviewId", 1))
                .hasValueSatisfying(review -> assertThat(review).hasFieldOrPropertyWithValue("content", "Bad film"))
                .hasValueSatisfying(review -> assertThat(review).hasFieldOrPropertyWithValue("isPositive", false))
                .hasValueSatisfying(review -> assertThat(review).hasFieldOrPropertyWithValue("filmId",1))
                .hasValueSatisfying(review -> assertThat(review).hasFieldOrPropertyWithValue("userId", 1))
                .hasValueSatisfying(review -> assertThat(review).hasFieldOrPropertyWithValue("useful", 0));
    }

    @Test
    void testPutDisLikeToReview() {
        userStorage.postUser(getTestUser());
        filmStorage.postFilm(getTestFilm());

        reviewStorage.postReview(getTestReview());
        reviewStorage.putDislikeToReview(1, 1);
        Optional<Review> reviewOptional = reviewStorage.getReviewById(1);

        assertThat(reviewOptional)
                .isPresent()
                .hasValueSatisfying(review -> assertThat(review).hasFieldOrPropertyWithValue("reviewId", 1))
                .hasValueSatisfying(review -> assertThat(review).hasFieldOrPropertyWithValue("content", "Bad film"))
                .hasValueSatisfying(review -> assertThat(review).hasFieldOrPropertyWithValue("isPositive", false))
                .hasValueSatisfying(review -> assertThat(review).hasFieldOrPropertyWithValue("filmId",1))
                .hasValueSatisfying(review -> assertThat(review).hasFieldOrPropertyWithValue("userId", 1))
                .hasValueSatisfying(review -> assertThat(review).hasFieldOrPropertyWithValue("useful", -1));

        reviewStorage.deleteDislikeFromReview(1, 1);
        Optional<Review> reviewOptional2 = reviewStorage.getReviewById(1);


        assertThat(reviewOptional2)
                .isPresent()
                .hasValueSatisfying(review -> assertThat(review).hasFieldOrPropertyWithValue("reviewId", 1))
                .hasValueSatisfying(review -> assertThat(review).hasFieldOrPropertyWithValue("content", "Bad film"))
                .hasValueSatisfying(review -> assertThat(review).hasFieldOrPropertyWithValue("isPositive", false))
                .hasValueSatisfying(review -> assertThat(review).hasFieldOrPropertyWithValue("filmId",1))
                .hasValueSatisfying(review -> assertThat(review).hasFieldOrPropertyWithValue("userId", 1))
                .hasValueSatisfying(review -> assertThat(review).hasFieldOrPropertyWithValue("useful", 0));
    }
}
