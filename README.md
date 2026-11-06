# TP2 y TP3 - Persistencia, Migraciones, Arquitectura Hexagonal, Seguridad JWT, Pruebas y CI/CD

## 1. Cómo levantar PostgreSQL y aplicar las migraciones (TP2)

Para levantar la base de datos PostgreSQL localmente, ejecutar en la raíz del proyecto:
```bash
docker-compose up -d
```
Esto iniciará una instancia de PostgreSQL en el puerto `5433`.

**Variables de entorno soportadas (`application.properties`):**
La aplicación toma su configuración sensible desde variables de entorno con valores por defecto para desarrollo local (`${VARIABLE:valorPorDefecto}`):
* `DB_URL` (por defecto: `jdbc:postgresql://localhost:5433/tp2_db?options=-c%20timezone=UTC`)
* `DB_USERNAME` (por defecto: `usuario`)
* `DB_PASSWORD` (por defecto: `password`)
* `JWT_SECRET` (secreto HMAC-SHA para firmar y verificar tokens JWT)
* `JWT_EXPIRATION_MS` (por defecto: `86400000` ms / 24 horas)

**Migraciones con Flyway (`src/main/resources/db/migration`):**
Las migraciones (`V1` a `V6`) se aplican automáticamente al iniciar la aplicación gracias a `spring-boot-starter-flyway` y `spring.jpa.hibernate.ddl-auto=validate`:
* `V1__create_favoritos.sql`: Tabla inicial de `favoritos`.
* `V2__create_listas.sql`: Tabla de `listas`.
* `V3__add_lista_id_a_favoritos.sql`: Relación `lista_id` en `favoritos`.
* `V4__lista_id_obligatorio.sql`: Creación de lista por defecto (`Sin clasificar`) si no existe, asignación a favoritos huérfanos y restricción `NOT NULL`.
* `V5__create_usuarios.sql`: Tabla `usuarios` (`id`, `email` único, `password_hash`, `rol`).
* `V6__add_usuario_id_a_listas.sql`: Relación `usuario_id` en `listas` hacia `usuarios(id)`.

---

## 2. Puertos y Adaptadores: Qué cambió y qué no (TP2)

Al migrar de memoria a JPA, las capas superiores (`Controller`, `Service` y DTOs) no dependen de detalles de base de datos, sino de interfaces de dominio (**Puertos**):
* `FavoritoRepository`, `ListaRepository` y `UsuarioRepository` son **puertos (contratos)**.
* `FavoritoRepositoryAdapter`, `ListaRepositoryAdapter` y `UsuarioRepositoryAdapter` son **adaptadores de infraestructura** que convierten entre modelos de dominio (`Favorito`, `Lista`, `Usuario`) y entidades JPA (`FavoritoEntity`, `ListaEntity`, `UsuarioEntity`) delegando en interfaces de Spring Data JPA.

---

## 3. Justificaciones Teóricas del TP2

### Evolución del Esquema (Migración V4)
**¿Por qué se resuelve con una migración nueva y no modificando una anterior?**
Porque Flyway almacena en `flyway_schema_history` un checksum de cada migración ya aplicada. Si se edita `V1`, `V2` o `V3`, el checksum cambia y Flyway rechaza iniciar la aplicación para proteger la integridad del historial. En entornos reales donde las migraciones anteriores ya se ejecutaron, todo cambio de esquema o datos debe aplicarse mediante una nueva migración incremental (`V4`, `V5`, `V6`).

### Transacciones (ACID) en mover favoritos
**¿Qué podría quedar inconsistente si sacamos el `@Transactional` y una escritura falla?**
Sin `@Transactional`, cada `save` o `deleteById` se confirmaría en su propia transacción independiente. Si el sistema reasigna los favoritos a la lista destino (confirmando esos `UPDATE`) pero luego falla al intentar eliminar la lista origen, la operación quedaría a mitad de camino: los favoritos se habrían movido pero la lista origen seguiría existiendo vacía. Con `@Transactional` se garantiza la **Atomicidad (A de ACID)**: o todas las escrituras se confirman juntas (commit), o ante cualquier fallo se deshacen todas (rollback).

---

## 4. Seguridad: Autenticación JWT y Autorización (TP3)

### Endpoints de Autenticación (`/api/auth`)
* `POST /api/auth/registro`: Registra un usuario nuevo hasheando la contraseña con `BCryptPasswordEncoder` y asignando el rol `USER`. Si el email ya existe, responde `409 Conflict`.
* `POST /api/auth/login`: Verifica credenciales contra el hash guardado y emite un token JWT firmado que contiene el `id` y el `rol` del usuario. Si falla, responde `401 Unauthorized`.

