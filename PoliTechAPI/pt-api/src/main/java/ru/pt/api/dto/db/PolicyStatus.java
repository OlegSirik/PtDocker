package ru.pt.api.dto.db;

public enum PolicyStatus {

    QUOTE,  // — можно редактировать и пересчитывать;
    UNDERWRITING, // передано на ручное рассмотрение;
    ISSUED, // договор выпущен.
    PAID // оплачен.

}
