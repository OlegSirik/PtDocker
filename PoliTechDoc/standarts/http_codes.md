# HTTP Code

## Категории кодов ответа

<table><thead><tr><th width="165">Категория</th><th width="117">Диапазон</th><th>Описание</th></tr></thead><tbody><tr><td>Successful</td><td>2xx</td><td>Запрос был получен, проверен и принят в обработку</td></tr><tr><td>Clienr error</td><td>4xx</td><td>Запрос содержит ошибки и не может быть выполнен из-за ошибок клиента</td></tr><tr><td>Server error</td><td>5xx</td><td>Сервер не смог обработать годный запрос</td></tr></tbody></table>

## Используемые коды ошибок

<table><thead><tr><th width="113">Код</th><th width="224">Наименовние</th><th>Описание</th></tr></thead><tbody><tr><td>200</td><td>OK</td><td>Request succeeded</td></tr><tr><td>201</td><td>Created</td><td>Resource created successfully</td></tr><tr><td>204</td><td>No Content</td><td>Successful request with no response body</td></tr><tr><td>400</td><td>Bad Request</td><td>Malformed request syntax or invalid parameters</td></tr><tr><td>401</td><td>Unauthorized</td><td>Authentication required and not provided</td></tr><tr><td>403</td><td>Forbidden</td><td>Client lacks access rights</td></tr><tr><td>404</td><td>Not Found</td><td>Resource not found</td></tr><tr><td>409</td><td>Conflict</td><td>Request conflicts with current state</td></tr><tr><td>422</td><td>Unprocessable Entity</td><td>Request format is correct but content is invalid</td></tr><tr><td>429</td><td>Too Many Requests</td><td>Rate limit exceeded</td></tr><tr><td>500</td><td>Internal Server Error</td><td>Generic server error</td></tr><tr><td>503</td><td>Service Unavailable</td><td>Service temporarily unavailable</td></tr></tbody></table>
