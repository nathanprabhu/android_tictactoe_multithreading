package com.prabhunathan.cs478.p4.cs478_proj4;

import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.util.Random;

public class MainActivity extends AppCompatActivity{

    protected Button button;
    protected TextView textview;
    protected int[][] cellsID = {{R.id.cell11,R.id.cell12, R.id.cell13}, {R.id.cell21, R.id.cell22, R.id.cell23}, {R.id.cell31, R.id.cell32, R.id.cell33}};
    protected char[][] cellsValue={{'.','.','.'},{'.','.','.'},{'.','.','.'}};
    protected int[] index = {0, 1, 2}; //Random selelction

    private final Object lock = new Object();

    protected int currentTurn=0; //0 - Thread A, 1 - Thread B
    protected Handler tAHandle=new Handler();
    protected Handler tBHandle=new Handler();

    Thread tA = new Thread(new ThreadA());
    Thread tB = new Thread(new ThreadB());

    //Synchronized lock to identify the current turn & to let the other thread know its turn ended
    protected void setTurn(int x){
        synchronized(lock){
            currentTurn=x;
        }
    }

    //Clears Board, Resets Game Stats and Starts New Game
    protected void clearUI(){
        TextView cell11=(TextView) findViewById(cellsID[0][0]);
        TextView cell12=(TextView) findViewById(cellsID[0][1]);
        TextView cell13=(TextView) findViewById(cellsID[0][2]);
        TextView cell21=(TextView) findViewById(cellsID[1][0]);
        TextView cell22=(TextView) findViewById(cellsID[1][1]);
        TextView cell23=(TextView) findViewById(cellsID[1][2]);
        TextView cell31=(TextView) findViewById(cellsID[2][0]);
        TextView cell32=(TextView) findViewById(cellsID[2][1]);
        TextView cell33=(TextView) findViewById(cellsID[2][2]);
        button.setText("Restart");
        cellsValue= new char[][]{{'.','.','.'},{'.','.','.'},{'.','.','.'}};
        cell11.setText("T");
        cell12.setText("I");
        cell13.setText("C");
        cell21.setText("T");
        cell22.setText("A");
        cell23.setText("C");
        cell31.setText("T");
        cell32.setText("O");
        cell33.setText("E");
        try{
            Thread.sleep(1000);
        }catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        cell11.setText(".");
        cell12.setText(".");
        cell13.setText(".");
        cell21.setText(".");
        cell22.setText(".");
        cell23.setText(".");
        cell31.setText(".");
        cell32.setText(".");
        cell33.setText(".");
    }

