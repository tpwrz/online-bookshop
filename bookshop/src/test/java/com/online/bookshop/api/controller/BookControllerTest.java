package com.online.bookshop.api.controller;

import com.online.bookshop.application.service.BookService;
import com.online.bookshop.domain.model.Book;
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
@DisplayName("BookController — unit tests")
class BookControllerTest {

    @Mock
    private BookService service;

    @InjectMocks
    private BookController controller;

    private Book book;

    @BeforeEach
    void setUp() {
        book = new Book();
        book.setId(1L);
        book.setTitle("Dune");
        book.setAuthorId(1L);
        book.setGenreId(1L);
        book.setPrice(14.99);
    }

    // ---------------------------------------------------------------- getAll

    @Nested
    @DisplayName("GET /books")
    class GetAll {

        @Test
        @DisplayName("returns 200 with list of books")
        void returnsAllBooks() {
            when(service.findAll()).thenReturn(List.of(book));

            ResponseEntity<List<Book>> response = controller.getAll();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).getTitle()).isEqualTo("Dune");
        }

        @Test
        @DisplayName("returns 200 with empty list when no books")
        void returnsEmptyList() {
            when(service.findAll()).thenReturn(List.of());

            assertThat(controller.getAll().getBody()).isEmpty();
        }
    }

    // --------------------------------------------------------------- getById

    @Nested
    @DisplayName("GET /books/{id}")
    class GetById {

        @Test
        @DisplayName("returns 200 when found")
        void returnsBookWhenFound() {
            when(service.findById(1L)).thenReturn(Optional.of(book));

            ResponseEntity<Book> response = controller.getById(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("returns 404 when not found")
        void returns404WhenNotFound() {
            when(service.findById(99L)).thenReturn(Optional.empty());

            assertThat(controller.getById(99L).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    // ---------------------------------------------------------------- create

    @Nested
    @DisplayName("POST /books")
    class Create {

        @Test
        @DisplayName("returns 201 with Location header and saved book")
        void returns201WithBook() {
            Book input = new Book();
            input.setTitle("Foundation");
            when(service.save(input)).thenReturn(book);

            ResponseEntity<Book> response = controller.create(input);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getHeaders().getLocation().toString()).isEqualTo("/books/1");
            assertThat(response.getBody().getId()).isEqualTo(1L);
        }
    }

    // ---------------------------------------------------------------- update

    @Nested
    @DisplayName("PUT /books/{id}")
    class Update {

        @Test
        @DisplayName("returns 200 with updated book when found")
        void returnsUpdatedBook() {
            when(service.findById(1L)).thenReturn(Optional.of(book));
            when(service.save(any(Book.class))).thenReturn(book);

            Book input = new Book();
            input.setTitle("Dune Messiah");
            ResponseEntity<Book> response = controller.update(1L, input);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(service).save(argThat(b -> b.getId().equals(1L)));
        }

        @Test
        @DisplayName("returns 404 when book not found")
        void returns404WhenNotFound() {
            when(service.findById(99L)).thenReturn(Optional.empty());

            ResponseEntity<Book> response = controller.update(99L, new Book());

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            verify(service, never()).save(any());
        }
    }

    // ---------------------------------------------------------------- delete

    @Nested
    @DisplayName("DELETE /books/{id}")
    class Delete {

        @Test
        @DisplayName("returns 200 and delegates to service")
        void delegatesToService() {
            doNothing().when(service).deleteById(1L);

            ResponseEntity<Void> response = controller.delete(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(service).deleteById(1L);
        }
    }

    // ---------------------------------------------------------------- count

    @Nested
    @DisplayName("GET /books/count")
    class Count {

        @Test
        @DisplayName("returns 200 with book count")
        void returnsCount() {
            when(service.bookCount()).thenReturn(42);

            ResponseEntity<Integer> response = controller.countBooks();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(42);
        }
    }

    // --------------------------------------------------------------- byGenre

    @Nested
    @DisplayName("GET /books/byGenre")
    class ByGenre {

        @Test
        @DisplayName("returns 200 with books for genre")
        void returnsBooksForGenre() {
            when(service.findByGenre("Science Fiction")).thenReturn(List.of(book));

            ResponseEntity<List<Book>> response = controller.findByGenre("Science Fiction");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
        }

        @Test
        @DisplayName("returns 200 with empty list when no books for genre")
        void returnsEmptyForGenre() {
            when(service.findByGenre("Unknown")).thenReturn(List.of());

            assertThat(controller.findByGenre("Unknown").getBody()).isEmpty();
        }
    }

    // -------------------------------------------------------------- byAuthor

    @Nested
    @DisplayName("GET /books/byAuthor")
    class ByAuthor {

        @Test
        @DisplayName("returns 200 with books for author")
        void returnsBooksForAuthor() {
            when(service.findByAuthor("Herbert")).thenReturn(List.of(book));

            ResponseEntity<List<Book>> response = controller.findByAuthor("Herbert");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
        }
    }

    // ---------------------------------------------------------------- byYear

    @Nested
    @DisplayName("GET /books/byYear")
    class ByYear {

        @Test
        @DisplayName("returns 200 with books for year")
        void returnsBooksForYear() {
            when(service.findByYear(1965)).thenReturn(List.of(book));

            ResponseEntity<List<Book>> response = controller.findByYear(1965);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
        }
    }

    // ---------------------------------------------------------------- search

    @Nested
    @DisplayName("GET /books/search")
    class Search {

        @Test
        @DisplayName("returns 200 with matching books")
        void returnsMatchingBooks() {
            when(service.findByTitleContaining("Du")).thenReturn(List.of(book));

            ResponseEntity<List<Book>> response = controller.findByTitleContaining("Du");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
        }

        @Test
        @DisplayName("returns 200 with empty list when no match")
        void returnsEmptyOnNoMatch() {
            when(service.findByTitleContaining("xyz")).thenReturn(List.of());

            assertThat(controller.findByTitleContaining("xyz").getBody()).isEmpty();
        }
    }

    // --------------------------------------------------------- sortedByPrice

    @Nested
    @DisplayName("GET /books/sortedByPrice")
    class SortedByPrice {

        @Test
        @DisplayName("returns 200 with books sorted by price")
        void returnsSortedByPrice() {
            Book cheap = new Book();
            cheap.setId(2L);
            cheap.setPrice(5.99);
            when(service.sortByPrice()).thenReturn(List.of(cheap, book));

            ResponseEntity<List<Book>> response = controller.sortByPrice();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().get(0).getPrice()).isEqualTo(5.99);
        }
    }

    // --------------------------------------------------------------- reviews

    @Nested
    @DisplayName("GET /books/{id}/reviews")
    class Reviews {

        @Test
        @DisplayName("returns 200 with reviews for book")
        void returnsReviewsForBook() {
            Review review = new Review(1L, 1L, 1L, "Amazing!", 5);
            when(service.getReviewsByBookId(1L)).thenReturn(List.of(review));

            ResponseEntity<List<Review>> response = controller.getReviewsByBookId(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).getReviewMessage()).isEqualTo("Amazing!");
        }

        @Test
        @DisplayName("returns 200 with empty list when no reviews")
        void returnsEmptyWhenNoReviews() {
            when(service.getReviewsByBookId(99L)).thenReturn(List.of());

            assertThat(controller.getReviewsByBookId(99L).getBody()).isEmpty();
        }
    }

    // -------------------------------------------------------- averageRating

    @Nested
    @DisplayName("GET /books/{id}/averageRating")
    class AverageRating {

        @Test
        @DisplayName("returns 200 with average rating when present")
        void returnsAverageRating() {
            when(service.getAverageRatingByBookId(1L)).thenReturn(Optional.of(4.5));

            ResponseEntity<Double> response = controller.getAverageRating(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(4.5);
        }

        @Test
        @DisplayName("returns 404 when no reviews exist")
        void returns404WhenNoRating() {
            when(service.getAverageRatingByBookId(99L)).thenReturn(Optional.empty());

            assertThat(controller.getAverageRating(99L).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    // ------------------------------------------------- sortedByAverageRating

    @Nested
    @DisplayName("GET /books/sortedByAverageRating")
    class SortedByAverageRating {

        @Test
        @DisplayName("returns 200 with books sorted by average rating")
        void returnsSortedByRating() {
            when(service.getBooksSortedByAverageRating()).thenReturn(List.of(book));

            ResponseEntity<List<Book>> response = controller.getBooksSortedByAverageRating();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
        }
    }

    // ---------------------------------------------------- reviewsByRating

    @Nested
    @DisplayName("GET /books/{id}/reviewsByRating")
    class ReviewsByRating {

        @Test
        @DisplayName("returns 200 with reviews filtered by rating")
        void returnsReviewsByRating() {
            Review review = new Review(1L, 1L, 1L, "Perfect", 5);
            when(service.getReviewsByBookIdAndRating(1L, 5.0)).thenReturn(List.of(review));

            ResponseEntity<List<Review>> response = controller.getReviewsByRating(1L, 5.0);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).getReviewRating()).isEqualTo(5);
        }

        @Test
        @DisplayName("returns 200 with empty list when no reviews match rating")
        void returnsEmptyWhenNoMatch() {
            when(service.getReviewsByBookIdAndRating(1L, 1.0)).thenReturn(List.of());

            assertThat(controller.getReviewsByRating(1L, 1.0).getBody()).isEmpty();
        }
    }
}