# Схема данных


<p align="center">
 <img width="1000px" src="pic01.jpg" />
</p>

## Список разделов (tenant). 
Хранит список разделов. В большинстве случаев, будет 1 запись.

~~~
CREATE TABLE IF NOT EXISTS acc_tenants (
    id BIGINT PRIMARY KEY DEFAULT nextval('account_seq'),
    name VARCHAR(250),
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
~~~

## Список клиентов, партнеров.
Справочник подключенных партнеров, с аттрибутами настройки подключения.
Партнерское подключение создается в рамках tenant, и видно только в нем.
client_id - из JWT токена.
default_account_id - id портфеля, в который будут попадать договоры, если не указан целевой портфель. JWT не содержит user_id.

~~~
CREATE TABLE IF NOT EXISTS acc_clients (
    tid BIGINT NOT NULL REFERENCES acc_tenants(id),
    id BIGINT PRIMARY KEY DEFAULT nextval('account_seq'),
    client_id varchar(255) NOT NULL,
    default_account_id BIGINT,
    name VARCHAR(250),
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
~~~

## Структура доступа к данным ( портфели )

~~~
CREATE TABLE IF NOT EXISTS acc_accounts (
    id BIGINT PRIMARY KEY,
    tid BIGINT NOT NULL REFERENCES acc_tenants(id),
    client_id BIGINT REFERENCES acc_clients(id),
    parent_id BIGINT REFERENCES acc_accounts(id),
    node_type VARCHAR(10) NOT NULL,
    name VARCHAR(250),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
~~~

## Настройка доступов к продуктам
Содержит привязку портфеля к продукту. Для каждого продукта задается список прав на действия, которые с ним можно проводить.
Чтение, расчет, создание договора, пролонгация и т.д.
~~~
-- Product roles table
CREATE TABLE IF NOT EXISTS acc_product_roles (
    id BIGINT PRIMARY KEY ,
    tid BIGINT NOT NULL REFERENCES acc_tenants(id),
    account_id BIGINT NOT NULL REFERENCES acc_accounts(id),
    role_product_id BIGINT NOT NULL,
    role_account_id BIGINT NOT NULL REFERENCES acc_accounts(id),
    is_deleted BOOLEAN DEFAULT FALSE,
    can_read BOOLEAN DEFAULT FALSE,
    can_quote BOOLEAN DEFAULT FALSE,
    can_policy BOOLEAN DEFAULT FALSE,
    can_addendum BOOLEAN DEFAULT FALSE,
    can_cancel BOOLEAN DEFAULT FALSE,
    can_prolongate BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
~~~

## Список пользователей
Список пользователей, которые могут иметь доступ к системе.
Включает в себя логин, уникальный в рамках своего раздела, и обзую информацию о владельце этой учетки.
```
-- Account logins table
CREATE TABLE IF NOT EXISTS acc_logins (
    id BIGINT PRIMARY KEY ,
    tid BIGINT NOT NULL REFERENCES acc_tenants(id),
    user_login VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    UNIQUE (user_login, tid)
);
```
## Привязка пользователя к account
Пользователи и портфели имею связь много ко многим.
Так же указывается, в рамках какой интеграции (client_id), пользователь имеет доступ.
Если в токене не указано, к какому портфелю нужен доступ, то берется дефолтный портфель с isDefault = true
Такой признак можно проставить только у одного портфеля в рамках client_id

```
-- Account logins table
CREATE TABLE IF NOT EXISTS acc_account_logins (
    id BIGINT PRIMARY KEY ,
    tid BIGINT NOT NULL REFERENCES acc_tenants(id),
    user_login VARCHAR(255) NOT NULL,
    client_id BIGINT NOT NULL REFERENCES acc_clients(id),
    is_default BOOLEAN DEFAULT FALSE,
    account_id BIGINT NOT NULL REFERENCES acc_accounts(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tid, user_login) REFERENCES acc_logins(tid, user_login),
    UNIQUE (user_login, account_id)
);
```
## Доступ к портфелю по коду доступа.
Иногда продажи проводятся на публичных сайтах, но есть потребность привязать продажи к разным портфелям.
Тогда можно сгенерить разные коды, раздать их продавцам, чтобы они вводили их при продаже и настроить этот код на определенный портфель.
Доступ только на запись. Получить данные портфеля по этому какналу не разрешено.
```
-- Account tokens table
CREATE TABLE IF NOT EXISTS acc_account_tokens (
    id BIGINT PRIMARY KEY,
    tid BIGINT NOT NULL REFERENCES acc_tenants(id),
    token VARCHAR(255) NOT NULL,
    client_id BIGINT NOT NULL REFERENCES acc_clients(id),
    account_id BIGINT NOT NULL REFERENCES acc_accounts(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (token, client_id)
);
```



