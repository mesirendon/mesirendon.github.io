package com.example.hells.androidtic_tac_toe;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;
import android.widget.Toast;

public class AndroidTicTacToeActivity extends AppCompatActivity {

    static final int DIALOG_DIFFICULTY_ID = 0;
    static final int DIALOG_QUIT_ID = 1;
    MediaPlayer mHumanMediaPlayer;
    MediaPlayer mComputerMediaPlayer;
    private SharedPreferences mPrefs;
    private TicTacToeGame mGame;
    private TextView mInfoTextView;
    private TextView mHumanScore;

    private TextView mTiesScore;

    private TextView mAndroidScore;
    private TextView mDebugTextView;
    private boolean mGameOver;
    private BoardView mBoardView;

    protected Dialog onCreateDialog(int id){
        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        switch(id){
            case DIALOG_DIFFICULTY_ID:
                builder.setTitle(R.string.difficulty_choose);

                final CharSequence[] levels = {
                        getResources().getString(R.string.difficulty_easy),
                        getResources().getString(R.string.difficulty_harder),
                        getResources().getString(R.string.difficulty_expert)
                };

                int selected = 0;
                TicTacToeGame.DifficultyLevel selectedDif = mGame.getDifficultyLevel();
                if(selectedDif == TicTacToeGame.DifficultyLevel.Easy)
                    selected = 0;
                else if(selectedDif == TicTacToeGame.DifficultyLevel.Harder)
                    selected = 1;
                else
                    selected = 2;
                builder.setSingleChoiceItems(levels, selected, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int itemSelected) {
                        dialog.dismiss();
                        if(itemSelected == 0 )
                            mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Easy);
                        else if(itemSelected == 1)
                            mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Harder);
                        else
                            mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Expert);