### Regla de Autorización: ¿Dónde vive y por qué? (Consigna 4.5)
La validación de propiedad de los recursos (`usuarioId` dueño de la lista o rol `ADMIN`) vive en la **capa de Servicio (`ListaServiceImpl` y `FavoritoService`)**, complementada por el filtro de autenticación `JwtAuthenticationFilter` que solo resuelve la identidad del usuario a partir del header `Authorization: Bearer <token>`.

**¿Por qué en el Service y no en el Controller ni en un filtro HTTP?**
1. **Es una regla de negocio del dominio:** Saber quién es dueño de una `Lista` requiere consultar el repositorio (`listaRepository.findById(id)`), verificar primero si el recurso existe (para responder `404 Not Found` si no existe) y luego comparar `lista.getUsuarioId()` contra el usuario autenticado o su rol `ADMIN` (para responder `403 Forbidden` si existe pero pertenece a otro usuario). Un filtro HTTP no tiene contexto del estado de la entidad en la base de datos.
2. **Independencia del canal de entrada (Evitar lógica en el Controller):** El `Controller` solo debe ocuparse de traducir HTTP (recibir DTOs y devolver códigos de estado). Si el día de mañana `ListaServiceImpl` es invocado desde otro servicio, un consumidor de mensajería o una tarea programada, la regla de negocio que impide modificar listas ajenas se sigue cumpliendo porque está encapsulada dentro del Service.

---

## 5. Cómo correr los Tests Unitarios y de Integración (TP3 - Consigna 5)

El proyecto separa las pruebas unitarias de las de integración mediante su sufijo y los plugins de Maven:

1. **Correr únicamente las pruebas unitarias (`*Test` con Maven Surefire):**
   ```bash
   ./mvnw test
   # En Windows:
   .\mvnw.cmd test
   ```
   Ejecuta `ListaServiceImplTest` y `UsuarioServiceImplTest` en memoria con Mockito, sin necesidad de levantar base de datos.

2. **Correr toda la suite incluyendo pruebas de integración (`*IT` con Maven Failsafe + Testcontainers):**
   ```bash
   ./mvnw verify
   # En Windows:
   .\mvnw.cmd verify
   ```
   *(Requiere tener Docker corriendo).* Ejecuta primero los tests unitarios y luego levanta automáticamente un contenedor PostgreSQL limpio con **Testcontainers**, corre las 6 migraciones de **Flyway** (`V1` a `V6`) y ejecuta `ListaControllerIT` validando los casos `200`, `401` y `403`.

---

## 6. Integración Continua con GitHub Actions (TP3 - Consigna 6)

El pipeline de CI está definido en `.github/workflows/ci.yml` y se dispara automáticamente en cada `push` o `pull_request`.
* **Cómo leer el resultado de CI:** En el repositorio de GitHub, ir a la pestaña **Actions**. Cada commit muestra un ícono verde (✅) si `./mvnw clean verify` compiló y pasó todos los tests unitarios y de integración contra el PostgreSQL de Testcontainers, o una cruz roja (❌) con el detalle del test fallido en el log del job `build-and-verify`.

---

## 7. Pasos para Desplegar en Render (TP3 - Consigna 7)

El proyecto incluye un `Dockerfile` multi-etapa con JDK 25 listo para desplegar en [Render](https://render.com/):
1. Crear una base de datos **PostgreSQL** gratuita en Render (*New -> PostgreSQL*) y copiar las credenciales (*Internal Database URL* o host/puerto/base, usuario y contraseña).
2. Crear un **Web Service** en Render (*New -> Web Service*) conectado a este repositorio de GitHub y seleccionar la rama del proyecto (`Runtime: Docker`).
3. En la sección **Environment Variables** del Web Service, configurar:
   * `DB_URL`: `jdbc:postgresql://<HOST>:5432/<DB_NAME>`
   * `DB_USERNAME`: `<USUARIO_RENDER>`
   * `DB_PASSWORD`: `<PASSWORD_RENDER>`
   * `JWT_SECRET`: una cadena aleatoria larga (mínimo 32 caracteres) para firmar los tokens en producción.
4. Al iniciar el despliegue, Flyway aplicará automáticamente las migraciones `V1` a `V6` sobre la base remota.
5. Verificar el estado abriendo `https://<tu-servicio>.onrender.com/health` (`200 OK`) y probando `GET /api/listas` sin token (`401 Unauthorized`) y con token válido (`200 OK`).