#Projeto Backend

## Rodando com MongoDB (branch `feature/migracao-mongodb`)

O banco é escolhido pelo **perfil** do Spring:

| Perfil | Banco | Dados iniciais |
| --- | --- | --- |
| `local` | MongoDB na sua máquina (`localhost:27017`, banco `helptec_local`) | Sim: usuários de teste, salas, equipamentos e skills |
| `professor` | MongoDB Atlas do professor (`MONGODB_URI`) | Não |

### 1. Configurar
Copie `.env.example` para `.env` na raiz do projeto. O `.env` não vai para o Git.
Para testar localmente deixe `spring.profiles.active=local`.

### 2. Subir o MongoDB local
Com Docker Desktop:
```bash
docker compose up -d
```
Isso sobe o MongoDB na porta 27017 e uma interface web em http://localhost:8081.
Sem Docker, instale o *MongoDB Community Server* (e o Compass para ver os dados).

### 3. Rodar o backend
Rode a `HelptecApplication` no IntelliJ (ou `./mvnw spring-boot:run`).
Na primeira execução o console mostra `[seed] ...` com o que foi criado.

Usuários de teste (senha `helptec123`, área "Informática"):

| E-mail | Papel |
| --- | --- |
| usuario@helptec.local | USER |
| aprovador@helptec.local | APROVADOR |
| gerente@helptec.local | GERENTE |
| suporte@helptec.local | SUPORTE |
| admin@helptec.local | ADMIN |

Entram por `POST /api/v1/auth/login` (Swagger em `/swagger-ui.html`).
Para entrar pelo **Google** no app, defina `SEED_GOOGLE_ADMIN_EMAIL` com o seu e-mail:
ele é cadastrado como ADMIN no banco local.

Para zerar o banco local: `docker compose down -v` (o seed roda de novo na próxima execução).

### 4. Trocar para o banco do professor
No `.env`:
```properties
spring.profiles.active=professor
MONGODB_URI=mongodb+srv://USUARIO:SENHA@CLUSTER.mongodb.net/?retryWrites=true&w=majority
MONGODB_DATABASE=helptec
```
Se a conexão der *timeout*, a rede pode estar bloqueando a porta 27017 ou o seu IP não está liberado
no Atlas (*Network Access*). Teste com `Test-NetConnection <host-do-cluster> -Port 27017`.
