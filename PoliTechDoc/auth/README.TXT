Текущее состояние 
Авторизация

Для авторизации нужно знать
1. тенант - получается из URL в фильтре 
2. client - в заголовке или в токена
3. user_login - не обязательно. Если есть то в токена или в заголовке
4. account_id - если не передан то выбирается первое попавшееся но по алгоритму.
Для клиента без логина берется дефолтный account_id
для user берется первый с признаком isDefault, или первый любой.
Если account_id есть в заголовке x-account-id то проверяется что он легитимный и далее используется он.

Аутентификация
1. Из url получает тенант и кладем в контекст.
2. По тенанту из контекста получаем конфиг тенанта, и получаем тип аутентификации.
если JWT то достаем из токен клиента + юзера
если header то ожидаем загоовки x=partner-client-id и x-partner-user-id
NONE - не проверяет ничего. Берет из заголовка account_id = X-Account-Id" и считает это источником правды.Только для теста !!!!! 
3. по тенант, клиент, юзер вычисляем account_id
4. кладем account_id как текущий контекст выполнения
5. чисти контекст фильтров.

Entity
1. Logins ( tenant + login ) пара уникальна. проверка что есть такой логин и он не залочен. 
2. account_logins ( tenant + login + account_id ) - список account_id для текцего пользователя.
3. accounts (id, client_id, role ) - отфильтровать список из 2 по текущему client, id из этой entity и есть текущий пользлватель .


Формат ответа me
{
    "productRoles": [],
    "accountId": 173,                                acc_accounts.ID
    "isDefault": false,                              acc_account_logins.is_Default
    "clientId": 171,                                 acc_accounts.client_id
    "accountName": "TNT_ADMIN",                      acc_accounts.name
    "clientName": "Default Admin App Client",        clients.name
    "id": 177,                                       ???
    "tenantCode": "tnt",                             acc_accounts.tid
    "accounts": [],                                  accounts[]
    "userRole": "TNT_ADMIN",                         acc_accounts.node_type 
    "authorities": [                                 ???
        {
            "authority": "ROLE_TNT_ADMIN"
        }
    ],
    "username": "sirik"                              acc_login.login_name
}

Заголовки
X-Account-Id"


роли
sys_admin - tnt_admin:all:manage, tnt:all:manage
tenant_admin - tnt:own:manage, client::create client::manage

group_admin subgroup::create, group:data:view
sales : account:data:view

tnt:all
tnt:name

client:all:manage
client:name:manage 