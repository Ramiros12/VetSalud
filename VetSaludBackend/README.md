# VetSalud Integral – Backend (Java 21 · Tomcat 11 · JPA/Hibernate · MySQL)

Arquitectura: `Servlet (controller) → Service → DAO → JPA/Hibernate → MySQL`, sin frameworks adicionales.
Transacciones manuales con `EntityManager` (`util/TransactionManager`), DTOs en la API, sesión HTTP para autenticación.

## 1. Puesta en marcha
1. XAMPP: iniciar **MySQL**. En phpMyAdmin importar `vetsalud_db.sql` y luego ejecutar `datos_iniciales.sql` (una sola vez).
2. NetBeans: abrir el proyecto → clic derecho → *Clean and Build* (descarga dependencias, incluida `jbcrypt`).
3. *Run* con Tomcat 11. Abrir `http://localhost:8080/VetSaludBackend/`.
4. Si tu MySQL tiene contraseña para `root`, cámbiala en `src/main/resources/META-INF/persistence.xml`.

Usuarios de prueba: `admin / admin123` (ADMINISTRADOR) · `veterinario / vet123` (VETERINARIO).

## 2. Endpoints (JSON)
| Recurso | Ruta | Métodos |
|---|---|---|
| Sesión | `/api/login` (POST), `/api/logout` (POST/GET), `/api/session` (GET) | |
| Propietarios | `/api/propietarios[/{id}]` | GET, POST, PUT, DELETE |
| Mascotas | `/api/mascotas[/{id}]` (`?propietarioId=`) | GET, POST, PUT, DELETE |
| Veterinarios | `/api/veterinarios[/{id}]` | GET, POST, PUT, DELETE |
| Historial clínico | `/api/historiales[/{id}]` (`?mascotaId=`) | GET, POST, PUT, DELETE |
| Usuarios | `/api/usuarios[/{id}]` | GET, POST, PUT, DELETE |
| Roles | `/api/roles[/{id}]` | GET, POST, PUT, DELETE |

Errores: `{"error": "mensaje"}` con 400 (validación), 401 (sin sesión), 403 (sin permiso), 404, 409 (duplicado / en uso).

## 3. Permisos por rol (`filter/AuthFilter.java`)
| | ADMINISTRADOR | VETERINARIO |
|---|---|---|
| Usuarios y roles | CRUD | sin acceso |
| Propietarios, mascotas, veterinarios | CRUD | solo lectura |
| Historial clínico | CRUD | consultar, crear, editar |

## 4. Prueba rápida (Postman o curl)
```
curl -c cj.txt -H "Content-Type: application/json" -d '{"username":"admin","password":"admin123"}' http://localhost:8080/VetSaludBackend/api/login
curl -b cj.txt http://localhost:8080/VetSaludBackend/api/propietarios
curl -b cj.txt -X POST -H "Content-Type: application/json" -d '{"documento":"123","nombre":"Ana"}' http://localhost:8080/VetSaludBackend/api/propietarios
curl -b cj.txt -X POST http://localhost:8080/VetSaludBackend/api/logout
```

## 5. Notas de diseño
- `hibernate.hbm2ddl.auto=none`: el esquema lo controla `vetsalud_db.sql`.
- Contraseñas con BCrypt (`util/PasswordUtil`: `cifrar` / `verificar`). Las contraseñas nunca salen en el JSON.
- Usuario ↔ Rol: relación muchos-a-muchos mediante la entidad intermedia `UsuarioRol` (clave compuesta).
- Los roles `ADMINISTRADOR` y `VETERINARIO` son del sistema (no se renombran ni borran); no se puede borrar un veterinario con historiales ni un rol en uso.
