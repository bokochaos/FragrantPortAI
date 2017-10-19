package connectK;

import connectK.CKPlayer;
import connectK.BoardModel;
import java.awt.Point;

//import java.util.Random;
//import java.util.ArrayList;
import java.util.*;
import java.lang.*;
import java.time.*;

public class FragrantPortAI extends CKPlayer {

	protected byte Opponent;
	
	protected int deadline;
	
	protected Instant startTime;
	
	protected int lastSuccessfulDepthForIDS = 3;
	protected Point lastSuccessfulMoveInIDS;

	public FragrantPortAI(byte player, BoardModel state) {
		super(player, state);
		teamName = "FragrantPortAI";
		Opponent = (super.player == (byte)1) ? (byte)2 : (byte)1;
		deadline = -1; //-1 deadline = no deadline
	}
	
	private BoardModel placeOpponentPiece(BoardModel state, Point move){
		//Simplify opponent piece placement
		return state.placePiece(move, this.Opponent);
	}
	
	private BoardModel placeMyPiece(BoardModel state, Point move){
		//Simplify my piece placement
		return state.placePiece(move, super.player);
	}
	
	private BoardModel removePiece(BoardModel state, Point move){
		//Simplify space piece removal
		return state.placePiece(move, (byte) 0);
	}

	private Point randomMoveMaker(BoardModel state) {
		//Get a random move from the BoardModel from all possible moves
		boolean move_made = false;
		Random r = new Random();
		boolean has_gravity = state.gravityEnabled();
		/*if (has_gravity) {
			//if gravity is active, randomly select a column and scroll the rows
			do {
				int r_col = r.nextInt(state.getWidth());
				for (int row = 0; row < state.getHeight(); row++) {
					if (state.getSpace(r_col, row) == 0){
						Point move = new Point(r_col, row);
						return move;
					}
				}
			} while (!move_made);
		} else {
			//if gravity is inactive, randomly select a position until empty cell found
			do {
				int r_col = r.nextInt(state.getWidth());
				int r_row = r.nextInt(state.getHeight());
				if (state.getSpace(r_col, r_row) == 0){
					Point move = new Point(r_col, r_row);
					return move;
				}
			} while (!move_made);
		}*/
		//Just make a random move. Gravity doesn't matter here, really
		do {
			int col = r.nextInt(state.getWidth());
			int row = r.nextInt(state.getHeight());
			if (state.getSpace(col, row) == 0){
				Point move = new Point(col, row);
				return move;
			}
		} while (!move_made);
		Point move = new Point(0, 0);
		return move;
	}
	
	
	//Get all possible moves from BoardModel state <- the current board model of any ply
	private ArrayList<Point> getPossibleMoves(BoardModel state) {
		ArrayList<Point> possibleMoves = new ArrayList<Point>();
		for (int col = 0; col < state.getWidth(); col++) {
			for (int row = 0; row < state.getHeight(); row++) {
				if (state.getSpace(col, row) == 0){
					Point move = new Point(col, row);
					possibleMoves.add(move);
					if (state.gravityEnabled()) {
						break;
					}
				}
			}
		}
		return possibleMoves;
	}
	
	private int getMiniMaxHorizontalUtility(BoardModel state) 
	{
		int AIscore = 0;
		int humanScore = 0;
		int extraAI = 0;
		int extraHuman = 0;
		int k = state.getkLength();
		for (int row = 0; row < state.getHeight(); row++) 
		{
			for (int col = 0; col <= state.getWidth() - k; col++) 
			{
				int i = 0;
				for (; i < k; i++) {
					if (state.getSpace(col + i,row) == this.player) { break; }
					if (state.getSpace(col,row + i) == this.Opponent) { extraAI++; }
				}
				if (i == k) { AIscore += 1; } 
				else { col = col + i; }
			}
			for (int col = 0; col <= state.getWidth() - k; col++) 
			{
				int i = 0;
				for (; i < k; i++) {
					if (state.getSpace(col + i,row) == this.Opponent) { break; }
					if (state.getSpace(col,row + i) == this.player) { extraHuman++; }
				}
				if (i == k) { humanScore += 1; } 
				else { col = col + i; }
			}
		}
		int finalScore = AIscore - humanScore;
		return finalScore;
	}
	
