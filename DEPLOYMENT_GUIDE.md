# 🚀 Deployment Guide - Docker Hub + GitHub Actions

## Overview

Automated CI/CD pipeline that:
1. ✅ Builds Docker images on every push to `master`
2. ✅ Publishes to Docker Hub
3. ✅ Auto-deploys to production server (optional)

## Quick Start

### 1️⃣ Setup GitHub Secrets (One-time)

**Required secrets** (add in GitHub repo settings):

```
DOCKERHUB_USERNAME = olegsirik
DOCKERHUB_TOKEN = dckr_pat_xxxxx (create at hub.docker.com)
SERVER_HOST = 185.130.212.179 (optional, for auto-deploy)
SERVER_USER = root (optional, for auto-deploy)
SERVER_SSH_KEY = (your private SSH key, optional)
```

See `.github/SETUP_SECRETS.md` for detailed instructions.

### 2️⃣ Push to Master

```bash
git add .
git commit -m "Your changes"
git push origin master
```

**GitHub Actions will automatically:**
- Build backend image → `olegsirik/papi-b:latest`
- Build frontend image → `olegsirik/papi-f:latest`
- Push to Docker Hub
- Deploy to production (if SSH secrets configured)

**Build time**: 5-10 minutes (first build), 2-4 minutes (cached)

### 3️⃣ Deploy on Production Server

```bash
# Pull latest images
docker pull olegsirik/papi-b:latest
docker pull olegsirik/papi-f:latest

# Start with published images
docker-compose -f docker-compose-published.yml --env-file docker-compose.env up -d

# Or update existing docker-compose.yml to use published images
```

## Files Created

### GitHub Actions Workflows

| File | Purpose |
|------|---------|
| `.github/workflows/docker-image.yml` | **Main workflow** - Build, push, deploy |
| `.github/workflows/docker-publish.yml` | **Advanced workflow** - Multi-tag support |
| `.github/workflows/docker-build-test.yml` | **Test workflow** - Validates PRs |
| `.github/workflows/README.md` | Workflow documentation |

### Docker Compose Files

| File | Purpose |
|------|---------|
| `docker-compose-published.yml` | **Use published images** from Docker Hub |
| `docker-compose.yml` | Build images locally |
| `docker-compose-external-db.yml` | External database setup |

### Documentation

| File | Purpose |
|------|---------|
| `.github/SETUP_SECRETS.md` | Step-by-step secrets configuration |
| `GITHUB_ACTIONS_SETUP.md` | Complete GitHub Actions guide |
| `DEPLOYMENT_GUIDE.md` | This file - deployment overview |

## Deployment Strategies

### Strategy 1: Auto-Deploy (Recommended for Dev/Staging)

**Setup:**
1. Add SSH secrets to GitHub
2. Push to master
3. Wait for build (~5-10 min)
4. Auto-deploys to production

**Pros**: Fully automated, zero manual steps
**Cons**: Deploys every push (may be too aggressive for production)

### Strategy 2: Manual Deploy (Recommended for Production)

**Setup:**
1. Build images via GitHub Actions (auto on push)
2. Manually deploy when ready

**Deploy:**
```bash
# On production server
ssh root@185.130.212.179

# Pull latest images
docker pull olegsirik/papi-b:latest
docker pull olegsirik/papi-f:latest

# Deploy
cd ~/PtDocker
docker-compose --env-file docker-compose.env pull
docker-compose --env-file docker-compose.env up -d

# Check status
docker-compose ps
```

**Pros**: Control over when to deploy
**Cons**: Requires manual SSH

### Strategy 3: Version Tags (Recommended for Production)

**Create release:**
```bash
git tag -a v2.0.0 -m "Release v2.0.0"
git push origin v2.0.0
```

**Images created:**
- `olegsirik/papi-b:v2.0.0`
- `olegsirik/papi-b:2.0`
- `olegsirik/papi-b:2`
- `olegsirik/papi-b:latest`

**Deploy specific version:**
```yaml
# docker-compose.yml
services:
  backend:
    image: olegsirik/papi-b:v2.0.0  # Pin to specific version
```

**Pros**: Rollback-friendly, version control
**Cons**: Requires tagging

## Workflows Comparison

### Workflow 1: `docker-image.yml` (Simpler)

**Features:**
- ✅ Builds both images
- ✅ Pushes to Docker Hub
- ✅ Auto-deploys via SSH
- ✅ Single job (faster)

**Use for**: Simple CI/CD, quick deployments

### Workflow 2: `docker-publish.yml` (Advanced)

**Features:**
- ✅ Parallel builds (faster)
- ✅ Multi-tag support (version, sha, branch)
- ✅ Metadata extraction
- ✅ Conditional deployment
- ✅ PR testing without push

**Use for**: Complex versioning, multiple environments

## Production Deployment Checklist

### Before First Deploy

- [ ] Create Docker Hub repositories:
  - `olegsirik/papi-b` (public or private)
  - `olegsirik/papi-f` (public or private)
- [ ] Add GitHub secrets (DOCKERHUB_USERNAME, DOCKERHUB_TOKEN)
- [ ] Test build locally: `docker-compose build`
- [ ] Configure environment variables in `docker-compose.env`

### Deploying

- [ ] Push to master (triggers build)
- [ ] Wait for GitHub Action to complete (~5-10 min)
- [ ] Verify images on Docker Hub
- [ ] Pull images on production server
- [ ] Update docker-compose configuration
- [ ] Start services: `docker-compose up -d`
- [ ] Verify health: `curl http://YOUR_SERVER:8080/actuator/health`

