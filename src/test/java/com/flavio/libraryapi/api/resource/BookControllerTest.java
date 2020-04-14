package com.flavio.libraryapi.api.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flavio.libraryapi.api.dto.BookDTO;
import com.flavio.libraryapi.exception.BusinessException;
import com.flavio.libraryapi.model.entity.Book;
import com.flavio.libraryapi.service.BookService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest
@AutoConfigureMockMvc
public class BookControllerTest {

    static String BOOK_API = "/api/books";

    @Autowired
    MockMvc mvc;

    @MockBean
    BookService service;

    @Test
    @DisplayName("Deve criar um livro com sucesso.")
    public void createBookTest() throws Exception {

        BookDTO dto = createNewBook();

        Book savedBook = Book.builder().id(10l).author("Artur").title("As aventuras").isbn("001").build();

        BDDMockito.given(service.save(Mockito.any(Book.class))).willReturn(savedBook);

        String json = new ObjectMapper().writeValueAsString(dto);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
                .perform(request)
                .andExpect( status().isCreated() )
                .andExpect( jsonPath("id").value(10l) )
                .andExpect( jsonPath("title").value( dto.getTitle() ) )
                .andExpect( jsonPath("author").value( dto.getAuthor() ) )
                .andExpect( jsonPath("isbn").value( dto.getIsbn() ) )
        ;
    }

    @Test
    @DisplayName("Deve lançar erro de validação quando não houver dados suficientes para criação do livro.")
    public void createInvalidBookTest() throws Exception {

        String json = new ObjectMapper().writeValueAsString(new BookDTO());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect( status().isBadRequest() )
                .andExpect( jsonPath("errors", Matchers.hasSize(3) ) );
        ;
    }

    @Test
    @DisplayName("Deve lançar erro ao tentar cadastrar um livro com isbn já utilizado por outro livro.")
    public void createBookWithDuplicatedIsbn() throws Exception {

        BookDTO dto = createNewBook();

        String json = new ObjectMapper().writeValueAsString(dto);

        String mensagemErro = "Isbn já cadastrado.";
        BDDMockito.given(service.save(Mockito.any(Book.class)))
                .willThrow(new BusinessException(mensagemErro));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect( status().isBadRequest() )
                .andExpect( jsonPath("errors", Matchers.hasSize(1) ) )
                .andExpect( jsonPath("errors[0]").value(mensagemErro) );
        ;
    }

    @Test
    @DisplayName("Deve obter informacoes de um livro.")
    public  void getBookDetailsTest() throws Exception {

        // cenario (given BDD)
        Long id = 1l;

        Book book = Book.builder()
                .id(id)
                .author(createNewBook().getAuthor())
                .title(createNewBook().getTitle())
                .isbn(createNewBook().getIsbn())
                .build();

        BDDMockito.given(service.getById(id)).willReturn(Optional.of(book));

        // execucao (when)
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON);

        // verificacao
        mvc
                .perform(request)
                .andExpect( status().isOk() )
                .andExpect( jsonPath("id").value(id) )  // espera que retorne o ID ja populado do saveBook
                .andExpect( jsonPath("title").value( createNewBook().getTitle() ) )
                .andExpect( jsonPath("author").value( createNewBook().getAuthor() ) )
                .andExpect( jsonPath("isbn").value( createNewBook().getIsbn() ) )
        ;
    }

    @Test
    @DisplayName("Deve retornar resource not found quando o livro procurado não existir.")
    public void  bookNotFoundTest() throws Exception {

        // cenario (given BDD)
        BDDMockito.given( service.getById(Mockito.anyLong()) ).willReturn(Optional.empty());

        // execucao
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + 1))
                .accept(MediaType.APPLICATION_JSON);

        // verificacao
        mvc
                .perform(request)
                .andExpect( status().isNotFound() )
        ;
    }

    @Test
    @DisplayName("Deve deletar um livro")
    public void deleteBookTest() throws Exception {

        // cenario (given BDD)
        BDDMockito.given( service.getById(Mockito.anyLong()) ).willReturn(Optional.of(Book.builder().id(1l).build()));

        // execucao
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/" + 1))
         ;

        // verificacao
        mvc
                .perform(request)
                .andExpect( status().isNoContent() )
        ;
    }

    @Test
    @DisplayName("Deve retornar not found quando não encontrar o livro para deletar.")
    public void deleteInexistentBookTest() throws Exception {

        // cenario (given BDD)
        BDDMockito.given( service.getById(Mockito.anyLong()) ).willReturn(Optional.empty());

        // execucao
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/" + 1))
                ;

        // verificacao
        mvc
                .perform(request)
                .andExpect( status().isNotFound() )
        ;
    }

    @Test
    @DisplayName("Deve atualizar um livro.")
    public void updateBookTest() throws  Exception {

        // cenario (given BDD)
        Long id = 1l;
        String json = new ObjectMapper().writeValueAsString(createNewBook());

        Book updatingBook = Book.builder().id(1l).author("some author").title("some title").isbn("321").build();
        BDDMockito.given( service.getById(id) ).willReturn( Optional.of(updatingBook) );
        Book updatedBook = Book.builder().id(id).author("Artur").title("As aventuras").isbn("321").build();
        BDDMockito.given( service.update(updatingBook) ).willReturn(updatedBook);

        // execucao
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + 1))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);
        ;

        // verificacao
        mvc.perform(request)
                .andExpect( status().isOk() )
                .andExpect( jsonPath("id").value(id) )
                .andExpect( jsonPath("title").value( createNewBook().getTitle() ) )
                .andExpect( jsonPath("author").value( createNewBook().getAuthor() ) )
                .andExpect( jsonPath("isbn").value( "321" ) )
        ;
    }

    @Test
    @DisplayName("Deve retornar 404 ao tentar atualizar um livro inexistente.")
    public void updateInexistentBookTest() throws Exception {

        // cenario (given BDD)
        String json = new ObjectMapper().writeValueAsString(createNewBook());

        BDDMockito.given( service.getById(Mockito.anyLong()) )
                .willReturn(Optional.empty());

        // execucao
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + 1))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);
        ;

        // verificacao
        mvc
                .perform(request)
                .andExpect( status().isNotFound() )
        ;
    }

    @Test
    @DisplayName("Deve filtrar livros.")
    public void findBooksTest() throws Exception {

        // cenario (given BDD)
        Long id = 1l;

        Book book = Book.builder()
                .id(id)
                .author( createNewBook().getAuthor() )
                .title( createNewBook().getTitle() )
                .isbn( createNewBook().getIsbn() )
                .build();

        BDDMockito.given( service.find(Mockito.any(Book.class), Mockito.any(Pageable.class)) )
                .willReturn( new PageImpl<Book>( Arrays.asList(book), PageRequest.of( 0, 100 ), 1 ) );

        String queryString = String.format("?title=%s&author=%s&page=0&size=100", book.getTitle(), book.getAuthor());

        // execucao
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat(queryString))
                .accept(MediaType.APPLICATION_JSON)
        ;

        // verificacao
        mvc
            .perform( request )
            .andExpect( status().isOk() )
            .andExpect( jsonPath("content", Matchers.hasSize(1)) )
            .andExpect( jsonPath("totalElements").value( 1) )
            .andExpect( jsonPath("pageable.pageSize").value( 100) )
            .andExpect( jsonPath("pageable.pageNumber").value( 0) )
        ;
    }

    private BookDTO createNewBook() {
        return BookDTO.builder().author("Artur").title("As aventuras").isbn("001").build();
    }

}
