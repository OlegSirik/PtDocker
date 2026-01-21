# 🔐 GitHub Secrets Setup

## Required Secrets

Configure these in: **GitHub Repository → Settings → Secrets and variables → Actions**

### For Docker Hub Publishing

| Secret Name | Description | How to Get |
|-------------|-------------|------------|
| `DOCKERHUB_USERNAME` | Your Docker Hub username | Your login username (e.g., `olegsirik`) |
| `DOCKERHUB_TOKEN` | Docker Hub access token | Create at hub.docker.com → Account Settings → Security |

### For SSH Deployment (Optional)

| Secret Name | Description | How to Get |
|-------------|-------------|------------|
| `SERVER_HOST` | Production server IP/hostname | Your VPS IP (e.g., `185.130.212.179`) |
| `SERVER_USER` | SSH username | Usually `root` or `ubuntu` |
| `SERVER_SSH_KEY` | Private SSH key | Generate with `ssh-keygen` |

## Step-by-Step Setup

### 1. Create Docker Hub Access Token

1. Go to [hub.docker.com](https://hub.docker.com/)
2. Log in to your account
3. Click your **profile icon** → **Account Settings**
4. Click **Security** tab
5. Click **New Access Token**
6. Fill in:
   - Description: `GitHub Actions - PtDocker`
   - Access permissions: **Read, Write, Delete**
7. Click **Generate**
8. **Copy the token** - save it somewhere safe!

Example token: `dckr_pat_abcdefghijklmnopqrstuvwxyz123456`

### 2. Add Secrets to GitHub

1. Go to your repository: `https://github.com/YOUR_USERNAME/PtDocker`
2. Click **Settings** tab
3. In left sidebar: **Secrets and variables** → **Actions**
4. Click **New repository secret**

**Add each secret:**

#### Secret 1: DOCKERHUB_USERNAME
```
Name: DOCKERHUB_USERNAME
Value: olegsirik
```
Click **Add secret**

#### Secret 2: DOCKERHUB_TOKEN
```
Name: DOCKERHUB_TOKEN
Value: dckr_pat_xxxxxxxxxxxxxxxxxxxxx
```
Click **Add secret**

#### Secret 3: SERVER_HOST (for auto-deploy)
```
Name: SERVER_HOST
Value: 185.130.212.179
```
Click **Add secret**

#### Secret 4: SERVER_USER (for auto-deploy)
```
Name: SERVER_USER
Value: root
```
Click **Add secret**

#### Secret 5: SERVER_SSH_KEY (for auto-deploy)
```
Name: SERVER_SSH_KEY
Value: (paste your private SSH key - see below)
```
Click **Add secret**

### 3. Generate SSH Key (for deployment)

On your **local machine**:

```bash
# Generate new SSH key pair
ssh-keygen -t ed25519 -C "github-actions@ptdocker" -f ~/.ssh/github_actions_deploy

# This creates two files:
# - github_actions_deploy (private key) → Add to GitHub Secret
# - github_actions_deploy.pub (public key) → Add to server
```

**Copy public key to server:**
```bash
# Copy public key content
cat ~/.ssh/github_actions_deploy.pub

# SSH to your server
ssh root@185.130.212.179

# Add to authorized_keys
echo "ssh-ed25519 AAAA... github-actions@ptdocker" >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys
```

**Copy private key to GitHub:**
```bash
# Display private key
cat ~/.ssh/github_actions_deploy

# Copy entire output including:
# -----BEGIN OPENSSH PRIVATE KEY-----
# ...
# -----END OPENSSH PRIVATE KEY-----

# Paste into GitHub Secret: SERVER_SSH_KEY
```

## Verification

### Test Docker Hub Login

```bash
# Local test
docker login -u olegsirik
# Enter your DOCKERHUB_TOKEN when prompted

# If successful, you'll see:
# Login Succeeded
```

### Test SSH Connection

```bash
# Test with the new key
ssh -i ~/.ssh/github_actions_deploy root@185.130.212.179

# If successful, you're connected to your server
```

### View Secrets in GitHub

1. Go to **Settings → Secrets and variables → Actions**
2. You should see:
   - ✅ DOCKERHUB_USERNAME
   - ✅ DOCKERHUB_TOKEN
   - ✅ SERVER_HOST
   - ✅ SERVER_USER
   - ✅ SERVER_SSH_KEY

**Note**: Secret values are hidden - you can only update/delete them.

## Security Best Practices

### ✅ Do This
- Use access tokens, never passwords
- Set token expiration (recommended: 90 days)
- Use least privilege (Read, Write only - no Delete if not needed)
- Rotate tokens regularly
- Use separate SSH key for CI/CD
- Restrict SSH key to specific IP (GitHub Actions IPs)

### ❌ Don't Do This
- Share tokens publicly
- Use your Docker Hub password
- Commit tokens to code
- Use your personal SSH key for CI/CD
- Give unlimited permissions

## Troubleshooting

### "Invalid username or password"

**Problem**: Docker Hub authentication failed

**Solutions**:
1. Verify `DOCKERHUB_USERNAME` is exact (case-sensitive)
2. Regenerate `DOCKERHUB_TOKEN`
3. Check token hasn't expired
4. Ensure token has "Write" permission

### "Permission denied (publickey)"

**Problem**: SSH authentication failed

**Solutions**:
1. Verify public key is in server's `~/.ssh/authorized_keys`
2. Check private key in `SERVER_SSH_KEY` is complete
3. Verify `SERVER_USER` is correct (root, ubuntu, etc.)
4. Check SSH port (default is 22)

### "failed to solve with frontend dockerfile"

**Problem**: Build failed in GitHub Actions

**Solutions**:
1. Test build locally first
2. Check Dockerfile paths are correct
3. Ensure all files are committed
4. Check `.dockerignore` isn't excluding needed files

## GitHub Actions Usage Limits

### Free Tier (Public Repos)
- ✅ **Unlimited** minutes
- ✅ **Unlimited** storage
- ✅ **Unlimited** concurrent jobs

### Free Tier (Private Repos)
- ⚠️ **2,000** minutes/month
- ⚠️ **500 MB** storage
- ⚠️ **20** concurrent jobs

**Estimate**: Each full build = ~15 minutes
**Monthly builds**: ~133 builds/month (within free tier)

### View Usage

**Settings → Billing → Plans and usage → Actions**

## Disable Auto-Deploy (Optional)

If you want to build images but **not** auto-deploy to production:

Edit `.github/workflows/docker-publish.yml`:

```yaml
  deploy:
    name: Deploy to Production
    runs-on: ubuntu-latest
    needs: [build-backend, build-frontend]
    if: false  # Disable auto-deploy
```

Or comment out the entire `deploy` job.

## Manual Deploy Trigger

To deploy manually after images are built:

1. Go to **Actions** tab
2. Select **Build and Push Docker Images**
3. Click **Run workflow**
4. Select `master` branch
5. Click **Run workflow**

## Success Indicators

After successful setup, pushing to master will:

1. ✅ Trigger GitHub Action
2. ✅ Build backend image (~5-8 min)
3. ✅ Build frontend image (~3-5 min)
4. ✅ Push to Docker Hub:
   - `olegsirik/papi-b:latest`
   - `olegsirik/papi-b:master`
   - `olegsirik/papi-f:latest`
   - `olegsirik/papi-f:master`
5. ✅ Deploy to production server (if enabled)
6. ✅ Services restart with new images

Check:
- GitHub Actions tab shows ✅ green check
- Docker Hub shows updated images
- Server runs new version

## Next Steps

1. ✅ Add all required secrets
2. ✅ Test Docker Hub login locally
3. ✅ Test SSH connection
4. ✅ Push to master to trigger first build
5. ✅ Monitor in Actions tab
6. ✅ Verify images on Docker Hub
