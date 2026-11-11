# JAYRAJ DSLR AI — Android Studio starter project

## Included
- Offline-first Android photo editor UI
- Gallery photo picker
- Live rear-camera preview and still-photo capture using CameraX
- CameraX `CAPTURE_MODE_MAXIMIZE_QUALITY`
- Captures at the highest resolution selected by CameraX/device for the configured still-capture use case
- Brightness, contrast, saturation controls
- Snap-inspired colour presets: Snap Natural, Vivid, Warm, Cool
- CameraX still capture requests maximum quality and JPEG quality 100
- Save to Pictures/JAYRAJ_DSLR_AI

## Important resolution and AI notes
- The live camera capture uses the phone camera's supported still-capture output. It does **not** force native 8K. Actual native capture resolution depends on the phone camera sensor, Camera2 hardware level, supported stream sizes, and vendor camera implementation. CameraX may select a nearest supported size.
- An 8K export option requires an actual on-device super-resolution model and sufficient RAM/storage/compute. This starter project does **not** bundle or run Real-ESRGAN/NCNN yet; the 8K AI upscale and automatic DSLR AI are not implemented in this starter.
- Strict face preservation means avoid generative face restoration. A super-resolution model can still alter fine details; test identity preservation before release.
- App does not need internet for the included gallery/camera/manual-edit features. The AI model must be bundled locally before offline AI processing is available.

## Build
Open this folder in Android Studio (recent stable version), allow Gradle sync, then Build > Build APK(s).

CameraX references: https://developer.android.com/media/camera/camerax/configuration


## Snap-style colour look
The editor includes non-generative colour presets inspired by the clean, vivid look common in social camera apps. These adjust colour and contrast only; they do not alter facial geometry. The presets are applied after capture/import, not as a real-time live preview filter.
