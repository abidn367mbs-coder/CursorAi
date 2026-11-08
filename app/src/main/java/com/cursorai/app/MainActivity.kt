package com.cursorai.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cursorai.app.ai.AiAssistant
import com.cursorai.app.ai.AssistantSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val Navy = Color(0xFF0B1020)
private val Panel = Color(0xFF11182B)
private val PanelAlt = Color(0xFF172139)
private val Border = Color(0xFF263454)
private val TextMain = Color(0xFFE7ECFF)
private val TextMuted = Color(0xFF8F9BBC)
private val Blue = Color(0xFF6EA8FE)
private val Green = Color(0xFF63D69F)
private val Orange = Color(0xFFFFC56E)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { CursorAiApp() }
    }
}

@Composable
private fun CursorAiApp() {
    val context = LocalContext.current
    var prompt by remember { mutableStateOf("") }
    var activeFile by remember { mutableStateOf("MainActivity.kt") }
    var isRunning by remember { mutableStateOf(false) }
    var answer by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }
    var settings by remember { mutableStateOf(loadAssistantSettings(context)) }
    var showSettings by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val assistant = remember(settings) { AiAssistant(settings) }
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Navy) {
            if (showSettings) {
                SettingsScreen(
                    settings = settings,
                    onBack = { showSettings = false },
                    onSave = {
                        settings = it
                        saveAssistantSettings(context, it)
                        showSettings = false
                    },
                )
            } else {
                Column {
                    TopBar(onBuild = { isRunning = true }, providerName = settings.displayProvider, onSettings = { showSettings = true })
                    Row(modifier = Modifier.fillMaxSize()) {
                        ActivityRail()
                        FileExplorer(activeFile = activeFile, onFileSelected = { activeFile = it })
                        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            EditorHeader(activeFile = activeFile)
                            CodeEditor(activeFile = activeFile, modifier = Modifier.weight(1f))
                            BottomPanel(isRunning = isRunning, onRun = { isRunning = true })
                        }
                        PromptPanel(
                            prompt = prompt,
                            answer = answer,
                            error = error,
                            isThinking = isThinking,
                            onPromptChange = { prompt = it },
                            onSend = {
                                if (prompt.isNotBlank() && !isThinking) {
                                    val requestedPrompt = prompt.trim()
                                    prompt = ""
                                    answer = ""
                                    error = ""
                                    isThinking = true
                                    scope.launch {
                                        val result = withContext(Dispatchers.IO) { assistant.ask(requestedPrompt) }
                                        result.onSuccess { answer = it }
                                            .onFailure { error = it.message ?: "Unable to reach the AI service" }
                                        isThinking = false
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBar(onBuild: () -> Unit, providerName: String, onSettings: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(58.dp).background(Panel).padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Code, contentDescription = null, tint = Blue, modifier = Modifier.size(25.dp))
        Spacer(Modifier.width(8.dp))
        Text("CursorAi", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("  /  $providerName", color = TextMuted, fontSize = 13.sp)
        Spacer(Modifier.weight(1f))
        TextButton(onClick = { }) { Text("Share", color = TextMuted) }
        Button(
            onClick = onBuild,
            colors = ButtonDefaults.buttonColors(containerColor = Blue, contentColor = Navy),
            shape = RoundedCornerShape(7.dp)
        ) {
            Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(5.dp))
            Text("Build", fontWeight = FontWeight.Bold)
        }
        IconButton(onClick = onSettings) { Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextMuted) }
    }
}

@Composable
private fun ActivityRail() {
    Column(
        modifier = Modifier.width(52.dp).fillMaxHeight().background(Color(0xFF0D1425)).padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Icon(Icons.Default.FolderOpen, contentDescription = "Explorer", tint = Blue, modifier = Modifier.size(23.dp))
        Icon(Icons.Default.Code, contentDescription = "Source", tint = TextMuted, modifier = Modifier.size(23.dp))
        Icon(Icons.Default.Terminal, contentDescription = "Terminal", tint = TextMuted, modifier = Modifier.size(23.dp))
        Spacer(Modifier.weight(1f))
        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextMuted, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun FileExplorer(activeFile: String, onFileSelected: (String) -> Unit) {
    Column(modifier = Modifier.width(190.dp).fillMaxHeight().background(Panel).padding(top = 14.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("EXPLORER", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.MoreVert, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(14.dp))
        Text("MOBILE-WORKSPACE", color = TextMain, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 14.dp))
        Spacer(Modifier.height(8.dp))
        FileRow("app", Icons.Default.Folder, false, activeFile, onFileSelected)
        FileRow("src", Icons.Default.Folder, false, activeFile, onFileSelected, indent = 12)
        FileRow("MainActivity.kt", Icons.Default.Description, true, activeFile, onFileSelected, indent = 24)
        FileRow("Theme.kt", Icons.Default.Description, false, activeFile, onFileSelected, indent = 24)
        FileRow("README.md", Icons.Default.Description, false, activeFile, onFileSelected)
        Spacer(Modifier.weight(1f))
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Green, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(6.dp))
            Text("Ready", color = TextMuted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun FileRow(name: String, icon: androidx.compose.ui.graphics.vector.ImageVector, selectable: Boolean, activeFile: String, onFileSelected: (String) -> Unit, indent: Int = 0) {
    val active = name == activeFile
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = (10 + indent).dp, end = 8.dp).height(30.dp)
            .background(if (active) PanelAlt else Color.Transparent, RoundedCornerShape(5.dp))
            .clickable(enabled = selectable) { onFileSelected(name) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = if (name == "app" || name == "src") Orange else TextMuted, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(7.dp))
        Text(name, color = if (active) TextMain else TextMuted, fontSize = 12.sp)
    }
}

@Composable
private fun EditorHeader(activeFile: String) {
    Row(modifier = Modifier.fillMaxWidth().height(38.dp).background(Color(0xFF0D1425)), verticalAlignment = Alignment.CenterVertically) {
        Row(modifier = Modifier.padding(horizontal = 14.dp).fillMaxHeight(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Description, contentDescription = null, tint = Blue, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(6.dp))
            Text(activeFile, color = TextMain, fontSize = 12.sp)
        }
        Spacer(Modifier.weight(1f))
        IconButton(onClick = { }) { Icon(Icons.Default.MoreVert, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp)) }
    }
}

@Composable
private fun CodeEditor(activeFile: String, modifier: Modifier = Modifier) {
    val code = if (activeFile == "MainActivity.kt") listOf(
        "package com.cursorai.app", "", "import android.os.Bundle", "import androidx.activity.ComponentActivity", "import androidx.activity.compose.setContent", "", "class MainActivity : ComponentActivity() {", "    override fun onCreate(savedInstanceState: Bundle?) {", "        super.onCreate(savedInstanceState)", "        setContent {", "            CursorAiApp()", "        }", "    }", "}"
    ) else listOf("package com.cursorai.app", "", "// Shared theme tokens live here", "// Ask the AI assistant to refactor this file.")
    Column(modifier = modifier.fillMaxWidth().background(Color(0xFF0B1120)).verticalScroll(rememberScrollState()).padding(vertical = 14.dp)) {
        code.forEachIndexed { index, line ->
            Row(modifier = Modifier.fillMaxWidth().height(23.dp)) {
                Text("%3d".format(index + 1), color = Color(0xFF4F6085), fontFamily = FontFamily.Monospace, fontSize = 12.sp, modifier = Modifier.width(42.dp).padding(end = 12.dp))
                Text(line, color = if (line.trimStart().startsWith("//")) Green else TextMain, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun BottomPanel(isRunning: Boolean, onRun: () -> Unit) {
    Column(modifier = Modifier.height(150.dp).fillMaxWidth().background(Panel)) {
        Row(modifier = Modifier.height(38.dp).fillMaxWidth().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("TERMINAL", color = TextMain, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(22.dp))
            Text("OUTPUT", color = TextMuted, fontSize = 11.sp)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onRun) { Icon(Icons.Default.PlayArrow, contentDescription = "Run", tint = Green, modifier = Modifier.size(19.dp)) }
        }
        Divider(color = Border)
        Text("$ ./gradlew assembleDebug", color = TextMain, fontFamily = FontFamily.Monospace, fontSize = 12.sp, modifier = Modifier.padding(12.dp, 10.dp, 12.dp, 4.dp))
        Text(if (isRunning) "✓ Build started — connect an Android SDK to generate the APK." else "Ready. Run a build to see output here.", color = if (isRunning) Green else TextMuted, fontFamily = FontFamily.Monospace, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 12.dp))
    }
}

@Composable
private fun PromptPanel(
    prompt: String,
    answer: String,
    error: String,
    isThinking: Boolean,
    onPromptChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    Column(modifier = Modifier.width(295.dp).fillMaxHeight().background(Panel).padding(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("AI WORKSPACE", color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Green, modifier = Modifier.size(15.dp))
        }
        Text(if (isThinking) "Contacting OpenAI securely…" else "OpenAI backend connected", color = if (isThinking) Orange else Green, fontSize = 11.sp, modifier = Modifier.padding(top = 5.dp))
        Spacer(Modifier.height(16.dp))
        Text("What should we build?", color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Text("Describe a change and review the suggested diff before applying it.", color = TextMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 5.dp))
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = prompt,
            onValueChange = onPromptChange,
            modifier = Modifier.fillMaxWidth().height(112.dp),
            placeholder = { Text("e.g. Add a login screen...", color = TextMuted, fontSize = 12.sp) },
            textStyle = MaterialTheme.typography.bodySmall.copy(color = TextMain),
            maxLines = 5,
            enabled = !isThinking
        )
        Spacer(Modifier.height(8.dp))
        Button(onClick = onSend, enabled = prompt.isNotBlank() && !isThinking, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Blue, contentColor = Navy), shape = RoundedCornerShape(7.dp)) {
            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(if (isThinking) "Thinking…" else "Ask CursorAi", fontWeight = FontWeight.Bold)
        }
        if (answer.isNotBlank() || error.isNotBlank()) {
            Spacer(Modifier.height(14.dp))
            Text("RESPONSE", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(answer.ifBlank { error }, color = if (error.isBlank()) TextMain else Color(0xFFFF8A8A), fontSize = 12.sp, modifier = Modifier.padding(top = 7.dp).verticalScroll(rememberScrollState()))
        }
        Spacer(Modifier.height(22.dp))
        Text("QUICK ACTIONS", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        QuickAction("Explain current file", Icons.Default.Code)
        QuickAction("Find and fix bugs", Icons.Default.CheckCircle)
        QuickAction("Prepare release build", Icons.Default.Upload)
        Spacer(Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CreateNewFolder, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("API adapter ready for later", color = TextMuted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun QuickAction(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Blue, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, color = TextMain, fontSize = 12.sp)
    }
}

private fun loadAssistantSettings(context: Context): AssistantSettings {
    val prefs = context.getSharedPreferences("cursorai_settings", Context.MODE_PRIVATE)
    return AssistantSettings(
        provider = prefs.getString("provider", "openai") ?: "openai",
        model = prefs.getString("model", "gpt-4o-mini") ?: "gpt-4o-mini",
        baseUrl = prefs.getString("baseUrl", "") ?: "",
    )
}

private fun saveAssistantSettings(context: Context, settings: AssistantSettings) {
    context.getSharedPreferences("cursorai_settings", Context.MODE_PRIVATE).edit()
        .putString("provider", settings.provider)
        .putString("model", settings.model)
        .putString("baseUrl", settings.baseUrl)
        .apply()
}

@Composable
private fun SettingsScreen(settings: AssistantSettings, onBack: () -> Unit, onSave: (AssistantSettings) -> Unit) {
    var selectedProvider by remember(settings) { mutableStateOf(settings.provider) }
    var model by remember(settings) { mutableStateOf(settings.model) }
    var baseUrl by remember(settings) { mutableStateOf(settings.baseUrl) }
    Column(modifier = Modifier.fillMaxSize().background(Navy).verticalScroll(rememberScrollState())) {
        Row(
            modifier = Modifier.fillMaxWidth().height(58.dp).background(Panel).padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextMain) }
            Text("AI Provider Settings", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Column(modifier = Modifier.width(520.dp).padding(22.dp)) {
            Text("Choose a provider", color = TextMain, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("The app sends the selection to your secure backend. Provider API keys never belong in the APK.", color = TextMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
            Spacer(Modifier.height(16.dp))
            ProviderOption("openai", "OpenAI", "GPT models through the configured server key", selectedProvider == "openai") {
                selectedProvider = "openai"
                if (model == "claude-3-5-haiku-latest" || model == "your-coding-model") model = "gpt-4o-mini"
            }
            ProviderOption("anthropic", "Claude", "Anthropic models through the configured server key", selectedProvider == "anthropic") {
                selectedProvider = "anthropic"
                if (model == "gpt-4o-mini" || model == "your-coding-model") model = "claude-3-5-haiku-latest"
            }
            ProviderOption("nvidia", "NVIDIA NIM", "Llama, Gemma and Nemotron through NVIDIA API", selectedProvider == "nvidia") {
                selectedProvider = "nvidia"
                if (model == "gpt-4o-mini" || model == "claude-3-5-haiku-latest" || model == "your-coding-model") model = "meta/llama-3.1-70b-instruct"
            }
            ProviderOption("self-hosted", "Self-hosted model", "OpenAI-compatible endpoint such as vLLM or Ollama", selectedProvider == "self-hosted") {
                selectedProvider = "self-hosted"
                if (model == "gpt-4o-mini" || model == "claude-3-5-haiku-latest" || model == "meta/llama-3.1-70b-instruct") model = "your-coding-model"
            }
            Spacer(Modifier.height(18.dp))
            Text("MODEL CONFIGURATION", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("Model name") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
            if (selectedProvider == "nvidia") {
                Text("Examples: meta/llama-3.1-70b-instruct · google/gemma-7b · choose a Nemotron ID from NVIDIA Model Catalog", color = TextMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 6.dp))
            }
            OutlinedTextField(value = baseUrl, onValueChange = { baseUrl = it }, label = { Text("Optional backend URL override") }, placeholder = { Text("Uses build default when blank") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
            Text("For production, use an HTTPS backend. Leave this blank to use the URL configured at build time.", color = TextMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 6.dp))
            Spacer(Modifier.height(18.dp))
            Button(onClick = { onSave(AssistantSettings(selectedProvider, model.trim(), baseUrl.trim())) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Blue, contentColor = Navy), shape = RoundedCornerShape(7.dp)) {
                Text("Save provider settings", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ProviderOption(id: String, title: String, subtitle: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp).background(if (selected) PanelAlt else Panel, RoundedCornerShape(8.dp)).clickable { onClick() }.padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(18.dp).background(if (selected) Blue else Border, RoundedCornerShape(9.dp)))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = TextMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 3.dp))
        }
    }
}
