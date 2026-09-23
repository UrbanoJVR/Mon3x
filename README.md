# Monex

Monex es una aplicación de escritorio multiplataforma para la gestión de las finanzas personales y familiares, con una interfaz moderna.

Sus áreas funcionales principales son:

- Categorías de ingresos y gastos.
- Registro de transacciones de ingresos y gastos.
- Cálculo de la capacidad de ahorro neto (CAN).
- Consultas y dashboards analíticos para apoyar la toma de decisiones financieras.
- Gestión de activos y seguimiento de sus ingresos y gastos.

## Stack tecnológico

### Frontend

- Angular 22.
- PrimeNG 22.
- Tema Aura personalizado para Monex.

### Backend

- Java 25 LTS.
- Spring Boot 4.
- Maven.
- JPA / Hibernate.
- Liquibase.
- SQLite.

### Desktop

- JavaFX.
- JavaFX WebView.
- jpackage.

## Estructura

```text
monex/
├── pom.xml
├── monex-backend/
├── monex-frontend/
└── monex-desktop/
```

- **`monex-backend`**: aplicación Spring Boot, persistencia y base de datos SQLite.
- **`monex-frontend`**: aplicación Angular con PrimeNG y el tema visual de Monex.
- **`monex-desktop`**: launcher JavaFX, WebView y configuración de jpackage.

Detalle en [docs/estructura-proyecto.md](docs/estructura-proyecto.md).  
Build, run, debug y `.app` local: [docs/build-run-debug.md](docs/build-run-debug.md) (atajos en el [`Makefile`](Makefile): `make help`).

## Requisitos

- JDK 25 (recomendado vía SDKMAN: `sdk install java 25.0.2-amzn`).
- Maven Wrapper incluido (`./mvnw`).
- Node.js no es obligatorio para el build Maven (el plugin descarga Node 22); útil para desarrollo del frontend.

## Construcción

Desde la raíz del repositorio:

```bash
./mvnw clean verify
```

Orden: Angular → copia de estáticos al backend → JAR Spring Boot → módulo desktop.

## Desarrollo

### Backend

```bash
./mvnw -pl monex-backend spring-boot:run
```

API de salud: `http://localhost:8080/api/health`  
La base SQLite se crea en `~/.monex/monex.db`.

### Frontend (hot reload)

Con el backend en marcha:

```bash
cd monex-frontend
npm ci   # o: npx npm@11 install
npm start
```

`ng serve` usa el proxy en `proxy.conf.json` hacia `http://localhost:8080`.

### Desktop

Tras empaquetar el backend:

```bash
./mvnw -pl monex-desktop -am package
./mvnw -pl monex-desktop javafx:run
```

El launcher elige un puerto libre, arranca el JAR del backend, espera a `/api/health` y abre la UI en WebView.

### Empaquetado macOS (`.app`)

Debe ejecutarse **en un Mac** con JDK 25 (incluye `jpackage`). No se puede cross-compilar desde otro SO.

```bash
source "$HOME/.sdkman/bin/sdkman-init.sh" && sdk use java 25.0.2-amzn
./mvnw -pl monex-desktop -am verify -Pnative
```

Resultado:

```text
monex-desktop/target/dist/Monex.app
```

Abrir:

```bash
open monex-desktop/target/dist/Monex.app
```

Gatekeeper bloqueará la primera apertura (app sin firmar ni notarizar). Solución local: clic derecho → Abrir, o:

```bash
xattr -cr monex-desktop/target/dist/Monex.app
open monex-desktop/target/dist/Monex.app
```

En Windows/Linux el mismo perfil genera una `APP_IMAGE` en `monex-desktop/target/dist`. MSI / DMG / DEB requieren herramientas del sistema y otro `<type>` de jpackage.

## Roadmap

1. Arquitectura multimódulo y empaquetado desktop.
2. Categorías de ingresos y gastos.
3. Registro de ingresos y gastos.
4. Cálculo de la CAN, consultas y dashboards analíticos.
5. Gestión de activos y seguimiento de sus ingresos y gastos.
6. Más adelante, aplicación móvil y capacidades de IA agentic.
