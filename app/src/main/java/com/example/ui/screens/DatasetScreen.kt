package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.LsmSign
import com.example.ui.LsmViewModel
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DatasetScreen(
    viewModel: LsmViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val samples by viewModel.datasetSamples.collectAsStateWithLifecycle()
    val totalCount by viewModel.totalSampleCount.collectAsStateWithLifecycle()

    var selectedSignForCapture by remember { mutableStateOf("A") }
    var showExportDialog by remember { mutableStateOf(false) }

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
                        text = "DATASET PROPIO LSM",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Captura de Landmarks & Gestión de Muestras",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Captura coordenadas 3D de las 21 articulaciones MediaPipe (63 features: dx, dy, dz normalizados) para alimentar los modelos KNN, SVM, Random Forest y MLP.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 2. Dataset Metrics Summary Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$totalCount",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = CyanPrimary
                        )
                        Text(
                            text = "Muestras Totales",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .width(1.dp)
                            .background(MaterialTheme.colorScheme.outline)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "63",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = EmeraldSuccess
                        )
                        Text(
                            text = "Features / Muestra",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .width(1.dp)
                            .background(MaterialTheme.colorScheme.outline)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "80 / 20",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Train / Test Split",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 3. Capture Landmark Studio Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "CAPTURAR NUEVA MUESTRA",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Selecciona la seña a registrar:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(LsmSign.VOCABULARY) { sign ->
                            val isSelected = sign.id == selectedSignForCapture
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedSignForCapture = sign.id
                                    viewModel.setSimulatorSign(sign.id)
                                },
                                label = { Text("${sign.displayName} ${sign.iconEmoji}") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanPrimary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.recordSampleForSign(selectedSignForCapture)
                                Toast.makeText(context, "Muestra guardada para seña '$selectedSignForCapture'", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_capture_sample"),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                        ) {
                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Capturar (+1)", color = MaterialTheme.colorScheme.onPrimary)
                        }

                        Button(
                            onClick = {
                                repeat(5) {
                                    viewModel.recordSampleForSign(selectedSignForCapture)
                                }
                                Toast.makeText(context, "5 muestras guardadas para '$selectedSignForCapture'", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_capture_batch"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(imageVector = Icons.Default.Science, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ráfaga (+5)", color = MaterialTheme.colorScheme.onSecondary)
                        }
                    }
                }
            }
        }

        // 4. Dataset Actions (Export, Clear)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { showExportDialog = true },
                    modifier = Modifier.testTag("btn_export_dataset")
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Exportar Dataset (CSV/JSON)")
                }

                if (samples.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            viewModel.clearDataset()
                            Toast.makeText(context, "Dataset limpiado", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("btn_clear_dataset")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Limpiar dataset",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        // 5. Samples List Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MUESTRAS REGISTRADAS (${samples.size})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        // 6. Samples List Items
        if (samples.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No hay muestras registradas. Captura landmarks arriba para comenzar a construir el dataset.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(samples.take(40)) { sample ->
                val dateStr = SimpleDateFormat("HH:mm:ss dd/MM", Locale.getDefault()).format(Date(sample.timestamp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(CyanPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = sample.signId.take(2),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanPrimary
                                )
                            }

                            Column {
                                Text(
                                    text = "Seña: ${sample.signName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "$dateStr • ${sample.source}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.deleteDatasetSample(sample.id) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar muestra",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Export Dataset Dialog
    if (showExportDialog) {
        val sampleFeaturesPreview = samples.take(5).joinToString("\n") { s ->
            "${s.signId},[${s.featuresCsv.take(45)}...]"
        }

        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Dataset Propio LSM (scikit-learn / LiteRT)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Formato matricial X (63 features normalizados de MediaPipe) e y (etiquetas de clase). Total: ${samples.size} registros.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = if (samples.isEmpty()) "Dataset vacío." else sampleFeaturesPreview,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val fullCsv = "label,features\n" + samples.joinToString("\n") { "${it.signId},\"${it.featuresCsv}\"" }
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("LSM Dataset CSV", fullCsv))
                        Toast.makeText(context, "Dataset CSV copiado al portapapeles", Toast.LENGTH_SHORT).show()
                        showExportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copiar CSV", color = MaterialTheme.colorScheme.onPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}
