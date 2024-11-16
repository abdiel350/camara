package es.ua.eps.camara
import android.Manifest
import android.content.pm.PackageManager
import android.hardware.camera2.*
import android.os.Bundle
import android.util.SparseIntArray
import android.view.Surface
import android.view.TextureView
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {

    private lateinit var cameraManager: CameraManager
    private lateinit var cameraDevice: CameraDevice
    private lateinit var textureView: TextureView
    private var cameraFacing = CameraCharacteristics.LENS_FACING_FRONT

    private val ORIENTATIONS = SparseIntArray()

    init {
        ORIENTATIONS.append(Surface.ROTATION_0, 90)
        ORIENTATIONS.append(Surface.ROTATION_90, 0)
        ORIENTATIONS.append(Surface.ROTATION_180, 270)
        ORIENTATIONS.append(Surface.ROTATION_270, 180)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Obteniendo la referencia
        textureView = findViewById(R.id.textureView)

        // Obteniendo el servicio
        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager

        // Solicitar permiso para acceder a la cámara
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1)

        // cámara frontal
        findViewById<View>(R.id.btnCameraFront).setOnClickListener {
            cameraFacing = CameraCharacteristics.LENS_FACING_FRONT
            closeCamera()
            openCamera()
        }
        //cámara trasera
        findViewById<View>(R.id.btnCameraRear).setOnClickListener {
            cameraFacing = CameraCharacteristics.LENS_FACING_BACK
            closeCamera()
            openCamera()
        }
    }

    private fun openCamera() {
        try {
            val cameraId = getCameraId(cameraFacing)
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return
            }

            // Abrir la cámara
            cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    // Iniciar la vista previa de la cámara
                    onCreateCameraPreviewSession()
                }

                override fun onDisconnected(camera: CameraDevice) {
                    cameraDevice.close()
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    cameraDevice.close()
                }
            }, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun getCameraId(facing: Int): String {
        val cameraIds = cameraManager.cameraIdList
        for (cameraId in cameraIds) {
            val cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId)
            if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == facing) {
                return cameraId
            }
        }
        return cameraIds[0]
    }

    private fun closeCamera() {
        cameraDevice.close()
    }

    private fun onCreateCameraPreviewSession() {
        // Configurar el tamaño del TextureView
        textureView.surfaceTexture?.setDefaultBufferSize(1920, 1080)
        val surface = Surface(textureView.surfaceTexture)

        try {
            // Crear un objeto CaptureRequest.Builder para configurar la vista previa de la cámara
            val captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder.addTarget(surface)

            // Crear un objeto CameraCaptureSession para la vista previa de la cámara
            cameraDevice.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    session.setRepeatingRequest(captureRequestBuilder.build(), null, null)
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    // Manejar el error si la creación de la sesión falla
                }
            }, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        }
    }
}