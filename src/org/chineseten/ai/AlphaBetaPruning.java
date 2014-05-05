package org.chineseten.ai;


//Copyright 2012 Google Inc.
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
////////////////////////////////////////////////////////////////////////////////


import java.util.Collections;
import java.util.List;

import org.game_api.GameApi.GameState;
import org.game_api.GameApi.MakeMove;

import com.google.common.collect.Lists;

/**
* http://en.wikipedia.org/wiki/Alpha-beta_pruning<br>
* This algorithm performs both A* and alpha-beta pruning.<br>
* The set of possible moves is maintained ordered by the current heuristic value of each move. We
* first use depth=1, and update the heuristic value of each move, then use depth=2, and so on until
* we get a timeout or reach maximum depth. <br>
* If a state has {@link TurnBasedState#whoseTurn} null (which happens in backgammon when we should
* roll the dice), then I treat all the possible moves with equal probabilities. <br>
* 
* @author yzibin@google.com (Yoav Zibin)
*/
public class AlphaBetaPruning<S extends GameState, T extends MakeMove> {
static class TimeoutException extends RuntimeException {

 private static final long serialVersionUID = 1L;
}

static class MoveScore<T> implements Comparable<MoveScore<T>> {
 T move;
 int score;

 @Override
 public int compareTo(MoveScore<T> o) {
   return o.score - score; // sort DESC (best score first)
 }
}

private Heuristic<S, T> heuristic;

public AlphaBetaPruning(Heuristic<S, T> heuristic) {
 this.heuristic = heuristic;
}

public T findBestMove(S state, int depth, Timer timer) {
 boolean isWhite = state.getPlayerColor().isWhite();
 // Do iterative deepening (A*), and slow get better heuristic values for the states.
 List<MoveScore<T>> scores = Lists.newArrayList();
 {
   Iterable<T> possibleMoves = heuristic.getOrderedMoves(state);
   for (T move : possibleMoves) {
     MoveScore<T> score = new MoveScore<T>();
     score.move = move;
     score.score = Integer.MIN_VALUE;
     scores.add(score);
   }
 }

 try {
   for (int i = 0; i < depth; i++) {
     for (MoveScore<T> moveScore : scores) {
       T move = moveScore.move;
       int score =
           findMoveScore(state.makeMove(move), i, Integer.MIN_VALUE, Integer.MAX_VALUE, timer);
       if (!isWhite) {
         // the scores are from the point of view of the white, so for black we need to switch.
         score = -score;
       }
       moveScore.score = score;
     }
     Collections.sort(scores); // This will give better pruning on the next iteration.
   }
 } catch (TimeoutException e) {
   // OK, it should happen
 }

 Collections.sort(scores);
 return scores.get(0).move;
}

/**
* If we get a timeout, then the score is invalid.
*/
private int findMoveScore(S state, int depth, int alpha, int beta, Timer timer)
   throws TimeoutException {
 if (timer.didTimeout()) {
   throw new TimeoutException();
 }
 GameOver over = state.getGameOver();
 if (depth == 0 || over != null) {
   return heuristic.getStateValue(state);
 }
 PlayerColor color = state.getPlayerColor();
 int scoreSum = 0;
 int count = 0;
 Iterable<T> possibleMoves = heuristic.getOrderedMoves(state);
 for (T move : possibleMoves) {
   count++;
   int childScore = findMoveScore(state.makeMove(move), depth - 1, alpha, beta, timer);
   if (color == null) {
     scoreSum += childScore;
   } else if (color.isWhite()) {
     alpha = Math.max(alpha, childScore);
     if (beta <= alpha) {
       break;
     }
   } else {
     beta = Math.min(beta, childScore);
     if (beta <= alpha) {
       break;
     }
   }
 }
 return color == null ? scoreSum / count : color.isWhite() ? alpha : beta;
}
}