package com.example.lipread;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;

public class LipRead extends AppCompatActivity {
VideoView videoView;
Button browse;
TextView text;
    Uri videoPath;
    String paths;
    String result;
public static final int PICK_VIDEO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lipread);

        videoView=findViewById(R.id.videoView);
        browse=findViewById(R.id.browser);
        text=findViewById(R.id.text);
        if (! Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
        browse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPickVideo(v);

            }
        });

    }

    // Button click handler to trigger video picker
    public void onPickVideo(View view) {

        // Intent to open video picker
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Video"),PICK_VIDEO);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_VIDEO && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri videoUri = data.getData();

            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(videoView);
            videoView.setMediaController(mediaController);
            videoView.setVideoURI(videoUri);
            videoView.requestFocus();
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    videoView.start();
                }
            });
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (resultCode == RESULT_OK) {

                        assert data != null;
                        videoPath = data.getData();


                        paths = String.valueOf(data.getData());
                        Python py = Python.getInstance();
                        PyObject pyObject = py.getModule("script");
                        PyObject object = pyObject.callAttr("LipRead", VideoBase64());
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LipRead.this,"Generation Completed",Toast.LENGTH_SHORT).show();
                                text.setText(object.toString());
                                text.setMovementMethod(new ScrollingMovementMethod());
                            }
                        });
                    }


                }
            }).start();
        }
    }



    private String VideoBase64() {
        ByteArrayOutputStream outputStream;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            try (InputStream inputStream = getContentResolver().openInputStream(videoPath)) {
                outputStream = getByteArrayOutputStream(inputStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return result = EncodeVideo(outputStream);
        }
        return null;
    }
    public ByteArrayOutputStream getByteArrayOutputStream(InputStream inputStream) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length = 0;
        while (true) {
            try {
                if ((length = inputStream.read(buffer)) == -1) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            outputStream.write(buffer, 0, length);
        }
        return outputStream;
    }

    public static String EncodeVideo( ByteArrayOutputStream outputStream)
    {
        byte[] data = outputStream.toByteArray();
        // encode the file into base64
        String encoded = Base64.encodeToString(data, 0);
        return "data:video/mpg;base64," + encoded;
    }

}