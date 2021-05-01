package com.example.libraryeventsconsumer.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
public class LibraryEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer libraryEventId;

    // propertyName that this is mappedBy in the book entity
    @OneToOne(mappedBy = "libraryEvent")
    // we need to exclude because it will cause a circular reference which will lead to a stackOverflowError
    @ToString.Exclude
    private Book book;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LibraryEventType libraryEventType;

}
