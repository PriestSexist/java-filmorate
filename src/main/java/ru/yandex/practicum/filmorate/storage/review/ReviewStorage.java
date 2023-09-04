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

    Optional<Review> putLikeToReview(int reviewId, int userId);

    Optional<Review> putDislikeToReview(int reviewId, int userId);

    Optional<Review> deleteLikeFromReview(int reviewId, int userId);

    Optional<Review> deleteDislikeFromReview(int reviewId, int userId);
}
