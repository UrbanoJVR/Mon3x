# Estructura del proyecto Monex

```
monex/
├── pom.xml
├── monex-backend/
├── monex-frontend/
└── monex-desktop/
```

## pom.xml padre

Coordina todo el proyecto:

- Declara los tres módulos.
- Fija Java 25.
- Centraliza versiones de dependencias y plugins.
- Define la configuración común de Maven.
- Establece el orden de construcción.

No contiene código de la aplicación.

## monex-backend

Contiene toda la lógica de Monex:

- Aplicación Spring Boot.
- Modelos de categorías, transacciones y activos.
- Cálculo de la capacidad de ahorro neto (CAN).
- Servicios y reglas de negocio.
- API consumida por Angular.
- Persistencia con JPA/Hibernate.
- Migraciones Liquibase.
- Base de datos SQLite.
- Tests del backend.
- Archivos Angular compilados, incorporados durante el build.

El backend también sirve la interfaz Angular desde una dirección local.

## monex-frontend

Contiene exclusivamente la interfaz:

- Proyecto Angular.
- Componentes PrimeNG.
- Tema Aura personalizado.
- Pantallas, formularios y dashboards.
- Comunicación con la API del backend.
- Tests del frontend.
- `package.json` y configuración de Angular.
- Un `pom.xml` que hace que Maven ejecute la instalación y compilación del frontend.

Su resultado es una carpeta de archivos estáticos (HTML, JavaScript y CSS) que se copia al backend.

## monex-desktop

Es el envoltorio de escritorio:

- Aplicación principal JavaFX.
- Arranque del backend Spring Boot en local.
- Selección o configuración del puerto local.
- Ventana principal.
- JavaFX WebView para cargar Angular.
- Cierre coordinado del backend al salir.
- Iconos y recursos de escritorio.
- Configuración de jpackage.
- Generación del instalador o aplicación nativa.

No contiene reglas financieras ni acceso directo a SQLite.

## Dependencias entre módulos

```
monex-frontend
       ↓ archivos estáticos
monex-backend
       ↓ aplicación completa
monex-desktop
       ↓
jpackage
```

La idea clave es:

- El frontend presenta.
- El backend calcula y guarda.
- El desktop arranca y empaqueta.
- El POM padre coordina todo.
