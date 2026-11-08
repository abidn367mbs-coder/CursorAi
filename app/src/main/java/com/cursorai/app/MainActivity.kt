package com.cursorai.app

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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    var prompt by remember { mutableStateOf("") }
    var activeFile by remember { mutableStateOf("MainActivity.kt") }
    var isRunning by remember { mutableStateOf(false) }
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Navy) {
            Column {
                TopBar(onBuild = { isRunning = true })
                Row(modifier = Modifier.fillMaxSize()) {
                    ActivityRail()
                    FileExplorer(activeFile = activeFile, onFileSelected = { activeFile = it })
                    Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        EditorHeader(activeFile = activeFile)
                        CodeEditor(activeFile = activeFile)
                        BottomPanel(isRunning = isRunning, onRun = { isRunning = true })
                    }
                    PromptPanel(prompt = prompt, onPromptChange = { prompt = it }, onSend = { prompt = "" })
                }
            }
        }
    }
}

@Composable
private fun TopBar(onBuild: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(58.dp).background(Panel).padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Code, contentDescription = null, tint = Blue, modifier = Modifier.size(25.dp))
        Spacer(Modifier.width(8.dp))
        Text("CursorAi", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("  /  mobile-workspace", color = TextMuted, fontSize = 13.sp)
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
        IconButton(onClick = { }) { Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextMuted) }
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
private fun CodeEditor(activeFile: String) {
    val code = if (activeFile == "MainActivity.kt") listOf(
        "package com.cursorai.app", "", "import android.os.Bundle", "import androidx.activity.ComponentActivity", "import androidx.activity.compose.setContent", "", "class MainActivity : ComponentActivity() {", "    override fun onCreate(savedInstanceState: Bundle?) {", "        super.onCreate(savedInstanceState)", "        setContent {", "            CursorAiApp()", "        }", "    }", "}"
    ) else listOf("package com.cursorai.app", "", "// Shared theme tokens live here", "// Ask the AI assistant to refactor this file.")
    Column(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFF0B1120)).verticalScroll(rememberScrollState()).padding(vertical = 14.dp)) {
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
private fun PromptPanel(prompt: String, onPromptChange: (String) -> Unit, onSend: () -> Unit) {
    Column(modifier = Modifier.width(295.dp).fillMaxHeight().background(Panel).padding(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("AI WORKSPACE", color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Green, modifier = Modifier.size(15.dp))
        }
        Text("Interface mode • API not connected", color = TextMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 5.dp))
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
            maxLines = 5
        )
        Spacer(Modifier.height(8.dp))
        Button(onClick = onSend, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Blue, contentColor = Navy), shape = RoundedCornerShape(7.dp)) {
            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Generate plan", fontWeight = FontWeight.Bold)
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
