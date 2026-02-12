## Таблица: acc_logins
#### Назначение таблицы: Хранение информации о пользователях (УЗ)
#### Структура  таблицы
<table style="width: 717px; height: 313px;">
<thead>
<tr style="height: 36px;">
<th style="text-align: center; width: 133.712px; height: 36px;">Название поля</th>
<th style="text-align: center; width: 44.225px; height: 36px;">Ключ</th>
<th style="text-align: center; width: 107.488px; height: 36px;">Тип поля</th>
<th style="text-align: center; width: 129.25px; height: 36px;">Обязательность</th>
<th style="text-align: center; width: 169.113px; height: 36px;">Пример</th>
<th style="text-align: center; width: 157.55px; height: 36px;">Описание</th>
</tr>
</thead>
<tbody>
<tr style="height: 44px;">
<td style="text-align: center; width: 133.712px; height: 43px;">id</td>
<td style="text-align: center; width: 44.225px; height: 43px;">PK</td>
<td style="text-align: center; width: 107.488px; height: 43px;">BIGINT</td>
<td style="text-align: center; width: 129.25px; height: 43px;">Да</td>
<td style="text-align: center; width: 169.113px; height: 43px;"><span>2</span></td>
<td style="text-align: center; width: 157.55px; height: 43px;">ИД пользователя</td>
</tr>
<tr style="height: 36px;">
<td style="text-align: center; width: 133.712px; height: 36px;">tid</td>
<td style="text-align: center; width: 44.225px; height: 36px;">FK</td>
<td style="text-align: center; width: 107.488px; height: 36px;">BIGINT</td>
<td style="text-align: center; width: 129.25px; height: 36px;">Да</td>
<td style="text-align: center; width: 169.113px; height: 36px;"><span>1</span></td>
<td style="text-align: center; width: 157.55px; height: 36px;">Внешний ключ для связи с таблицей&nbsp;acc_tenants</td>
</tr>
<tr style="height: 36px;">
<td style="text-align: center; width: 133.712px; height: 36px;">user_login</td>
<td style="text-align: center; width: 44.225px; height: 36px;">-</td>
<td style="text-align: center; width: 107.488px; height: 36px;">VARCHAR(255)</td>
<td style="text-align: center; width: 129.25px; height: 36px;">Да</td>
<td style="text-align: center; width: 169.113px; height: 36px;"><span>ratner@vsk.ru</span></td>
<td style="text-align: center; width: 157.55px; height: 36px;">Логин пользователя</td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;"><span>password</span></td>
<td style="text-align: center; width: 44.225px; height: 36px;">-</td>
<td style="width: 12.5%; height: 18px;"><span>VARCHAR(255)</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Нет</span></td>
<td style="text-align: center; width: 169.113px; height: 36px;"><span>3fd90706aa13b38f74ba8e0ca16cd598</span></td>
<td style="width: 50%; height: 18px;"><span>Хеш пароля пользователя</span></td>
</tr>
<tr style="height: 36px;">
<td style="text-align: center; width: 133.712px; height: 36px;">full_name</td>
<td style="text-align: center; width: 44.225px; height: 36px;">-</td>
<td style="text-align: center; width: 107.488px; height: 36px;">VARCHAR(255)</td>
<td style="text-align: center; width: 129.25px; height: 36px;">Да</td>
<td style="text-align: center; width: 169.113px; height: 36px;">Ратнер Екатерина Александровна</td>
<td style="text-align: center; width: 157.55px; height: 36px;">ФИО</td>
</tr>
<tr style="height: 36px;">
<td style="text-align: center; width: 133.712px; height: 36px;">position</td>
<td style="text-align: center; width: 44.225px; height: 36px;">-</td>
<td style="text-align: center; width: 107.488px; height: 36px;">VARCHAR(255)</td>
<td style="text-align: center; width: 129.25px; height: 36px;">Нет</td>
<td style="text-align: center; width: 169.113px; height: 36px;">Страховой агент</td>
<td style="text-align: center; width: 157.55px; height: 36px;">Должность</td>
</tr>
<tr style="height: 18px;">
<td style="text-align: center; width: 133.712px; height: 18px;">is_deleted</td>
<td style="text-align: center; width: 44.225px; height: 18px;">-</td>
<td style="text-align: center; width: 107.488px; height: 18px;">BOOLEAN</td>
<td style="text-align: center; width: 129.25px; height: 18px;">Да</td>
<td style="text-align: center; width: 169.113px; height: 18px;">false</td>
<td style="text-align: center; width: 157.55px; height: 18px;">Флаг удаления. True - неактивный(удален), false - активный</td>
</tr>
<tr style="height: 18px;">
<td style="text-align: center; width: 133.712px; height: 18px;">created_at</td>
<td style="text-align: center; width: 44.225px; height: 18px;">-</td>
<td style="text-align: center; width: 107.488px; height: 18px;">TIMESTAMP</td>
<td style="text-align: center; width: 129.25px; height: 18px;">Да</td>
<td style="text-align: center; width: 169.113px; height: 18px;"><span>&nbsp;</span><code dir="ltr" class="o8j0Mc" jscontroller="PR9Qj" jsuid="y3NKp_d">2025-11-18T15:30:00Z</code></td>
<td style="text-align: center; width: 157.55px; height: 18px;">Дата/время создания&nbsp;</td>
</tr>
<tr style="height: 18px;">
<td style="text-align: center; width: 133.712px; height: 18px;">updated_at</td>
<td style="text-align: center; width: 44.225px; height: 18px;">-</td>
<td style="text-align: center; width: 107.488px; height: 18px;">TIMESTAMP</td>
<td style="text-align: center; width: 129.25px; height: 18px;">Нет</td>
<td style="text-align: center; width: 169.113px; height: 18px;"><code dir="ltr" class="o8j0Mc" jscontroller="PR9Qj" jsuid="y3NKp_d">2025-11-20T15:30:00Z</code></td>
<td style="text-align: center; width: 157.55px; height: 18px;">Дата/время обновления&nbsp;</td>
</tr>
</tbody>
</table>
<p></p>

