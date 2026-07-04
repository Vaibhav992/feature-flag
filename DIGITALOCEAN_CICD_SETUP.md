# DigitalOcean CI/CD Setup Guide (Feature Flag Service)

This guide explains:
- how to create DigitalOcean infrastructure,
- how to prepare a droplet with Docker,
- how to set up CI/CD from GitHub Actions,
- what to configure in this repository.

It is intentionally practical and simple (KISS/YAGNI).

---

## 1) Target Deployment Architecture

- GitHub (`main` branch)
  -> GitHub Actions CI (`mvn clean verify`)
  -> GitHub Actions CD (build Docker image and push to registry)
  -> DigitalOcean Container Registry (`registry.digitalocean.com/feature-flag`)
  -> DigitalOcean Droplet (runs container)
  -> DigitalOcean Managed PostgreSQL (used by app)

---

## 2) Create DigitalOcean Managed PostgreSQL

1. In DigitalOcean dashboard:
   - `Create` -> `Databases` -> `PostgreSQL`.
2. Choose region close to users.
3. Pick plan/size.
4. After creation, collect:
   - host
   - port
   - database
   - username
   - password
   - SSL mode (`require`)
5. Allow trusted sources:
   - add your droplet IP
   - add your local IP (optional for manual testing)
6. Keep credentials in secrets only (do not commit in repo).

---

## 3) Create and Prepare Droplet

1. Create droplet:
   - Ubuntu LTS (24.04/22.04)
   - Basic plan is fine to start
   - Enable password login (as per your setup)
2. Optional but recommended:
   - reserve a static IP
   - attach domain DNS (`A` record to droplet IP)
3. SSH into droplet:

```bash
ssh root@<DROPLET_IP>
```

Then enter root password when prompted.

4. Basic hardening:

```bash
adduser deployer
usermod -aG sudo deployer
ufw allow OpenSSH
ufw allow 80
ufw allow 443
ufw --force enable
```

---

## 4) Install Docker on Droplet

Run as root (or with sudo):

```bash
apt-get update
apt-get install -y ca-certificates curl gnupg
install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
chmod a+r /etc/apt/keyrings/docker.asc
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" \
  > /etc/apt/sources.list.d/docker.list
apt-get update
apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin
usermod -aG docker deployer
```

Re-login after group update.

---

## 5) App Runtime Folder on Droplet

Create deployment folder:

```bash
mkdir -p /opt/feature-flag
cd /opt/feature-flag
```

Create `.env` for container runtime using nano:

```bash
nano /opt/feature-flag/.env
```

Paste this:

```bash
APP_NAME=feature-flag
spring.profiles.active=prod
SERVER_PORT=8080

DB_HOST=<DO_POSTGRES_HOST>
DB_PORT=<DO_POSTGRES_PORT>
DB_NAME=<DO_POSTGRES_DB>
DB_USERNAME=<DO_POSTGRES_USER>
DB_PASSWORD=<DO_POSTGRES_PASSWORD>
DB_SSLMODE=require
```

Nano save/exit:
- `Ctrl + O`
- `Enter`
- `Ctrl + X`

Note:
- This `.env` stays on server only.
- Never commit this file.

---

## 6) Dockerfile in Repository

Add a simple Dockerfile at repo root:

```dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/feature-flag-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
```

If jar name changes, update `COPY` line.

---

## 7) Run Container on Droplet (No Compose)

Pull and run container directly:

```bash
docker pull registry.digitalocean.com/feature-flag/feature-flag:latest
docker rm -f feature-flag || true
docker run -d \
  --name feature-flag \
  --restart unless-stopped \
  --env-file /opt/feature-flag/.env \
  -p 8080:8080 \
  registry.digitalocean.com/feature-flag/feature-flag:latest
docker logs -f feature-flag
```

---

## 8) GitHub Secrets Required

In GitHub repo -> `Settings` -> `Secrets and variables` -> `Actions`, add:

- `DROPLET_HOST` -> droplet public IP
- `DROPLET_USER` -> `deployer` (or your deploy user)
- `DROPLET_PASSWORD` -> droplet SSH password
- `DIGITALOCEAN_ACCESS_TOKEN` -> DigitalOcean personal access token
- `DOCR_REGISTRY` -> `registry.digitalocean.com/feature-flag`

