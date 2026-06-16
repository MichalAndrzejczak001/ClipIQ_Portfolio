# ClipIQ

AI-powered tool for transcribing and analyzing YouTube videos, TikTok clips, and local MP3/MP4 files.
Upload a file or paste a link — ClipIQ downloads the audio, transcribes it, summarizes the content, and determines whether the author's attitude is positive, negative, or neutral.

---

## What it does

1. You upload an MP3/MP4 file or a YouTube/TikTok URL
2. The backend downloads/converts the audio and sends it to the AI service
3. OpenAI Whisper transcribes the speech to text
4. GPT-4o-mini generates a summary and sentiment label
5. Results appear in real time via WebSocket — no need to refresh the page

---

## Tech stack

| Layer | Technology |
|---|---|
| Frontend | React 18, Vite, TypeScript, Tailwind CSS, SockJS + STOMP |
| Backend | Spring Boot 3.2, Spring Data MongoDB, Spring WebSocket |
| AI Service | FastAPI, OpenAI Whisper API, GPT-4o-mini |
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
| E2E | Selenium (in progress) |
