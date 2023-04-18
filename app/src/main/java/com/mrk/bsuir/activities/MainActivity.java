package com.mrk.bsuir.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.mrk.bsuir.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);
        AppCompatButton gameButton = findViewById(R.id.play_button);
        AppCompatButton rulesButton = findViewById(R.id.rules_button);
        gameButton.setOnClickListener(this);
        rulesButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.play_button) {
            Intent intent = new Intent(this, GameActivity.class);
            startActivity(intent);
        }
        else if(view.getId() == R.id.rules_button){
            Uri uri = Uri.parse("https://xchess.ru/" +
                    "kak-igrat-v-shakhmaty-dlya-nachinayushchikh-polnoe-rukovodstvo.html");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    }
}
