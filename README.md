# API REST con Spring Boot, PostgreSQL y JWT

API con autenticación por token y CRUD de usuarios, sobre arquitectura hexagonal.

---

## Cómo está hecho

El proyecto se divide en dos partes:

- **Núcleo** (`domain` + `application`): las reglas de negocio. Java puro, sin frameworks.
- **Adaptadores** (`infrastructure`): lo técnico. PostgreSQL, HTTP, BCrypt, JWT.

El núcleo no conoce a los adaptadores. Declara lo que necesita mediante interfaces
llamadas **puertos**, y los adaptadores las implementan.

```
   HTTP  ──>  infrastructure/web  ──>  application  ──>  domain
                                                           ▲
                              infrastructure/persistence ───┤
                              infrastructure/security    ───┘
                                          │
                                          v
                              PostgreSQL / BCrypt / JWT
```

Todas las dependencias apuntan hacia `domain`. Ninguna sale de él.

### Los 3 puertos

| Puerto (`domain/port`) | Adaptador (`infrastructure`) | Tecnología que aísla |
|---|---|---|
| `UserRepository` | `UserRepositoryAdapter` | Spring Data JPA + PostgreSQL |
| `PasswordHasher` | `BCryptPasswordHasher` | BCrypt |
| `TokenProvider` | `JwtService` | JJWT |

Cambiar PostgreSQL por otra base solo obliga a reescribir `UserRepositoryAdapter`.

### Estructura

```
src/main/java/com/example/api/
├── ApiSpringbootApplication.java
│
├── domain/                     Java puro
│   ├── model/User.java
│   ├── port/                   UserRepository, PasswordHasher, TokenProvider
│   └── exception/              UserNotFound, EmailAlreadyExists, InvalidCredentials
│
├── application/                Casos de uso
│   ├── UserService.java        CRUD
│   └── AuthService.java        Registro y login
│
└── infrastructure/             Adaptadores
    ├── persistence/            UserEntity, SpringDataUserRepository,
    │                           UserRepositoryAdapter
    ├── security/               BCryptPasswordHasher, JwtService,
    │                           JwtAuthenticationFilter
    ├── web/                    UserController, AuthController,
    │                           GlobalExceptionHandler, dto/
    └── config/                 SecurityConfig
```

### Cómo funciona la autenticación

1. **Registro**: BCrypt cifra la contraseña antes de guardarla.
2. **Login**: se compara la contraseña con el hash. Si coincide, `JwtService` firma un
   token que lleva el email dentro y caduca en 1 hora.
3. **Peticiones protegidas**: `JwtAuthenticationFilter` lee
   `Authorization: Bearer <token>` y verifica firma y caducidad. Si falla, 401.

`UserResponse` no tiene campo `password`, así que la API nunca devuelve contraseñas.

### Decisiones de diseño

- DTOs como `record` de Java 17, sin Lombok.
- Conversión `UserEntity` ↔ `User` con métodos privados, sin MapStruct.
- Un solo token de 1 hora, sin refresh tokens ni OAuth2.
- Sin roles: solo autenticado o no autenticado.
- `STATELESS`: sin sesión en el servidor.
- Tabla `users`, no `user`, que es palabra reservada en PostgreSQL.

---

## Puesta en marcha

**1. Base de datos.** PostgreSQL en Docker con la base `apidb` creada:

```sql
CREATE DATABASE apidb;
```

La tabla `users` la crea Hibernate al arrancar.

**2. Configuración.** Ajusta `src/main/resources/application.properties`:

```properties
server.port=8081
spring.datasource.url=jdbc:postgresql://localhost:5432/apidb
spring.datasource.username=alumno
spring.datasource.password=123456
jwt.secret=${JWT_SECRET:cambia-esta-clave-por-una-propia-de-32-caracteres-minimo}
jwt.expiration=3600000
```

`jwt.secret` se toma de la variable de entorno `JWT_SECRET`; el valor tras `:` es el
de respaldo. La clave debe tener 32 caracteres como mínimo.

**3. Arrancar.**

```powershell
.\mvnw.cmd spring-boot:run
```

