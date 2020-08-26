package com.studioios.linhlee.icamera.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.ExifInterface;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.siyamed.shapeimageview.CircularImageView;
import com.mz.A;
import com.mz.Mz;
import com.studioios.linhlee.icamera.MyApplication;
import com.studioios.linhlee.icamera.R;
import com.studioios.linhlee.icamera.utils.Constant;
import com.studioios.linhlee.icamera.utils.GPUImageFilterTools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;

public class MainActivity extends AppCompatActivity implements Camera.PictureCallback, Camera.ShutterCallback, View.OnTouchListener, View.OnClickListener, SurfaceHolder.Callback {
    private String IMAGE_PATH;
    private String VIDEO_PATH;

    private GPUImage mGPUImage;
    private GPUImageFilter mFilter;
    private GPUImageFilterTools.FilterAdjuster mFilterAdjuster;

    private Camera mCamera;
    private Camera mCameraRecord;
    private MediaRecorder mMediaRecorder;
    private GLSurfaceView mPreview;
    private SurfaceView mRecordPreview;
    private SurfaceHolder mPreviewHolder;
    private SurfaceHolder mRecordPreviewHolder;
    private View topBlack;
    private View bottomBlack;
    private ImageView shotButton;
    private ImageView changeCamButton;
    private CircularImageView photoView;
    private ImageView settingsButton;
    private ImageView effectButton;
    private ImageView flashButton;
    private ImageView clockButton;
    private TextView timeText;
    private TextView clockTimeText;
    private TextView videoText;
    private TextView photoText;
    private TextView squareText;
    private ArrayList<String> listImagePath;
    private Bitmap photoPreview, takenPhoto;
    private SharedPreferences sharedPreferences;
    private Timer timer, cameraTimer;
    private int recordTime = -1;
    private int cameraType;
    private int state = 1;
    private String flashMode;
    private int shotMode;
    private int shotTime;
    private int orientation;
    private boolean isRecording = false;
    private boolean isSquare = false;

    private float x1, x2;
    private static final int MIN_DISTANCE = 150;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = MyApplication.getSharedPreferences();

