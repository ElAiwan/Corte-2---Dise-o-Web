# Entrega del integrante 2: API REST y reglas de negocio

Rama: `feature/2-crud-servicios`. Java 17, Spring Boot 3.3.5, sin Lombok.
Se usan las entidades y los métodos de repositorio del integrante 1, sin modificarlos.
La seguridad y el login se integran con el trabajo del integrante 3.
Por indicación del usuario se cambió la base de datos del contrato original a PostgreSQL,
administrada con pgAdmin. Este cambio también adapta el driver Maven, properties, SQL,
ejemplo de entorno y alternativa Docker del integrante 1; debe comunicarse al equipo.

## Archivos y explicación para la defensa

Todas las rutas siguientes parten de la raíz del repositorio.

| Archivo | Qué hace y por qué existe |
|---|---|
| `backend/src/main/java/com/uam/prestamos/dto/EquipoRequest.java` | Define los únicos campos editables de un equipo. Normaliza el código con `trim()` y mayúsculas independientes del idioma antes de Jakarta Validation, de consultar duplicados y de guardar. Valida campos obligatorios y largos máximos. No contiene `estado`. |
| `backend/src/main/java/com/uam/prestamos/dto/EquipoResponse.java` | Expone id, código, nombre, tipo y estado. `from(Equipo)` evita serializar entidades y su colección de préstamos. |
| `backend/src/main/java/com/uam/prestamos/dto/PrestamoRequest.java` | Valida id positivo, solicitante obligatorio de máximo 100 caracteres y ambas fechas obligatorias. La comparación entre fechas corresponde al servicio. |
| `backend/src/main/java/com/uam/prestamos/dto/PrestamoResponse.java` | Devuelve los campos exactos del contrato, incluyendo el resumen del equipo. Se construye dentro del servicio transaccional porque la relación `equipo` es LAZY. |
| `backend/src/main/java/com/uam/prestamos/dto/ErrorResponse.java` | Formato de errores compartido con seguridad: timestamp, status, error, message, path y errores. `NON_NULL` omite el mapa `errores` cuando no corresponde. El integrante 3 puede reutilizar este record con sus seis argumentos. |
| `backend/src/main/java/com/uam/prestamos/exception/RecursoNoEncontradoException.java` | Indica que no existe el equipo o préstamo solicitado: HTTP 404. |
| `backend/src/main/java/com/uam/prestamos/exception/ConflictoNegocioException.java` | Indica código duplicado, préstamo de equipo ocupado, eliminación bloqueada o devolución repetida: HTTP 409. |
| `backend/src/main/java/com/uam/prestamos/exception/SolicitudInvalidaException.java` | Indica fechas de préstamo inválidas: HTTP 400. |
| `backend/src/main/java/com/uam/prestamos/exception/GlobalExceptionHandler.java` | Convierte las excepciones en el JSON común. También maneja validación, JSON/enums/fechas inválidos, parámetros inválidos, rutas inexistentes, integridad de BD y errores inesperados. El detalle del 500 se registra en el servidor; no se envía al cliente. |
| `backend/src/main/java/com/uam/prestamos/service/EquipoService.java` | CRUD, código único normalizado, creación DISPONIBLE, actualización sin alterar estado y bloqueo de eliminación si hay préstamos ACTIVO. La eliminación del historial devuelto depende de la cascada JPA ya definida por el integrante 1. |
| `backend/src/main/java/com/uam/prestamos/service/PrestamoService.java` | Consulta con filtro opcional; registro con validación de fechas y doble comprobación de disponibilidad; devolución con fecha real. Las escrituras son transaccionales y guardan préstamo y equipo juntos. |
| `backend/src/main/java/com/uam/prestamos/controller/EquipoController.java` | Recibe HTTP, aplica `@Valid` y delega en el servicio. POST devuelve 201 con Location; DELETE devuelve 204 sin cuerpo; consultas y PUT devuelven 200. |
| `backend/src/main/java/com/uam/prestamos/controller/PrestamoController.java` | Expone listado, consulta, registro y devolución. Convierte el filtro `estado` a enum y devuelve 201 con Location al registrar. |
| `backend/src/test/java/com/uam/prestamos/controller/CrudApiTest.java` | Pruebas MockMvc con controladores, validaciones, servicios y manejador reales; repositorios Mockito. Cubre los 15 escenarios de la guía y casos adicionales. Deshabilita filtros únicamente en este contexto de prueba. |