	private int getMiniMaxVerticalUtility(BoardModel state) {
		int AIscore = 0;
		int humanScore = 0;
		int k = state.getkLength();
		for (int col = 0; col < state.getWidth(); col++) 
		{
			for (int row = 0; row <= state.getHeight() - k; row++) 
			{
				int i = 0;
				int extraAI = 0;
				for (; i < k; i++) {
					if (state.getSpace(col,row + i) == this.player) { break; }
					if (state.getSpace(col,row + i) == this.Opponent) { extraAI++; }
				}
				if (i == k) { AIscore += 1; } 
				else { row = row + i; }
			}
			for (int row = 0; row <= state.getHeight() - k; row++) 
			{
				int i = 0;
				int extraHuman = 0;
				for (; i < k; i++) 
				{
					if (state.getSpace(col,row + i) == this.Opponent) { break; }
					if (state.getSpace(col,row + i) == this.player) { extraHuman++; }
				}
				if (i == k) { humanScore += 1; } 
				else { row = row + i; }
			}
		}
		int finalScore = AIscore - humanScore;
		return finalScore;
	}
	
	private int getMiniMaxSouthEastUtility(BoardModel state) 
	{
		int AIscore = 0;
		int humanScore = 0;
		int k = state.getkLength();
		for (int col = 0; col <= state.getWidth() - k; col++) 
		{
			for (int row = k - 1; row < state.getHeight(); row++) 
			{
				int i = 0;
				boolean aiPossible = true;
				boolean humanPossible = true;
				int extraHuman = 0;
				int extraAI = 0;
				for (; i < k; i++) 
				{
					if (state.getSpace(col + i,row - i) == this.player) 
					{
						extraHuman++;
						aiPossible = false;
					} 
					else if (state.getSpace(col + i,row - i) == this.Opponent) 
					{
						extraAI++;
						humanPossible = false;
					}
					if (!aiPossible && !humanPossible) { break; }
				}
				if (i == k) {
					if(aiPossible) { AIscore += 1; }
					if(humanPossible) { humanScore += 1; }
				}
			}
		}
		int finalScore = AIscore - humanScore;
		return finalScore;
	}
	
	public int getMiniMaxNorthEastUtility(BoardModel state) 
	{
		int AIscore = 0;
		int humanScore = 0;
		int k = state.getkLength();
		for (int col = 0; col <= state.getWidth() - k; col++) 
		{
			for (int row = 0; row <= state.getHeight() - k; row++) 
			{
				int i = 0;
				boolean aiPossible = true;
				boolean humanPossible = true;
				int extraHuman = 0;
				int extraAI = 0;
				for (; i < k; i++) 
				{
					if (state.getSpace(col + i,row + i) == this.player) 
					{
						extraHuman++;
						aiPossible = false;
					} 
					else if (state.getSpace(col + i,row + i) == this.Opponent) 
					{
						extraAI++;
						humanPossible = false;
					}
					if (!aiPossible && !humanPossible) { break; }
				}
				if (i == k) 
				{
					if(aiPossible) { AIscore += 1; }
					if(humanPossible) { humanScore += 1; }
				}
			}
		}
		int finalScore = AIscore - humanScore;
		return finalScore;
	}
	
