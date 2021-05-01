package com.example.libraryeventsconsumer.repo;

import com.example.libraryeventsconsumer.model.LibraryEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibraryEventRepo extends JpaRepository<LibraryEvent, Integer> {
}
