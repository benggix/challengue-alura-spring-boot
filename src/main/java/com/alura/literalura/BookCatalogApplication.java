package com.alura.literalura;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.alura.literalura.service.BookService;

@SpringBootApplication
public class BookCatalogApplication implements CommandLineRunner {

  private final BookService bookService;

  public BookCatalogApplication(BookService bookService) {
    this.bookService = bookService;
  }

  public static void main(String[] args) {
    SpringApplication.run(BookCatalogApplication.class, args);
  }

  @Override
  public void run(String... args) {
    // Llamar al método run de BookService
    try {
      bookService.run();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
