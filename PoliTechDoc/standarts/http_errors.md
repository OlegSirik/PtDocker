# Формат ошибок

### Problem Detail Properties <a href="#problem-detail-properties" id="problem-detail-properties"></a>

<table><thead><tr><th width="141">Property</th><th width="403">Description</th><th>Required</th></tr></thead><tbody><tr><td>code</td><td>Числовой код ошибки</td><td>No</td></tr><tr><td>message</td><td>Краткое описание ошибки</td><td>Yes</td></tr><tr><td><code>errors</code></td><td>Детализация ошибки</td><td>No</td></tr><tr><td><code>domain</code></td><td>Где произошла ошибка</td><td>No</td></tr><tr><td><code>reason</code></td><td>Причина ошибки</td><td>No</td></tr><tr><td>message</td><td>Описание ошибки</td><td>No</td></tr><tr><td>field</td><td>Место в данных, вызвавшее ошибку</td><td>No</td></tr></tbody></table>



### Problem Detail Example <a href="#problem-detail-example" id="problem-detail-example"></a>

```
// Some code
{ "code": 400,
  "message": "Страхователь должен быть старше 18 лет",
  "errors": [
    { "domain": "policy",
      "field": "policyHolder.dateOfBirth"
    }
  ]
}

```
