package com.innitsocial.abcdish.content.repository;

import com.innitsocial.abcdish.content.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
}