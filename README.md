# Zelif Kudos

I genuinely love our team I work with, a.k.a my Zelifgangs.

Every day, I see my colleagues putting in real effort, helping each other out, going the extra mile, and making work a better place to be. I wanted a simple way to say "hey, I noticed, and I appreciate you." That's why I built this app.

Whenever someone lends you a hand, brightens your day, or does something worth celebrating, send them a kudo. It takes five seconds, but it can make someone's whole day.

Here's to a team that lifts each other up. Let's keep it going.

with love,
Seah

Try Me: https://zelifkudos.ddnsking.com/

<img width="227" height="24" alt="Screenshot 2026-03-16 at 4 53 58 PM" src="https://github.com/user-attachments/assets/7c1fcef5-a049-4170-abde-b1770756196d" />
<img width="1263" height="643" alt="Screenshot 2026-03-12 at 4 05 28 PM" src="https://github.com/user-attachments/assets/9b118394-d3cf-4236-8cbf-52f636257553" />
<img width="648" height="314" alt="Screenshot 2026-03-16 at 4 52 39 PM" src="https://github.com/user-attachments/assets/5c846c6d-343d-4203-9378-ca42760ed9f2" />
<img width="648" height="228" alt="Screenshot 2026-03-16 at 4 44 19 PM" src="https://github.com/user-attachments/assets/6bb95a43-fbea-4611-a31c-a13e50eb5533" />
<img width="649" height="375" alt="Screenshot 2026-03-17 at 6 14 59 PM" src="https://github.com/user-attachments/assets/69d03c46-ece7-4029-86cd-45384c1e7e1c" />
<img width="648" height="479" alt="Screenshot 2026-03-18 at 4 37 57 PM" src="https://github.com/user-attachments/assets/5da13304-b634-4ffc-ac3e-7812444d3cb9" />
<img width="647" height="389" alt="Screenshot 2026-03-18 at 4 42 14 PM" src="https://github.com/user-attachments/assets/167e69e6-03f2-4619-b883-c7586c8536a7" />
<img width="646" height="250" alt="Screenshot 2026-03-18 at 4 42 24 PM" src="https://github.com/user-attachments/assets/e1eeb352-4779-44e6-8e67-0ac32f002161" />
<img width="1470" height="835" alt="Screenshot 2026-03-28 at 7 20 23 PM" src="https://github.com/user-attachments/assets/e03c2e4e-4730-4333-9066-7ecd4d9758d8" />
---

## What Is This?

A peer recognition app built for my team at work. Designed, developed, and deployed solo — from idea to production with real users.

Team members send each other kudos throughout the week. Every Friday at 6 PM, everyone gets a personalized email report with the week's top stars and any messages they received. Then the slate resets and a new week begins.

## Why I Built It

I wanted my teammates to feel appreciated. Slack messages disappear, verbal praise gets forgotten. I wanted something persistent, fun, and low-friction — something that takes 5 seconds to use but makes someone's day.

No existing tool fit what I wanted, so I built it myself.

## Architecture

```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│   Browser   │────>│  Controller  │────>│   Service   │
│  (GSP+JS)   │<────│ + Interceptor│<────│ (@Transact) │
└──────┬──────┘     └──────────────┘     └──────┬──────┘
       │                                        │
       │ WebSocket                    GORM/HQL  │
       │ (SockJS+STOMP)                         │
       v                                        v
┌─────────────┐                        ┌─────────────┐
│  ChatWS     │──────────────────────> │ PostgreSQL  │
│  Controller │     ChatService        │ + Liquibase │
└─────────────┘                        │ + Session   │
                                       └─────────────┘
       ┌─────────────┐
       │   Quartz    │──> WeeklyEmailService ──> SMTP
       │ (FRI 18:00) │
       └─────────────┘
```

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Grails 6, Groovy, Spring Boot 2 |
| ORM | GORM 7 / Hibernate 5 |
| Database | PostgreSQL 16 |
| Real-time | WebSocket (SockJS + STOMP) |
| Auth | Passwordless magic link (email token) |
| Scheduling | Quartz 4.0 |
| Migration | Liquibase |
| Session | Spring Session JDBC (survives restarts) |
| Deployment | Docker multi-stage build, Oracle Cloud VM (Always Free) |
| UI | Windows 98 retro theme (pure CSS), Bootstrap 5 |

