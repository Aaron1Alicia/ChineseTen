package org.chineseten.client;

import static com.google.common.base.Preconditions.checkArgument;





//import java_cup.internal_error;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import java_cup.internal_error;

import org.apache.bcel.generic.NEW;
import org.chineseten.client.Card;
import org.chineseten.client.Card.Rank;
import org.chineseten.client.Card.Suit;
import org.chineseten.client.GameApi.EndGame;
import org.chineseten.client.GameApi.Operation;
import org.chineseten.client.GameApi.Set;
import org.chineseten.client.GameApi.SetTurn;
import org.chineseten.client.GameApi.SetVisibility;
import org.chineseten.client.GameApi.Shuffle;
import org.chineseten.client.GameApi.VerifyMove;
import org.chineseten.client.GameApi.VerifyMoveDone;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gwt.core.shared.GWT;


/**
 * 
 * @author aaronwong
 * @
 */
public class ChineseTenLgoic {

    private static final String C = "C"; // Card key (C0 .. C51)
    //private final int wId = 41;
    //private final int bId = 42;
    private final int stage0 = 0;
    private final int stage1 = 1;
    private final int stage2 = 2;
    private final int stage3 = 3;
    //private final String reset = "reset";
    private final String match = "match";
    private final String noMatch = "noMatch";
    private final String special = "special";
    //private final String noMatch = "nomatch";
    private final String flip = "flip";
    //private final String noFlip = "noflip";
    //private final String playerId = "playerId";
    private static final String STAGE = "stage"; // 0 for init, 4 for end, 1,2,3 for three stages
    private static final String W = "W"; // White hand
    private static final String B = "B"; // Black hand
    private static final String M = "M"; // Middle pile
    private static final String WC = "WC"; // Cards collected by W
    private static final String BC = "BC"; // Cards collected by B
    private static final String D = "D"; // Cards faced up around M
    //private static final String C = "C"; // Card key (C1 .. C54)
    private static final String CLAIM = "claim"; 

    public VerifyMoveDone verify(VerifyMove verifyMove) {
        try {
            checkMoveIsLegal(verifyMove);
            return new VerifyMoveDone();
        } catch (Exception e) {
            GWT.log(e.getMessage());
            e.printStackTrace();
            return new VerifyMoveDone(verifyMove.getLastMovePlayerId(),
                    e.getMessage());
            
        }
    }

    void checkMoveIsLegal(VerifyMove verifyMove) {
      List<Operation> lastMove = verifyMove.getLastMove();
      Map<String, Object> lastState = verifyMove.getLastState();
      // Checking the operations are as expected.
      List<Operation> expectedOperations = getExpectedOperations(
          lastState, lastMove, verifyMove.getPlayerIds(), verifyMove.getLastMovePlayerId());
      check(expectedOperations.equals(lastMove), expectedOperations, lastMove);
      // We use SetTurn, so we don't need to check that the correct player did the move.
      // However, we do need to check the first move is done by the white player (and then in the
      // first MakeMove we'll send SetTurn which will guarantee the correct player send MakeMove).
      if (lastState.isEmpty()) {
        check(verifyMove.getLastMovePlayerId() == verifyMove.getPlayerIds().get(0),
                verifyMove.getLastMovePlayerId());
        int temp = verifyMove.getLastMovePlayerId();
      //System.out.println("player ID is " + temp + "\n");
      GWT.log("plyaer ID is" + temp + "\n");
      GWT.log("verifyMove.getPlayerIds().get(0)" + verifyMove.getPlayerIds().get(0) + "\n");
        }
      }