#### SQL для создания таблицы:
~~~
CREATE TABLE IF NOT EXISTS acc_logins (
    id BIGINT PRIMARY KEY ,
    tid BIGINT NOT NULL REFERENCES acc_tenants(id),
    user_login VARCHAR(255) NOT NULL,
    password VARCHAR(255) ,
    full_name VARCHAR(255) NOT NULL,
    position VARCHAR(255),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_login, tid)
);
~~~
### Логика загрузки данных
#### Название метода: 
```
POST /tnts/{tenantCode}/logins
```
#### Назначение метода: Создание пользователя
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
<td style="width: 12.5%;"><span>string</span></td>
<td style="width: 12.5%; text-align: center;"><span>Да</span></td>
<td style="width: 50%; height: 18px;">
<p>Код тенанта</p>
<p></p>
</td>
</tr>
</tbody>
</table>
<p><em>*Комменатрий: значение&nbsp;tenantCode можно получить в таблице&nbsp;acc_tenants поле code.</em></p>
<p>body:</p>
<table border="1" style="border-collapse: collapse; width: 100%; height: 144px;">
<tbody>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px; text-align: center;"><strong>Значение параметра</strong></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><strong>Тип</strong></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><strong>Обязательность</strong></td>
<td style="width: 50%; height: 18px; text-align: center;"><strong>Описание</strong></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;"><span>userLogin</span></td>
<td style="width: 12.5%; height: 18px;"><span>string</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Да</span></td>
<td style="width: 50%; height: 18px;"><span>Логин пользователя</span></td>
</tr>
<tr style="height: 90px;">
<td style="width: 25%; height: 90px;"><span>fullName</span></td>
<td style="width: 12.5%; height: 90px;"><span>string</span></td>
<td style="width: 12.5%; text-align: center; height: 90px;"><span>Да</span></td>
<td style="width: 50%; height: 90px;"><span>ФИО пользователя</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;"><span>position</span></td>
<td style="width: 12.5%; height: 18px;"><span>string</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Нет</span></td>
<td style="width: 50%; height: 18px;"><span>Должность</span></td>
</tr>
</tbody>
</table>
<p>Пример запроса:&nbsp;</p>
<pre> {
 "userLogin": "ratner@vsk.ru", 
 "fullName": "Ратнер Екатерина Александровна", 
 "position": "Страховой агент"
  }
