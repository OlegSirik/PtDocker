# Docker image for ng-front4

Repository: `olegsirik/vsk-robot-zaytsev-front`

## Build locally

```bash
# from project root
docker build -t olegsirik/vsk-robot-zaytsev-front:latest .
```

## Run locally

```bash
docker run --rm -p 8080:80 olegsirik/vsk-robot-zaytsev-front:latest
# open http://localhost:8080
```

## Push to Docker Hub

```bash
# login first if needed
docker login

# push latest tag
docker push olegsirik/vsk-robot-zaytsev-front:latest
```

Notes:
- Multi-stage build compiles Angular app and serves via Nginx
- SPA routing is supported via `try_files ... /index.html`
- If output path changes, update COPY path in Dockerfile accordingly
