package com.nimo.lewis.mesa;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.sql.Time;
import java.util.HashMap;
import java.util.Map;

public class ShowCaptureActivity extends AppCompatActivity {

    String Uid;
    File image;
    Bitmap decodeBitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_capture);
        //Get extras from intent
        Bundle extras = getIntent().getExtras();
        assert extras != null;
        String path = extras.getString("capture");
        //In this case, extras contains image path
        if (path != null) {
            image = new File(path);
            ImageView preview = findViewById(R.id.imageCaptured);
            decodeBitmap = BitmapFactory.decodeFile(image.getAbsolutePath());
            preview.setImageBitmap(decodeBitmap);
        }
        //Get current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Uid = user.getUid();
        //Send image to database
        Button send_image = findViewById(R.id.send_button);
        send_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendToGov();
            }
        });

    }

    private void sendToGov() {
        final DatabaseReference userReportDb  = FirebaseDatabase.getInstance().getReference().child("users").child(Uid).child("report");
        final String key = userReportDb.push().getKey();
        final StorageReference filePath = FirebaseStorage.getInstance().getReference().child("Reports").child(key);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        decodeBitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
        byte[] dataToSend = baos.toByteArray();
        final UploadTask uploadTask = filePath.putBytes(dataToSend);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if(!task.isSuccessful()){
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            Long currentTimestamp = System.currentTimeMillis();
                            Map<String, Object> mapToUpload = new HashMap<>();
                            mapToUpload.put("imageUrl", task.getResult().toString());
                            mapToUpload.put("time", currentTimestamp);
                            userReportDb.child(key).setValue(mapToUpload);
                            Toast.makeText(getApplicationContext(), "Sent", Toast.LENGTH_LONG).show();
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Not Successful", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                finish();
                return;
            }
        });

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ShowCaptureActivity.this, "Failed to Upload", Toast.LENGTH_LONG).show();
                return;
            }
        });
    }

}
