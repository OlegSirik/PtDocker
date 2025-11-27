## Таблица: acc_accounts 
#### Назаначение: Хранение информации об аккаунтах от имени которого работают пользователи
Комментарий: К одному account может быть привязано несколько логинов (учётных записей людей). У каждого account определяется набор ролей И прав И УЗ, которые определяют:
<ul>
<li dir="auto">какие действия доступны (задается через таблицу acc_products_roles);</li>
<li dir="auto">какие продукты можно "продовать" (задается через таблицу acc_products_roles);</li>
<li dir="auto">какая роль (задается через таблицу acc_account_logins)</li>
</ul>

#### Стркутра таблицы 

<table style="height: 313px; width: 669px;">
<thead>
<tr style="height: 36px;">
<th style="text-align: center; height: 36px; width: 80.125px;">Название поля</th>
<th style="text-align: center; height: 36px; width: 44.2273px;">Ключ</th>
<th style="text-align: center; height: 36px; width: 107.489px;">Тип поля</th>
<th style="text-align: center; height: 36px; width: 129.25px;">Обязательность</th>
<th style="text-align: center; width: 161.557px; height: 36px;">Пример</th>
<th style="text-align: center; height: 36px; width: 113.989px;">Описание</th>
</tr>
</thead>
<tbody>
<tr style="height: 36px;">
<td style="text-align: center; height: 35px; width: 80.125px;">id</td>
<td style="text-align: center; height: 35px; width: 44.2273px;">PK</td>
<td style="text-align: center; height: 35px; width: 107.489px;">BIGINT</td>
<td style="text-align: center; height: 35px; width: 129.25px;">Да</td>
<td style="text-align: center; width: 161.557px; height: 35px;">3</td>
<td style="text-align: center; height: 35px; width: 113.989px;">Идентификатор записи</td>
</tr>
<tr style="height: 36px;">
<td style="text-align: center; height: 36px; width: 80.125px;">tid</td>
<td style="text-align: center; height: 36px; width: 44.2273px;">FK</td>
<td style="text-align: center; height: 36px; width: 107.489px;">BIGINT</td>
<td style="text-align: center; height: 36px; width: 129.25px;">
<p>Нет</p>
</td>
<td style="text-align: center; width: 161.557px; height: 36px;">1</td>
<td style="text-align: center; height: 36px; width: 113.989px;">Внешний ключ для связи с таблицей acc_tenants.id</td>
</tr>
<tr style="height: 18px;">
<td style="text-align: center; width: 80.125px; height: 18px;">client_id</td>
<td style="text-align: center; width: 44.2273px; height: 18px;">FK</td>
<td style="text-align: center; width: 107.489px; height: 18px;">VARCHAR(255)</td>
<td style="text-align: center; width: 129.25px; height: 18px;">Да</td>
<td style="text-align: center; width: 161.557px; height: 18px;">SRAVNI</td>
<td style="text-align: center; width: 113.989px; height: 18px;">Внешний ключ для связи с таблицей acc_clients.clients_id</td>
</tr>
<tr style="height: 72px;">
<td style="text-align: center; height: 80px; width: 80.125px;">parent_id</td>
<td style="text-align: center; height: 80px; width: 44.2273px;">-</td>
<td style="text-align: center; height: 80px; width: 107.489px;">BIGINT</td>
<td style="text-align: center; height: 80px; width: 129.25px;">Нет</td>
<td style="text-align: center; width: 161.557px; height: 80px;"><span>3</span></td>
<td style="text-align: center; height: 80px; width: 113.989px;">Родитель acc_accounts.id</td>
</tr>
<tr style="height: 36px;">
<td style="text-align: center; height: 36px; width: 80.125px;">accounts_type</td>
<td style="text-align: center; height: 36px; width: 44.2273px;">-</td>
<td style="text-align: center; height: 36px; width: 107.489px;">VARCHAR(10)</td>
<td style="text-align: center; height: 36px; width: 129.25px;">Да</td>
<td style="text-align: center; width: 161.557px; height: 36px;"><span>ACCOUNT</span></td>
<td style="text-align: center; height: 36px; width: 113.989px;">
<p>Тип узла</p>
<p></p>
</td>
</tr>
<tr style="height: 18px;">
<td style="text-align: center; width: 80.125px; height: 18px;">name</td>
<td style="text-align: center; width: 44.2273px; height: 18px;"></td>
<td style="text-align: center; width: 107.489px; height: 18px;">VARCHAR(250)</td>
<td style="text-align: center; width: 129.25px; height: 18px;">Да</td>
<td style="text-align: center; width: 161.557px; height: 18px;"><span>Аккаунт для продаж СРАВНИ</span></td>
<td style="text-align: center; width: 113.989px; height: 18px;">Наименование</td>
</tr>
<tr style="height: 18px;">
<td style="text-align: center; width: 80.125px; height: 18px;">created_at</td>
<td style="text-align: center; width: 44.2273px; height: 18px;">-</td>
<td style="text-align: center; width: 107.489px; height: 18px;">TIMESTAMP</td>
<td style="text-align: center; width: 129.25px; height: 18px;">Да</td>
<td style="text-align: center; width: 161.557px; height: 18px;">2025-11-20T15:30:00Z</td>
<td style="text-align: center; width: 113.989px; height: 18px;">Дата / время созадния</td>
</tr>
<tr style="height: 36px;">
<td style="text-align: center; height: 36px; width: 80.125px;">updated_at</td>
<td style="text-align: center; height: 36px; width: 44.2273px;">-</td>
<td style="text-align: center; height: 36px; width: 107.489px;">TIMESTAMP</td>
<td style="text-align: center; height: 36px; width: 129.25px;">Нет</td>
<td style="text-align: center; width: 161.557px; height: 36px;">2025-11-20T15:30:00Z</td>
<td style="text-align: center; height: 36px; width: 113.989px;">Дата / время обновления</td>
</tr>
</tbody>
</table>
<p></p>
<p>Справочные значения для поля accounts_type:</p>
<table border="1" style="border-collapse: collapse; width: 100%; height: 216px;">
<tbody>
<tr style="height: 18px;">
<td style="width: 50%; height: 18px; text-align: center;"><strong>Значение</strong></td>
<td style="width: 50%; height: 18px; text-align: center;"><strong>Описание</strong></td>
</tr>
<tr style="height: 18px;">
<td style="width: 50%; height: 18px;"><span>ROOT</span></td>
<td style="width: 50%; height: 18px;"><span>Может создавать новые разделы (tenant).</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 50%; height: 18px;"><span>TENANT</span></td>
<td style="width: 50%; height: 18px;"><span>Может создавать учетки клиентов и управлять настройкой ресурсов клиентов.</span></td>
</tr>
<tr style="height: 72px;">
<td style="width: 50%; height: 72px;"><span>CLIENT</span></td>
<td style="width: 50%; height: 72px;"><span>Может управлять учетками и правами в рамках своих групп</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 50%; height: 18px;"><span>GROUP</span></td>
<td style="width: 50%; height: 18px;"><span>Может управлять учетками и правами в рамках своих групп</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 50%; height: 18px;"><span>ACCOUNT&nbsp;</span><span><br /></span></td>
<td style="width: 50%; height: 18px;"><span>Могут видеть свои договоры и выполнять действия в рамках прав.</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 50%; height: 18px;"><span>SUB</span><span><br /></span></td>
<td style="width: 50%; height: 18px;"><span>Продавцы. Могут видеть свои договоры и выполнять действия в рамках прав.</span></td>
</tr>
</tbody>
</table>

#### SQL для создания таблицы:

