package msp.cambridge.emotiondemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.android.cameraview.CameraView;
import com.microsoft.projectoxford.emotion.EmotionServiceClient;
import com.microsoft.projectoxford.emotion.EmotionServiceRestClient;
import com.microsoft.projectoxford.emotion.contract.RecognizeResult;

import java.util.List;

/**
 * Activity showing the camera and allowing users to take photos.
 *
 * @author Henry Thompson
 */
public class CameraActivity extends AppCompatActivity implements CallEmotionApiTask.OnEmotionRequestComplete {
    /** The tag used in the log to indicate that a log entry was created in this class */
    private static final String LOG_TAG = "CameraActivity";

    /** The Client for accessing the Microsoft Emotion API endpoint */
    private EmotionServiceClient mEmotionClient;

    /** The View which displays what is in front of the back camera to the user */
    private CameraView mCameraView;

    /** The Button which, when pressed, triggers the camera to take a picture and submit it to the Emotion API */
    private Button mTakePhotoButton;

    /** A spinning animation shown when the Client is sending the image to the Emotion API endpoint */
    private ProgressBar mProgressBar;

    /** The ListView displaying the result of the request to the Emotion API endpoint */
    private ListView mResultsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mCameraView = (CameraView) findViewById(R.id.activity_camera_camera_view);
        mTakePhotoButton = (Button) findViewById(R.id.activity_camera_take_photo_button);
        mProgressBar = (ProgressBar) findViewById(R.id.activity_camera_progress);
        mResultsList = (ListView) findViewById(R.id.activity_camera_result_list);

        mEmotionClient = new EmotionServiceRestClient(getString(R.string.emotion_api_subscription_key));

        mCameraView.addCallback(onPictureTaken);
        mTakePhotoButton.setOnClickListener(v -> takePicture());
        mResultsList.setTranslationY(1000);

