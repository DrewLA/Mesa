package com.nimo.lewis.mesa;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
//import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.LayoutInflater;
import android.view.View;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
/*This Class creates a surface view as its fragment content that streams camera preview until capture button clicked

 */
public class CameraFragment extends Fragment implements SurfaceHolder.Callback{

    private final int CAMERA_REQUEST_CODE = 1;
    Camera camera;
    Camera.PictureCallback jpegCallBack;
    SurfaceHolder mSurfaceHolder;
    SurfaceView mSurfaceView;

    public static CameraFragment newInstance(){
        CameraFragment fragment = new CameraFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        mSurfaceView = view.findViewById(R.id.camera);
        mSurfaceHolder = mSurfaceView.getHolder();
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        }
        else {
            mSurfaceHolder.addCallback(this);
            mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        Button mCapture = view.findViewById(R.id.capture);
        mCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureImage();
            }
        });

        jpegCallBack = new Camera.PictureCallback(){
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera){
                Intent intent = new Intent(getActivity(), ShowCaptureActivity.class);
                Bitmap coded = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
                Bitmap decoded = rotate(coded);
                String filepath = tempFileImage(getContext(), decoded, "capture");
                intent.putExtra("capture", filepath);
                startActivity(intent);
                return;
            }
        };
        return view;
    }
    //Rotate image
    private Bitmap rotate(Bitmap decodeBitmap) {
        int w = decodeBitmap.getWidth();
        int h = decodeBitmap.getHeight();

        Matrix matrix = new Matrix();
        matrix.setRotate(90);

        return Bitmap.createBitmap(decodeBitmap,0,0, w, h, matrix,true);
    }
    //Create /temporary Image file path
    private String tempFileImage(Context context, Bitmap bitmap, String capture) {
        File outputDir = context.getCacheDir();
        File imageFile = new File(outputDir, capture + ".jpg");

        OutputStream os;
        try {
            os = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
        } catch (Exception e) {
            Log.e(context.getClass().getSimpleName(), "Error writing file", e);
        }

        return imageFile.getAbsolutePath();
    }
    //Button Capture
    private void captureImage(){
        //Toast.makeText(getContext(), "Captured", Toast.LENGTH_LONG).show();
        camera.takePicture(null, null, jpegCallBack);
    }
    //SurfaceView
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder){
        camera = Camera.open();
        Camera.Parameters parameters;
        parameters = camera.getParameters();
        camera.setDisplayOrientation(90);
        parameters.setPreviewFrameRate(60);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

        Camera.Size defaultsize = null;
        List<Camera.Size> sizes = camera.getParameters().getSupportedPreviewSizes();
        defaultsize = sizes.get(0);

        //Loop through supported preview sizes for best resolution
        for (int i = 0; i < sizes.size(); i++){
            if ((sizes.get(i).width * sizes.get(i).height) > (defaultsize.width*defaultsize.height)){
                defaultsize = sizes.get(i);
            }
        }
        /*
        String x = "Height: " + defaultsize.height;
        String y = " Width: " + defaultsize.width;
        String c = x+y;
        Toast.makeText(getContext(), c, Toast.LENGTH_LONG).show();*/

        parameters.setPreviewSize(defaultsize.width, defaultsize.height);
        camera.setParameters(parameters);
        try {
            camera.setPreviewDisplay(surfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.startPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2){

    }
    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder){

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length> 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    mSurfaceHolder.addCallback(this);
                    mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                }
                else {
                    Toast.makeText(getContext(), "Needs Camera", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }
}
