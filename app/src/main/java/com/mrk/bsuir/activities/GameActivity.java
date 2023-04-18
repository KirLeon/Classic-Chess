package com.mrk.bsuir.activities;

import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatButton;

import com.mrk.bsuir.R;
import com.mrk.bsuir.service.GameLogService;
import com.mrk.bsuir.service.Action;
import com.mrk.bsuir.service.MoveService;
import com.mrk.bsuir.model.Board;
import com.mrk.bsuir.model.Color;
import com.mrk.bsuir.model.Piece;
import com.mrk.bsuir.model.impl.Bishop;
import com.mrk.bsuir.model.impl.King;
import com.mrk.bsuir.model.impl.Knight;
import com.mrk.bsuir.model.impl.Pawn;
import com.mrk.bsuir.model.impl.Queen;
import com.mrk.bsuir.model.impl.Rook;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GameActivity extends AppCompatActivity implements View.OnClickListener {

    private Board board;
    private GameLogService logService;
    private MoveService moveService;
    private boolean checkmate;
    private boolean checkMove;
    private static final int PROMOTE_REQUEST_CODE = 1;
    private King playerKing;
    private King enemyPlayerKing;
    private TextView checkmateText;

    final List<Button> buttonList = new ArrayList<>(70);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        this.board = new Board();
        this.logService = new GameLogService(board);
        this.moveService = new MoveService(board, logService);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_game);
        ImageButton undoButton = findViewById(R.id.undo_button);
        undoButton.setOnClickListener(this);
        AppCompatButton menuButton = findViewById(R.id.menu_button);
        menuButton.setOnClickListener(this);
        checkmateText = findViewById(R.id.checkmateText);
        checkmateText.setVisibility(View.INVISIBLE);
        initButtons();
        drawAllBoard();
    }

    private void initButtons() {

        Button button;
        Point point;

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                int id = getResources().getIdentifier
                        ("button" + i + j, "id", getPackageName());

                button = findViewById(id);
                point = new Point(i, j);
                button.setTag(point);

                button.setOnClickListener(this);
                buttonList.add(button);
            }
        }
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.menu_button) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return;
        } else if (view.getId() == R.id.undo_button) {
            undoMove();
            return;
        } else if (view instanceof AppCompatButton) {
            if (checkmate) {
                return;
            }
            Point point = (Point) view.getTag();
            checkMove = false;

            int x = point.x;
            int y = point.y;

            Piece pieceInHand = board.getCurrentPiece();
            Color playerColor = logService.getCurrentMove() % 2 == 0
                    ? Color.WHITE : Color.BLACK;
            List<Action> actionList = new ArrayList<>();

            playerKing = board.getKingOfThisColor(playerColor);
            enemyPlayerKing = board.getKingOfAnotherColor(playerColor);

            if (pieceInHand == null) {

                pieceInHand = board.getPieceFromCell(x, y);
                if (pieceInHand != null && pieceInHand.getColor().equals(playerColor)) {
                    board.setCurrentPiece(pieceInHand);
                }

            } else {

                int startX = board.getPieceCords(pieceInHand)[0];
                int startY = board.getPieceCords(pieceInHand)[1];

                if (isKingUnderCheck(playerKing)) {
                    if (moveService
                            .onCheckAvailableMove(startX, startY, x, y, pieceInHand, playerKing)) {
                        afterMove(startX, startY, x, y, pieceInHand, null, actionList);
                    }
                    board.setCurrentPiece(null);
                } else {

                    if (moveService.allowPieceMove(startX, startY, x, y, pieceInHand)) {

                        if (isEnPassant(pieceInHand, startX, x, y)) {
                            actionList.add(Action.EN_PASSANT);
                        } else if (pieceInHand instanceof King && Math.abs(startX - x) > 1) {
                            actionList.add(x == 2 ? Action.LONG_CASTLING : Action.SHORT_CASTLING);
                        } else if (ifPromotionRequired(pieceInHand, y, playerColor)) {
                            openPromoteActivity(playerColor.getColor(), startX, x);
                            return;
                        }
                        afterMove(startX, startY, x, y, pieceInHand, null, actionList);
                    }
                }
                board.setCurrentPiece(null);
            }
        }
    }

    public void drawAllBoard() {
        AppCompatButton button;
        int id;

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {

                // Finding button by its id and setting image of the found piece to it
                id = getResources().getIdentifier("button" + i + j,
                        "id", getPackageName());
                button = findViewById(id);

                if (board.getPieceFromCell(i, j) == null) {
                    button.setBackgroundDrawable
                            (AppCompatResources.getDrawable(this, R.color.transparent));
                } else {
                    button.setBackgroundDrawable(getPieceDrawable(board.getPieceFromCell(i, j)));
                }
            }
        }
    }

    // Returns the image of the input piece
    public Drawable getPieceDrawable(Piece piece) {
        if (piece instanceof Pawn) {
            return piece.getColor().equals(Color.WHITE)
                    ? AppCompatResources.getDrawable(this, R.drawable.white_pawn)
                    : AppCompatResources.getDrawable(this, R.drawable.black_pawn);
        } else if (piece instanceof Bishop) {
            return piece.getColor().equals(Color.WHITE)
                    ? AppCompatResources.getDrawable(this, R.drawable.white_bishop)
                    : AppCompatResources.getDrawable(this, R.drawable.black_bishop);
        } else if (piece instanceof Knight) {
            return piece.getColor().equals(Color.WHITE)
                    ? AppCompatResources.getDrawable(this, R.drawable.white_knight)
                    : AppCompatResources.getDrawable(this, R.drawable.black_knight);
        } else if (piece instanceof Rook) {
            return piece.getColor().equals(Color.WHITE)
                    ? AppCompatResources.getDrawable(this, R.drawable.white_rook)
                    : AppCompatResources.getDrawable(this, R.drawable.black_rook);
        } else if (piece instanceof Queen) {
            return piece.getColor().equals(Color.WHITE)
                    ? AppCompatResources.getDrawable(this, R.drawable.white_queen)
                    : AppCompatResources.getDrawable(this, R.drawable.black_queen);
        } else if (piece instanceof King) {
            return piece.getColor().equals(Color.WHITE)
                    ? AppCompatResources.getDrawable(this, R.drawable.white_king)
                    : AppCompatResources.getDrawable(this, R.drawable.black_king);
        } else throw new RuntimeException("Cannot find piece drawable");
    }

    public boolean isKingUnderCheck(King king) {

        int kingX = board.getPieceCords(king)[0];
        int kingY = board.getPieceCords(king)[1];
        king.setUnderCheck(moveService.isThisCellUnderAttack(kingX, kingY, king.getColor()));

        return king.isUnderCheck();
    }

    public void afterMove(int startX, int startY, int x, int y, Piece pieceInHand,
                          Piece promotedPiece, List<Action> actions) {

        playerKing.setUnderCheck(false);
        Piece capturedPiece = null;
        boolean moved = false;

        if (actions.contains(Action.LONG_CASTLING)
                || actions.contains(Action.SHORT_CASTLING)) {

            int rookStartX = startX < x ? 7 : 0;
            int rookEndX = rookStartX == 0 ? 3 : 5;
            Rook rook = (Rook) board.getPieceFromCell(rookStartX, startY);

            board.movePiece(startX, startY, x, y, pieceInHand);
            board.movePiece(rookStartX, startY, rookEndX, startY, rook);
        } else {
            if (actions.contains(Action.EN_PASSANT)) {
                int enemyPawnY = y == 5 ? 4 : 3;
                capturedPiece = board.getPieceFromCell(x, enemyPawnY);
                if (capturedPiece == null) {
                    throw new RuntimeException("Error during logging en passant");
                }
                board.placePiece(x, enemyPawnY, null);
            } else {
                if (board.getPieceFromCell(x, y) != null) {
                    actions.add(Action.CAPTURE);
                    capturedPiece = board.getPieceFromCell(x, y);
                }
                if (actions.contains(Action.PROMOTION)) {
                    board.placePiece(startX, startY, null);
                    board.placePiece(x, y, promotedPiece);
                    moved = true;
                }
            }
            if (!moved) {
                board.movePiece(startX, startY, x, y, pieceInHand);
            }
        }

        enemyPlayerKing.setUnderCheck(isKingUnderCheck(enemyPlayerKing));
        checkMove = enemyPlayerKing.isUnderCheck();

        if (checkMove) {
            checkmate = isCheckmate(enemyPlayerKing);
            if (checkmate) {
                actions.add(Action.CHECKMATE);
                checkmateText.setVisibility(View.VISIBLE);
                checkmate = true;
            } else {
                actions.add(Action.CHECK);
            }
        }
        logService.logMove(startX, startY, x, y, pieceInHand, capturedPiece, promotedPiece, actions);
        Log.i("LOG", logService.getLastMove());
        drawAllBoard();
        board.setCurrentPiece(null);
    }

    public boolean ifPromotionRequired(Piece pieceInHand, int y, Color currentMoveColor) {
        return pieceInHand instanceof Pawn &&
                ((currentMoveColor.equals(Color.WHITE) && y == 7) ||
                        (currentMoveColor.equals(Color.BLACK) && y == 0));
    }

    public boolean isEnPassant(Piece pieceInHand, int startX, int endX, int endY) {
        return pieceInHand instanceof Pawn && Math.abs(startX - endX) == 1 &&
                board.getPieceFromCell(endX, endY) == null;
    }

    public boolean isCheckmate(King king) {

        checkmate = moveService.checkCheckmate(king);
        Log.d("CHECKMATE CHECKING: ", "IT IS " + checkmate);

        return checkmate;
    }

    public void undoMove() {
        board.setCurrentPiece(null);
        checkmateText.setVisibility(View.INVISIBLE);
        Map<Integer[], Piece> previousPosition = logService.getPreviousPosition();

        Piece piece;
        for (Integer[] cords : previousPosition.keySet()) {
            piece = previousPosition.get(cords);
            board.placePiece(cords[0], cords[1], piece);
        }
        checkmate = false;
        drawAllBoard();
    }

    public void openPromoteActivity(String color, int startX, int endX) {

        Intent intent = new Intent(this, PromoteActivity.class);
        intent.putExtra("playerColor", color);
        intent.putExtra("startX", startX);
        intent.putExtra("endX", endX);
        intent.putExtra("color", color);

        startActivityForResult(intent, PROMOTE_REQUEST_CODE);
    }

    public void promotePawn(Piece promotedPiece, int startX, int endX, int piecesStartY) {

        int pawnStartY = piecesStartY == 7 ? 6 : 1;
        List<Action> actions = Stream.of(Action.PROMOTION).collect(Collectors.toList());
        Pawn movedPawn = (Pawn) board.getPieceFromCell(startX, pawnStartY);

        afterMove(startX, pawnStartY, endX, piecesStartY, movedPawn, promotedPiece, actions);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PROMOTE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                assert data != null;
                int startX = data.getIntExtra("startX", 0);
                int endX = data.getIntExtra("endX", 0);
                String color = data.getStringExtra("color");
                String chosenPiece = data.getStringExtra("chosenPiece");

                Color pieceColor = color.equals("White") ? Color.WHITE : Color.BLACK;
                int piecesStartY = color.equals("White") ? 7 : 0;

                Piece promotedPiece;

                switch (chosenPiece) {
                    case "Knight": {
                        promotedPiece = new Knight(pieceColor, endX, piecesStartY);
                        break;
                    }
                    case "Bishop": {
                        promotedPiece = new Bishop(pieceColor, endX, piecesStartY);
                        break;
                    }
                    case "Rook": {
                        promotedPiece = new Rook(pieceColor, endX, piecesStartY);
                        break;
                    }
                    case "Queen": {
                        promotedPiece = new Queen(pieceColor, endX, piecesStartY);
                        break;
                    }
                    default: {
                        return;
                    }
                }

                promotePawn(promotedPiece, startX, endX, piecesStartY);

            } else {
                throw new RuntimeException("ERROR DURING ACTIVITY RESULT");
            }
        }
    }
}
