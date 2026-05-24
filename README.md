# Project Manager — SaaS de Gestión de Proyectos

Aplicación web full-stack para la gestión de proyectos por workspaces con control de roles. Permite a los usuarios iniciar sesión, seleccionar un workspace y gestionar proyectos según su rol asignado.

---

## Tabla de contenidos

- [Descripción](#descripción)
- [Arquitectura](#arquitectura)
- [Stack tecnológico](#stack-tecnológico)
- [Requisitos previos](#requisitos-previos)
- [Ejecución con Docker](#ejecución-con-docker)
- [Ejecución en desarrollo local](#ejecución-en-desarrollo-local)
- [Usuario de prueba](#usuario-de-prueba)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Variables de entorno](#variables-de-entorno)

---

## Descripción

Project Manager es un SaaS MVP que permite gestionar proyectos organizados en **workspaces**. Cada usuario puede pertenecer a múltiples workspaces con roles diferentes:

| Rol | Ver proyectos | Crear proyectos |
|-----|:---:|:---:|
| ADMIN | ✅ | ✅ |
| EDITOR | ✅ | ✅ |
| LECTOR | ✅ | ❌ |

El flujo de autenticación es en dos pasos: primero el usuario inicia sesión y obtiene un token temporal, luego selecciona el workspace al que desea acceder y recibe un JWT final con su rol codificado.

---

## Arquitectura

```
project-manager/
├── docker-compose.yml                  # Orquestación de contenedores
├── project-manager-backend/            # API REST - Spring Boot
│   ├── Dockerfile
│   └── src/
└── project-manager-frontend/           # SPA - Angular 18
    ├── Dockerfile
    ├── nginx.conf
    └── src/
```

Los tres servicios corren en contenedores Docker independientes y se comunican en una red interna:

```
Browser
  │
  ├── http://localhost:4200  →  pm-frontend (Nginx + Angular)
  │
  └── http://localhost:8080  →  pm-backend (Spring Boot)
                                    │
                              pm-mysql (MySQL 8)
```

---

## Stack tecnológico

| Capa | Tecnología |
|------|-----------|
| Frontend | Angular 18, SCSS, Angular Signals |
| Backend | Spring Boot 3.3.5, Java 17, Spring Security, JWT |
| Base de datos | MySQL 8.0 |
| Servidor web | Nginx (Alpine) |
| Contenedores | Docker, Docker Compose |

---

## Requisitos previos

Solo necesitas tener instalado:

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (incluye Docker Compose)

No se requiere tener Node.js, Java, Maven ni MySQL instalados localmente.

---

## Ejecución con Docker

### 1. Clonar el repositorio

```bash
git clone <url-del-repositorio>
cd project-manager
```

### 2. Levantar todos los servicios

Desde la raíz del proyecto (donde está el `docker-compose.yml`):

```bash
docker compose up --build
```

Este comando:
- Construye las imágenes del backend y frontend
- Descarga la imagen de MySQL 8
- Crea y conecta los 3 contenedores
- Inicializa la base de datos con datos de prueba

La primera ejecución tarda entre 3 y 5 minutos porque descarga dependencias de Maven y npm.

### 3. Verificar que todo esté corriendo

Espera ver estos mensajes en la terminal:

```
pm-mysql    | ready for connections
pm-backend  | Started DemoApplication in X seconds
pm-frontend | nginx: configuration file test is successful
```

### 4. Abrir la aplicación

Abre el navegador en:

```
http://localhost:4200
```

### Servicios disponibles

| Servicio | URL | Descripción |
|----------|-----|-------------|
| Frontend | http://localhost:4200 | Aplicación Angular |
| Backend API | http://localhost:8080 | API REST Spring Boot |
| Swagger UI | http://localhost:8080/swagger-ui/index.html | Documentación de la API |
| MySQL | localhost:3307 | Base de datos (puerto 3307 para no chocar con MySQL local) |

### Detener los servicios

```bash
# Detener sin borrar datos
docker compose down

# Detener y borrar la base de datos
docker compose down -v
```

### Reconstruir tras cambios en el código

```bash
docker compose up --build
```

---

## Ejecución en desarrollo local

Si prefieres correr cada servicio por separado sin Docker:

### Backend

Requisitos: Java 17, Maven 3.9+, MySQL 8 corriendo en localhost:3306

```bash
cd project-manager-backend
mvn spring-boot:run
```

El backend queda disponible en `http://localhost:8080`

### Frontend

Requisitos: Node 22, Angular CLI 18

```bash
cd project-manager-frontend
npm install
ng serve
```

El frontend queda disponible en `http://localhost:4200`

---

## Usuario de prueba

La base de datos se inicializa automáticamente con el siguiente usuario:

| Campo | Valor |
|-------|-------|
| Email | carlos@test.com |
| Contraseña | password123 |

Este usuario tiene acceso a dos workspaces con roles diferentes:

| Workspace | Rol | Permisos |
|-----------|-----|----------|
| Workspace Alfa | ADMIN | Ver y crear proyectos |
| Workspace Beta | LECTOR | Solo ver proyectos |

---

## Base de datos

### Con Docker (automático)

No se requiere ninguna acción manual. Al correr `docker compose up --build`:

1. Docker crea el contenedor MySQL con la base de datos `project_manager`
2. Spring Boot se conecta y ejecuta `ddl-auto=create` — Hibernate crea todas las tablas automáticamente
3. Spring ejecuta `data.sql` — se insertan el usuario de prueba, workspaces y proyectos

La base de datos queda lista sin intervención manual.

### En desarrollo local (sin Docker)

Requisitos: MySQL 8 corriendo en `localhost:3306`

**Paso 1 — Crear la base de datos:**

```sql
CREATE DATABASE IF NOT EXISTS project_manager;
```

**Paso 2 — Verificar `application.properties`:**

Asegúrate de que estas líneas estén presentes y con los valores correctos:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/project_manager?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=TU_PASSWORD_LOCAL

# Crea las tablas automaticamente al arrancar
spring.jpa.hibernate.ddl-auto=create

# Ejecuta data.sql despues de que JPA cree las tablas
spring.sql.init.mode=always
spring.jpa.defer-datasource-initialization=true
```

> ⚠️ **Importante:** `ddl-auto=create` recrea las tablas limpias cada vez que arranca el backend. Esto es intencional para el MVP — los datos de prueba se reinsertan desde `data.sql` en cada arranque.

**Paso 3 — Arrancar el backend:**

```bash
cd project-manager-backend
mvn spring-boot:run
```

Spring Boot creará las tablas e insertará los datos automáticamente. No es necesario ejecutar ningún script SQL manualmente.

---

## Estructura del proyecto

```
project-manager/
├── docker-compose.yml
│
├── project-manager-backend/
│   ├── Dockerfile
│   └── src/main/
│       ├── java/com/proyectmanager/
│       │   ├── auth/           # Autenticación y JWT
│       │   ├── projects/       # Gestión de proyectos
│       │   ├── workspaces/     # Gestión de workspaces
│       │   └── users/          # Gestión de usuarios
│       └── resources/
│           ├── application.properties
│           └── data.sql        # Datos iniciales de prueba
│
└── project-manager-frontend/
    ├── Dockerfile
    ├── nginx.conf
    └── src/app/
        ├── auth/
        │   ├── login/                      # Pantalla de login
        │   ├── workspace-selector/         # Seleccion de workspace
        │   └── auth.service.ts             # Logica de autenticacion
        ├── projects/
        │   ├── dashboard/                  # Lista de proyectos
        │   ├── create-project/             # Modal crear proyecto
        │   └── project.service.ts          # Llamadas a la API
        ├── core/
        │   ├── interceptors/               # JWT interceptor
        │   ├── guards/                     # Auth guard
        │   └── models/                     # Interfaces TypeScript
        ├── app.routes.ts                   # Rutas con lazy loading
        └── app.config.ts                   # Configuracion principal
```

---

## Coleccion Postman

En la carpeta `docs/` se incluye la coleccion de Postman con todos los endpoints listos para probar, incluyendo scripts que automatizan el manejo de tokens.

### Importar en Postman

1. Abre Postman
2. Click en **Import**
3. Selecciona el archivo `docs/project-manager.postman_collection.json`
4. La coleccion queda disponible con todas las variables y scripts configurados

### Flujo de prueba

La coleccion esta ordenada para seguir el flujo completo de la API:

| # | Request | Descripcion |
|---|---------|-------------|
| 1 | POST /api/auth/login | Inicia sesion y guarda el `temp_token` automaticamente |
| 2 | POST /api/auth/token (Alfa) | Selecciona Workspace Alfa como ADMIN y guarda el `access_token` |
| 2b | POST /api/auth/token (Beta) | Selecciona Workspace Beta como LECTOR y guarda el `access_token` |
| 3 | GET /api/projects | Lista los proyectos del workspace activo |
| 4 | POST /api/projects | Crea un proyecto (requiere ADMIN o EDITOR) |
| 5 | POST /api/projects | Intenta crear como LECTOR — debe retornar **403 Forbidden** |

> Los scripts de test en los requests 1, 2 y 2b guardan los tokens automaticamente en variables de coleccion. No es necesario copiar y pegar tokens manualmente.

---

## Variables de entorno

Todas las variables se configuran en `docker-compose.yml`. Para modificarlas no es necesario tocar el código fuente:

| Variable | Valor por defecto | Descripción |
|----------|------------------|-------------|
| `MYSQL_ROOT_PASSWORD` | rootpassword | Contraseña de MySQL |
| `MYSQL_DATABASE` | project_manager | Nombre de la base de datos |
| `SPRING_DATASOURCE_USERNAME` | root | Usuario de la base de datos |
| `SPRING_DATASOURCE_PASSWORD` | rootpassword | Contraseña de la base de datos |
| `JWT_SECRET` | mi-clave-secreta-super-segura... | Clave para firmar los JWT |
| `JWT_EXPIRATION` | 86400000 | Expiracion del token en ms (24h) |
