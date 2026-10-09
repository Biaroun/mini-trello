# Mini Trello – Contexte du projet

## Objectif

Petit projet d'entraînement pour pratiquer la stack **Java / Spring Boot / PostgreSQL / Angular** avant un stage Full Stack.
Le but n'est pas de cloner Trello entièrement, mais d'avoir une application **simple, propre, testée et documentée**.

Priorités, dans l'ordre :
1. Une API REST propre et bien structurée
2. Des tests (JUnit 5 + Mockito)
3. Un front Angular fonctionnel
4. Un README clair

---

## Stack technique

### Backend
- Java 21
- Spring Boot (dernière version stable) : Web, Data JPA, Validation
- PostgreSQL + Hibernate
- Flyway pour les migrations
- springdoc-openapi (Swagger UI)
- Maven
- Tests : JUnit 5, Mockito, `@WebMvcTest`, `@DataJpaTest` (optionnel : Testcontainers)

### Frontend
- Angular (dernière version stable) avec composants **standalone**
- Signals pour l'état local
- Formulaires réactifs (Reactive Forms)
- `HttpClient` + services
- Angular CDK Drag & Drop pour déplacer les cartes (bonus)
- UI : Angular Material ou CSS simple

### Outils
- Docker Compose pour lancer PostgreSQL
- Git + GitHub (une branche/PR par fonctionnalité)
- GitHub Actions pour build + tests (bonus)

---

## Fonctionnalités (MVP)

### Tableaux (Boards)
- Créer, lister, renommer, supprimer un tableau

### Colonnes (Columns)
- Ajouter une colonne à un tableau (ex : "À faire", "En cours", "Terminé")
- Renommer, supprimer une colonne
- Les colonnes ont un ordre (`position`)

### Cartes (Cards)
- Créer une carte dans une colonne
- Modifier : titre, description, priorité, date d'échéance
- Supprimer une carte
- **Déplacer** une carte dans une autre colonne et/ou à une autre position

### Hors périmètre (volontairement)
- Authentification / utilisateurs multiples
- Temps réel (WebSocket)
- Pièces jointes, commentaires, étiquettes

Ces points peuvent être ajoutés plus tard comme évolutions.

---

## Modèle de données

```
Board (1) ──── (n) BoardColumn (1) ──── (n) Card
```

### Board
| Champ      | Type          | Contraintes          |
|------------|---------------|----------------------|
| id         | Long          | PK, auto             |
| name       | String        | non vide, max 100    |
| createdAt  | LocalDateTime | auto                 |

### BoardColumn
> Nommée `BoardColumn` car `Column` entre en conflit avec l'annotation JPA `@Column`.

| Champ    | Type   | Contraintes                 |
|----------|--------|-----------------------------|
| id       | Long   | PK, auto                    |
| name     | String | non vide, max 50            |
| position | int    | ≥ 0                         |
| board    | Board  | FK, `@ManyToOne(fetch = LAZY)` |

### Card
| Champ       | Type          | Contraintes                       |
|-------------|---------------|-----------------------------------|
| id          | Long          | PK, auto                          |
| title       | String        | non vide, max 100                 |
| description | String        | optionnel, max 1000               |
| priority    | enum          | LOW, MEDIUM, HIGH (défaut MEDIUM) |
| dueDate     | LocalDate     | optionnel                         |
| position    | int           | ≥ 0                               |
| column      | BoardColumn   | FK, `@ManyToOne(fetch = LAZY)`    |
| createdAt   | LocalDateTime | auto                              |

Suppression en cascade : supprimer un tableau supprime ses colonnes et ses cartes.

---

## Règles métier

1. Un nom de colonne doit être **unique dans un même tableau**.
2. Une nouvelle carte est ajoutée **à la fin** de sa colonne (position = nombre de cartes existantes).
3. Lors d'un déplacement de carte, les positions des autres cartes (colonne source et colonne cible) sont **réordonnées** pour rester continues (0, 1, 2…).
4. Une carte ne peut être déplacée que vers une colonne **du même tableau**.
5. La date d'échéance ne peut pas être dans le passé à la création.

Ces règles vivent dans la **couche service** et doivent être **testées unitairement**.

---

## API REST

Base : `/api`

### Boards
| Méthode | Route             | Description                          |
|---------|-------------------|--------------------------------------|
| GET     | `/boards`         | Liste des tableaux                   |
| GET     | `/boards/{id}`    | Détail d'un tableau avec colonnes et cartes |
| POST    | `/boards`         | Créer                                |
| PUT     | `/boards/{id}`    | Renommer                             |
| DELETE  | `/boards/{id}`    | Supprimer                            |

### Columns
| Méthode | Route                        | Description |
|---------|------------------------------|-------------|
| POST    | `/boards/{boardId}/columns`  | Créer       |
| PUT     | `/columns/{id}`              | Renommer    |
| DELETE  | `/columns/{id}`              | Supprimer   |

