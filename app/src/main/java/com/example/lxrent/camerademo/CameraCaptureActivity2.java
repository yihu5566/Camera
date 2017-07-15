package com.example.lxrent.camerademo;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.lxrent.camerademo.util.BitmapUtil;
import com.example.lxrent.camerademo.util.PathManager;

import java.io.File;

import static android.content.ContentValues.TAG;

/**
 * Created by 卢东方 on 2017/4/25 下午5:28.
 * <p>
 * God love people
 * <p>
 * description:
 */
public class CameraCaptureActivity2 extends Activity implements SurfaceHolder.Callback, View.OnClickListener, CaptureSensorsObserver.RefocuseListener {

    private SurfaceView cameraPreview;
    private Camera mCamera;
    private SurfaceHolder mHolder;
    private ImageView bnCapture;
    private Camera.PictureCallback pictureCallback;
    private float kPhotoMaxSaveSideLen = 1000;
    private int _rotation = 90;
    private int _rotationfront = -90;
    private CaptureOrientationEventListener _orientationEventListener;
    private CaptureSensorsObserver observer;
    private Camera.AutoFocusCallback focusCallback;
    private CaptureFocuseView focuseView;

    private boolean _isCapturing;
    private RelativeLayout rl_root_supernatant;
    private ImageView bnToggleCamera;
    private CheckBox bnToggleLight;

    private int currentCameraId;
    private int frontCameraId;

    private boolean isOpen;
    private MediaRecorder mRecorder;
    private boolean isRecording;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_preview_layout);
        _orientationEventListener = new CaptureOrientationEventListener(this);//屏幕旋转监听
        observer = new CaptureSensorsObserver(this);//传感器监听
        initView();
        initListener();
        setupDevice();

    }

    private void setupDevice() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
