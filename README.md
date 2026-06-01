# SaaS Multi-Tenant App

Application **SaaS multi-tenant** de gestion de stock, développée avec **Spring Boot 4** et **PostgreSQL**.  
Chaque entreprise (tenant) dispose de son propre schéma de base de données, avec authentification JWT, gestion des utilisateurs et API REST documentées.

---

## Fonctionnalités

- **Multi-tenancy** par schéma PostgreSQL (isolation des données par tenant)
- **Authentification JWT** (RS256) avec access token + refresh token
- Support **Bearer header** et **cookies HttpOnly**
- **Logout** avec révocation de tokens (blacklist)
- Gestion des **tenants** (approbation, activation, suspension)
- Gestion des **utilisateurs** et rôles (`ROLE_COMPANY_ADMIN`, `ROLE_ADMINISTRATOR`, etc.)
- CRUD **catégories**, **produits**, **mouvements de stock** (IN / OUT)
- Pagination et recherche avec `PageResponse`
- Migrations **Flyway**
- Documentation **Swagger / OpenAPI**
- Sécurité : CSRF, rate limiting auth, headers HTTP, CORS, BCrypt

---

## Stack technique

| Composant        | Technologie                          |
|------------------|--------------------------------------|
| Langage          | Java 17                              |
| Framework        | Spring Boot 4.0.6                    |
| Persistence      | Spring Data JPA / Hibernate          |
| Base de données  | PostgreSQL 17                        |
| Migrations       | Flyway                               |
| Sécurité         | Spring Security + JWT (jjwt)         |
| Documentation    | SpringDoc OpenAPI 3                  |
| Build            | Maven                                |
| Conteneurisation | Docker Compose                       |

---

## Architecture

```
src/main/java/com/example/saas/
├── config/          # Multi-tenant, JPA, beans
├── controllers/     # REST API
├── entities/        # Entités JPA
├── enums/           # Rôles, statuts, types de mouvement
├── exceptions/      # Exceptions métier + GlobalExceptionHandler
├── mappers/         # DTO ↔ Entity
├── properties/      # Configuration JWT & sécurité
├── repositories/    # Spring Data JPA
├── request/         # DTOs entrée
├── response/        # DTOs sortie
├── security/        # JWT, filtres, SecurityConfig
└── services/        # Logique métier
```

### Modèle de données (schéma `public`)

- `tenants` — entreprises clientes
- `users` — utilisateurs liés à un tenant
- `categories` — catégories produits (par tenant)
- `products` — produits avec référence, prix, seuil d'alerte
- `stock_mvts` — mouvements de stock (`IN` / `OUT`)

---

## Prérequis

- **Java 17+**
- **Maven 3.9+**
- **Docker** & **Docker Compose** (recommandé)
- Port **5433** libre pour PostgreSQL Docker (évite le conflit avec une install locale sur 5432)

---

## Démarrage rapide

### 1. Cloner le projet

```bash
git clone https://github.com/<votre-username>/saas-multi-tenant-app.git
cd saas-multi-tenant-app
```

### 2. Lancer PostgreSQL

```bash
docker compose up -d
```

### 3. Configurer l'environnement

**a) Variables d'environnement**

```bash
cp .env.example .env
```

Éditez `.env` avec vos valeurs. Ce fichier **n'est pas versionné** (secrets locaux).

**b) Clés JWT (RS256)**

Les fichiers `private_key.pem` et `public_key.pem` **ne sont pas dans le dépôt Git**.

Créez le dossier et générez une paire de clés :

```bash
mkdir -p src/main/resources/certs

openssl genrsa -out src/main/resources/certs/private_key.pem 2048
openssl rsa -in src/main/resources/certs/private_key.pem -pubout -out src/main/resources/certs/public_key.pem
```

> Ne commitez jamais les fichiers `.pem` ni le fichier `.env`.

### 4. Lancer l'application

```bash
./mvnw spring-boot:run
```

Ou depuis IntelliJ : exécuter `SaasMultiTenantAppApplication`.

L'API est disponible sur : **http://localhost:8080**

---

## Documentation API

