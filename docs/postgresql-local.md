# Ejecutar el backend con PostgreSQL local

Cada integrante ejecuta PostgreSQL en su propia computadora y configura sus propias credenciales.
La configuración actual usa PostgreSQL local: `localhost`, puerto `5432`, base `prestamos_db`.
pgAdmin permite administrar ese servidor y ver sus tablas; no necesita permanecer abierto para usar la API.

## 1. Requisitos

- JDK 17 y `JAVA_HOME` apuntando a su instalación.
- PostgreSQL 16 instalado y su servicio iniciado.
- pgAdmin para crear y consultar la base.
- Los cambios del backend que incluyen el driver PostgreSQL y los scripts SQL actualizados.
- Conexión a Internet en el primer arranque para que Maven Wrapper descargue sus dependencias.

El repositorio incluye `mvnw.cmd`; no hace falta instalar Maven por separado.

## 2. Crear la base en pgAdmin

1. Conectarse al servidor de PostgreSQL de la computadora: host `localhost` y puerto `5432`
   (o el puerto elegido al instalar PostgreSQL).
2. En **Databases > Create > Database**, escribir `prestamos_db`.
3. En **Owner**, seleccionar el usuario local que se configurará en `DB_USERNAME`.
4. Guardar. Spring Boot crea las tablas y carga las semillas al arrancar.

El usuario suele ser `postgres`, pero cada integrante puede usar otro rol local con permiso de inicio
de sesión. Debe ser propietario de su base para poder crear y actualizar las tablas y secuencias.
Si la base ya contiene tablas creadas por otro usuario, sus permisos también deben revisarse desde
la cuenta propietaria; cambiar solo el usuario del archivo `.env` no transfiere esas tablas.

La contraseña necesaria es la del usuario del servidor PostgreSQL, que puede ser distinta
de la contraseña maestra de pgAdmin.

## 3. Configurar las credenciales de cada integrante

Desde PowerShell, en la raíz del proyecto:

```powershell
cd backend
if (-not (Test-Path -LiteralPath .env)) {
    Copy-Item -LiteralPath .env.example -Destination .env
}
notepad .env
```

Editar estas variables en `backend/.env`:

```properties
DB_URL=jdbc:postgresql://localhost:5432/prestamos_db
DB_USERNAME=tu_usuario_local
DB_PASSWORD=tu_contrasena_local
JWT_SECRET=tu_secreto_aleatorio_de_al_menos_32_caracteres
JWT_EXPIRATION_MS=3600000
CORS_ALLOWED_ORIGINS=http://localhost:5173
ADMIN_PASSWORD=Admin123*
USER_PASSWORD=Usuario123*
```

Reemplazar los valores de ejemplo. Si el servidor local usa otro puerto, modificarlo solo en `DB_URL`.
Los valores se escriben sin comillas. No agregar `spring.datasource.username=` ni
`spring.datasource.password=` a este archivo: el proyecto ya los enlaza con `DB_USERNAME` y `DB_PASSWORD`.
Reiniciar el backend después de guardar cambios en las credenciales.

Para generar un secreto local, ejecutar esto en PowerShell y copiar el resultado en `JWT_SECRET`:

```powershell
$bytes = New-Object byte[] 48
$generador = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$generador.GetBytes($bytes)
[Convert]::ToBase64String($bytes)
$generador.Dispose()
```

`backend/.env` está excluido de Git. Cada integrante lo crea en su computadora; no se comparte
la contraseña de otro integrante. `.env.example` sí se comparte, con valores de ejemplo.
Si el IDE ya define variables `DB_URL`, `DB_USERNAME` o `DB_PASSWORD`, actualizarlas o quitarlas
de su configuración de ejecución para que no sustituyan los valores locales del archivo.

## 4. Arrancar el backend

Desde la carpeta `backend`, mientras esté pendiente la seguridad del integrante 3:

```powershell
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.arguments=--spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration'
```

Este parámetro sirve para probar el CRUD en la etapa actual. Cuando se integre la seguridad JWT,
ejecutar el comando normal y usar los tokens correspondientes:

```powershell
.\mvnw.cmd spring-boot:run
```

Si Java no se detecta, definir `JAVA_HOME` con la ruta real del JDK 17 de esa computadora
antes de ejecutar Maven. Ejemplo:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'
```

## 5. Comprobar

La consola debe mostrar `Started PrestamosApplication` y el servidor en el puerto `8080`.
En Postman, consultar `GET http://localhost:8080/api/equipos`: con la semilla inicial devuelve seis equipos.
Cuando esté integrado JWT, incluir `Authorization: Bearer <token>`.
En pgAdmin, actualizar `prestamos_db > Schemas > public > Tables` para ver
`equipos`, `prestamos` y `usuarios`.

Las cuentas de PostgreSQL solo conectan el backend a la base. Los usuarios `admin` y `usuario`
de la aplicación son cuentas distintas, que corresponden al trabajo de seguridad del integrante 3.
Cada computadora tendrá sus propios datos locales.

## Problemas frecuentes

| Mensaje o síntoma | Qué revisar |
|---|---|
| Conexión rechazada | Servicio PostgreSQL iniciado; host y puerto local correctos. |
| Autenticación fallida | `DB_USERNAME` y `DB_PASSWORD` del usuario del servidor PostgreSQL. |
| La base no existe | Crear `prestamos_db` en pgAdmin; el backend crea tablas, no la base. |
| Permisos insuficientes | El usuario debe poder crear y modificar tablas y secuencias de su base. |
| Puerto 8080 ocupado | Detener la otra instancia del backend antes de arrancar. |
| Respuesta 401 | Revisar el comando de esta etapa o enviar token si ya se integró JWT. |

El script `database/script.sql` es una alternativa manual para crear tablas y semillas dentro
de `prestamos_db`; no es necesario ejecutarlo si Spring Boot ya inició correctamente.