</pre>
<p>Выходные параметры:&nbsp;</p>
<table border="1" style="border-collapse: collapse; width: 100%; height: 216px;">
<tbody>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px; text-align: center;"><strong>Значение параметра</strong></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><strong>Тип</strong></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><strong>Обязательность</strong></td>
<td style="width: 50%; height: 18px; text-align: center;"><strong>Описание</strong></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;"><span>id</span><span><br /></span></td>
<td style="width: 12.5%; height: 18px;"><span>string</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Да</span></td>
<td style="width: 50%; height: 18px;"><span>&nbsp;ИД пользователя</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;"><span>userLogin</span></td>
<td style="width: 12.5%; height: 18px;"><span>string</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Да</span></td>
<td style="width: 50%; height: 18px;"><span>Логин пользователя</span></td>
</tr>
<tr style="height: 90px;">
<td style="width: 25%; height: 90px;"><span>fullName</span></td>
<td style="width: 12.5%; height: 90px;"><span>&nbsp;string</span></td>
<td style="width: 12.5%; text-align: center; height: 90px;"><span>Да</span></td>
<td style="width: 50%; height: 90px;"><span>ФИО пользователя</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;"><span>position</span></td>
<td style="width: 12.5%; height: 18px;"><span>string</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Нет</span></td>
<td style="width: 50%; height: 18px;"><span>Должность</span></td>
</tr>
</tbody>
</table>
<p></p>
<p>Пример ответа:&nbsp;</p>
<p>Успех, код ответа 201</p>
<pre> {
 "id": "5544888",
 "userLogin": "ratner@vsk.ru", 
 "fullName": "Ратнер Екатерина Александровна",
 "position": "Страховой агент" 
  }
</pre>

### Название сценария: Создание пользователя (логина)
#### Триггер: Вызван метод POST /tnts/{tenantCode}/logins
#### Сценарий :

1. Проверить, что заполнены обязательные параметры в теле запроса "userLogin", "fullName" и в параметре пути {tenantCode}. Если проверка пройдена, то перейти на шаг 2, иначе исключение 2а
2. Проверить по tenantCode наличие тенанта в таблице acc_tenants. Если запись найдена, то перейти на шаг 3, иначе исключение 3а
~~~
SELECT * FROM acc_tenants WHERE code = '<значение tenantCode>';
~~~
3. Проверить уникальность пользователя для данного tenantCode. Если userLogin в таблице acc_logins для данного тенанта НЕ существует, то перейти на след. шаг, иначе исключение 4а
~~~
SELECT l.* 
FROM acc_logins l
INNER JOIN tenants t ON l.tid = t.id
WHERE t.code = '<значение tenantCode>' AND l.user_login = '<значение userLogin>';
~~~
<p>4 Создать в таблице acc_logins запись</p>
<p>4.1. Поле id = сгенерировать уникальный идентификатор </p>
<p>4.2. Поле tid =  ИД тенанта из таблицы acc_tenants поле id (найти по tenantCode) </p>
<p>4.3. Поле user_login= значение из вход. параметра "userLogin" </p>
<p>4.4. Поле full_name =  значение из вход. параметра "fullName" </p>
<p>4.5. Поле position =  значение из вход. параметра "position" </p>
<p>4.6. Поле is_deleted = по умолчанию false</p>
<p>4.7. Поле created_at = время/дата текущая</p>
<p>4.8. Поле updated_at = NULL</p>
<p>5. Вернуть ответ POST/tnts/{tenantCode}/logins, где:
<ul>
<li>"id" = значение шаг 4.1 &nbsp;</li>
<li>"userLogin" = значение шаг 4.3 &nbsp;</li>
<li>"fullName"= значение шаг 4.5 &nbsp;</li>
<li>"position" = значение шаг 4.6 &nbsp;</li>
</ul>

#### Исключение 
<p>2а Сформировать сообщение об ошибке </p>
<p>3а Сформировать сообщение об ошибке </p>
<p>4а Сформировать сообщение об ошибке </p>

