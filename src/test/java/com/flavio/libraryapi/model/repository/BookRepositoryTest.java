package com.flavio.libraryapi.model.repository;

import com.flavio.libraryapi.model.entity.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class BookRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    BookRepository repository;

    @Test
    @DisplayName("Deve retornar verdadeiro quando existir um livro na base com o ISBN informado.")
    public void returnTrueWhenIsbnExists() {

        // cenario
        String isbn = "123";
        Book book = createNewBook(isbn);
        entityManager.persist(book);

        // execucao
        boolean exists = repository.existsByIsbn(isbn);

        // verificacao
        assertThat(exists).isTrue();
    }

    private Book createNewBook(String isbn) {
        return Book.builder().author("Fulano").title("Aventuras").isbn(isbn).build();
    }

    @Test
    @DisplayName("Deve retornar false quando não existir um livro na base com o ISBN informado.")
    public void returnFalseWhenIsbnDoesntExists() {

        // cenario
        String isbn = "123";

        // execucao
        boolean exists = repository.existsByIsbn(isbn);

        // verificacao
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Deve obter um livro por Id.")
    public void findByIdTest() {

        // cenario
        String isbn = "123";
        Book book = createNewBook(isbn);
        entityManager.persist(book);

        // execucao
        Optional<Book> foundBook = repository.findById(book.getId());

        // verificacao
        assertThat( foundBook.isPresent() ).isTrue();

    }

    @Test
    @DisplayName("Deve salvar um livro.")
    public void saveBookTest() {

        // cenario
        Book book = createNewBook("123");

        // execucao
        Book savedBook = repository.save(book);

        // verificacao
        assertThat( savedBook.getId() ).isNotNull();

    }

    @Test
    @DisplayName("Deve deletar um livro.")
    public void deleteBookTest() {

        // cenario
        String isbn = "123";
        Book book = createNewBook(isbn);
        entityManager.persist(book);
        Book foundBook = entityManager.find( Book.class, book.getId() );

        // execucao
        repository.delete(foundBook);

        // verificacao
        Book deleteBook = entityManager.find( Book.class, book.getId() );
        assertThat( deleteBook ).isNull();
    }

}
