import java.io.File;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.Arrays;
import java.io.*;
import java.util.*;
import java.text.DecimalFormat;
import java.util.Collections;


public class timmockPlayer {
  
  //Declare the variables
  private static final int positiveNumber = 1000; //maximum number possible
  private static final int negativeNumber = -1000; //minimum number possible
  private static final int MAX_DEPTH = 20; //How many levels deep to go on the AB Pruning
  
  public static void main(String[] args) {
    System.out.println(readString(args[0]));
  }
  
  
  //Reads the string passed in and generates the board/last move
  public static String readString(String string){
    
    //Split the string passed in into tokens
    String delims = "\\[|\\]";
    String[] tokens = string.split(delims);
    
    
    //Remove all empty spaces.
    int j = 0;
    for( int i=0;  i<tokens.length;  i++ )
    {
      if (tokens[i].equals("")==false)
        tokens[j++] = tokens[i];
    }
    String [] newArray = new String[j];
    System.arraycopy( tokens, 0, newArray, 0, j );
    
    int size = newArray.length-1;
    
    //Convert into a matrix
    int matrix [][] = new int [size][size];
    
    //Convert characters to ints and fill matrix
    for (int i=0;  i<size; i++){
      for (int z =0 ; z<newArray[i].length(); z++){
        matrix[i][z]= Integer.parseInt(Character.toString(newArray[i].charAt(z)));
      }      
    }
    
    //Make sure all numbers that arent part of the board are invalid (4)
    for (int i=0;  i<size; i++){
      for (int z =0 ; z < size - newArray[i].length(); z++){
        matrix[i][z + newArray[i].length()]= 4;
      }     
    }
    
    int [] playerMove =  new int [4]; //The last move
    
    //If we have the first move, populate our player move with -1s.
    if (newArray[newArray.length - 1].equals("LastPlay:null")){
      for (int i =0; i< playerMove.length; i++){
        playerMove[i] = -1;
      }
    }else{
      //split the last play up.
      delims = "\\(|\\)";
      String[] tokens2 = newArray[newArray.length - 1].split(delims);
      char [] tokens3 = tokens2[tokens2.length -1 ].toCharArray();
      
      //split by commas and remove spaces
      int x = 0;
      for( int i=0;  i<tokens3.length;  i++ )
      {
        if (Character.toString(tokens3[i]).equals(",")==false)
          tokens3[x++] = tokens3[i];
      }
      char [] newArray2 = new char[x];
      System.arraycopy( tokens3, 0, newArray2, 0, x );
      
      //convert to ints
      for (int i =0; i<playerMove.length; i++){
        playerMove[i]=Integer.parseInt(Character.toString((newArray2[i])));  
      }
    }
    
    //Find the best move
    String bestMove = move(matrix, playerMove);    
    return  bestMove;
  }
  
