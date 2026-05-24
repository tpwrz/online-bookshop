package com.online.bookshop.api.controller;

import com.online.bookshop.application.service.PersonService;
import com.online.bookshop.domain.model.Person;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PersonController — unit tests")
class PersonControllerTest {

    @Mock
    private PersonService service;

    @InjectMocks
    private PersonController controller;

    private Person person;

    @BeforeEach
    void setUp() {
        person = new Person(1L, "John", "MiddleName", "Doe",
                "test", "12345567", LocalDate.of(2020, 2, 2));
    }

    // ---------------------------------------------------------------- getAll

    @Nested
    @DisplayName("GET /persons")
    class GetAll {

        @Test
        @DisplayName("returns 200 with list of persons")
        void returnsAllPersons() {
            when(service.findAll()).thenReturn(List.of(person));

            ResponseEntity<List<Person>> response = controller.getAll();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).getLastName()).isEqualTo("Doe");
        }

        @Test
        @DisplayName("returns 200 with empty list when no persons")
        void returnsEmptyList() {
            when(service.findAll()).thenReturn(List.of());

            assertThat(controller.getAll().getBody()).isEmpty();
        }
    }

    // --------------------------------------------------------------- getById

    @Nested
    @DisplayName("GET /persons/{id}")
    class GetById {

        @Test
        @DisplayName("returns 200 when found")
        void returnsPersonWhenFound() {
            when(service.findById(1L)).thenReturn(Optional.of(person));

            ResponseEntity<Person> response = controller.getById(1L);

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

    // --------------------------------------------------------- getByLastName

    @Nested
    @DisplayName("GET /persons/search")
    class Search {

        @Test
        @DisplayName("returns 200 with matching persons")
        void returnsMatching() {
            when(service.findByLastName("Doe")).thenReturn(List.of(person));

            ResponseEntity<List<Person>> response = controller.getByLastName("Doe");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            verify(service).findByLastName("Doe");
        }

        @Test
        @DisplayName("returns 200 with empty list when no match")
        void returnsEmptyOnNoMatch() {
            when(service.findByLastName("xyz")).thenReturn(List.of());

            assertThat(controller.getByLastName("xyz").getBody()).isEmpty();
        }
    }

    // ---------------------------------------------------------------- create

    @Nested
    @DisplayName("POST /persons")
    class Create {

        @Test
        @DisplayName("returns 200 with saved person")
        void returnsSavedPerson() {
            Person input = new Person(1L, "test", "test", "test",
                    "test", "123456789", LocalDate.of(2020, 2, 2));
            input.setFirstName("Jane");
            when(service.save(input)).thenReturn(person);

            ResponseEntity<Person> response = controller.create(input);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getId()).isEqualTo(1L);
            verify(service).save(input);
        }
    }

    // ---------------------------------------------------------------- update

    @Nested
    @DisplayName("PUT /persons/{id}")
    class Update {

        @Test
        @DisplayName("sets id from path and returns 200")
        void setsIdAndReturnsUpdated() {
            Person input = new Person(1L, "test", "test", "test",
                    "test", "123456789", LocalDate.of(2020, 2, 2));
            input.setFirstName("Updated");
            when(service.save(any(Person.class))).thenReturn(person);

            ResponseEntity<Person> response = controller.update(1L, input);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(service).save(argThat(p -> p.getId().equals(1L)));
        }
    }

    // ---------------------------------------------------------------- delete

    @Nested
    @DisplayName("DELETE /persons/{id}")
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
}