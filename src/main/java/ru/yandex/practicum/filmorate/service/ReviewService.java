package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.film.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.user.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.utility.EventOperation;
import ru.yandex.practicum.filmorate.utility.EventType;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final UserService userService;
    private final FilmService filmService;

    @Autowired
    public ReviewService(ReviewStorage reviewStorage, UserService userService, FilmService filmService) {
        this.reviewStorage = reviewStorage;
        this.userService = userService;
        this.filmService = filmService;
    }

    public Optional<Review> postReview(Review review) {
        checkUserId(review.getUserId()); /*если не проверять id тут, а просто отлавливать ошибки в ReviewDbStorage,
        то id генерируются с  gap, по тестам это получается 1, 4, 5*/
        checkFilmId(review.getFilmId());
        return reviewStorage.postReview(review);
    }

    public Optional<Review> updateReview(Review review) {
        return reviewStorage.updateReview(review);
    }

    public void deleteReview(int reviewId) {
        reviewStorage.deleteReview(reviewId);
    private final EventService eventService;

    @Autowired
    public ReviewService(ReviewStorage reviewStorage, UserService userService, FilmService filmService, EventService eventService) {
        this.reviewStorage = reviewStorage;
        this.userService = userService;
        this.filmService = filmService;
        this.eventService = eventService;
    }

    public Optional<Review> postReview(Review review) {
        checkUserId(review.getUserId()); //проверяю тут, чтобы не генерировались лишние id при ошибке создания review
        checkFilmId(review.getFilmId());
        Optional<Review> review1 = reviewStorage.postReview(review);
        eventService.createEvent(review1.get().getUserId(), EventType.REVIEW, EventOperation.ADD, review1.get().getReviewId());
        return review1;
    }

    public Optional<Review> updateReview(Review review) {
        Optional<Review> review1 = reviewStorage.updateReview(review);
        eventService.createEvent(review1.get().getUserId(), EventType.REVIEW, EventOperation.UPDATE, review1.get().getReviewId());
        return review1;
    }

    public void deleteReview(int reviewId) {
        Optional<Review> review1 = reviewStorage.getReviewById(reviewId);
        reviewStorage.deleteReview(reviewId);
        eventService.createEvent(review1.get().getUserId(), EventType.REVIEW, EventOperation.REMOVE, review1.get().getReviewId());
    }

    public Optional<Review> getReviewById(int reviewId) {
        return reviewStorage.getReviewById(reviewId);
    }

    public List<Review> getReviewsByFilmId(Integer filmId, int count) {
        if (filmId == null) {
            return reviewStorage.getAllReviews(count).stream()
                    .sorted(Comparator.comparing(Review::getUseful).reversed())
                    .collect(Collectors.toList());
        } else {
            return reviewStorage.getReviewsByFilmId(filmId, count).stream()
                    .sorted(Comparator.comparing(Review::getUseful).reversed())
                    .collect(Collectors.toList());
        }
    }

    public void putLikeToReview(int reviewId, int userId) {
        reviewStorage.putLikeToReview(reviewId, userId);
    }

    public void putDislikeToReview(int reviewId, int userId) {
        reviewStorage.putDislikeToReview(reviewId, userId);
    }

    public void deleteLikeFromReview(int reviewId, int userId) {
        reviewStorage.deleteLikeFromReview(reviewId, userId);
    }

    public void deleteDislikeFromReview(int reviewId, int userId) {
        reviewStorage.deleteDislikeFromReview(reviewId, userId);
    }

    private void checkUserId(int userId) {
        if (userService.getUserById(userId).isEmpty()) {
            throw new UserNotFoundException("User is not found");
        }
    }

    private void checkFilmId(int userId) {
        if (filmService.getFilmById(userId).isEmpty()) {
            throw new FilmNotFoundException("Film is not found");
        }
    }

}

