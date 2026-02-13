# Сборка и запуск с BuildKit (кэш Gradle/npm между сборками)
# Использование: .\up-local.ps1

$env:DOCKER_BUILDKIT = 1
$compose = "docker compose --env-file docker-compose-local.env -f docker-compose-external-db.yml"

# Параллельная сборка backend + frontend, затем запуск
docker compose --env-file docker-compose-local.env -f docker-compose-external-db.yml build --parallel
docker compose --env-file docker-compose-local.env -f docker-compose-external-db.yml up -d
