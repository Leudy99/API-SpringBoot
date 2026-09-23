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
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/apidb}
spring.datasource.username=${DB_USER:alumno}
spring.datasource.password=${DB_PASSWORD:123456}
jwt.secret=${JWT_SECRET:cambia-esta-clave-por-una-propia-de-32-caracteres-minimo}
jwt.expiration=3600000
```

La sintaxis `${VARIABLE:respaldo}` usa la variable de entorno si existe, y si no,
el valor que va después de los dos puntos. Sin definir nada, la API arranca con
los valores de respaldo. Para apuntar a otra base basta con definir `DB_URL`,
`DB_USER` y `DB_PASSWORD` en el entorno, sin tocar el archivo.

La clave de `jwt.secret` debe tener 32 caracteres como mínimo.

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