If you want Actions to also manage app env remotely, add:
- `APP_ENV_FILE` (optional multi-line `.env` content)

---

## 9) CI Workflow (`.github/workflows/ci.yml`)

Recommended CI:
- trigger on push + pull request
- setup Java 21
- run `./mvnw clean verify`
- optionally build docker image (no push)

Minimal flow:
1. Checkout
2. Setup Java 21
3. Cache Maven
4. `./mvnw clean verify`

---

## 10) CD Workflow (`.github/workflows/cd.yml`)

Recommended CD on `main`:
1. Checkout
2. Setup Java 21
3. Build jar (`./mvnw clean package -DskipTests`)
4. Build Docker image
5. Docker login to DOCR
6. Push image to DOCR
7. SSH to droplet (password login):
   - `docker login registry.digitalocean.com`
   - `docker pull registry.digitalocean.com/feature-flag/feature-flag:latest`
   - `docker rm -f feature-flag || true`
   - `docker run -d --name feature-flag --restart unless-stopped --env-file /opt/feature-flag/.env -p 8080:8080 registry.digitalocean.com/feature-flag/feature-flag:latest`
   - run health check

Core image push commands:

```bash
echo "${DIGITALOCEAN_ACCESS_TOKEN}" | docker login registry.digitalocean.com -u doctl --password-stdin
docker build -t registry.digitalocean.com/feature-flag/feature-flag:latest .
docker push registry.digitalocean.com/feature-flag/feature-flag:latest
```

Remote deploy (GitHub Actions) using password-based SSH:

```bash
sudo apt-get update && sudo apt-get install -y sshpass
sshpass -p "${DROPLET_PASSWORD}" ssh -o StrictHostKeyChecking=no ${DROPLET_USER}@${DROPLET_HOST} \
  "echo '${DIGITALOCEAN_ACCESS_TOKEN}' | docker login registry.digitalocean.com -u doctl --password-stdin && \
   docker pull registry.digitalocean.com/feature-flag/feature-flag:latest && \
   docker rm -f feature-flag || true && \
   docker run -d --name feature-flag --restart unless-stopped --env-file /opt/feature-flag/.env -p 8080:8080 registry.digitalocean.com/feature-flag/feature-flag:latest"
```

Health check example:

```bash
curl -f http://localhost:8080/actuator/health
```

---

## 11) Repository Checklist

Ensure repo contains:

- `Dockerfile`
- `.github/workflows/ci.yml`
- `.github/workflows/cd.yml`
- `src/main/resources/application.yml`
- `src/main/resources/application-prod.yml`
- `src/main/resources/db/migration/...` (Flyway scripts)
- `.gitignore` includes `.env`

Do not commit:
- `.env`
- SSH private keys
- DB passwords

---

## 12) First Deployment Runbook

1. Push code to `main`.
2. Confirm CI passed.
3. Confirm CD ran successfully.
4. First-time on droplet, login to registry:

```bash
echo "<DIGITALOCEAN_ACCESS_TOKEN>" | docker login registry.digitalocean.com -u doctl --password-stdin
```

5. On droplet:
   - `docker ps`
   - `docker logs -f feature-flag`
6. Validate app endpoint manually.
7. If failure:
   - inspect GitHub Actions logs
   - inspect container logs
   - verify `.env` values
   - verify DB network trusted sources

---

## 13) Troubleshooting Quick Notes

- `UnsupportedClassVersionError`: Maven/Java mismatch. Use Java 21 for both `java` and `mvn`.
- `Failed to configure DataSource`: prod profile not active or DB env missing.
- `Unsupported Database` (Flyway): include `flyway-database-postgresql` dependency.
- `Port already in use`: free port 8080 or change `SERVER_PORT`.

---

## 14) Security Best Practices (Minimum)

- Rotate database credentials regularly.
- Use least-privilege DB user.
- If using password login, use a strong password and rotate it regularly.
- Restrict database trusted sources.
- Keep secrets only in GitHub secrets and droplet `.env`.

