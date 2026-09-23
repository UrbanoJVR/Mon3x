# Monex — atajos de build, run, debug y empaquetado local.
# Uso: make help

SHELL := /bin/bash
MVN := ./mvnw
FRONTEND_DIR := monex-frontend
BACKEND_MODULE := monex-backend
DESKTOP_MODULE := monex-desktop
APP_PATH := $(DESKTOP_MODULE)/target/dist/Monex.app
NODE22 := export PATH="/opt/homebrew/opt/node@22/bin:$$PATH"
SDKJAVA := source "$$HOME/.sdkman/bin/sdkman-init.sh" && sdk use java 25.0.2-amzn

.DEFAULT_GOAL := help

.PHONY: help
help: ## Lista objetivos disponibles
	@echo "Monex — comandos make (ver docs/build-run-debug.md)"
	@grep -E '^[a-zA-Z0-9_.-]+:.*##' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*## "}; {printf "  \033[36m%-22s\033[0m %s\n", $$1, $$2}'

.PHONY: verify
verify: ## Build completo multimódulo (tests incluidos)
	$(SDKJAVA) && $(MVN) clean verify

.PHONY: backend-sync-static
backend-sync-static: ## Copia el Angular compilado a target/classes/static (para :8080)
	$(SDKJAVA) && $(MVN) -pl $(BACKEND_MODULE) -am process-classes

.PHONY: backend-run
backend-run: backend-sync-static ## Backend en http://localhost:8080 (UI integrada si hay dist/)
	$(SDKJAVA) && $(MVN) -pl $(BACKEND_MODULE) spring-boot:run

.PHONY: backend-debug
backend-debug: backend-sync-static ## Backend con depuración remota en puerto 5005
	$(SDKJAVA) && $(MVN) -pl $(BACKEND_MODULE) spring-boot:run \
		-Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"

.PHONY: frontend-install
frontend-install: ## npm install en monex-frontend (Node 22 recomendado)
	$(NODE22) && cd $(FRONTEND_DIR) && npx --yes npm@11 install

.PHONY: frontend-dev
frontend-dev: ## Angular en caliente http://localhost:4200 (proxy → :8080)
	$(NODE22) && cd $(FRONTEND_DIR) && npm start

.PHONY: desktop-run
desktop-run: ## Launcher JavaFX (arranca backend + WebView)
	$(SDKJAVA) && $(MVN) -pl $(DESKTOP_MODULE) -am package -DskipTests && \
	$(MVN) -pl $(DESKTOP_MODULE) javafx:run

.PHONY: app
app: ## Genera Monex.app (macOS, perfil native)
	$(SDKJAVA) && $(MVN) -pl $(DESKTOP_MODULE) -am verify -Pnative

.PHONY: app-open
app-open: ## Abre Monex.app (genera antes si no existe)
	@test -d "$(APP_PATH)" || $(MAKE) app
	open "$(APP_PATH)"

.PHONY: app-trust
app-trust: ## Quita cuarentena Gatekeeper del .app local
	xattr -cr "$(APP_PATH)"

.PHONY: health
health: ## Comprueba GET /api/health en :8080
	curl -sf http://localhost:8080/api/health && echo
