package abc.p2w2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.File;

public class ReceiveActivity extends AppCompatActivity {

    private File picturefile;
    private String picturePath;
    private ImageView mImageView;
    private EditText mEditText;
    private Bitmap bitmap;
    private String response;
    private String URL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);
        URL = getString(R.string.server_address)+getString(R.string.picture);
        mImageView = findViewById(R.id.image2);
        mEditText = findViewById(R.id.edit_text);
        response = "null";

        Intent i = getIntent();
        picturePath = i.getStringExtra(MainActivity.PATH);
        picturefile = new File(picturePath);
        new Thread(new Runnable() {
            @Override
            public void run() {
                response = UploadUtil.uploadImage(picturefile,URL);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String receivedPicPath = Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_PICTURES) + "/MyCameraApp";
                        Bitmap bitmap = BitmapFactory.decodeFile(receivedPicPath + File.separator + "receive.jpg");
                        //Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
                        mImageView.setImageBitmap(bitmap);
                        mEditText.setText(response);
                    }
                });
            }
        }).start();


    }

}