~~~
CREATE TABLE IF NOT EXISTS acc_accounts (
    id BIGINT PRIMARY KEY,
    tid BIGINT NOT NULL REFERENCES acc_tenants(id),
    client_id VARCHAR(255) NOT NULL  REFERENCES acc_clients(clients_id),
    parent_id BIGINT REFERENCES acc_accounts(id),
    account_type VARCHAR(10) NOT NULL,
    name VARCHAR(250) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
~~~
## Таблица: acc_products_roles  
#### Назаначение: Содержит привязку аккаунта (портфеля) к продукту. Для каждого продукта задается список прав на действия, которые с ним можно проводить (чтение, расчет, создание договора, пролонгация и т.д.)
#### Стркутра таблицы 
<table style="height: 419px; width: 669px;">
<thead>
<tr style="height: 36px;">
<th style="text-align: center; height: 36px; width: 111.75px;">Название поля</th>
<th style="text-align: center; height: 36px; width: 44.2273px;">Ключ</th>
<th style="text-align: center; height: 36px; width: 82.3636px;">Тип поля</th>
<th style="text-align: center; height: 36px; width: 129.25px;">Обязательность</th>
<th style="text-align: center; width: 112.682px; height: 36px;">Пример</th>
<th style="text-align: center; height: 36px; width: 156.364px;">Описание</th>
</tr>
</thead>
<tbody>
<tr style="height: 36px;">
<td style="text-align: center; height: 35px; width: 111.75px;">id</td>
<td style="text-align: center; height: 35px; width: 44.2273px;">PK</td>
<td style="text-align: center; height: 35px; width: 82.3636px;">BIGINT</td>
<td style="text-align: center; height: 35px; width: 129.25px;">Да</td>
<td style="text-align: center; width: 112.682px; height: 35px;">4</td>
<td style="text-align: center; height: 35px; width: 156.364px;">Идентификатор записи</td>
</tr>
<tr style="height: 36px;">
<td style="text-align: center; height: 36px; width: 111.75px;">tid</td>
<td style="text-align: center; height: 36px; width: 44.2273px;">FK</td>
<td style="text-align: center; height: 36px; width: 82.3636px;">BIGINT</td>
<td style="text-align: center; height: 36px; width: 129.25px;">Да</td>
<td style="text-align: center; width: 112.682px; height: 36px;">1</td>
<td style="text-align: center; height: 36px; width: 156.364px;">Внешний ключ для связи с таблицей acc_tenants.id</td>
</tr>
<tr style="height: 72px;">
<td style="text-align: center; height: 80px; width: 111.75px;">role_products_id</td>
<td style="text-align: center; height: 80px; width: 44.2273px;">FK</td>
<td style="text-align: center; height: 80px; width: 82.3636px;">BIGINT</td>
<td style="text-align: center; height: 80px; width: 129.25px;">Да</td>
<td style="text-align: center; width: 112.682px; height: 80px;">5</td>
<td style="text-align: center; height: 80px; width: 156.364px;">Внешний ключ для связи с таблицей products.id</td>
</tr>
<tr style="height: 36px;">
<td style="text-align: center; height: 36px; width: 111.75px;">role_account_id</td>
<td style="text-align: center; height: 36px; width: 44.2273px;">FK</td>
<td style="text-align: center; height: 36px; width: 82.3636px;">BIGINT</td>
<td style="text-align: center; height: 36px; width: 129.25px;">Да</td>
<td style="text-align: center; width: 112.682px; height: 36px;">2</td>
<td style="text-align: center; height: 36px; width: 156.364px;">Внешний ключ для связи с таблицей acc_accounts.id</td>
</tr>
<tr style="height: 18px;">
<td style="text-align: center; width: 111.75px; height: 18px;">is_deleted</td>
<td style="text-align: center; width: 44.2273px; height: 18px;">-</td>
<td style="text-align: center; width: 82.3636px; height: 18px;">BOOLEAN</td>
<td style="text-align: center; width: 129.25px; height: 18px;">Да</td>
<td style="text-align: center; width: 112.682px; height: 18px;">false</td>
<td style="text-align: center; width: 156.364px; height: 18px;">Флаг удаления. True - неактивный(удален), false - активынй</td>
</tr>
<tr style="height: 18px;">
<td style="text-align: center; width: 111.75px; height: 18px;">can_read</td>
<td style="text-align: center; width: 44.2273px; height: 18px;">-</td>
<td style="text-align: center; width: 82.3636px; height: 18px;">BOOLEAN</td>
<td style="text-align: center; width: 129.25px; height: 18px;">Да</td>
<td style="text-align: center; width: 112.682px; height: 18px;">true</td>
<td style="text-align: center; width: 156.364px; height: 18px;">Разрешение на чтение &nbsp;</td>

<tr style="height: 18px;">
<td style="text-align: center; width: 111.75px; height: 18px;">can_printform</td>
<td style="text-align: center; width: 44.2273px; height: 18px;">-</td>
<td style="text-align: center; width: 82.3636px; height: 18px;">BOOLEAN</td>
<td style="text-align: center; width: 129.25px; height: 18px;">Да</td>
<td style="text-align: center; width: 112.682px; height: 18px;">true</td>
<td style="text-align: center; width: 156.364px; height: 18px;">Разрешение на получение ПФ &nbsp;</td>

</tr>
<tr style="height: 36px;">
<td style="text-align: center; height: 36px; width: 111.75px;">can_quote</td>
<td style="text-align: center; height: 36px; width: 44.2273px;">-</td>
<td style="text-align: center; height: 36px; width: 82.3636px;">BOOLEAN</td>
<td style="text-align: center; height: 36px; width: 129.25px;">Да</td>
<td style="text-align: center; width: 112.682px; height: 36px;">true</td>
<td style="text-align: center; height: 36px; width: 156.364px;">Разрешение на пред. расчет</td>
</tr>
<tr style="height: 17px;">
<td style="text-align: center; width: 111.75px; height: 17px;">can_policy</td>
<td style="text-align: center; width: 44.2273px; height: 17px;">-</td>
<td style="text-align: center; width: 82.3636px; height: 17px;">BOOLEAN</td>
<td style="text-align: center; width: 129.25px; height: 17px;">Да</td>
<td style="text-align: center; width: 112.682px; height: 17px;">true</td>
<td style="text-align: center; width: 156.364px; height: 17px;">Разрешение на итог. расчет</td>
</tr>
<tr style="height: 18px;">
<td style="text-align: center; width: 111.75px; height: 18px;">can_addendum</td>
<td style="text-align: center; width: 44.2273px; height: 18px;">-</td>
<td style="text-align: center; width: 82.3636px; height: 18px;">BOOLEAN</td>
<td style="text-align: center; width: 129.25px; height: 18px;">Да</td>
<td style="text-align: center; width: 112.682px; height: 18px;">false</td>
<td style="text-align: center; width: 156.364px; height: 18px;">Разрешение на создание&nbsp; доп.соглашен</td>
</tr>
<tr style="height: 17px;">
<td style="text-align: center; width: 111.75px; height: 17px;">can_cancel</td>
<td style="text-align: center; width: 44.2273px; height: 17px;">-</td>
<td style="text-align: center; width: 82.3636px; height: 17px;">BOOLEAN</td>
<td style="text-align: center; width: 129.25px; height: 17px;">Да</td>
<td style="text-align: center; width: 112.682px; height: 17px;">true</td>
<td style="text-align: center; width: 156.364px; height: 17px;">Разрешение на аннулирование договора</td>
</tr>
<tr style="height: 18px;">
<td style="text-align: center; width: 111.75px; height: 18px;">can_prolongate</td>
<td style="text-align: center; width: 44.2273px; height: 18px;">-</td>
<td style="text-align: center; width: 82.3636px; height: 18px;">BOOLEAN</td>
<td style="text-align: center; width: 129.25px; height: 18px;">Да</td>
<td style="text-align: center; width: 112.682px; height: 18px;">false</td>
<td style="text-align: center; width: 156.364px; height: 18px;">Разрешение на пролонгацию договора</td>
</tr>
<tr style="height: 18px;">
<td style="text-align: center; width: 111.75px; height: 18px;">created_at</td>
<td style="text-align: center; width: 44.2273px; height: 18px;">-</td>
<td style="text-align: center; width: 82.3636px; height: 18px;">TIMESTAMP</td>
<td style="text-align: center; width: 129.25px; height: 18px;">Да</td>
<td style="text-align: center; width: 112.682px; height: 18px;">2025-11-20T15:30:00Z</td>
<td style="text-align: center; width: 156.364px; height: 18px;">Дата/время создания&nbsp;</td>
</tr>
<tr style="height: 18px;">
<td style="text-align: center; width: 111.75px; height: 18px;">updated_at</td>
<td style="text-align: center; width: 44.2273px; height: 18px;">-</td>
<td style="text-align: center; width: 82.3636px; height: 18px;">TIMESTAMP</td>
<td style="text-align: center; width: 129.25px; height: 18px;">Нет</td>
<td style="text-align: center; width: 112.682px; height: 18px;">2025-11-20T15:30:00Z</td>
<td style="text-align: center; width: 156.364px; height: 18px;">Дата/время обновления&nbsp;</td>
</tr>
</tbody>
</table>

#### SQL для создания таблицы:
~~~
CREATE TABLE IF NOT EXISTS acc_products_roles (
    id BIGINT PRIMARY KEY ,
    tid BIGINT NOT NULL REFERENCES acc_tenants(id),
    role_products_id BIGINT NOT NULL,
    role_account_id BIGINT NOT NULL REFERENCES acc_accounts(id),
    is_deleted BOOLEAN  NOT NULL DEFAULT FALSE,
    can_read BOOLEAN NOT NULL DEFAULT FALSE,
    can_printform BOOLEAN  NOT NULL DEFAULT FALSE,
    can_quote BOOLEAN  NOT NULL DEFAULT FALSE,
    can_policy BOOLEAN  NOT NULL DEFAULT FALSE,
    can_addendum BOOLEAN  NOT NULL DEFAULT FALSE,
    can_cancel BOOLEAN  NOT NULL DEFAULT FALSE,
    can_prolongate BOOLEAN  NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
~~~
## Таблица: pt_products 
#### Назаначение: Содержит список продуктов 
#### Стркутра таблицы 

<table style="height: 331px; width: 634px;">
<thead>
<tr style="height: 36px;">
<th style="text-align: center; height: 36px; width: 118px;">Название поля</th>
<th style="text-align: center; height: 36px; width: 44px;">Ключ</th>
<th style="text-align: center; height: 36px; width: 107px;">Тип поля</th>
<th style="text-align: center; height: 36px; width: 129px;">Обязательность</th>
<th style="text-align: center; width: 102px; height: 36px;">Пример</th>
<th style="text-align: center; height: 36px; width: 134px;">Описание</th>
</tr>
</thead>
<tbody>
<tr style="height: 36px;">
<td style="text-align: center; height: 35px; width: 118px;">id</td>
<td style="text-align: center; height: 35px; width: 44px;">PK</td>
<td style="text-align: center; height: 35px; width: 107px;">INTEGER</td>
<td style="text-align: center; height: 35px; width: 129px;">Да</td>
<td style="text-align: center; width: 102px; height: 35px;">4</td>
<td style="text-align: center; height: 35px; width: 134px;">Идентификатор продукта</td>
</tr>
<tr style="height: 36px;">
<td style="text-align: center; height: 36px; width: 118px;">tid</td>
<td style="text-align: center; height: 36px; width: 44px;">FK</td>
<td style="text-align: center; height: 36px; width: 107px;">BIGINT</td>
<td style="text-align: center; height: 36px; width: 129px;">Да</td>
<td style="text-align: center; width: 102px; height: 36px;">1</td>
<td style="text-align: center; height: 36px; width: 134px;">Внешний ключ для связи с таблицей acc_tenants.id</td>
</tr>
<tr style="height: 72px;">
<td style="text-align: center; height: 80px; width: 118px;">code</td>
<td style="text-align: center; height: 80px; width: 44px;">-</td>
<td style="text-align: center; height: 80px; width: 107px;">VARCHAR(30)</td>
<td style="text-align: center; height: 80px; width: 129px;">Да</td>
<td style="text-align: center; width: 102px; height: 80px;"><span>Acclient </span></td>
<td style="text-align: center; height: 80px; width: 134px;">Код продукта</td>
</tr>
<tr style="height: 72px;">
<td style="text-align: center; width: 118px; height: 72px;">name</td>
<td style="text-align: center; width: 44px; height: 72px;">-</td>
<td style="text-align: center; width: 107px; height: 72px;">VARCHAR(250)</td>
<td style="text-align: center; width: 129px; height: 72px;">Да</td>
<td style="text-align: center; width: 102px; height: 72px;">Страхование от несчастных случаев (НС)</td>
<td style="text-align: center; width: 134px; height: 72px;">Наименование продукта</td>
</tr>
<tr style="height: 18px;">
<td style="text-align: center; width: 118px; height: 18px;">lob</td>
<td style="text-align: center; width: 44px; height: 18px;">-</td>
<td style="text-align: center; width: 107px; height: 18px;">VARCHAR(30)</td>
<td style="text-align: center; width: 129px; height: 18px;">Да</td>
<td style="text-align: center; width: 102px; height: 18px;">Страхование жизни</td>
<td style="text-align: center; width: 134px; height: 18px;">Линия бизнеса</td>
</tr>
<tr style="height: 18px;">
<td style="text-align: center; width: 118px; height: 18px;">prod_version_no</td>
<td style="text-align: center; width: 44px; height: 18px;">-</td>
<td style="text-align: center; width: 107px; height: 18px;">INTEGER</td>
<td style="text-align: center; width: 129px; height: 18px;">Нет</td>
<td style="text-align: center; width: 102px; height: 18px;">1</td>
<td style="text-align: center; width: 134px; height: 18px;">Версия продукта прод</td>
</tr>
<tr style="height: 18px;">
<td style="text-align: center; width: 118px; height: 18px;">dev_version_no</td>
<td style="text-align: center; width: 44px; height: 18px;">-</td>
<td style="text-align: center; width: 107px; height: 18px;">INTEGER</td>
<td style="text-align: center; width: 129px; height: 18px;">Нет</td>
<td style="text-align: center; width: 102px; height: 18px;">1</td>
<td style="text-align: center; width: 134px; height: 18px;">Версия продукта dev</td>
</tr>
<tr style="height: 18px;">
<td style="text-align: center; width: 118px; height: 18px;">isDeleted</td>
<td style="text-align: center; width: 44px; height: 18px;">-</td>
<td style="text-align: center; width: 107px; height: 18px;">BOOLEAN</td>
<td style="text-align: center; width: 129px; height: 18px;">Да</td>
<td style="text-align: center; width: 102px; height: 18px;">false</td>
<td style="text-align: center; width: 134px; height: 18px;">Флаг удаления. true - да, false - нет.</td>
</tr>
</tbody>
</table>


## Таблица: acc_account_tokens 
#### Назаначение:<p><span>Доступ к портфелю по коду доступа. Иногда продажи проводятся на публичных сайтах, но есть потребность привязать продажи к разным портфелям. Тогда можно сгенерить разные коды, раздать их продавцам, чтобы они вводили их при продаже и настроить этот код на определенный портфель</span></p> 
#### Стркутра таблицы 

<table style="height: 277px; width: 669px;">
<thead>
<tr style="height: 36px;">
<th style="text-align: center; height: 36px; width: 80.125px;">Название поля</th>
<th style="text-align: center; height: 36px; width: 44.2273px;">Ключ</th>
<th style="text-align: center; height: 36px; width: 107.489px;">Тип поля</th>
<th style="text-align: center; height: 36px; width: 129.25px;">Обязательность</th>
<th style="text-align: center; width: 161.568px; height: 36px;">Пример</th>
<th style="text-align: center; height: 36px; width: 113.977px;">Описание</th>
</tr>
</thead>
<tbody>
<tr style="height: 36px;">
<td style="text-align: center; height: 35px; width: 80.125px;">id</td>
<td style="text-align: center; height: 35px; width: 44.2273px;">PK</td>
<td style="text-align: center; height: 35px; width: 107.489px;">BIGINT</td>
<td style="text-align: center; height: 35px; width: 129.25px;">Да</td>
<td style="text-align: center; width: 161.568px; height: 35px;">4</td>
<td style="text-align: center; height: 35px; width: 113.977px;">Идентификатор записи</td>
</tr>
<tr style="height: 36px;">
<td style="text-align: center; height: 36px; width: 80.125px;">tid</td>
<td style="text-align: center; height: 36px; width: 44.2273px;">FK</td>
<td style="text-align: center; height: 36px; width: 107.489px;">BIGINT</td>
<td style="text-align: center; height: 36px; width: 129.25px;">Да</td>
<td style="text-align: center; width: 161.568px; height: 36px;">1</td>
<td style="text-align: center; height: 36px; width: 113.977px;">Внешний ключ для связи с таблицей acc_tenants.id</td>
</tr>
<tr style="height: 18px;">
<td style="text-align: center; width: 80.125px; height: 18px;">token</td>
<td style="text-align: center; width: 44.2273px; height: 18px;">-</td>
<td style="text-align: center; width: 107.489px; height: 18px;">VARCHAR(255)</td>
<td style="text-align: center; width: 129.25px; height: 18px;">Да</td>
<td style="text-align: center; width: 161.568px; height: 18px;">PROMO</td>
<td style="text-align: center; width: 113.977px; height: 18px;">Название токена</td>
</tr>
<tr style="height: 18px;">
<td style="text-align: center; width: 80.125px; height: 18px;">client_id</td>
<td style="text-align: center; width: 44.2273px; height: 18px;">FK</td>
<td style="text-align: center; width: 107.489px; height: 18px;">VARCHAR(255)</td>
<td style="text-align: center; width: 129.25px; height: 18px;">Да</td>
<td style="text-align: center; width: 161.568px; height: 18px;">SRAVNI</td>
<td style="text-align: center; width: 113.977px; height: 18px;">Внешний ключ для связи с таблицей acc_clients.clients_id</td>
</tr>
<tr style="height: 18px;">
<td style="text-align: center; width: 80.125px; height: 18px;">aid</td>
<td style="text-align: center; width: 44.2273px; height: 18px;">FK</td>
<td style="text-align: center; width: 107.489px; height: 18px;">BIGINT</td>
<td style="text-align: center; width: 129.25px; height: 18px;">Да</td>
<td style="text-align: center; width: 161.568px; height: 18px;">3</td>
<td style="text-align: center; width: 113.977px; height: 18px;">Внешний ключ для связи с таблицей acc_accounts.id</td>
</tr>
<tr style="height: 72px;">
<td style="text-align: center; height: 80px; width: 80.125px;">created_at</td>
<td style="text-align: center; height: 80px; width: 44.2273px;">-</td>
<td style="text-align: center; height: 80px; width: 107.489px;">TIMESTAMP</td>
<td style="text-align: center; height: 80px; width: 129.25px;">Да</td>
<td style="text-align: center; width: 161.568px; height: 80px;">2025-11-20T15:30:00Z</td>
<td style="text-align: center; height: 80px; width: 113.977px;">Дата/время создания</td>
</tr>
<tr style="height: 36px;">
<td style="text-align: center; height: 36px; width: 80.125px;">updated_at</td>
<td style="text-align: center; height: 36px; width: 44.2273px;">-</td>
<td style="text-align: center; height: 36px; width: 107.489px;">TIMESTAMP</td>
<td style="text-align: center; height: 36px; width: 129.25px;">Нет</td>
<td style="text-align: center; width: 161.568px; height: 36px;">2025-11-20T15:30:00Z</td>
<td style="text-align: center; height: 36px; width: 113.977px;">Дата/время обновления</td>
</tr>
</tbody>
</table>

#### SQL для создания таблицы:
~~~
CREATE TABLE IF NOT EXISTS acc_account_tokens (
    id BIGINT PRIMARY KEY,
    tid BIGINT NOT NULL REFERENCES acc_tenants(id),
    token VARCHAR(255) NOT NULL,
    client_id VARCHAR(255) NOT NULL REFERENCES acc_clients(clients_id),
    aid BIGINT NOT NULL REFERENCES acc_accounts(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (token, client_id)
);
~~~
## Таблица: acc_account_logins
#### Назаначение: Привязка пользователя к account.
Комментарий: Пользователи и портфели имею связь много ко многим. Так же указывается, в рамках какой интеграции (client_id), пользователь имеет доступ.
Если в токене не указано, к какому портфелю нужен доступ, то берется дефолтный портфель с isDefault = true
Такой признак можно проставить только у одного портфеля в рамках client_id 
#### Стркутра таблицы 

<table style="height: 295px; width: 669px;">
<thead>
<tr style="height: 36px;">
<th style="text-align: center; height: 36px; width: 80.125px;">Название поля</th>
<th style="text-align: center; height: 36px; width: 44.2273px;">Ключ</th>
<th style="text-align: center; height: 36px; width: 107.489px;">Тип поля</th>
<th style="text-align: center; height: 36px; width: 129.25px;">Обязательность</th>
<th style="text-align: center; width: 161.568px; height: 36px;">Пример</th>
<th style="text-align: center; height: 36px; width: 113.977px;">Описание</th>
</tr>
</thead>
<tbody>
<tr style="height: 36px;">
<td style="text-align: center; height: 35px; width: 80.125px;">id</td>
<td style="text-align: center; height: 35px; width: 44.2273px;">PK</td>
<td style="text-align: center; height: 35px; width: 107.489px;">BIGINT</td>
<td style="text-align: center; height: 35px; width: 129.25px;">Да</td>
<td style="text-align: center; width: 161.568px; height: 35px;">5</td>
<td style="text-align: center; height: 35px; width: 113.977px;">Идентификатор записи</td>
</tr>
<tr style="height: 36px;">
<td style="text-align: center; height: 36px; width: 80.125px;">tid</td>
<td style="text-align: center; height: 36px; width: 44.2273px;">FK</td>
<td style="text-align: center; height: 36px; width: 107.489px;">BIGINT</td>
<td style="text-align: center; height: 36px; width: 129.25px;">Да</td>
<td style="text-align: center; width: 161.568px; height: 36px;">1</td>
<td style="text-align: center; height: 36px; width: 113.977px;">Внешний ключ для связи с таблицей acc_tenants.id</td>
</tr>
<tr style="height: 18px;">
<td style="text-align: center; width: 80.125px; height: 18px;">user_login</td>
<td style="text-align: center; width: 44.2273px; height: 18px;">-</td>
<td style="text-align: center; width: 107.489px; height: 18px;">VARCHAR(255)</td>
<td style="text-align: center; width: 129.25px; height: 18px;">Да</td>
<td style="text-align: center; width: 161.568px; height: 18px;">user1</td>
<td style="text-align: center; width: 113.977px; height: 18px;"></td>
</tr>
<tr style="height: 18px;">
<td style="text-align: center; width: 80.125px; height: 18px;">client_id</td>
<td style="text-align: center; width: 44.2273px; height: 18px;">FK</td>
<td style="text-align: center; width: 107.489px; height: 18px;">VARCHAR(255)</td>
<td style="text-align: center; width: 129.25px; height: 18px;">Да</td>
<td style="text-align: center; width: 161.568px; height: 18px;">SRAVNI</td>
<td style="text-align: center; width: 113.977px; height: 18px;">Внешний ключ для связи с таблицей acc_clients.client_id&nbsp;</td>
</tr>
<tr style="height: 18px;">
<td style="text-align: center; width: 80.125px; height: 18px;">is_default</td>
<td style="text-align: center; width: 44.2273px; height: 18px;">-</td>
<td style="text-align: center; width: 107.489px; height: 18px;">BOOLEAN</td>
<td style="text-align: center; width: 129.25px; height: 18px;">Да</td>
<td style="text-align: center; width: 161.568px; height: 18px;">true</td>
<td style="text-align: center; width: 113.977px; height: 18px;">
<p>Дефолтный портефль&nbsp;</p>
<p>true - да, false - нет</p>
</td>
</tr>
<tr style="height: 18px;">
<td style="text-align: center; width: 80.125px; height: 18px;">aid</td>
<td style="text-align: center; width: 44.2273px; height: 18px;">FK</td>
<td style="text-align: center; width: 107.489px; height: 18px;">BIGINT</td>
<td style="text-align: center; width: 129.25px; height: 18px;">Да</td>
<td style="text-align: center; width: 161.568px; height: 18px;">2</td>
<td style="text-align: center; width: 113.977px; height: 18px;">Внешний ключ для связи с таблицей acc_account.id&nbsp;</td>
</tr>
<tr style="height: 72px;">
<td style="text-align: center; height: 80px; width: 80.125px;">created_at</td>
<td style="text-align: center; height: 80px; width: 44.2273px;">-</td>
<td style="text-align: center; height: 80px; width: 107.489px;">TIMESTAMP</td>
<td style="text-align: center; height: 80px; width: 129.25px;">Да</td>
<td style="text-align: center; width: 161.568px; height: 80px;">2025-11-20T15:30:00Z</td>
<td style="text-align: center; height: 80px; width: 113.977px;">Дата/время создания</td>
</tr>
<tr style="height: 36px;">
<td style="text-align: center; height: 36px; width: 80.125px;">updated_at</td>
<td style="text-align: center; height: 36px; width: 44.2273px;">-</td>
<td style="text-align: center; height: 36px; width: 107.489px;">TIMESTAMP</td>
<td style="text-align: center; height: 36px; width: 129.25px;">Нет&nbsp;</td>
<td style="text-align: center; width: 161.568px; height: 36px;">2025-11-20T15:30:00Z</td>
<td style="text-align: center; height: 36px; width: 113.977px;">Дата/время обновления</td>
</tr>
</tr>
<tr style="height: 36px;">
<td style="text-align: center; height: 36px; width: 80.125px;">user_role</td>
<td style="text-align: center; height: 36px; width: 44.2273px;">-</td>
<td style="text-align: center; height: 36px; width: 107.489px;">VARCHAR(30)</td>
<td style="text-align: center; height: 36px; width: 129.25px;">Да&nbsp;</td>
<td style="text-align: center; width: 161.568px; height: 36px;">2025-11-20T15:30:00Z</td>
<td style="text-align: center; height: 36px; width: 113.977px;">Роль пользователя</td>>
</tr>
</tbody>
</table>

#### SQL для создания таблицы:
~~~
CREATE TABLE IF NOT EXISTS acc_account_logins (
    id BIGINT PRIMARY KEY ,
    tid BIGINT NOT NULL REFERENCES acc_tenants(id),
    user_login VARCHAR(255) NOT NULL,
    client_id BIGINT NOT NULL REFERENCES acc_clients(client_code),
    is_default BOOLEAN DEFAULT FALSE,
    account_id BIGINT NOT NULL REFERENCES acc_accounts(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tid, user_login) REFERENCES acc_logins(tid, user_login),
    UNIQUE (user_login, account_id)
);
~~~

### Логика загрузки данных
#### Название метода: 
```
POST /tnts/{tenantCode}/clients/{clientId}/accounts
```
#### Назначние метода: Создание аккаунта И наделение правами пользователя И создание токена

<p>Входные параметры&nbsp;</p>
<p><span>path</span>:</p>
<table border="1" style="border-collapse: collapse; width: 100%; height: 216px;">
<tbody>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px; text-align: center;"><strong>Значение параметра</strong></td>
<td style="width: 12.5%; text-align: center;"><strong>Тип</strong></td>
<td style="width: 12.5%; text-align: center;"><strong>Обязательность</strong></td>
<td style="width: 50%; height: 18px; text-align: center;"><strong>Описание</strong></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;"><span>tenantCode</span></td>
<td style="width: 12.5%;"><span>string</span><span>&nbsp;</span></td>
<td style="width: 12.5%; text-align: center;"><span>Да</span></td>
<td style="width: 50%; height: 18px;">
<p>Код тенанта</p>
<p></p>
</td>
</tr>
<tr>
<td style="width: 25%;"><span>clientId</span><span>&nbsp;</span><span><br /></span></td>
<td style="width: 12.5%;"><span>string&nbsp;</span></td>
<td style="width: 12.5%; text-align: center;"><span>Да</span></td>
<td style="width: 50%;">
<p>Код клиента (партнера)</p>
</td>
</tr>
</tbody>
</table>

<p><em>*Комменатрий: значение&nbsp;tenantCode можно получить в таблице&nbsp;acc_tenants поле code, значение clientId в таблице acc_clients значение поля  id .</em></p>

<p>body:</p>
<table border="1" style="border-collapse: collapse; width: 100%; height: 432px;">
<tbody>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px; text-align: center;"><strong>Значение параметра</strong></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><strong>Тип</strong></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><strong>Обязательность</strong></td>
<td style="width: 50%; height: 18px; text-align: center;"><strong>Описание</strong></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;">id</td>
<td style="width: 12.5%; height: 18px;"><span>string</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Да</span></td>
<td style="width: 50%; height: 18px;">Ид&nbsp;</td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;">parentId</td>
<td style="width: 12.5%; height: 18px;"><span>string</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Нет</span></td>
<td style="width: 50%; height: 18px;"><span>Родитель acc_accounts.id</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;"><span>name</span></td>
<td style="width: 12.5%; height: 18px;"><span>string</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Да</span></td>
<td style="width: 50%; height: 18px;"><span>Наименование аккаунта</span></td>
</tr>
<tr style="height: 90px;">
<td style="width: 25%; height: 90px;">
<p>accountType</p>
</td>
<td style="width: 12.5%; height: 90px;"><span>&nbsp;string</span></td>
<td style="width: 12.5%; text-align: center; height: 90px;"><span>Да</span></td>
<td style="width: 50%; height: 90px;">
<p>Тип аккаунта&nbsp;</p>
<p><span>Enum:</span></p>
<ul>
<li><span>ROOT</span></li>
<li><span>TENANT</span></li>
<li><span>CLIENT</span></li>
<li><span>GROUP</span></li>
<li><span>ACCOUNT&nbsp;</span></li>
<li><span>SUB</span></li>
<li><span>products</span></li>
</ul>
</td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;">
<p>logins</p>
</td>
<td style="width: 12.5%; height: 18px;"><span>массив</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Да</span></td>
<td style="width: 50%; height: 18px;"><span>Список продающих учеток, имеющих доступ к этому узлу.</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;">logins.login</td>
<td style="width: 12.5%; height: 18px;"><span>string</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Да</span></td>
<td style="width: 50%; height: 18px;"><span>Логин УЗ</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;">logins.role</td>
<td style="width: 12.5%; height: 18px;"><span>string</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Да</span></td>
<td style="width: 50%; height: 18px;"><span>Роль УЗ</span></td>
</tr>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;">tokens</td>
<td style="width: 12.5%; height: 18px;"><span>массив</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Нет</span></td>
<td style="width: 50%; height: 18px;"><span>Список активных токенов на узле. Для ACCOUNT &amp; SUB</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;"><span>tokens.token</span></td>
<td style="width: 12.5%; height: 18px;"><span>string</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Нет</span></td>
<td style="width: 50%; height: 18px;"><span>Токен</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;">products</td>
<td style="width: 12.5%; height: 18px;"><span>массив</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Да</span></td>
<td style="width: 50%; height: 18px;"><span>Продуктовые роли</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;">products.roleproductsId</td>
<td style="width: 12.5%; height: 18px;"><span>string</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Да</span></td>
<td style="width: 50%; height: 18px;"><span>ИД роли. (Внешний ключ для связи с таблицей products.id)</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;">products.roleAccauntId</td>
<td style="width: 12.5%; height: 18px;"><span>string</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Да</span></td>
<td style="width: 50%; height: 18px;"><span>ИД Аккаунта. (Внешний ключ для связи с таблицей acc_accounts.id)</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;"><span>products.canRead</span></td>
<td style="width: 12.5%; height: 18px;"><span>boolean</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Нет</span></td>
<td style="width: 50%; height: 18px;"><span>Разрешение на чтение. Если НЕ пришло, то false</span></td>
</tr>
    </tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;"><span>products.canPrintform</span></td>
<td style="width: 12.5%; height: 18px;"><span>boolean</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Нет</span></td>
<td style="width: 50%; height: 18px;"><span>Разрешение на получение ПФ. Если НЕ пришло, то false</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;"><span>products.canQuote</span></td>
<td style="width: 12.5%; height: 18px;"><span>boolean</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Нет</span></td>
<td style="width: 50%; height: 18px;"><span>Разрешение на пред. расчет.&nbsp;Если НЕ пришло, то false</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;"><span>products.canPolicy</span></td>
<td style="width: 12.5%; height: 18px;"><span>boolean</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Нет</span></td>
<td style="width: 50%; height: 18px;"><span>Разрешение на итог. расчет.&nbsp;Если НЕ пришло, то false</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;"><span>products.canAddendum</span></td>
<td style="width: 12.5%; height: 18px;"><span>boolean</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Нет</span></td>
<td style="width: 50%; height: 18px;"><span>Разрешение на создание&nbsp; доп.соглашение.&nbsp; Если НЕ пришло, то false</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;"><span>products.canCancel</span></td>
<td style="width: 12.5%; height: 18px;"><span>boolean</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Нет</span></td>
<td style="width: 50%; height: 18px;"><span>Разрешение на аннулирование договора.&nbsp;Если НЕ пришло, то false</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;"><span>products.canProlongate</span></td>
<td style="width: 12.5%; height: 18px;"><span>boolean</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Нет</span></td>
<td style="width: 50%; height: 18px;"><span>Разрешение на пролонгацию договора. Если НЕ пришло, то false</span></td>
</tr>
</tbody>
</table>

<p>Пример запроса:&nbsp;</p>
<p>POST /tnts/VSK/clients/1/accounts</p>

<pre> {
  "parentId": "3",
  "name": "Сравни",
  "accountType": "ACCOUNT",
  "logins": [
    {
      "login": "sravni@mail.ru",
     "role": "seles"
    }
  ],
  "tokens": [
    {
      "token": "SR"
    }
  ],
  "products": [
    {
      "roleproductsId": "5",
      "roleproductsId": "5",
      "canRead": true,
      "canPrintform": true,
      "canQuote": true,
      "canPolicy": true,
      "canAddendum": true,
      "canCancel": false,
      "canProlongate": false
    }
  ]
}
}
  }
</pre>
<p>Выходные параметры:&nbsp;</p>

<p>body:</p>
<table border="1" style="border-collapse: collapse; width: 100%; height: 414px;">
<tbody>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px; text-align: center;"><strong>Значение параметра</strong></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><strong>Тип</strong></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><strong>Обязательность</strong></td>
<td style="width: 50%; height: 18px; text-align: center;"><strong>Описание</strong></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;">id</td>
<td style="width: 12.5%; height: 18px;"><span>string</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Да</span></td>
<td style="width: 50%; height: 18px;">Ид&nbsp;</td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;">parentId</td>
<td style="width: 12.5%; height: 18px;"><span>string</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Нет</span></td>
<td style="width: 50%; height: 18px;"><span>Родитель acc_accounts.id</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;"><span>name</span></td>
<td style="width: 12.5%; height: 18px;"><span>string</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Да</span></td>
<td style="width: 50%; height: 18px;"><span>Наименование аккаунта</span></td>
</tr>
<tr style="height: 90px;">
<td style="width: 25%; height: 90px;">
<p>accountType</p>
</td>
<td style="width: 12.5%; height: 90px;"><span>&nbsp;string</span></td>
<td style="width: 12.5%; text-align: center; height: 90px;"><span>Да</span></td>
<td style="width: 50%; height: 90px;">
<p>Тип аккаунта&nbsp;</p>
<p><span>Enum:</span></p>
<ul>
<li><span>ROOT</span></li>
<li><span>TENANT</span></li>
<li><span>CLIENT</span></li>
<li><span>GROUP</span></li>
<li><span>ACCOUNT&nbsp;</span></li>
<li><span>SUB</span></li>
<li><span>products</span></li>
</ul>
</td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;">
<p>logins</p>
</td>
<td style="width: 12.5%; height: 18px;"><span>массив</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Да</span></td>
<td style="width: 50%; height: 18px;"><span>Список продающих учеток, имеющих доступ к этому узлу.</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;">logins.login</td>
<td style="width: 12.5%; height: 18px;"><span>string</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Да</span></td>
<td style="width: 50%; height: 18px;"><span>Логин УЗ</span></td>
</tr>
<tr>
<td style="width: 25%;">logins.role</td>
<td style="width: 12.5%;"><span>string</span></td>
<td style="width: 12.5%; text-align: center;"><span>Да</span></td>
<td style="width: 50%;"><span>Роль УЗ</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;"><span>logins.isDefault</span></td>
<td style="width: 12.5%; height: 18px;"><span>string</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Нет</span></td>
<td style="width: 50%; height: 18px;">
<p><span>Дефолтный портефль&nbsp;</span></p>
<p><span>true - да, false - нет</span></p>
</td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;">tokens</td>
<td style="width: 12.5%; height: 18px;"><span>массив</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Нет</span></td>
<td style="width: 50%; height: 18px;"><span>Список активных токенов на узле. Для ACCOUNT &amp; SUB</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;"><span>tokens.token</span></td>
<td style="width: 12.5%; height: 18px;"><span>string</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Нет</span></td>
<td style="width: 50%; height: 18px;"><span>Токен</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;">products</td>
<td style="width: 12.5%; height: 18px;"><span>массив</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Да</span></td>
<td style="width: 50%; height: 18px;"><span>Продуктовые роли</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;">products.roleproductsId</td>
<td style="width: 12.5%; height: 18px;"><span>string</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Да</span></td>
<td style="width: 50%; height: 18px;"><span>ИД роли. (Внешний ключ для связи с таблицей products.id)</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;">products.roleAccauntId</td>
<td style="width: 12.5%; height: 18px;"><span>string</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Да</span></td>
<td style="width: 50%; height: 18px;"><span>ИД Аккаунта. (Внешний ключ для связи с таблицей acc_accounts.id)</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;"><span>products.canRead</span></td>
<td style="width: 12.5%; height: 18px;"><span>boolean</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Да</span></td>
<td style="width: 50%; height: 18px;"><span>Разрешение на чтение &nbsp;</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;"><span>products.canPrintform</span></td>
<td style="width: 12.5%; height: 18px;"><span>boolean</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Нет</span></td>
<td style="width: 50%; height: 18px;"><span>Разрешение на получение ПФ. Если НЕ пришло, то false</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;"><span>products.canQuote</span></td>
<td style="width: 12.5%; height: 18px;"><span>boolean</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Да</span></td>
<td style="width: 50%; height: 18px;"><span>Разрешение на пред. расчет</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;"><span>products.canPolicy</span></td>
<td style="width: 12.5%; height: 18px;"><span>boolean</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Да</span></td>
<td style="width: 50%; height: 18px;"><span>Разрешение на итог. расчет</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;"><span>products.canAddendum</span></td>
<td style="width: 12.5%; height: 18px;"><span>boolean</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Да</span></td>
<td style="width: 50%; height: 18px;"><span>Разрешение на создание&nbsp; доп.соглашение</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;"><span>products.canCancel</span></td>
<td style="width: 12.5%; height: 18px;"><span>boolean</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Да</span></td>
<td style="width: 50%; height: 18px;"><span>Разрешение на аннулирование договора</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;"><span>products.canProlongate</span></td>
<td style="width: 12.5%; height: 18px;"><span>boolean</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Да</span></td>
<td style="width: 50%; height: 18px;"><span>Разрешение на пролонгацию договора</span></td>
</tr>
</tbody>
</table>

Пример ответа:
<pre> {
  "parentId": "3",
  "name": "Сравни",
  "accountType": "ACCOUNT",
  "logins": [
    {
      "login": "sravni@mail.ru",
     "role": "seles"
    }
  ],
  "tokens": [
    {
      "token": "SR"
    }
  ],
  "products": [
    {
      "roleproductsId": "5",
      "canRead": true,
      "canPrintform": true,
      "canQuote": true,
      "canPolicy": true,
      "canAddendum": true,
      "canCancel": false,
      "canProlongate": false
    }
  ]
}
}
  }
    </pre> 

