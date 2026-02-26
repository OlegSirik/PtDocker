package ru.pt.api.service.db;

import java.util.List;
import java.util.Map;

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

   /**
    * Получить полный стравичник по названию
    * @param attributeCode
    * @return
    */
   Map<String, String> getRefData(String attributeCode);

   /**
    * Получить отфильтрованный по ключам справочник
    * @param attributeCode
    * @param filter
    * @return
    */
   Map<String, String> getRefData(String attributeCode, List<String> filter);
   

}