	private Point getUtilityOfACell(int col, int row, BoardModel state)
	{ //TODO fix utility in this method
		int currentPiece = state.getSpace(col, row);
		Point utility = new Point(0, 0); //x=ai, y=player
		int k = state.getkLength();
		for(int direction = 0; direction < 4; direction++)
		{
			int r = row;
			int c = col;
			int count = 1;
			boolean connected = true;
			byte tempPiece = 0;
			while(count < k)
			{
				if(direction == 0) //North
				{ r++; }
				else if (direction == 1) //North-East
				{
					r++;
					c++;
				}
				else if (direction == 2) //East
				{ c++; }
				else
				{
					r--;
					c++;
				}
				if(c < state.getkLength() && r < state.getWidth() && r >= 0)
				{
					if(currentPiece == (byte) 0)
					{
						connected = false;
						if(state.getSpace(c,r) == (byte) 0 || state.getSpace(c,r) == tempPiece)
						{
							count++;
						}
						else if(tempPiece == (byte) 0)
						{
							//This will be run firstime when a AI_PIECE or HUMAN_PIECE is encountered.
							//In this case we will ignore the aberration and just save it as a tempPiece.
							tempPiece = state.getSpace(c,r);
							count++;
						}
						else { break; }

					}
					else
					{
						if(state.getSpace(c,r) == currentPiece) { count++; }
						else { break; }
					}
				}
				else { break; }
			}
			if(count == k)
			{
				if(currentPiece == this.Opponent)
				{
					if(connected)
					{
						utility.x = Integer.MAX_VALUE;
						return utility;
					}
					utility.x += 1;
				}
				else if (currentPiece == this.player)
				{
					if(connected)
					{
						utility.y = Integer.MAX_VALUE;
						return utility;
					}
					utility.y += 1;
				}
				else
				{
					if(tempPiece == (byte) 0)
					{
						utility.y += 1;
						utility.x += 1;
					}
					else if (tempPiece == this.player) { utility.y += 1; }
					else { utility.x += 1; }
				}
			}
		}
		return utility;
	}
	
	//Check to see if the minimax hit terminal functionality
	private boolean minimaxTerminal(Point m, BoardModel state) 
	{ // should work as intended
		int player = state.getSpace(m);
		int count = 0;
		int i = m.x; int j = m.y;
		int k = state.getkLength();
		while (count < k && i >= 0 && state.getSpace(i--,m.y) == player) { count++; }
		i = m.x + 1;
		while (count < k && i < state.getWidth() && state.getSpace(i++,m.y) == player) { count++; }
		if (count == k) { return true; }
		count = 0;
		i = m.x; j = m.y;
		while (count < k && i >= 0 && state.getSpace(m.x,j--) == player) { count++; }
		j = m.y + 1;
		while (count < k && j < state.getHeight() && state.getSpace(m.x,j++) == player) { count++; }
		if (count == k) { return true; }
		count = 0;
		i = m.x; j = m.y;
		while (count < k && i >= 0 && j >= 0 && state.getSpace(i--,j--) == player) { count++; }
		i = m.x + 1; j = m.y + 1;
		while (count < k && i < state.getWidth() && j < state.getHeight() && state.getSpace(i++,j++) == player) { count++; }
		if (count == k) { return true; }
		count = 0;
		i = m.x; j = m.y;
		while (count < k && i < state.getWidth() && j >= 0 && state.getSpace(i++,j--) == player) 
		{ count++; }
		i = m.x - 1; j = m.y + 1;
		while (count < k && i >= 0 && j < state.getHeight() && state.getSpace(i--,j++) == player) { count++; }
		if (count == k) { return true; }
		return false;
	}

	private int getMiniMaxUtility(BoardModel state) 
	{
		return getMiniMaxHorizontalUtility(state) + getMiniMaxVerticalUtility(state) + getMiniMaxSouthEastUtility(state) + getMiniMaxNorthEastUtility(state);
		/*Point utility = new Point(0,0);
		for(int i=0; i<state.getWidth(); ++i)
		{
			for(int j=0; j<state.getHeight(); ++j)
			{
				utility = getUtilityOfACell(i, j, state); //TODO
				
				//x=ai, y=player
				if(utility.x == Integer.MAX_VALUE)
				{
					return Integer.MAX_VALUE;
				}
				if(utility.y == Integer.MAX_VALUE)
				{
					return Integer.MIN_VALUE;
				}
				utility.x += utility.x;
				utility.y += utility.y;
			}
		}
		int finalUtility = utility.x - utility.y;
		return finalUtility;*/
	}
	
