# GitHub Actions Workflows

This directory contains CI/CD workflows for automated building and publishing of Docker images.

## Workflows

### 1. `docker-publish.yml` - Build and Publish to Docker Hub

**Triggers:**
- Push to `master`, `main`, or `develop` branches
- Version tags (`v*.*.*`)
- Manual trigger via GitHub UI

**Actions:**
- Builds backend Docker image (`papi`)
- Builds frontend Docker image (`pfront`)
- Pushes to Docker Hub (on push to branches, not on PRs)
- Tags images with:
  - Branch name (e.g., `master`, `develop`)
  - Semantic version (e.g., `v1.2.3`, `1.2`, `1`)
  - `latest` (for default branch only)
  - Git SHA (e.g., `master-abc1234`)

**Docker Hub Images:**
- Backend: `{username}/papi:latest`
- Frontend: `{username}/pfront:latest`

### 2. `docker-build-test.yml` - Test Builds on PR

**Triggers:**
- Pull requests that modify:
  - `PoliTechAPI/**`
  - `PoliTechFront/**`
  - `docker-compose*.yml`
  - Workflow files

**Actions:**
- Tests backend build (no push)
- Tests frontend build (no push)
- Validates docker-compose files

## Setup

### Required GitHub Secrets

Configure these in: **Repository Settings → Secrets and variables → Actions**

| Secret | Description | Example |
|--------|-------------|---------|
| `DOCKERHUB_USERNAME` | Your Docker Hub username | `fant560` |
| `DOCKERHUB_TOKEN` | Docker Hub access token | `dckr_pat_...` |

### Create Docker Hub Access Token

