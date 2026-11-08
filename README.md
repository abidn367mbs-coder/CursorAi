# CursorAi

CursorAi is an Android Studio-ready Kotlin + Jetpack Compose prototype for a prompt-driven coding workspace. This first release intentionally ships the complete interface without an AI provider: API integration can be added later through a dedicated adapter layer.

## Included in this first version

- Dark IDE-style workspace with activity rail, file explorer, editor, terminal/output panel, and AI workspace.
- Prompt composer with quick actions for explaining files, finding bugs, and preparing a release build.
- Provider selector and local settings screen for OpenAI, Claude, and OpenAI-compatible self-hosted models.
- Build status and APK-oriented workflow placeholders.
- Gradle version catalog and GitHub Actions workflow that builds and uploads a debug APK.
- Dev Container configuration for GitHub Codespaces with Android SDK and VS Code extensions.

## Open in Android Studio

1. Clone this repository or download it as a ZIP.
2. Open the repository root in Android Studio Ladybug or newer.
3. Allow Gradle sync to finish and install Android SDK Platform 35 if prompted.
4. Run the `app` configuration on an Android emulator or physical device.

## Build an APK in Codespaces

```bash
chmod +x gradlew
./gradlew assembleDebug
```

The APK will be at `app/build/outputs/apk/debug/app-debug.apk`. On GitHub, every push to `main` also runs `.github/workflows/android.yml` and uploads `cursorai-debug-apk` as a workflow artifact.

## Add an AI API later

Keep UI state independent from the provider. A recommended next step is to add an `AiAssistant` interface under `app/src/main/java/com/cursorai/app/ai/`, then implement a provider using a secure backend. Do not put secret API keys in the Android client or commit them to this repository.

## Current limitations

The AI Workspace now calls the included OpenAI backend. The editor, project file system, and terminal remain intentionally scoped UI surfaces; arbitrary code execution and file mutation need an additional sandbox/permission layer before enabling them in production. See [DEPLOYMENT.md](DEPLOYMENT.md) for backend setup, API configuration, and release signing.

Open the gear icon in the app to choose a provider and model. The selection is stored in Android local preferences and sent as metadata to the backend; provider credentials remain server-side.