	//Base MiniMax functions
	
	//Get the max value of a MiniMax instance
	//Board state passed through to make future instances easier
	private int minimaxMaxValue(int depth, BoardModel state) {
		if (depth <= 0) {
			return getMiniMaxUtility(state);
		}
		int maxValue = Integer.MIN_VALUE;
		ArrayList<Point> moves = getPossibleMoves(state);
		int movesLen = moves.size();
		//Iterate through all possible moves
		for (int i = 0; i < movesLen; i++) {
			Point move = moves.get(i);
			state = placeOpponentPiece(state, move);
			if (minimaxTerminal(move, state)) {
				maxValue = Integer.MAX_VALUE;
				state = removePiece(state, move);
				break;
			}
			int valueForMove = minimaxMinValue(depth - 1, state);
			if (valueForMove > maxValue) {
				maxValue = valueForMove;
			}
			state = removePiece(state, move);
			if (maxValue == Integer.MAX_VALUE) break;
		}
		return maxValue;
	}

	//Get the min value of a MiniMax instance
	//Board state passed through to make future instances easier
	private int minimaxMinValue(int depth, BoardModel state) {
		if (depth <= 0) {
			return getMiniMaxUtility(state);
		}
		int minValue = Integer.MAX_VALUE;
		ArrayList<Point> moves = getPossibleMoves(state);
		int movesLen = moves.size();
		//Iterate through all possible moves
		for (int i = 0; i < movesLen; i++) {
			Point move = moves.get(i);
			state = placeMyPiece(state, move);
			if (minimaxTerminal(move, state)) {
				minValue = Integer.MIN_VALUE;
				state.placePiece(move, (byte)0);
				break;
			}
			int valueForMove = minimaxMaxValue(depth - 1, state);
			if (valueForMove < minValue) {
				minValue = valueForMove;
			}
			state = removePiece(state, move);
			if (minValue == Integer.MIN_VALUE) break;
		}
		return minValue;
	}
	
	private Point minimaxSearch(BoardModel state) {
		// for the current game state, get all possible moves Max (i.e. the AI) can make
		// Move maxMove; int maxValue = -Inf (initialization of max's move and the move's value)
		// for each move m of the possible moves:
		//   val = call miniMaxSearchMinValue after applying m to the state
		//   if val > maxValue:
		//      maxValues = val
		//      maxMove = m
		//   revert the move m from the state
		// return maxMove
		int maxDepth = state.gravityEnabled() ? 5 : 3;
		//Establish max depth for the search
		ArrayList<Point> moves = getPossibleMoves(state);
		Point maxMove = moves.get(0);
		int maxValue = Integer.MIN_VALUE;
		int movesLen = moves.size();
		//Iterate through all possible moves
		for (int i = 0; i < movesLen; i++) {
			Point move = moves.get(i);
			state = placeOpponentPiece(state, move);
			if (minimaxTerminal(move, state)) {
				maxMove = new Point(move);
				state = removePiece(state, move);
				break;
			}
			int valueForMove = minimaxMinValue(maxDepth - 1, state);
			if (valueForMove > maxValue) {
				maxValue = valueForMove;
				maxMove = new Point(move);
			}
			state = state.placePiece(move, (byte) 0);
			if (maxValue == Integer.MAX_VALUE) break;
		}
		return maxMove;
	}
	
