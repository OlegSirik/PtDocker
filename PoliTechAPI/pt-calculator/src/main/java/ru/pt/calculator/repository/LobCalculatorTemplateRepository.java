package ru.pt.calculator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.pt.calculator.entity.LobCalculatorTemplateEntity;

import java.util.List;

public interface LobCalculatorTemplateRepository extends JpaRepository<LobCalculatorTemplateEntity, Long> {
    
    
    @Query("select c from LobCalculatorTemplateEntity c where c.tId = :tId and c.lobCode = :lobCode  order by c.id desc")
    List<LobCalculatorTemplateEntity> findByTIdAndLobCodeOrderByIdDesc(Long tId, String lobCode);
}
