package net.bendele.preferenceactivityexample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

    private ToggleButton tb;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tb = (ToggleButton) findViewById(R.id.toggle_Button_TTS_available);
        tb.setChecked(MainApp.getTtsAvailable());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    public void onStartClick(View view) {
        startActivity(new Intent(this, PrefsActivity.class));
    }

    public void onTtsAvailableToggle(View view) {
        MainApp.setTtsAvailable(tb.isChecked());
    }
}
