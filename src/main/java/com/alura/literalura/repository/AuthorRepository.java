package com.alura.literalura.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.alura.literalura.model.Author;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Integer> {
}