        mPreview = (GLSurfaceView) findViewById(R.id.preview);
        mRecordPreview = (SurfaceView) findViewById(R.id.preview_record);
        topBlack = findViewById(R.id.top_black);
        bottomBlack = findViewById(R.id.bottom_black);
        shotButton = (ImageView) findViewById(R.id.shot_button);
        changeCamButton = (ImageView) findViewById(R.id.change_cam_button);
        photoView = (CircularImageView) findViewById(R.id.photo_view);
        settingsButton = (ImageView) findViewById(R.id.settings);
        effectButton = (ImageView) findViewById(R.id.effect);
        flashButton = (ImageView) findViewById(R.id.flash);
        clockButton = (ImageView) findViewById(R.id.clock);
        timeText = (TextView) findViewById(R.id.time_text);
        clockTimeText = (TextView) findViewById(R.id.clock_time_text);
        videoText = (TextView) findViewById(R.id.video_text);
        photoText = (TextView) findViewById(R.id.photo_text);
        squareText = (TextView) findViewById(R.id.square_text);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_PHONE_STATE},
                        1);
            } else {
                Mz.init(this);
                A.f(this);
                A.b(this);
            }
        } else {
            Mz.init(this);
            A.f(this);
            A.b(this);
        }

        mGPUImage = new GPUImage(this);
        mGPUImage.setGLSurfaceView(mPreview);

        cameraType = sharedPreferences.getInt("camera_type", 0);
        flashMode = sharedPreferences.getString("flash_mode", Camera.Parameters.FLASH_MODE_OFF);
        shotMode = sharedPreferences.getInt("shot_mode", 0);

        if (state == 1) {
            videoText.setTextColor(getResources().getColor(android.R.color.white));
            photoText.setTextColor(getResources().getColor(R.color.colorAccent));
            squareText.setTextColor(getResources().getColor(android.R.color.white));
        }

        switch (flashMode) {
            case Camera.Parameters.FLASH_MODE_OFF:
                flashButton.setImageResource(R.mipmap.ic_flash_off);
                break;
            case Camera.Parameters.FLASH_MODE_ON:
                flashButton.setImageResource(R.mipmap.ic_flash_on);
                break;
            case Camera.Parameters.FLASH_MODE_AUTO:
                flashButton.setImageResource(R.mipmap.ic_flash_auto);
                break;
        }

        switch (shotMode) {
            case 0:
                shotTime = 1;
                clockButton.setImageResource(R.mipmap.ic_clock);
                break;
            case 1:
                shotTime = 4;
                clockButton.setImageResource(R.mipmap.ic_clock3);
                break;
            case 2:
                shotTime = 6;
                clockButton.setImageResource(R.mipmap.ic_clock5);
                break;
            case 3:
                shotTime = 11;
                clockButton.setImageResource(R.mipmap.ic_clock10);
                break;
        }

        listImagePath = new ArrayList<>();
        setPhotoPreview();

        mPreviewHolder = mPreview.getHolder();
        mPreviewHolder.addCallback(this);
        mPreviewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mRecordPreviewHolder = mRecordPreview.getHolder();
        mRecordPreviewHolder.addCallback(this);
        mRecordPreviewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mPreview.setOnTouchListener(this);
        mRecordPreview.setOnTouchListener(this);
        shotButton.setOnClickListener(this);
        changeCamButton.setOnClickListener(this);
        settingsButton.setOnClickListener(this);
        effectButton.setOnClickListener(this);
        flashButton.setOnClickListener(this);
        clockButton.setOnClickListener(this);
        photoView.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Constant.hideNavigationBar(this);
        if (state == 0) {
            if (mCameraRecord == null) {
                try {
                    mCameraRecord = Camera.open(cameraType);
                    mCameraRecord.setPreviewDisplay(mRecordPreviewHolder);
                    Camera.Parameters params = mCameraRecord.getParameters();
                    params.setRotation(getRotation());
                    if (cameraType == Camera.CameraInfo.CAMERA_FACING_BACK) {
                        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                    }
                    params.setFlashMode(flashMode);
                    mCameraRecord.setParameters(params);
                    mCameraRecord.setDisplayOrientation(90);
                    mCameraRecord.startPreview();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                mCameraRecord.startPreview();
            }
        } else {
            if (mCamera == null) {
                try {
                    mCamera = Camera.open(cameraType);
                    Camera.Parameters params = mCamera.getParameters();
                    params.setRotation(getRotation());
                    if (cameraType == Camera.CameraInfo.CAMERA_FACING_BACK) {
                        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                    }
                    params.setFlashMode(flashMode);
                    mCamera.setParameters(params);
                    mCamera.setDisplayOrientation(90);
                    mGPUImage.setUpCamera(mCamera, getRotation(), false, false);
                    mCamera.startPreview();
                } catch (Exception e) {
                    Toast.makeText(this, "Unable to open camera.", Toast.LENGTH_LONG)
                            .show();
                }
            } else {
                mCamera.startPreview();
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Display mdisp = getWindowManager().getDefaultDisplay();
        int maxY= mdisp.getHeight();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                float deltaX = x2 - x1;

                if (Math.abs(deltaX) > MIN_DISTANCE) {
                    // Left to Right swipe action
                    if (x2 > x1) {
                        //Toast.makeText(this, "Left to Right swipe [Next]", Toast.LENGTH_SHORT).show();

                        if (state == 1) {
                            videoText.setTextColor(getResources().getColor(R.color.colorAccent));
                            photoText.setTextColor(getResources().getColor(android.R.color.white));
                            squareText.setTextColor(getResources().getColor(android.R.color.white));

                            if (mCamera != null) {
                                mCamera.stopPreview();
                                mCamera.setPreviewCallback(null);
                                mCamera.release();
                                mCamera = null;
                            }

                            mPreview.setVisibility(View.GONE);
                            mRecordPreview.setVisibility(View.VISIBLE);
                            effectButton.setVisibility(View.GONE);
                            clockButton.setVisibility(View.GONE);
                            timeText.setVisibility(View.VISIBLE);
                            shotButton.setImageResource(R.drawable.img_record);

                            try {
                                mCameraRecord = Camera.open(cameraType);
                                mCameraRecord.setPreviewDisplay(mRecordPreviewHolder);
                                Camera.Parameters params = mCameraRecord.getParameters();
                                params.setRotation(getRotation());
                                if (cameraType == Camera.CameraInfo.CAMERA_FACING_BACK) {
                                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                                }
                                params.setFlashMode(flashMode);
                                mCameraRecord.setParameters(params);
                                mCameraRecord.setDisplayOrientation(90);
                                mCameraRecord.startPreview();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            shotButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (!isRecording) {
                                        shotButton.setImageResource(R.drawable.img_stop);
                                        isRecording = true;
                                        mRecordPreview.setOnTouchListener(null);
                                        try {
                                            initRecorder(mRecordPreviewHolder.getSurface());
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        mMediaRecorder.start();

                                        timer = new Timer();
                                        timer.scheduleAtFixedRate(new TimerTask() {
                                            @Override
                                            public void run() {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        recordTime++;
                                                        int minute = recordTime / 60;
                                                        int second = recordTime % 60;
                                                        timeText.setText(Constant.formatTime(minute) + ":" + Constant.formatTime(second));
                                                    }
                                                });
                                            }
                                        }, 0, 1000);
                                    } else {
                                        shotButton.setImageResource(R.drawable.img_record);
                                        isRecording = false;
                                        mRecordPreview.setOnTouchListener(MainActivity.this);
                                        mMediaRecorder.stop();
                                        mMediaRecorder.reset();
                                        mMediaRecorder = null;

                                        if (timer != null) {
                                            timer.cancel();
                                            timer.purge();
                                        }
                                        recordTime = -1;
                                        timeText.setText("00:00");

                                        setPhotoPreview();
                                        Constant.addVideoToGallery(VIDEO_PATH, MainActivity.this);
                                    }
                                }
                            });
                            state = 0;
                        } else if (state == 2) {
                            videoText.setTextColor(getResources().getColor(android.R.color.white));
                            photoText.setTextColor(getResources().getColor(R.color.colorAccent));
                            squareText.setTextColor(getResources().getColor(android.R.color.white));

                            state = 1;
                            isSquare = false;

                            /*Animation topAnimation = new TranslateAnimation(0, 0, 0, 0, Animation.ABSOLUTE, 0, Animation.ABSOLUTE, -topBlack.getHeight());
                            topAnimation.setDuration(200);
                            topAnimation.setFillAfter(true);
                            topBlack.startAnimation(topAnimation);*/

                            /*Animation bottomAnimation = new TranslateAnimation(0, 0, mPreview.getHeight() - bottomBlack.getHeight(), mPreview.getHeight());
                            bottomAnimation.setDuration(200);
                            bottomAnimation.setFillAfter(true);
                            bottomBlack.startAnimation(bottomAnimation);*/

                            topBlack.setVisibility(View.GONE);
                            bottomBlack.setVisibility(View.GONE);
                        }
                    }

                    // Right to left swipe action
                    else {
                        //Toast.makeText(this, "Right to Left swipe [Previous]", Toast.LENGTH_SHORT).show();

                        if (state == 0) {
                            videoText.setTextColor(getResources().getColor(android.R.color.white));
                            photoText.setTextColor(getResources().getColor(R.color.colorAccent));
                            squareText.setTextColor(getResources().getColor(android.R.color.white));

                            if (mCameraRecord != null) {
                                mCameraRecord.stopPreview();
                                mCameraRecord.setPreviewCallback(null);
                                mCameraRecord.release();
                                mCameraRecord = null;
                            }

                            mPreview.setVisibility(View.VISIBLE);
                            mRecordPreview.setVisibility(View.GONE);
                            effectButton.setVisibility(View.VISIBLE);
                            clockButton.setVisibility(View.VISIBLE);
                            timeText.setVisibility(View.GONE);
                            shotButton.setImageResource(R.mipmap.ic_shot);

                            try {
                                mCamera = Camera.open(cameraType);
                                Camera.Parameters params = mCamera.getParameters();
                                params.setRotation(getRotation());
                                if (cameraType == Camera.CameraInfo.CAMERA_FACING_BACK) {
                                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                                }
                                params.setFlashMode(flashMode);
                                mCamera.setParameters(params);
                                mCamera.setDisplayOrientation(90);
                                mGPUImage.setUpCamera(mCamera, getRotation(), false, false);
                                mCamera.startPreview();
                            } catch (Exception e) {
                                Toast.makeText(this, "Unable to open camera.", Toast.LENGTH_LONG)
                                        .show();
                            }

                            shotButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    cameraTimer = new Timer();
                                    cameraTimer.scheduleAtFixedRate(new TimerTask() {
                                        @Override
                                        public void run() {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    mPreview.setOnTouchListener(null);
                                                    shotTime--;
                                                    clockTimeText.setVisibility(View.VISIBLE);
                                                    clockTimeText.setText(shotTime + "");
                                                    if (shotTime == 0) {
                                                        onSnapClick();
                                                        cameraTimer.cancel();
                                                        cameraTimer.purge();
                                                        clockTimeText.setVisibility(View.GONE);
                                                        mPreview.setOnTouchListener(MainActivity.this);

                                                        switch (shotMode) {
                                                            case 0:
                                                                shotTime = 1;
                                                                break;
                                                            case 1:
                                                                shotTime = 4;
                                                                break;
                                                            case 2:
                                                                shotTime = 6;
                                                                break;
                                                            case 3:
                                                                shotTime = 11;
                                                                break;
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                    }, 0, 1000);
                                }
                            });
                            state = 1;
                        } else if (state == 1) {
                            videoText.setTextColor(getResources().getColor(android.R.color.white));
                            photoText.setTextColor(getResources().getColor(android.R.color.white));
                            squareText.setTextColor(getResources().getColor(R.color.colorAccent));

                            state = 2;
                            isSquare = true;

                            /*Animation topAnimation = new TranslateAnimation(0, 0, 0, 0, Animation.ABSOLUTE, -topBlack.getHeight(), Animation.ABSOLUTE, 0);
                            topAnimation.setDuration(200);
                            topAnimation.setFillAfter(true);
                            topBlack.startAnimation(topAnimation);*/

                            /*Animation bottomAnimation = new TranslateAnimation(0, 0, 0, 0, Animation.ABSOLUTE, maxY, Animation.ABSOLUTE, maxY - bottomBlack.getTop());
                            bottomAnimation.setDuration(200);
                            bottomAnimation.setFillAfter(true);
                            bottomBlack.startAnimation(bottomAnimation);*/

                            topBlack.setVisibility(View.VISIBLE);
                            bottomBlack.setVisibility(View.VISIBLE);
                        }
                    }

                } else {
                    // consider as something else - a screen tap for example
                    if (sharedPreferences.getBoolean("touch", false)) {
                        onSnapClick();
                    }
                }
                break;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.shot_button:
                cameraTimer = new Timer();
                cameraTimer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mPreview.setOnTouchListener(null);
                                shotTime--;
                                clockTimeText.setVisibility(View.VISIBLE);
                                clockTimeText.setText(shotTime + "");
                                if (shotTime == 0) {
                                    onSnapClick();
                                    cameraTimer.cancel();
                                    cameraTimer.purge();
                                    clockTimeText.setVisibility(View.GONE);
                                    mPreview.setOnTouchListener(MainActivity.this);

                                    switch (shotMode) {
                                        case 0:
                                            shotTime = 1;
                                            break;
                                        case 1:
                                            shotTime = 4;
                                            break;
                                        case 2:
                                            shotTime = 6;
                                            break;
                                        case 3:
                                            shotTime = 11;
                                            break;
                                    }
                                }
                            }
                        });
                    }
                }, 0, 1000);
                break;
            case R.id.change_cam_button:
                if (state == 0) {
                    switchCameraRecord();
                } else {
                    switchCameraShot();
                }
                break;
            case R.id.settings:
                Intent settingIntent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(settingIntent);
                break;
            case R.id.effect:
                GPUImageFilterTools.showDialog(this, new GPUImageFilterTools.OnGpuImageFilterChosenListener() {
                    @Override
                    public void onGpuImageFilterChosenListener(final GPUImageFilter filter) {
                        switchFilterTo(filter);
                    }
                });
                break;
            case R.id.flash:
                Camera.Parameters params = mCamera.getParameters();
                if (flashMode.equals(Camera.Parameters.FLASH_MODE_OFF)) {
                    flashMode = Camera.Parameters.FLASH_MODE_ON;
                    flashButton.setImageResource(R.mipmap.ic_flash_on);
                } else if (flashMode.equals(Camera.Parameters.FLASH_MODE_ON)) {
                    flashMode = Camera.Parameters.FLASH_MODE_AUTO;
                    flashButton.setImageResource(R.mipmap.ic_flash_auto);
                } else if (flashMode.equals(Camera.Parameters.FLASH_MODE_AUTO)) {
                    flashMode = Camera.Parameters.FLASH_MODE_OFF;
                    flashButton.setImageResource(R.mipmap.ic_flash_off);
                }

                params.setFlashMode(flashMode);
                mCamera.setParameters(params);

                sharedPreferences.edit().putString("flash_mode", flashMode).apply();
                break;
            case R.id.clock:
                if (shotMode == 0) {
                    shotMode = 1;
                    shotTime = 4;
                    clockButton.setImageResource(R.mipmap.ic_clock3);

                    sharedPreferences.edit().putInt("shot_mode", 1).apply();
                } else if (shotMode == 1) {
                    shotMode = 2;
                    shotTime = 6;
                    clockButton.setImageResource(R.mipmap.ic_clock5);

                    sharedPreferences.edit().putInt("shot_mode", 2).apply();
                } else if (shotMode == 2) {
                    shotMode = 3;
                    shotTime = 11;
                    clockButton.setImageResource(R.mipmap.ic_clock10);

                    sharedPreferences.edit().putInt("shot_mode", 3).apply();
                } else if (shotMode == 3) {
                    shotMode = 0;
                    shotTime = 1;
                    clockButton.setImageResource(R.mipmap.ic_clock);

                    sharedPreferences.edit().putInt("shot_mode", 4).apply();
                }
                break;
            case R.id.photo_view:
                if (listImagePath.size() > 0) {
                    Intent photoIntent = new Intent(MainActivity.this, PhotoActivity.class);
                    startActivity(photoIntent);
                } else {
                    Toast.makeText(MainActivity.this, "No image", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void switchFilterTo(final GPUImageFilter filter) {
        if (mFilter == null
                || (filter != null && !mFilter.getClass().equals(filter.getClass()))) {
            mFilter = filter;
            mGPUImage.setFilter(mFilter);
            mFilterAdjuster = new GPUImageFilterTools.FilterAdjuster(mFilter);
        }
    }

    private void setPhotoPreview() {
        listImagePath.clear();
        listImagePath.addAll(Constant.getListGPUPath());
        if (listImagePath.size() > 0) {
            String path = listImagePath.get(0);

            File imgFile = new File(path);

            try {
                ExifInterface exif = new ExifInterface(imgFile.getAbsolutePath());
                orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            }
            else if (orientation == 3) {
                matrix.postRotate(180);
            }
            else if (orientation == 8) {
                matrix.postRotate(270);
            }

            if (imgFile.exists()) {
                String lastString = path.substring(path.length() - 4);
                if (lastString.equals(".mp4")) {
                    photoPreview = ThumbnailUtils.createVideoThumbnail(path,
                            MediaStore.Images.Thumbnails.MINI_KIND);
                } else {
                    photoPreview = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    int nh = (int) ( photoPreview.getHeight() * (512.0 / photoPreview.getWidth()) );
                    photoPreview = Bitmap.createScaledBitmap(photoPreview, 512, nh, true);
                    photoPreview = Bitmap.createBitmap(photoPreview, 0, 0, photoPreview.getWidth(), photoPreview.getHeight(), matrix, true);
                }

                //photoPreview = Bitmap.createScaledBitmap(photoPreview, photoPreview.getWidth() / 10, photoPreview.getHeight() / 10, true);
                photoView.setImageBitmap(photoPreview);
            }
        } else {
            photoView.setImageResource(R.drawable.filter_amoro);
        }
    }

    private void initRecorder(Surface surface) throws IOException {
        VIDEO_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/GPUImage/" + System.currentTimeMillis() + ".mp4";

        mCameraRecord.unlock();
        if (mMediaRecorder == null) mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setPreviewDisplay(surface);
        mMediaRecorder.setCamera(mCameraRecord);

        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        mMediaRecorder.setVideoEncodingBitRate(512 * 1000);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(640, 480);
        mMediaRecorder.setOutputFile(VIDEO_PATH);

        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            // This is thrown if the previous calls are not called with the
            // proper order
            e.printStackTrace();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Constant.hideNavigationBar(this);
    }

    private void onSnapClick() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraType, info);
        if (info.canDisableShutterSound) {
            if (sharedPreferences.getBoolean("sound", false)) {
                mCamera.enableShutterSound(true);
            } else {
                mCamera.enableShutterSound(false);
            }
        }

        mCamera.takePicture(this, null, null, this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        topBlack.getLayoutParams().height = (mPreview.getHeight() - mPreview.getWidth()) / 2;
        bottomBlack.getLayoutParams().height = (mPreview.getHeight() - mPreview.getWidth()) / 2;
        topBlack.requestLayout();
        bottomBlack.requestLayout();

        if (mCameraRecord != null) {
            try {
                mCameraRecord.setPreviewDisplay(mRecordPreviewHolder);
                Camera.Parameters params = mCameraRecord.getParameters();
                params.setRotation(getRotation());
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                params.setFlashMode(flashMode);

                mCameraRecord.setParameters(params);
                mCameraRecord.setDisplayOrientation(90);
                mCameraRecord.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    private int getRotation() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraType, info);
        int rotation = getWindowManager().getDefaultDisplay()
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

        int rotate;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            rotate = (info.orientation - degrees + 360) % 360;
        } else {
            rotate = (info.orientation + degrees + 360) % 360;
            //rotate = (360 - rotate) % 360;
        }

        return rotate;
    }

    @Override
    public void onShutter() {
        //Toast.makeText(this, "Click!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPictureTaken(final byte[] data, Camera camera) {
        IMAGE_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/GPUImage/" + System.currentTimeMillis() + ".jpg";

        takenPhoto = BitmapFactory.decodeByteArray(data, 0, data.length);
        //takenPhoto = mGPUImage.getBitmapWithFilterApplied(takenPhoto);

        Matrix mat = new Matrix();
        mat.postRotate(getRotation());

        takenPhoto = Bitmap.createBitmap(takenPhoto, 0, 0, takenPhoto.getWidth(),
                takenPhoto.getHeight(), mat, true);
        if (isSquare) {
            takenPhoto = Constant.cropSquareImage(takenPhoto);
        }

        mPreview.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mGPUImage.saveToPictures(takenPhoto, "GPUImage", System.currentTimeMillis() + ".jpg",
                new GPUImage.OnPictureSavedListener() {

                    @Override
                    public void onPictureSaved(final Uri uri) {
                        Constant.addImageToGallery(IMAGE_PATH, MainActivity.this);
                        setPhotoPreview();
                        mCamera.startPreview();

                        mPreview.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
                    }
                });

        /*new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                if (takenPhoto != null) {
                    File file = new File(IMAGE_PATH);

                    try {
                        Matrix mat = new Matrix();
                        mat.postRotate(getRotation());

                        takenPhoto = Bitmap.createBitmap(takenPhoto, 0, 0, takenPhoto.getWidth(),
                                takenPhoto.getHeight(), mat, true);
                        if (isSquare) {
                            takenPhoto = Constant.cropSquareImage(takenPhoto);
                        }

                        FileOutputStream fileOutputStream = new FileOutputStream(file);
                        takenPhoto.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);

                        fileOutputStream.flush();
                        fileOutputStream.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }

                    Constant.addImageToGallery(IMAGE_PATH, MainActivity.this);

                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                setPhotoPreview();
                mCamera.startPreview();
            }
        }.execute();*/

    }

    public void switchCameraShot() {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.setPreviewCallback(null);
                mCamera.release();
                mCamera = null;

                boolean b;

                if (cameraType == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    cameraType = Camera.CameraInfo.CAMERA_FACING_FRONT;
                    b = true;
                } else {
                    cameraType = Camera.CameraInfo.CAMERA_FACING_BACK;
                    b = false;
                }
                mCamera = Camera.open(cameraType);

                Camera.Parameters params = mCamera.getParameters();
                params.setRotation(getRotation());
                if (cameraType == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                }
                mCamera.setParameters(params);
                mCamera.setDisplayOrientation(90);
                mGPUImage.setUpCamera(mCamera, getRotation(), b, false);
                mCamera.startPreview();

                sharedPreferences.edit().putInt("camera_type", cameraType).apply();
            } else {
                Toast.makeText(this, "Camera error", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "This device does not support front camera", Toast.LENGTH_SHORT).show();
        }
    }

    public void switchCameraRecord() {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            if (mCameraRecord != null) {
                mCameraRecord.stopPreview();
                mCameraRecord.setPreviewCallback(null);
                mCameraRecord.release();
                mCameraRecord = null;

                if (cameraType == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    cameraType = Camera.CameraInfo.CAMERA_FACING_FRONT;
                } else {
                    cameraType = Camera.CameraInfo.CAMERA_FACING_BACK;
                }

                try {
                    mCameraRecord = Camera.open(cameraType);
                    mCameraRecord.setPreviewDisplay(mRecordPreviewHolder);
                    Camera.Parameters params = mCameraRecord.getParameters();
                    params.setRotation(getRotation());
                    if (cameraType == Camera.CameraInfo.CAMERA_FACING_BACK) {
                        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                    }
                    mCameraRecord.setParameters(params);
                    mCameraRecord.setDisplayOrientation(90);
                    mCameraRecord.startPreview();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                sharedPreferences.edit().putInt("camera_type", cameraType).apply();
            } else {
                Toast.makeText(this, "Camera error", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "This device does not support front camera", Toast.LENGTH_SHORT).show();
        }
    }

    public static void setMute(boolean b) {
        //mgr.setStreamMute(AudioManager.STREAM_SYSTEM, b);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED
                        && grantResults[3] == PackageManager.PERMISSION_GRANTED
                        && grantResults[4] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                    Mz.init(this);
                    A.f(this);
                    A.b(this);
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(MainActivity.this, "Permission denied!", Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
        if (mCameraRecord != null) {
            mCameraRecord.stopPreview();
            mCameraRecord.setPreviewCallback(null);
            mCameraRecord.release();
            mCameraRecord = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        if (mCameraRecord != null) {
            mCameraRecord.stopPreview();
            mCameraRecord.setPreviewCallback(null);
            mCameraRecord.release();
            mCameraRecord = null;
        }
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
        if (photoPreview != null) {
            photoPreview.recycle();
        }
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
    }
}