    /**
     *  
     * @param lastApiState
     * @param lastMove
     * @param playerIds
     * @param lastMovePlayerId
     * @return
     */
    List<Operation> getExpectedOperations(Map<String, Object> lastApiState,
            List<Operation> lastMove, List<Integer> playerIds,
            int lastMovePlayerId) {
        if (lastApiState.isEmpty()) {
            return getInitialMove(playerIds.get(0), playerIds.get(1));
        }
        ChineseTenState lastState = gameApiStateToChineseTenState(lastApiState,
                Color.values()[playerIds.indexOf(lastMovePlayerId)], playerIds);
        
//         There are 3 types of moves:
//         1) stage1: claim one card from W/B and one card from D or do nothing
//         2) stage2: flip one card from M to D if there is still cards in M
//         3) stage3: collect two cards within D if rule allows 
        
        if (lastMove.contains(new Set(STAGE, stage1))) {
            //System.out.println("Enter stage1");
            // Check if move is legal in stage1
            check(lastState.getStage() == 3 || lastState.getStage() == 0);        
            if (lastMove.contains(new Set(CLAIM, special))) {
                return doClaimMoveOnSpecialCase(lastState, lastMove, playerIds);
            }  
            if (lastMove.contains(new Set(CLAIM, match))) {
                return doClaimMoveOnStage1(lastState, lastMove, playerIds);  
            } else {
                check(lastMove.contains(new Set(CLAIM, noMatch)));
                return doNoClaimMoveOnStage1(lastState, lastMove, playerIds);
            }
                
        } else if (lastMove.contains(new Set(STAGE, stage2))) {
            //System.out.println("Enter stage2");
            // Check if move is legal in stage2
            check(lastState.getStage() == 1, lastState.getStage());
            
            //if (lastMove.contains(new Set(CLAIM, match))) {
            return doClaimMoveOnStage2(lastState, lastMove, playerIds);   
        } else if (lastMove.contains(new Set(STAGE, stage3))) {
            //System.out.println("Enter stage3");
            // Check if move is legal in stage3
            check(lastState.getStage() == 2, lastState.getStage());
            if (lastMove.contains(new Set(CLAIM, match))) {
                return doClaimMoveOnStage3(lastState, lastMove, playerIds);  
            } else {
                check(lastMove.contains(new Set(CLAIM, noMatch)));
                return doNoClaimMoveOnStage3(lastState, lastMove, playerIds);        
            }
                  
        } else {
            return getInitialMove(playerIds.get(0), playerIds.get(1));
        }

    }

    @SuppressWarnings("unchecked")
    ChineseTenState gameApiStateToChineseTenState(
            Map<String, Object> gameApiState, Color turnOfColor,
            List<Integer> playerIds) {
        List<Optional<Card>> cards = Lists.newArrayList();
        
        for (int i = 0; i < 52; i++) {
            String cardString = (String) gameApiState.get(C + i);
            Card card;
            if (cardString == null) {
              card = null;
            } else {
              Rank rank = Rank.fromFirstLetter(cardString.substring(0, cardString.length() - 1));
              Suit suit = Suit.fromFirstLetterLowerCase(
                      cardString.substring(cardString.length() - 1));
              card = new Card(suit, rank);
            }
            cards.add(Optional.fromNullable(card));
          }
              
        int stage = (Integer) gameApiState.get(STAGE);
        List<Integer> white = (List<Integer>) gameApiState.get(W);
        List<Integer> black = (List<Integer>) gameApiState.get(B);
        List<Integer> whiteCollect = (List<Integer>) gameApiState.get(WC);
        List<Integer> blackCollect = (List<Integer>) gameApiState.get(BC);
        List<Integer> deck = (List<Integer>) gameApiState.get(D);
        List<Integer> middle = (List<Integer>) gameApiState.get(M);
        
//        return new ChineseTenState(turnOfColor, stage, Optional.fromNullable(Claim
//                .fromClaimEntryInGameState((List<String>) gameApiState.get(CLAIM))), 
//                 ImmutableList.copyOf(white), ImmutableList.copyOf(black),
//                 ImmutableList.copyOf(whiteCollect), ImmutableList.copyOf(blackCollect),
//                 ImmutableList.copyOf(deck), ImmutableList.copyOf(middle),
//                 ImmutableList.copyOf(cards), ImmutableList.copyOf(playerIds));
        
        return new ChineseTenState(turnOfColor, stage, Optional.fromNullable(
                (String) gameApiState.get(CLAIM)), ImmutableList.copyOf(white), 
                ImmutableList.copyOf(black),
                 ImmutableList.copyOf(whiteCollect), ImmutableList.copyOf(blackCollect),
                 ImmutableList.copyOf(deck), ImmutableList.copyOf(middle),
                 ImmutableList.copyOf(cards), ImmutableList.copyOf(playerIds));
    }
    