                        Toast.makeText(getApplicationContext(), levels[itemSelected], Toast.LENGTH_SHORT).show();
                    }
                });
                dialog = builder.create();
                break;
            case DIALOG_QUIT_ID:
                builder.setMessage(R.string.quit_question)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                AndroidTicTacToeActivity.this.finish();
                            }
                        })
                        .setNegativeButton(R.string.no, null);
                dialog = builder.create();;
                builder.setCancelable(false);
                break;
        }
        return dialog;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = getSharedPreferences("ttt_prefs", MODE_PRIVATE);
        setContentView(R.layout.activity_android_tic_tac_toe);
        mGame = new TicTacToeGame(mPrefs.getInt("mHumanWins", 0), mPrefs.getInt("mComputerWins", 0), mPrefs.getInt("mTies", 0));
        mBoardView = (BoardView) findViewById(R.id.board);
        mBoardView.setGame(mGame);
        mBoardView.setOnTouchListener(mTouchListener);

        mInfoTextView = (TextView) findViewById(R.id.information);
        mHumanScore = (TextView) findViewById(R.id.humanScore);
        mTiesScore = (TextView) findViewById(R.id.tiesScore);
        mAndroidScore = (TextView) findViewById(R.id.androidScore);

        mHumanScore.setText("" + mGame.getHumanScore());
        mTiesScore.setText("" + mGame.getTiesScore());
        mAndroidScore.setText("" + mGame.getAndroidScore());
        startNewGame();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    private void resetScores() {
        mGame.setHumanScore(0);
        mGame.setTiesScore(0);
        mGame.setAndroidScore(0);
        mHumanScore.setText("" + mGame.getHumanScore());
        mTiesScore.setText("" + mGame.getTiesScore());
        mAndroidScore.setText("" + mGame.getAndroidScore());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_game:
                startNewGame();
                return true;
            case R.id.ai_difficulty:
                showDialog(DIALOG_DIFFICULTY_ID);
                return true;
            case R.id.resetScores:
                resetScores();
                return true;
            case R.id.quit:
                showDialog(DIALOG_QUIT_ID);
                return true;
        }
        return false;
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Save the current scores
        SharedPreferences.Editor ed = mPrefs.edit();
        ed.putInt("mHumanWins", mGame.getHumanScore());
        ed.putInt("mComputerWins", mGame.getAndroidScore());
        ed.putInt("mTies", mGame.getTiesScore());
        ed.commit();
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mGame.setBoardState(savedInstanceState.getCharArray("board"));
        mGameOver = savedInstanceState.getBoolean("mGameOver");
        mInfoTextView.setText(savedInstanceState.getCharSequence("info"));
       // mGame.setHumanScore(savedInstanceState.getInt("mHumanWins"));
        mHumanScore.setText(mGame.getHumanScore()+"");
        //mGame.setAndroidScore(savedInstanceState.getInt("mComputerWins"));
        mAndroidScore.setText(mGame.getAndroidScore()+"");
        //mGame.setTiesScore(savedInstanceState.getInt("mTies"));
        mTiesScore.setText(mGame.getTiesScore()+"");

        //mHumanScore.setText("" + mGame.getHumanScore());
        //mTiesScore.setText("" + mGame.getTiesScore());
        //mAndroidScore.setText("" + mGame.getAndroidScore());
       // mGoFirst = savedInstanceState.getChar("mGoFirst");
    }

    private void startNewGame(){
        this.mGameOver = false;

        mGame.clearBoard();
        mBoardView.invalidate();

    }

    private boolean setMove(char player, int location) {
        if (player == TicTacToeGame.HUMAN_PLAYER){
            mHumanMediaPlayer.start();    // Play the sound effect
        } else {
            mComputerMediaPlayer.start();
        }
        if (mGame.setMove(player, location)) {
            mBoardView.invalidate();   // Redraw the board
            return true;
        }
        return false;
    }


    // Listen for touches on the board
    private OnTouchListener mTouchListener = new OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {

            // Determine which cell was touched
            int col = (int) event.getX() / mBoardView.getBoardCellWidth();
            int row = (int) event.getY() / mBoardView.getBoardCellHeight();
            int pos = row * 3 + col;

            if (!mGameOver && setMove(TicTacToeGame.HUMAN_PLAYER, pos))	{
                setMove(TicTacToeGame.HUMAN_PLAYER, pos);

                //if no winner yet, let the computer make a move
                int winner = mGame.checkForWinner();
                if (winner == 0) {
                    mInfoTextView.setText(R.string.turn_computer);
                    int move = mGame.getComputerMove();
                    setMove(TicTacToeGame.COMPUTER_PLAYER, move);
                    winner = mGame.checkForWinner();
                }

                if (winner == 0)
                    mInfoTextView.setText(R.string.turn_human);
                else if (winner == 1) {
                    mGameOver = true;
                    mGame.increaseTiesScore();
                    mInfoTextView.setText(R.string.result_tie);
                    mTiesScore.setText("" + mGame.getTiesScore());
                } else if (winner == 2) {
                    mGameOver = true;
                    mGame.increaseHumanScore();
                    mInfoTextView.setText(R.string.result_human_wins);
                    mHumanScore.setText("" + mGame.getHumanScore());
                } else {
                    mGameOver = true;
                    mGame.increaseAndroidScore();
                    mInfoTextView.setText(R.string.result_computer_wins);
                    mAndroidScore.setText("" + mGame.getAndroidScore());
                }
            }

// So we aren't notified of continued events when finger is moved
            return false;
        }
    };
    protected void onResume() {
        super.onResume();

        mHumanMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.b);
        mComputerMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.f);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mHumanMediaPlayer.release();
        mComputerMediaPlayer.release();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putCharArray("board", mGame.getBoardState());
        outState.putBoolean("mGameOver", mGameOver);
        outState.putInt("mHumanWins", mGame.getHumanScore());
        outState.putInt("mComputerWins", mGame.getAndroidScore());
        outState.putInt("mTies", mGame.getTiesScore());
        outState.putCharSequence("info", mInfoTextView.getText());
    }

}
