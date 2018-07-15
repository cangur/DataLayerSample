package org.kodluyoruz.datalayersample_1;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.graphics.BitmapCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnSendMessage, btnChooseFromGallery, btnSendImage;
    String dataPath = "/message_path";
    String imagePath = "/image_path";
    ImageView imageView;
    Bitmap bitmap = null;
    TextView logger;
    String TAG = "Mobile MainActivity";
    int num = 1;
    private DataClient mDataClient;
    private int bitmapByteCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        btnSendImage = findViewById( R.id.send );
        btnChooseFromGallery = findViewById( R.id.galeri_sec );
        btnSendMessage = findViewById( R.id.sendbtn );

        imageView = findViewById( R.id.image_view );

        btnSendImage.setOnClickListener( this );
        btnChooseFromGallery.setOnClickListener( this );
        btnSendMessage.setOnClickListener( this );

        mDataClient = Wearable.getDataClient( this );

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send:

                if (bitmap != null) {
                    sendPhoto( toAsset( bitmap ) );
                }

                break;

            case R.id.galeri_sec:

                Intent intent = new Intent( Intent.ACTION_PICK );
                intent.setType( "image/*" );
                startActivityForResult( intent, 1 );

                break;

            case R.id.sendbtn:

                String message = "Hello wearable " + num;
                sendMessage( message );
                num++;

                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult( requestCode, resultCode, data );

        if (resultCode == RESULT_OK) {
            Uri uri = data.getData();

            try {
                bitmap = MediaStore.Images.Media.getBitmap( this.getContentResolver(), uri );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        imageView.setImageBitmap( bitmap );
        bitmap = Bitmap.createScaledBitmap( bitmap, 350, 350, false );
        bitmapByteCount = BitmapCompat.getAllocationByteCount( bitmap );
    }

    private void sendMessage(String message) {
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create( dataPath );
        putDataMapRequest.getDataMap().putString( "message", message );
        PutDataRequest putDataRequest = putDataMapRequest.asPutDataRequest();
        putDataRequest.setUrgent();

        Task<DataItem> putDataTask = mDataClient.putDataItem( putDataRequest )
                .addOnSuccessListener( new OnSuccessListener<DataItem>() {
                    @Override
                    public void onSuccess(DataItem dataItem) {
                        Log.d( TAG, "Sending message successful: " + dataItem );
                    }
                } )
                .addOnFailureListener( new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d( TAG, "Sending message was unsuccess:" + e.getMessage() );
                    }
                } );

    }

    private void sendPhoto(Asset asset) {
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create( imagePath );
        putDataMapRequest.getDataMap().putAsset( "image", asset );
        putDataMapRequest.getDataMap().putLong( "time", new Date().getTime() );
        PutDataRequest request = putDataMapRequest.asPutDataRequest();
        request.setUrgent();

        Task<DataItem> dataItemTask = Wearable.getDataClient( this ).putDataItem( request );

        dataItemTask
                .addOnSuccessListener( new OnSuccessListener<DataItem>() {
                    @Override
                    public void onSuccess(DataItem dataItem) {
                        Log.d( TAG, "Sending image successful: " + dataItem );
                    }
                } )
                .addOnFailureListener( new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d( TAG, "Sending iamge was unsuccess:" + e.getMessage() );
                    }
                } );
    }

    private static Asset toAsset(Bitmap bitmap) {
        ByteArrayOutputStream byteStream = null;
        try {
            byteStream = new ByteArrayOutputStream();
            bitmap.compress( Bitmap.CompressFormat.PNG, 100, byteStream );
            return Asset.createFromBytes( byteStream.toByteArray() );
        } finally {
            if (null != byteStream) {
                try {
                    byteStream.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }


}
