package org.kodluyoruz.datalayersample_1;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.io.InputStream;
import java.util.concurrent.ExecutionException;

public class MainActivity extends WearableActivity implements DataClient.OnDataChangedListener {

    private TextView mTextView;
    private ImageView mImageView;
    private final static String TAG = "Wear MainActivity";
    String datapath = "/message_path";
    String imagepath = "/image_path";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        mTextView = findViewById( R.id.text );
        mImageView = findViewById( R.id.image );
        // Enables Always-on
        setAmbientEnabled();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Wearable.getDataClient( this ).addListener( this );
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.getDataClient( this ).removeListener( this );
    }

    @Override
    public void onDataChanged(@NonNull DataEventBuffer dataEventBuffer) {
        Log.d( TAG, "onDataChanged: " + dataEventBuffer );

        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();
                if (path.equals( datapath )) {

                    DataMapItem dataMapItem = DataMapItem.fromDataItem( event.getDataItem() );
                    String message = dataMapItem.getDataMap().getString( "message" );

                    Log.v( TAG, "Wear activity received message: " + message );
                    mTextView.setText( message );

                } else if(path.equals( imagepath )){

                    DataMapItem dataMapItem = DataMapItem.fromDataItem( event.getDataItem() );
                    Asset photoAsset = dataMapItem.getDataMap().getAsset( "image" );
                    new LoadBitmapAsyncTask().execute(photoAsset);

                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                Log.v( TAG, "Data deleted : " + event.getDataItem().toString() );
            } else {
                Log.e( TAG, "Unknown data event Type = " + event.getType() );
            }
        }
    }

    private class LoadBitmapAsyncTask extends AsyncTask<Asset, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Asset... params) {

            if (params.length > 0) {

                Asset asset = params[0];

                Task<DataClient.GetFdForAssetResponse> getFdForAssetResponseTask =
                        Wearable.getDataClient(getApplicationContext()).getFdForAsset(asset);

                try {
                    // Block on a task and get the result synchronously. This is generally done
                    // when executing a task inside a separately managed background thread. Doing
                    // this on the main (UI) thread can cause your application to become
                    // unresponsive.
                    DataClient.GetFdForAssetResponse getFdForAssetResponse =
                            Tasks.await(getFdForAssetResponseTask);

                    InputStream assetInputStream = getFdForAssetResponse.getInputStream();

                    if (assetInputStream != null) {
                        return BitmapFactory.decodeStream(assetInputStream);

                    } else {
                        Log.w(TAG, "Requested an unknown Asset.");
                        return null;
                    }

                } catch (ExecutionException exception) {
                    Log.e(TAG, "Failed retrieving asset, Task failed: " + exception);
                    return null;

                } catch (InterruptedException exception) {
                    Log.e(TAG, "Failed retrieving asset, interrupt occurred: " + exception);
                    return null;
                }

            } else {
                Log.e(TAG, "Asset must be non-null");
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {

            if (bitmap != null) {
                mImageView.setImageBitmap( bitmap );
            }
        }
    }
}
