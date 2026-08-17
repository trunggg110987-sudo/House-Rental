package vn.codegym.house_rental.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.codegym.house_rental.model.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
