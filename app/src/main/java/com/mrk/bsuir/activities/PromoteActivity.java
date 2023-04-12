package com.mrk.bsuir.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.mrk.bsuir.R;


public class PromoteActivity extends AppCompatActivity {

    private String playerColor;
    private String chosenPiece;
    private int startX;
    private int endX;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promote);

        // Initialize buttons and set icons
        AppCompatButton knightButton = findViewById(R.id.knight_button);
        AppCompatButton bishopButton = findViewById(R.id.bishop_button);
        AppCompatButton rookButton = findViewById(R.id.rook_button);
        AppCompatButton queenButton = findViewById(R.id.queen_button);

        startX = getIntent().getIntExtra("startX", 0);
        endX = getIntent().getIntExtra("endX", 0);


        // Get player color from main activity
        playerColor = getIntent().getStringExtra("playerColor");

        if (!playerColor.equals("White")) {
            knightButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.black_knight, 0, 0, 0);
            bishopButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.black_bishop, 0, 0, 0);
            rookButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.black_rook, 0, 0, 0);
            queenButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.black_queen, 0, 0, 0);
        }

        // Set click listeners for each button
        knightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chosenPiece = "Knight";
                returnResult();
            }
        });

        bishopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chosenPiece = "Bishop";
                returnResult();
            }
        });

        rookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chosenPiece = "Rook";
                returnResult();
            }
        });

        queenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chosenPiece = "Queen";
                returnResult();
            }
        });
    }

    // Return player's choice and finish activity
    private void returnResult() {

        Intent resultIntent = new Intent();

        resultIntent.putExtra("chosenPiece", chosenPiece);
        resultIntent.putExtra("color", playerColor);
        resultIntent.putExtra("startX", startX);
        resultIntent.putExtra("endX", endX);

        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