### Логика добавления/обновления пароля для пользователя
#### Название метода: 
```
POST /api/auth/set-password
```
#### Назначение метода: Добавление/обновление пароля
#### Важно: для добавления пароля у пользователя должна быть назначенаа роль SYS_ADMIN
<p>Входные параметры&nbsp;</p>
<p>body:</p>
<table border="1" style="border-collapse: collapse; width: 100%; height: 216px;">
<tbody>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;"><span>userLogin</span></td>
<td style="width: 12.5%; height: 18px;"><span>string</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Да</span></td>
<td style="width: 50%; height: 18px;"><span>Логин пользователя</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;"><span>password</span></td>
<td style="width: 12.5%; height: 18px;"><span>string</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Да</span></td>
<td style="width: 50%; height: 18px;"><span>Пароль пользователя</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;"><span>clientId</span></td>
<td style="width: 12.5%; height: 18px;"><span>string</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Да</span></td>
<td style="width: 50%; height: 18px;"><span>client_id партнера</span></td>
</tr>
</tbody>
</table>
<p>Пример запроса:&nbsp;</p>
<pre> {
 "userLogin": "ratner@vsk.ru", 
 "password": "rKIbv677P0",    
 "clientId": "2"
  }
</pre>
<p>Пример ответа:&nbsp;</p>
<p>Успех, код ответа 200</p>
<pre> {
 "success": true,
    "message": "Password set successfully for user: sys_admin"
  }
</pre>
<p>Доступ запрещен, код ответа 403</p>
<pre> {
 "Forbidden" 
  }
</pre>

### Название сценария: Добавление пароля
#### Триггер: Вызван метод POST /api/auth/set-password
#### Сценарий :

1. Проверить, что заполнены обязательные параметры в теле запроса "userLogin", "password", "clientId". Если проверка пройдена, то перейти на шаг 2, иначе исключение 2а
2. Проверить по userLogin наличие пользователя в таблице acc_logins. Если запись найдена, то перейти на шаг 3, иначе исключение 3а
~~~
SELECT * FROM acc_logins WHERE user_login = '<значение userLogin>';
~~~
3. Проверить по clientId наличие пользователя для данного клиента/партнера в таблице acc_account_logins. Если проверка пройдена, то перейти на шаг 4, иначе исключение 4а
~~~
SELECT * FROM acc_account_logins WHERE user_login = '<значение userLogin>' AND clientId = '<значение clientId>'
~~~
4. Для найденного пользователя заполнить поле "password" = хеш пароля из вход. параметра  "password"
5. Вернуть ответ POST /api/auth/set-password

#### Исключение 
<p>2а Сформировать сообщение об ошибке </p>
<p>3а Сформировать сообщение об ошибке </p>
<p>4а Сформировать сообщение об ошибке </p>

### Логика обновления данных
#### Название метода: 
```
PATCH/tnts/{tenantCode}/logins/{id}
```
Входящие параметры
<p><span>path</span>:&nbsp;</p>
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
<td style="width: 12.5%;"><span>string</span></td>
<td style="width: 12.5%; text-align: center;"><span>Да</span></td>
<td style="width: 50%; height: 18px;">
<p>Код тенанта </p>
<p></p>
</td>
</tr>
<tr>
<td style="width: 25%;"><span>id</span></td>
<td style="width: 12.5%;"><span>string</span></td>
<td style="width: 12.5%; text-align: center;"><span>Да</span></td>
<td style="width: 50%;">
<p>Идентификатор пользователя</p>
</td>
</tr>
</tbody>
</table>
<p><em>Комментарий: Значение tenantCode можно получить в таблице acc_tenants поле code, значение&nbsp;<span>id в таблице&nbsp;acc_logins поле id</span></em>

<p>body:</p>
<table border="1" style="border-collapse: collapse; width: 100%; height: 144px;">
<tbody>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px; text-align: center;"><strong>Значение параметра</strong></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><strong>Тип</strong></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><strong>Обязательность</strong></td>
<td style="width: 50%; height: 18px; text-align: center;"><strong>Описание</strong></td>
</tr>
<tr style="height: 90px;">
<td style="width: 25%; height: 90px;"><span>fullName</span></td>
<td style="width: 12.5%; height: 90px;"><span>string</span></td>
<td style="width: 12.5%; text-align: center; height: 90px;"><span>Нет</span></td>
<td style="width: 50%; height: 90px;"><span>ФИО пользователя</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;"><span>position</span></td>
<td style="width: 12.5%; height: 18px;"><span>string</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Нет</span></td>
<td style="width: 50%; height: 18px;"><span>Должность</span></td>
</tr>
<tr>
<td style="width: 25%;">isDeleted</td>
<td style="width: 12.5%;"><span>bool</span></td>
<td style="width: 12.5%; text-align: center;"><span>Нет</span></td>
<td style="width: 50%;">
<p>Флаг удаления&nbsp;</p>
</td>
</tr>
</tbody>
</table>