## Key Features & Technical Decisions

### Passwordless Authentication
No passwords to manage or forget. Users receive a magic link via email, click it, and they're in. Token expires in 15 minutes with a 2-minute cooldown to prevent spam. Access is restricted to `@zelifcam.net` company emails — authentication and authorization in one step.

### Anti-Spam: Defense in Depth
Abuse prevention at multiple layers, each enforced at the appropriate level:
- **Kudos** (service layer): 5/day limit per receiver + 10-minute cooldown between sends
- **Chat** (WebSocket controller): 3-second cooldown + duplicate message blocking within 10 seconds
- **Login** (service layer): 2-minute cooldown between token requests

### Real-Time Team Chat
SockJS + STOMP over WebSocket with session-based authentication at the handshake level. The `SessionHandshakeInterceptor` rejects connections without a valid session. Rate limiting is tracked per WebSocket session using `ConcurrentHashMap`, cleaned up on disconnect via `SessionDisconnectEvent`.

### Weekly Email Report System
Quartz cron job fires every Friday at 6 PM CT. Each user gets a personalized HTML email with:
- **Top 3 Stars** using dense ranking (ties share the same rank; e.g., 3 people tied for 1st all get gold)
- **Personal kudos count** and messages received
- **Rotating self-esteem messages** that cycle through the entire list before repeating

Smart recipient filtering: active users always receive the email; inactive users only get it if they received kudos that week. Sends emails first, resets the week only after all succeed.

### Weekly Reset with Audit Trail
Every reset is recorded in `KudosReset` with who triggered it (system or admin). This separates automated Friday resets from manual admin resets and provides a full history of cycle boundaries.

### Zero-Cost Production Deployment
- **Docker multi-stage build**: JDK 17 for build, JRE 17 for runtime (smaller image)
- **Oracle Cloud Always Free VM**: $0/month hosting
- **256MB JVM heap**: tuned for free-tier constraints
- **Spring Session JDBC**: sessions persist across Docker rebuilds, so users stay logged in during deploys
- **Liquibase**: schema changes ship with the app, no manual SQL on the server

### Windows 98 UI
811 lines of pure CSS recreating the classic Windows look. Draggable and resizable chat window, mobile-responsive (768px breakpoint), pixel-perfect window chrome and taskbar. The retro theme boosted team adoption — people used it because it was fun.

## What I Learned

- **Passwordless auth is underrated** — simpler to implement than password hashing + reset flows, better UX, and more secure for a small team app
- **Rate limiting belongs at the right layer** — HTTP-level limits wouldn't catch WebSocket abuse; service-level limits wouldn't catch duplicate messages. Each defense sits where it can actually see the problem
- **Dense ranking has edge cases** — "top 3" means top 3 rank levels, not top 3 people. Handling ties correctly required careful thought
- **Free-tier constraints force good decisions** — 256MB heap limit meant I couldn't be wasteful. Spring Session JDBC solved the "deploy kills all sessions" problem without Redis

## Project Structure

```
grails-app/
├── controllers/    # Thin controllers (auth, kudos, chat, users)
├── domain/         # 7 domain classes (User, Kudos, ChatMessage, Feeling, ...)
├── services/       # Business logic (kudos, login, email, chat, feeling)
├── jobs/           # Quartz scheduled jobs (weekly email + reset)
├── views/          # GSP server-rendered templates
└── conf/           # App config, Spring resources, interceptors

src/main/groovy/    # WebSocket config, chat controller, auth interceptor
src/main/resources/ # Liquibase changelogs
```

## Running Locally

```bash
# Start PostgreSQL
docker compose up -d

# Run the app (dev mode)
./gradlew bootRun

# Production build
docker build -t zelifkudos .
```

## Future Plans

- Service layer tests (KudosService, WeeklyEmailService, LoginService)
- CI/CD via GitHub Actions
- Self-esteem message seed data (100 messages ready, need DB migration)
