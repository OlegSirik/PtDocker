# Environment Configuration Guide

## Overview

This application uses environment variables for configuration to keep sensitive data out of version control and enable environment-specific settings.

## Quick Start

### Local Development

1. Copy the example environment file:
   ```bash
   cp .env.example .env
   ```

2. Update `.env` with your local values

3. Run with Docker Compose:
   ```bash
   docker-compose --env-file .env up
   ```

### Production

1. Copy the production example:
   ```bash
   cp .env.production.example .env.production
   ```

2. **IMPORTANT**: Update all `CHANGE_ME` values with strong, random secrets

3. Use a secrets manager (AWS Secrets Manager, Azure Key Vault, HashiCorp Vault, etc.) for production

## Environment Variables

### 🔐 Security (Required in Production)

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `JWT_SECRET` | Secret key for JWT tokens | ⚠️ Change in production! | Yes |
| `JWT_EXPIRATION` | JWT token expiration (ms) | 86400000 (24h) | Yes |
| `KEYCLOAK_ADMIN_PASSWORD` | Keycloak admin password | ⚠️ Change in production! | Yes |

### 🔑 Keycloak Admin API

Used by the backend to manage Keycloak clients and users.

| Variable | Description | Default |
|----------|-------------|---------|
| `KEYCLOAK_SERVER_URL` | Keycloak server URL | http://localhost:8000 |
| `KEYCLOAK_ADMIN_REALM` | Admin realm | master |
| `KEYCLOAK_ADMIN_USER` | Admin username | admin |
| `KEYCLOAK_ADMIN_PASSWORD` | Admin password | ⚠️ **MUST CHANGE** |
| `KEYCLOAK_ADMIN_CLIENT` | Admin client ID | admin-cli |
| `KEYCLOAK_DEFAULT_REALM` | Default realm for operations | politech |

### 🌐 Keycloak Frontend

Used by the Angular frontend for user authentication.

| Variable | Description | Default |
|----------|-------------|---------|
| `KEYCLOAK_URL` | Keycloak URL (for frontend) | http://localhost:8000 |
| `KEYCLOAK_REALM` | Keycloak realm | politech |
| `KEYCLOAK_CLIENT_ID` | Frontend client ID | politech-web |

### 💾 Database

| Variable | Description | Default |
|----------|-------------|---------|
| `POSTGRES_HOST` | PostgreSQL host | localhost |
| `POSTGRES_PORT` | PostgreSQL port | 5432 |
| `POSTGRES_DB` | Database name | pt-db |
| `POSTGRES_USER` | Database user | postgres |
| `POSTGRES_PASSWORD` | Database password | ⚠️ **MUST CHANGE** |

### 🔗 Application URLs

| Variable | Description | Default |
|----------|-------------|---------|
| `BASE_URL` | Backend API URL | http://localhost:8080 |

## Security Best Practices

### 🚨 Critical: Never Commit Secrets!

```bash
# Add to .gitignore (already done)
.env
.env.local
.env.production
.env.*.local
```

### ✅ Production Checklist

- [ ] Generate strong random JWT_SECRET (64+ characters)
- [ ] Use unique Keycloak admin password
- [ ] Use HTTPS for all URLs (https://, not http://)
- [ ] Store secrets in a secrets manager
- [ ] Rotate secrets regularly
- [ ] Use read-only database users where possible
- [ ] Enable database SSL/TLS
- [ ] Set restrictive CORS origins

### 🔐 Generate Secure Secrets

```bash
# Generate JWT secret (Linux/macOS)
openssl rand -base64 64

# Generate JWT secret (Windows PowerShell)
[Convert]::ToBase64String((1..64 | ForEach-Object { Get-Random -Maximum 256 }))

# Generate random password
openssl rand -base64 32
```

## Docker Compose

### With .env file

```bash
docker-compose --env-file .env up
```

### With specific environment

```bash
# Development
docker-compose --env-file .env up

# Production
docker-compose --env-file .env.production up
```

### Override for specific services

```yaml
# docker-compose.override.yml
services:
  backend:
    environment:
      - KEYCLOAK_SERVER_URL=http://custom-keycloak:8000
```

## Kubernetes / Cloud Deployment

### Using Secrets

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: politech-secrets
type: Opaque
stringData:
  jwt-secret: "your-secret-here"
  keycloak-admin-password: "your-password-here"
  postgres-password: "your-db-password-here"
```

### Using ConfigMap

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: politech-config
data:
  KEYCLOAK_SERVER_URL: "https://auth.yourcompany.com"
  KEYCLOAK_DEFAULT_REALM: "politech-prod"
  BASE_URL: "https://api.yourcompany.com"
```

## Validation

Check if environment variables are loaded:

```bash
# In container
docker exec -it politech-backend env | grep KEYCLOAK

# Expected output:
# KEYCLOAK_SERVER_URL=http://localhost:8000
# KEYCLOAK_ADMIN_REALM=master
# ...
```

## Troubleshooting

### Variables not loading

1. Check .env file exists and is readable
2. Verify docker-compose is using correct --env-file
3. Check for typos in variable names
4. Ensure no quotes around values (unless needed for spaces)

### Keycloak connection fails

1. Verify KEYCLOAK_SERVER_URL is reachable
2. Check admin credentials are correct
3. Ensure default realm exists in Keycloak
4. Check network connectivity between services

## References

- [Spring Boot External Configuration](https://docs.spring.io/spring-boot/reference/features/external-config.html)
- [Docker Compose Environment Variables](https://docs.docker.com/compose/environment-variables/)
- [Keycloak Admin REST API](https://www.keycloak.org/docs-api/latest/rest-api/index.html)