	//Base AB-Pruning functions
	private int alphaBetaMax(int depth, int alpha, int beta, BoardModel state) {
		if (depth <= 0) {
			return getMiniMaxUtility(state);
		}//Avoids max recursion depth is reached
		ArrayList<Point> moves = getPossibleMoves(state);
		int movesLen = moves.size();
		//iterate through all possible moves
		for (int i = 0; i < movesLen; i++) {
			Point move = moves.get(i);
			state = placeMyPiece(state, move);
			//If the game is over, cease iteration on this line
			if (minimaxTerminal(move, state)) {
				beta = Integer.MIN_VALUE;
				state = removePiece(state, move);
				break;
			}
			//Simulate opponent's moves
			int alphaForMove = alphaBetaMin(depth - 1, alpha, beta, state);
			if (alphaForMove > alpha) {
				alpha = alphaForMove;
			}
			state = removePiece(state, move);
			//Prune if not optimal
			if (alpha >= beta) return Integer.MAX_VALUE;
			if (alpha == Integer.MAX_VALUE) break;
		}
		return alpha;
	}

	private int alphaBetaMin(int depth, int alpha, int beta, BoardModel state) {
		//Simulate a user's max optimal choice to limit user's optimal choices
		if (depth <= 0) {
			return getMiniMaxUtility(state);
		} //Done to ensure that max recursion depth or memory exhaustion is avoided
		ArrayList<Point> moves = getPossibleMoves(state);
		int movesLen = moves.size();
		//Iterate through all possible moves for the user
		for (int i = 0; i < movesLen; i++) {
			Point move = moves.get(i);
			state = placeMyPiece(state, move);
			//If the game is over, cease iteration on this line 
			if (minimaxTerminal(move, state)) {
				beta = Integer.MIN_VALUE;
				state = removePiece(state, move); //reset the board for the next iteration 
				break;
			}
			//Simulate user moves
			int betaForMove = alphaBetaMax(depth - 1, alpha, beta, state);
			if (betaForMove < beta) {
				beta = betaForMove;
			}
			state = removePiece(state, move);
			//Prune if not optimal
			if (alpha >= beta) return Integer.MIN_VALUE;
			if (beta == Integer.MIN_VALUE) break;
		}
		return beta;
	}

	private Point alphaBetaSearch(BoardModel state) {
		//Search all possible moves for their utility value.
		//Return the move with the best potential based on the depth pre-set
		int alpha = Integer.MIN_VALUE; int beta = Integer.MAX_VALUE;
		int maxDepth = state.gravityEnabled() ? 5 : 4;
		ArrayList<Point> moves = getPossibleMoves(state);
		Point maxMove = moves.get(0);
		int movesLen = moves.size();
		for (int i = 0; i < movesLen; i++) {
			Point move = moves.get(i);
			state = placeOpponentPiece(state, move);
			if (minimaxTerminal(move, state)) {
				maxMove = new Point(move);
				state = removePiece(state, move);
				break;
			}
			int alphaForMove = alphaBetaMin(maxDepth - 1, alpha, beta, state);
			if (alphaForMove > alpha) {
				alpha = alphaForMove;
				maxMove = new Point(move);
			}
			state = removePiece(state, move);
			if (alpha == Integer.MAX_VALUE) break;
		}
		return maxMove;
	}
	
	
	//Iterative Deepening Search Functions
	//Iterative Deepening Search abMax value calculator
	//Iterative Deepening Search Functions
	//Iterative Deepening Search abMax value calculator
	private int alphaBetaMax(int depth, int alpha, int beta, BoardModel state, 
		int currentDepth, boolean validMove, Map<Integer, Map<Point, Integer>> depthToMoves) {
		if (depth <= 0) 
			{
				return getMiniMaxUtility(state);
			}//Avoids max recursion depth is reached
		ArrayList<Point> moves = getPossibleMoves(state);
		
		if(depthToMoves.contains(Integer(currentDepth + 1)))
		{
			moves = depthToMoves.get(Integer(1)).keySet().toArray();
		}
		else
		{
			moves = getActions(state);
		}
		
		int movesLen = moves.size();
		if (movesLen == 0) 
		{
			return 0; 
		}
		
		Map<Point, Integer> movesByUtility = new HashMap<Point, Integer>();
		//iterate through all possible moves
		for (int i = 0; i < movesLen; i++) 
		{
			Point move = moves.get(i);
			state = placeMyPiece(state, move);
			//If the game is over, cease iteration on this line
			if (minimaxTerminal(move, state)) 
			{
				alpha = Integer.MAX_VALUE;
				state = removePiece(state, move);
				break;
			}
			//Simulate opponent's moves
			int moveAlpha = alphaBetaMin(depth - 1, alpha, beta, state, currentDepth+1, validMove, depthToMoves);
			
			if (validMove == false) 
			{
				return Integer.MIN_VALUE;
			}
			
			
			if(!validMove){ return Integer.MIN_VALUE; }
			movesByUtility.put(move, Integer(alphaForMove));
			
			Instant currentTime = Instant.now(Clock.systemDefaultZone());
			if (currentTime.toEpochMilli() - startTime.toEpochMilli >= (long)deadline) {
				validMove = false;
				return Integer.MAX_VALUE;
			}

			if (moveAlpha > alpha) 
			{
				alpha = moveAlpha;
			}
			state = removePiece(state, move);
			//Prune if not optimal
			if (alpha >= beta)
			{
				addToMapMoves(depthToMoves, movesByUtility, 1);
				return Integer.MAX_VALUE;
			}
			if (alpha == Integer.MAX_VALUE)
			{
				break;
			}
		}
		addToMapMoves(depthToMoves, movesByUtility, depth);
		return alpha;
	}