### Cards
| Méthode | Route                         | Description                         |
|---------|-------------------------------|-------------------------------------|
| POST    | `/columns/{columnId}/cards`   | Créer                               |
| PUT     | `/cards/{id}`                 | Modifier                            |
| PATCH   | `/cards/{id}/move`            | Déplacer `{ targetColumnId, position }` |
| DELETE  | `/cards/{id}`                 | Supprimer                           |

### Erreurs
Format uniforme géré par un `@RestControllerAdvice` :
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Board 12 introuvable",
  "timestamp": "2026-10-08T10:00:00"
}
```
- `400` : validation échouée (avec le détail des champs)
- `404` : ressource introuvable
- `409` : conflit de règle métier (ex : nom de colonne déjà utilisé)

---

## Architecture backend

```
src/main/java/com/example/minitrello/
├── board/
│   ├── Board.java
│   ├── BoardController.java
│   ├── BoardService.java
│   ├── BoardRepository.java
│   └── dto/
├── column/
├── card/
├── common/
│   ├── exception/      (exceptions métier + GlobalExceptionHandler)
│   └── config/         (CORS, OpenAPI)
└── MiniTrelloApplication.java

src/main/resources/
├── application.yml
└── db/migration/       (V1__init.sql, ...)
```

Conventions :
- Organisation **par fonctionnalité** (board, column, card), pas par couche technique
- Les contrôleurs ne manipulent **que des DTO** (records Java), jamais les entités
- Mapping entité ↔ DTO dans une classe `Mapper` (manuel ou MapStruct)
- Injection par constructeur
- `@Transactional` sur les méthodes de service qui écrivent
- `spring.jpa.hibernate.ddl-auto=validate` : le schéma est géré **uniquement par Flyway**

### Point d'attention Hibernate
`GET /boards/{id}` charge le tableau, ses colonnes et leurs cartes : risque de **problème N+1**.
À traiter avec `@EntityGraph` ou une requête `JOIN FETCH`, puis vérifier avec `spring.jpa.show-sql=true`.
Noter l'avant/après dans le README.

---

## Tests attendus

### Unitaires (service + Mockito)
- Création d'une carte → position en fin de colonne
- Déplacement d'une carte → positions réordonnées dans les deux colonnes
- Déplacement vers une colonne d'un autre tableau → exception
- Nom de colonne en double → exception
- Ressource introuvable → exception

### Intégration
- `@WebMvcTest` : statuts HTTP et validation des contrôleurs (200, 201, 400, 404)
- `@DataJpaTest` (ou Testcontainers) : requêtes personnalisées du repository

Objectif de couverture : ~70 % sur la couche service (mesurable avec JaCoCo, bonus).

---

## Architecture frontend

```
src/app/
├── core/
│   ├── models/          (interfaces TypeScript : Board, Column, Card)
│   ├── services/        (board.service.ts, column.service.ts, card.service.ts)
│   └── interceptors/    (gestion d'erreurs HTTP)
├── features/
│   ├── board-list/      (page liste des tableaux)
│   └── board-detail/    (page d'un tableau : colonnes + cartes)
│       ├── column/
│       ├── card/
│       └── card-dialog/ (formulaire de création/édition)
├── app.routes.ts
└── app.config.ts
```

Routes :
- `/boards` → liste des tableaux
- `/boards/:id` → détail du tableau

Conventions :
- Composants standalone uniquement
- Nouveau control flow (`@if`, `@for`)
- Les appels HTTP passent **uniquement** par les services
- URL de l'API dans `environment.ts`

---

## Lancement local

```bash
# Base de données
docker compose up -d

# Backend (http://localhost:8080, Swagger : /swagger-ui.html)
./mvnw spring-boot:run

# Frontend (http://localhost:4200)
npm install
ng serve
```

`docker-compose.yml` : un service `postgres` (image officielle, base `minitrello`, port 5432).

---

## Roadmap

- [ ] **Étape 1** – Initialiser le backend (Spring Initializr), docker-compose Postgres, Flyway `V1__init.sql`
- [ ] **Étape 2** – CRUD Boards + gestion d'erreurs globale + Swagger
- [ ] **Étape 3** – CRUD Columns + règle du nom unique + tests
- [ ] **Étape 4** – CRUD Cards + déplacement + tests des règles de position
- [ ] **Étape 5** – Optimisation N+1 sur le détail d'un tableau
- [ ] **Étape 6** – Initialiser Angular, modèles et services
- [ ] **Étape 7** – Page liste des tableaux
- [ ] **Étape 8** – Page détail : colonnes, cartes, formulaire de carte
- [ ] **Étape 9** – Drag & drop avec Angular CDK (bonus)
- [ ] **Étape 10** – README (captures, choix techniques, lancement), CI GitHub Actions (bonus)

---

## Évolutions possibles

- Authentification JWT (Spring Security) + utilisateurs propriétaires des tableaux
- Étiquettes et filtres sur les cartes
- Recherche paginée de cartes
- Tests e2e (Playwright)
- Déploiement (Render / Railway + Neon pour Postgres)
