package com.online.bookshop.api.controller;

import com.online.bookshop.application.service.GenreService;
import com.online.bookshop.domain.model.Genre;
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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GenreController — unit tests")
class GenreControllerTest {

    @Mock
    private GenreService service;

    @InjectMocks
    private GenreController controller;

    private Genre genre;

    @BeforeEach
    void setUp() {
        genre = new Genre(1L, "Science Fiction");
    }

    // ---------------------------------------------------------------- getAll

    @Nested
    @DisplayName("GET /genres")
    class GetAll {

        @Test
        @DisplayName("returns 200 with list of genres")
        void returnsAllGenres() {
            when(service.findAll()).thenReturn(List.of(genre));

            ResponseEntity<List<Genre>> response = controller.getAll();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).getName()).isEqualTo("Science Fiction");
        }

        @Test
        @DisplayName("returns 200 with empty list when no genres")
        void returnsEmptyList() {
            when(service.findAll()).thenReturn(List.of());

            ResponseEntity<List<Genre>> response = controller.getAll();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }
    }

    // --------------------------------------------------------------- getById

    @Nested
    @DisplayName("GET /genres/{id}")
    class GetById {

        @Test
        @DisplayName("returns 200 with genre when found")
        void returnsGenreWhenFound() {
            when(service.findById(1L)).thenReturn(Optional.of(genre));

            ResponseEntity<Genre> response = controller.getById(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(1L);
            assertThat(response.getBody().getName()).isEqualTo("Science Fiction");
        }

        @Test
        @DisplayName("returns 404 when genre not found")
        void returns404WhenNotFound() {
            when(service.findById(99L)).thenReturn(Optional.empty());

            ResponseEntity<Genre> response = controller.getById(99L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    // --------------------------------------------------------- getByLastName

    @Nested
    @DisplayName("GET /genres/search")
    class GetByLastName {

        @Test
        @DisplayName("returns 200 with matching genres")
        void returnsMatchingGenres() {
            when(service.findByLastName("Sci")).thenReturn(List.of(genre));

            ResponseEntity<List<Genre>> response = controller.getByLastName("Sci");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            verify(service).findByLastName("Sci");
        }

        @Test
        @DisplayName("returns 200 with empty list when no matches")
        void returnsEmptyOnNoMatch() {
            when(service.findByLastName("xyz")).thenReturn(List.of());

            ResponseEntity<List<Genre>> response = controller.getByLastName("xyz");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }
    }

    // ---------------------------------------------------------------- create

    @Nested
    @DisplayName("POST /genres")
    class Create {

        @Test
        @DisplayName("returns 200 with saved genre")
        void returnsSavedGenre() {
            Genre input = new Genre(0L, "Fantasy");
            Genre saved = new Genre(2L, "Fantasy");
            when(service.save(input)).thenReturn(saved);

            ResponseEntity<Genre> response = controller.create(input);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getId()).isEqualTo(2L);
            assertThat(response.getBody().getName()).isEqualTo("Fantasy");
            verify(service).save(input);
        }
    }

    // ---------------------------------------------------------------- update

    @Nested
    @DisplayName("PUT /genres/{id}")
    class Update {

        @Test
        @DisplayName("sets id from path and returns 200 with updated genre")
        void setsIdAndReturnsUpdated() {
            Genre input = new Genre(0L, "Updated Name");
            Genre updated = new Genre(1L, "Updated Name");
            when(service.save(any(Genre.class))).thenReturn(updated);

            ResponseEntity<Genre> response = controller.update(1L, input);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getId()).isEqualTo(1L);
            assertThat(response.getBody().getName()).isEqualTo("Updated Name");
            // проверяем что id был проставлен перед save
            verify(service).save(argThat(g -> g.getId().equals(1L)));
        }
    }

    // ---------------------------------------------------------------- delete

    @Nested
    @DisplayName("DELETE /genres/{id}")
    class Delete {

        @Test
        @DisplayName("returns 200 and delegates to service")
        void delegatesToService() {
            doNothing().when(service).deleteById(1L);

            ResponseEntity<Void> response = controller.delete(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(service, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("does not throw when genre does not exist")
        void doesNotThrowForMissingId() {
            doNothing().when(service).deleteById(99L);

            assertThatCode(() -> controller.delete(99L)).doesNotThrowAnyException();
        }
    }
}