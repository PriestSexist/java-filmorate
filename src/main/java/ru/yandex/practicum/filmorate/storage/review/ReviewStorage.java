package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {

    Optional<Review> postReview(Review review);

    Optional<Review> updateReview(Review review);

    void deleteReview(int reviewId);

    Optional<Review> getReviewById(int reviewId);

    List<Review> getAllReviews(int count);

    List<Review> getReviewsByFilmId(Integer filmId, int count);

    void putLikeToReview(int reviewId, int userId);

    void putDislikeToReview(int reviewId, int userId);

    void deleteLikeFromReview(int reviewId, int userId);

    void deleteDislikeFromReview(int reviewId, int userId);
}