  //returns best possible move
  public static String move(int [][] board, int [] playerMove){
    //Find coordinates of the move
    int playerX = playerMove[1];    
    int playerY = playerMove[2];
    int playerZ = playerMove[3];
    
    //initialize max to a loss
    int max = negativeNumber;
    //initialize best move to 0,0,0,0
    int [] bestMove =  {0,0,0,0};
    int val = max;
    
    //Find all available moves based on the last move
    int [][] avail = avail(board, playerMove);
    //Go through each option
    for (int i=0; i<avail[0].length; i++){
      //Go through each color
      for(int color = 1; color <=3; color++){
        //If coordinates are valid
        if(avail[0][i] < 99){
          //color the coordinate with color
          board[avail[0][i]][avail[1][i]] = color;
          //Check to see if you have lost yet if not, then use minmax
          if (canMove(board)!=false){
            //Find the x, y, and z coordinates
            int x = board.length-avail[0][i]-1;
            int y = avail[1][i];
            //Populate next move based on values given
            int [] nextMove ={color, x, y, board.length - x - y};
            //find the max value of that move
            val = max(val, minMax(board, nextMove, 1, negativeNumber, positiveNumber));

            //If its a win, set as best move and remove the color
            if (eval(board, nextMove) == positiveNumber) {
              bestMove[0] = color;
              bestMove[1] = board.length-avail[0][i]-1;
              bestMove[2] = avail[1][i];
              bestMove[3] = board.length - (board.length-avail[0][i]-1) - avail[1][i];
              board[avail[0][i]][avail[1][i]] = 0;
            }
            
            //reset max and bestMove according to highest value
            if (val > max) {
              bestMove[0] = color;
              bestMove[1] = board.length-avail[0][i]-1;
              bestMove[2] = avail[1][i];
              bestMove[3] = board.length - (board.length-avail[0][i]-1) - avail[1][i];
              max = val;
            }
          }
          //Reset the board position to 0
          board[avail[0][i]][avail[1][i]] = 0;
        }
      }
    }
    
    //make any move if none are available
    //if(bestMove[0] == 0 && bestMove[1] == 0){
      //return "(0,0,0,0)";
    //}
    
    //Convert int array into string.
    String builder="(";    
    for (int i=0; i<bestMove.length; i++){
      if (i!=3){
        builder=builder+Integer.toString(bestMove[i])+",";
      }
      else{
        builder=builder+Integer.toString(bestMove[i])+")";
      }
    }
    String best;
    best=builder;
    
    //return the best move
    return best;
  }
  
  
  //Evaluate board per move
  public static int eval(int[][] board, int[] lastMove){
    
    //If the right side is color 1, then return
    int counter =1;
    for (int i=1; i<board.length-1; i++){
      if (board[i][counter] ==1){
        return negativeNumber;
      }
      counter ++;
    }
    
    //If the left side is color 2, then return
    for (int i=0; i<board.length-1; i++){
      if (board[i][1] == 2){
        return negativeNumber;
      }
    }
    
    //If the bottom row is color 3, then return
    for (int j=1; j<board[board.length-2].length; j++){
      if (board[board.length-2][j] ==3){
        return negativeNumber;
      }
    }
    
    //Scan Board to see if you lost.
    int rowAdjuster = 3;
    for (int i =1; i<board.length-1; i++){
      for (int j=1; j<rowAdjuster-1; j++){
        for (int k = 1; k <= 3; k++){
          int x = 0;
          int y = 0;
          
          if(k == 1){ //Variables when color = 1
            x = 2;
            y = 3;
          }else if(k == 2){ //Variables when color = 2
            x = 1;
            y = 3;
          }else if(k == 3){ //Variables when color = 3
            x = 1;
            y = 2;
          }
          
          if(board[i][j]==k){
            //Check the top right corner. 
            if ((board[i-1][j]==x && board[i][j+1]==y) || (board[i-1][j]==y && board[i][j+1]==x)  ){
              return negativeNumber;
            }
            //Check the bottom right corner. 
            if ((board[i+1][j+1]==x && board[i][j+1]==y) || (board[i+1][j+1]==y && board[i][j+1]==x)  ){
              return negativeNumber;
            }
            //Check the bottom left corner. 
            if ((board[i+1][j]==x && board[i][j-1]==y) || (board[i+1][j]==y && board[i][j-1]==x)  ){
              return negativeNumber;
            }
            //Check the top left corner. 
            if ((board[i-1][j-1]==x && board[i][j-1]==y) || (board[i-1][j-1]==y && board[i][j-1]==x)  ){
              return negativeNumber;
            }
            //Check the bottom two. 
            if ((board[i+1][j+1]==x && board[i+1][j]==y) || (board[i+1][j+1]==y && board[i+1][j]==x)  ){
              return negativeNumber;
            }
            //Check the top two. 
            if ((board[i-1][j-1]==x && board[i-1][j]==y) || (board[i-1][j-1]==y && board[i-1][j]==x)  ){
              return negativeNumber;
            }
          }
        }
      }
      rowAdjuster++;
    }
       
    //Get the index of the last move
    int playerX = board.length-lastMove[1]-1;
    int playerY = lastMove[2];
   
    //See how many of the other options are filled. The more that are filled, the higher the score of this move
    //Similar approach to finding all options
    int score = 0;
    if(playerY != -1){
      //Above right
      if(board[playerX+1][playerY]!=0){
        score++;
      }
      //Below right
      if(board[playerX-1][playerY]!=0){
        score++;
      }
      //Left
      if(board[playerX][playerY-1]!=0){
        score++;
      }
      //Right
      if(board[playerX][playerY+1]!=0){
        score++;
      }
      //Diagonal Left
      if(board[playerX-1][playerY-1]!=0){
        score++;
      }
      //Diagonal Right
      if(board[playerX+1][playerY+1]==0){
        score++;
      }
    }
    return score;
  }
  
