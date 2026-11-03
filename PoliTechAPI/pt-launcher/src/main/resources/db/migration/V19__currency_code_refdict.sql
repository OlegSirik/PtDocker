-- Справочник валют (ISO 4217 letter code + наименование по-русски).
-- Код справочника currencyCode — для varList у pl_currencyCode / currencyCode.

INSERT INTO pt_refdicts (tid, code, name)
SELECT DISTINCT src.tid, 'currencyCode', 'Валюты'
FROM pt_refdicts src
WHERE NOT EXISTS (
    SELECT 1
    FROM pt_refdicts d
    WHERE d.tid = src.tid
      AND d.code = 'currencyCode'
);

INSERT INTO pt_refdata (tid, ref_code, md_code, md_name)
SELECT d.tid, 'currencyCode', v.md_code, v.md_name
FROM pt_refdicts d
CROSS JOIN (
    VALUES
        ('RUB', 'Российский рубль'),
        ('USD', 'Доллар США'),
        ('EUR', 'Евро'),
        ('CNY', 'Китайский юань'),
        ('GBP', 'Фунт стерлингов'),
        ('CHF', 'Швейцарский франк'),
        ('JPY', 'Японская иена'),
        ('KZT', 'Казахстанский тенге'),
        ('BYN', 'Белорусский рубль'),
        ('AMD', 'Армянский драм'),
        ('AZN', 'Азербайджанский манат'),
        ('KGS', 'Киргизский сом'),
        ('TJS', 'Таджикский сомони'),
        ('UZS', 'Узбекский сум'),
        ('TRY', 'Турецкая лира'),
        ('AED', 'Дирхам ОАЭ'),
        ('INR', 'Индийская рупия'),
        ('KRW', 'Южнокорейская вона'),
        ('HKD', 'Гонконгский доллар'),
        ('SGD', 'Сингапурский доллар'),
        ('CAD', 'Канадский доллар'),
        ('AUD', 'Австралийский доллар'),
        ('NZD', 'Новозеландский доллар'),
        ('SEK', 'Шведская крона'),
        ('NOK', 'Норвежская крона'),
        ('DKK', 'Датская крона'),
        ('PLN', 'Польский злотый'),
        ('CZK', 'Чешская крона'),
        ('HUF', 'Венгерский форинт'),
        ('RON', 'Румынский лей'),
        ('BGN', 'Болгарский лев'),
        ('UAH', 'Украинская гривна'),
        ('GEL', 'Грузинский лари'),
        ('XDR', 'СДР (специальные права заимствования)')
) AS v(md_code, md_name)
WHERE d.code = 'currencyCode'
  AND NOT EXISTS (
      SELECT 1
      FROM pt_refdata r
      WHERE r.tid = d.tid
        AND r.ref_code = 'currencyCode'
        AND r.md_code = v.md_code
  );