//            Toast.makeText(this, "当前设备没有闪光灯", Toast.LENGTH_LONG).show();
            bnToggleLight.setVisibility(View.INVISIBLE);
        } else {
            bnToggleLight.setVisibility(View.VISIBLE);
        }
        if (android.os.Build.VERSION.SDK_INT > 8) {
            int cameraCount = Camera.getNumberOfCameras();

            if (cameraCount < 1) {
                Toast.makeText(this, "你的设备木有摄像头。。。", Toast.LENGTH_SHORT).show();
                finish();
                return;
            } else if (cameraCount == 1) {
                bnToggleCamera.setVisibility(View.INVISIBLE);
            } else {
                bnToggleCamera.setVisibility(View.VISIBLE);
            }

            currentCameraId = 0;
            frontCameraId = findFrontFacingCamera();
            if (-1 == frontCameraId) {
                bnToggleCamera.setVisibility(View.INVISIBLE);
            }
        }


    }

    /**
     * 找摄像头
     *
     * @return
     */
    private int findFrontFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                Log.d(TAG, "Camera found");
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _orientationEventListener = null;
        if (null != observer) {
            observer.setRefocuseListener(null);
            observer = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        _orientationEventListener.disable();
        observer.stop();
    }

    private void initListener() {
        bnCapture.setOnClickListener(this);
        observer.setRefocuseListener(this);
        bnToggleCamera.setOnClickListener(this);
        bnToggleLight.setOnClickListener(this);
        pictureCallback = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                _isCapturing = false;
                //直接保存图片即可
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

                bitmap = BitmapUtil.rotateAndScale(bitmap, _rotation, true);
                File photoFile = PathManager.getCropPhotoPath();//获取裁剪后的图片路径
                boolean successful = BitmapUtil.saveBitmap2file(bitmap, photoFile, Bitmap.CompressFormat.JPEG, 100);
                if (successful) {
                    Toast.makeText(CameraCaptureActivity2.this, "保存成功", Toast.LENGTH_SHORT).show();

                }
                finish();

            }
        };
    }

    private void initView() {
        cameraPreview = (SurfaceView) findViewById(R.id.cameraPreview);
        bnCapture = (ImageView) findViewById(R.id.bnCapture);
        focuseView = (CaptureFocuseView) findViewById(R.id.viewFocuse);
        rl_root_supernatant = (RelativeLayout) findViewById(R.id.rl_root_supernatant);
        bnToggleCamera = (ImageView) findViewById(R.id.bnToggleCamera);
        bnToggleLight = (CheckBox) findViewById(R.id.bnToggleLight);


        mHolder = cameraPreview.getHolder();
        mHolder.setKeepScreenOn(true);
        mHolder.addCallback(this);
        openCamera(mHolder);
        _orientationEventListener.enable();

        observer.start();
        focusCallback = new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean successed, Camera camera) {
                focuseView.setVisibility(View.INVISIBLE);
            }
        };

    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (Exception e) {
            Log.d(TAG, "Error setting mCamera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();// 停止预览
            mCamera.release(); // 释放摄像头资源
            mCamera = null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bnCapture:
                //拍照修改为录制小视频


                if (!isRecording) {
                    startRecord();
                } else {
                    stopRecord();
                }
//                if (_isCapturing) {
//                    return;
//                }
//                _isCapturing = true;
//                focuseView.setVisibility(View.INVISIBLE);
//                //这里我们获取jpeg的回调
//                try {
//                    camera.takePicture(null, null, pictureCallback);
//                } catch (RuntimeException e) {
//                    e.printStackTrace();
//                    _isCapturing = false;
//                }

                break;
            case R.id.bnToggleCamera:
                //翻转相机
                Animation animation = AnimationUtils.loadAnimation(this, R.anim.rotate_camcer_anim);
                bnToggleCamera.startAnimation(animation);
                switchCamera();
                break;
            case R.id.bnToggleLight:
                openORCloseLight();
                break;
        }
    }

    /**
     * 开始录制
     */
    private void startRecord() {

        if (mRecorder == null) {
            mRecorder = new MediaRecorder(); // 创建MediaRecorder
        }
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.unlock();
            mRecorder.setCamera(mCamera);
        }
        try {
            // 设置音频采集方式
            mRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            //设置视频的采集方式
            mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            //设置文件的输出格式
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);//aac_adif， aac_adts， output_format_rtp_avp， output_format_mpeg2ts ，webm
            //设置audio的编码格式
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            //设置video的编码格式
            mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            //设置录制的视频编码比特率
            mRecorder.setVideoEncodingBitRate(1024 * 1024);
            //设置录制的视频帧率,注意文档的说明:
            mRecorder.setVideoFrameRate(30);
            //设置要捕获的视频的宽度和高度
            mHolder.setFixedSize(320, 240);//最高只能设置640x480
            mRecorder.setVideoSize(320, 240);//最高只能设置640x480
            //设置记录会话的最大持续时间（毫秒）
            mRecorder.setMaxDuration(60 * 1000);
            mRecorder.setPreviewDisplay(mHolder.getSurface());
            String path = getExternalCacheDir().getPath();
            if (path != null) {
                File dir = new File(path + "/videos");
                if (!dir.exists()) {
                    dir.mkdir();
                }
                path = dir + "/" + System.currentTimeMillis() + ".mp4";
                //设置输出文件的路径
                mRecorder.setOutputFile(path);
                //准备录制
                mRecorder.prepare();
                //开始录制
                mRecorder.start();
                isRecording = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止录制
     */
    private void stopRecord() {
        try {
            //停止录制
            mRecorder.stop();
            //重置
            mRecorder.reset();
        } catch (Exception e) {
            e.printStackTrace();
        }
        isRecording = false;
    }
    private void openORCloseLight() {


        if (isOpen) {
            Camera.Parameters parameter = mCamera.getParameters();
            parameter.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(parameter);
            isOpen = false;
        } else {
            Camera.Parameters parameter = mCamera.getParameters();
            parameter.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            mCamera.setParameters(parameter);
            isOpen = true;
        }
        bnToggleLight.setChecked(isOpen);

    }

    /**
     * 释放MediaRecorder
     */
    private void releaseMediaRecorder() {
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }
    }

    private void switchCamera() {
        if (currentCameraId == 0) {
            currentCameraId = frontCameraId;
            //隐藏闪光灯
            bnToggleLight.setVisibility(View.INVISIBLE);

        } else {
            currentCameraId = 0;
            bnToggleLight.setVisibility(View.VISIBLE);
        }
        releaseCamera();
        openCamera(mHolder);
    }
    private void openCamera(SurfaceHolder mHolder) {
        Log.d("onDestroy--", "openCamera");
        if (android.os.Build.VERSION.SDK_INT > 8) {
            try {
                mCamera = Camera.open(currentCameraId);
                //谷歌6p打开相机会闪一次，强制进行关闭闪光灯。
//                Camera.Parameters parameter = camera.getParameters();
//                parameter.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
//                camera.setParameters(parameter);
            } catch (Exception e) {
                Toast.makeText(this, "摄像头打开失败", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            setCameraDisplayOrientation(this, currentCameraId, mCamera);
        } else {
            try {
                mCamera = Camera.open();
            } catch (Exception e) {
                Toast.makeText(this, "摄像头打开失败", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }
        /**************************/

        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }
        Log.i("preview____拍照", "surfaceChanged1");
        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e) {
        }
        /****************/
        observer.start();
        _orientationEventListener.enable();
    }
    /**
     * 释放摄像头
     */
    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release(); // release the camera for other applications
            mCamera = null;
        }
    }
    /**
     * 控制相机方向
     *
     * @param activity
     * @param cameraId
     * @param camera
     */
    private static void setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        //LogEx.i("result: " + result);
        camera.setDisplayOrientation(result);
    }
    @Override
    public void needFocuse() {
        if (null == mCamera || _isCapturing) {
            return;
        }
        //LogEx.i("autoFocus");
        mCamera.cancelAutoFocus();
        try {
            mCamera.autoFocus(focusCallback);
        } catch (Exception e) {
            return;
        }
        if (View.INVISIBLE == focuseView.getVisibility()) {
            focuseView.setVisibility(View.VISIBLE);
            focuseView.getParent().requestTransparentRegion(cameraPreview);
        }

    }


    private class CaptureOrientationEventListener extends OrientationEventListener {
        public CaptureOrientationEventListener(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            if (null == mCamera)
                return;
            if (orientation == ORIENTATION_UNKNOWN)
                return;

            orientation = (orientation + 45) / 90 * 90;
            if (android.os.Build.VERSION.SDK_INT <= 8) {
                _rotation = (90 + orientation) % 360;
                return;
            }

            Camera.CameraInfo info = new Camera.CameraInfo();
//            Camera.getCameraInfo(currentCameraId, info);
//        info.orientation的值  前置是270  后置的是90
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                _rotationfront = (info.orientation - orientation + 360) % 360;
            } else { // back-facing camera
                _rotation = (info.orientation + orientation) % 360;
            }
//            Log.d(TAG, "Camera_rotation--" +"info"+info.orientation+ "orientation" + orientation);
            //  Log.d(TAG, "Camera_rotation--" + "rotation" + _rotation);

        }
    }

}
