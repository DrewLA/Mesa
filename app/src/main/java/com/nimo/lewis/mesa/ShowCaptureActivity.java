package com.nimo.lewis.mesa;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;

public class ShowCaptureActivity extends AppCompatActivity {

    String Uid;
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
            File image = new File(path);
            ImageView preview = findViewById(R.id.imageCaptured);
            Bitmap decodeBitmap = BitmapFactory.decodeFile(image.getAbsolutePath());
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
        DatabaseReference userReport  = FirebaseDatabase.getInstance().getReference().child("users").child(Uid).child("report");

    }

}