    //Makes New Move by Handling messages from thread
    protected void makeMove(){
        tAHandle = new Handler() {
            //Gets position from msg and 'A' makes a move in that position
            @Override
            public void handleMessage(Message msg) {
                cellsValue[msg.arg1][msg.arg2]='X';
                final TextView currentValue=(TextView) findViewById(cellsID[msg.arg1][msg.arg2]);
                //Communicates to UI thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        currentValue.setText("X");
                        currentValue.setTextColor(Color.BLUE);
                    }
                });
                //Turn Ended, waits for opponent to make a move
                setTurn(1);
            }
        };
        tBHandle = new Handler() {
            //Gets position from msg and 'B' makes a move in that position
            @Override
            public void handleMessage(Message msg) {
                cellsValue[msg.arg1][msg.arg2]='O';
                final TextView currentValue=(TextView) findViewById(cellsID[msg.arg1][msg.arg2]);
                //Communicates to UI thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        currentValue.setText("O");
                        currentValue.setTextColor(Color.RED);
                    }
                });
                //Turn Ended, waits for opponent to make a move
                setTurn(0);
            }
        };
        //Initializing threads
        tA = new Thread(new ThreadA());
        tB = new Thread(new ThreadB());
        //Checks if the threads are already running
        if (tA.getState() == Thread.State.NEW)
        {
            tA.start();
        }
        if (tB.getState() == Thread.State.NEW)
        {
            tB.start();
        }
    }
    //Generated Random value from the array
    protected int getRandomPos(int[] array) {
        int rand = new Random().nextInt(array.length);
        return array[rand];
    }
    //Assigns a Random Position in the Board to requesting thread
    protected Message getPosition(Message m){
        while(true){
            int x = getRandomPos(index);
            int y = getRandomPos(index);
            if(cellsValue[x][y]=='.'){
                m.arg1 = x;
                m.arg2 = y;
                break;
            }
        }
        return m;
    }
    //Checks whether the game ended and identifies Winner / ended in a Tie
    public boolean isGameOver() {
        //Horizontal Row Check for Winner
        for (int i = 0; i < 3; i++) {
            if (cellsValue[i][0] == cellsValue[i][1] && cellsValue[i][1] == cellsValue[i][2]) {
                if (cellsValue[i][0] == '.') {
                    return false;
                } else if (cellsValue[i][0] == 'X') {
                    //Communicates Winner to UI Thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textview.setText("THREAD A (X) WINS");
                        }
                    });
                    return true;
                } else if (cellsValue[i][0] == 'O'){
                    //Communicates Winner to UI Thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textview.setText("THREAD B (O) WINS");
                        }
                    });
                    return true;
                }
            }
        }
        //Vertical Column Check for Winner
        for (int i = 0; i < 3; i++) {
            if (cellsValue[0][i] == cellsValue[1][i] && cellsValue[1][i] == cellsValue[2][i]) {
                if (cellsValue[0][i] == '.') {
                    return false;
                } else if (cellsValue[0][i] == 'X') {
                    //Communicates Winner to UI Thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textview.setText("THREAD A (X) WINS");
                        }
                    });
                    return true;
                } else if (cellsValue[0][i] == 'O'){
                    //Communicates Winner to UI Thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textview.setText("THREAD B (O) WINS");
                        }
                    });
                    return true;
                }
            }
        }
        //Standard Diagonal Check for Winner
        if (cellsValue[0][0] == cellsValue[1][1] && cellsValue[1][1] == cellsValue[2][2]){
            if (cellsValue[0][0] == '.') {
                return false;
            } else if (cellsValue[0][0] == 'X') {
                //Communicates Winner to UI Thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textview.setText("THREAD A (X) WINS");
                    }
                });
                return true;
            } else if (cellsValue[0][0] == 'O'){
                //Communicates Winner to UI Thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textview.setText("THREAD B (O) WINS");
                    }
                });
                return true;
            }
        }
        //Reverse Diagonal Check for Winner
        if (cellsValue[2][0] == cellsValue[1][1] && cellsValue[1][1] == cellsValue[0][2]){
            if (cellsValue[2][0] == '.') {
                return false;
            } else if (cellsValue[2][0] == 'X') {
                //Communicates Winner to UI Thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textview.setText("THREAD A (X) WINS");
                    }
                });
                return true;
            } else if (cellsValue[2][0] == 'O'){
                //Communicates Winner to UI Thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textview.setText("THREAD B (O) WINS");
                    }
                });
                return true;
            }
        }
        //Condition for Game Tie
        if( (cellsValue[0][0] == 'O' || cellsValue[0][0]== 'X') &&
            (cellsValue[0][1] == 'O' || cellsValue[0][1]== 'X') &&
            (cellsValue[0][2] == 'O' || cellsValue[0][2]== 'X') &&
            (cellsValue[1][0] == 'O' || cellsValue[1][0]== 'X') &&
            (cellsValue[1][1] == 'O' || cellsValue[1][1]== 'X') &&
            (cellsValue[1][2] == 'O' || cellsValue[1][2]== 'X') &&
            (cellsValue[2][0] == 'O' || cellsValue[2][0]== 'X') &&
            (cellsValue[2][1] == 'O' || cellsValue[2][1]== 'X') &&
            (cellsValue[2][2] == 'O' || cellsValue[2][2]== 'X')
            ){
            //Communicates Winner to UI Thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textview.setText("GAME TIED. RESTART");
                }
            });
            return true;
        }
        //Default Case - Game In Progress
        return false;
    }
    //App execution Starts here - On Create
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Sets Title of App
        setTitle("Tic Tac Toe");
        button = (Button) findViewById(R.id.button);
        textview = (TextView)findViewById(R.id.textView61);
        //Listener to button - Starts Game on First App Load
        //Restarts Game subsequently by killing existing threads, Starting new threads
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tA = new Thread(new ThreadA());
                tB = new Thread(new ThreadB());
                clearUI();
                textview.setText("GAME IN PROGRESS");
                //Kills threads when restarting, checks if threads are alive before killing
                if (tA.isAlive ()){
                    tA.interrupt();
                }
                if (tB.isAlive ()){
                    tB.interrupt();
                }
                //Starts new threads, checks if its not existing already
                if (tA.getState() == Thread.State.NEW)
                {
                    tA.start();
                }
                if (tB.getState() == Thread.State.NEW)
                {
                    tB.start();
                }
                //setTurn(0);
            }
        });

    }
    //Thread A Class
    protected class ThreadA implements Runnable{
        //Thread execution Starts here
        @Override
        public void run() {
            //Looper to handle multiple messages
            Looper.prepare();
            //Wait till opponent move is complete and current game is not ended
            while (currentTurn==0 && !isGameOver()) {
            try {
                //Delay 1s, before making a move
                try {
                    Thread.sleep(1000);
                }
                catch(InterruptedException e){
                    System.err.println("An InterruptedException was caught: " + e.getMessage());
                }
                //Gets random position in msg.arg1 and msg.arg2
                Message msg = Message.obtain();
                msg = getPosition(msg);
                //Sends position(move) to thread 'A' handler
                tAHandle.sendMessage(msg);
                //Indicates move has ended
                setTurn(1);
                //Makes a move in Board
                makeMove();
            } catch (Exception e) {
                e.printStackTrace();
            }
            }
            //Checks if game has ended to kill thread A
            if(isGameOver()){
                //Kill Thread A
                Thread.currentThread().interrupt();
                tA=null;
            }
            //Runs message queue in this thread
            Looper.loop();
        }
    }
    //Thread B Class
    protected class ThreadB implements Runnable{
        //Thread execution Starts here
        @Override
        public void run() {
            //Looper to handle multiple messages
            Looper.prepare();
            //Wait till opponent move is complete and current game is not ended
            while (currentTurn==1 && !isGameOver()) {
            try {
                //Delay 1s, before making a move
                try {
                    Thread.sleep(1000);
                }
                catch(InterruptedException e){
                    System.err.println("An InterruptedException was caught: " + e.getMessage());
                }
                //Gets random position in msg.arg1 and msg.arg2
                Message msg = Message.obtain();
                msg = getPosition(msg);
                //Sends position(move) to thread 'A' handler
                tBHandle.sendMessage(msg);
                //Indicates move has ended
                setTurn(0);
                //Makes a move in Board
                makeMove();
            } catch (Exception e) {
                e.printStackTrace();
            }
            }
            //Checks if game has ended to kill thread B
            if(isGameOver()){
                //Kill Thread A
                Thread.currentThread().interrupt();
                tB=null;
            }
            //Runs message queue in this thread
            Looper.loop();
        }
    }

}