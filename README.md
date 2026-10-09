# TP2 - Persistencia y Arquitectura Hexagonal

## Cómo levantar PostgreSQL y aplicar las migraciones

Para levantar la base de datos PostgreSQL, ejecutar el siguiente comando en la raíz del proyecto, el cual utilizará el archivo `docker-compose.yml`:
```bash
docker-compose up -d
```
Esto iniciará una instancia de PostgreSQL en el puerto `5433` (por conflictos locales, se mapea allí). 

**Migraciones con Flyway:**
Las migraciones se aplican automáticamente al iniciar la aplicación Spring Boot, gracias a que hemos configurado Spring Boot con `spring-boot-starter-flyway`. 
Los archivos de migración se encuentran en `src/main/resources/db/migration` y siguen el formato de nombres estricto `V{versión}__{descripción}.sql`.

## Puertos y Adaptadores: Qué cambió y qué no

Al migrar de memoria a JPA, las siguientes clases **no tuvieron que ser modificadas**:
* `FavoritoService`
* `FavoritoController`
* DTOs de favoritos

Solo se tuvieron que agregar:
* `FavoritoEntity` (para mapear a base de datos)
* `FavoritoJpaRepository` (la interfaz de Spring Data JPA)
* `FavoritoRepositoryAdapter` (implementa el puerto)

**Por qué fue posible:**
Porque la arquitectura está basada en *Puertos y Adaptadores*. `FavoritoRepository` actúa como un **puerto (un contrato)**. Tanto `InMemoryFavoritoRepository` del TP1 como `FavoritoRepositoryAdapter` del TP2 son **adaptadores intercambiables** que implementan la misma interfaz. El `FavoritoService` nunca se entera de qué implementación de infraestructura concreta se está usando, lo que permite cambiar el motor de persistencia sin alterar el dominio ni la capa de aplicación.

## Evolución del Esquema (Migración V4)

¿Por qué se resuelve con una migración nueva y no modificando las anteriores?
Porque Flyway calcula un checksum por cada migración que ya fue aplicada con éxito. Si modificamos `V1`, `V2` o `V3`, el checksum diferiría de lo guardado en la base de datos (`flyway_schema_history`) y Flyway rechazaría iniciar la aplicación. Además, en bases de producción donde las migraciones pasadas ya corrieron, las nuevas estructuras y transformaciones de datos deben modelarse obligatoriamente como nuevos pasos incrementales hacia adelante.

## Transacciones (ACID) en mover favoritos

¿Qué podría quedar inconsistente si sacamos el `@Transactional` y una escritura falla?
Si la operación para transferir favoritos no fuera atómica y la eliminación de la lista de origen fallara o el sistema colapsara luego de haber cambiado el `lista_id` de los favoritos pero antes de eliminar la lista, tendríamos la base de datos en un estado inconsistente. La lista original persistiría a pesar de haber perdido sus elementos y la operación de "mover y borrar lista original" quedaría incompleta y en un estado inválido. Gracias a `@Transactional`, logramos **Atomicidad**: o bien ocurre todo de manera conjunta (el traspaso y el borrado) y se comitea, o falla una de sus partes y se realiza un rollback automático de los pasos ejecutados en esa transacción, garantizando la consistencia global del sistema.

## Colección de Pruebas (Archivo HTTP)

Se incluye el archivo `tests.http` en la raíz del proyecto para probar un caso de éxito y de error de los nuevos endpoints, en el cual se valida por ejemplo el retorno de *HTTP 409 Conflict* si se intenta borrar una lista con elementos asociados.