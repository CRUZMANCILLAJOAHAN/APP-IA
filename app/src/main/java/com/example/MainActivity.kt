package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.LsmViewModel
import com.example.ui.screens.DatasetScreen
import com.example.ui.screens.DictionaryScreen
import com.example.ui.screens.MetricsScreen
import com.example.ui.screens.RecognitionScreen
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.MyApplicationTheme

enum class AppTab(val title: String, val testTag: String) {
    RECOGNITION("Traductor", "tab_recognition"),
    DICTIONARY("Vocabulario", "tab_dictionary"),
    DATASET("Dataset", "tab_dataset"),
    METRICS("Métricas", "tab_metrics")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                LsmAppRoot()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LsmAppRoot(
    viewModel: LsmViewModel = viewModel()
) {
    var currentTab by remember { mutableStateOf(AppTab.RECOGNITION) }
    val selectedClassifier by viewModel.selectedClassifier.collectAsStateWithLifecycle()
    val isTtsReady by viewModel.ttsManager.isReady.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "LSM Reconocedor",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CyanPrimary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = selectedClassifier.shortName,
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanPrimary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                actions = {
                    // TTS status indicator
                    Surface(
                        shape = CircleShape,
                        color = if (isTtsReady) EmeraldSuccess.copy(alpha = 0.15f) else Color(0x22FFA000),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "TTS Estado",
                                tint = if (isTtsReady) EmeraldSuccess else Color(0xFFFFA000),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isTtsReady) "Voz es-MX" else "Iniciando",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isTtsReady) EmeraldSuccess else Color(0xFFFFA000)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                windowInsets = WindowInsets.navigationBars,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = currentTab == AppTab.RECOGNITION,
                    onClick = { currentTab = AppTab.RECOGNITION },
                    icon = { Icon(Icons.Default.Translate, contentDescription = "Traductor en tiempo real") },
                    label = { Text("Traductor") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = CyanPrimary.copy(alpha = 0.2f),
                        selectedIconColor = CyanPrimary,
                        selectedTextColor = CyanPrimary
                    ),
                    modifier = Modifier.testTag(AppTab.RECOGNITION.testTag)
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.DICTIONARY,
                    onClick = { currentTab = AppTab.DICTIONARY },
                    icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Vocabulario LSM") },
                    label = { Text("Vocabulario") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = CyanPrimary.copy(alpha = 0.2f),
                        selectedIconColor = CyanPrimary,
                        selectedTextColor = CyanPrimary
                    ),
                    modifier = Modifier.testTag(AppTab.DICTIONARY.testTag)
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.DATASET,
                    onClick = { currentTab = AppTab.DATASET },
                    icon = { Icon(Icons.Default.Storage, contentDescription = "Dataset") },
                    label = { Text("Dataset") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = CyanPrimary.copy(alpha = 0.2f),
                        selectedIconColor = CyanPrimary,
                        selectedTextColor = CyanPrimary
                    ),
                    modifier = Modifier.testTag(AppTab.DATASET.testTag)
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.METRICS,
                    onClick = { currentTab = AppTab.METRICS },
                    icon = { Icon(Icons.Default.Assessment, contentDescription = "Métricas") },
                    label = { Text("Métricas") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = CyanPrimary.copy(alpha = 0.2f),
                        selectedIconColor = CyanPrimary,
                        selectedTextColor = CyanPrimary
                    ),
                    modifier = Modifier.testTag(AppTab.METRICS.testTag)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentTab, label = "tab_crossfade") { tab ->
                when (tab) {
                    AppTab.RECOGNITION -> RecognitionScreen(viewModel = viewModel)
                    AppTab.DICTIONARY -> DictionaryScreen(
                        viewModel = viewModel,
                        onNavigateToRecognition = { currentTab = AppTab.RECOGNITION }
                    )
                    AppTab.DATASET -> DatasetScreen(viewModel = viewModel)
                    AppTab.METRICS -> MetricsScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme { Greeting("Android") }
}
