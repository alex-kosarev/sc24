# Разработка проектов со Spring (2024)

## Keycloak

Запуск в Docker:

```shell
docker run --name selmag-keycloak -p 8082:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin -v ./config/keycloak/import:/opt/keycloak/data/import quay.io/keycloak/keycloak:23.0.4 start-dev --import-realm
```

## PostgreSQL

Запуск в Docker:

```shell
docker run --name catalogue-db -p 5432:5432 -e POSTGRES_USER=catalogue -e POSTGRES_PASSWORD=catalogue -e POSTGRES_DB=catalogue postgres:16
```

## MongoDB

Запуск в Docker:

```shell
docker run --name feedback-db -p 27017:27017 mongo:7
```