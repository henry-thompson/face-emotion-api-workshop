package msp.cambridge.facedemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.microsoft.projectoxford.emotion.contract.RecognizeResult;


/**
 * A list adapter for showing the results of the API call to the user
 *
 * @author Henry Thompson
 */
public class EmotionApiListAdapter extends ArrayAdapter<EmotionApiListAdapter.Item> {
    public EmotionApiListAdapter(Context context, int resource, Item[] items) {
        super(context, resource, items);
    }

    @Override
    public View getView(final int position,
                        @Nullable View convertView,
                        @Nullable final ViewGroup parent) {

        if (convertView == null) {
            final LayoutInflater inflater;
            inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item_api_result, null);
        }

        final TextView resultTitle = (TextView) convertView.findViewById(R.id.list_item_api_result_title);
        final TextView resultText = (TextView) convertView.findViewById(R.id.list_item_results);
        final ImageView resultImage = (ImageView) convertView.findViewById(R.id.list_item_api_image);

        resultTitle.setText("Face #" + (position + 1));

        final RecognizeResult result = getItem(position).getRecognizeResult();
        final Bitmap face = getItem(position).getFaceImage();

        final StringBuilder builder = new StringBuilder();

        builder.append("Anger: " + result.scores.anger + "\n");
        builder.append("Contempt: " + result.scores.contempt + "\n");
        builder.append("Disgust: " + result.scores.disgust + "\n");
        builder.append("Fear: " + result.scores.fear + "\n");
        builder.append("Happiness: " + result.scores.happiness + "\n");
        builder.append("Neutral: " + result.scores.neutral + "\n");
        builder.append("Sadness: " + result.scores.sadness + "\n");
        builder.append("Surprise: " + result.scores.surprise + "\n");

        resultText.setText(builder.toString());

        resultImage.setImageBitmap(face);

        return convertView;
    }

    /**
     * Represents a single item in the ListView showing the result of an Emotion API query to the
     * user.
     */
    public static class Item {
        /** The bitmap showing the face that was detected. */
        private final Bitmap _face;

        /** The result from the Emotion API to be shown in this list item */
        private final RecognizeResult _result;

        /**
         * @param face The image of the face that was found by the Emotion API.
         * @param result The Emotion API result of the face image as specified by <code>face</code>.
         */
        public Item(Bitmap face, RecognizeResult result) {
            _face = face;
            _result = result;
        }

        /** Get the bitmap showing the face that was detected. */
        public Bitmap getFaceImage() {
            return _face;
        }

        /** Get the result from the Emotion API to be shown in this list item */
        public RecognizeResult getRecognizeResult() {
            return _result;
        }
    }
}