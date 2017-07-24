package com.example.lxrent.camerademo;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.lxrent.camerademo.util.BitmapUtil;
import com.example.lxrent.camerademo.util.FSScreen2;
import com.example.lxrent.camerademo.util.PathManager;
import com.example.lxrent.camerademo.view.LProgressBar;

import java.io.File;
import java.util.List;

/**
 * Created by 卢东方 on 2017/4/25 下午5:28.
 * <p>
 * God love people
 * <p>
 * description:
 */
public class CameraCaptureActivity extends Activity implements SurfaceHolder.Callback, View.OnClickListener, CaptureSensorsObserver.RefocuseListener {
    private static String TAG = CameraCaptureActivity.class.getSimpleName();
    private SurfaceView cameraPreview;
    private Camera mCamera;
    private SurfaceHolder mHolder;
    private LProgressBar bnCapture;
    private Camera.PictureCallback pictureCallback;
    private int _rotation = 90;
    private int _rotationfront = -90;
    private CaptureOrientationEventListener _orientationEventListener;
    private CaptureSensorsObserver observer;
    private Camera.AutoFocusCallback focusCallback;
    private CaptureFocuseView focuseView;

    private boolean _isCapturing = true;
    private RelativeLayout rl_root_supernatant;
    private ImageView bnToggleCamera;
    private CheckBox bnToggleLight;

    private int currentCameraId;
    private int frontCameraId;

    private boolean isOpen;
    private MediaRecorder mRecorder;
    private boolean isRecording;

    private TextView tv_cancel;
    private int screenWidth;
    private int screenHeight;
    private Camera.Size mSize;
    private String videoPath;

    private MediaPlayer mMediaPlayer;
    private VideoView sv_video_play;
    private TextView tv_confirm;

    private void playVideo(final SurfaceHolder mHolder) {
        releaseMediaRecorder();
        releaseCamera();
        mHolder.removeCallback(this);
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setDisplay(mHolder);
        mMediaPlayer.getCurrentPosition();
        //设置显示视频显示在SurfaceView上
        Log.d(TAG, videoPath);
        try {
            mMediaPlayer.setDataSource(videoPath);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    bnCapture.setClickable(true);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

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
        if (Build.VERSION.SDK_INT > 8) {
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
    public void onPause() {
        super.onPause();
        releaseCamera();
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


    private void initView() {
        cameraPreview = (SurfaceView) findViewById(R.id.cameraPreview);
        bnCapture = (LProgressBar) findViewById(R.id.bnCapture);
        focuseView = (CaptureFocuseView) findViewById(R.id.viewFocuse);
        rl_root_supernatant = (RelativeLayout) findViewById(R.id.rl_root_supernatant);
        bnToggleCamera = (ImageView) findViewById(R.id.bnToggleCamera);
        bnToggleLight = (CheckBox) findViewById(R.id.bnToggleLight);
        tv_cancel = (TextView) findViewById(R.id.tv_cancel);
        sv_video_play = (VideoView) findViewById(R.id.sv_video_play);
        tv_confirm = (TextView) findViewById(R.id.tv_confirm);


        mHolder = cameraPreview.getHolder();
        mHolder.setKeepScreenOn(true);

        mHolder.addCallback(this);
        _orientationEventListener.enable();

        observer.start();
        focusCallback = new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean successed, Camera camera) {
                focuseView.setVisibility(View.INVISIBLE);
            }
        };

    }

    private void initListener() {
        bnCapture.setOnProgressTouchListener(new LProgressBar.OnProgressTouchListener() {
            @Override
            public void onClick(LProgressBar progressBar) {
                if (!_isCapturing) {
                    return;
                }
                _isCapturing = false;
                focuseView.setVisibility(View.INVISIBLE);
                //这里我们获取jpeg的回调
                try {
                    mCamera.takePicture(null, null, pictureCallback);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    _isCapturing = false;
                }
            }

            @Override
            public void onLongClick(LProgressBar progressBar) {
                startRecord();
            }

            @Override
            public void onLongClickUp(LProgressBar progressBar) {
                //抬起的话直接取消录制，并切删除录制的文件
                stopRecord();
                playVideo(mHolder);
            }
        });

        observer.setRefocuseListener(this);
        bnToggleCamera.setOnClickListener(this);
        bnToggleLight.setOnClickListener(this);
        tv_cancel.setOnClickListener(this);
        tv_confirm.setOnClickListener(this);

        pictureCallback = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                _isCapturing = false;
                bnCapture.setVisibility(View.GONE);
                //直接保存图片即可
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                if (currentCameraId == frontCameraId) {
                    bitmap = BitmapUtil.rotateAndScale(bitmap, _rotationfront, true);
                } else {
                    bitmap = BitmapUtil.rotateAndScale(bitmap, _rotation, true);
                }
                File photoFile = PathManager.getCropPhotoPath();//获取裁剪后的图片路径
                boolean successful = BitmapUtil.saveBitmap2file(bitmap, photoFile, Bitmap.CompressFormat.JPEG, 100);
                if (successful) {
                    Toast.makeText(CameraCaptureActivity.this, "保存成功", Toast.LENGTH_SHORT).show();

                }
                camera.stopPreview();
                tv_confirm.setVisibility(View.VISIBLE);

            }
        };
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mHolder = holder;
        Log.i(TAG, "surfaceCreated----开始预览了");
        openCamera(mHolder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mHolder = holder;
        //设置相机的一些尺寸
        screenWidth = FSScreen2.getScreenWidth();
        //包括虚拟按键的高度
        screenHeight = FSScreen2.getHasVirtualKey();
        Camera.Parameters parameters = mCamera.getParameters();
        //获取支持的预览尺寸
        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        Camera.Size optimalPreviewSize = getOptimalPreviewSize(sizes, screenHeight, screenWidth);
        parameters.setPreviewSize(optimalPreviewSize.width, optimalPreviewSize.height);
        parameters.setPreviewFormat(ImageFormat.YV12);
        cameraPreview.setLayoutParams(new FrameLayout.LayoutParams(optimalPreviewSize.height, optimalPreviewSize.width));
        //获取video的尺寸
        List<Camera.Size> supportedVideoSizes1 = parameters.getSupportedVideoSizes();
        mSize = getOptimalPreviewSize(supportedVideoSizes1, screenHeight, screenWidth);
        //获取照片的尺寸
        sizes = parameters.getSupportedPictureSizes();//支持图片尺寸
        optimalPreviewSize = getOptimalPreviewSize(sizes, optimalPreviewSize.width, optimalPreviewSize.height);//比值1.77
        parameters.setPictureSize(optimalPreviewSize.width, optimalPreviewSize.height);
        Log.i(TAG, "surfaceChanged----视频尺寸");
        parameters.setRotation(0);
        mCamera.setParameters(parameters);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // surfaceDestroyed的时候同时对象设置为null
        Log.i(TAG, "surfaceDestroyed----相机预览结束");
        releaseMediaRecorder();
        releaseCamera();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bnToggleCamera:
                //翻转相机
                Animation animation = AnimationUtils.loadAnimation(this, R.anim.rotate_camcer_anim);
                bnToggleCamera.startAnimation(animation);
                switchCamera();
                break;
            case R.id.bnToggleLight:
                openORCloseLight();
                break;
            case R.id.tv_cancel:
                if (_isCapturing || isRecording) {
                    finish();
                    return;
                }
                _isCapturing = true;
                releaseCamera();
                releaseMediaRecorder();
                openCamera(mHolder);
                tv_confirm.setVisibility(View.GONE);
                bnCapture.setVisibility(View.VISIBLE);
                break;
            case R.id.tv_confirm:
                finish();
                break;

        }
    }


