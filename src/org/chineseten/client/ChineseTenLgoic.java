package org.chineseten.client;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;

//import java_cup.internal_error;

import org.chineseten.client.GameApi.Operation;
import org.chineseten.client.GameApi.Set;
import org.chineseten.client.GameApi.SetVisibility;
import org.chineseten.client.GameApi.Shuffle;
import org.chineseten.client.Card.Rank;
import org.chineseten.client.Card.Suit;
import org.chineseten.client.GameApi.VerifyMove;
import org.chineseten.client.GameApi.VerifyMoveDone;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class ChineseTenLgoic {
    
  private static final String C = "C"; // Card key (C0 .. C51)
  private final int wId = 41;
  private final int bId = 42;
  private final int stage0 = 0;
  private final int stage1 = 1;
  private final int stage2 = 2;
  private final int stage3 = 3;
  //private final int Stage
  private final String playerId = "playerId";
  private static final String TURN = "turn"; // turn of which player (either W or B)
  private static final String STAGE = "stage"; // 0 for init, 4 for end, 1,2,3 for three stages
  private static final String W = "W"; // White hand
  private static final String B = "B"; // Black hand
  private static final String M = "M"; // Middle pile
  private static final String WC = "WC"; // Cards collected by W
  private static final String BC = "BC"; // Cards collected by B
  private static final String D = "D"; //
    
    
  public VerifyMoveDone verify(VerifyMove verifyMove) {
  // TODO: I will implement this method in HW2
   return new VerifyMoveDone();
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
    
    List<Operation> getInitialMove(int whitePlayerId, int blackPlayerId) {
        List<Operation> operations = Lists.newArrayList();
        // The order of operations: turn, isCheater, W, B, M, claim, C0...C51
        operations.add(new Set(TURN, W));
        operations.add(new Set(STAGE, stage0));
        // set W and B hands, each with 12 cards at hand before start
        operations.add(new Set(W, getIndicesInRange(0, 11)));
        operations.add(new Set(B, getIndicesInRange(12, 23)));
        // set WC and BC for W player and B player, both are empty before start
        operations.add(new Set(WC, ImmutableList.of()));
        operations.add(new Set(BC, ImmutableList.of()));
        // D is the pile faced up for collect, has 4 cards before start
        operations.add(new Set(D,getIndicesInRange(48, 51)));       
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
          operations.add(new SetVisibility(C + i, ImmutableList.of(whitePlayerId)));
        }
        for (int i = 12; i < 24; i++) {
          operations.add(new SetVisibility(C + i, ImmutableList.of(blackPlayerId)));
        }
        for (int i = 24; i < 48; i++) {
            operations.add(new SetVisibility(C + i, ImmutableList.<Integer>of()));
        }
        for (int i = 48; i < 52; i++) {
            operations.add(new SetVisibility(C + i));
        }
        
        
        return operations;
      }
     
}
