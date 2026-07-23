# ALIVOS API

API REST de la plataforma de cursos de ALIVOS Medicina de Rehabilitación: cursos, módulos, lecciones, alumnos, compras, tareas y accesos manuales.

## Stack

- Java 21 + Spring Boot 3.3
- Spring Web, Spring Data JPA, Spring Security
- PostgreSQL (driver `org.postgresql`)
- JWT (jjwt) + BCrypt
- Bean Validation
- Lombok
- Maven (con Maven Wrapper — no requiere Maven instalado)

## Variables de entorno

Copia `.env.example` a `.env` (o expórtalas en tu shell) y completa:

```
DATABASE_URL=jdbc:postgresql://localhost:5432/alivos
DB_USERNAME=alivos
DB_PASSWORD=change_me
PORT=8080
JWT_SECRET=change_me_super_secret
JWT_EXPIRATION_MS=604800000
CORS_ORIGIN=http://localhost:3000,https://alivoweb-uuoo.vercel.app
VIMEO_ACCESS_TOKEN=
MERCADO_PAGO_ACCESS_TOKEN=
SEED_ON_STARTUP=true
```

`CORS_ORIGIN` acepta una lista separada por comas. `http://localhost:3000` y
`https://alivoweb-uuoo.vercel.app` ya están permitidos por defecto aunque no
los incluyas.

Spring Boot no lee `.env` automáticamente: en local, exporta las variables en
tu shell antes de correr la app (ver más abajo).

## Instalación y ejecución local

Requiere una base PostgreSQL corriendo (local o remota) con la base de datos creada.

```bash
# Windows (PowerShell)
$env:DATABASE_URL="jdbc:postgresql://localhost:5432/alivos"
$env:DB_USERNAME="alivos"
$env:DB_PASSWORD="tu_password"
$env:JWT_SECRET="cambia_esto"
$env:CORS_ORIGIN="http://localhost:3000"
.\mvnw.cmd clean package -DskipTests
java -jar target/alivos-api.jar
```

```bash
# macOS / Linux / Git Bash
export DATABASE_URL="jdbc:postgresql://localhost:5432/alivos"
export DB_USERNAME="alivos"
export DB_PASSWORD="tu_password"
export JWT_SECRET="cambia_esto"
export CORS_ORIGIN="http://localhost:3000"
./mvnw clean package -DskipTests
java -jar target/alivos-api.jar
```

La API queda escuchando en `http://localhost:8080` (o el puerto de `PORT`).
Todas las rutas cuelgan de `/api`, más un `GET /health` de diagnóstico.

Al iniciar, `DataSeeder` puebla la base con datos demo de forma **idempotente**
(se puede reiniciar la app las veces que quieras sin duplicar datos: usuarios
y cursos se buscan por email/slug antes de crear, y módulos/lecciones/compras/
tareas/accesos solo se crean si el curso todavía no tiene contenido).

## Usuarios demo (creados por el seed)

| Rol   | Correo             | Contraseña     |
|-------|---------------------|----------------|
| Admin | admin@alivos.com    | Admin12345!    |
| Alumno| cliente@alivos.com  | Cliente12345!  |

El seed también crea 4 alumnos de relleno (María, Laura, Ana, Carolina) con
compras y progreso variado. Todos usan la contraseña temporal `Alivos12345!`.

Cuando un admin otorga acceso manual (`POST /api/admin/manual-access`) a un
correo sin cuenta todavía, la API crea automáticamente un STUDENT con esa
misma contraseña temporal (`Alivos12345!`).

## Endpoints principales

Todos bajo `/api`, responden JSON. Rutas públicas: `GET /courses`,
`GET /courses/:slug`, `GET /settings`, `POST /auth/login`. El resto requiere
`Authorization: Bearer <token>`; las rutas `/admin/*` y `POST /vimeo/resolve`
además requieren rol `ADMIN`.

- **Auth**: `POST /auth/login`, `GET /auth/me`
- **Cursos**: `GET /courses`, `GET /courses/:slug`, `GET /my/courses`, `GET /my/dashboard`
- **Progreso**: `POST /lessons/:lessonId/complete`, `POST /tasks/:lessonId/submit`
- **Admin dashboard**: `GET /admin/dashboard`
- **Admin cursos**: `GET|POST /admin/courses`, `PATCH|DELETE /admin/courses/:id` (DELETE oculta el curso, no lo borra)
- **Admin módulos**: `POST /admin/courses/:courseId/modules`, `PATCH|DELETE /admin/modules/:id`
- **Admin lecciones**: `POST /admin/modules/:moduleId/lessons`, `PATCH|DELETE /admin/lessons/:id`
- **Admin alumnos**: `GET /admin/students`, `GET /admin/students/:id`, `PATCH /admin/students/:id/status`
- **Admin compras**: `GET /admin/purchases`
- **Admin tareas**: `GET /admin/tasks`, `PATCH /admin/tasks/:id/review`
- **Admin accesos manuales**: `GET|POST /admin/manual-access`, `PATCH /admin/manual-access/:id/revoke`
- **Vimeo**: `POST /vimeo/resolve` (admin) — `{ url }` → `{ vimeoId, title, thumbnailUrl, duration, embedUrl }`
- **Settings**: `GET /settings`, `PATCH /admin/settings`
- **Pagos**: `POST /payments/mercadopago/webhook` (placeholder, ver abajo)

## Notas sobre Vimeo

Extrae el `vimeoId` de cualquier URL de Vimeo, arma el embed
(`https://player.vimeo.com/video/{id}`) y trata de obtener metadata:

1. Si hay `VIMEO_ACCESS_TOKEN`, consulta la API privada `api.vimeo.com/videos/:id`.
2. Si no, intenta el oEmbed público `vimeo.com/api/oembed.json`.
3. Si el video es privado o falla la red, igual guarda `vimeoId`/`vimeoUrl`/`embedUrl`
   y usa una miniatura de reemplazo — nunca lanza error ni bloquea el guardado.

## Notas sobre Mercado Pago

Todavía no hay integración real. `POST /payments/mercadopago/webhook`
responde `200 { ok: true }`. Pendiente: verificar firma, consultar el pago en
la API de Mercado Pago, actualizar el `Purchase` y activar el `Enrollment`.

## Deploy en Render

Render no tiene runtime nativo para Java, así que el despliegue usa Docker
(`Dockerfile` incluido, multi-stage con Maven + JRE).

1. Crea una base **PostgreSQL** en Render (o usa `render.yaml` con Blueprint).
2. Crea un **Web Service** apuntando a este repo, tipo **Docker**.
   - **Root directory**: `alivosApi`
   - Render construye la imagen con el `Dockerfile` incluido.
3. Configura las variables de entorno (`DATABASE_URL` desde la base creada,
   `JWT_SECRET`, `CORS_ORIGIN=https://alivoweb-uuoo.vercel.app`, Vimeo/Mercado
   Pago si ya los tienes). Render inyecta `PORT` automáticamente.
4. Tras el deploy, la app siembra los datos demo automáticamente al iniciar
   (idempotente — no hace falta correr nada manualmente).
5. Copia la URL pública del servicio (algo como `https://alivos-api.onrender.com`)
   y úsala en Vercel como `NEXT_PUBLIC_API_URL=https://alivos-api.onrender.com/api`.

También se incluye un `render.yaml` de referencia (Blueprint) con esta misma
configuración.
"# alivoapi" 
