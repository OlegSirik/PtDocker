package ru.pt.api.service.db;

import java.util.List;

public interface ReferenceDataService {
    
   /**
     * Получить список всех справочников
     * @return список всех справочников
     */
   List<String> getAllRefs();

   /**
     * Получить наименование по коду
     * @param attributeCode код атрибута
     * @param code код по справочнику
     * @return наименование из справочника
     */
   String getName(String attributeCode, String code);

}
