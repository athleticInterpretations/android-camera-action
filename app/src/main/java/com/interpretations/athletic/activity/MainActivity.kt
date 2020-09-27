package com.interpretations.athletic.activity

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.camera2.*
import android.media.ImageReader
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.interpretations.athletic.R
import com.interpretations.athletic.databinding.ActivityMainBinding
import com.interpretations.athletic.framework.base.BaseActivity
import com.interpretations.athletic.framework.utils.FrameworkUtils
import com.interpretations.athletic.network.NetworkReceiver
import com.interpretations.athletic.utils.CompareSizesByArea
import com.interpretations.athletic.utils.DialogUtils
import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import kotlin.math.max

// exceptions
private const val EXCEPTION_CAMERA2_NOT_SUPPORTED_DEVICE =
    "Camera2 API not supported on this device."
private const val EXCEPTION_TIMEOUT_LOCK_CAMERA_OPENING =
    "Time out waiting to lock camera opening."
private const val EXCEPTION_INTERRUPTED_LOCK_CAMERA_OPENING =
    "Interrupted while trying to lock camera opening."
private const val EXCEPTION_INTERRUPTED_LOCK_CAMER_CLOSING =
    "Interrupted while trying to lock camera closing."
private const val EXCEPTION_CAMERA2_NO_PERMISSION =
    "No permissions granted to use Camera."
private const val EXCEPTION_CAMERA2_CONFIGURATION_FAILED =
    "Camera configuration failed."

private const val TAG = "MainActivity"
private const val CAMERA_THREAD = "CameraThread"

// preview maximum dimensions
private const val MAX_PREVIEW_WIDTH = 1920
private const val MAX_PREVIEW_HEIGHT = 1080

// status code
private const val REQUEST_CAMERA_PERMISSION = 200

@Suppress("DEPRECATION")
class MainActivity : BaseActivity(), NetworkReceiver.NetworkStatusObserver {
    // view binding and layout widgets
    // this property is only valid between onCreateView and onDestroyView
    private lateinit var binding: ActivityMainBinding

    // camera
    private var cameraId: String? = null
    private var cameraDevice: CameraDevice? = null
    private var cameraCaptureSession: CameraCaptureSession? = null
    private var imageReader: ImageReader? = null

    // view used to draw the preview of the camera
    private val textureView: TextureView? = null

    // the size of the camera preview
    private var previewSize: Size? = null
    private var previewRequestBuilder: CaptureRequest.Builder? = null
    private var previewRequest: CaptureRequest? = null
    private var backgroundHandler: Handler? = null
    private var backgroundThread: HandlerThread? = null
    private val cameraOpenCloseLock: Semaphore = Semaphore(1)

    // network receiver
    private val networkReceiver: NetworkReceiver = NetworkReceiver()