    /**
     * 开始录制
     */
    private void startRecord() {
        Log.i(TAG, "startRecord----开始录像了");
        if (mRecorder == null) {
            mRecorder = new MediaRecorder(); // 创建MediaRecorder
        }
        if (mCamera != null) {
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
            mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
            //设置录制的视频编码比特率
            mRecorder.setVideoEncodingBitRate(2 * 1024 * 1024);// 设置帧频率，然后就清晰了
//            CamcorderProfile cProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
//            mRecorder.setProfile(cProfile);
            //设置录制的视频帧率,注意文档的说明:
            mRecorder.setVideoFrameRate(30);
            //设置要捕获的视频的宽度和高度
            mHolder.setFixedSize(mSize.width, mSize.height);//最高只能设置640x480
            mRecorder.setVideoSize(mSize.width, mSize.height);//最高只能设置640x480
            //设置记录会话的最大持续时间（毫秒）
            mRecorder.setMaxDuration(60 * 1000);
            // 输出旋转90度，保持竖屏录制
            mRecorder.setOrientationHint(90);
//注释掉并无影响，猜测是camera设置过显示就可以了
//            mRecorder.setPreviewDisplay(mHolder.getSurface());
            videoPath = getSDPath();
            if (videoPath != null) {
                File dir = new File(videoPath + "/videos");
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                videoPath = dir + "/" + System.currentTimeMillis() + ".mp4";
                //设置输出文件的路径
                mRecorder.setOutputFile(videoPath);
                Log.i(TAG, "startRecord----path" + videoPath);
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

    public String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED);//判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        }
        return sdDir.toString();
    }

    /**
     * 停止录制
     */
    private void stopRecord() {
        Log.i(TAG, "stopRecord----停止录像了");
        bnCapture.setVisibility(View.GONE);
        tv_confirm.setVisibility(View.VISIBLE);
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
        if (Build.VERSION.SDK_INT > 8) {
            try {
                mCamera = Camera.open(currentCameraId);
                //谷歌6p打开相机会闪一次，强制进行关闭闪光灯。
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
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
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
        if (null == mCamera || !_isCapturing) {
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
            if (Build.VERSION.SDK_INT <= 8) {
                _rotation = (90 + orientation) % 360;
                return;
            }

            Camera.CameraInfo info = new Camera.CameraInfo();
//            Camera.getCameraInfo(currentCameraId, info);
//        info.orientation的值  前置是270  后置的是90
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                _rotationfront = (info.orientation - orientation + 360) % 360;
            } else { // back-facing camera
//                _rotation = (info.orientation + orientation) % 360;
            }
//            Log.d(TAG, "Camera_rotation--" +"info"+info.orientation+ "orientation" + orientation);
            //  Log.d(TAG, "Camera_rotation--" + "rotation" + _rotation);

        }

    }


    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
//            Collections.sort(sizes, sizeComparator);
        final double ASPECT_TOLERANCE = 0.05;
        double targetRatio = (double) w / h;//预览标准比值
        if (sizes == null)
            return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }


        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }


        return optimalSize;
    }
}
