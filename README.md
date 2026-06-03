# SaaS Multi-Tenant App

Application **SaaS multi-tenant** de gestion de stock, développée avec **Spring Boot 4** et **PostgreSQL**.  
Chaque entreprise (tenant) dispose de son propre schéma de base de données, avec authentification JWT, gestion des utilisateurs, **notifications temps réel** (WebSocket STOMP) et API REST documentées.

---

## Fonctionnalités

- **Multi-tenancy** par schéma PostgreSQL (isolation des données par tenant)
- **Authentification JWT** (RS256) avec access token + refresh token
- Support **Bearer header** et **cookies HttpOnly**
- **Logout** avec révocation de tokens (blacklist)
- Gestion des **tenants** (approbation, activation, suspension)
- Gestion des **utilisateurs** et rôles (`ROLE_COMPANY_ADMIN`, `ROLE_ADMINISTRATOR`, etc.)
- CRUD **catégories**, **produits**, **mouvements de stock** (IN / OUT)
- **Notifications temps réel** via WebSocket STOMP (push instantané + historique REST)
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
| Temps réel       | WebSocket STOMP (Spring Messaging)   |
| Documentation    | SpringDoc OpenAPI 3                  |
| Build            | Maven                                |
| Conteneurisation | Docker Compose                       |

---

## Architecture

```
src/main/java/com/example/saas/
├── config/          # Multi-tenant, JPA, WebSocket STOMP
├── controllers/     # REST API
├── entities/        # Entités JPA
├── enums/           # Rôles, statuts, types de mouvement, notifications
├── exceptions/      # Exceptions métier + GlobalExceptionHandler
├── mappers/         # DTO ↔ Entity
├── notification/    # Destinations STOMP (/queue, /topic)
├── properties/      # Configuration JWT & sécurité
├── repositories/    # Spring Data JPA
├── request/         # DTOs entrée
├── response/        # DTOs sortie
├── security/        # JWT, filtres, SecurityConfig, JwtStompChannelInterceptor
└── services/        # Logique métier
```

### Modèle de données (schéma `public`)

- `tenants` — entreprises clientes
- `users` — utilisateurs liés à un tenant
- `categories` — catégories produits (par tenant)
- `products` — produits avec référence, prix, seuil d'alerte
- `stock_mvts` — mouvements de stock (`IN` / `OUT`)
- `notifications` — notifications utilisateur (type, priorité, lu/non lu, lien ressource)

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

Éditez le fichier **`.env`** à la racine du projet (chargé automatiquement au démarrage).  
Ce fichier **n'est pas versionné** (secrets locaux).

> Premier clone ? Copiez le template : `cp .env.example .env` puis adaptez les valeurs.

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

### 5. Compte admin système (automatique)

Au **premier démarrage**, l'application crée automatiquement :

- un tenant système (`companyCode: system`, statut `ACTIVE`)
- un utilisateur **`ROLE_PLATFORM_ADMIN`** pour administrer la plateforme (gestion des tenants)

| Variable | Défaut | Description |
|----------|--------|-------------|
| `SYSTEM_ADMIN_ENABLED` | `true` | Activer/désactiver l'initialisation |
| `SYSTEM_ADMIN_USERNAME` | `platform-admin` | Identifiant de connexion |
| `SYSTEM_ADMIN_PASSWORD` | `PlatformAdmin@123` | Mot de passe (à changer en prod) |
| `SYSTEM_ADMIN_EMAIL` | `platform-admin@system.local` | Email admin |

