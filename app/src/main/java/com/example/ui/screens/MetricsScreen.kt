package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.BenchmarkData
import com.example.model.ClassifierType
import com.example.model.LsmSign
import com.example.model.ModelBenchmark
import com.example.ui.LsmViewModel
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.TealSecondary

@Composable
fun MetricsScreen(
    viewModel: LsmViewModel,
    modifier: Modifier = Modifier
) {
    val currentPose by viewModel.currentPose.collectAsStateWithLifecycle()
    var selectedModelType by remember { mutableStateOf(ClassifierType.MLP) }
    val benchmarks = BenchmarkData.BENCHMARKS
    val activeBenchmark = BenchmarkData.getBenchmark(selectedModelType)

    // Benchmark comparison on current live hand
    var liveComparisonResults by remember {
        mutableStateOf<Map<ClassifierType, Float>?>(null)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "COMPARACIÓN DE MODELOS & MÉTRICAS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "KNN vs SVM vs Random Forest vs MLP",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Evaluación comparativa exhaustiva con scikit-learn y LiteRT/ONNX sobre 16 señas LSM y 63 features de MediaPipe Hands.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 2. Real-Time Latency Benchmark Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "BENCHMARK EN VIVO (CPU MÓVIL)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(20.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ejecuta inferencia simultánea de los 4 clasificadores sobre la mano actual para medir el tiempo real en milisegundos:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            currentPose?.let { pose ->
                                val results = mutableMapOf<ClassifierType, Float>()
                                for (type in ClassifierType.values()) {
                                    val clf = viewModel.modelManager.getClassifier(type)
                                    val res = clf.predict(pose)
                                    results[type] = res.inferenceTimeMs
                                }
                                liveComparisonResults = results
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_run_benchmark"),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ejecutar Test de Inferencia en Vivo", color = MaterialTheme.colorScheme.onPrimary)
                    }

                    liveComparisonResults?.let { map ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            map.forEach { (type, ms) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = type.displayName, style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        text = "%.2f ms".format(ms),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldSuccess
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Side-by-side Comparative Cards (Accuracy, F1, Latency, Size)
        item {
            Text(
                text = "TABLA COMPARATIVA GLOBAL",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        items(benchmarks) { bm ->
            val isCurrentActive = bm.type == viewModel.selectedClassifier.collectAsStateWithLifecycle().value
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrentActive) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                ),
                border = if (isCurrentActive) androidx.compose.foundation.BorderStroke(1.5.dp, CyanPrimary) else null,
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = bm.type.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Librería: ${bm.type.libraryFamily}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (isCurrentActive) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = CyanPrimary.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "ACTIVO",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyanPrimary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Metrics Bars: Accuracy
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Exactitud (Accuracy)", style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = "%.1f%%".format(bm.accuracy * 100),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = CyanPrimary
                        )
                    }
                    LinearProgressIndicator(
                        progress = { bm.accuracy },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = CyanPrimary,
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        strokeCap = StrokeCap.Round
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Metrics Badges: F1, Latency, Size
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "F1-Score: %.3f".format(bm.f1Score),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "Latencia: %.2f ms".format(bm.avgLatencyMs),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "Peso: ${bm.modelSizeBytesKb} KB",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        // 4. Model Deep Dive & Architecture Section
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DETALLE DE ARQUITECTURA & HIPERPARÁMETROS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(ClassifierType.values()) { type ->
                            val isSelected = type == selectedModelType
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedModelType = type },
                                label = { Text(type.shortName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanPrimary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = activeBenchmark.architectureDetails,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Ventajas:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldSuccess
                    )
                    Text(
                        text = activeBenchmark.strengths,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Limitaciones:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFA000)
                    )
                    Text(
                        text = activeBenchmark.limitations,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 5. Confusion Matrix Analysis Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "MATRIZ DE CONFUSIÓN (ANÁLISIS DE ERROR LSM)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Principales confusiones observadas en el dataset de validación cruzada (16 clases):",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val confusionPairs = listOf(
                        Triple("A ↔ E", "4.2% confusión", "Alta similitud en flexión de dedos; pulgar pegado vs doblado."),
                        Triple("U ↔ V", "3.1% confusión", "Separación angular entre índice y medio es el único discriminador."),
                        Triple("C ↔ O", "2.8% confusión", "Curvatura de dedos similar; difiere el contacto en yemas de dedos."),
                        Triple("D ↔ L", "1.9% confusión", "Orientación vertical de índice idéntica; difiere posición de pulgar.")
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        confusionPairs.forEach { (pair, rate, note) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = pair,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = note,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0x22FFA000)
                                ) {
                                    Text(
                                        text = rate,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFA000),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
