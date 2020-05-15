package sk.fei.videoeditor.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import sk.fei.videoeditor.R;

public class AboutActivity extends AppCompatActivity {

    private TextView title, text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        title = findViewById(R.id.titleAbout);
        text = findViewById(R.id.textAbout);

        title.setText("Efektívne spracovanie videa na mobilnom zariadnení Android");

        text.setText("Táto práca vznikla v spolupráci ...............");


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

}
