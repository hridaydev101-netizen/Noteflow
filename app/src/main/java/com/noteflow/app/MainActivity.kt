package com.noteflow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("noteflow", MODE_PRIVATE)
        setContent {
            var dark by remember { mutableStateOf(prefs.getBoolean("dark", false)) }
            NoteFlowTheme(dark) {
                NoteFlowApp(
                    dark = dark,
                    onDarkChange = {
                        dark = it
                        prefs.edit().putBoolean("dark", it).apply()
                    },
                    load = {
                        val raw = prefs.getString("notes", "[]") ?: "[]"
                        val arr = JSONArray(raw)
                        buildList {
                            for (i in 0 until arr.length()) {
                                val o = arr.getJSONObject(i)
                                add(Note(
                                    id = o.optString("id"),
                                    title = o.optString("title"),
                                    body = o.optString("body"),
                                    strokes = o.optJSONArray("strokes")?.let(::decodeStrokes) ?: emptyList(),
                                    paper = o.optString("paper", "Ruled")
                                ))
                            }
                        }
                    },
                    save = { notes ->
                        val arr = JSONArray()
                        notes.forEach { n ->
                            val o = JSONObject()
                            o.put("id", n.id); o.put("title", n.title)
                            o.put("body", n.body); o.put("paper", n.paper)
                            val strokes = JSONArray()
                            n.strokes.forEach { s ->
                                val so = JSONObject()
                                so.put("color", s.color); so.put("width", s.width)
                                val pts = JSONArray()
                                s.points.forEach { p ->
                                    val po = JSONObject()
                                    po.put("x", p.x); po.put("y", p.y); pts.put(po)
                                }
                                so.put("points", pts); strokes.put(so)
                            }
                            o.put("strokes", strokes); arr.put(o)
                        }
                        prefs.edit().putString("notes", arr.toString()).apply()
                    }
                )
            }
        }
    }
}

private fun decodeStrokes(a: JSONArray): List<InkStroke> = buildList {
    for (i in 0 until a.length()) {
        val s = a.getJSONObject(i)
        val pts = s.getJSONArray("points")
        val points = buildList {
            for (j in 0 until pts.length()) {
                val p = pts.getJSONObject(j)
                add(Offset(p.getDouble("x").toFloat(), p.getDouble("y").toFloat()))
            }
        }
        add(InkStroke(s.optLong("color"), s.optDouble("width", 4.0).toFloat(), points))
    }
}

data class InkStroke(val color: Long, val width: Float, val points: List<Offset>)
data class Note(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Untitled note",
    val body: String = "",
    val strokes: List<InkStroke> = emptyList(),
    val paper: String = "Ruled"
)

@Composable
fun NoteFlowTheme(dark: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (dark) darkColorScheme() else lightColorScheme(),
        content = content
    )
}

@Composable
fun NoteFlowApp(
    dark: Boolean,
    onDarkChange: (Boolean) -> Unit,
    load: () -> List<Note>,
    save: (List<Note>) -> Unit
) {
    var notes by remember { mutableStateOf(load().ifEmpty { listOf(Note()) }) }
    var selected by remember { mutableStateOf<String?>(null) }
    val current = notes.firstOrNull { it.id == selected }
    fun update(note: Note) {
        notes = notes.map { if (it.id == note.id) note else it }
        save(notes)
    }
    Surface(Modifier.fillMaxSize()) {
        if (current == null) {
            Column(Modifier.fillMaxSize().padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("NoteFlow", style = MaterialTheme.typography.headlineLarge)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { onDarkChange(!dark) }) {
                        Icon(if (dark) Icons.Default.LightMode else Icons.Default.DarkMode, "Toggle theme")
                    }
                    Button(onClick = {
                        val n = Note(title = "New note")
                        notes = listOf(n) + notes
                        save(notes)
                        selected = n.id
                    }) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(6.dp)); Text("New note") }
                }
                Text("Your notebooks", color = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.height(18.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(notes, key = { it.id }) { n ->
                        Card(
                            onClick = { selected = n.id },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Description, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(n.title, style = MaterialTheme.typography.titleMedium)
                                    Text(n.body.ifBlank { "Handwritten or typed note" }.take(90),
                                        color = MaterialTheme.colorScheme.secondary, maxLines = 1)
                                }
                                IconButton(onClick = {
                                    notes = notes.filterNot { it.id == n.id }
                                    if (notes.isEmpty()) notes = listOf(Note())
                                    save(notes)
                                }) { Icon(Icons.Default.DeleteOutline, "Delete note") }
                            }
                        }
                    }
                }
            }
        } else {
            Editor(
                note = current,
                onChange = ::update,
                onBack = { selected = null },
                dark = dark,
                onDarkChange = onDarkChange
            )
        }
    }
}