    /** Returns the operations for stage1. */
    @SuppressWarnings("unchecked")
    List<Operation> doClaimMoveOnStage1(ChineseTenState state, List<Operation> lastMove, 
        List<Integer> playerIds) {
        
        Color turnOfColor = state.getTurn();
        
        // Get the diff between last state W/B and last move W/B
        List<Integer> lastWorB = state.getWhiteOrBlack(turnOfColor);      
        Set setWorB = (Set) lastMove.get(3);
        List<Integer> lastMoveWorB = (List<Integer>) setWorB.getValue();
        List<Integer> diffWorB = subtract(lastWorB, lastMoveWorB);
        check(diffWorB.size() == 1, lastWorB, lastMoveWorB, diffWorB);
        
        // Get the diff between last state D and last move D
        List<Integer> lastD = state.getDeck();
        Set setD = (Set) lastMove.get(5);
        List<Integer> lastMoveD = (List<Integer>) setD.getValue();
        List<Integer> diffD = subtract(lastD, lastMoveD);
        check(diffD.size() == 1, lastD, lastMoveD, diffD);
        
        // Check whether sum is ten
        check(checkWhetherSumIsTen(state, diffWorB, diffD), diffWorB, diffD);
        
        List<Integer> newCollection = concat(diffWorB, diffD);
        List<Integer> newWCOrBC = concat(newCollection, state.getWCOrBC(turnOfColor));       
      
      List<Operation> expectedOperations = Lists.newArrayList();
             expectedOperations.add(new SetTurn(state.getPlayerId(turnOfColor)));
             expectedOperations.add(new Set(STAGE, stage1));
             expectedOperations.add(new Set(CLAIM, match));
             expectedOperations.add(new Set(turnOfColor.name(), lastMoveWorB));
             expectedOperations.add(new Set(turnOfColor.name() + "C", newWCOrBC));
             expectedOperations.add(new Set(D, lastMoveD));
             expectedOperations.add(new SetVisibility(C + diffWorB.get(0)));
             
                 
      
      if (newWCOrBC.size() + state.getOppositeCollection(turnOfColor).size() == 52) {
          
          // If it is time to end the game, we need choose the winner and shuffle the cards
          //endGame.add(new Shuffle(getCardsInRange(0, 51)));
          if (newWCOrBC.size() > state.getOppositeCollection(turnOfColor).size()) {
              expectedOperations.add(new EndGame(state.getPlayerId(turnOfColor)));
          } else {
              expectedOperations.add(new EndGame(
                      state.getPlayerId(turnOfColor.getOppositeColor())));
          }
         // return endGame;
      }
      return expectedOperations;
      //return endGame;
    }
    
    
    @SuppressWarnings("unchecked")
    List<Operation> doNoClaimMoveOnStage1(ChineseTenState state, List<Operation> lastMove, 
        List<Integer> playerIds) {
        
        Color turnOfColor = state.getTurn();
        
        // Get the diff between last state W/B and last move W/B
        List<Integer> lastWorB = state.getWhiteOrBlack(turnOfColor);      
        Set setWorB = (Set) lastMove.get(3);
        List<Integer> lastMoveWorB = (List<Integer>) setWorB.getValue();
        List<Integer> diffWorB = subtract(lastWorB, lastMoveWorB);
        check(diffWorB.size() == 1, lastWorB, lastMoveWorB, diffWorB);
        
        // Get the diff between last state D and last move D
        List<Integer> lastD = state.getDeck();
        Set setD = (Set) lastMove.get(4);
        List<Integer> lastMoveD = (List<Integer>) setD.getValue();
        List<Integer> diffD = subtract(lastMoveD, lastD);
        check(diffD.size() == 1, lastD, lastMoveD, diffD);
        
        // Check diffWorB equals diffD
        check(diffWorB.get(0) == diffD.get(0), diffWorB, diffD);      

      // If Collect at stage1  then the format must be:
//        new SetTurn(state.getPlayerId(turnOfColor)),
//        new Set(STAGE, stage1),
//        new Set(CLAIM, match),
//        new Set(turnOfColor.name(), lastMoveWorB),
//        new Set(turnOfColor.name() + "C", newWCOrBC),
//        new Set(D, lastMoveD),
//        new SetVisibility(C + diffWorB.get(0)));  

      List<Operation> expectedOperations = ImmutableList.<Operation>of(
          new SetTurn(state.getPlayerId(turnOfColor)),
          new Set(STAGE, stage1),
          new Set(CLAIM, noMatch),
          new Set(turnOfColor.name(), subtract(lastWorB, diffWorB)),
          new Set(D, concat(diffWorB, lastD)),
          new SetVisibility(C + diffWorB.get(0)));
      
      
      return expectedOperations;
      //return endGame;
    }
    
