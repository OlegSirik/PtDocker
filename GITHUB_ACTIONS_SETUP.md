# 🚀 GitHub Actions Setup Guide

## Quick Setup (5 Minutes)

### Step 1: Create Docker Hub Access Token

1. Go to [Docker Hub](https://hub.docker.com/)
2. Log in with your account
3. Click your **username** → **Account Settings**
4. Click **Security** tab
5. Click **New Access Token**
6. Fill in:
   - **Description**: `GitHub Actions - PtDocker`
   - **Access permissions**: **Read, Write, Delete**
7. Click **Generate**
8. **Copy the token** (you won't see it again!)

### Step 2: Add Secrets to GitHub

1. Go to your GitHub repository: `https://github.com/YOUR_USERNAME/PtDocker`
2. Click **Settings** tab
3. In left sidebar, click **Secrets and variables** → **Actions**
4. Click **New repository secret**

**Add these two secrets:**

**Secret 1:**
- Name: `DOCKERHUB_USERNAME`
- Value: `fant560` (your Docker Hub username)
- Click **Add secret**

**Secret 2:**
- Name: `DOCKERHUB_TOKEN`
- Value: `dckr_pat_xxxxxxxxxxxxx` (paste the token from Step 1)
- Click **Add secret**

### Step 3: Commit and Push Workflow

```bash
git add .github/workflows/
git commit -m "Add GitHub Actions for Docker builds"
git push origin master
```

### Step 4: Watch Build Progress

1. Go to **Actions** tab in GitHub
2. Click on the running workflow
3. Watch the build progress

**Build time**: 5-10 minutes (first run), 2-4 minutes (cached)

## What Gets Built

After successful build, you'll have:

### Docker Hub Images

```
hub.docker.com/r/fant560/papi:latest        # Backend
hub.docker.com/r/fant560/papi:master        # Backend (master branch)

hub.docker.com/r/fant560/pfront:latest      # Frontend
hub.docker.com/r/fant560/pfront:master      # Frontend (master branch)
```

### Usage

Pull and run your published images:

```bash
# Pull latest images
docker pull fant560/papi:latest
docker pull fant560/pfront:latest

# Run with docker-compose
docker-compose up -d
```

## Workflow Files Created

### 1. `.github/workflows/docker-publish.yml`

**Main workflow** - Builds and publishes on:
- ✅ Push to master/main/develop
- ✅ Version tags (v1.2.3)
- ✅ Manual trigger

**Features:**
- Parallel builds (backend + frontend)
- GitHub Actions cache for faster rebuilds
- Automatic tagging (latest, version, sha)
- Only pushes on merge to main branches (not on PRs)

### 2. `.github/workflows/docker-build-test.yml`

**Test workflow** - Tests builds on:
- ✅ Pull requests
- ✅ Changes to Docker files

**Features:**
- Builds without pushing
- Validates docker-compose files
- Faster feedback on PRs

## Triggering Builds

### Automatic (on push)

```bash
# Any push to master triggers build
git push origin master

# Images created:
# - fant560/papi:master
# - fant560/papi:latest
# - fant560/pfront:master
# - fant560/pfront:latest
```

### Manual (via GitHub UI)

1. Go to **Actions** tab
2. Select **Build and Push Docker Images**
3. Click **Run workflow**
4. Select branch (e.g., `master`)
5. Click **Run workflow**

### Version Releases

```bash
# Create version tag
git tag -a v2.0.0 -m "Release v2.0.0"
git push origin v2.0.0

# Images created:
# - fant560/papi:v2.0.0
# - fant560/papi:2.0
# - fant560/papi:2
# - fant560/papi:latest
```

## Using Published Images

### Update docker-compose.yml

Replace build sections with image references:

```yaml
services:
  backend:
    image: fant560/papi:latest  # Instead of build: ...
    environment:
      # ... your env vars
    ports:
      - "8080:8080"

  frontend:
    image: fant560/pfront:latest  # Instead of build: ...
    environment:
      # ... your env vars
    ports:
      - "80:80"
```

### Deploy to Production

```bash
# Pull latest images
docker-compose pull

# Restart with new images
docker-compose up -d

# Or in one command
docker-compose pull && docker-compose up -d
```

## Image Tagging Strategy

| Git Action | Backend Tags | Frontend Tags |
|------------|--------------|---------------|
| Push to master | `papi:master`, `papi:latest` | `pfront:master`, `pfront:latest` |
| Push to develop | `papi:develop` | `pfront:develop` |
| Tag v1.2.3 | `papi:v1.2.3`, `papi:1.2`, `papi:1`, `papi:latest` | Same for pfront |
| Commit abc1234 | `papi:master-abc1234` | `pfront:master-abc1234` |

## Monitoring Builds

### GitHub Actions UI

1. **Actions** tab shows all workflow runs
2. Click on a run to see:
   - Build logs
   - Duration
   - Success/failure status
   - Artifacts

### Email Notifications

GitHub sends email on:
- ❌ Build failures
- ✅ Fixed builds (after previous failure)

Configure in: **Settings → Notifications → Actions**

### Status Badges

Add to `README.md`:

```markdown
![Backend Build](https://github.com/YOUR_USERNAME/PtDocker/actions/workflows/docker-publish.yml/badge.svg)
```

## Build Performance

### First Build (No Cache)
- Backend: ~8-10 minutes
- Frontend: ~5-7 minutes
- **Total**: ~15 minutes

### Subsequent Builds (With Cache)
- Backend: ~3-5 minutes (if code changed)
- Frontend: ~2-3 minutes (if code changed)
- **Total**: ~5-8 minutes

### Cache Hit (No Changes)
- Backend: ~1 minute
- Frontend: ~30 seconds
- **Total**: ~2 minutes

## Customization

### Change Image Names

Edit `.github/workflows/docker-publish.yml`:

```yaml
env:
  BACKEND_IMAGE_NAME: my-backend-name  # Default: papi
  FRONTEND_IMAGE_NAME: my-frontend-name  # Default: pfront
```

### Change Dockerfile

```yaml
- name: Build Backend
  with:
    file: ./PoliTechAPI/DockerfileMacLocal  # Use different Dockerfile
```

### Add Environment Variables to Build

```yaml
- name: Build Backend
  with:
    build-args: |
      APP_VERSION=${{ github.ref_name }}
      BUILD_DATE=${{ github.event.head_commit.timestamp }}
```

### Build Only on Tag

```yaml
on:
  push:
    tags:
      - 'v*.*.*'  # Only version tags
```

## Troubleshooting

### ❌ "authentication required"

**Problem**: Docker Hub login failed

**Solution**:
1. Verify `DOCKERHUB_USERNAME` matches your Docker Hub username exactly
2. Regenerate `DOCKERHUB_TOKEN`
3. Check token permissions include "Write"

### ❌ "repository does not exist"

**Problem**: Docker Hub repository not created

**Solution**:
1. Log in to Docker Hub
2. Create repositories:
   - `fant560/papi`
   - `fant560/pfront`
3. Set to **Public** (or **Private** if preferred)

### ❌ "Build failed: daemon disappeared"

**Problem**: Out of memory in GitHub runner

**Solution**: Add cleanup step before build:

```yaml
- name: Free up space
  run: docker system prune -af
```

### ❌ "Gradle build failed"

**Problem**: Compilation errors

**Solution**:
1. Test build locally first: `.\gradlew.bat build`
2. Fix compilation errors
3. Push fix

## Security Best Practices

### ✅ Do This

- Use access tokens, not passwords
- Set token expiration (90 days)
- Use separate tokens for different projects
- Scan images for vulnerabilities
- Use minimal base images (Alpine)

### ❌ Don't Do This

- Store passwords in code
- Use admin tokens for CI/CD
- Push images with secrets baked in
- Use `latest` tag in production

## Cost Tracking

### GitHub Actions Minutes

**Free tier**: 2,000 minutes/month (private repos)

**Estimate per build**: ~15 minutes
**Builds per month**: ~133 builds (within free tier)

**View usage**: Settings → Billing → Plans and usage

### Docker Hub

**Free tier**: 
- ✅ Unlimited public repositories
- ✅ Unlimited pulls (with rate limits)
- ⚠️ 1 private repository only

## Next Steps After Setup

1. ✅ Add secrets to GitHub
2. ✅ Push workflow files
3. ✅ Watch first build complete
4. ✅ Verify images on Docker Hub
5. ✅ Update docker-compose to use published images
6. ✅ Test pulling and running images
7. ✅ Add status badge to README

## Example: Full Deployment Flow

```bash
# 1. Make changes
git add .
git commit -m "Add new feature"

# 2. Push to master (triggers build)
git push origin master

# 3. Wait for build (~5 min)
# Check: github.com/your-repo/actions

# 4. Deploy to server
ssh production-server
docker-compose pull
docker-compose up -d

# 5. Verify
curl http://your-server/api/health
```

## Support

- [GitHub Actions Docs](https://docs.github.com/en/actions)
- [Docker Build Push Action](https://github.com/docker/build-push-action)
- [Docker Hub](https://hub.docker.com/)
