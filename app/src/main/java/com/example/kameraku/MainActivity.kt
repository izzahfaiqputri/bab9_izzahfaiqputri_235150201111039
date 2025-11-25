package com.example.kameraku

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Surface
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
// --- Import CameraX ---
import androidx.camera.core.*
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
// ----------------------
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lens
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume



@Composable
fun CameraPreview(onPreviewReady: (PreviewView) -> Unit) {
    AndroidView(
        factory = { context ->
            PreviewView(context).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                post { onPreviewReady(this) }
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@SuppressLint("RestrictedApi")
suspend fun bindPreview(
    context: Context,
    owner: LifecycleOwner,
    view: PreviewView,
    cameraSelector: CameraSelector,
    imageCapture: ImageCapture
): Pair<Preview, androidx.camera.core.Camera> {
    val provider = suspendCancellableCoroutine<ProcessCameraProvider> { cont ->
        val f = ProcessCameraProvider.getInstance(context)
        f.addListener({ cont.resume(f.get()) }, ContextCompat.getMainExecutor(context))
    }

    val targetRotation = view.display?.rotation ?: Surface.ROTATION_0
    imageCapture.targetRotation = targetRotation


    val resolutionSelector = ResolutionSelector.Builder()
        .setAspectRatioStrategy(AspectRatioStrategy(AspectRatio.RATIO_4_3, AspectRatioStrategy.FALLBACK_RULE_AUTO))
        .build()

    val preview = Preview.Builder()
        .setTargetRotation(targetRotation)
        .setResolutionSelector(resolutionSelector)
        .build().also {
            it.setSurfaceProvider(view.surfaceProvider)
        }

    provider.unbindAll()
    val camera = provider.bindToLifecycle(owner, cameraSelector, preview, imageCapture)

    return preview to camera
}

fun outputOptions(ctx: Context, name: String): ImageCapture.OutputFileOptions {
    val v = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/KameraKu")
    }
    val r = ctx.contentResolver
    val uri = r.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, v)
        ?: throw IllegalStateException("Gagal membuat URI MediaStore")
    return ImageCapture.OutputFileOptions.Builder(r, uri, v).build()
}

fun takePhoto(ctx: Context, ic: ImageCapture, onSaved: (Uri) -> Unit) {
    val opt = outputOptions(ctx, "IMG_" + System.currentTimeMillis())
    ic.takePicture(
        opt,
        ContextCompat.getMainExecutor(ctx),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(res: ImageCapture.OutputFileResults) {
                onSaved(res.savedUri!!)
            }

            override fun onError(e: ImageCaptureException) {
                e.printStackTrace()
            }
        })
}


@Composable
fun KameraKuApp() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var hasCameraPermission by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.CAMERA)
    }

    var previewView: PreviewView? by remember { mutableStateOf(null) }

    val imageCapture = remember {
        val resolutionSelector = ResolutionSelector.Builder()
            .setAspectRatioStrategy(AspectRatioStrategy(AspectRatio.RATIO_4_3, AspectRatioStrategy.FALLBACK_RULE_AUTO))
            .build()
            
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setResolutionSelector(resolutionSelector)
            .build()
    }
    
    var camera: androidx.camera.core.Camera? by remember { mutableStateOf(null) }
    var latestImageUri: Uri? by remember { mutableStateOf(null) }
    var cameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }
    var isTorchEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(cameraSelector, previewView) {
        if (hasCameraPermission && previewView != null) {
            try {
                val (_, currentCamera) = bindPreview(
                    context,
                    lifecycleOwner,
                    previewView!!,
                    cameraSelector,
                    imageCapture
                )
                camera = currentCamera
                currentCamera.cameraControl.enableTorch(isTorchEnabled)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(isTorchEnabled) {
        camera?.cameraControl?.enableTorch(isTorchEnabled)
        imageCapture.flashMode = if (isTorchEnabled) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
    }


    if (!hasCameraPermission) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "Izin Kamera Diperlukan.",
                style = MaterialTheme.typography.headlineSmall
            )
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview { view ->
            previewView = view
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Card(
                    modifier = Modifier.size(64.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f))
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (latestImageUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(model = latestImageUri),
                                contentDescription = "Last taken photo thumbnail",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text("Foto", color = Color.White)
                        }
                    }
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            takePhoto(context, imageCapture) { uri ->
                                latestImageUri = uri
                            }
                        }
                    },
                    modifier = Modifier.size(80.dp),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Icon(
                        Icons.Filled.Lens,
                        contentDescription = "Take Photo",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Button(
                    onClick = {
                        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        } else {
                            CameraSelector.DEFAULT_BACK_CAMERA
                        }
                        isTorchEnabled = false
                    },
                    modifier = Modifier.size(64.dp),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(
                        Icons.Filled.Cameraswitch,
                        contentDescription = "Switch Camera",
                        tint = Color.White
                    )
                }
            }

            if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                Button(
                    onClick = { isTorchEnabled = !isTorchEnabled },
                    modifier = Modifier.padding(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(
                        if (isTorchEnabled) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                        contentDescription = "Toggle Flash",
                        tint = Color.White
                    )
                }
            }

        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                KameraKuApp()
            }
        }
    }
}
