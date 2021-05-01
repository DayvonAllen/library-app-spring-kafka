package com.example.libraryeventsconsumer.repo;

import com.example.libraryeventsconsumer.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepo extends JpaRepository<Book, Integer> {
}