### After Deploy

- [ ] Monitor logs: `docker-compose logs -f backend`
- [ ] Check service status: `docker-compose ps`
- [ ] Test API endpoints
- [ ] Verify frontend loads
- [ ] Monitor resource usage: `docker stats`

## Rollback Procedure

### Quick Rollback

```bash
# On production server
cd ~/PtDocker

# Pull previous version
docker pull olegsirik/papi-b:v1.9.0
docker pull olegsirik/papi-f:v1.9.0

# Update docker-compose.yml to use old version
# Then restart
docker-compose up -d
```

### Emergency Rollback

```bash
# Stop services
docker-compose down

# Revert to previous commit
git reset --hard HEAD~1

# Rebuild locally
docker-compose build
docker-compose up -d
```

## Monitoring

### GitHub Actions

**View build status:**
- Go to **Actions** tab
- See running/completed builds
- View logs for debugging

**Email notifications:**
- Automatic on build failures
- Configure in GitHub settings

### Docker Hub

**View images:**
1. Go to hub.docker.com/r/olegsirik/papi-b
2. See all tags (latest, master, versions)
3. Check image size, last updated

**Monitor pulls:**
- See download statistics
- Set up webhooks for events

### Production Server

**Monitor services:**
```bash
# Service status
docker-compose ps

# Resource usage
docker stats

# Logs
docker-compose logs -f backend
docker-compose logs -f frontend

# Health check
curl http://localhost:8080/actuator/health
```

## CI/CD Workflow

```
Developer               GitHub Actions           Docker Hub          Production
---------               --------------           ----------          ----------
   │                                                                       
   │ git push master                                                      
   ├────────────────────►                                                 
   │                     │                                                
   │                     │ Build Backend                                 
   │                     │ Build Frontend                                
   │                     │                                                
   │                     ├─────────────────────► Push images             
   │                     │                       (papi-b:latest)          
   │                     │                       (papi-f:latest)          
   │                     │                                                
   │                     │ SSH Deploy ──────────────────────────────────►
   │                     │                                        │       
   │                     │                                        │ Pull images
   │                     │                                        │ Restart services
   │                     │                                        │       
   │◄────────────────────┤                                        ✅ Live
   │ ✅ Build complete   │                                                
   │                     │                                                
```

## Environment-Specific Configs

### Development (Local)

```bash
# Build locally, don't use Docker Hub
docker-compose -f docker-compose-external-db.yml --env-file docker-compose-local.env up -d --build
```

### Staging (Using Docker Hub)

```bash
# Use published images with staging env
docker-compose -f docker-compose-published.yml --env-file docker-compose-staging.env up -d
```

### Production (Using Docker Hub)

```bash
# Use published images with production env
docker-compose -f docker-compose-published.yml --env-file docker-compose.env up -d
```

## Updating Images

### Update Backend Only

```bash
# On production server
docker pull olegsirik/papi-b:latest
docker-compose up -d backend

# Or via GitHub Actions
# Push changes → auto-builds → manual deploy
```

### Update Frontend Only

```bash
docker pull olegsirik/papi-f:latest
docker-compose up -d frontend
```

### Update Both

```bash
docker-compose pull
docker-compose up -d
```

## Best Practices

### Development Workflow

```bash
# 1. Create feature branch
git checkout -b feature/new-feature

# 2. Make changes and test locally
docker-compose -f docker-compose-external-db.yml up -d --build

# 3. Create PR (triggers test build)
git push origin feature/new-feature

# 4. Merge to master (triggers production build)
git checkout master
git merge feature/new-feature
git push origin master

# 5. Monitor build in GitHub Actions
# 6. Verify images on Docker Hub
# 7. Deploy to production
```

### Versioning

```bash
# Before major release
git tag -a v2.0.0 -m "Major release: Keycloak integration"
git push origin v2.0.0

# This creates:
# - olegsirik/papi-b:v2.0.0
# - olegsirik/papi-b:2.0
# - olegsirik/papi-b:2
# - olegsirik/papi-b:latest
```

### Production Deploy

```bash
# Always use specific versions in production
image: olegsirik/papi-b:v2.0.0  # Not :latest

# Pull, backup, deploy
docker-compose pull
docker-compose up -d
docker-compose ps
```

## Troubleshooting CI/CD

### Build Fails in GitHub Actions

1. Check **Actions** tab for error logs
2. Test build locally first
3. Verify secrets are set correctly
4. Check Dockerfile paths

### Images Not Updating on Server

```bash
# Force pull new image
docker pull olegsirik/papi-b:latest --no-cache

# Recreate containers
docker-compose up -d --force-recreate backend
```

### Deployment Fails

```bash
# Check SSH connection
ssh -i ~/.ssh/github_actions_deploy root@185.130.212.179

# Check server disk space
df -h

# Check Docker is running
docker ps
```

## Support & Documentation

- **GitHub Actions**: `.github/workflows/README.md`
- **Secrets Setup**: `.github/SETUP_SECRETS.md`
- **Environment Config**: `ENV_VARIABLES.md`
- **Docker Build Issues**: Check existing workflow logs in Actions tab

## Summary

✅ **Workflows created** in `.github/workflows/`
✅ **Auto-build** on push to master
✅ **Auto-publish** to Docker Hub (`olegsirik/papi-b`, `olegsirik/papi-f`)
✅ **Auto-deploy** via SSH (if secrets configured)
✅ **docker-compose-published.yml** for using Docker Hub images

**Next**: Add secrets to GitHub and push to master! 🚀