    // dialog
    private val dialog: DialogUtils = DialogUtils()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }

        // initialize views and handlers
        initializeViews()
        initializeHandlers()
        initializeListeners()
    }

    /**
     * Method is used to initialize views
     */
    private fun initializeViews() {

    }

    /**
     * Method is used to initialize click listeners
     */
    private fun initializeHandlers() {

    }

    /**
     * Initialize custom listeners
     */
    private fun initializeListeners() {

    }

    /**
     * This listener can be used to be notified when the surface texture associated with this
     * texture view is available.
     *
     * <p>[TextureView.SurfaceTextureListener] handles several lifecycle events on a TextureView.
     * In this case, we're listening to those events. When the SurfaceTexture is ready, we
     * initialize the camera. When it size changes, we setup the preview coming from the
     * camera accordingly</p>
     */
    private var surfaceTextureListener: SurfaceTextureListener = object : SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            // open your camera here
            openCamera(width, height)
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
            // transform you image captured size according to the surface width and height
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            return false
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
    }

    /**
     * A callback objects for receiving updates about the state of a camera device.
     * A callback instance must be provided to the CameraManager.openCamera method to open a
     * camera device.
     *
     * These state updates include notifications about the device completing startup
     * (allowing for createCaptureSession to be called), about device disconnection or closure,
     * and about unexpected device errors.
     */
    private val stateCallback: CameraDevice.StateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            Log.e(TAG, "camara state :: onOpened")
            // releases a permit, increasing the number of available permits by one.
            // If any threads are trying to acquire a permit, then one is selected and
            // given the permit that was just released. That thread is (re)enabled for
            // thread scheduling purposes
            cameraOpenCloseLock.release()
            cameraDevice = camera
            createCameraPreviewSession()
        }

        override fun onDisconnected(camera: CameraDevice) {
            Log.e(TAG, "camara state :: onDisconnected")
            // releases a permit, increasing the number of available permits by one.
            // If any threads are trying to acquire a permit, then one is selected and
            // given the permit that was just released. That thread is (re)enabled for
            // thread scheduling purposes
            cameraOpenCloseLock.release()
            cameraDevice?.close()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            Log.e(TAG, "camara state :: onError")
            // releases a permit, increasing the number of available permits by one.
            // If any threads are trying to acquire a permit, then one is selected and
            // given the permit that was just released. That thread is (re)enabled for
            // thread scheduling purposes
            cameraOpenCloseLock.release()
            cameraDevice?.close()
            cameraDevice = null
        }
    }

    /**
     * Open a connection to a camera with the given ID.
     * Use getCameraIdList to get the list of available camera devices. Note that even if an id
     * is listed, open may fail if the device is disconnected between the calls to getCameraIdList
     * and openCamera, or if a higher-priority camera API client begins using the camera device.
     *
     * <p>As of API level 23, devices for which the
     * CameraManager.AvailabilityCallback.onCameraUnavailable(String) callback has been
     * called due to the device being in use by a lower-priority, background camera API
     * client can still potentially be opened by calling this method when the calling camera
     * API client has a higher priority than the current camera API client using this device.
     * In general, if the top, foreground activity is running within your application process,
     * your process will be given the highest priority when accessing the camera, and this
     * method will succeed even if the camera device is in use by another camera API client.</p>
     *
     * <p>Any lower-priority application that loses control of the camera in this way will
     * receive an CameraDevice.StateCallback.onDisconnected callback.
     * Once the camera is successfully opened, CameraDevice.StateCallback.onOpened will be
     * invoked with the newly opened CameraDevice. The camera device can then be set up for
     * operation by calling CameraDevice.createCaptureSession and CameraDevice.createCaptureRequest.</p>
     *
     * <p>If the camera becomes disconnected during initialization after this function call returns,
     * CameraDevice.StateCallback.onDisconnected with a CameraDevice in the disconnected state
     * (and CameraDevice.StateCallback.onOpened will be skipped).</p>
     *
     * <p>If opening the camera device fails, then the device callback's onError method will be
     * called, and subsequent calls on the camera device will throw a CameraAccessException.</p>
     *
     * @param width Int Size of camera width
     * @param height Int Size of camera height
     */
    private fun openCamera(
        width: Int,
        height: Int
    ) {
        // determine whether you have been granted a particular permission.
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestCameraPermission()
            return
        }
        // setup camera output and configuration
        setUpCameraOutputs(width, height)
        configureTransform(width, height)
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        try {
            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw RuntimeException(EXCEPTION_TIMEOUT_LOCK_CAMERA_OPENING)
            }
            // open a connection to a camera with the given ID
            manager.openCamera(cameraId.orEmpty(), stateCallback, backgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            throw RuntimeException(EXCEPTION_INTERRUPTED_LOCK_CAMERA_OPENING, e)
        }
    }

    /**
     * Get a list of sizes compatible with the requested image format.
     * The format should be a supported format (one of the formats returned by getOutputFormats).
     *
     * <p>As of API level 23, the getHighResolutionOutputSizes method can be used on devices
     * that support the BURST_CAPTURE capability to get a list of high-resolution output sizes
     * that cannot operate at the preferred 20fps rate. This means that for some supported
     * formats, this method will return an empty list, if all the supported resolutions
     * operate at below 20fps. For devices that do not support the BURST_CAPTURE capability,
     * all output resolutions are listed through this method.</p>
     *
     * @param width Int Size of camera width
     * @param height Int Size of camera height
     */
    private fun setUpCameraOutputs(
        width: Int,
        height: Int
    ) {
        val manager = getSystemService(CAMERA_SERVICE) as CameraManager
        try {
            for (cameraId in manager.cameraIdList) {
                val characteristics = manager.getCameraCharacteristics(cameraId)

                val map = characteristics.get(
                    CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
                ) ?: continue

                // for still image captures, we use the largest available size
                val largest = Collections.max(
                    map.getOutputSizes(ImageFormat.JPEG).asList(),
                    CompareSizesByArea()
                )

                // create a new reader for images of the desired size and format
                imageReader = ImageReader.newInstance(
                    largest.width, largest.height,
                    ImageFormat.JPEG,  /*maxImages*/2
                )
                imageReader?.setOnImageAvailableListener(
                    null, backgroundHandler
                )
                val displaySize = Point()
                // gets the size of the display, in pixels. Value returned by this method
                // does not necessarily represent the actual raw size (native resolution)
                // of the display
                windowManager.defaultDisplay.getSize(displaySize)

                // max dimensions (width/height)
                var maxPreviewWidth: Int = displaySize.x
                var maxPreviewHeight: Int = displaySize.y
                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                    maxPreviewWidth = MAX_PREVIEW_WIDTH
                }
                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                    maxPreviewHeight = MAX_PREVIEW_HEIGHT
                }

                // Danger! Attempting to use too large a preview size could exceed the camera
                // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
                // garbage capture data
                previewSize = chooseOptimalSize(
                    map.getOutputSizes(SurfaceTexture::class.java),
                    width, height, maxPreviewWidth,
                    maxPreviewHeight, largest
                )
                this.cameraId = cameraId
                return
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            // currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs
            Toast.makeText(
                this@MainActivity,
                EXCEPTION_CAMERA2_NOT_SUPPORTED_DEVICE,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Given choices of Sizes supported by a camera, choose the smallest one that is
     * at least as large as the respective texture view size, and that is at most as
     * large as the respective max size, and whose aspect ratio matches with the
     * specified value. If doesn't exist, choose the largest one that is at most as
     * large as the respective max size, and whose aspect ratio matches with the
     * specified value.
     *
     * @param choices Array<Size> Supported camera sizes
     * @param textureViewWidth Int Screen width size
     * @param textureViewHeight Int Screen height size
     * @param maxWidth Int The maximum supported width for device
     * @param maxHeight Int The maximum supported height for device
     * @param aspectRatio Size The ratio of device sizes in different dimensions
     * @return Size Immutable class for describing width and height dimensions in pixels
     */
    private fun chooseOptimalSize(
        choices: Array<Size>,
        textureViewWidth: Int,
        textureViewHeight: Int,
        maxWidth: Int,
        maxHeight: Int,
        aspectRatio: Size
    ): Size {
        // collect the supported resolutions that are at least as big as the preview Surface
        val bigEnough = mutableListOf<Size>()
        // collect the supported resolutions that are smaller than the preview Surface
        val notBigEnough = mutableListOf<Size>()

        val width = aspectRatio.width
        val height = aspectRatio.height
        for (option in choices) {
            if (option.width <= maxWidth && option.height <= maxHeight &&
                option.height == option.width * height / width
            ) {
                if (option.width >= textureViewWidth &&
                    option.height >= textureViewHeight
                ) {
                    bigEnough.add(option)
                } else {
                    notBigEnough.add(option)
                }
            }
        }

        // pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        return when {
            bigEnough.size > 0 -> {
                Collections.min(bigEnough, CompareSizesByArea())
            }
            notBigEnough.size > 0 -> {
                Collections.max(notBigEnough, CompareSizesByArea())
            }
            else -> {
                Log.e("Camera2", "Couldn't find any suitable preview size")
                choices[0]
            }
        }
    }

    /**
     * The method configures the necessary [Matrix] transformation to [textureView].
     *
     * @param viewWidth Int Size of texture view width
     * @param viewHeight Int Size of texture view height
     */
    private fun configureTransform(viewWidth: Int, viewHeight: Int) {
        if (null == textureView || null == previewSize) {
            return
        }
        val rotation = windowManager.defaultDisplay.rotation
        val matrix = Matrix()
        val previewHeight = previewSize?.height?.toFloat() ?: 0f
        val previewWidth = previewSize?.width?.toFloat() ?: 0f
        val viewRect = RectF(
            0f,
            0f,
            viewWidth.toFloat(),
            viewHeight.toFloat()
        )
        val bufferRect = RectF(
            0f,
            0f,
            previewHeight,
            previewWidth
        )
        val centerX: Float = viewRect.centerX()
        val centerY: Float = viewRect.centerY()
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY())
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL)
            val scale: Float = max(
                viewHeight.toFloat() / previewHeight,
                viewWidth.toFloat() / previewWidth
            )
            matrix.postScale(scale, scale, centerX, centerY)
            matrix.postRotate(90 * (rotation - 2).toFloat(), centerX, centerY)
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180f, centerX, centerY)
        }
        textureView.setTransform(matrix)
    }

    /**
     * A configured capture session for a CameraDevice, used for capturing images from the
     * camera or reprocessing images captured from the camera in the same session previously.
     *
     * A CameraCaptureSession is created by providing a set of target output surfaces to
     * createCaptureSession, or by providing an android.hardware.camera2.params.InputConfiguration
     * and a set of target output surfaces to createReprocessableCaptureSession for a
     * reprocessable capture session. Once created, the session is active until a new session
     * is created by the camera device, or the camera device is closed.
     *
     * All capture sessions can be used for capturing images from the camera but only
     * reprocessable capture sessions can reprocess images captured from the camera in the
     * same session previously.
     *
     * <p>Creating a session is an expensive operation and can take several hundred milliseconds,
     * since it requires configuring the camera device's internal pipelines and allocating
     * memory buffers for sending images to the desired targets. Therefore the setup is done
     * asynchronously, and createCaptureSession and createReprocessableCaptureSession will
     * send the ready-to-use CameraCaptureSession to the provided listener's onConfigured
     * callback. If configuration cannot be completed, then the onConfigureFailed is called,
     * and the session will not become active.
     * If a new session is created by the camera device, then the previous session is closed,
     * and its associated onClosed callback will be invoked. All of the session methods will
     * throw an IllegalStateException if called once the session is closed.</p>
     *
     * <p>A closed session clears any repeating requests (as if stopRepeating had been called),
     * but will still complete all of its in-progress capture requests as normal, before a
     * newly created session takes over and reconfigures the camera device.</p>
     */
    private fun createCameraPreviewSession() {
        try {
            val texture: SurfaceTexture = binding.textureViewCamera.surfaceTexture

            // configure the size of default buffer to be the size of camera preview we want
            val width = previewSize?.width ?: 0
            val height = previewSize?.height ?: 0
            texture.setDefaultBufferSize(width, height)

            // this is the output Surface we need to start preview.
            val surface = Surface(texture)

            // we set up a CaptureRequest.Builder with the output Surface.
            previewRequestBuilder = cameraDevice?.createCaptureRequest(
                CameraDevice.TEMPLATE_PREVIEW
            )
            previewRequestBuilder?.addTarget(surface)

            // create a CameraCaptureSession for camera preview
            cameraDevice?.createCaptureSession(
                listOf(surface, imageReader?.surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                        // camera is already closed
                        if (cameraDevice == null) {
                            return
                        }

                        // the session is ready, we start displaying the preview
                        this@MainActivity.cameraCaptureSession = cameraCaptureSession
                        // update preview
                        updatePreview()
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        Toast.makeText(
                            this@MainActivity,
                            EXCEPTION_CAMERA2_CONFIGURATION_FAILED,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }, null
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    /**
     * Request endlessly repeating capture of images by this capture session.
     *
     * With this method, the camera device will continually capture images using the settings
     * in the provided CaptureRequest, at the maximum rate possible.
     *
     * Repeating requests are a simple way for an application to maintain a preview or other
     * continuous stream of frames, without having to continually submit identical requests
     * through capture.
     *
     * Repeat requests have lower priority than those submitted through capture or captureBurst,
     * so if capture is called when a repeating request is active, the capture request will be
     * processed before any further repeating requests are processed.
     *
     * To stop the repeating capture, call stopRepeating. Calling abortCaptures will also
     * clear the request.
     *
     * <p>Calling this method will replace any earlier repeating request or burst set up by this
     * method or setRepeatingBurst, although any in-progress burst will be completed before
     * the new repeat request will be used.</p>
     *
     * <p>This method does not support reprocess capture requests because each reprocess
     * CaptureRequest must be created from the TotalCaptureResult that matches the input image
     * to be reprocessed. This is either the TotalCaptureResult of capture that is sent for
     * reprocessing, or one of the TotalCaptureResults of a set of captures, when data from
     * the whole set is combined by the application into a single reprocess input image.</p>
     *
     * <p>The request must be capturing images from the camera. If a reprocess capture request
     * is submitted, this method will throw IllegalArgumentException.</p>
     */
    private fun updatePreview() {
        try {
            // auto focus should be continuous for camera preview
            previewRequestBuilder?.set(
                CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
            )

            // start displaying the camera preview
            previewRequest = previewRequestBuilder?.build()
            previewRequest?.let {
                cameraCaptureSession?.setRepeatingRequest(
                    it, // previewRequest object
                    null, // listener
                    backgroundHandler // background handler
                )
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    /**
     * Close the connection to this camera device as quickly as possible.
     *
     * <p>Immediately after this call, all calls to the camera device or active session
     * interface will throw a IllegalStateException, except for calls to close(). Once the
     * device has fully shut down, the CameraDevice.StateCallback.onClosed callback will be
     * called, and the camera is free to be re-opened.</p>
     *
     * <p>Immediately after this call, besides the final CameraDevice.StateCallback.onClosed
     * calls, no further callbacks from the device or the active session will occur, and any
     * remaining submitted capture requests will be discarded, as if
     * CameraCaptureSession.abortCaptures had been called, except that no success or
     * failure callbacks will be invoked.</p>
     */
    private fun closeCamera() {
        try {
            cameraOpenCloseLock.acquire()
            if (cameraCaptureSession != null) {
                cameraCaptureSession?.close()
                cameraCaptureSession = null
            }
            if (cameraDevice != null) {
                cameraDevice?.close()
                cameraDevice = null
            }
            if (imageReader != null) {
                imageReader?.close()
                imageReader = null
            }
        } catch (e: InterruptedException) {
            throw java.lang.RuntimeException(EXCEPTION_INTERRUPTED_LOCK_CAMER_CLOSING, e)
        } finally {
            cameraOpenCloseLock.release()
        }
    }

    /**
     * Causes this thread to begin execution; the Java Virtual Machine calls the run method of
     * this thread.
     *
     * The result is that two threads are running concurrently: the current thread
     * (which returns from the call to the start method) and the other thread
     * (which executes its run method).
     */
    private fun startBackgroundThread() {
        backgroundThread = HandlerThread(CAMERA_THREAD)
        backgroundThread?.start()
        backgroundHandler?.let { backgroundThread -> Handler(backgroundThread.looper) }
    }

    /**
     * Quits the handler thread's looper safely.
     *
     * <p>Causes the handler thread's looper to terminate as soon as all remaining messages
     * in the message queue that are already due to be delivered have been handled.
     * Pending delayed messages with due times in the future will not be delivered.</p>
     *
     * <p>Any attempt to post messages to the queue after the looper is asked to quit will fail.
     * For example, the Handler.sendMessage(Message) method will return false.</p>
     */
    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        try {
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    /***
     * Requests permissions to be granted to this application. These permissions must
     * be requested in your manifest, they should not be granted to your app, and they
     * should have protection level dangerous, regardless whether they are declared by
     * the platform or a third-party app.
     */
    private fun requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            dialog.showYesNoAlert(
                this,
                resources.getString(R.string.camera_permission_required),
                resources.getString(R.string.permission_required_to_use_camera),
                null, null,
                DialogInterface.OnClickListener { _, _ ->
                    // agrees to camera permissions
                    ActivityCompat.requestPermissions(
                        this@MainActivity, arrayOf(Manifest.permission.CAMERA),
                        REQUEST_CAMERA_PERMISSION
                    )
                }, DialogInterface.OnClickListener { _, _ ->
                    // declines camera permission
                    finish()
                })
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.size != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                    this,
                    EXCEPTION_CAMERA2_NO_PERMISSION,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun notifyConnectionChange(isConnected: Boolean) {
        if (isConnected) {
            // app is connected to network
            dialog.dismissNoNetworkDialog()
        } else {
            // app is not connected to network
            dialog.showDefaultNoNetworkAlert(
                this,
                resources.getString(R.string.no_internet_detected),
                resources.getString(R.string.check_network)
            )
        }
    }

    override fun onResume() {
        super.onResume()
        // print info
        FrameworkUtils.printInfo(this)

        // start camera background thread
        startBackgroundThread()

        // when the screen is turned off and turned back on, the SurfaceTexture is already
        // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
        // a camera and start preview from here (otherwise, we wait until the surface is ready in
        // the SurfaceTextureListener).
        if (binding.textureViewCamera.isAvailable) {
            openCamera(binding.textureViewCamera.width, binding.textureViewCamera.height)
        } else {
            binding.textureViewCamera.surfaceTextureListener = surfaceTextureListener
        }

        // only register receiver if it has not already been registered
        if (!networkReceiver.contains(this)) {
            // register network receiver
            networkReceiver.addObserver(this)
            registerReceiver(networkReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
            // print observer list
            networkReceiver.printObserverList()
        }
    }

    override fun onPause() {
        // stop camera background thread
        closeCamera()
        stopBackgroundThread()
        super.onPause()
    }

    override fun onDestroy() {
        // remove network observer
        val observerSize = networkReceiver.observerSize
        if (observerSize > 0 && networkReceiver.contains(this)) {
            try { // unregister network receiver
                unregisterReceiver(networkReceiver)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
            networkReceiver.removeObserver(this)
        }
        super.onDestroy()
    }
}