package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.Review.ReviewNotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/reviews")
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public Review postReview(@RequestBody @Valid final Review review) {
        return reviewService.postReview(review).get();
    }

    @PutMapping
    public Review updateReview(@RequestBody @Valid Review review) {
        return reviewService.updateReview(review).orElseThrow(() -> {
            throw new ReviewNotFoundException("Review not found");
        });
    }

    @DeleteMapping("/{reviewId}")
    public void deleteReview(@PathVariable int reviewId) {
        reviewService.deleteReview(reviewId);
    }

    @GetMapping("/{reviewId}")
    public Review getReviewById(@PathVariable int reviewId) {
        return reviewService.getReviewById(reviewId).orElseThrow(() -> {
            throw new ReviewNotFoundException("Review not found");
        });
    }

    @GetMapping
    public List<Review> getAllReviewsByFilmId(@RequestParam(required = false) Integer filmId,
                                              @RequestParam(required = false, defaultValue = "10") int count) {
        return reviewService.getReviewsByFilmId(filmId, count);
    }

    @PutMapping("/{reviewId}/like/{userId}")
    public Review putLikeToReview(@PathVariable int reviewId, @PathVariable int userId) {
        return reviewService.putLikeToReview(reviewId, userId).orElseThrow(() -> {
            throw new NotFoundException("Film or User is not found");
        });
    }

    @PutMapping("/{reviewId}/dislike/{userId}")
    public Review putDislikeToReview(@PathVariable int reviewId, @PathVariable int userId) {
        return reviewService.putDislikeToReview(reviewId, userId).orElseThrow(() -> {
            throw new NotFoundException("Film or User is not found");
        });
    }

    @DeleteMapping("/{reviewId}/like/{userId}")
    public Review deleteLikeFromReview(@PathVariable int reviewId, @PathVariable int userId) {
        return reviewService.deleteLikeFromReview(reviewId, userId).orElseThrow(() -> {
            throw new NotFoundException("Film or User is not found");
        });
    }

    @DeleteMapping("/{reviewId}/dislike/{userId}")
    public Review deleteDislikeFromReview(@PathVariable int reviewId, @PathVariable int userId) {
        return reviewService.deleteDislikeFromReview(reviewId, userId).orElseThrow(() -> {
            throw new NotFoundException("Film or User is not found");
        });
    }
}
