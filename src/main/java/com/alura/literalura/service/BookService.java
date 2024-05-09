package com.alura.literalura.service;

import com.alura.literalura.model.Author;
import com.alura.literalura.model.Book;
import com.alura.literalura.repository.AuthorRepository;
import com.alura.literalura.repository.BookRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;


import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BookService implements CommandLineRunner {
  private final BookRepository bookRepository;
  private final AuthorRepository authorRepository;

  @Autowired
  public BookService(BookRepository bookRepository, AuthorRepository authorRepository) {
    this.bookRepository = bookRepository;
    this.authorRepository = authorRepository;
  }

  private static final HttpClient httpClient = HttpClient.newBuilder().build();

  public String getBookData(String searchQuery) throws Exception {
    HttpRequest request = HttpRequest.newBuilder()
            .uri(new URI("https://gutendex.com/books/?search=" + searchQuery))
            .GET()
            .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    // Check status code, if 200 then return response body
    if (response.statusCode() == 200) {
      return response.body();
    } else {
      throw new Exception("Failed to fetch book data: " + response.statusCode());
    }
  }

  private void insertBook() {
    Scanner teclado = new Scanner(System.in);
    System.out.println("Ingrese el título del libro a buscar:");
    String searchQuery = teclado.nextLine();

    try {
      String jsonData = getBookData(searchQuery);
      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode rootNode = objectMapper.readTree(jsonData);
      JsonNode resultsNode = rootNode.get("results").get(0); // Obtener solo el primer resultado

      // Mapear los datos del libro desde JSON a un objeto Book
      Book book = new Book();
      book.setId(resultsNode.get("id").asLong());
      book.setTitle(resultsNode.get("title").asText());

      // Obtener los idiomas del libro y limitar a los dos primeros
      JsonNode languagesNode = resultsNode.get("languages");
      StringBuilder languages = new StringBuilder();
      languages.append("[");
      for (int i = 0; i < languagesNode.size() && i < 2; i++) {
        languages.append(languagesNode.get(i).asText());
        if (i < 1 || i < languagesNode.size() - 1) {
          languages.append(", ");
        }
      }
      languages.append("]");
      book.setLanguages(languages.toString());

      book.setDownloadCount(resultsNode.get("download_count").asInt());

      // Mapear los datos del autor
      JsonNode authorsNode = resultsNode.get("authors").get(0);
      Author author = new Author();
      author.setAuthorName(authorsNode.get("name").asText());
      author.setBirthYear(authorsNode.get("birth_year").asInt());
      author.setDeathYear(authorsNode.get("death_year").asInt());

      // Guardar el libro y su autor en la base de datos
      bookRepository.save(book);
      authorRepository.save(author);

      System.out.println("Libro insertado exitosamente.");
    } catch (Exception e) {
      System.out.println("Error al insertar el libro: " + e.getMessage());
    }
  }


  private void listRegisteredBooks() {
    List<Book> books = bookRepository.findAll();
    if (books.isEmpty()) {
      System.out.println("No hay libros registrados en la base de datos.");
    } else {
      System.out.println("Libros registrados:");
      for (Book book : books) {
        System.out.println("Título: " + book.getTitle());
        System.out.println("Número de descargas: " + book.getDownloadCount());

        // Obtener los dos primeros idiomas y mostrarlos
        String languages = book.getLanguages().replace("[", "").replace("]", "");
        String[] languagesArray = languages.split(", ");
        if (languagesArray.length > 2) {
          System.out.println("Idiomas: " + languagesArray[0] + ", " + languagesArray[1] + ", ...");
        } else {
          System.out.println("Idiomas: " + languages);
        }

        System.out.println("--------------------");
      }
    }
  }


  private void listBooksByLanguage() {
    // Obtener todos los idiomas de los libros registrados
    List<Book> books = bookRepository.findAll();
    Set<String> availableLanguages = new HashSet<>();
    books.forEach(book -> {
      String[] languages = book.getLanguages().split(", ");
      for (String language : languages) {
        availableLanguages.add(language);
      }
    });

    // Mostrar los idiomas disponibles al usuario
    System.out.println("Idiomas disponibles:");
    int index = 1;
    for (String lang : availableLanguages) {
      System.out.println(index + ". " + lang);
      index++;
    }

    // Permitir al usuario elegir un idioma
    Scanner scanner = new Scanner(System.in);
    System.out.println("Seleccione el número del idioma que desea consultar:");
    int selectedLanguageIndex = scanner.nextInt();

    // Obtener el idioma seleccionado por el usuario
    String selectedLanguage = null;
    if (selectedLanguageIndex > 0 && selectedLanguageIndex <= availableLanguages.size()) {
      selectedLanguage = (String) availableLanguages.toArray()[selectedLanguageIndex - 1];
    } else {
      System.out.println("Opción no válida.");
      return;
    }

    final String finalSelectedLanguage = selectedLanguage; // Variable final

    // Contar los libros en el idioma seleccionado
    long count = books.stream()
            .filter(book -> book.getLanguages().contains(finalSelectedLanguage))
            .count();

    System.out.println("Cantidad de libros en " + finalSelectedLanguage + ": " + count);
  }



  private void listLivingAuthorsInYear() {
    System.out.println("Ingrese el año para consultar autores vivos:");
    Scanner teclado = new Scanner(System.in);
    int year = teclado.nextInt();

    List<Author> authors = authorRepository.findAll();
    List<Author> livingAuthors = authors.stream()
            .filter(author -> author.getBirthYear() <= year && (author.getDeathYear() == 0 || author.getDeathYear() >= year))
            .collect(Collectors.toList());

    if (livingAuthors.isEmpty()) {
      System.out.println("No hay autores vivos en el año " + year);
    } else {
      System.out.println("Autores vivos en el año " + year + ":");
      livingAuthors.forEach(author -> System.out.println(author.getAuthorName()));
    }
  }

  private void displayMenu() {
    Scanner teclado = new Scanner(System.in);
    boolean continuar = true;
    while (continuar) {
      System.out.println("Bienvenido al Catálogo de Libros!");
      System.out.println("Seleccione una opción:");
      System.out.println("1. Insertar libro");
      System.out.println("2. Listar libros por idioma");
      System.out.println("3. Listar autores vivos en determinado año");
      System.out.println("4. Listar libros registrados");
      System.out.println("5. Salir");

      int option = teclado.nextInt();
      switch (option) {
        case 1:
          insertBook();
          break;
        case 2:
          listBooksByLanguage();
          break;
        case 3:
          listLivingAuthorsInYear();
          break;
        case 4:
          listRegisteredBooks();
          break;
        case 5:
          System.out.println("Saliendo del programa. ¡Hasta luego!");
          System.exit(0);
          break;
        default:
          System.out.println("Opción no válida. Inténtelo de nuevo.");
          displayMenu();
      }
    }
  }
  @Override
  public void run(String... args) throws Exception {
    displayMenu();
  }
}