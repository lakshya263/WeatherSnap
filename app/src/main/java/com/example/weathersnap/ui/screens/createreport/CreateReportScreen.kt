package com.example.weathersnap.ui.createreport

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil3.compose.AsyncImage
import com.example.weathersnap.ui.navigation.Routes
import com.example.weathersnap.ui.navigation.SharedViewModel
import com.example.weathersnap.ui.screens.createreport.CreateReportViewModel
import com.example.weathersnap.ui.screens.createreport.SaveReportState
import com.example.weathersnap.ui.weather.WeatherResultCard

@Composable
fun CreateReportScreen(
    navController: NavHostController,
    viewModel: CreateReportViewModel = hiltViewModel()
) {
    // ── Key fix: scope SharedViewModel to WEATHER back stack entry ────────────
    //
    // hiltViewModel() with no arguments creates a ViewModel scoped to the
    // CURRENT back stack entry (CREATE_REPORT). That's a different instance
    // from the one WeatherScreen wrote to — so weatherData would always be null,
    // hiding the weather card AND the Save button.
    //
    // By scoping to the WEATHER entry, both screens share the exact same
    // SharedViewModel instance, so selectedWeather is the value WeatherScreen set.
    val currentEntry by navController.currentBackStackEntryAsState()
    val weatherEntry = remember(currentEntry) {
        navController.getBackStackEntry(Routes.WEATHER)
    }
    val sharedViewModel: SharedViewModel = hiltViewModel(
        viewModelStoreOwner = weatherEntry
    )

    val weatherData by sharedViewModel.selectedWeather.collectAsStateWithLifecycle()
    val notes       by viewModel.notesFlow.collectAsStateWithLifecycle()
    val imagePath   by viewModel.imagePathFlow.collectAsStateWithLifecycle()
    val imageSizes  by viewModel.imageSizes.collectAsStateWithLifecycle()
    val saveState   by viewModel.saveState.collectAsStateWithLifecycle()

    // Navigate to Saved Reports after successful save
    LaunchedEffect(saveState) {
        if (saveState is SaveReportState.Success) {
            navController.navigate(Routes.SAVED_REPORTS) {
                popUpTo(Routes.WEATHER)
            }
        }
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // ── Top bar ───────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Create Report",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Capture, compress, annotate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Back",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // ── Weather snapshot card ─────────────────────────────────────
            // weatherData is now guaranteed non-null because SharedViewModel
            // is the same instance WeatherScreen wrote to
            weatherData?.let { weather ->
                WeatherResultCard(data = weather)
            }

            // ── Photo preview card ────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {

                    // Animated image preview after capture
                    AnimatedVisibility(
                        visible = imagePath != null,
                        enter = fadeIn(tween(300)) +
                                scaleIn(initialScale = 0.88f, animationSpec = tween(300))
                    ) {
                        imagePath?.let { path ->
                            Column {
                                AsyncImage(
                                    model = path,
                                    contentDescription = "Captured photo",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(210.dp)
                                        .clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    SizeChip(
                                        label = "Original",
                                        value = formatSize(imageSizes.first),
                                        modifier = Modifier.weight(1f)
                                    )
                                    SizeChip(
                                        label = "Compressed",
                                        value = formatSize(imageSizes.second),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                    }

                    // Placeholder before any photo is taken
                    if (imagePath == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(170.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.6f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Photo preview",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Capture button — just navigates, CameraScreen owns permission
                    Button(
                        onClick = { navController.navigate(Routes.CAMERA) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor   = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            text = if (imagePath == null) "Capture Photo" else "Retake Photo",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // ── Field Notes card ──────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Field Notes",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = viewModel::onNotesChanged,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        placeholder = {
                            Text(
                                text = "Notes",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor   = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                            focusedBorderColor      = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor    = MaterialTheme.colorScheme.background,
                            focusedTextColor        = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor      = MaterialTheme.colorScheme.onSurface,
                            cursorColor             = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            // ── Save Report button ────────────────────────────────────────
            // Now always visible because weatherData is always non-null
            weatherData?.let { weather ->
                Column {
                    Button(
                        onClick = { viewModel.saveReport(weather) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = saveState !is SaveReportState.Saving,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor   = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                    ) {
                        if (saveState is SaveReportState.Saving) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(18.dp),
                                color       = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Saving...", fontWeight = FontWeight.SemiBold)
                        } else {
                            Text(
                                text = "Save Report",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    if (saveState is SaveReportState.Error) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = (saveState as SaveReportState.Error).message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

@Composable
private fun SizeChip(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f))
            .padding(horizontal = 10.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

private fun formatSize(kb: Long): String = when {
    kb >= 1024 -> "${"%.1f".format(kb / 1024.0)} MB"
    else       -> "$kb KB"
}