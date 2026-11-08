# CursorAi production setup

## 1. Deploy the OpenAI backend

The Android app calls `POST /v1/chat`; the server calls OpenAI without exposing the OpenAI key to the APK.

```bash
cd server
cp .env.example .env
# Edit .env and set OPENAI_API_KEY.
npm start
```

For a container host, build and run:

```bash
docker build -t cursorai-api ./server
docker run --rm -p 8080:8080 \
  -e OPENAI_API_KEY="$OPENAI_API_KEY" \
  -e CURSORAI_CLIENT_TOKEN="$(openssl rand -hex 24)" \
  cursorai-api
```

Set `CORS_ORIGIN` to a specific web origin when a web client is added. Use HTTPS in every hosted environment. Configure provider spending limits and monitoring in the OpenAI dashboard.

## 2. Point the Android app at the backend

For an emulator, the default URL is `http://10.0.2.2:8080`. For a physical phone or hosted backend, add these Gradle properties in `~/.gradle/gradle.properties` or pass them on the command line:

```properties
cursoraiApiBaseUrl=https://your-api.example.com
cursoraiClientToken=your-client-token
```

Then build:

```bash
./gradlew assembleDebug
```

The app sends only the user prompt and project context. The OpenAI secret remains server-side.

## 3. Release signing

A production Android release must be signed with your own keystore. Never commit the keystore or passwords. Add signing values through CI secrets and wire them into the `release` build type before publishing to Google Play. The repository currently produces an unsigned release artifact and a debug APK; this is intentional until the owner supplies a signing key.

## API contract

`POST /v1/chat`

```json
{
  "messages": [{"role": "user", "content": "Explain this file"}],
  "projectContext": "CursorAi Android project"
}
```

Response:

```json
{"model":"gpt-4o-mini","message":"..."}
```
