package ru.pt.api.service.auth;

import java.util.List;
import ru.pt.api.dto.auth.Account;

public interface AccountHierarchyProvider {
    
    /*
    * Проверяет что parent для child 
    */
    boolean isParent(Long parent, Long child);

    /*
    * Список всех узлов от account до корня
    */
    List<Account> getPathToRoot(Long accountId);
}
