package com.studioios.linhlee.icamera.utils;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lequy on 12/24/2016.
 */

public class SquareCameraPreview extends GLSurfaceView {
    private static final double ASPECT_RATIO = 0.75;
    private static final int FOCUS_MAX_BOUND = 1000;
    private static final int FOCUS_MIN_BOUND = -1000;
    private static final int FOCUS_SQR_SIZE = 100;
    private static final int INVALID_POINTER_ID = -1;
    public static final String TAG = SquareCameraPreview.class.getSimpleName();
    private static final int ZOOM_DELTA = 1;
    private static final int ZOOM_IN = 1;
    private static final int ZOOM_OUT = 0;
    private int mActivePointerId = -1;
    private Camera mCamera;
    private Camera.Area mFocusArea;
    private ArrayList<Camera.Area> mFocusAreas;
    private boolean mIsFocus;
    private boolean mIsFocusReady;
    private boolean mIsZoomSupported;
    private float mLastTouchX;
    private float mLastTouchY;
    private int mMaxZoom;
    private ScaleGestureDetector mScaleDetector;
    private int mScaleFactor = 1;

    public SquareCameraPreview(Context context) {
        super(context);
        this.init(context);
    }

    public SquareCameraPreview(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.init(context);
    }

    /*
     * Enabled aggressive block sorting
     */
    private void handleFocus(Camera.Parameters parameters) {
        List list;
        if (!this.setFocusBound(this.mLastTouchX, this.mLastTouchY) || (list = parameters.getSupportedFocusModes()) == null || !list.contains("auto")) {
            return;
        }
        Log.d((String) TAG, (String) ("" + this.mFocusAreas.size() + ""));
        parameters.setFocusAreas(this.mFocusAreas);
        parameters.setFocusMode("auto");
        this.mCamera.setParameters(parameters);
        this.mCamera.autoFocus(new Camera.AutoFocusCallback() {

            public void onAutoFocus(boolean bl, Camera camera) {
            }
        });
    }

    /*
     * Enabled aggressive block sorting
     */
    private void handleZoom(Camera.Parameters parameters) {
        int n2;
        int n3 = parameters.getZoom();
        if (this.mScaleFactor == 1) {
            n2 = n3;
            if (n3 < this.mMaxZoom) {
                n2 = n3 + 1;
            }
        } else {
            n2 = n3;
            if (this.mScaleFactor == 0) {
                n2 = n3;
                if (n3 > 0) {
                    n2 = n3 - 1;
                }
            }
        }
        parameters.setZoom(n2);
        this.mCamera.setParameters(parameters);
    }

    private void init(Context context) {
        this.mScaleDetector = new ScaleGestureDetector(context, (ScaleGestureDetector.OnScaleGestureListener) new ScaleListener());
        this.mFocusArea = new Camera.Area(new Rect(), 1000);
        this.mFocusAreas = new ArrayList();
        this.mFocusAreas.add(this.mFocusArea);
    }

    /*
     * Enabled aggressive block sorting
     */
    private boolean setFocusBound(float f2, float f3) {
        int n2 = (int) (f2 - 50.0f);
        int n3 = (int) (f2 + 50.0f);
        int n4 = (int) (f3 - 50.0f);
        int n5 = (int) (f3 + 50.0f);
        if (-1000 > n2 || n2 > 1000 || -1000 > n3 || n3 > 1000 || -1000 > n4 || n4 > 1000 || -1000 > n5 || n5 > 1000) {
            return false;
        }
        this.mFocusArea.rect.set(n2, n4, n3, n5);
        return true;
    }

    public int getViewHeight() {
        return this.getHeight();
    }

    public int getViewWidth() {
        return this.getWidth();
    }

    protected void onMeasure(int n2, int n3) {
        n3 = MeasureSpec.getSize((int) n3);
        this.setMeasuredDimension(MeasureSpec.getSize((int) n2), n3);
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        this.mScaleDetector.onTouchEvent(motionEvent);
        switch (motionEvent.getAction() & 255) {
            default: {
                return true;
            }
            case 0: {
                this.mIsFocus = true;
                this.mLastTouchX = motionEvent.getX();
                this.mLastTouchY = motionEvent.getY();
                this.mActivePointerId = motionEvent.getPointerId(0);
                return true;
            }
            case 1: {
                if (this.mIsFocus && this.mIsFocusReady) {
                    this.handleFocus(this.mCamera.getParameters());
                }
                this.mActivePointerId = -1;
                return true;
            }
            case 5: {
//                this.mCamera.cancelAutoFocus();
                this.mIsFocus = false;
                return true;
            }
            case 3:
        }
        this.mActivePointerId = -1;
        return true;
    }

    public void setCamera(Camera camera) {
        mCamera = camera;

        if (camera != null) {
            Camera.Parameters params = camera.getParameters();
            mIsZoomSupported = params.isZoomSupported();
            if (mIsZoomSupported) {
                mMaxZoom = params.getMaxZoom();
            }
        }
    }

    public void setIsFocusReady(boolean bl) {
        this.mIsFocusReady = bl;
    }

    private class ScaleListener
            extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        private ScaleListener() {
        }

        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            SquareCameraPreview.this.mScaleFactor = (int) scaleGestureDetector.getScaleFactor();
            SquareCameraPreview.this.handleZoom(SquareCameraPreview.this.mCamera.getParameters());
            return true;
        }
    }
}