**Connexion :**

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "platform-admin",
  "password": "PlatformAdmin@123"
}
```

> L'initialisation est **idempotente** : si le username existe déjà, rien n'est recréé.  
> **Production** : définissez un mot de passe fort via `SYSTEM_ADMIN_PASSWORD` dans `.env` avant le premier démarrage.

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

> La création d'un mouvement de stock déclenche automatiquement une notification `STOCK_MOVEMENT` pour l'utilisateur connecté.

### Notifications — `/api/v1/notifications`

| Méthode | Endpoint           | Description                              |
|---------|--------------------|------------------------------------------|
| GET     | `/`                | Liste paginée (utilisateur connecté)   |
| GET     | `/unread-count`    | Nombre de notifications non lues         |
| PATCH   | `/{id}/read`       | Marquer une notification comme lue       |
| PATCH   | `/read-all`        | Tout marquer comme lu                    |
| DELETE  | `/{id}`            | Supprimer (soft delete)                  |

**Exemple de réponse (`NotificationResponse`) :**

```json
{
  "id": "...",
  "userId": "...",
  "tenantId": "...",
  "typeNotification": "STOCK_MOVEMENT",
  "title": "Mouvement de stock",
  "message": "IN : 10 unités — produit « Laptop »",
  "resourceType": "STOCK_MVT",
  "resourceId": "...",
  "priority": "MEDIUM",
  "read": false,
  "readAt": null,
  "createdAt": "2026-06-03T14:30:00"
}
```

**Types de notification :** `STOCK_ALERT`, `STOCK_MOVEMENT`, `TENANT_STATUS`, `USER_STATUS`, `SYSTEM`  
**Priorités :** `LOW`, `MEDIUM`, `HIGH`

---

## Notifications temps réel (WebSocket STOMP)

Les notifications sont **persistées en base** puis **poussées en temps réel** via WebSocket.

### Flux

```
Événement métier (ex. mouvement stock)
  → NotificationService.sendToUser()
  → INSERT en PostgreSQL
  → SimpMessagingTemplate → client WebSocket
```

### Connexion

| Paramètre    | Valeur                                      |
|--------------|---------------------------------------------|
| Endpoint     | `ws://localhost:8080/ws` (SockJS supporté)  |
| Protocole    | STOMP                                       |
| Auth CONNECT | Header `Authorization: Bearer <accessToken>` |

### Destinations STOMP

| Destination                               | Usage                                      |
|-------------------------------------------|--------------------------------------------|
| `/user/queue/notifications`               | Notifications privées (par utilisateur)    |
| `/topic/tenant/{tenantId}/notifications`  | Broadcast tenant (tableau de bord équipe)  |

### Exemple client (JavaScript)

```javascript
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const client = new Client({
  webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
  connectHeaders: {
    Authorization: 'Bearer ' + accessToken
  },
  onConnect: () => {
    client.subscribe('/user/queue/notifications', (message) => {
      const notification = JSON.parse(message.body);
      console.log('Nouvelle notification', notification);
    });
  }
});
client.activate();
```

> Si le client est déconnecté, l'historique reste accessible via `GET /api/v1/notifications`.

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
| **WebSocket JWT**   | Auth au frame STOMP `CONNECT` (`JwtStompChannelInterceptor`) |
| **CSRF WebSocket**  | `/ws/**` exempté du CSRF (auth JWT au CONNECT)    |

### Fichiers exclus du dépôt Git

| Fichier | Raison |
|---------|--------|
| `.env` | Mots de passe DB, config locale |
| `certs/*.pem` | Clés privées/publiques JWT |

Utilisez le fichier **`.env`** pour la configuration locale.

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
| `SYSTEM_ADMIN_ENABLED`        | `true`              | Création auto admin plateforme |
| `SYSTEM_ADMIN_USERNAME`       | `platform-admin`    | Username admin système         |
| `SYSTEM_ADMIN_PASSWORD`       | `PlatformAdmin@123` | Mot de passe admin système     |

Voir **`.env`** (ou `.env.example` comme template) pour la liste complète des variables.

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

- [x] Notifications temps réel (WebSocket STOMP + API REST)
- [ ] Alertes stock automatiques (`STOCK_ALERT` quand `availableQuantity <= alertThreshold`)
- [ ] Broker externe (RabbitMQ / Redis) pour scaling multi-instances
- [ ] Enregistrement tenant via API publique
- [ ] Blacklist Redis (multi-instances)
- [ ] Tests d'intégration auth, multi-tenant & WebSocket
- [ ] Frontend React (dashboard + cloche notifications)

---

## Auteur

Projet réalisé dans le cadre de l'apprentissage **SaaS multi-tenant** avec Spring Boot.

---

## Licence

Ce projet est à usage éducatif. Ajoutez une licence (MIT, Apache 2.0, etc.) avant publication publique si nécessaire.