`@Transactional(readOnly = true)` delimita las lecturas y mantiene abierta la sesión JPA mientras se crean los DTOs.
`@Transactional` en las escrituras permite revertir los cambios de préstamo y equipo si falla la operación.
Las dependencias se inyectan por constructor, por lo que cada clase declara explícitamente qué necesita.

## Ejecutar pruebas

Desde PowerShell, en `backend`:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'
.\mvnw.cmd test
```

Estas pruebas no necesitan PostgreSQL ni credenciales. Usan persistencia simulada y no verifican el SQL,
la eliminación en cascada real, el rollback real de JPA ni las autorizaciones JWT.
La prueba completa con PostgreSQL y seguridad debe repetirse durante la integración del equipo.

Resultado verificado el 26/10/2026 con Java 17: **28 pruebas, 0 fallos, 0 errores, 0 omitidas; BUILD SUCCESS**.
Después del cambio a PostgreSQL 16 se repitieron satisfactoriamente las 28 pruebas y los
**15 escenarios manuales contra la base real**, incluyendo cambios de estado y eliminación
en cascada del préstamo devuelto. Se comprobó el arranque con creación de tablas por Hibernate
y carga de los seis equipos y el préstamo semilla. También se ejecutó dos veces el script manual:
no duplicó datos y conservó las secuencias después de eliminar los registros de prueba.
La base local `prestamos_db` quedó creada con propietario `postgres`.
Para la prueba HTTP se utilizó temporalmente el puerto 18080 y la exclusión de seguridad
indicada en la guía; el puerto configurado del proyecto sigue siendo 8080. La prueba con JWT
queda para cuando se integre la parte del integrante 3.
Los cambios se dejan locales,
sin commit, push ni PR; los mensajes sugeridos están al final de este documento.

## Levantar para pruebas manuales

Guía para cada computadora y sus credenciales: [PostgreSQL local](postgresql-local.md).

1. Configurar `backend/.env` a partir de `.env.example` con las credenciales locales.
2. Tener PostgreSQL disponible en `localhost:5432`. En pgAdmin crear la base `prestamos_db`
   con propietario `postgres` si aún no existe. Spring crea las tablas y carga los datos al arrancar.
   Como alternativa manual, abrir Query Tool sobre esa base y ejecutar `database/script.sql`.
   pgAdmin es la herramienta de administración; el backend se conecta directamente al servidor PostgreSQL.
   Para esta etapa se usa PostgreSQL instalado localmente en cada computadora.
3. Mientras no esté integrada la configuración de seguridad del integrante 3, ejecutar desde `backend`:

```powershell
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.arguments=--spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration'
```

Cuando se integre seguridad, ejecutar `.\mvnw.cmd spring-boot:run` y enviar
`Authorization: Bearer <token-admin>` en las escrituras; USER y ADMIN pueden consultar.

## Peticiones para Postman

Base: `http://localhost:8080`. En POST y PUT de equipos, y POST de préstamos, usar
`Content-Type: application/json`. Los identificadores `equipoId` y `prestamoId` de esta tabla
se toman de las respuestas de creación, no se asumen valores fijos.

Cuerpo A, crear equipo:

```json
{"codigoPatrimonial":"UAM-LAP-010","nombre":"Laptop Lenovo ThinkPad E14","tipo":"LAPTOP"}
```

Cuerpo B, actualizar equipo:

```json
{"codigoPatrimonial":"UAM-LAP-010","nombre":"Laptop Lenovo ThinkPad E14 actualizada","tipo":"LAPTOP"}
```

Cuerpo C, registrar préstamo (reemplazar 7 por el id creado):

```json
{"equipoId":7,"nombreSolicitante":"Instructora Ana Ruiz","fechaPrestamo":"2026-10-28","fechaDevolucionPrevista":"2026-10-31"}
```

