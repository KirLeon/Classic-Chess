package com.mrk.bsuir;

import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatButton;

import com.mrk.bsuir.log.GameLogService;
import com.mrk.bsuir.logic.Action;
import com.mrk.bsuir.logic.MoveService;
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

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Board board;
    private GameLogService logService;
    private MoveService moveService;
    private boolean checkmate;

    final List<Button> buttonList = new ArrayList<>(70);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        this.logService = new GameLogService();
        this.board = new Board();
        this.moveService = new MoveService(board, logService);
        board.setMoveService(moveService);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_game);
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
        if (view instanceof AppCompatButton) {

            AppCompatButton button = (AppCompatButton) view;
            Point point = (Point) button.getTag();

            int x = point.x;
            int y = point.y;

            Log.i("MainActivity", "Нажата кнопка: " + "\n" +
                    "Координаты: " + x + "_" + y);
            Log.i("MainActivity", "Фигура в руке: " + board.getCurrentPiece());

            Piece piece = board.getCurrentPiece();
            if (piece != null) {
                Log.i("Move", "Фигура защищает короля: " + board.getCurrentPiece().isProtectingKing());

                if (board.getPieceCords(piece) == null) {
                    Log.i("BEFORECRUSH: " , x + "" + y + "  " + piece + "  " + board.getPieceFromCell(x,y));
                    throw new RuntimeException("INCORRECT PIECE POSITION");
                }

                int startX = board.getPieceCords(piece)[0];
                int startY = board.getPieceCords(piece)[1];

                if (!moveService.allowPieceMove(startX, startY, x, y, piece)) {
                    Log.w("MOVEMENTS", "Incorrect move for:" + startX + "" + startY +
                            "___" + x + "" + y);
                } else {

                    Log.w("MOVEMENTS", "Correct move for:" + startX + "" + startY +
                            "___" + x + "" + y);

                    logService.logMove(startX, startY, x, y, piece, Action.CHECK);
                    board.movePiece(startX, startY, x, y, piece);
                    board.setCurrentPiece(null);

                    Log.w("AFTERMOVE", "Piece on " + startX + "" + startY + "is" +
                            board.getPieceFromCell(startX, startY));
                }

                King king = piece.getColor().equals(Color.WHITE) ? board.getWhiteKing() :
                        board.getBlackKing();
                if(isCheckmate(king)){
                    board.showCheckmate(king.getColor());
                }

                board.setCurrentPiece(null);
                drawAllBoard();

            } else {

                if (board.getPieceFromCell(x, y) == null) {
                    return;
                }

                if (board.getPieceFromCell(x, y).getColor().ordinal()
                        == logService.getCurrentMove() % 2) {
                    board.setCurrentPiece(board.getPieceFromCell(x, y));
                }

            }

        }
    }

    public void drawAllBoard() {
        AppCompatButton button;
        int id;

        //iterating all over the board
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {

                //finding button by its id and setting image of the found piece to it
                id = getResources().getIdentifier("button" + i + j,
                        "id", getPackageName());
                button = findViewById(id);

                if (board.getPieceFromCell(i, j) == null) {
                    button.setBackgroundDrawable(getDrawable(R.color.transparent));
                    continue;
                } else {
                    button.setBackgroundDrawable(getPieceDrawable(board.getPieceFromCell(i, j)));
                }
            }
        }
    }

    //returns the image of the input piece
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
        } else throw new RuntimeException("Incorrect piece");
    }

    public Action defineAction(int startX, int startY, int endX, int endY, Piece piece) {

//        if (piece instanceof King && Math.abs(startX - endX) > 1) {
//            return endX > startX ? Action.SHORT_CASTLING : Action.LONG_CASTLING;
//        }
//
//        Piece attackedPiece = board.getPieceFromCell(endX, endY);
//
//        if (piece instanceof Pawn) {
//            if (endY == 0 || endY == 8) {
//                return Action.PROMOTION;
//            } else if (attackedPiece == null) {
//                return Action.EN_PASSANT;
//            }
//        }
//        else if(attackedPiece != null){
//            return Action.CAPTURE;
//        }

        //TODO Finish the action, add the ability of varargs
        return null;
    }

    public boolean isCheckmate(King king){

        int kingX = board.getPieceCords(king)[0];
        int kingY = board.getPieceCords(king)[1];

        checkmate = moveService.checkCheckmate(king, kingX, kingY);
        Log.d("CHECKMATE CHECKING: ", "IT IS " + checkmate);

        return checkmate;
    }
}