<p>Пример запроса:&nbsp;</p>
<p>PATCH /tnts/VSK/logins/775988</p>
<pre> {   
 "fullName": "Стрельцова Екатерина Александровна", 
 "position": "Старший страховой агент"
  }
</pre>
<p>Выходные параметры:&nbsp;</p>

<p><span>body</span>:&nbsp;</p>
<table border="1" style="border-collapse: collapse; width: 100%; height: 216px;">
<tbody>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px; text-align: center;"><strong>Значение параметра</strong></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><strong>Тип</strong></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><strong>Обязательность</strong></td>
<td style="width: 50%; height: 18px; text-align: center;"><strong>Описание</strong></td>
</tr>
<tr style="height: 90px;">
<td style="width: 25%; height: 90px;"><span>fullName</span></td>
<td style="width: 12.5%; height: 90px;"><span>string</span></td>
<td style="width: 12.5%; text-align: center; height: 90px;"><span>Нет</span></td>
<td style="width: 50%; height: 90px;"><span>ФИО пользователя</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;"><span>position</span></td>
<td style="width: 12.5%; height: 18px;"><span>string</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Нет</span></td>
<td style="width: 50%; height: 18px;"><span>Должность</span></td>
</tr>
<tr>
<td style="width: 25%;">isDeleted</td>
<td style="width: 12.5%;"><span>bool</span></td>
<td style="width: 12.5%; text-align: center;"><span>Нет</span></td>
<td style="width: 50%;">
<p>Флаг удаления&nbsp;</p>
</td>
</tr>
</tbody>
</table>

<p>Пример ответа:&nbsp;</p>
<p>Успех, код ответа 200</p>
<pre> {
 "fullName": "Стрельцова Екатерина Александровна", 
 "position": "Старший страховой агент"
  }
</pre>

### Название сценария:  Обновление данных пользователя
#### Триггер: Вызван метод /tnts/{tenantCode}/logins/{id}
#### Сценарий :
1. Проверить по tenantCode наличие тенанта в таблице acc_tenants. Если запись найдена, то перейти на шаг 2, иначе исключение 2а
~~~
SELECT * FROM acc_tenants WHERE code = '<значение tenantCode>';
~~~
2. Проверить по id наличие пользователя в таблице acc_logins. Если запись найдена, то перейти на шаг 3, иначе исключение 3а
~~~
SELECT * FROM acc_logins WHERE id = '<значение id>';
~~~
3. Проверить наличие пользователя для данного tenantCode. Если id в таблице acc_logins для данного тенанта существует, то перейти на след. шаг, иначе исключение 4а
~~~
SELECT l.* 
FROM acc_logins l
INNER JOIN tenants t ON l.tid = t.id
WHERE t.code = '<значение tenantCode>' AND l.id = '<значение id>';
~~~
4. Обновить данные в таблице acc_logins на основании запроса в разерезе конкретного ИД пользователя. Маппинг для обновления:
<p>4.1 табл.acc_logins.full_name = fullName</p>
<p>2.2 табл.acc_logins.position = position</p>
<p>2.3 табл.acc_logins.is_deleted = isDeleted.</p>
<p>2.4 табл.acc_logins.updated_at = Время/дата текущая </p>
3. Вернуть ответ:
<p><em>*в зависмости от переданных параметров</em></p>
<ul>
<li><span>fullName</span></li>
<li><span>position</span></li>
<li><span>isDeleted</span></li>
</ul> 

#### Исключение 
<p>2а Сформировать сообщение об ошибке </p>
<p>3а Сформировать сообщение об ошибке </p>
<p>4а Сформировать сообщение об ошибке </p>

### Логика получения данных
#### Название метода: 
```
GET /tnts/{tenantCode}/logins
```
#### Назначние метода: Получение всех пользователей (УЗ)
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
<td style="width: 12.5%;"><span>string</span></td>
<td style="width: 12.5%; text-align: center;"><span>Да</span></td>
<td style="width: 50%; height: 18px;">
<p>Код тенанта</p>
<p></p>
</td>
</tr>
</tbody>
</table>