### Название сценария: Создание аккаунта И наделение правами пользователя И создание токена 
#### Триггер: Вызван метод POST /tnts/{tenantCode}/clients/{clientId}/accounts
#### Сценарий :
<p>1. Проверить по code наличие тенанта в таблице acc_tenants. Если совпадение найдено, то перейти на шаг 2, иначе исключение 2а </p>

~~~
select t.code from acc_tenants t 
where t.code = <'tenantCode из запроса'>
~~~
    
<p>2. Проверить по client_id наличие клиента в таблице acc_clients. Если совпадение найдено, то перейти на шаг 3, иначе исключение 3а </p>

~~~
select с.client_id from acc_clients с
where с.client_id = <'clientId из запроса'>
~~~

<p>3. Проверить,что заполнены обязательные параметры и их тип соотв. структуре данных. Если проверка пройдена, то перейти на шаг 4, иначе исключение 4а </p>

<p>4. Проверить параметры, связанные с таблицей acc_accounts:</p> 
<p>4.1. id пророверить, что в таблице acc_accounts НЕТ такого id</p>

~~~
select с.id from acc_clients с
where с.id = <'id из запроса'>
~~~

<p>4.2. Проверить на допустимые значения accountType:</p>
<ul>
<li><span>ROOT</span></li>
<li><span>TENANT</span></li>
<li><span>CLIENT</span></li>
<li><span>GROUP</span></li>
<li><span>ACCOUNT&nbsp;</span></li>
<li><span>SUB</span></li>
</ul>
Если проверка пройдена, то перейти на шаг 5, иначе исключение 5а </p>