        mResultsList.post(this::hideResultsListNoAnimation);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (hasCameraPermission()) {
            mCameraView.start();
        }
        else {
            requestCameraPermission();
        }
    }

    @Override
    public void onPause() {
        mCameraView.stop();
        super.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (mResultsList.getVisibility() == View.VISIBLE) {
                resetCameraActivity();
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode,
                                           @NonNull final String permissions[],
                                           @NonNull final int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_CODE_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mCameraView.start();
                }
                else {
                    // We absolutely need the camera. Try again.
                    requestCameraPermission();
                }

                break;
            }
        }
    }

    /** Takes a picture using the Camera View provided that we have sufficient permissions to. */
    private void takePicture() {
        if (hasCameraPermission()) {
            mCameraView.takePicture();
            mTakePhotoButton.setEnabled(false);
        }
        else {
            requestCameraPermission();
        }
    }

    /**
     * @return <code>true</code> if and only if the user has granted permission for this app to
     * use the camera.
     */
    private boolean hasCameraPermission() {
        return PackageManager.PERMISSION_GRANTED ==
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
    }

    /**
     * Requests permission to use the Camera from the user, displaying a rationale if the user
     * has declined permission in the past.
     */
    private void requestCameraPermission() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            createCameraPermissionsRationaleDialog(this::requestCameraPermissionNoRationale).show();
        }
        else {
            requestCameraPermissionNoRationale();
        }
    }

    /** Requests permission to use the Camera from the user, never displaying the rationale. */
    private void requestCameraPermissionNoRationale() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                PERMISSION_CODE_REQUEST_CAMERA);
    }

    /**
     * @return <code>true</code> if and only if the user has granted permission for this app to
     * use the Internet.
     */
    private boolean hasInternetPermission() {
        return PackageManager.PERMISSION_GRANTED ==
                ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
    }

    /**
     * Requests permission to use the Internet from the user, displaying a rationale if the user
     * has declined permission in the past.
     */
    private void requestInternetPermission() {
        Log.d(LOG_TAG, "Requesting internet permission");

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                shouldShowRequestPermissionRationale(Manifest.permission.INTERNET)) {
            createInternetPermissionsRationaleDialog(this::requestInternetPermissionNoRationale).show();
        }
        else {
            requestInternetPermissionNoRationale();
        }
    }

    /** Requests permission to use the Internet from the user, never displaying the rationale. */
    private void requestInternetPermissionNoRationale() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.INTERNET},
                PERMISSION_CODE_REQUEST_INTERNET);

    }

    /**
     * Creates a dialog which displays the rationale for requiring the internet to the user.
     * @param onDismiss A callback to be invoked when the dialog is dismissed.
     * @return A dialog that, when shown, displays to the user a message indicating that
     * why the internet permission is required. Note that the dialog is not shown, and so
     * AlertDialog.show() must be called explicitly.
     */
    @NonNull
    public AlertDialog createInternetPermissionsRationaleDialog(@Nullable Runnable onDismiss) {
        return new AlertDialog.Builder(this)
                .setMessage(R.string.internet_permission_rationale)
                .setPositiveButton(android.R.string.ok, (dialog, id) -> {
                    if (onDismiss != null) {
                        onDismiss.run();
                    }
                })
                .setOnDismissListener(dialog -> {
                    if (onDismiss != null) {
                        onDismiss.run();
                    }
                })
                .create();
    }

    /**
     * Creates a dialog which displays the rationale for requiring the camera to the user.
     * @param onDismiss A callback to be invoked when the dialog is dismissed.
     * @return A dialog that, when shown, displays to the user a message indicating that
     * why the camera permission is required. Note that the dialog is not shown, and so
     * AlertDialog.show() must be called explicitly.
     */
    @NonNull
    public AlertDialog createCameraPermissionsRationaleDialog(@Nullable Runnable onDismiss) {
        return new AlertDialog.Builder(this)
                .setMessage(R.string.camera_permission_rationale)
                .setPositiveButton(android.R.string.ok, (dialog, id) -> {
                    if (onDismiss != null) {
                        onDismiss.run();
                    }
                })
                .setOnDismissListener(dialog -> {
                    if (onDismiss != null) {
                        onDismiss.run();
                    }
                })
                .create();
    }

    /** Callback called by the CameraView whenever a picture is taken. */
    private CameraView.Callback onPictureTaken = new CameraView.Callback() {
        @Override
        public void onPictureTaken(@NonNull final CameraView cameraView,
                                   @NonNull final byte[] data) {
            if (!hasInternetPermission()) {
                requestInternetPermission();
                return;
            }
            else if (!hasCameraPermission()) {
                requestCameraPermission();
                return;
            }

            showProgressSpinner();

            new CallEmotionApiTask(data, mEmotionClient, CameraActivity.this).execute();
        }
    };

    @Override
    public void onEmotionResult(@NonNull final List<RecognizeResult> results,
                                @NonNull final byte[] image) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);

        EmotionApiListAdapter.Item[] items = new EmotionApiListAdapter.Item[results.size()];

        for (int i = 0; i < results.size(); i++) {
            final RecognizeResult result = results.get(i);
            final Bitmap face = Bitmap.createBitmap(bitmap,
                    result.faceRectangle.left,
                    result.faceRectangle.top,
                    result.faceRectangle.width,
                    result.faceRectangle.height);

            items[i] = new EmotionApiListAdapter.Item(face, result);
        }

        mResultsList.setVisibility(View.VISIBLE);
        mResultsList.setAdapter(new EmotionApiListAdapter(this, R.layout.list_item_api_result, items));
        showResultsList();
    }

    @Override
    public void onError(@NonNull Exception exception) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.error_connecting_to_api)
                .setMessage(exception.getMessage())
                .show();
    }

    /** Shows the progress spinner. */
    private void showProgressSpinner() {
        mProgressBar.setVisibility(View.VISIBLE);
        mCameraView.stop();
    }

    /** Hides the result ListView by altering its Y-translation only. */
    private void hideResultsListNoAnimation() {
        mResultsList.setTranslationY(mResultsList.getHeight());
    }

    /** Hides the result ListView by animating its Y-translation. */
    private void hideResultsList() {
        mResultsList.animate()
                .translationY(mResultsList.getHeight())
                .withEndAction(() -> mResultsList.setVisibility(View.GONE));
    }

    /** Shows the results ListView by translating its Y-coordinate. */
    private void showResultsList() {
        mResultsList.setVisibility(View.VISIBLE);
        mResultsList.animate().translationY(0);
    }

    /** Resets this activity to the state it was in when the user opened the app */
    private void resetCameraActivity() {
        if (hasCameraPermission()) {
            mCameraView.start();
        }
        else {
            requestCameraPermission();
        }

        mProgressBar.setVisibility(View.GONE);
        mTakePhotoButton.setEnabled(true);
        hideResultsList();
    }

    /** The code representing this fragment's request to use the Camera */
    private static final int PERMISSION_CODE_REQUEST_CAMERA = 1;

    /** The code representing this fragment's request to use the Internet */
    private static final int PERMISSION_CODE_REQUEST_INTERNET = 2;
}