  //MinMax method for AB Pruning
  public static int minMax(int [][]board, int []playerMove, int depth, int alpha, int beta) {
    //if the playerMove is done, one winner has won, or it has reach the max depth return the evaluation
    if( depth == MAX_DEPTH || eval(board, playerMove) == negativeNumber || 
       eval(board, playerMove) == positiveNumber){ 
      return eval(board, playerMove);
    }     
    //if it is the AI's turn
    else if (depth % 2 == 0){
      int [][] avail = avail(board, playerMove); //Get all available moves
      int val = negativeNumber;  //set val to indicate loss
      for (int i=0; i<avail[0].length; i++){ //Go through all options
        for(int color = 1; color <=3; color++){ //Go through all colors 
          if(avail[0][i] < 99){ //if the coordinates are valid
            board[avail[0][i]][avail[1][i]] = color;//Color the coordinate
            if (canMove(board)!=false){//Make sure you havent lost
              //Get the coordinates and populate the next move
              int x = board.length-avail[0][i]-1;
              int y = avail[1][i];
              int [] nextMove ={color, x, y, board.length - x - y};
              //find the max value of that move
              val = max(val, minMax(board, nextMove, depth + 1, alpha, beta));
              //set alpha
              alpha = max(alpha, val);
              //If alpha and beta overlap, then finish the reccurence
              if(beta < alpha){
                board[avail[0][i]][avail[1][i]] = 0;
                return alpha;
              }
            }
            board[avail[0][i]][avail[1][i]] = 0;//Reset the coordinate to 0
          }
        }
        return alpha; //Return the alpha
      }
    }else{
      //The same as even, except using the minimum moves
      int [][] avail = avail(board, playerMove);
      int val = positiveNumber;  //set val to indicate loss
      for (int i=0; i<avail[0].length; i++){
        for(int color = 1; color <=3; color++){
          if(avail[0][i] < 99){
            board[avail[0][i]][avail[1][i]] = color;
            if (canMove(board)!=false){
              int x = board.length-avail[0][i]-1;
              int y = avail[1][i];
              int [] nextMove ={color, x, y, board.length - x - y};
              val = min(val, minMax(board, nextMove, depth + 1, alpha, beta));
              beta = min(beta, val);
              if(beta < alpha){
                board[avail[0][i]][avail[1][i]] = 0;
                return beta;
              }
            }
            board[avail[0][i]][avail[1][i]] = 0;
          }
        }
        return beta;
      }
    }
    return -1;
  }
  
  //determine the max
  public static int max(int alpha, int val){
    if(val>alpha)
      return val;
    else 
      return alpha;
  }
  
  //determine the min
  public static int min(int beta, int val){
    if(val<beta)
      return val;
    else 
      return beta;
  }
  
  //Find all available moves on the board according to the last move.
  public static int [][] avail(int [] [] board, int [] playerMove){
    //If there was a previous move.
    if(playerMove[2] != -1){
      //get index of move
      int playerX = board.length-playerMove[1]-1;
      int playerY = playerMove[2];
      int [][] avail = new int[2][6];
      //Go through every possibility to find the coordinates
      //Above right
      if(board[playerX+1][playerY] == 0){
        avail[0][0] = playerX+1;
        avail[1][0] = playerY;
      }else{
        avail[0][0] = 100;
        avail[1][0] = 100;
      }//Below Right
      if(board[playerX-1][playerY] == 0){
        avail[0][1] = playerX-1;
        avail[1][1] = playerY;     
      }else{
        avail[0][1] = 100;
        avail[1][1] = 100;
      }//Left
      if(board[playerX][playerY-1] == 0){
        avail[0][2] = playerX;
        avail[1][2] = playerY-1;
      }else{
        avail[0][2] = 100;
        avail[1][2] = 100;
      }//Right
      if(board[playerX][playerY+1] == 0){
        avail[0][3] = playerX;
        avail[1][3] = playerY+1;
      }else{
        avail[0][3] = 100;
        avail[1][3] = 100;
      }//Diagonal Left
      if(board[playerX-1][playerY-1] == 0){
        avail[0][4] = playerX-1;
        avail[1][4] = playerY-1;
      }else{
        avail[0][4] = 100;
        avail[1][4] = 100;
      }//Diagonal Right
      if(board[playerX+1][playerY+1] == 0){
        avail[0][5] = playerX+1;
        avail[1][5] = playerY+1;
      }else{
        avail[0][5] = 100;
        avail[1][5] = 100;
      }
      //If they are all invalid, chose the next available spot.
      if(avail[0][0] == 100 && avail[0][1] == 100 && avail[0][2] == 100 && avail[0][3] == 100 && avail[0][4] == 100 && avail[0][5] == 100){
        for (int i=0; i<board.length; i++){
          for (int j=0; j<board.length; j++){
            if (board[i][j]==0){
              avail[0][0]=i;
              avail[1][0]=j;
            }
          }
        }
      }
      return avail;
    }else{
      //If this is the first move, or there is an invald input
      Random rand = new Random();
      int [][] avail = new int[2][1];
      boolean yes = false;
      while (yes == false){
        avail[0][0] = rand.nextInt((board.length-1));
        avail[1][0] = rand.nextInt((board.length-1));
        if(board[avail[0][0]][avail[1][0]] == 0){
          yes = true;
        }
      }
      
      return avail;
    }
  }
  
  //See if there is a valid move 
  public static boolean canMove(int [] [] board){
    int [] test = {-1,-1,-1,-1};
    if (eval(board, test) == negativeNumber){
      return false;
    }
    else {
      return true; 
    }
  }
  
  
  
}