<p>5. Проверить параметры, связанные с таблицей acc_products_roles:</p> 
<p>5.1. проверить, что ИД продукта products.id существует в таблице products поле role_products_id 

~~~
select p.role_products_id from acc_products_roles с
where p.role_products_id = <'products.id из запроса'>
~~~

Если проверка пройдена, то перейти на шаг 6, иначе исключение 6а </p>

<p>6. Проверить параметры, связанные с таблицей acc_account_logins:</p> 
<p>6.1. Проверить, что logins.login) в таблице acc_account_logins уникален в разрезе ид тената (tid) и логина УЗ (user_login) </p> 
<p>6.2. Проверить, что logins.user_login есть в тбалице acc_logins </p> 

~~~
select l.logins from acc_logins l
where  l.logins = <'logins.user_login из запроса'>
~~~

<p>6.3. Проверить, что logins.role из списка: .... (чуть позже приложим) </p> 

Если проверка пройдена, то перейти на шаг 7, иначе исключение 7а </p>

<p>6. Выполнить маппинг и создать записи в таблицах:</p> 
--- acc_accounts
<table border="1" style="border-collapse: collapse; width: 80.5243%; height: 162px;">
<tbody>
<tr style="height: 18px;">
<td style="width: 25.1704%; height: 18px; text-align: center;"><strong>Значение параметра в API</strong></td>
<td style="width: 23.7363%; text-align: center; height: 18px;"><strong>Значение параметра в таблице</strong></td>
<td style="width: 50.9998%; height: 18px; text-align: center;"><strong>Описание</strong></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25.1704%; height: 18px;">id</td>
<td style="width: 23.7363%; height: 18px;"><span>=</span>acc_accounts.<span>id</span></td>
<td style="width: 50.9998%; height: 18px;">ИД аккаунта</td>
</tr>
<tr style="height: 18px;">
<td style="width: 25.1704%; height: 18px;">-</td>
<td style="width: 23.7363%; height: 18px;">
<p><span>=</span>acc_accounts.<span>tid</span></p>
<p><span>по значению параметра {tenantCode} определить ИД тенанта</span></p>
</td>
<td style="width: 50.9998%; height: 18px;">ИД тенанта</td>
</tr>
<tr style="height: 18px;">
<td style="width: 25.1704%; height: 18px;"><span>clientId (path параметр)</span></td>
<td style="width: 23.7363%; height: 18px;"><span>=</span>acc_accounts.<span>client_id</span></td>
<td style="width: 50.9998%; height: 18px;">Код клиента</td>
</tr>
<tr style="height: 18px;">
<td style="width: 25.1704%; height: 18px;">parentId</td>
<td style="width: 23.7363%; height: 18px;"><span>=</span>acc_accounts.<span>parent_id</span></td>
<td style="width: 50.9998%; height: 18px;">
<p><span>ИД </span><span>родителя acc_accounts.id</span></p>
</td>
</tr>
<tr style="height: 18px;">
<td style="width: 25.1704%; height: 18px;">accountType</td>
<td style="width: 23.7363%; height: 18px;">
<p><span>=</span>acc_accounts.account_type</p>
</td>
<td style="width: 50.9998%; height: 18px;">Тип аккаунта</td>
</tr>
<tr style="height: 18px;">
<td style="width: 25.1704%; height: 18px;"><span>name&nbsp;</span></td>
<td style="width: 23.7363%; height: 18px;"><span>=</span>acc_accounts.<span>name</span></td>
<td style="width: 50.9998%; height: 18px;">Наименование аккаунта</td>
</tr>
<tr style="height: 18px;">
<td style="width: 25.1704%; height: 18px;">-</td>
<td style="width: 23.7363%; height: 18px;"><span>=</span>acc_accounts.<span>created_at</span></td>
<td style="width: 50.9998%; height: 18px;">Дата/время создания&nbsp;аккаунта&nbsp;</td>
</tr>
</tbody>
</table>
--- acc_account_logins
<table border="1" style="border-collapse: collapse; width: 99.9065%; height: 334px;">
<tbody>
<tr style="height: 18px;">
<td style="width: 25.1704%; height: 18px; text-align: center;"><strong>Значение параметра в API</strong></td>
<td style="width: 24.4627%; text-align: center; height: 18px;"><strong>Значение параметра в таблице</strong></td>
<td style="width: 50.2734%; height: 18px; text-align: center;"><strong>Описание</strong></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25.1704%; height: 18px;">logins</td>
<td style="width: 24.4627%; height: 18px;"></td>
<td style="width: 50.2734%; height: 18px;"></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25.1704%; height: 18px;">-</td>
<td style="width: 24.4627%; height: 18px;"><span>=acc_account_logins.id сгенирировать&nbsp;</span></td>
<td style="width: 50.2734%; height: 18px;"></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25.1704%; height: 18px;">-</td>
<td style="height: 18px;">
<p>=acc_account_logins.tid&nbsp;по значению параметра {tenantCode} определить ИД в таблице&nbsp;acc_tenants</p>
</td>
<td style="width: 50.2734%; height: 18px;">ИД тенанта&nbsp;</td>
</tr>
<tr style="height: 18px;">
<td style="width: 25.1704%; height: 18px;">logins.login</td>
<td style="width: 24.4627%; height: 18px;"><span>=acc_account_logins.user_login</span></td>
<td style="width: 50.2734%; height: 18px;"><span>Логин УЗ</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25.1704%; height: 18px;"><span>clientId (path параметр)</span></td>
<td style="width: 24.4627%; height: 18px;"><span>=acc_account_logins.client_id взять из&nbsp;clientId (path параметр)</span></td>
<td style="width: 50.2734%; height: 18px;">ИД клиента</td>
</tr>
<tr style="height: 18px;">
<td style="width: 25.1704%; height: 18px;">-</td>
<td style="width: 24.4627%; height: 18px;"><span>=acc_account_logins.is_default уст. false</span></td>
<td style="width: 50.2734%; height: 18px;">
<p><span>Дефолтный портефль:&nbsp;</span><span>true - да, false - нет</span></p>
</td>
</tr>
<tr style="height: 18px;">
<td style="width: 25.1704%; height: 18px;">logins.role</td>
<td style="width: 24.4627%; height: 18px;"><span>=acc_account_logins.role</span></td>
<td style="width: 50.2734%; height: 18px;"><span>Роль УЗ</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25.1704%; height: 18px;">-</td>
<td style="width: 24.4627%; height: 18px;"><span>=acc_account_logins.created_at уст. тек. дату</span></td>
<td style="width: 50.2734%; height: 18px;"><span>Дата/время создания</span></td>
</tr>
</tbody>
</table>
---acc_account_tokens
<table border="1" style="border-collapse: collapse; width: 99.9065%; height: 825px;">
<tbody>
<tr style="height: 18px;">
<td style="width: 25.1704%; height: 18px; text-align: center;"><strong>Значение параметра в API</strong></td>
<td style="width: 24.4627%; text-align: center; height: 18px;"><strong>Значение параметра в таблице</strong></td>
<td style="width: 50.2734%; height: 18px; text-align: center;"><strong>Описание</strong></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25.1704%; height: 18px;">tokens</td>
<td style="width: 24.4627%; height: 18px;"><span>массив</span></td>
<td style="width: 50.2734%; height: 18px;"><span>Список активных токенов на узле. Для ACCOUNT &amp; SUB</span></td>
</tr>
<tr>
<td style="width: 25.1704%;">-</td>
<td style="width: 24.4627%;"><span><span>=</span></span>acc_account_tokens.<span>id сгенирировать</span></td>
<td style="width: 50.2734%;"><span>ИД токена</span></td>
</tr>
<tr>
<td style="width: 25.1704%;">-</td>
<td style="width: 24.4627%;"><span>=acc_account_tokens.tid&nbsp;по значению параметра {tenantCode} определить ИД тенанта</span></td>
<td style="width: 50.2734%;"><span>ИД тенанта</span></td>
</tr>
<tr>
<td style="width: 25.1704%;"><span>tokens.token</span></td>
<td style="width: 24.4627%;"><span>=acc_account_tokens.token</span></td>
<td style="width: 50.2734%;"><span>Токен </span></td>
</tr>
<tr>
<td style="width: 25.1704%;"><span>{clientId}</span></td>
<td style="width: 24.4627%;"><span>=acc_account_tokens.client_id</span></td>
<td style="width: 50.2734%;"><span>Код клиента</span></td>
</tr>
<tr>
<td style="width: 25.1704%;">id</td>
<td style="width: 24.4627%;"><span>=acc_account_tokens.aid</span></td>
<td style="width: 50.2734%;"><span>Внешний ключ для связи с таблицей acc_accounts.id</span></td>
</tr>
<tr>
<td style="width: 25.1704%;">-</td>
<td style="width: 24.4627%;"><span>=acc_account_tokens.created_at</span></td>
<td style="width: 50.2734%;"><span>Дата/время создания токена</span></td>
</tr>
</tbody>
</table>
--- acc_product_roles
<table border="1" style="border-collapse: collapse; width: 80.5243%; height: 324px;">
<tbody>
<tr style="height: 18px;">
<td style="width: 25.1704%; height: 18px; text-align: center;"><strong>Значение параметра в API</strong></td>
<td style="width: 24.4627%; text-align: center; height: 18px;"><strong>Значение параметра в таблице</strong></td>
<td style="width: 50.2734%; height: 18px; text-align: center;"><strong>Описание</strong></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25.1704%; height: 18px;">products</td>
<td style="width: 24.4627%; height: 18px;"><span>массив</span></td>
<td style="width: 50.2734%; height: 18px;"><span>Продуктовые роли</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25.1704%; height: 18px;">products.roleProductId</td>
<td style="width: 24.4627%; height: 18px;"><span><span>=</span></span>acc_product_roles.<span>role_product_id</span></td>
<td style="width: 50.2734%; height: 18px;"><span>ИД роли. (Внешний ключ для связи с таблицей products.id)</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25.1704%; height: 18px;">id</td>
<td style="width: 24.4627%; height: 18px;"><span>=</span>acc_product_roles.id</td>
<td style="width: 50.2734%; height: 18px;"><span>ИД Аккаунта. (Внешний ключ для связи с таблицей acc_accounts.id)</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25.1704%; height: 18px;"><span>products.canRead</span></td>
<td style="width: 24.4627%; height: 18px;"><span>=</span>acc_product_roles.<span>can_read</span></td>
<td style="width: 50.2734%; height: 18px;"><span>Разрешение на чтение &nbsp;</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25.1704%; height: 18px;"><span>products.canPolicy</span></td>
<td style="width: 24.4627%; height: 18px;"><span>=</span>acc_product_roles<span>.can_policy</span></td>
<td style="width: 50.2734%; height: 18px;"><span>Разрешение на пред. расчет</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25.1704%; height: 18px;"><span>products.canQuote</span></td>
<td style="width: 24.4627%; height: 18px;"><span>=</span>acc_product_roles<span>.can_quote</span></td>
<td style="width: 50.2734%; height: 18px;"><span>Разрешение на итог. расчет</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25.1704%; height: 18px;"><span>products.canAddendum</span></td>
<td style="width: 24.4627%; height: 18px;"><span>=</span>acc_product_roles<span>.can_addendum</span></td>
<td style="width: 50.2734%; height: 18px;"><span>Разрешение на создание&nbsp; доп.соглашение</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25.1704%; height: 18px;"><span>products.canCancel</span></td>
<td style="width: 24.4627%; height: 18px;"><span>=</span>acc_product_roles<span>.can_cancel</span></td>
<td style="width: 50.2734%; height: 18px;"><span>Разрешение на аннулирование договора</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25.1704%; height: 18px;"><span>products.canProlongate</span></td>
<td style="width: 24.4627%; height: 18px;"><span>=</span>acc_product_roles<span>.can_prolongate</span></td>
<td style="width: 50.2734%; height: 18px;"><span>Разрешение на пролонгацию договора</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25.1704%; height: 18px;"><span>-</span></td>
<td style="width: 24.4627%; height: 18px;"><span>=acc_product_roles.is_deleted&nbsp;уст. false</span></td>
<td style="width: 50.2734%; height: 18px;"><span>Флаг удаления</span></td>
</tr>
<tr style="height: 36px;">
<td style="width: 25.1704%; height: 36px;"><span>-</span></td>
<td style="width: 24.4627%; height: 36px;"><span>=acc_product_roles.created_at&nbsp;уст. тек. дату</span></td>
<td style="width: 50.2734%; height: 36px;"><span>Дата/время создания</span></td>
</tr>
<tr style="height: 54px;">
<td style="width: 25.1704%; height: 54px;"><span>-</span></td>
<td style="width: 24.4627%; height: 54px;"><span>acc_product_roles.tid по значению параметра {tenantCode} определить ИД тенанта</span></td>
<td style="width: 50.2734%; height: 54px;"><span>ИД тенанта</span></td>
</tr>
<tr style="height: 36px;">
<td style="width: 25.1704%; height: 36px;"><span>-</span></td>
<td style="width: 24.4627%; height: 36px;"><span>acc_product_roles.id сгенерировать&nbsp;</span></td>
<td style="width: 50.2734%; height: 36px;"><span>ИД записи</span></td>
</tr>
</tbody>
</table>
8. Вернуть ответ 


#### Исключение 
<p>2а Сформировать сообщение об ошибке </p>
<p>3а Сформировать сообщение об ошибке </p>
<p>4а Сформировать сообщение об ошибке </p>
<p>5а Сформировать сообщение об ошибке </p>
<p>6а Сформировать сообщение об ошибке </p>
<p>7а Сформировать сообщение об ошибке </p>

