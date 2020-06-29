package com.example.chamikanandasiri.interactivebookreader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static androidx.constraintlayout.widget.Constraints.TAG;

/**
 * A basic Camera preview class
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    protected final Paint rectanglePaint = new Paint();
    private Point p1;
    private Point p2;
    private int width, height;
    Boolean status = false;

    private Size pictureSize;

    //Camera 2
    private HandlerThread cameraHandlerThread;
    private Handler cameraHandler;
    private CameraManager cameraManager;
    private String cameraId; //either 0 or 1 I think
    private CameraDevice cameraDevice;
    private CameraCharacteristics cameraCharacteristics;
    private CaptureRequest.Builder captureRequestBuilder;
    private CameraCaptureSession cameraCaptureSession;
    private StreamConfigurationMap streamConfigs;

    private ImageReader cameraImageReader;
    private Surface surfacePreview;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);

    }

    public CameraPreview(Context context) {
        super(context);
//        mCamera = camera;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.

        mHolder = getHolder();
        mHolder.addCallback(this);

        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        rectanglePaint.setARGB(100, 255, 255, 255);
        rectanglePaint.setStyle(Paint.Style.FILL);
        rectanglePaint.setStrokeWidth(2);

        pictureSize = new Size(1280, 720);

        // Camera2
        cameraId = "0";

        cameraHandlerThread = new HandlerThread("Camera");
        cameraHandlerThread.start();
        cameraHandler = new Handler(cameraHandlerThread.getLooper());

        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);

    }

    private CameraDevice.StateCallback cameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            startPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            if (camera != null) {
                camera.close();
                cameraDevice = null;
            }
            cameraCaptureSession = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {

        }
    };

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            cameraManager.openCamera(cameraId, cameraDeviceStateCallback, null); // use cameraHandler instead of null

            width = this.getWidth();
            height = this.getHeight();
            Toast.makeText(getContext(), "surface " + width + " " + height , Toast.LENGTH_SHORT).show();
            Log.d("CameraPreview-width", String.valueOf(this.getWidth()));
            Log.d("CameraPreview-height", String.valueOf(this.getHeight()));

            p1 = new Point(0, 0);
            p2 = new Point(width, height);
            setWillNotDraw(false);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }

    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
//
//        if (mHolder.getSurface() == null) {
//            // preview surface does not exist
//            return;
//        }
//
//        // stop preview before making changes
//        try {
//            mCamera.stopPreview();
//        } catch (Exception e) {
//            // ignore: tried to stop a non-existent preview
//        }
//
//        // set preview size and make any resize, rotate or
//        // reformatting changes here
//        Camera.Parameters parameters = mCamera.getParameters();
//        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
//        Log.d("CameraPreview-Heightsurfacechanged", String.valueOf(this.getHeight()));
//        //TODO:add preview size dynamically
//        parameters.setPreviewSize(1280, 720);
////        parameters.setPictureSize(height, width);
//        parameters.setPictureSize(pictureSize.getWidth(), pictureSize.getHeight());
//
//        parameters.setRotation(90);
//        List<Camera.Size> pictureSizes = parameters.getSupportedPictureSizes();
//        for (Camera.Size picturesize : pictureSizes) {
//            Log.d("preview Size", picturesize.toString());
//        }
//        mCamera.setParameters(parameters);
//
//        // start preview with new settings
//        try {
//            mCamera.setDisplayOrientation(90);
//            mCamera.setPreviewDisplay(mHolder);
//            mCamera.startPreview();
//        } catch (Exception e) {
//            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
//        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.w(this.getClass().getName(), "On Draw Called");
//        super.onDraw(canvas);
        canvas.drawRect(p1.x, p1.y, p2.x, p2.y, rectanglePaint);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        status = true;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                p1.x = x;
                p1.y = y;

                return true;
            case MotionEvent.ACTION_UP:

                p2.x = x;
                p2.y = y;
                postInvalidate();
                break;
            default:
                return false;
        }
        return true;
    }

    private Range<Integer> getRange() {
        CameraCharacteristics chars = null;
        try {
            chars = cameraManager.getCameraCharacteristics("0");
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Range<Integer>[] ranges = chars.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);

        Range<Integer> result = null;

        for (Range<Integer> range : ranges) {
            int upper = range.getUpper();

            // 10 - min range upper for my needs
            if (upper >= 10) {
                if (result == null || upper < result.getUpper().intValue()) {
                    result = range;
                }
            }
        }

        if (result == null) {
            result = ranges[0];
        }

        return result;
    }


    private void startPreview()
    {
//        SurfaceTexture mySurfaceTexture1 = myTextureView1.getSurfaceTexture();
//        SurfaceTexture mySurfaceTexture2 = myTextureView2.getSurfaceTexture();
        try {
            cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
        }catch(CameraAccessException e) {
            e.printStackTrace();
        }

        streamConfigs = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        Size dim = streamConfigs.getOutputSizes(SurfaceHolder.class)[0];
        //mySurfaceTexture1.setDefaultBufferSize(dim.getWidth(), dim.getHeight());
        this.getHolder().setFixedSize(dim.getWidth(), dim.getHeight());

        cameraImageReader = ImageReader.newInstance(dim.getWidth(), dim.getWidth(), ImageFormat.JPEG, 1);
        Toast.makeText(getContext(), "Image " + dim.getWidth() + " " + dim.getHeight(), Toast.LENGTH_SHORT).show();
        surfacePreview = this.getHolder().getSurface();

        List<Surface> surfaceList = new ArrayList<>();
        surfaceList.add(surfacePreview);
        surfaceList.add(cameraImageReader.getSurface());

        CaptureRequest.Builder camptureRequestStillBuilder;
        try {
            cameraDevice.createCaptureSession(surfaceList, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    Log.i(TAG, "capture session configured: " + session);
                    cameraCaptureSession = session;

                    try {
                        captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                        captureRequestBuilder.set(CaptureRequest.CONTROL_AE_LOCK, false);
                        captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
                        captureRequestBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_AUTO);
                        captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        captureRequestBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, CameraMetadata.STATISTICS_FACE_DETECT_MODE_SIMPLE);
                        captureRequestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, getRange());
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }

                    captureRequestBuilder.addTarget(surfacePreview);

                    CaptureRequest captureRequest = captureRequestBuilder.build();

                    try {
                        session.setRepeatingRequest(captureRequest, null, cameraHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    Log.e(TAG, "capture session configure failed: " + session);
                }
            }, cameraHandler);
        }catch(CameraAccessException e){
            e.printStackTrace();
        }

    }

    public void captureImage(Consumer<Bitmap> imageCallback)
    {

        try {
            cameraImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = reader.acquireLatestImage();
                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                    byte[] bytes = new byte[buffer.capacity()];
                    buffer.get(bytes);
                    Bitmap bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
                    pictureSize = new Size(bitmapImage.getWidth(), bitmapImage.getHeight());
                    image.close();
                    imageCallback.accept(bitmapImage);
                }
            }, null);

            CaptureRequest.Builder captureStillRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureStillRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, 90);
//            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_LOCK, false);
//            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
//            captureRequestBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_AUTO);
//            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
//            captureRequestBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, CameraMetadata.STATISTICS_FACE_DETECT_MODE_SIMPLE);
//            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, getRange());

            captureStillRequestBuilder.addTarget(cameraImageReader.getSurface());
            cameraCaptureSession.capture(captureStillRequestBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }


    }

    public Rect getFocusArea() {
        Rect rect = getRectangle(p1, p2);
        return new Rect((rect.left * pictureSize.getWidth() / width) , (rect.top * pictureSize.getHeight() / height),
                (rect.right * pictureSize.getWidth() / width) , (rect.bottom * pictureSize.getHeight() / height));
    }


    public Rect getRectangle(Point p1, Point p2) {
        if (p1.x < p2.x) {
            if (p1.y < p2.y) {
                return new Rect(p1.x, p1.y, p2.x, p2.y);
            } else {
                return new Rect(p1.x, p2.y, p2.x, p1.y);
            }
        } else {
            if (p1.y < p2.y) {
                return new Rect(p2.x, p1.y, p1.x, p2.y);
            } else {
                return new Rect(p2.x, p2.y, p1.x, p1.y);
            }

        }
    }
}
//    public Camera.Size getOptimalPreviewSize(List<Camera.Size> pictureSizes,int width,int height){
//        Size display=new Size(height,width);
//        Camera.Size size;
//        for (Camera.Size pictureSize:pictureSizes){
//            if (pictureSize.equals(display)){
//                Log.v(TAG,"optimalPreview"+height+" "+width);
//                return pictureSize;
//            }
//            else{
//                if(pictureSize.height<=width &&pictureSize.width<=height){
//                    size=pictureSize;
//                    return size;
//                }
//
//            }
//            pictureSize.height
//        }
//    }
//    }

