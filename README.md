# TecnoUAM: préstamo de equipos

Proyecto integrado de los cinco integrantes: API Spring Boot, entidades JPA, CRUD y reglas
de negocio, seguridad JWT, frontend React y colección Postman.

## Requisitos

- JDK 17.
- PostgreSQL 16 instalado y ejecutándose localmente; pgAdmin para administrarlo.
- Node.js y npm para el frontend (compilación verificada con Node.js 22.15.0).
- Git. Maven Wrapper está incluido en `backend`.

## PostgreSQL local y backend

1. En pgAdmin crear `prestamos_db` con el usuario de PostgreSQL de tu computadora como propietario.
2. Desde PowerShell, en la raíz del proyecto:

```powershell
cd backend
if (-not (Test-Path -LiteralPath .env)) {
    Copy-Item -LiteralPath .env.example -Destination .env
}
```

3. Editar `backend/.env` con tus datos:

```properties
DB_URL=jdbc:postgresql://localhost:5432/prestamos_db
DB_USERNAME=tu_usuario_local
DB_PASSWORD=tu_contrasena_local
JWT_SECRET=tu_secreto_aleatorio_de_al_menos_32_caracteres
```

4. Desde `backend`, ejecutar:

```powershell
.\mvnw.cmd spring-boot:run
```

El backend escucha en `http://localhost:8080`. Hibernate crea las tablas y carga semillas
sin duplicarlas. La base debe existir antes del arranque. Cada integrante utiliza su propia
instalación local de PostgreSQL y sus credenciales; `.env` está excluido de Git.

La [guía de PostgreSQL local](docs/postgresql-local.md) explica permisos, generación del secreto,
configuración de Java y solución de problemas. El [modelo de datos](docs/modelo-er.md) describe las relaciones.

## Frontend

En otra terminal, desde la raíz:

```powershell
cd frontend
if (-not (Test-Path -LiteralPath .env)) {
    Copy-Item -LiteralPath .env.example -Destination .env
}
npm ci
npm run dev
```

Abrir `http://localhost:5173`. `VITE_API_URL=http://localhost:8080/api` permite ajustar la URL
del backend; ese también es el valor local predeterminado. La interfaz consulta datos reales
mediante Axios y envía el JWT en cada petición autenticada.

## Usuarios de la aplicación

| Usuario | Contraseña inicial de ejemplo | Permisos |
|---|---|---|
| `admin` | `Admin123*` | Consultas, CRUD, préstamos y devoluciones. |
| `usuario` | `Usuario123*` | Consultas de equipos y préstamos. |

`ADMIN_PASSWORD` y `USER_PASSWORD` permiten cambiar las contraseñas iniciales en `.env`
antes del primer arranque. Las cuentas se crean con BCrypt si aún no existen.
Estas cuentas son distintas del usuario y contraseña de PostgreSQL.

Login: `POST /api/auth/login` con `{"username":"admin","password":"Admin123*"}`.
Las demás rutas requieren `Authorization: Bearer <token>`.

## Validación y Postman

```powershell
# Desde backend
.\mvnw.cmd test

# Desde frontend
npm run build
```

Importar en Postman:

- `postman/Prestamos_Equipos.postman_collection.json`
- `postman/Prestamos_Local.postman_environment.json`

Seleccionar **Prestamos Local** y ejecutar la colección en orden con Collection Runner.
Los logins guardan los tokens automáticamente. La colección usa las contraseñas iniciales
de ejemplo; ajustar las peticiones de login si se cambiaron localmente. El último grupo crea
un equipo para demostrar la actualización de React al recargar.

La colección verifica login, 401/403, consultas, validación 400, inexistentes 404, conflictos 409,
creaciones 201 con Location, devolución y eliminación 204. Las pruebas Java de CRUD utilizan
repositorios simulados; la colección ejercita la API con PostgreSQL real.

Validación de la integración del 26/10/2026: 32 pruebas Java sin fallos, compilación de React
correcta y colección Postman ejecutada con Newman 6.2.3 contra PostgreSQL local y JWT activo:
28 peticiones y 46 verificaciones, sin fallos. Para esa ejecución se utilizó temporalmente
el puerto 18080; la configuración compartida conserva el puerto 8080.
