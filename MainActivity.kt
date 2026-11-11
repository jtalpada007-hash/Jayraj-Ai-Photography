package com.jayraj.dslrai

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Canvas
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { StudioScreen() } }
}

@Composable
private fun StudioScreen() {
    val context = LocalContext.current
    var original by remember { mutableStateOf<Bitmap?>(null) }
    var brightness by remember { mutableFloatStateOf(0f) }
    var contrast by remember { mutableFloatStateOf(1f) }
    var saturation by remember { mutableFloatStateOf(1f) }
    var tone by remember { mutableStateOf("Snap Natural") }
    var result by remember { mutableStateOf<Bitmap?>(null) }
    var status by remember { mutableStateOf("ફોટો પસંદ કરો અથવા Live Camera ખોલો") }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        val uriText = res.data?.getStringExtra("photo_uri")
        if (res.resultCode == android.app.Activity.RESULT_OK && uriText != null) {
            context.contentResolver.openInputStream(android.net.Uri.parse(uriText))?.use { original = BitmapFactory.decodeStream(it) }
            result = null; status = "Live camera photo loaded — device-supported maximum resolution"
        }
    }
    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) cameraLauncher.launch(Intent(context, CameraCaptureActivity::class.java)) else status = "Live Camera માટે Camera permission જરૂરી છે"
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                original = BitmapFactory.decodeStream(stream)
                result = null
                status = "ફોટો લોડ થયો — AI મોડલ જોડ્યા પછી DSLR processing સક્રિય થશે"
            }
        }
    }

    MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFFD49A48), background = Color(0xFF10151D), surface = Color(0xFF1B2430))) {
        Column(Modifier.fillMaxSize().background(Color(0xFF10151D)).verticalScroll(rememberScrollState()).padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("JAYRAJ DSLR AI", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = Color.White)
            Text("OFFLINE PHOTO STUDIO", color = Color(0xFFD49A48), style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(18.dp))
            Box(Modifier.fillMaxWidth().height(330.dp).background(Color(0xFF202B39), RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
                val shown = result ?: original
                if (shown != null) Image(shown.asImageBitmap(), null, Modifier.fillMaxSize())
                else Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.AddPhotoAlternate, null, tint = Color.LightGray, modifier = Modifier.size(54.dp))
                    Text("તમારો ફોટો અહીં દેખાશે", color = Color.LightGray)
                }
            }
            Spacer(Modifier.height(14.dp))
            Button(onClick = { picker.launch("image/*") }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.AddPhotoAlternate, null); Spacer(Modifier.width(8.dp)); Text("UPLOAD PHOTO") }
            OutlinedButton(onClick = {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) cameraLauncher.launch(Intent(context, CameraCaptureActivity::class.java))
                else cameraPermission.launch(Manifest.permission.CAMERA)
            }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.CameraAlt, null); Spacer(Modifier.width(8.dp)); Text("LIVE CAMERA — TAKE PHOTO") }
            Button(onClick = {
                val src = original
                if (src == null) status = "પહેલાં ફોટો પસંદ કરો"
                else status = "AI મોડલ હજી જોડાયેલું નથી. Real-ESRGAN NCNN offline engine અને model files ઉમેર્યા પછી આ બટન AI DSLR processing કરશે."
            }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB8792E))) {
                Icon(Icons.Default.AutoFixHigh, null); Spacer(Modifier.width(8.dp)); Text("DSLR MODE — AUTO AI")
            }
            Text(status, color = Color(0xFFFFD59A), style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(12.dp))
            Text("SNAP-STYLE COLOUR TONES", color = Color.White, fontWeight = FontWeight.Bold)
            Text("ચહેરો બદલ્યા વગર natural colour look પસંદ કરો", color = Color(0xFFB7C4D4), style = MaterialTheme.typography.bodySmall)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Snap Natural", "Vivid", "Warm", "Cool").forEach { preset ->
                    FilterChip(
                        selected = tone == preset,
                        onClick = { tone = preset; result = original?.let { b -> adjustBitmap(b, brightness, contrast, saturation, tone) } },
                        label = { Text(preset, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
            Text("MANUAL ADJUSTMENTS", color = Color.White, fontWeight = FontWeight.Bold)
            AdjustSlider("Brightness", brightness, -1f, 1f) { brightness = it; result = original?.let { b -> adjustBitmap(b, brightness, contrast, saturation, tone) } }
            AdjustSlider("Contrast", contrast, 0f, 2f) { contrast = it; result = original?.let { b -> adjustBitmap(b, brightness, contrast, saturation, tone) } }
            AdjustSlider("Colour / Saturation", saturation, 0f, 2f) { saturation = it; result = original?.let { b -> adjustBitmap(b, brightness, contrast, saturation, tone) } }
            OutlinedButton(onClick = {
                val bmp = result ?: original
                if (bmp == null) Toast.makeText(context, "પહેલાં ફોટો પસંદ કરો", Toast.LENGTH_SHORT).show()
                else {
                    val values = ContentValues().apply { put(MediaStore.Images.Media.DISPLAY_NAME, "JAYRAJ_DSLR_${System.currentTimeMillis()}.jpg"); put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg"); put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/JAYRAJ_DSLR_AI") }
                    val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    if (uri != null) context.contentResolver.openOutputStream(uri)?.use { bmp.compress(Bitmap.CompressFormat.JPEG, 98, it) }
                    Toast.makeText(context, "ફોટો Gallery માં સેવ થયો", Toast.LENGTH_LONG).show()
                }
            }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.SaveAlt, null); Spacer(Modifier.width(8.dp)); Text("SAVE PHOTO") }
            Text("DEVICE MAX CAMERA CAPTURE • 8X UPSCALE MODEL REQUIRED • STRICT FACE PRESERVATION", color = Color(0xFFB7C4D4), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun AdjustSlider(label: String, value: Float, min: Float, max: Float, onChange: (Float) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(label, color = Color.White); Text("${(value * 100).toInt()}%", color = Color(0xFFD49A48)) }
        Slider(value = value, onValueChange = onChange, valueRange = min..max)
    }
}

private fun adjustBitmap(src: Bitmap, brightness: Float, contrast: Float, saturation: Float, tone: String): Bitmap {
    val out = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
    val c = Canvas(out)
    val presetSaturation = when (tone) { "Vivid" -> 1.18f; "Warm" -> 1.08f; "Cool" -> 1.06f; else -> 1.04f }
    val presetContrast = when (tone) { "Vivid" -> 1.06f; else -> 1.02f }
    val warmRed = if (tone == "Warm") 5f else 0f
    val coolBlue = if (tone == "Cool") 5f else 0f
    val cm = ColorMatrix().apply { setSaturation(saturation * presetSaturation) }
    val scale = contrast * presetContrast
    val translate = 0.5f * (1f - scale) * 255f + brightness * 100f
    val contrastMatrix = ColorMatrix(floatArrayOf(scale,0f,0f,0f,translate + warmRed, 0f,scale,0f,0f,translate, 0f,0f,scale,0f,translate + coolBlue, 0f,0f,0f,1f,0f))
    cm.postConcat(contrastMatrix)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { colorFilter = ColorMatrixColorFilter(cm) }
    c.drawBitmap(src, 0f, 0f, paint)
    return out
}