1. Log in to [Docker Hub](https://hub.docker.com/)
2. Go to **Account Settings → Security**
3. Click **New Access Token**
4. Name: `GitHub Actions - PtDocker`
5. Permissions: **Read & Write**
6. Copy the token (save it securely!)
7. Add to GitHub Secrets as `DOCKERHUB_TOKEN`

### Configure GitHub Secrets

```bash
# Go to your repository on GitHub
# Settings → Secrets and variables → Actions → New repository secret

Name: DOCKERHUB_USERNAME
Value: your-dockerhub-username

Name: DOCKERHUB_TOKEN
Value: dckr_pat_xxxxxxxxxxxxx
```

## Usage

### Automatic Builds

**Push to master:**
```bash
git push origin master
```

Images will be built and pushed with tags:
- `{username}/papi:master`
- `{username}/papi:latest`
- `{username}/pfront:master`
- `{username}/pfront:latest`

**Create release tag:**
```bash
git tag v1.2.3
git push origin v1.2.3
```

Images will be tagged:
- `{username}/papi:v1.2.3`
- `{username}/papi:1.2`
- `{username}/papi:1`
- `{username}/papi:latest`

### Manual Trigger

1. Go to **Actions** tab in GitHub
2. Select **Build and Push Docker Images**
3. Click **Run workflow**
4. Select branch
5. Click **Run workflow**

### Pull Request Testing

When you create a PR, the workflow will:
- Build both images (no push)
- Validate docker-compose files
- Report build status in PR

## Image Tags Explained

| Tag Type | Example | When Used |
|----------|---------|-----------|
| `latest` | `papi:latest` | Default branch only |
| Branch | `papi:master` | Every push to branch |
| Version | `papi:v1.2.3` | Git tags |
| Major.Minor | `papi:1.2` | Git tags |
| Major | `papi:1` | Git tags |
| SHA | `papi:master-abc1234` | Every commit |

## Customization

### Change Image Names

Edit `.github/workflows/docker-publish.yml`:

```yaml
env:
  BACKEND_IMAGE_NAME: your-backend-name
  FRONTEND_IMAGE_NAME: your-frontend-name
```

### Change Trigger Branches

```yaml
on:
  push:
    branches:
      - master
      - production  # Add your branch
```

### Build Specific Dockerfile

```yaml
- name: Build Backend
  with:
    file: ./PoliTechAPI/Dockerfile  # Change to DockerfileMacLocal if needed
```

## Build Optimization

The workflows use:
- ✅ **BuildKit** for faster builds
- ✅ **GitHub Actions cache** for layers
- ✅ **Gradle dependency caching** for backend
- ✅ **Multi-platform support** (can add ARM64)

## Monitoring

### View Workflow Runs

1. Go to **Actions** tab in GitHub
2. Select a workflow run
3. View logs for each job

### Build Status Badge

Add to README.md:

```markdown
![Docker Build](https://github.com/your-username/PtDocker/actions/workflows/docker-publish.yml/badge.svg)
```

## Troubleshooting

### Build fails with "authentication required"

- Check `DOCKERHUB_TOKEN` is set correctly
- Regenerate token if expired
- Verify token has Read & Write permissions

### Build fails with "No space left on device"

- GitHub Actions runners have limited space
- Add cleanup step before build:

```yaml
- name: Free up space
  run: |
    docker system prune -af
    sudo rm -rf /usr/local/lib/android
```

### Build is slow

- Check if cache is working
- Consider using self-hosted runners
- Use matrix builds for parallel execution

## Advanced Features

### Multi-platform Builds (ARM64 + AMD64)

```yaml
- name: Build and push
  uses: docker/build-push-action@v5
  with:
    platforms: linux/amd64,linux/arm64
    # ... other options
```

### Slack Notifications

Add to end of workflow:

```yaml
- name: Slack Notification
  uses: 8398a7/action-slack@v3
  with:
    status: ${{ job.status }}
    webhook_url: ${{ secrets.SLACK_WEBHOOK }}
  if: always()
```

### Scan for Vulnerabilities

```yaml
- name: Scan image
  uses: aquasecurity/trivy-action@master
  with:
    image-ref: ${{ secrets.DOCKERHUB_USERNAME }}/papi:latest
    format: 'sarif'
    output: 'trivy-results.sarif'
```

## Example Workflow Run

```
✅ Checkout code
✅ Set up Docker Buildx
✅ Log in to Docker Hub
✅ Extract metadata
✅ Set up Gradle caching
✅ Build and push Backend image
   - Building (3-5 min with cache)
   - Pushing to fant560/papi:master
   - Pushing to fant560/papi:latest
✅ Build and push Frontend image
   - Building (2-3 min with cache)
   - Pushing to fant560/pfront:master
   - Pushing to fant560/pfront:latest
✅ Build summary
```

## Manual Docker Commands

To pull and run published images:

```bash
# Pull images
docker pull fant560/papi:latest
docker pull fant560/pfront:latest

# Run with docker-compose using published images
# Update docker-compose.yml:
services:
  backend:
    image: fant560/papi:latest
    # Remove build section

  frontend:
    image: fant560/pfront:latest
    # Remove build section
```

## Version Management

### Creating Releases

```bash
# Create and push version tag
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0

# This will create images:
# - fant560/papi:v1.0.0
# - fant560/papi:1.0
# - fant560/papi:1
# - fant560/papi:latest
```

### Rolling Back

```bash
# Pull specific version
docker pull fant560/papi:v1.0.0

# Update docker-compose.yml to use specific version
services:
  backend:
    image: fant560/papi:v1.0.0
```

## Cost Optimization

### Free Tier Limits (Docker Hub)

- ✅ Unlimited public repositories
- ✅ Unlimited pulls
- ⚠️ Limited to 1 private repository (free tier)

### GitHub Actions Free Tier

- ✅ 2,000 minutes/month for private repos
- ✅ Unlimited for public repos

### Reduce Build Time (Save Minutes)

```yaml
# Skip tests in CI
RUN gradle bootJar -x test

# Use smaller base images
FROM amazoncorretto:21-alpine  # ✅ Good
# vs
FROM amazoncorretto:21  # ❌ Larger
```

## Next Steps

1. ✅ Add secrets to GitHub
2. ✅ Push to master branch
3. ✅ Check Actions tab for build
4. ✅ Pull images from Docker Hub
5. ✅ Update docker-compose to use published images