    /** Returns the operations for stage2. */
    @SuppressWarnings("unchecked")
    List<Operation> doClaimMoveOnStage2(ChineseTenState state, List<Operation> lastMove, 
        List<Integer> playerIds) {
        
        Color turnOfColor = state.getTurn();
        
        // Get the diff between last state W/B and last move W/B
        List<Integer> lastM = state.getMiddle();      
        Set setM = (Set) lastMove.get(4);
        List<Integer> lastMoveM = (List<Integer>) setM.getValue();
        List<Integer> diffM = subtract(lastM, lastMoveM);
        check(diffM.size() == 1, lastM, lastMoveM, diffM);
        
        int flipNumber = diffM.get(0);
        
        List<Integer> lastD = state.getDeck();
        
        List<Integer> newD = concat(diffM, lastD);       

      List<Operation> expectedOperations = ImmutableList.<Operation>of(
          new SetTurn(state.getPlayerId(turnOfColor)),
          new Set(STAGE, stage2),
          new Set(CLAIM, flip),
          new Set(D, newD),
          new Set(M, lastMoveM),
          new SetVisibility(C + flipNumber));
      return expectedOperations;
    }
    
    /** Returns the operations for stage3. */
    @SuppressWarnings("unchecked")
    List<Operation> doClaimMoveOnStage3(ChineseTenState state, List<Operation> lastMove, 
        List<Integer> playerIds) { 
        
        check(state.getStage() == 2, state.getStage());
        Color turnOfColor = state.getTurn();
        
        // Get the diff between last state D and last move D
        List<Integer> lastD = state.getDeck();
        Set setD = (Set) lastMove.get(4);
        List<Integer> lastMoveD = (List<Integer>) setD.getValue();
        List<Integer> diffD = subtract(lastD, lastMoveD);
        check(diffD.size() == 2, lastD, lastMoveD, diffD);
        
        List<Integer> diffD1 = diffD.subList(0, 1);
        List<Integer> diffD2 = diffD.subList(1, 2);
               
        // Check whether sum is ten
        check(checkWhetherSumIsTen(state, diffD1, diffD2), diffD1, diffD2);
        
        List<Integer> newWCOrBC = concat(diffD, state.getWCOrBC(turnOfColor));  
        
      List<Operation> expectedOperations = Lists.newArrayList();
              
         expectedOperations.add(new SetTurn(state.getPlayerId(turnOfColor.getOppositeColor())));
         expectedOperations.add(new Set(STAGE, stage3));
         expectedOperations.add(new Set(CLAIM, match));
         expectedOperations.add(new Set(turnOfColor.name() + "C", newWCOrBC));
         expectedOperations.add(new Set(D, lastMoveD));
      
      
    if (newWCOrBC.size() + state.getOppositeCollection(turnOfColor).size() == 52) {
    
     if (newWCOrBC.size() > state.getOppositeCollection(turnOfColor).size()) {
         expectedOperations.add(new EndGame(state.getPlayerId(turnOfColor)));
     } else {
         expectedOperations.add(new EndGame(state.getPlayerId(turnOfColor.getOppositeColor())));
     }
     //return endGame;
 }
      
      
      
      return expectedOperations;
    }
    
    
    //x@SuppressWarnings("unchecked")
    List<Operation> doNoClaimMoveOnStage3(ChineseTenState state, List<Operation> lastMove, 
        List<Integer> playerIds) {       
        Color turnOfColor = state.getTurn();
       
        
      // If Collect at stage3  then the format must be:
//        new SetTurn(state.getPlayerId(turnOfColor.getOppositeColor())),
//        new Set(STAGE, stage3),
//        new Set(CLAIM, noMatch),

      List<Operation> expectedOperations = ImmutableList.<Operation>of(
          new SetTurn(state.getPlayerId(turnOfColor.getOppositeColor())),
          new Set(STAGE, stage3),
          new Set(CLAIM, noMatch));
      
      return expectedOperations;
    }
    
