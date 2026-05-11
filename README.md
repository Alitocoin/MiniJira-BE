# MiniJira-BE

API REST del sistema MiniJira. Gestiona tareas, proyectos y usuarios con autenticacion JWT. Construida sobre Spring Boot 3 y PostgreSQL.

---

## Stack

| Tecnologia        | Version  | Rol                              |
|-------------------|----------|----------------------------------|
| Java              | 17       | Lenguaje                         |
| Spring Boot       | 3.2.5    | Framework principal              |
| Spring Security   | (boot)   | Filtros de autenticacion         |
| Spring Data JPA   | (boot)   | Capa de persistencia             |
| PostgreSQL        | 15+      | Base de datos principal          |
| H2                | (boot)   | Base de datos en memoria (tests) |
| jjwt              | 0.12.6   | Generacion y validacion de JWT   |
| Maven Wrapper     | -        | Build tool                       |
| JUnit 5           | (boot)   | Framework de tests               |
| Mockito           | (boot)   | Mocking en tests unitarios       |

---

## Variables de entorno

Definidas en `src/main/resources/application.properties`. Todas tienen valor por defecto para desarrollo local; en produccion se deben sobreescribir via variables de entorno del sistema operativo o del runtime (Cloud Run, Kubernetes, etc.).

| Variable               | Descripcion                                            | Default / Ejemplo                                   |
|------------------------|--------------------------------------------------------|-----------------------------------------------------|
| `SERVER_PORT`          | Puerto en que escucha la API                           | `8080`                                              |
| `DB_URL`               | JDBC URL de PostgreSQL                                 | `jdbc:postgresql://localhost:5432/minijira`         |
| `DB_USERNAME`          | Usuario de la base de datos                            | `postgres`                                          |
| `DB_PASSWORD`          | Contrasena de la base de datos                         | `postgres`                                          |
| `JPA_DDL_AUTO`         | Estrategia DDL de Hibernate                            | `update` (dev) — usar `validate` en produccion      |
| `CORS_ALLOWED_ORIGINS` | Origenes CORS permitidos, separados por coma           | `http://localhost:5173,http://localhost:3000`        |
| `JWT_SECRET`           | Clave para firmar tokens JWT (minimo 32 caracteres)    | Cambiar obligatoriamente antes de deployar          |
| `JWT_EXPIRATION`       | Duracion del token en milisegundos                     | `86400000` (24 horas)                               |

> **Produccion**: nunca uses los defaults de `DB_PASSWORD` ni `JWT_SECRET`. Injectalos como secrets del entorno de ejecucion.

---

## Como levantar localmente

**Prerequisitos:** Java 17 instalado, Maven (o usar el wrapper `./mvnw` incluido), PostgreSQL corriendo.

```bash
# 1. Clonar el repositorio
git clone <url-del-repo>
cd MiniJira-BE

# 2. Crear la base de datos en PostgreSQL
createdb minijira

# 3. Exportar variables de entorno (ajusta segun tu entorno local)
export DB_URL=jdbc:postgresql://localhost:5432/minijira
export DB_USERNAME=postgres
export DB_PASSWORD=tu_password
export JWT_SECRET=una-clave-secreta-de-minimo-32-caracteres-aqui
export CORS_ALLOWED_ORIGINS=http://localhost:3000

# 4. Levantar la API
./mvnw spring-boot:run
```

La API queda disponible en `http://localhost:8080`. Las tablas se crean automaticamente al iniciar gracias a la estrategia DDL `update` de Hibernate.

---

## Endpoints principales

Los endpoints bajo `/api/tasks/*`, `/api/users/*` y `/api/projects/*` requieren el header:

```
Authorization: Bearer <token>
```

El token se obtiene en `/api/auth/login` o `/api/auth/register`.

### Autenticacion — `/api/auth` (publico)

| Metodo | Ruta                  | Descripcion               |
|--------|-----------------------|---------------------------|
| POST   | `/api/auth/register`  | Registra un nuevo usuario |
| POST   | `/api/auth/login`     | Autentica y devuelve JWT  |

### Tareas — `/api/tasks` (requiere Bearer token)

| Metodo | Ruta                     | Descripcion                  |
|--------|--------------------------|------------------------------|
| GET    | `/api/tasks`             | Lista todas las tareas       |
| GET    | `/api/tasks/{id}`        | Detalle de una tarea         |
| POST   | `/api/tasks`             | Crea una tarea               |
| PUT    | `/api/tasks/{id}`        | Reemplaza una tarea completa |
| PATCH  | `/api/tasks/{id}/status` | Actualiza solo el status     |
| DELETE | `/api/tasks/{id}`        | Elimina una tarea            |

### Usuarios — `/api/users` (requiere Bearer token)

| Metodo | Ruta              | Descripcion              |
|--------|-------------------|--------------------------|
| GET    | `/api/users`      | Lista todos los usuarios |
| GET    | `/api/users/{id}` | Detalle de un usuario    |
| POST   | `/api/users`      | Crea un usuario          |
| PUT    | `/api/users/{id}` | Actualiza un usuario     |
| DELETE | `/api/users/{id}` | Elimina un usuario       |

### Proyectos — `/api/projects` (requiere Bearer token)

| Metodo | Ruta                 | Descripcion               |
|--------|----------------------|---------------------------|
| GET    | `/api/projects`      | Lista todos los proyectos |
| GET    | `/api/projects/{id}` | Detalle de un proyecto    |
| POST   | `/api/projects`      | Crea un proyecto          |
| PUT    | `/api/projects/{id}` | Actualiza un proyecto     |
| DELETE | `/api/projects/{id}` | Elimina un proyecto       |

---

## Como correr los tests

```bash
./mvnw test
```

Los tests son unitarios y no requieren PostgreSQL. Usan H2 en memoria como base de datos y Mockito para aislar los repositorios. Cubren las implementaciones de servicio: `AuthServiceImpl`, `TaskServiceImpl` y `UserServiceImpl`.

---

## Estructura del proyecto

```
src/
├── main/
│   ├── java/com/minijira/backend/
│   │   ├── BackendApplication.java        # Punto de entrada Spring Boot
│   │   ├── controller/                    # Controllers REST
│   │   │   ├── AuthController.java
│   │   │   ├── TaskController.java
│   │   │   ├── UserController.java
│   │   │   └── ProjectController.java
│   │   ├── service/                       # Interfaces + implementaciones de negocio
│   │   ├── repository/                    # Interfaces Spring Data JPA
│   │   ├── model/                         # Entidades JPA (Task, User, Project, TaskStatus)
│   │   ├── dto/                           # Objetos de transferencia (Request/Response)
│   │   ├── security/                      # JwtUtil: generacion y validacion de tokens
│   │   ├── config/                        # CorsConfig, SecurityConfig
│   │   └── exception/                     # GlobalExceptionHandler, excepciones propias
│   └── resources/
│       └── application.properties
└── test/
    └── java/com/minijira/backend/
        └── service/                       # Tests unitarios de AuthServiceImpl, TaskServiceImpl, UserServiceImpl
```
