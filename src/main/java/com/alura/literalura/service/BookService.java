package com.alura.literalura.service;

import com.alura.literalura.model.Book;
import com.alura.literalura.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

  @Autowired
  private BookRepository bookRepository;

  public Book save(Book book) {
    return bookRepository.save(book);
  }

  public List<Book> findAll() {
    return bookRepository.findAll();
  }
}
