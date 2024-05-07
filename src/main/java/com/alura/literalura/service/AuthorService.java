package com.alura.literalura.service;

import com.alura.literalura.model.Author;
import com.alura.literalura.repository.AuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthorService {

  @Autowired
  private AuthorRepository authorRepository;

  public Author save(Author author) {
    return authorRepository.save(author);
  }

  public List<Author> findAll() {
    return authorRepository.findAll();
  }
}

