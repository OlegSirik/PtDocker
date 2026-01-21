# Manual Deployment Guide

## Quick Deploy from Docker Hub

### On Production Server

```bash
# Navigate to project directory
cd ~/PtDocker

# Pull latest code (contains docker-compose-published.yml)
git pull origin master

# Pull latest images from Docker Hub
docker pull olegsirik/papi-b:latest
docker pull olegsirik/papi-f:latest

# Stop existing containers
docker-compose -f docker-compose-published.yml --env-file docker-compose.env down

# Start with published images
docker-compose -f docker-compose-published.yml --env-file docker-compose.env up -d

# Wait for services to initialize
sleep 30

# Check status
docker-compose -f docker-compose-published.yml --env-file docker-compose.env ps

# Check backend health
curl http://localhost:8080/actuator/health

# View logs if needed
docker-compose -f docker-compose-published.yml logs -f backend
```

## What's the Difference?

### `docker-compose.yml` (Development)
- **Builds** images locally from source code
- Use for: Local development
- Command: `docker-compose up --build`

### `docker-compose-published.yml` (Production)
- **Uses** pre-built images from Docker Hub
- Use for: Production deployment
- Command: `docker-compose -f docker-compose-published.yml up -d`

## Files Comparison

**docker-compose.yml:**
```yaml
backend:
  build:
    context: ./PoliTechAPI
    dockerfile: Dockerfile
```

**docker-compose-published.yml:**
```yaml
backend:
  image: olegsirik/papi-b:latest  # ← Uses published image
```

## Complete Deployment Workflow

```bash
# 1. Pull latest images
docker pull olegsirik/papi-b:latest
docker pull olegsirik/papi-f:latest

# 2. Stop old containers
docker-compose -f docker-compose-published.yml --env-file docker-compose.env down

# 3. Start new containers
docker-compose -f docker-compose-published.yml --env-file docker-compose.env up -d

# 4. Verify deployment
docker-compose -f docker-compose-published.yml --env-file docker-compose.env ps
curl http://localhost:8080/actuator/health

# 5. Monitor logs
docker-compose -f docker-compose-published.yml logs -f
```

## Troubleshooting

### Containers not starting

```bash
# Check logs
docker-compose -f docker-compose-published.yml logs backend
docker-compose -f docker-compose-published.yml logs frontend

# Check if ports are already in use
netstat -tlnp | grep -E '80|8080|5432'

# Restart specific service
docker-compose -f docker-compose-published.yml restart backend
```

### Images not pulling

```bash
# Verify Docker Hub connectivity
docker pull hello-world

# Login to Docker Hub (if using private repos)
docker login

# Pull with explicit tag
docker pull olegsirik/papi-b:latest
docker pull olegsirik/papi-f:latest
```

### Environment variables not loading

```bash
# Check env file exists
ls -la docker-compose.env

# Test with explicit file
docker-compose -f docker-compose-published.yml --env-file docker-compose.env config

# View resolved configuration
docker-compose -f docker-compose-published.yml --env-file docker-compose.env config | grep image
```

## Rollback to Previous Version

```bash
# Pull specific version
docker pull olegsirik/papi-b:v1.0.0
docker pull olegsirik/papi-f:v1.0.0

# Update docker-compose-published.yml temporarily
# Change: image: olegsirik/papi-b:latest
# To:     image: olegsirik/papi-b:v1.0.0

# Restart
docker-compose -f docker-compose-published.yml --env-file docker-compose.env up -d
```

## One-Line Deploy

```bash
docker pull olegsirik/papi-b:latest && docker pull olegsirik/papi-f:latest && docker-compose -f docker-compose-published.yml --env-file docker-compose.env down && docker-compose -f docker-compose-published.yml --env-file docker-compose.env up -d && sleep 30 && docker-compose -f docker-compose-published.yml --env-file docker-compose.env ps
```

## Automated via GitHub Actions

When you push to `master`, GitHub Actions automatically:

1. ✅ Builds images
2. ✅ Pushes to Docker Hub
3. ✅ SSHs to production server
4. ✅ Pulls latest images
5. ✅ Restarts containers with `docker-compose-published.yml`
6. ✅ Verifies deployment

**No manual intervention needed!**

## Services

After deployment:
- **Backend**: http://YOUR_SERVER:8080
- **Frontend**: http://YOUR_SERVER:80
- **Database**: PostgreSQL on port 5432 (internal)

## Health Checks

```bash
# Backend health
curl http://localhost:8080/actuator/health

# Frontend
curl http://localhost:80

# Database connection (from backend container)
docker-compose -f docker-compose-published.yml exec backend curl postgres:5432
```

## Cleanup

```bash
# Remove old images
docker image prune -f

# Remove stopped containers
docker container prune -f

# Full cleanup (⚠️ removes volumes!)
docker-compose -f docker-compose-published.yml down -v
docker system prune -af
```
