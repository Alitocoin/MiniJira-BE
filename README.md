# MiniJira Backend

API REST para gestión de proyectos y tareas al estilo Jira, construida con Spring Boot 3 y PostgreSQL.

## Stack tecnológico

- Java 17
- Spring Boot 3.2
- Spring Data JPA / Hibernate
- PostgreSQL
- Maven
- Lombok
- Bean Validation (Jakarta)

## Arquitectura

```
com.devforce.minijira
├── controller/     → Endpoints REST, manejo de HTTP
├── service/        → Lógica de negocio
├── repository/     → Acceso a datos (Spring Data JPA)
├── model/          → Entidades JPA
├── dto/
│   ├── request/    → Objetos de entrada (validados)
│   └── response/   → Objetos de salida (sin exponer entidades)
├── exception/      → Excepciones custom + @ControllerAdvice
└── config/         → CORS y configuración general
```

## Cómo correr localmente

### Prerequisitos

- Java 17+
- Maven 3.8+
- PostgreSQL corriendo en localhost:5432

### Variables de entorno (opcionales — tienen defaults)

| Variable       | Default                                  | Descripción              |
|----------------|------------------------------------------|--------------------------|
| `DB_URL`       | `jdbc:postgresql://localhost:5432/minijira` | URL de conexión JDBC   |
| `DB_USER`      | `postgres`                               | Usuario de la base       |
| `DB_PASSWORD`  | `postgres`                               | Password de la base      |
| `SERVER_PORT`  | `8080`                                   | Puerto del servidor      |

### Crear la base de datos

```sql
CREATE DATABASE minijira;
```

### Levantar el servidor

```bash
# Con defaults
mvn spring-boot:run

# Con variables personalizadas
DB_URL=jdbc:postgresql://localhost:5432/minijira \
DB_USER=miusuario \
DB_PASSWORD=mipassword \
mvn spring-boot:run
```

El servidor queda disponible en `http://localhost:8080`.

## Endpoints disponibles

### Tasks — `/api/tasks`

| Método   | Ruta                        | Descripción                  |
|----------|-----------------------------|------------------------------|
| GET      | `/api/tasks`                | Listar todas las tareas      |
| GET      | `/api/tasks/{id}`           | Obtener tarea por ID         |
| GET      | `/api/tasks/status/{status}`| Filtrar por status           |
| POST     | `/api/tasks`                | Crear tarea                  |
| PUT      | `/api/tasks/{id}`           | Actualizar tarea completa    |
| PATCH    | `/api/tasks/{id}/status`    | Cambiar solo el status       |
| DELETE   | `/api/tasks/{id}`           | Eliminar tarea               |

Status válidos: `TODO`, `IN_PROGRESS`, `DONE`

### Users — `/api/users`

| Método | Ruta             | Descripción             |
|--------|------------------|-------------------------|
| GET    | `/api/users`     | Listar todos los users  |
| GET    | `/api/users/{id}`| Obtener user por ID     |
| POST   | `/api/users`     | Crear user              |

### Projects — `/api/projects`

| Método | Ruta                | Descripción               |
|--------|---------------------|---------------------------|
| GET    | `/api/projects`     | Listar todos los proyectos|
| GET    | `/api/projects/{id}`| Obtener proyecto por ID   |
| POST   | `/api/projects`     | Crear proyecto            |

## Ejemplo de request — crear tarea

```json
POST /api/tasks
{
  "title": "Implementar login",
  "description": "JWT con refresh token",
  "status": "TODO",
  "storyPoints": 5,
  "estimatedHours": 8.0,
  "startDate": "2026-05-10",
  "endDate": "2026-05-15",
  "assignedUserId": 1,
  "projectId": 1
}
```

## Manejo de errores

Todos los errores retornan el mismo formato:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Tarea con id 99 no encontrada",
  "path": "/api/tasks/99",
  "timestamp": "2026-05-10T10:30:00"
}
```

## CORS

Configurado para aceptar requests desde cualquier origen (`*`). En producción se recomienda restringir con la variable de entorno o modificando `CorsConfig`.
