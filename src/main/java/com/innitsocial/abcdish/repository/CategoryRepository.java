package com.innitsocial.abcdish.repository;

import com.innitsocial.abcdish.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
}