Listo cuando aparezca `Tomcat started on port 8081`.

---

## Endpoints

| Método | Ruta | Token | Éxito |
|---|---|---|---|
| `POST` | `/auth/register` | no | 201 |
| `POST` | `/auth/login` | no | 200 |
| `GET` | `/api/users` | sí | 200 |
| `GET` | `/api/users/{id}` | sí | 200 |
| `POST` | `/api/users` | sí | 201 |
| `PUT` | `/api/users/{id}` | sí | 200 |
| `DELETE` | `/api/users/{id}` | sí | 204 |
| `GET` | `/actuator/health` | sí | 200 |

| Error | Cuándo |
|---|---|
| `400` | Datos inválidos, id mal formado o JSON ilegible |
| `401` | Sin token, token inválido o caducado, credenciales incorrectas |
| `404` | El usuario no existe |
| `409` | El email ya está registrado |

---

## Probar en Postman

**1. Importar.** Postman → *Import* (`Ctrl+O`) → arrastra
`postman/API_SpringBoot.postman_collection.json`.

**2. Escribir los datos.** Van en la pestaña **Body** de cada request. Abre el
request, edita el JSON y pulsa *Send*. Los Headers y el Authorization ya vienen
configurados. La pestaña *Params* no se usa: esta API no recibe query params.

**3. Ejecutar en orden**, de arriba hacia abajo. O clic derecho en la colección →
*Run collection*, que corre los 15 requests y muestra 27 tests.

### Las 4 variables

| Variable | Qué es |
|---|---|
| `baseUrl` | `http://localhost:8081` |
| `token` | Lo guarda solo el paso 2 (login) |
| `userId` | Lo guarda solo el paso 1 (registro) |
| `createdId` | Lo guarda solo el paso 5 (crear usuario) |

Las tres últimas empiezan vacías y se llenan solas. No se escriben a mano: los ids
los asigna PostgreSQL y el token lo genera el servidor.

### Al editar, cuidado con los emails repetidos

| Dato | Aparece en |
|---|---|
| Email del primer usuario | Pasos 1, 2, 10 y 11 |
| Email del segundo usuario | Pasos 5 y 6 |

Si cambias uno y olvidas otro, el login responde 401 aunque la API esté bien.

### Los 15 requests

**1. Autenticación**

| # | Request | Qué hace |
|---|---|---|
| 1 | Registrar usuario | Crea el usuario y guarda su id en `userId` |
| 2 | Iniciar sesión | Devuelve el JWT y lo guarda en `token` |

**2. Usuarios** (requieren token)

| # | Request | Qué hace |
|---|---|---|
| 3 | Listar usuarios | Devuelve la lista |
| 4 | Buscar por id | Usa `userId` del paso 1 |
| 5 | Crear usuario | Segundo usuario. Guarda su id en `createdId` |
| 6 | Actualizar usuario | Cambia el nombre. Sin password, se conserva la actual |
| 7 | Eliminar usuario | Borra el del paso 5 |

**3. Casos de error** (deben fallar)

| # | Request | Espera |
|---|---|---|
| 8 | Sin token | 401 |
| 9 | Token inválido | 401 |
| 10 | Contraseña incorrecta | 401 |
| 11 | Email duplicado | 409 |
| 12 | Usuario inexistente | 404 |
| 13 | Datos inválidos | 400 con el detalle de cada campo |

**4. Sistema**

| # | Request | Espera |
|---|---|---|
| 14 | Health | 200 con `{"status":"UP"}` |

**5. Limpieza**

| # | Request | Qué hace |
|---|---|---|
| 15 | Eliminar mi usuario | Borra el del paso 1 y vacía las variables |

El paso 15 deja la base como estaba, para poder repetir la colección. Si te lo
saltas, el paso 1 dará 409 la próxima vez.

---

## Compilar y empaquetar

```powershell
.\mvnw.cmd clean package
```

Genera `target/api-springboot-1.0.0.jar`, que se ejecuta con:

```powershell
java -jar target/api-springboot-1.0.0.jar
```