    @SuppressWarnings("unchecked")
    /** Return the operations on special case.*/
    List<Operation> doClaimMoveOnSpecialCase(ChineseTenState state, List<Operation> lastMove, 
        List<Integer> playerIds) {
        
        Color turnOfColor = state.getTurn();
        
        // Get the diff between last state W/B and last move W/B
        List<Integer> lastWorB = state.getWhiteOrBlack(turnOfColor);      
        Set setWorB = (Set) lastMove.get(3);
        List<Integer> lastMoveWorB = (List<Integer>) setWorB.getValue();
        List<Integer> diffWorB = subtract(lastWorB, lastMoveWorB);
        check(diffWorB.size() == 1, lastWorB, lastMoveWorB, diffWorB);
        
        // Get the diff between last state D and last move D
        List<Integer> lastD = state.getDeck();
        Set setD = (Set) lastMove.get(5);
        List<Integer> lastMoveD = (List<Integer>) setD.getValue();
        List<Integer> diffD = subtract(lastD, lastMoveD);
        check(diffD.size() == 3, lastD, lastMoveD, diffD);
        
        // Check whether sum is ten
        check(checkWhetherIsSpcecialCase(state, diffWorB, diffD), diffWorB, diffD);
        
        List<Integer> newCollection = concat(diffWorB, diffD);
        List<Integer> newWCOrBC = concat(newCollection, state.getWCOrBC(turnOfColor));       

      // If the requirement for special case is satisfied,  then the format must be:
//        new SetTurn(state.getPlayerId(turnOfColor)),
//        new Set(STAGE, stage1),
//        new Set(CLAIM, special),
//        new Set(turnOfColor.name(), lastMoveWorB),
//        new Set(turnOfColor.name() + "C", newWCOrBC),
//        new Set(D, lastMoveD),
//        new SetVisibility(C + diffWorB.get(0)));   

      List<Operation> expectedOperations = ImmutableList.<Operation>of(
          new SetTurn(state.getPlayerId(turnOfColor)),
          new Set(STAGE, stage1),
          new Set(CLAIM, special),
          new Set(turnOfColor.name(), lastMoveWorB),
          new Set(turnOfColor.name() + "C", newWCOrBC),
          new Set(D, lastMoveD),
          new SetVisibility(C + diffWorB.get(0)));
      return expectedOperations;
    }

    List<Integer> getIndicesInRange(int fromInclusive, int toInclusive) {
        List<Integer> keys = Lists.newArrayList();
        for (int i = fromInclusive; i <= toInclusive; i++) {
            keys.add(i);
        }
        return keys;
    }

    List<String> getCardsInRange(int fromInclusive, int toInclusive) {
        List<String> keys = Lists.newArrayList();
        for (int i = fromInclusive; i <= toInclusive; i++) {
            keys.add(C + i);
        }
        return keys;
    }

    String cardIdToString(int cardId) {
        checkArgument(cardId >= 0 && cardId < 52);
        int rank = (cardId / 4);
        String rankString = Rank.values()[rank].getFirstLetter();
        int suit = cardId % 4;
        String suitString = Suit.values()[suit].getFirstLetterLowerCase();
        return rankString + suitString;
    }

