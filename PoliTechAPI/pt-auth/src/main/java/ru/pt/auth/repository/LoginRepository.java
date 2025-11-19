package ru.pt.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.pt.auth.entity.LoginEntity;

import java.util.Optional;

public interface LoginRepository extends JpaRepository<LoginEntity, Long> {

    @Query("SELECT l FROM LoginEntity l WHERE l.userLogin = :userLogin")
    Optional<LoginEntity> findByUserLogin(@Param("userLogin") String userLogin);

}
