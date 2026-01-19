# Stage 1: Build Angular app
FROM --platform=linux/amd64 node:20-alpine AS build
WORKDIR /app

# Upgrade npm to latest stable version
RUN npm install -g npm@11.7.0

# Install dependencies first (better caching)
COPY package*.json ./
RUN npm ci

# Copy source and build
COPY . .
RUN npm run build

# Stage 2: Nginx to serve the app
FROM --platform=linux/amd64 nginx:alpine AS runtime

# Copy SPA-friendly nginx config
COPY docker/nginx.conf /etc/nginx/conf.d/default.conf

# Copy built artifacts from Angular dist (Angular 18 default output path)
COPY --from=build /app/dist/ng-front4/browser /usr/share/nginx/html
# Copy runtime env templating assets
COPY public/assets/env.template.js /usr/share/nginx/html/assets/env.template.js
COPY docker/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

EXPOSE 80
ARG BASE_URL
ENV BASE_URL=${BASE_URL}
ENTRYPOINT ["/entrypoint.sh"]
