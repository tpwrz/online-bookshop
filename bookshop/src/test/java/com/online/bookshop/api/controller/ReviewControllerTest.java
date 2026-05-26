package com.online.bookshop.api.controller;

import com.online.bookshop.application.service.ReviewService;
import com.online.bookshop.domain.model.Review;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewController — unit tests")
class ReviewControllerTest {

    @Mock
    private ReviewService reviewService;

    @InjectMocks
    private ReviewController controller;

    private Review review;

    @BeforeEach
    void setUp() {
        review = new Review(1L, 1L, 1L, "Great book!", 5);
    }

    @Nested
    @DisplayName("GET /reviews")
    class GetAll {

        @Test
        @DisplayName("returns 200 with list of reviews")
        void returnsAllReviews() {
            when(reviewService.findAll()).thenReturn(List.of(review));

            ResponseEntity<List<Review>> response = controller.getAllReviews();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).getReviewMessage()).isEqualTo("Great book!");
        }

        @Test
        @DisplayName("returns 200 with empty list when no reviews")
        void returnsEmptyList() {
            when(reviewService.findAll()).thenReturn(List.of());

            ResponseEntity<List<Review>> response = controller.getAllReviews();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }
    }

    @Nested
    @DisplayName("GET /reviews/{id}")
    class GetById {

        @Test
        @DisplayName("returns 200 with review when found")
        void returnsReviewWhenFound() {
            when(reviewService.findById(1L)).thenReturn(Optional.of(review));

            ResponseEntity<Review> response = controller.getReviewById(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("returns 404 when review not found")
        void returns404WhenNotFound() {
            when(reviewService.findById(99L)).thenReturn(Optional.empty());

            ResponseEntity<Review> response = controller.getReviewById(99L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("POST /reviews")
    class Create {

        @Test
        @DisplayName("returns 201 with saved review")
        void returns201WithSavedReview() {
            Review input = new Review(0L, 1L, 1L, "Good read", 4);
            when(reviewService.save(input)).thenReturn(review);

            ResponseEntity<Review> response = controller.createReview(input);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody().getId()).isEqualTo(1L);
            verify(reviewService).save(input);
        }
    }

    @Nested
    @DisplayName("PUT /reviews/{id}")
    class Update {

        @Test
        @DisplayName("sets id from path and returns 200")
        void setsIdAndReturnsUpdated() {
            Review input = new Review(0L, 1L, 1L, "Updated", 3);
            Review updated = new Review(1L, 1L, 1L, "Updated", 3);
            when(reviewService.save(any(Review.class))).thenReturn(updated);

            ResponseEntity<Review> response = controller.updateReview(1L, input);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getId()).isEqualTo(1L);
            verify(reviewService).save(argThat(r -> r.getId().equals(1L)));
        }
    }

    @Nested
    @DisplayName("DELETE /reviews/{id}")
    class Delete {

        @Test
        @DisplayName("returns 200 and delegates to service")
        void delegatesToService() {
            doNothing().when(reviewService).deleteById(1L);

            ResponseEntity<Void> response = controller.deleteReview(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(reviewService).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("GET /reviews/byBook/{bookId}")
    class ByBook {

        @Test
        @DisplayName("returns 200 with reviews for book")
        void returnsReviewsForBook() {
            when(reviewService.findByBookId(1L)).thenReturn(List.of(review));

            ResponseEntity<List<Review>> response = controller.getReviewsByBook(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
        }

        @Test
        @DisplayName("returns 200 with empty list when no reviews for book")
        void returnsEmptyForBook() {
            when(reviewService.findByBookId(99L)).thenReturn(List.of());

            ResponseEntity<List<Review>> response = controller.getReviewsByBook(99L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }
    }

    @Nested
    @DisplayName("GET /reviews/byUser/{userId}")
    class ByUser {

        @Test
        @DisplayName("returns 200 with reviews for user")
        void returnsReviewsForUser() {
            when(reviewService.findByUserId(1L)).thenReturn(List.of(review));

            ResponseEntity<List<Review>> response = controller.getReviewsByUser(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
        }

        @Test
        @DisplayName("returns 200 with empty list when no reviews for user")
        void returnsEmptyForUser() {
            when(reviewService.findByUserId(99L)).thenReturn(List.of());

            ResponseEntity<List<Review>> response = controller.getReviewsByUser(99L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }
    }
}