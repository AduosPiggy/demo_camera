package com.example.demo_camera;//package com.example.demo_camera;
//
//import android.Manifest;
//import android.content.pm.PackageManager;
//import android.media.AudioFormat;
//import android.media.AudioRecord;
//import android.media.MediaRecorder;
//import android.os.Bundle;
//import android.util.Log;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
//
//public class MainActivity extends AppCompatActivity {
//
//    private static final int REQUEST_AUDIO_PERMISSION_CODE = 200;
//    private boolean isRecording = false;
//    private static final String TAG = "AudioActivity";
//
//    private int sampleRate = 44100; // 采样率
//    private int channelConfig = AudioFormat.CHANNEL_IN_MONO; // 单声道
//    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT; // 16位 PCM
//    private int bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
//    private AudioRecord audioRecord ; // 将 AudioRecord 声明为类成员
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        // 请求麦克风权限
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_AUDIO_PERMISSION_CODE);
//        } else {
//            startRecording();
//        }
//    }
//
//    private void startRecording() {
//
//        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioFormat, bufferSize);
//        audioRecord.startRecording();
//        isRecording = true;
//
//        byte[] audioBuffer = new byte[bufferSize];
//
//        Log.d(TAG, "Recording started...");
//
//        new Thread(() -> {
//            while (isRecording) {
//                int read = audioRecord.read(audioBuffer, 0, audioBuffer.length);
//                if (read > 0) {
//                    // 处理音频数据，例如通过 WebRTC 发送
//                    // sendAudioData(audioBuffer, read);
//                }
//            }
//
//            audioRecord.stop();
//            audioRecord.release();
//            Log.d(TAG, "Recording stopped.");
//        }).start();
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        if (requestCode == REQUEST_AUDIO_PERMISSION_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                startRecording();
//            } else {
//                Log.e(TAG, "Permission denied to record audio");
//            }
//        }
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        isRecording = false; // 停止录音
//        if (audioRecord != null) {
//            audioRecord.stop();
//            audioRecord.release();
//        }
//    }
//}

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