| # | Petición | Resultado esperado |
|---|---|---|
| 1 | `GET /api/equipos` | 200; seis equipos al partir de la semilla limpia. |
| 2 | `GET /api/equipos/999` | 404 si ese id no existe. |
| 3 | `POST /api/equipos`, cuerpo A | 201, Location `/api/equipos/{equipoId}`, estado DISPONIBLE. Guardar el id. |
| 4 | Repetir cuerpo A | 409 por código duplicado. También con código `  uam-lap-010  `. |
| 5 | Cuerpo A con `nombre: ""` | 400, `errores.nombre`. |
| 6 | Cuerpo A con `tipo: "CELULAR"` | 400 por enum inválido. |
| 7 | `PUT /api/equipos/{equipoId}`, cuerpo B | 200, nombre actualizado, mismo estado. |
| 8 | `POST /api/prestamos`, cuerpo C | 201 y Location `/api/prestamos/{prestamoId}`. Guardar el id; GET del equipo debe mostrar PRESTADO. |
| 9 | Repetir cuerpo C | 409 por equipo ocupado. |
| 10 | Cuerpo C con devolución `2026-10-27` | 400 por fechas. Se comprueba antes de buscar disponibilidad. |
| 11 | `DELETE /api/equipos/4` | 409 mientras mantenga el préstamo activo semilla. |
| 12 | `PUT /api/prestamos/{prestamoId}/devolver`, sin cuerpo | 200, DEVUELTO y fecha real del servidor; GET del equipo muestra DISPONIBLE. |
| 13 | Repetir devolución | 409. |
| 14 | `DELETE /api/equipos/{equipoId}` | 204, cuerpo vacío; se borra el historial devuelto por cascada JPA. |
| 15 | `GET /api/prestamos?estado=ACTIVO` | 200, solo activos. |

También probar `/api/equipos/abc`, `/api/prestamos?estado=XYZ`, fechas con formato incorrecto,
campos omitidos, nombres demasiado largos, préstamo inexistente y un PUT con código de otro equipo.

## Mensajes finales para coordinar con el integrante 5

| HTTP | Mensaje |
|---|---|
| 400 | `Datos inválidos` y mapa de campos `errores`. |
| 400 | `JSON mal formado o valor no permitido (tipo: LAPTOP, PROYECTOR o TABLETA; fechas yyyy-MM-dd)` |
| 400 | `Parámetro inválido: id` o `Parámetro inválido: estado` |
| 400 | `La fecha prevista de devolución no puede ser anterior a la fecha del préstamo` |
| 404 | `No existe el equipo con id X` |
| 404 | `No existe el préstamo con id X` |
| 404 | `Ruta no encontrada` |
| 409 | `Ya existe un equipo con el código patrimonial X` |
| 409 | `No se puede eliminar el equipo X porque tiene préstamos activos` |
| 409 | `El equipo X ya se encuentra PRESTADO` |
| 409 | `El préstamo X ya fue devuelto` |
| 409 | `La operación viola una restricción de la base de datos` |
| 500 | `Error interno del servidor` |

En los mensajes de equipo, X es el código patrimonial normalizado; en mensajes de préstamo,
X es su id. `errores` se omite cuando no hay errores de validación de campos.

## Commits sugeridos

Verificar antes `git config user.name` y `git config user.email` con la identidad propia.

```text
feat(dto): agregar contratos y validaciones de equipos y préstamos
feat(api): implementar CRUD y reglas de préstamo y devolución
feat(errores): unificar respuestas 400, 404, 409 y 500
test(api): verificar escenarios CRUD y documentar entrega del integrante 2
```

Antes del PR a `main`, coordinar la integración con el integrante 5 y repetir las peticiones
con PostgreSQL y los tokens del integrante 3. No incluir `.env`, `target/` ni secretos.

La URL es `jdbc:postgresql://localhost:5432/prestamos_db` y el usuario predeterminado es `postgres`.
La contraseña real está únicamente en `backend/.env`, excluido de Git; `.env.example` usa un marcador.
Las semillas usan `ON CONFLICT DO NOTHING` y sincronizan las secuencias IDENTITY para que
crear equipos y préstamos después de cargar ids fijos no produzca conflictos de clave primaria.
Referencia SQL: [INSERT / ON CONFLICT](https://www.postgresql.org/docs/16/sql-insert.html)
y [secuencias / setval](https://www.postgresql.org/docs/16/functions-sequence.html).
