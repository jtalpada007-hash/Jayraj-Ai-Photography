package com.jayraj.dslrai

import android.content.ContentValues
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.Button
import android.graphics.Color
import android.view.ViewGroup

class CameraCaptureActivity : ComponentActivity() {
    private var imageCapture: ImageCapture? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = FrameLayout(this).apply { setBackgroundColor(Color.BLACK) }
        val previewView = PreviewView(this).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
        root.addView(previewView, FrameLayout.LayoutParams(-1, -1))
        val capture = Button(this).apply { text = "●  CAPTURE PHOTO"; textSize = 18f; setTextColor(Color.WHITE); setBackgroundColor(Color.rgb(184,121,46)) }
        val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM).apply { setMargins(32, 24, 32, 48) }
        root.addView(capture, params)
        setContentView(root)
        val providerFuture = ProcessCameraProvider.getInstance(this)
        providerFuture.addListener({
            val provider = providerFuture.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .setJpegQuality(100)
                .build()
            try {
                provider.unbindAll()
                provider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
            } catch (e: Exception) { Toast.makeText(this, "Camera start failed: ${e.message}", Toast.LENGTH_LONG).show(); finish() }
        }, ContextCompat.getMainExecutor(this))
        capture.setOnClickListener { takePhoto() }
    }

    private fun takePhoto() {
        val capture = imageCapture ?: return
        val name = "JAYRAJ_CAMERA_${System.currentTimeMillis()}.jpg"
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/JAYRAJ_DSLR_AI")
        }
        val options = ImageCapture.OutputFileOptions.Builder(contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values).build()
        capture.takePicture(options, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val uri = output.savedUri
                setResult(RESULT_OK, intent.putExtra("photo_uri", uri?.toString()))
                finish()
            }
            override fun onError(exception: ImageCaptureException) {
                Toast.makeText(this@CameraCaptureActivity, "Photo capture failed: ${exception.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
