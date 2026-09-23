# Build, run y debug de Monex

Guía operativa para desarrollo diario, depuración y un arranque **similar a producción** en local (UI servida por Spring Boot o `.app` de macOS).

Los mismos flujos están automatizados en el [`Makefile`](../Makefile) de la raíz. Ejecuta `make help` para ver los alias.

## Requisitos

| Herramienta | Uso |
|-------------|-----|
| JDK 25 | Backend, desktop, jpackage (`sdk use java 25.0.2-amzn`) |
| `./mvnw` | Build Maven multimódulo |
| Node.js 22 LTS | Desarrollo frontend en caliente (Homebrew: `node@22`) |
| npm 11 | Instalar deps del frontend si hace falta (`npx npm@11 install`) |

Base de datos en desarrollo: `~/.monex/monex.db` (SQLite).

---

## Desarrollo en caliente (frontend y backend separados)

Es el flujo recomendado mientras cambias la UI: Angular recarga al guardar; el backend expone la API en el puerto 8080.

### 1. Backend (terminal 1)

```bash
make backend-run
```

Equivalente manual:

```bash
./mvnw -pl monex-backend -am process-classes
./mvnw -pl monex-backend spring-boot:run
```

Comprueba la API:

- URL: [http://localhost:8080/api/health](http://localhost:8080/api/health)
- Respuesta esperada: `{"status":"UP","application":"monex"}`

En `http://localhost:8080/` **no** hace falta ver la SPA en este modo; el frontend va en el puerto 4200.

### 2. Frontend (terminal 2)

Primera vez (o tras cambiar `package.json`):

```bash
make frontend-install
```

Servidor de desarrollo:

```bash
make frontend-dev
```

- UI: [http://localhost:4200](http://localhost:4200)
- Las peticiones a `/api/**` se reenvían al backend vía [`monex-frontend/proxy.conf.json`](../monex-frontend/proxy.conf.json).

### Por qué a veces falla `http://localhost:8080/`

La interfaz integrada en el backend vive en **`monex-backend/target/classes/static/`**, copiada desde `monex-frontend/dist/...` en la fase Maven `process-classes`. El botón **Run** del IDE sobre `MonexApplication` **no** ejecuta esa copia por defecto, así que `/` puede devolver Whitelabel (404) aunque `/api/health` funcione.

Solución rápida antes de arrancar desde el IDE:

```bash
make backend-sync-static
```

Luego vuelve a lanzar el backend.

---

## Build completo

Valida frontend + backend + desktop (tests incluidos):

```bash
make verify
```

Equivalente: `./mvnw clean verify`.

---

## Depuración

### Backend (IntelliJ IDEA / Cursor)

**Opción A — Maven con puerto de depuración**

```bash
make backend-debug
```

Conecta el depurador a **localhost:5005** (Remote JVM Debug / Attach).

**Opción B — Run desde el IDE**

1. Ejecuta antes `make backend-sync-static` (o añade un paso *Before launch* → Maven → `monex-backend` → goal `process-classes`).
2. Depura la clase `com.urbanojvr.monex.MonexApplication` en el módulo `monex-backend`.

Puntos de parada en controladores (`/api/**`), servicios JPA, etc.

### Frontend (Angular)

1. `make frontend-dev`
2. Abre [http://localhost:4200](http://localhost:4200)
3. DevTools del navegador (F12): consola, red, Angular DevTools si lo tienes instalado.

El proxy oculta CORS en desarrollo; si llamas al backend directamente desde otro origen, recuerda que CORS solo permite `http://localhost:4200`.

### Desktop (JavaFX)

```bash
make desktop-run
```

Depura `MonexDesktopApp` desde el IDE con el perfil Maven `javafx:run`, o pon breakpoints en `BackendProcess` / `HealthWaiter` si el backend no arranca a tiempo.

---

## Arranque “production-like” en local

Dos formas: **misma máquina, sin empaquetar .app**, o **bundle macOS**.

### A) UI servida por Spring Boot (puerto 8080)

Simula el despliegue embebido (Angular en `classpath:/static/`):

```bash
make verify          # compila Angular y copia estáticos al JAR/classes
make backend-run     # o: java -jar monex-backend/target/*.jar
```

Abre [http://localhost:8080/](http://localhost:8080/) — deberías ver la pantalla de bienvenida de Monex y el estado de `/api/health`.

### B) Aplicación de escritorio `.app` (macOS)

Solo en **Mac**, con JDK 25:

```bash
make app
```

Salida:

```text
monex-desktop/target/dist/Monex.app
```

Abrir:

```bash
make app-open
```

Si macOS bloquea la app (sin firma/notarización):

```bash
make app-trust
open monex-desktop/target/dist/Monex.app
```

El `.app` arranca un backend en un puerto libre y muestra la UI en JavaFX WebView (comportamiento cercano al producto empaquetado).

---

## Referencia rápida de alias `make`

| Objetivo | Descripción |
|----------|-------------|
| `make help` | Lista todos los comandos |
| `make backend-run` | API + estáticos en :8080 |
| `make backend-sync-static` | Solo copia frontend → `target/classes/static` |
| `make backend-debug` | Backend con JDWP en :5005 |
| `make frontend-install` | Dependencias npm del frontend |
| `make frontend-dev` | `ng serve` en :4200 |
| `make desktop-run` | JavaFX launcher |
| `make app` | Genera `Monex.app` |
| `make app-open` | Genera (si falta) y abre el `.app` |
| `make app-trust` | `xattr -cr` para Gatekeeper local |
| `make health` | `curl` a `/api/health` |
| `make verify` | Build multimódulo completo |

---

## Enlaces relacionados

- [estructura-proyecto.md](estructura-proyecto.md) — arquitectura de módulos
- [README.md](../README.md) — visión general del proyecto
