package com.flavio.libraryapi.service.impl;

import com.flavio.libraryapi.exception.BusinessException;
import com.flavio.libraryapi.model.entity.Book;
import com.flavio.libraryapi.model.repository.BookRepository;
import com.flavio.libraryapi.service.BookService;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BookServiceImpl implements BookService {

    private BookRepository repository;

    public BookServiceImpl(BookRepository repository) {
        this.repository = repository;
    }

    @Override
    public Book save(Book book) {
        if ( this.repository.existsByIsbn(book.getIsbn()) ) {
            throw new BusinessException("Isbn já cadastrado.");
        }
        return this.repository.save(book);
    }

    @Override
    public Optional<Book> getById(Long id) {
        return this.repository.findById(id);
    }

    @Override
    public void delete(Book book) {
        if ( book == null || book.getId() == null ) {
            throw new IllegalArgumentException("Book id cant be null.");
        }
        this.repository.delete(book);
    }

    @Override
    public Book update(Book book) {
        if ( book == null || book.getId() == null ) {
            throw new IllegalArgumentException("Book id cant be null.");
        }
        return this.repository.save(book);
    }

    @Override
    public Page<Book> find( Book filter, Pageable pageRequest ) {
        Example<Book> example = Example.of(filter,
                ExampleMatcher
                        .matching()
                        .withIgnoreCase()
                        .withIgnoreNullValues()
                        .withStringMatcher( ExampleMatcher.StringMatcher.CONTAINING )
        ) ;
        return repository.findAll(example, pageRequest);
    }
}
