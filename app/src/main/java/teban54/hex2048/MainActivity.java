package teban54.hex2048;

import android.app.ActionBar;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    // Reference YouTube video: https://www.youtube.com/watch?v=-igAiudpBng
    static String TAG = "MainActivity";

    static String colorScheme = "default";

    private void initializeFonts() {
        // Reference: https://stackoverflow.com/questions/27588965/how-to-use-custom-font-in-android-studio
        Typeface custom_font = Typeface.createFromAsset(getAssets(),  "fonts/ClearSans-Bold.ttf");

        TextView tx = (TextView)findViewById(R.id.gameTitle);
        tx.setTypeface(custom_font);
        tx = (TextView)findViewById(R.id.gameLogo);
        tx.setTypeface(custom_font);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "Sample debug message");

        initializeFonts();

        /*Button sampleButton = (Button) (findViewById(R.id.sampleButton));
        sampleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Clicked");
            }
        });*/
    }
}
