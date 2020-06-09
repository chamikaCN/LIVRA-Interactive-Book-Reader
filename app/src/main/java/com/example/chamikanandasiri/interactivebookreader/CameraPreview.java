package com.example.chamikanandasiri.interactivebookreader;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

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

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.

        mHolder = getHolder();
        mHolder.addCallback(this);

        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        rectanglePaint.setARGB(100, 255, 255, 255);
        rectanglePaint.setStyle(Paint.Style.FILL);
        rectanglePaint.setStrokeWidth(2);

    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            width = this.getWidth();
            height = this.getHeight();
            Log.d("CameraPreview-width", String.valueOf(this.getWidth()));
            Log.d("CameraPreview-height", String.valueOf(this.getHeight()));

            p1 = new Point(0, 0);
            p2 = new Point(width, height);
            setWillNotDraw(false);

        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        Log.d("CameraPreview-Heightsurfacechanged", String.valueOf(this.getHeight()));
        //TODO:add preview size dynamically
        parameters.setPreviewSize(1280, 720);
        parameters.setPictureSize(height, width);

        parameters.setRotation(90);
        List<Camera.Size> pictureSizes = parameters.getSupportedPictureSizes();
        for (Camera.Size picturesize : pictureSizes) {
            Log.d("preview Size", picturesize.toString());
        }
        mCamera.setParameters(parameters);

        // start preview with new settings
        try {
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
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

    public Rect getFocusArea() {
        return getRectangle(p1, p2);
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