	//Iterative Deepening Search abMin value calculator
	private int alphaBetaMin(int depth, int alpha, int beta, BoardModel state,
		int currentDepth, boolean validMove, Map<Integer, Map<Point, Integer>> depthToMoves) {
		//Simulate a user's max optimal choice to limit user's optimal choices
		if (depth <= 0) {
			return getMiniMaxUtility(state);
		} //Done to ensure that max recursion depth or memory exhaustion is avoided
		
		//ArrayList<Point> moves = getPossibleMoves(state);
		ArrayList<Point> moves = new ArrayList<Point>(); //moves possible;
		//get moves from the map
		if ( depthToMoves.contains(Integer(currentDepth + 1)) ) { moves = depthToMoves.get(Integer(1)).keySet().toArray(); }
		//Return an arraylist of the moves in the mapping of depth -> (move -> utility value of move)
		else { moves = getPossibleMoves(state); }
		
		int movesLen = moves.size();
		if(movesLen == 0) { return movesLen; }
		
		Map<Point, Integer> movesByUtility = new HashMap<Point, Integer>();
		
		//Iterate through all possible moves for the user
		for (int i = 0; i < movesLen; i++) {
			Point move = moves.get(i);
			state = placeMyPiece(state, move);
			//If the game is over, cease iteration on this line 
			if (minimaxTerminal(move, state)) {
				beta = Integer.MIN_VALUE;
				state = removePiece(state, move); //reset the board for the next iteration 
				break;
			}
			
			//Simulate user moves
			int betaForMove = alphaBetaMax(depth - 1, alpha, beta, state, currentDepth + 1, validMove, depthToMoves);
			
			if(!validMove){ return Integer.MAX_VALUE; }
			movesByUtility.put(move, Integer(alphaForMove));
			
			Instant currentTime = Instant.now(Clock.systemDefaultZone());
			if (currentTime.toEpochMilli() - startTime.toEpochMilli >= (long)deadline) {
				validMove = false;
				return Integer.MAX_VALUE;
			}
			
			if (betaForMove < beta) {
				beta = betaForMove;
			}
			state = removePiece(state, move);
			//Prune if not optimal
			if (alpha >= beta){
				addToMapMoves(depthToMoves, movesByUtility, depth);
				return Integer.MIN_VALUE;
			}
			if (beta == Integer.MIN_VALUE) break;
		}
		return beta;
	}
	
	
	private Point alphaBetaSearch(BoardModel state, int depthToTry, Map<Integer, Map<Point, Integer>> depthToMoves) {
		//Search all possible moves for their utility value.7
		//Return the move with the best potential based on the depth pre-set
		int alpha = Integer.MIN_VALUE; int beta = Integer.MAX_VALUE;
		int maxDepth = state.gravityEnabled() ? 5 : 4;
		ArrayList<Point> moves = new ArrayList<Point>(); //moves possible;
		//ArrayList<Point> moves = getPossibleMoves(state); //possible moves
		//get moves from the map
		if ( depthToMoves.contains(Integer(1)) ) { moves = depthToMoves.get(Integer(1)).keySet().toArray(); }
		//Return an arraylist of the moves in the mapping of depth -> (move -> utility value of move)
		else { moves = getPossibleMoves(state); }
		
		Point maxMove = moves.get(0);
		int movesLen = moves.size();
		boolean validMove = true;
		Map<Point, Integer> movesByUtility = new HashMap<Point, Integer>(); 
		
		for (int i = 0; i < movesLen; i++) {
			Point move = moves.get(i);
			state = placeOpponentPiece(state, move);
			if (minimaxTerminal(move, state)) {
				maxMove = new Point(move);
				state = removePiece(state, move);
				break;
			}
			int alphaForMove = alphaBetaMin(maxDepth - 1, alpha, beta, state, 1, validMove, depthToMoves);
			
			if(!validMove){ return new Point(-1, -1); }
			movesByUtility.put(move, Integer(alphaForMove));
			
			Instant currentTime = Instant.now(Clock.systemDefaultZone());
			if (currentTime.toEpochMilli() - startTime.toEpochMilli >= (long)deadline) {
				return new Point(-1, -1);
			}
			
			if (alphaForMove > alpha) {
				alpha = alphaForMove;
				maxMove = new Point(move);
			}
			state = removePiece(state, move);
			if (alpha == Integer.MAX_VALUE) break;
		}
		addToMapMoves(depthToMoves, movesByUtility, 1);
		return maxMove;
	}
	