@Composable
fun Editor(
    note: Note,
    onChange: (Note) -> Unit,
    onBack: () -> Unit,
    dark: Boolean,
    onDarkChange: (Boolean) -> Unit
) {
    var tool by remember { mutableStateOf("Pen") }
    var inkColor by remember { mutableStateOf(Color(0xFF2457C5)) }
    var width by remember { mutableFloatStateOf(4f) }
    var livePoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var undo by remember(note.id) { mutableStateOf(listOf(note.strokes)) }
    var paper by remember(note.id) { mutableStateOf(note.paper) }

    Column(Modifier.fillMaxSize().padding(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
            BasicTextField(
                value = note.title,
                onValueChange = { onChange(note.copy(title = it)) },
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                decorationBox = { inner -> if (note.title.isEmpty()) Text("Note title"); inner() }
            )
            IconButton(onClick = { onDarkChange(!dark) }) {
                Icon(if (dark) Icons.Default.LightMode else Icons.Default.DarkMode, "Toggle theme")
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("Pen", "Highlighter", "Eraser").forEach { t ->
                FilterChip(selected = tool == t, onClick = { tool = t }, label = { Text(t) })
            }
            IconButton(onClick = {
                if (undo.size > 1) {
                    undo = undo.dropLast(1)
                    onChange(note.copy(strokes = undo.last()))
                }
            }) { Icon(Icons.Default.Undo, "Undo") }
            IconButton(onClick = {
                paper = when (paper) { "Ruled" -> "Grid"; "Grid" -> "Blank"; else -> "Ruled" }
                onChange(note.copy(paper = paper))
            }) { Icon(Icons.Default.GridOn, "Change paper") }
            Spacer(Modifier.weight(1f))
            Text(paper, style = MaterialTheme.typography.labelMedium)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            listOf(Color(0xFF2457C5), Color(0xFF202124), Color(0xFFD34242), Color(0xFF16834A), Color(0xFFFFA000))
                .forEach { c ->
                    Box(
                        Modifier.size(25.dp).background(c, CircleShape)
                            .border(if (inkColor == c) 3.dp else 0.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                    ) {
                        Surface(onClick = { inkColor = c }, modifier = Modifier.fillMaxSize(), color = Color.Transparent, shape = CircleShape) {}
                    }
                }
            Slider(value = width, onValueChange = { width = it }, valueRange = 1f..14f, modifier = Modifier.width(120.dp))
        }
        Box(
            Modifier.weight(1f).fillMaxWidth()
                .background(if (dark) Color(0xFF202124) else Color(0xFFFFFEFA), RoundedCornerShape(12.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
        ) {
            PaperLines(paper, dark)
            Canvas(
                Modifier.fillMaxSize().pointerInput(note.id, tool, inkColor, width, note.strokes) {
                    detectDragGestures(
                        onDragStart = { start ->
                            livePoints = listOf(start)
                        },
                        onDrag = { change, _ ->
                            livePoints = livePoints + change.position
                        },
                        onDragEnd = {
                            if (livePoints.isNotEmpty()) {
                                val c = when (tool) {
                                    "Eraser" -> if (dark) Color(0xFF202124) else Color(0xFFFFFEFA)
                                    "Highlighter" -> inkColor.copy(alpha = 0.28f)
                                    else -> inkColor
                                }
                                val stroke = InkStroke(c.value.toLong(), if (tool == "Highlighter") width * 3 else width, livePoints)
                                val updated = note.strokes + stroke
                                undo = undo + listOf(updated)
                                onChange(note.copy(strokes = updated))
                            }
                            livePoints = emptyList()
                        }
                    )
                }
            ) {
                (note.strokes + if (livePoints.isNotEmpty()) listOf(
                    InkStroke(inkColor.value.toLong(), width, livePoints)
                ) else emptyList()).forEach { s ->
                    if (s.points.isNotEmpty()) {
                        val path = Path().apply {
                            moveTo(s.points.first().x, s.points.first().y)
                            s.points.drop(1).forEach { lineTo(it.x, it.y) }
                        }
                        drawPath(
                            path,
                            Color(s.color.toULong()),
                            style = Stroke(width = s.width, cap = StrokeCap.Round)
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Text("Typed text", style = MaterialTheme.typography.labelLarge)
        OutlinedTextField(
            value = note.body,
            onValueChange = { onChange(note.copy(body = it)) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp, max = 130.dp),
            placeholder = { Text("Type your notes here…") }
        )
    }
}

@Composable
fun PaperLines(paper: String, dark: Boolean) {
    Canvas(Modifier.fillMaxSize()) {
        val line = if (dark) Color(0xFF42464B) else Color(0xFFE2E5EA)
        when (paper) {
            "Ruled" -> {
                var y = 34f
                while (y < size.height) {
                    drawLine(line, Offset(0f, y), Offset(size.width, y), 1f)
                    y += 34f
                }
            }
            "Grid" -> {
                var x = 0f
                while (x < size.width) {
                    drawLine(line, Offset(x, 0f), Offset(x, size.height), 1f); x += 28f
                }
                var y = 0f
                while (y < size.height) {
                    drawLine(line, Offset(0f, y), Offset(size.width, y), 1f); y += 28f
                }
            }
        }
    }
}
