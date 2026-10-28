/**
 * Сборка OpenAPI 3 YAML для sales quotes/policies по JSON-скелету схемы договора.
 */

/** JSON-значение (скелет metadata) → OpenAPI Schema Object. */
export function jsonToOpenApiSchema(value: unknown): Record<string, unknown> {
  if (value === null || value === undefined) {
    return { type: 'string', nullable: true };
  }
  if (typeof value === 'boolean') {
    return { type: 'boolean' };
  }
  if (typeof value === 'number') {
    return { type: 'number' };
  }
  if (typeof value === 'string') {
    return { type: 'string' };
  }
  if (Array.isArray(value)) {
    const item = value.length > 0 ? jsonToOpenApiSchema(value[0]) : { type: 'object' };
    return { type: 'array', items: item };
  }
  if (typeof value === 'object') {
    const props: Record<string, unknown> = {};
    for (const [k, v] of Object.entries(value as Record<string, unknown>)) {
      props[k] = jsonToOpenApiSchema(v);
    }
    return {
      type: 'object',
      properties: props,
      additionalProperties: false,
    };
  }
  return { type: 'string' };
}

export interface ProductSalesOpenApiParams {
  serverUrl: string;
  tenantCode: string;
  productCode: string;
  productName: string;
  versionNo: number | string;
  /** Скелет/пример тела запроса (схема договора). */
  contractDocument: unknown;
}

/** Минимальный OpenAPI 3.0 YAML: server + POST quotes/policies + schema. */
export function buildProductSalesOpenApiYaml(p: ProductSalesOpenApiParams): string {
  const schema = jsonToOpenApiSchema(p.contractDocument);
  const title = p.productName || p.productCode || 'Product';
  const base = p.serverUrl.replace(/\/$/, '');
  const tenant = p.tenantCode || 'tenant';
  const quotesPath = `/api/v1/${tenant}/sales/quotes`;
  const policiesPath = `/api/v1/${tenant}/sales/policies`;

  const doc = {
    openapi: '3.0.3',
    info: {
      title: `${title} — Sales API`,
      version: String(p.versionNo ?? '1'),
      description: `Автогенерация для продукта ${p.productCode}. Тело запроса = схема договора (INSURANCE_CONTRACT).`,
    },
    servers: [{ url: base, description: 'Current server' }],
    paths: {
      [quotesPath]: {
        post: {
          tags: ['sales'],
          summary: 'Create quote',
          operationId: 'createQuote',
          security: [{ bearerAuth: [] as string[] }],
          requestBody: {
            required: true,
            content: {
              'application/json': {
                schema: { $ref: '#/components/schemas/InsuranceContract' },
                example: p.contractDocument,
              },
            },
          },
          responses: {
            '200': {
              description: 'Quote result (JSON)',
              content: {
                'application/json': {
                  schema: { type: 'object', additionalProperties: true },
                },
              },
            },
          },
        },
      },
      [policiesPath]: {
        post: {
          tags: ['sales'],
          summary: 'Create policy',
          operationId: 'createPolicy',
          security: [{ bearerAuth: [] as string[] }],
          requestBody: {
            required: true,
            content: {
              'application/json': {
                schema: { $ref: '#/components/schemas/InsuranceContract' },
                example: p.contractDocument,
              },
            },
          },
          responses: {
            '200': {
              description: 'Policy result (JSON)',
              content: {
                'application/json': {
                  schema: { type: 'object', additionalProperties: true },
                },
              },
            },
          },
        },
      },
    },
    components: {
      securitySchemes: {
        bearerAuth: {
          type: 'http',
          scheme: 'bearer',
          bearerFormat: 'JWT',
        },
      },
      schemas: {
        InsuranceContract: schema,
      },
    },
  };

  return toYaml(doc) + '\n';
}

function toYaml(value: unknown, indent = 0): string {
  const pad = '  '.repeat(indent);
  if (value === null || value === undefined) {
    return 'null';
  }
  if (typeof value === 'boolean' || typeof value === 'number') {
    return String(value);
  }
  if (typeof value === 'string') {
    return yamlQuote(value);
  }
  if (Array.isArray(value)) {
    if (value.length === 0) {
      return '[]';
    }
    return value
      .map((item) => {
        if (isPlainObject(item) || Array.isArray(item)) {
          const nested = toYaml(item, indent + 1);
          const lines = nested.split('\n');
          const first = `${pad}- ${lines[0].trimStart()}`;
          const rest = lines
            .slice(1)
            .map((line) => `${pad}  ${line.trimStart()}`)
            .join('\n');
          return rest ? `${first}\n${rest}` : first;
        }
        return `${pad}- ${toYaml(item, 0)}`;
      })
      .join('\n');
  }
  if (isPlainObject(value)) {
    const entries = Object.entries(value as Record<string, unknown>);
    if (entries.length === 0) {
      return '{}';
    }
    return entries
      .map(([k, v]) => {
        const key = needsYamlKeyQuote(k) ? yamlQuote(k) : k;
        if (v !== null && typeof v === 'object') {
          const nested = toYaml(v, indent + 1);
          if (nested === '[]' || nested === '{}') {
            return `${pad}${key}: ${nested}`;
          }
          return `${pad}${key}:\n${nested}`;
        }
        return `${pad}${key}: ${toYaml(v, 0)}`;
      })
      .join('\n');
  }
  return yamlQuote(String(value));
}

function isPlainObject(v: unknown): v is Record<string, unknown> {
  return v !== null && typeof v === 'object' && !Array.isArray(v);
}

function needsYamlKeyQuote(key: string): boolean {
  return /[:#{}[\],&*!|>'"%@`/]/.test(key) || key.includes(' ') || /^\d/.test(key);
}

function yamlQuote(s: string): string {
  if (s === '') {
    return '""';
  }
  if (/^[\w.-]+$/.test(s) && !/^(true|false|null|yes|no)$/i.test(s)) {
    return s;
  }
  return JSON.stringify(s);
}
