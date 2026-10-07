package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanPrimary

@Composable
fun SentenceBuilderBar(
    sentence: String,
    onSpeakSentence: () -> Unit,
    onAddSpace: () -> Unit,
    onBackspace: () -> Unit,
    onClearSentence: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("sentence_builder_bar"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TRANSCRIPCIÓN LSM EN VIVO",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                if (sentence.isNotEmpty()) {
                    Text(
                        text = "${sentence.length} caracteres",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Display sentence box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                if (sentence.isEmpty()) {
                    Text(
                        text = "Realiza señas frente a la cámara o selecciónalas para formar una frase...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                } else {
                    Text(
                        text = sentence,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Action buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onAddSpace,
                    modifier = Modifier.testTag("btn_space"),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.SpaceBar,
                        contentDescription = "Espacio",
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onBackspace,
                    enabled = sentence.isNotEmpty(),
                    modifier = Modifier.testTag("btn_backspace"),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Borrar último",
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onClearSentence,
                    enabled = sentence.isNotEmpty(),
                    modifier = Modifier.testTag("btn_clear"),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Limpiar texto",
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = {
                        if (sentence.isNotBlank()) {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("LSM Texto", sentence)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Texto copiado al portapapeles", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = sentence.isNotEmpty(),
                    modifier = Modifier.testTag("btn_copy"),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copiar",
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onSpeakSentence,
                    enabled = sentence.isNotEmpty(),
                    modifier = Modifier
                        .testTag("btn_speak_sentence")
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (sentence.isNotEmpty()) CyanPrimary else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Leer frase en voz alta",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
