package com.studioios.linhlee.icamera.fragments;

import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.studioios.linhlee.icamera.R;

/**
 * Created by lequy on 12/14/2016.
 */

public class PhotoFilterFragment extends Fragment implements SurfaceHolder.Callback {
    private Camera mCamera;
    private SurfaceView mPreview1, mPreview2, mPreview3;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_photo_filter, container, false);

        mPreview1 = (SurfaceView) rootView.findViewById(R.id.preview1);
        mPreview2 = (SurfaceView) rootView.findViewById(R.id.preview2);
        mPreview3 = (SurfaceView) rootView.findViewById(R.id.preview3);

        mPreview1.getHolder().addCallback(this);
        mPreview1.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        /*if (mCamera == null) {
            try {
                mCamera = Camera.open(0);
                mCamera.setPreviewDisplay(mPreview1.getHolder());
                mCamera.startPreview();
            } catch (Exception e) {
                Toast.makeText(getActivity(), "Unable to open camera.", Toast.LENGTH_LONG)
                        .show();
            }
        } else {
            mCamera.startPreview();
        }*/
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        /*try {
            mCamera.setPreviewDisplay(mPreview1.getHolder());
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        /*Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_FRONT, info);
        int rotation = getActivity().getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break; // Natural orientation
            case Surface.ROTATION_90:
                degrees = 90;
                break; // Landscape left
            case Surface.ROTATION_180:
                degrees = 180;
                break;// Upside down
            case Surface.ROTATION_270:
                degrees = 270;
                break;// Landscape right
        }
        int rotate = (info.orientation - degrees + 360) % 360;
        Camera.Parameters params = mCamera.getParameters();
        params.setRotation(rotate);
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

        mCamera.setParameters(params);
        mCamera.setDisplayOrientation(90);
        mCamera.startPreview();*/
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

}
