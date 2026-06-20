# ClipIQ

AI-powered tool for transcribing and analyzing YouTube videos, TikTok clips, and local MP3/MP4 files.
Upload a file or paste a link — ClipIQ downloads the audio, transcribes it, summarizes the content, and determines whether the author's attitude is positive, negative, or neutral.

---

## What it does

1. You upload an MP3/MP4 file or a YouTube/TikTok URL
2. The backend downloads/converts the audio and sends it to the AI service
3. OpenAI Whisper transcribes the speech to text
4. GPT-3.5-turbo summarizes the transcript, and VADER (an offline sentiment lexicon, no extra API call) labels the author's attitude as positive, negative, or neutral
5. Results appear in real time via WebSocket — no need to refresh the page, with a live progress bar
6. Every analysis is saved to a local history (searchable, with PDF export and browser notifications)

---

## Tech stack

| Layer | Technology |
|---|---|
| Frontend | React 18, Vite, TypeScript, Tailwind CSS, SockJS + STOMP |
| Backend | Spring Boot 3.2, Spring Data MongoDB, Spring WebSocket |
| AI Service | FastAPI, OpenAI Whisper API, GPT-3.5-turbo, VADER (offline sentiment) |
| Database | MongoDB 7 |
| Infrastructure | Docker, Docker Compose, nginx |
| CI | GitHub Actions |

### Testing

| Type | Tools |
|---|---|
| Backend unit tests | JUnit 5, Mockito, MockWebServer, Testcontainers |
| Backend API tests | TestNG, REST Assured, JSON Schema validation, Allure |
| BDD | Cucumber 7 (7 scenarios), JUnit Platform Suite |
| Frontend | Jest, React Testing Library |
| E2E | Selenium 4, Page Object Model, allure-pytest |

---

## Running locally

### Prerequisites

- Docker + Docker Compose
- OpenAI API key ([platform.openai.com](https://platform.openai.com))

### 1. Clone and configure

```bash
git clone https://github.com/MichalAndrzejczak1/ClipIQ_Portfolio.git
cd ClipIQ_Portfolio
cp ai_service/.env.example ai_service/.env
```

Open `ai_service/.env` and set your key:

```
OPENAI_API_KEY=sk-...
```

Copy the root env file and adjust if needed (defaults work for local dev):

```bash
cp .env.example .env
```

### 2. Start all services

```bash
docker compose up --build
```

This starts MongoDB, the AI service, the Spring Boot backend, and the React frontend.
First build takes a few minutes (downloads ffmpeg, yt-dlp, etc.).

| Service | URL |
|---|---|
| Frontend | http://localhost:3000 |
| Backend API | http://localhost:8080 |
| AI Service | http://localhost:8000 |

### 3. Run backend tests

```bash
cd backend
./gradlew test        # JUnit 5
./gradlew testNG      # TestNG + REST Assured + Cucumber
```

Allure reports are generated in `backend/build/allure-results`.

### 4. Run AI service tests

```bash
cd ai_service
pip install -r requirements-dev.txt
pytest tests/ -v
```

### 5. Run frontend tests

```bash
cd frontend
npm install
npm test
```

### 6. Run Selenium E2E tests

Requires the full stack to be running (`docker compose up`).

```bash
cd selenium_tests
pip install -r requirements.txt
pytest tests/ -v --alluredir=allure-results
```
