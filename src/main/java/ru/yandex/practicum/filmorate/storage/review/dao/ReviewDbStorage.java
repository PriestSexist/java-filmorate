package ru.yandex.practicum.filmorate.storage.review.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.Review.ReviewNotFoundException;
import ru.yandex.practicum.filmorate.exception.user.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.Review.ReviewNotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@Component
public class ReviewDbStorage implements ReviewStorage {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsertForReviews;

    @Autowired
    public ReviewDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        simpleJdbcInsertForReviews = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reviews")
                .usingGeneratedKeyColumns("review_id");
    }

    @Override
    public Optional<Review> postReview(Review review) {

        Map<String, Object> values = new HashMap<>();
        values.put("content", review.getContent());
        values.put("is_positive", review.getIsPositive());
        values.put("film_id", review.getFilmId());
        values.put("user_id", review.getUserId());
        values.put("useful", review.getUseful());

        log.debug("Creating review {}", review);
        int reviewId = simpleJdbcInsertForReviews.executeAndReturnKey(values).intValue();
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reviews")
                .usingGeneratedKeyColumns("review_id");
        log.debug("Creating review {}", review);

        review.setReviewId(reviewId);
        return Optional.of(review);
    }

    @Override
    public Optional<Review> updateReview(Review review) {
        int reviewId = review.getReviewId();

        String sql = "UPDATE reviews SET content = ?, is_positive = ? WHERE review_id = ?"; //userId & filmId final поля, не меняем
        log.debug("Updating review {}", review);

        if (jdbcTemplate.update(sql, review.getContent(), review.getIsPositive(), reviewId) == 0) {
            log.debug("Review with id {} is not found", reviewId);
            return Optional.empty();
        }
        checkRowsUpdated(jdbcTemplate.update(sql, review.getContent(), review.getIsPositive(), reviewId));
        return getReviewById(reviewId);
    }

    @Override
    public void deleteReview(int reviewId) {
        String sql = "DELETE FROM reviews WHERE review_id = ?";
        checkRowsUpdated(jdbcTemplate.update(sql, reviewId));
    }

    @Override
    public Optional<Review> getReviewById(int reviewId) {
        String sql = "SELECT * FROM reviews WHERE review_id = ?";

        Optional<Review> review;
        try {
            review = Optional.ofNullable(jdbcTemplate.queryForObject(sql, this::reviewFromSql, reviewId));
        } catch (EmptyResultDataAccessException e) {
           return Optional.empty();
        }
        return review;
    }

    @Override
    public List<Review> getReviewsByFilmId(Integer filmId, int count) {
        String sql = "SELECT * FROM reviews WHERE film_id = ? LIMIT ?";

        log.debug("Getting reviews to film with id {}", filmId);
        return new ArrayList<>(jdbcTemplate.query(sql, this::reviewFromSql, filmId, count));
    }

    public List<Review> getAllReviews(int count) {
        String sql = "SELECT * FROM reviews LIMIT ?";
        return new ArrayList<>(jdbcTemplate.query(sql, this::reviewFromSql, count));
    }

    @Override
    public void putLikeToReview(int reviewId, int userId) {
        String sql = "INSERT INTO review_rating (review_id, user_id, points) VALUES (?, ?, ?)";

        try {
            checkRowsUpdated(jdbcTemplate.update(sql, reviewId, userId, 1));
        } catch (DataIntegrityViolationException e) {
            log.debug("User with id {} is not found");
            throw new UserNotFoundException("User with id " + userId + " is not found");
        }
    }

    @Override
    public void putDislikeToReview(int reviewId, int userId) {
        String sql = "INSERT INTO review_rating (review_id, user_id, points) VALUES (?, ?, ?)";

        try {
            checkRowsUpdated(jdbcTemplate.update(sql, reviewId, userId, -1));
        } catch (DataIntegrityViolationException e) {
            log.debug("User with id {} is not found");
            throw new UserNotFoundException("User with id " + userId + " is not found");
        }
    }

    @Override
    public void deleteLikeFromReview(int reviewId, int userId) {
        String sql = "DELETE FROM review_rating WHERE review_id = ? AND user_id = ? AND points = ?";

        try {
            checkRowsUpdated(jdbcTemplate.update(sql, reviewId, userId, 1));
        } catch (DataIntegrityViolationException e) {
            log.debug("User with id {} is not found");
            throw new UserNotFoundException("User with id " + userId + " is not found");
        }
    }

    @Override
    public void deleteDislikeFromReview(int reviewId, int userId) {
        String sql = "DELETE FROM review_rating WHERE review_id = ? AND user_id = ? AND points = ?";

        try {
            checkRowsUpdated(jdbcTemplate.update(sql, reviewId, userId, -1));
        } catch (DataIntegrityViolationException e) {
            log.debug("User with id {} is not found");
            throw new UserNotFoundException("User with id " + userId + " is not found");
        }
    }

    private Integer calculateUseful(int reviewId) {
        String sql = "SELECT SUM(points) FROM review_rating WHERE review_id = ?";
        Integer pointsCount = jdbcTemplate.queryForObject(sql, Integer.class, reviewId);
        if (pointsCount == null) {
            return 0;
        }
        return pointsCount;
    }

    private void checkRowsUpdated(int result) {
        if (result == 0) {
            log.debug("Review is not found");
            throw new ReviewNotFoundException("Review is not found");
        }
    }

    private Review reviewFromSql(ResultSet rs, int rowNum) throws SQLException {
        int reviewId = rs.getInt("review_id");
        return Review.builder()
                .reviewId(reviewId)
                .content(rs.getString("content"))
                .isPositive(rs.getBoolean("is_positive"))
                .filmId(rs.getInt("film_id"))
                .userId(rs.getInt("user_id"))
                .useful(calculateUseful(reviewId))
                .build();
    }
}
