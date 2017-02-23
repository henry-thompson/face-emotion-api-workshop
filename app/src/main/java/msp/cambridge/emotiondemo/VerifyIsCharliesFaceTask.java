package msp.cambridge.emotiondemo;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.contract.VerifyResult;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;


/**
 * Asynchronous task calling the Face API to compare the similarity of a face with Charlie's.
 *
 * @author Henry Thompson
 */
public class VerifyIsCharliesFaceTask extends AsyncTask<byte[], Void, VerifyResult> {
    /** Input Stream representing the bytes in the image of Charlie's Face */
    private final InputStream _charliesFace;

    /** The Client for contacting the Face API endpoint. */
    private final FaceServiceClient _faceClient;

    /** The Callback so that the caller can be notified when the requests task is done. */
    private final OnAssessIsCharliesFaceComplete _listener;

    /**
     * If not null, indicates that an exception was thrown during calling the endpoint and that
     * this was the exception thrown.
     */
    private Exception mException;

    /** The bytes representing the JPEG image to be submitted to the Face API endpoint. */
    private byte[] mImage;

    /** Interface for the callback after the VerifyIsCharliesFace asynchronous task completes. */
    public interface OnAssessIsCharliesFaceComplete {
        /**
         * Called after successfully verifying if the face is Charlie's.
         * @param result The verification result.
         * @param image The image for which this verification result was produced.
         */
        void onAssessIsCharliesFaceComplete(@NonNull VerifyResult result, @NonNull byte[] image);

        /**
         * Called after some error occured when verifying if the face is Charlie's.
         * @param exception The exception that was thrown during the assessment of the face.
         */
        void onError(@NonNull Exception exception);
    }

    /**
     * @param charliesFace InputStream representing the bytes constituting the image of Charlie's
     *                     face which we are comparing against.
     * @param faceClient The Client for accessing the Face API endpoints.
     * @param listener Callback so that the caller can be notified when the assessment of the
     *                 image is complete.
     */
    public VerifyIsCharliesFaceTask(@NonNull final InputStream charliesFace,
                                    @NonNull final FaceServiceClient faceClient,
                                    @NonNull final OnAssessIsCharliesFaceComplete listener) {
        _charliesFace = charliesFace;
        _faceClient = faceClient;
        _listener = listener;
    }

    @Override
    protected VerifyResult doInBackground(byte[]... image) {
        throw new UnsupportedOperationException("Unimplemented!");
    }

    @Override
    protected void onPostExecute(VerifyResult result) {
        if (mException != null) {
            _listener.onError(mException);
        }
        else {
            _listener.onAssessIsCharliesFaceComplete(result, mImage);
        }
    }

    /** Exception thrown by VerifyIsCharliesFaceTask when there is not just one face being assesed */
    private class UnexpectedNumberOfFacesException extends Exception {
        UnexpectedNumberOfFacesException(@NonNull final String message) {
            super(message);
        }
    }
}