<p>Выходные параметры&nbsp;</p>
<p>body:</p>
<table border="1" style="border-collapse: collapse; width: 100%; height: 144px;">
<tbody>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px; text-align: center;"><strong>Значение параметра</strong></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><strong>Тип</strong></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><strong>Обязательность</strong></td>
<td style="width: 50%; height: 18px; text-align: center;"><strong>Описание</strong></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;"><span>id</span><span><br /></span></td>
<td style="width: 12.5%; height: 18px;"><span>string</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Да</span></td>
<td style="width: 50%; height: 18px;"><span>&nbsp;ИД пользователя</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;"><span>userLogin</span></td>
<td style="width: 12.5%; height: 18px;"><span>string</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Да</span></td>
<td style="width: 50%; height: 18px;"><span>Логин пользователя</span></td>
</tr>
<tr style="height: 90px;">
<td style="width: 25%; height: 90px;"><span>fullName</span></td>
<td style="width: 12.5%; height: 90px;"><span>string</span></td>
<td style="width: 12.5%; text-align: center; height: 90px;"><span>Да</span></td>
<td style="width: 50%; height: 90px;"><span>ФИО пользователя</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;"><span>position</span></td>
<td style="width: 12.5%; height: 18px;"><span>string</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Нет</span></td>
<td style="width: 50%; height: 18px;"><span>Должность</span></td>
</tr>
<tr style="height: 46px;">
<td style="width: 25%; height: 46px;">isDeleted</td>
<td style="width: 12.5%; height: 46px;"><span>bool</span></td>
<td style="width: 12.5%; text-align: center; height: 46px;">Да</td>
<td style="width: 50%; height: 46px;">
<p>Флаг удаления</p>
</td>
</tr>
<tr style="height: 46px;">
<td style="width: 25%; height: 46px;">createdAt</td>
<td style="width: 12.5%; height: 46px;"><span>string</span></td>
<td style="width: 12.5%; text-align: center; height: 46px;">Да</td>
<td style="width: 50%; height: 46px;">
<p>Дата/время создания</p>
</td>
</tr>
<tr style="height: 46px;">
<td style="width: 25%; height: 46px;">updatedAt</td>
<td style="width: 12.5%; height: 46px;"><span>string</span></td>
<td style="width: 12.5%; text-align: center; height: 46px;">Да</td>
<td style="width: 50%; height: 46px;">
<p>Дата/время обновления</p>
</td>
</tr>
</tbody>
</table>
<p>Пример ответа:&nbsp;</p>
<p>Успех, код ответа 200</p>
<pre> [{
 "id": "5544888",
 "userLogin": "ratner@vsk.ru", 
 "fullName": "Ратнер Екатерина Александровна",
 "position": "Страховой агент",
 "isDeleted": "false",
 "createdAt": "2025-11-20T15:30:00Z",
 "updatedAt": "2025-12-20T15:30:00Z"
  },
 "id": "34001",
 "userLogin": "volgova@vsk.ru", 
 "fullName": "Волгова Екатерина Александровна",
 "position": "Страховой агент",
 "isDeleted": "true",
 "createdAt": "2025-11-20T15:30:00Z",
 "updatedAt": "2025-12-230T15:30:00Z"
  }  
    ]
</pre>

### Название сценария: Получение всех пользователей (УЗ)
#### Триггер: Вызван метод GET /tnts/{tenantCode}/logins
#### Сценарий :
1. Проверить по tenantCode наличие УЗ по данному тенанту в таблице acc_logins. Если запись найдена, то перейти на шаг 2, иначе исключение 2а
~~~
SELECT
    t.id AS tenant_id,
    t.code AS tenant_code,
    l.logins_id
FROM
    acc_tenants t
JOIN
    acc_logins c ON l.tid = t.id
WHERE
    t.code = 'tenantCode из запроса'
~~~
2. Получить данные из таблицы acc_logins, где указан определнный тенант tenantCode 
~~~
SELECT *
l.id,
l.user_login
l.full_name,
l.position,
l.is_deleted,
l.created_at,
l.updated_at
FROM
    acc_logins l
JOIN
    acc_tenants t ON t.id = l.tid 
WHERE
    t.сode = 'tenantCode из запроса'
~~~
3. Выполнить маппинг
   <ul>
<li>acc_logins.id = id&nbsp;</li>
<li>acc_logins.full_name=&nbsp;fullName</li>
<li>acc_logins.position = position</li>
<li>acc_clients.is_deleted = isDeleted</li>
<li>acc_clients.created_at = createdAt</li>
<li>acc_clients.updated_at =&nbsp;updatedAt</li>
</ul>
4. Вернуть ответ GET /tnts/{tenantCode}/logins