| Ressource   | URL                                              |
|-------------|--------------------------------------------------|
| Swagger UI  | http://localhost:8080/swagger-ui/index.html      |
| OpenAPI JSON| http://localhost:8080/v3/api-docs                |

---

## Authentification

### Login

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "votre-mot-de-passe"
}
```

**Réponse :**

```json
{
  "accessToken": "eyJ...",
  "refreshToken": "eyJ...",
  "tokenType": "Bearer",
  "expiresIn": 900000,
  "userId": "...",
  "username": "admin",
  "tenantId": "...",
  "role": "ROLE_COMPANY_ADMIN"
}
```

Des cookies HttpOnly (`access_token`, `refresh_token`) sont aussi posés pour les clients web.

### Requêtes authentifiées

**Option A — Header (Postman, mobile)**

```http
Authorization: Bearer <accessToken>
```

**Option B — Cookies (navigateur)**

```javascript
fetch('/api/v1/categorie', { credentials: 'include' });
```

### Refresh token

```http
POST /api/v1/auth/refresh
Cookie: refresh_token=...
```

### Logout

```http
POST /api/v1/auth/logout
Authorization: Bearer <accessToken>
X-XSRF-TOKEN: <valeur du cookie XSRF-TOKEN>
```

### Profil connecté

```http
GET /api/v1/auth/me
Authorization: Bearer <accessToken>
```

---

## Endpoints principaux

### Auth — `/api/v1/auth`

| Méthode | Endpoint   | Description              | Auth |
|---------|------------|--------------------------|------|
| POST    | `/login`   | Connexion                | Non  |
| POST    | `/refresh` | Renouveler access token  | Non  |
| POST    | `/logout`  | Déconnexion              | Oui  |
| GET     | `/me`      | Utilisateur courant      | Oui  |

### Tenants — `/api/v1/tenants`

| Méthode | Endpoint                  | Description           |
|---------|---------------------------|-----------------------|
| GET     | `/`                       | Liste paginée         |
| POST    | `/approve/{tenant-id}`    | Approuver + provision |
| PATCH   | `/activate/{tenant-id}`   | Activer               |
| PATCH   | `/deactivate/{tenant-id}` | Désactiver          |
| PATCH   | `/suspend/{tenant-id}`    | Suspendre             |

### Users — `/api/v1/users`

| Méthode | Endpoint              | Description        |
|---------|-----------------------|--------------------|
| POST    | `/`                   | Créer utilisateur  |
| GET     | `/`                   | Liste paginée      |
| GET     | `/{user-id}`          | Détail             |
| PUT     | `/{user-id}`          | Modifier           |
| DELETE  | `/{user-id}`          | Supprimer (soft)   |
| PUT     | `/{user-id}/enable`   | Activer            |
| PUT     | `/{user-id}/disable`  | Désactiver         |

### Catégories — `/api/v1/categorie`

| Méthode | Endpoint              | Description   |
|---------|-----------------------|---------------|
| POST    | `/`                   | Créer         |
| GET     | `/`                   | Liste paginée |
| GET     | `/search?keyword=`    | Recherche     |
| GET     | `/{categorie_id}`     | Détail        |
| PUT     | `/{categorie_id}`     | Modifier      |
| DELETE  | `/{categorie_id}`     | Supprimer     |

### Produits — `/api/v1/product`

Même structure que les catégories (`/search`, pagination, CRUD).

### Mouvements de stock — `/api/v1/stock-mvt`

| Méthode | Endpoint           | Description                          |
|---------|--------------------|--------------------------------------|
| POST    | `/`                | Créer mouvement (`IN` ou `OUT`)      |
| GET     | `/`                | Liste paginée                        |
| GET     | `/search?keyword=` | Recherche (commentaire, produit)     |

---

## Rôles utilisateur

| Rôle                    | Description                    |
|-------------------------|--------------------------------|
| `ROLE_PLATFORM_ADMIN`   | Administration plateforme      |
| `ROLE_COMPANY_ADMIN`    | Admin entreprise (tenant)      |
| `ROLE_ADMINISTRATOR`    | Administrateur                 |
| `ROLE_USER`             | Utilisateur standard           |
| `ROLE_SALES_OPERATOR`   | Opérateur ventes               |

---

## Sécurité

| Mesure              | Détail                                              |
|---------------------|-----------------------------------------------------|
| **JWT RS256**       | Clés RSA locales dans `src/main/resources/certs/` (gitignored) |
| **BCrypt**          | Hash des mots de passe                              |
| **CSRF**            | Cookie `XSRF-TOKEN` (requêtes avec cookies)       |
| **Rate limiting**   | 10 tentatives / min / IP sur login & refresh        |
| **Token blacklist** | Révocation au logout                                |
| **Headers HTTP**    | HSTS, X-Frame-Options, Referrer-Policy              |
| **SQL injection**   | Requêtes JPA paramétrées                            |
| **CORS**            | Configurable dans `SecurityConfig`                |

### Fichiers exclus du dépôt Git

| Fichier | Raison |
|---------|--------|
| `.env` | Mots de passe DB, config locale |
| `certs/*.pem` | Clés privées/publiques JWT |

Utilisez `.env.example` comme modèle pour la configuration.

> En production : `COOKIE_SECURE=true`, HTTPS, clés JWT dédiées par environnement, secrets via variables CI/CD (GitHub Secrets, etc.).

---

## Configuration

Fichier principal : `src/main/resources/application.yml`

| Variable                      | Défaut              | Description                    |
|-------------------------------|---------------------|--------------------------------|
| `DB_HOST`                     | `localhost`         | Hôte PostgreSQL                |
| `DB_PORT`                     | `5433`              | Port PostgreSQL                |
| `DB_NAME`                     | `saas-db`           | Nom de la base                 |
| `DB_USERNAME`                 | `postgres`          | Utilisateur DB                 |
| `DB_PASSWORD`                 | `postgres`          | Mot de passe DB                |
| `SERVER_PORT`                 | `8080`              | Port de l'API                  |
| `JWT_PRIVATE_KEY_PATH`        | `certs/private_key.pem` | Chemin clé privée JWT      |
| `JWT_PUBLIC_KEY_PATH`         | `certs/public_key.pem`  | Chemin clé publique JWT    |
| `JWT_ACCESS_TOKEN_EXPIRATION` | `900000` (15 min)   | Durée access token (ms)        |
| `JWT_REFRESH_TOKEN_EXPIRATION`| `604800000` (7 j)   | Durée refresh token (ms)       |
| `COOKIE_SECURE`               | `false`             | Cookie Secure flag (HTTPS)     |
| `AUTH_RATE_LIMIT_MAX`         | `10`                | Max requêtes auth par fenêtre  |
| `AUTH_RATE_LIMIT_WINDOW`      | `60`                | Fenêtre rate limit (secondes)  |

Voir `.env.example` pour un modèle complet.

---

## Migrations Flyway

| Fichier                                      | Schéma   | Description              |
|----------------------------------------------|----------|--------------------------|
| `db.migration/commom/V1__Init_DB.sql`        | `public` | Tables globales          |
| `db.migration/tenant/V1__Init_DB_For_Tenant.sql` | tenant | Tables par tenant    |

Pour repartir sur une base vide :

```bash
docker compose down -v
docker compose up -d
```

---

## Tests

```bash
./mvnw test
```

---

## Structure des réponses paginées

```json
{
  "content": [...],
  "totalElements": 42,
  "totalPages": 5,
  "pageNumber": 0,
  "pageSize": 10,
  "hasNextPage": true,
  "hasPreviousPage": false,
  "firstPage": true,
  "lastPage": false
}
```

---

## Roadmap

- [ ] Enregistrement tenant via API publique
- [ ] Blacklist Redis (multi-instances)
- [ ] Tests d'intégration auth & multi-tenant
- [ ] Dashboard alertes stock (seuil `alert_threshold`)

---

## Auteur

Projet réalisé dans le cadre de l'apprentissage **SaaS multi-tenant** avec Spring Boot.

---

## Licence

Ce projet est à usage éducatif. Ajoutez une licence (MIT, Apache 2.0, etc.) avant publication publique si nécessaire.
