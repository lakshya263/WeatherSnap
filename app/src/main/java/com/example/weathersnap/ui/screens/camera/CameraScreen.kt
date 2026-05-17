package com.example.weathersnap.ui.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.weathersnap.ui.navigation.Routes
import com.example.weathersnap.ui.screens.createreport.CreateReportViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Composable
fun CameraScreen(
    navController: NavHostController
) {
    // ── Correct fix for "getBackStackEntry during composition" ────────────────
    //
    // The Navigation Compose warning says the key for remember() MUST be a
    // NavBackStackEntry — not NavController, not a string, not Unit.
    //
    // currentBackStackEntryAsState() is the composition-safe way to observe
    // the back stack. We use the State value as the remember key so that if
    // the back stack changes, the remembered entry is also updated.
    //
    // While CameraScreen is visible, the back stack is:
    //   [WEATHER] → [CREATE_REPORT] → [CAMERA]  ← current
    //
    // getBackStackEntry(Routes.CREATE_REPORT) safely reaches one level back.
    val currentEntry by navController.currentBackStackEntryAsState()

    val createReportEntry = remember(currentEntry) {
        navController.getBackStackEntry(Routes.CREATE_REPORT)
    }

    val createReportViewModel: CreateReportViewModel = hiltViewModel(
        viewModelStoreOwner = createReportEntry
    )

    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    val previewView  = remember { PreviewView(context) }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    var isCapturing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        bindCamera(
            context        = context,
            lifecycleOwner = lifecycleOwner,
            previewView    = previewView,
            imageCapture   = imageCapture
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // Live camera preview — fills entire screen
        AndroidView(
            factory  = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Top gradient — keeps title readable over any scene
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .align(Alignment.TopStart)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.60f), Color.Transparent)
                    )
                )
        )

        // Bottom gradient — keeps Capture button readable
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .align(Alignment.BottomStart)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.70f))
                    )
                )
        )

        // "Custom Camera" title + Close button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Custom Camera",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            OutlinedButton(
                onClick = { navController.popBackStack() },
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.30f),
                    contentColor   = Color.White
                ),
                border = ButtonDefaults.outlinedButtonBorder
            ) {
                Text(
                    text = "Close",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White
                )
            }
        }

        // Capture button at bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 22.dp)
        ) {
            Button(
                onClick = {
                    if (!isCapturing) {
                        isCapturing = true
                        coroutineScope.launch {
                            try {
                                val (path, origKb, compKb) = captureAndCompress(
                                    context      = context,
                                    imageCapture = imageCapture
                                )
                                // Writes to the SAME ViewModel instance as CreateReportScreen
                                createReportViewModel.onPhotoCaptured(
                                    imagePath    = path,
                                    originalKb   = origKb,
                                    compressedKb = compKb
                                )
                                navController.popBackStack()
                            } catch (e: Exception) {
                                e.printStackTrace()
                                isCapturing = false
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isCapturing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor   = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            ) {
                if (isCapturing) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(18.dp),
                        color       = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Capturing...", fontWeight = FontWeight.SemiBold)
                } else {
                    Text("Capture", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
            }
        }
    }
}

// ── Camera binding ────────────────────────────────────────────────────────────

private suspend fun bindCamera(
    context: Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    previewView: PreviewView,
    imageCapture: ImageCapture
) {
    val cameraProvider = withContext(Dispatchers.IO) {
        suspendCoroutine { cont ->
            val future = ProcessCameraProvider.getInstance(context)
            future.addListener(
                {
                    try   { cont.resume(future.get()) }
                    catch (e: Exception) { cont.resumeWithException(e) }
                },
                ContextCompat.getMainExecutor(context)
            )
        }
    }

    val preview = Preview.Builder().build().also {
        it.surfaceProvider = previewView.surfaceProvider
    }

    try {
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            imageCapture
        )
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// ── Capture + Compress ────────────────────────────────────────────────────────

private suspend fun captureAndCompress(
    context: Context,
    imageCapture: ImageCapture
): Triple<String, Long, Long> {

    val rawFile = File(context.filesDir, "raw_${System.currentTimeMillis()}.jpg")

    withContext(Dispatchers.IO) {
        suspendCoroutine { cont ->
            val options = ImageCapture.OutputFileOptions.Builder(rawFile).build()
            imageCapture.takePicture(
                options,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        cont.resume(Unit)
                    }
                    override fun onError(e: ImageCaptureException) {
                        cont.resumeWithException(e)
                    }
                }
            )
        }
    }

    // Capture original size BEFORE deleting the raw file
    val originalKb     = rawFile.length() / 1024
    val compressedFile = File(context.filesDir, "photo_${System.currentTimeMillis()}.jpg")

    withContext(Dispatchers.Default) {
        val bitmap = BitmapFactory.decodeFile(rawFile.absolutePath)
        FileOutputStream(compressedFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, out)
            out.flush()
        }
        bitmap.recycle()
    }

    rawFile.delete()

    val compressedKb = compressedFile.length() / 1024
    return Triple(compressedFile.absolutePath, originalKb, compressedKb)
}