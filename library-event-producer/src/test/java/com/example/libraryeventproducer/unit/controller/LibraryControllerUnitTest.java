package com.example.libraryeventproducer.unit.controller;

import com.example.libraryeventproducer.controller.LibraryController;
import com.example.libraryeventproducer.domain.Book;
import com.example.libraryeventproducer.domain.LibraryEvent;
import com.example.libraryeventproducer.producer.LibraryEventProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LibraryController.class)
@AutoConfigureMockMvc
public class LibraryControllerUnitTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    // mockito creates mock bean of this dependency
    @MockBean
    LibraryEventProducer libraryEventProducer;

    @Test
    void postLibraryEvent() throws Exception {
        Book book = Book.builder().bookId(1).bookName("Moby Dick").bookAuthor("John Doe").build();
        LibraryEvent libraryEvent = LibraryEvent.builder().libraryEventId(1).book(book).build();

        // mockito is mocking the behavior of this method
        when(libraryEventProducer.sendLibraryEvent_Approach2(isA(LibraryEvent.class))).thenReturn(null);

        mockMvc.perform(post("/api/v1/library/event").content(objectMapper.writeValueAsString(libraryEvent))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated());
    }

    @Test
    void putLibraryEvent() throws Exception {
        Book book = Book.builder().bookId(1).bookName("Moby Dick").bookAuthor("John Doe").build();
        LibraryEvent libraryEvent = LibraryEvent.builder().libraryEventId(1).book(book).build();

        // mockito is mocking the behavior of this method
        when(libraryEventProducer.sendLibraryEvent_Approach2(isA(LibraryEvent.class))).thenReturn(null);

        mockMvc.perform(put("/api/v1/library/event").content(objectMapper.writeValueAsString(libraryEvent))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void postLibraryEvent_4xx() throws Exception {
        Book book = Book.builder().bookId(null).bookName(null).bookAuthor("John Doe").build();

        LibraryEvent libraryEvent = LibraryEvent.builder().libraryEventId(1).book(book).build();

        // mockito is mocking the behavior of this method
        when(libraryEventProducer.sendLibraryEvent_Approach2(isA(LibraryEvent.class))).thenReturn(null);

        String expectedErrorMessage = "book.bookId - must not be null,book.bookName - must not be blank";
        mockMvc.perform(post("/api/v1/library/event").content(objectMapper.writeValueAsString(libraryEvent))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
        .andExpect(content().string(expectedErrorMessage));
    }

    @Test
    void putLibraryEvent_4xx() throws Exception {
        Book book = Book.builder().bookId(1).bookName("Moby Dick").bookAuthor("John Doe").build();

        LibraryEvent libraryEvent = LibraryEvent.builder().libraryEventId(null).book(book).build();

        // mockito is mocking the behavior of this method
        when(libraryEventProducer.sendLibraryEvent_Approach2(isA(LibraryEvent.class))).thenReturn(null);

        String expectedErrorMessage = "runtime exception";
        mockMvc.perform(put("/api/v1/library/event").content(objectMapper.writeValueAsString(libraryEvent))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(expectedErrorMessage));
    }
}
