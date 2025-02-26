package com.example.demo_camera;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity {

    TextureView mTextureView = null;
    CameraManager mCamManager = null;
    CameraDevice mCamDevice = null;
    CameraCaptureSession mCamSession = null;
    Button mClick = null;

    //预览数据大小 1080 × 720
    private Size mPreviewSize = new Size(1080, 720);
    //拍照数据图像大小 1080 × 720
    private Size mCaptureSize = new Size(1080, 720);

    private Surface mPreviewSurface = null;
    private CaptureRequest.Builder mCaptureRequestBuilder = null;
    private CaptureRequest mCaptureRequest = null;
    private ImageReader mImageReader = null;
    static String LOG_TAG = "Camera";

    private void log(String s) {
        if (s.isEmpty()) {
            Log.e(LOG_TAG, "s is empty");
            return;
        }
        Log.i(LOG_TAG, "" + s);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTextureView = findViewById(R.id.textview);
        mTextureView.setSurfaceTextureListener(textureListener);
        mClick = findViewById(R.id.button);
        mImageReader = ImageReader.newInstance(mCaptureSize.getWidth(), mCaptureSize.getHeight(), ImageFormat.JPEG, 2);
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                Image image = reader.acquireNextImage();
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                //由缓冲区存入字节数组
                buffer.get(bytes);
                //字节数组转换为jpeg格式图片，并存储在设备中
                doByte2JpegFile(bytes);
                image.close();
            }
        }, null /*mCameraHandler*/);
        mClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //创建CaptureRequest.Builder,TEMPLATE_STILL_CAPTURE代表了此Request是用于拍照
                    CaptureRequest.Builder b = mCamDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                    b.addTarget(mImageReader.getSurface());
                    mCamSession.capture(b.build(), new CameraCaptureSession.CaptureCallback() {
                        @Override
                        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                            super.onCaptureCompleted(session, request, result);
                            //返回result --> meta data
                        }
                    }, null);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 1);
            }

            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            }

            //获取CameraManager服务
            mCamManager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
            try {
                //打开主摄
                mCamManager.openCamera("0", mStateCallback, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        //App需要实现该接口，用于接收CameraDevice实例
        public CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(CameraDevice camera) {
                if (camera != null) {
                    log("camera is not null");
                }
                log("onOpened");
                //返回CameraDevice,将其存入mCamDevice
                mCamDevice = camera;
                startPreview();
            }

            @Override
            public void onDisconnected(CameraDevice camera) {
                camera.close();
                mCamDevice = null;
            }

            @Override
            public void onError(CameraDevice camera, int error) {
                camera.close();
                mCamDevice = null;
            }
        };

        public void startPreview() {
            SurfaceTexture mSurfaceTexture = mTextureView.getSurfaceTexture();
            // 设置TextureView 用于显示的缓冲区大小
            mSurfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            //创建Surface，用于显示预览数据
            mPreviewSurface = new Surface(mSurfaceTexture);
            try {
                //创建CameraCaptureSession,App需要实现CameraCaptureSession.StateCallback用于接收CameraCaptureSession实例
                mCamDevice.createCaptureSession(Arrays.asList(mPreviewSurface, mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(CameraCaptureSession session) {
                        try {
                            //创建用于预览的CaptureRequest.Builder,进而得到CaptureRequest
                            CaptureRequest.Builder b = mCamDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                            b.addTarget(mPreviewSurface);
                            CaptureRequest r = b.build();
                            mCamSession = session;
                            //下发预览需求
                            mCamSession.setRepeatingRequest(r, mPreviewCaptureCallback, null);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }
                    CameraCaptureSession.CaptureCallback mPreviewCaptureCallback = new CameraCaptureSession.CaptureCallback() {
                        @Override
                        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                            //返回result --> meta data
                        }
                    };

                    @Override
                    public void onReady(CameraCaptureSession session) {
                        super.onReady(session);
                    }

                    @Override
                    public void onConfigureFailed(CameraCaptureSession session) {

                    }
                }, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            log("onSurfaceTextureSizeChanged Enter");
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            log("onSurfaceTextureDestroyed Enter");
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            log("onSurfaceTextureUpdated Enter");
        }
    };

    private String doByte2JpegFile(byte[]... jpeg) {
        File photo = new File(Environment.getExternalStorageDirectory(), "photo-test.jpg");
        log("dir : " + Environment.getExternalStorageDirectory());

        if (photo.exists()) {
            photo.delete();
            log("photo exists");
        } else {
            log("photo not exists");
        }

        try {
            FileOutputStream fos = new FileOutputStream(photo.getPath());
            log("photo path : " + photo.getPath());

            fos.write(jpeg[0]);
            fos.close();
        }
        catch (java.io.IOException e) {
            Log.e(LOG_TAG, "Exception in photoCallback", e);
        }

        log("get jpeg files done");
        return(null);
    }
}