    /** Get Initial move at the beginning of the game.*/
    List<Operation> getInitialMove(int whitePlayerId, int blackPlayerId) {
        List<Operation> operations = Lists.newArrayList();
        // The order of operations: turn, isCheater, W, B, M, claim, C0...C51
        operations.add(new SetTurn(whitePlayerId));
        operations.add(new Set(STAGE, stage0));
        // set W and B hands, each with 12 cards at hand before start
        operations.add(new Set(W, getIndicesInRange(0, 11)));
        operations.add(new Set(B, getIndicesInRange(12, 23)));
        // set WC and BC for W player and B player, both are empty before start
        operations.add(new Set(WC, ImmutableList.of()));
        operations.add(new Set(BC, ImmutableList.of()));
        // D is the pile faced up for collect, has 4 cards before start
        operations.add(new Set(D, getIndicesInRange(48, 51)));
        // middle pile is empty
        operations.add(new Set(M, getIndicesInRange(24, 47)));
        // sets all 52 cards: set(C0,2h), �, set(C51,Ac)
        for (int i = 0; i < 52; i++) {
            operations.add(new Set(C + i, cardIdToString(i)));
        }
        // shuffle(C0,...,C51)
        operations.add(new Shuffle(getCardsInRange(0, 51)));
        // sets visibility
        for (int i = 0; i < 12; i++) {
            operations.add(new SetVisibility(C + i, ImmutableList
                    .of(whitePlayerId)));
        }
        for (int i = 12; i < 24; i++) {
            operations.add(new SetVisibility(C + i, ImmutableList
                    .of(blackPlayerId)));
        }
        for (int i = 24; i < 48; i++) {
            operations.add(new SetVisibility(C + i, ImmutableList
                    .<Integer> of()));
        }
        for (int i = 48; i < 52; i++) {
            operations.add(new SetVisibility(C + i));
        }

        return operations;
    }

    private void check(boolean val, Object... debugArguments) {
        if (!val) {
            throw new RuntimeException("We have a hacker! debugArguments="
                    + Arrays.toString(debugArguments));
        }
    }
    
    <T> List<T> concat(List<T> a, List<T> b) {
        return Lists.newArrayList(Iterables.concat(a, b));
    }

    <T> List<T> subtract(List<T> removeFrom, List<T> elementsToRemove) {
        check(removeFrom.containsAll(elementsToRemove), removeFrom,
                elementsToRemove);
        List<T> result = Lists.newArrayList(removeFrom);
        result.removeAll(elementsToRemove);
        check(removeFrom.size() == result.size() + elementsToRemove.size());
        return result;
    }
    
    /** Check whether the sum of collected cards is ten or other cases that the rule allows.*/
    boolean checkWhetherSumIsTen(ChineseTenState state, List<Integer> a, List<Integer> b) {
        check(a.size() == 1, a);
        check(b.size() == 1, b);
        //int aValue = a.get(0);
        //int bValue = b.get(0);
        
        int aValue = state.getCards().get(a.get(0)).get().getRank().getNumberfromRank();
        int bValue = state.getCards().get(b.get(0)).get().getRank().getNumberfromRank();
        
        if (aValue + bValue == 10) {
            return true; 
        } else if (aValue == 10 && bValue == 10) {
            return true;
        } else if (aValue == 11 && bValue == 11) {
            return true;
        } else if (aValue == 12 && bValue == 12) {
            return true;
        } else if (aValue == 13 && bValue == 13) {
            return true;
        } else {
            return false;
        }        
    }
    
    /** Check Whether collect 4 cards is legal.*/
    boolean checkWhetherIsSpcecialCase(ChineseTenState state, List<Integer> a, List<Integer> b) {
        Rank tmp = state.getCards().get(a.get(0)).get().getRank();
        
        for (Integer cardIndex : b) {          
            Card card = state.getCards().get(cardIndex).get();
            check(card.getRank().checkWhetherNumberIsSpecial(), card);
            if (card.getRank() != tmp) {
                return false;
            }               
            }
        return true;
    }
}

