# Mini Trello : commandes de développement
# Utilisation : make <cible>   (make help pour la liste)

# Sous Windows, les commandes sont exécutées par PowerShell ; ailleurs, par le shell par défaut.
ifeq ($(OS),Windows_NT)
  SHELL := powershell.exe
  .SHELLFLAGS := -NoProfile -Command
  IN_BACKEND := Set-Location backend;
  IN_FRONTEND := Set-Location frontend;
  MVNW := .\mvnw.cmd
else
  IN_BACKEND := cd backend &&
  IN_FRONTEND := cd frontend &&
  MVNW := ./mvnw
endif

.DEFAULT_GOAL := help
.PHONY: help install db-up db-down db-reset db-psql backend frontend dev test test-backend test-frontend build clean

help:
	@echo "Mini Trello - commandes disponibles :"
	@echo ""
	@echo "  make install        Installe les dependances du front (npm ci)"
	@echo "  make db-up          Demarre PostgreSQL (Docker) et attend qu'il soit pret"
	@echo "  make db-down        Arrete PostgreSQL (les donnees sont conservees)"
	@echo "  make db-reset       Arrete PostgreSQL et SUPPRIME ses donnees (volume Docker)"
	@echo "  make db-psql        Ouvre une console SQL sur la base"
	@echo "  make backend        Lance l'API Spring Boot       -> http://localhost:8080"
	@echo "  make frontend       Lance Angular                 -> http://localhost:4200"
	@echo "  make dev            Demarre la base, puis le back et le front en parallele"
	@echo "  make test           Lance tous les tests (back + front)"
	@echo "  make test-backend   Tests JUnit / Mockito (necessite la base pour le test de contexte)"
	@echo "  make test-frontend  Tests Vitest du front"
	@echo "  make build          Compile le back (jar) et le front (dist/)"
	@echo "  make clean          Supprime les dossiers de build"

install:
	$(IN_FRONTEND) npm ci

db-up:
	docker compose up -d --wait

db-down:
	docker compose down

# Le schema est recree par Flyway au prochain demarrage du back
db-reset:
	docker compose down -v

db-psql:
	docker exec -it minitrello-postgres psql -U minitrello minitrello

backend:
	$(IN_BACKEND) $(MVNW) spring-boot:run

frontend:
	$(IN_FRONTEND) npm start

# -j2 : le back et le front tournent en meme temps (Ctrl+C arrete les deux)
dev: db-up
	$(MAKE) -j2 backend frontend

test: test-backend test-frontend

test-backend:
	$(IN_BACKEND) $(MVNW) test

test-frontend:
	$(IN_FRONTEND) npx ng test --watch=false

build:
	$(IN_BACKEND) $(MVNW) -DskipTests package
	$(IN_FRONTEND) npx ng build

clean:
	$(IN_BACKEND) $(MVNW) clean
ifeq ($(OS),Windows_NT)
	Remove-Item -Recurse -Force -ErrorAction SilentlyContinue frontend/dist, frontend/.angular
else
	rm -rf frontend/dist frontend/.angular
endif