#### Исключение 
<p>2а Сформировать сообщение об ошибке </p>

### Логика удаления УЗ
#### Название метода: 
```
DELETE /tnts/{tenantCode}/logins/{id}
```
#### Назначние метода: Удаление УЗ пользователя
Входящие параметры
<p><span>path</span>:&nbsp;</p>
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
<td style="width: 12.5%;"><span>string</span></td>
<td style="width: 12.5%; text-align: center;"><span>Да</span></td>
<td style="width: 50%; height: 18px;">
<p>Код тенанта </p>
<p></p>
</td>
</tr>
<tr>
<td style="width: 25%;"><span>id</span></td>
<td style="width: 12.5%;"><span>string</span></td>
<td style="width: 12.5%; text-align: center;"><span>Да</span></td>
<td style="width: 50%;">
<p>Идентификатор пользователя</p>
</td>
</tr>
</tbody>
</table>
<p><em>Комментарий: Значение tenantCode можно получить в таблице acc_tenants поле code, значение&nbsp;<span>id в таблице&nbsp;acc_logins поле id</span></em>

<p>Выходные параметры:&nbsp;</p>
<p><span>body</span>:&nbsp;</p>
<table border="1" style="border-collapse: collapse; width: 100%; height: 216px;">
<tbody>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px; text-align: center;"><strong>Значение параметра</strong></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><strong>Тип</strong></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><strong>Обязательность</strong></td>
<td style="width: 50%; height: 18px; text-align: center;"><strong>Описание</strong></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;"><span>id</span><span><br /></span></td>
<td style="width: 12.5%; height: 18px;"><span>string</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Да</span></td>
<td style="width: 50%; height: 18px;"><span>&nbsp;ИД пользователя</span></td>
</tr>
<tr style="height: 18px;">
<td style="width: 25%; height: 18px;"><span>userLogin</span></td>
<td style="width: 12.5%; height: 18px;"><span>string</span></td>
<td style="width: 12.5%; text-align: center; height: 18px;"><span>Да</span></td>
<td style="width: 50%; height: 18px;"><span>Логин пользователя</span></td>
</tr>
<tr>
<td style="width: 25%;">isDeleted</td>
<td style="width: 12.5%;"><span>bool</span></td>
<td style="width: 12.5%; text-align: center;"><span>Да</span></td>
<td style="width: 50%;">
<p>Флаг удаления&nbsp;</p>
</td>
</tr>
</tbody>
</table>

<p>Пример ответа:&nbsp;</p>
<p>Успех, код ответа 201</p>
<pre> {
 "id": "5544888",
 "userLogin": "ratner@vsk.ru", 
 "isDeleted": true 
  }
</pre>

### Название сценария: Удаление УЗ
#### Триггер: Вызван метод DELETE /tnts/{tenantCode}/logins/{id}
#### Сценарий :
1. 1. Проверить по tenantCode наличие тенанта в таблице acc_tenants. Если запись найдена, то перейти на шаг 2, иначе исключение 2а
~~~
SELECT * FROM acc_tenants WHERE code = '<значение tenantCode>';
~~~
2. Проверить по id наличие пользователя в таблице acc_logins. Если запись найдена, то перейти на шаг 3, иначе исключение 3а
~~~
SELECT * FROM acc_logins WHERE id = '<значение id>';
~~~
3. Проверить наличие пользователя для данного tenantCode. Если id в таблице acc_logins для данного тенанта существует, то перейти на след. шаг, иначе исключение 4а
~~~
SELECT l.* 
FROM acc_logins l
INNER JOIN tenants t ON l.tid = t.id
WHERE t.code = '<значение tenantCode>' AND l.id = '<значение id>';
~~~
4. Обновить данные в таблице acc_logins в разерезе конкретного ИД пользователя: табл.acc_logins.is_deleted = true
5. Вернуть ответ:
<ul>
<li><span>id</span></li>
<li><span>user_login</span></li>
<li><span>isDeleted</span></li>
</ul> 

#### Исключение 
<p>2а Сформировать сообщение об ошибке </p>
<p>3а Сформировать сообщение об ошибке </p>
<p>4а Сформировать сообщение об ошибке </p>