	void addToMapMoves(Map<Integer, Map<Point, Integer>> depthToMoves, Map<Point, Integer> movesByUtility, int depth){
		depthToMoves.get(Integer(depth)).putAll(movesByUtility);
	}
	
	private Point iterativeDeepeningSearch(BoardModel state){
		Point move;
		boolean pastDeadline = false;
		deadline = deadline - 500;
		
		Instant currentTime = Instant.now(Clock.systemDefaultZone());
		
		map<Integer, Map<Point, Integer>> depthToMoves = new HashMap<Integer, Map<Point, Integer>>();
		int depthToTry = lastSuccessfulDepthForIDS - 1;
		do{
			Point currentMove = alphaBetaSearch(state, depthToTry, depthToMoves);
			if(currentMove.x == -1){ return lastSuccessfulMoveInIDS; }
			
			currentTime = Instant.now(Clock.systemDefaultZone());
			long timeElapsed = currentTime - startTime;
			
			if( (int)timeElapsed > deadline ){
				pastDeadline = true;
				//break;
			}
			else {
				move = currentMove;
				lastSuccessfulDepthForIDS = depthToTry;
				lastSuccessfulMoveInIDS = move;
				depthToTry++;
			}
		} while(!pastDeadline);
		return m;
		
	}

	@Override
	public Point getMove(BoardModel state) {
		//Returns the move the AI uses
		/*for(int i=0; i<state.getWidth(); ++i)
			for(int j=0; j<state.getHeight(); ++j)
				if(state.getSpace(i, j) == 0)
					return new Point(i,j);*/
		//return alphaBetaSearch(state);
		return iterativeDeepeningSearch(state);
		//return null;
	}

	@Override
	public Point getMove(BoardModel state, int deadline) {
		this.deadline = deadline; //Set deadline if there is one.
		this.startTime = Instant.now(Clock.systemDefaultZone());
		return getMove(state);
	}
}
