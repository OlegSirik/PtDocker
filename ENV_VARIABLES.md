# Environment Variables Reference

## Quick Setup

1. **Copy example to create your .env file:**
   ```bash
   # For local development
   cp env.example.txt .env
   
   # For production
   cp env.production.example.txt .env.production
   ```

2. **Update sensitive values** (marked with ⚠️)

3. **Run with environment file:**
   ```bash
   docker-compose --env-file .env up
   # or
   docker-compose --env-file docker-compose.env up
   ```

## Environment Files

| File | Purpose | Commit to Git? |
|------|---------|----------------|
| `env.example.txt` | Template for local development | ✅ Yes |
| `env.production.example.txt` | Template for production | ✅ Yes |
| `docker-compose.env` | Current values (can be dev or prod) | ✅ Yes (with dummy values) |
| `.env` | Your actual local values | ❌ **NO** |
| `.env.production` | Your actual production values | ❌ **NO** |

## Required Variables

### 🔐 Security (Critical)

```bash
# JWT Secret - MUST be changed in production!
# Generate with: openssl rand -base64 64
JWT_SECRET=CHANGE_ME_STRONG_RANDOM_64_CHARS

# Keycloak Admin Password
KEYCLOAK_ADMIN_PASSWORD=CHANGE_ME_STRONG_PASSWORD
```

### 🔑 Keycloak Admin API

```bash
# Server URL
KEYCLOAK_SERVER_URL=http://localhost:8000

# Admin realm (usually 'master')
KEYCLOAK_ADMIN_REALM=master

# Admin credentials
KEYCLOAK_ADMIN_USER=admin
KEYCLOAK_ADMIN_PASSWORD=admin

# Admin client ID
KEYCLOAK_ADMIN_CLIENT=admin-cli

# Default realm for creating clients/users
KEYCLOAK_DEFAULT_REALM=politech
```

### 🌐 Keycloak Frontend (Angular)

```bash
# Frontend uses these for user authentication
KEYCLOAK_URL=http://localhost:8000
KEYCLOAK_REALM=politech
KEYCLOAK_CLIENT_ID=politech-web
```

### 💾 Database

```bash
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB=pt-db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres@!123
```

### 🔗 Application

```bash
# Backend API URL (for frontend)
BASE_URL=http://localhost:8080

# Payment gateway
VSK_ROOT_URL=http://v-partapi-apps-p1:9191/
```

## Usage Examples

### Local Development

```bash
# docker-compose.env or .env
BASE_URL=http://localhost:8080
KEYCLOAK_SERVER_URL=http://localhost:8000
KEYCLOAK_ADMIN_PASSWORD=admin
JWT_SECRET=local-dev-secret-key-123456789
```

### Production

```bash
# .env.production
BASE_URL=https://api.yourcompany.com
KEYCLOAK_SERVER_URL=https://auth.yourcompany.com
KEYCLOAK_ADMIN_PASSWORD=veryStrongRandomPassword123!@#
JWT_SECRET=base64EncodedRandomString64CharsLongGeneratedWithOpenSSL
```

### Kubernetes

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: politech-secrets
type: Opaque
stringData:
  KEYCLOAK_ADMIN_PASSWORD: "your-password"
  JWT_SECRET: "your-jwt-secret"
  POSTGRES_PASSWORD: "your-db-password"
---
apiVersion: v1
kind: ConfigMap
metadata:
  name: politech-config
data:
  KEYCLOAK_SERVER_URL: "https://auth.yourcompany.com"
  BASE_URL: "https://api.yourcompany.com"
```

## Security Best Practices

### ✅ Do This

- Store `.env` files locally only (never commit)
- Use secrets manager in production (AWS Secrets Manager, Vault, etc.)
- Rotate secrets regularly
- Use HTTPS in production
- Generate strong random secrets
- Use different secrets per environment
- Set restrictive file permissions: `chmod 600 .env`

### ❌ Never Do This

- Commit .env files to git
- Use default passwords in production
- Share secrets in plain text (email, Slack, etc.)
- Reuse secrets across environments
- Use weak or short secrets
- Log sensitive values

## Generate Secure Secrets

```bash
# Linux/macOS
openssl rand -base64 64

# Windows PowerShell
-join ((65..90) + (97..122) + (48..57) | Get-Random -Count 64 | ForEach-Object {[char]$_})

# Online (use with caution)
# https://passwordsgenerator.net/
```

## Validation

Check if variables are loaded:

```bash
# Docker
docker-compose config

# In running container
docker exec -it politech-backend env | grep KEYCLOAK
```

## Troubleshooting

**Variables not loading?**
1. Check file path in `--env-file`
2. Verify file format (no spaces around `=`)
3. Restart Docker Compose
4. Check for typos in variable names

**Keycloak connection fails?**
1. Verify `KEYCLOAK_SERVER_URL` is reachable
2. Check admin credentials
3. Ensure realm exists
4. Check network connectivity

## See Also

- `ENVIRONMENT_SETUP.md` - Detailed configuration guide
- `README_KEYCLOAK.md` - Keycloak service documentation
- `docker-compose.env` - Current environment values
