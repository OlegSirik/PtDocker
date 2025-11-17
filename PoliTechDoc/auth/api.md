# API

https://app.swaggerhub.com/apis/insur/PoliTechAccounts/2

## Релизы
1 - must have
2 - ready to prod
3 - nice to have

## Tenant
```
/tnts/
```
Сразу не нужно. Можно заполнить БД инсертом.

<table><thead>
  <tr><th width="120">Роль</th><th>Описание</th><th>Определение</th></tr>
</thead><tbody>
  <tr><td>Операция</td><td>Описание</td><td>Появится в релизе</td></tr>
  <tr><td>GET</td><td>Список тенантов. </td><td>2</td></tr>
  <tr><td>POST</td><td>Создать тенант. </td><td>2</td></tr>
  <tr><td>PUT</td><td>Изменить тенант. </td><td>3</td></tr>
  <tr><td>DELETE</td><td>Удалить тенант. </td><td>3</td></tr>
</tbody></table>

## Client
```
/tnts/{tenantId}/clients
```
Настройка интеграции, подключения партнера.
В 1 версии поддерживаем только партнеркую интеграцию, без поддержки пользовательских учеток.

<table><thead>
  <tr><th width="120">Роль</th><th>Описание</th><th>Определение</th></tr>
</thead><tbody>
  <tr><td>Операция</td><td>Описание</td><td>Появится в релизе</td></tr>
  <tr><td>GET</td><td>Список подключенных систем. </td><td>2</td></tr>
  <tr><td>POST</td><td>Создать подключение. </td><td>2</td></tr>
  <tr><td>PUT</td><td>Изменить данные подключения. </td><td>3</td></tr>
  <tr><td>DELETE</td><td>Удалить подключение. </td><td>3</td></tr>
</tbody></table>

## Users
```
/tnts/{tenantId}/users
```
Список подключенных пользователей.
Сразу не нужно.

<table><thead>
  <tr><th width="120">Роль</th><th>Описание</th><th>Определение</th></tr>
</thead><tbody>
  <tr><td>Операция</td><td>Описание</td><td>Появится в релизе</td></tr>
  <tr><td>GET</td><td>Список пользователей. </td><td>2</td></tr>
  <tr><td>POST</td><td>Создать учетку. </td><td>2</td></tr>
  <tr><td>PUT</td><td>Изменить данные учетки. </td><td>3</td></tr>
  <tr><td>DELETE</td><td>Удалить учетку. </td><td>2</td></tr>
</tbody></table>

## Tokens
```
/tnts/{tenantId}/tokens
```
Список кодов для анонимных продаж.
Сразу не нужно.

<table><thead>
  <tr><th width="120">Роль</th><th>Описание</th><th>Определение</th></tr>
</thead><tbody>
  <tr><td>Операция</td><td>Описание</td><td>Появится в релизе</td></tr>
  <tr><td>GET</td><td>Список. </td><td>3</td></tr>
  <tr><td>POST</td><td>Создать. </td><td>3</td></tr>
  <tr><td>PUT</td><td>Изменить данные. </td><td>3</td></tr>
  <tr><td>DELETE</td><td>Удалить. </td><td>3</td></tr>
</tbody></table>

## Accounts
```
/tnts/{tenantId}/clients/{clientId}/accounts
```
Страховой портфель. Основная единица авторизации.

<table><thead>
  <tr><th width="120">Роль</th><th>Описание</th><th>Определение</th></tr>
</thead><tbody>
  <tr><td>Операция</td><td>Описание</td><td>Появится в релизе</td></tr>
  <tr><td>GET</td><td>Данные по объекту account. Для UI в основном. </td><td>2</td></tr>
  <tr><td>POST</td><td>Создать новый account. </td><td>2</td></tr>
  <tr><td>PUT</td><td>Изменить данные. </td><td>3</td></tr>
  <tr><td>DELETE</td><td>Удалить. </td><td>3</td></tr>
</tbody></table>

## Accounts
```
/tnts/{tenantId}/clients/{clientId}/accounts/{accountId}/sub
```
Настройка прав для страхового портфеля. 

<table><thead>
  <tr><th width="120">Роль</th><th>Описание</th><th>Определение</th></tr>
</thead><tbody>
  <tr><td>Операция</td><td>Описание</td><td>Появится в релизе</td></tr>
  <tr><td>GET</td><td>Данные по объекту account. Для UI в основном. </td><td>3</td></tr>
  <tr><td>POST</td><td>Создать новый account. </td><td>3</td></tr>
  <tr><td>PUT</td><td>Изменить данные. </td><td>3</td></tr>
  <tr><td>DELETE</td><td>Удалить. </td><td>3</td></tr>
</tbody></table>

## Me
```
/tnts/{tenantId}/me/account
```
Данные текущего account текущего пользователя. Нужно сразу.

```
/tnts/{tenantId}/me/roles
```
Список ролей текущего account текущего пользователя. Нужно сразу.






