package org.chineseten.client;

//import static com.google.common.base.Preconditions.checkArgument;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

//import java_cup.internal_error;



//import org.cheat.client.GameApi.Delete;
import org.chineseten.client.GameApi.EndGame;
//import org.cheat.client.GameApi.Shuffle;
import org.chineseten.client.GameApi.Set;
import org.chineseten.client.GameApi.SetVisibility;
import org.chineseten.client.GameApi.Shuffle;
import org.chineseten.client.GameApi.Operation;
//import org.chineseten.client.ChineseTenLgoic;
import org.chineseten.client.GameApi.VerifyMove;
import org.chineseten.client.GameApi.VerifyMoveDone;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;



//import com.gargoylesoftware.htmlunit.javascript.host.OfflineResourceList;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
//import com.google.common.collect.Iterables;
//import com.google.common.collect.Lists;

@RunWith(JUnit4.class)
public class ChineseTenLogicTest {

    ChineseTenLgoic chineseTenLgoic = new ChineseTenLgoic();
    
    private final int wId = 41;
    private final int bId = 42;
    private final int stage0 = 0;
    private final int stage1 = 1;
    private final int stage2 = 2;
    private final int stage3 = 3;
    private final String reset = "reset";
    private final String match = "match";
    //private final String noMatch = "nomatch";
    private final String flip = "flip";
    //private final String noFlip = "noflip";
    private final String playerId = "playerId";
    private static final String TURN = "turn"; // turn of which player (either W or B)
    private static final String STAGE = "stage"; // 0 for init, 4 for end, 1,2,3 for three stages
    private static final String W = "W"; // White hand
    private static final String B = "B"; // Black hand
    private static final String M = "M"; // Middle pile
    private static final String WC = "WC"; // Cards collected by W
    private static final String BC = "BC"; // Cards collected by B
    private static final String D = "D"; // Cards faced up around M
    //private static final String C = "C"; // Card key (C1 .. C54)
    private static final String CLAIM = "claim"; // we claim we have a cheater
    //private final List<Integer> visibleToW = ImmutableList.of(wId);
    //private final List<Integer> visibleToB = ImmutableList.of(bId);
    private final Map<String, Object> wInfo = ImmutableMap.<String, Object> of(playerId, wId);
    private final Map<String, Object> bInfo = ImmutableMap.<String, Object> of(playerId, bId);
    private final List<Map<String, Object>> playersInfo = ImmutableList.of(wInfo, bInfo);
    private final Map<String, Object> emptyState = ImmutableMap.<String, Object> of();
    private final Map<String, Object> nonEmptyState = ImmutableMap.<String, Object> of("k", "v");
    
    // Intermediate state for test stage1
    Map<String, Object> initialStateOfStage0CanCollect = ImmutableMap.<String, Object>builder()
            .put(TURN, W)
            .put(STAGE, stage0)
            .put(W, getIndicesInRange(0, 11))
            .put(B, getIndicesInRange(12, 23))
            .put(WC, ImmutableList.of())
            .put(BC, ImmutableList.of())
            .put(D, getIndicesInRange(48, 51))
            .put(M, getIndicesInRange(24, 47))
            .put("C11", "4s")
            .put("C51", "6h")
            .put("C10", "6d")
            .build();
    
    Map<String, Object> initialStateOfStage0CanNotCollect = ImmutableMap.<String, Object>builder()
            .put(TURN, W)
            .put(STAGE, stage0)
            .put(W, getIndicesInRange(0, 11))
            .put(B, getIndicesInRange(12, 23))
            .put(WC, ImmutableList.of())
            .put(BC, ImmutableList.of())
            .put(D, getIndicesInRange(48, 51))
            .put(M, getIndicesInRange(24, 47))
            .put("C11", "6s")
            .put("C51", "6h")
            .build();
    
   // Intermediate state for test stage2
    Map<String, Object> initialStateOfStage1CanFlip = ImmutableMap.<String, Object>builder()
            .put(TURN, W)
            .put(STAGE, stage1)
            .put(W, getIndicesInRange(0, 10))
            .put(B, getIndicesInRange(12, 23))
            .put(WC, ImmutableList.<Integer>of(11, 51))
            .put(BC, ImmutableList.of())
            .put(D, getIndicesInRange(48, 50))
            .put(M, getIndicesInRange(24, 47))
            .build();
    
 // Intermediate state for test stage3
    Map<String, Object> initialStateOfStage2CanCollect = ImmutableMap.<String, Object>builder()
            .put(TURN, W)
            .put(STAGE, stage2)
            .put(W, getIndicesInRange(0, 10))
            .put(B, getIndicesInRange(12, 23))
            .put(WC, ImmutableList.<Integer>of(11, 51))
            .put(BC, ImmutableList.of())
            .put(D, getIndicesInRange(47, 50))
            .put(M, getIndicesInRange(24, 46))
            .put("C47", "As")
            .put("C48", "9h")
            .build();
    
 // Intermediate state for test stage3
    Map<String, Object> initialStateOfStage2CanNotCollect = ImmutableMap.<String, Object>builder()
            .put(TURN, W)
            .put(STAGE, stage2)
            .put(W, getIndicesInRange(0, 10))
            .put(B, getIndicesInRange(12, 23))
            .put(WC, ImmutableList.<Integer>of(11, 51))
            .put(BC, ImmutableList.of())
            .put(D, getIndicesInRange(47, 50))
            .put(M, getIndicesInRange(24, 46))
            .put("C47", "As")
            .put("C48", "Ah")
            .build();
    
    //Intermediate state for test special initial case
    Map<String, Object> specialInitialState = ImmutableMap.<String, Object>builder()
            .put(TURN, W)
            .put(STAGE, stage0)
            .put(W, getIndicesInRange(0, 11))
            .put(B, getIndicesInRange(12, 23))
            .put(WC, ImmutableList.<Integer>of())
            .put(BC, ImmutableList.<Integer>of())
            .put(D, getIndicesInRange(48, 51))
            .put(M, getIndicesInRange(24, 47))
            .put("C48", "Ks")
            .put("C49", "Kh")
            .put("C50", "Kd")
            .put("C11", "Kc")
            .build();
    
    Map<String, Object> specialInitialState2 = ImmutableMap.<String, Object>builder()
            .put(TURN, W)
            .put(STAGE, stage0)
            .put(W, getIndicesInRange(0, 11))
            .put(B, getIndicesInRange(12, 23))
            .put(WC, ImmutableList.<Integer>of())
            .put(BC, ImmutableList.<Integer>of())
            .put(D, getIndicesInRange(48, 51))
            .put(M, getIndicesInRange(24, 47))
            .put("C48", "5s")
            .put("C49", "5h")
            .put("C50", "5d")
            .put("C11", "5c")
            .build();
    
    
    
    /* The entries used in the cheat game are:
     *   turn:W/B, stage:0/1/2/3/4, claim, W, B, WC, BC, C, M, C0...C51
     * When we send operations on these keys, it will always be in the above order.
     */
    
    // Below operations are used to test stage1
    private final List<Operation> claimWithWAndD = ImmutableList.<Operation>of(
            new Set(TURN, W),
            new Set(STAGE, stage1),
            new Set(CLAIM, match),
            new Set(W, getIndicesInRange(0, 10)),
            new Set(WC, ImmutableList.of(11, 51)),
            new Set(D, ImmutableList.of(48, 49, 50)),
            new SetVisibility("C11"));
    
    private final List<Operation> claimWithFourCards = ImmutableList.<Operation>of(
            new Set(TURN, W),
            new Set(STAGE, stage1),
            new Set(CLAIM, match),
            new Set(W, getIndicesInRange(0, 10)),
            new Set(WC, ImmutableList.<Integer>of(11, 48, 49, 50)),
            new Set(D, ImmutableList.<Integer>of(51)),
            new SetVisibility("C11")
            );
    
    private final List<Operation> claimWithTwoW = ImmutableList.<Operation>of(
            new Set(TURN, W),
            new Set(STAGE, stage1),
            new Set(CLAIM, match),
            new Set(W, getIndicesInRange(0, 9)),
            new Set(WC, ImmutableList.of(10, 11)),
            //new Set(D, ImmutableList.of(48, 49, 50)),
            new SetVisibility("C10"),
            new SetVisibility("C11"));
        
    // Below operations are used to test stage2
    private final List<Operation> shouldFlipOneCard = ImmutableList.<Operation>of(
            new Set(TURN, W),
            new Set(STAGE, stage2),
            new Set(CLAIM, flip),
            new Set(D, getIndicesInRange(47, 50)),
            new Set(M, getIndicesInRange(24, 46)),
            //new Set(D, ImmutableList.of(48, 49, 50)),
            new SetVisibility("C47"));
    
    private final List<Operation> shouldNotFlipTwoCards = ImmutableList.<Operation>of(
            new Set(TURN, W),
            new Set(STAGE, stage2),
            new Set(CLAIM, flip),
            new Set(D, getIndicesInRange(46, 50)),
            new Set(M, getIndicesInRange(24, 45)),
            //new Set(D, ImmutableList.of(48, 49, 50)),
            new SetVisibility("C47"),
            new SetVisibility("C46"));
    
 // Below operations are used to test stage3
    private final List<Operation> claimWithTwoDs = ImmutableList.<Operation>of(
            new Set(TURN, B),
            new Set(STAGE, stage3),
            new Set(CLAIM, match),
            new Set(WC, ImmutableList.of(11, 51, 47, 48)),
            new Set(D, ImmutableList.of(49, 50)));
    
    private final List<Operation> claimWithWrongTurn = ImmutableList.<Operation>of(
            new Set(TURN, W),
            new Set(STAGE, stage3),
            new Set(CLAIM, match),
            new Set(WC, ImmutableList.of(11, 51, 47, 48)),
            new Set(D, ImmutableList.of(49, 50)));
    
    
    private void assertMoveOk(VerifyMove verifyMove) {
        VerifyMoveDone verifyDone = new ChineseTenLgoic().verify(verifyMove);
        assertEquals(new VerifyMoveDone(), verifyDone);
    }

    private void assertHacker(VerifyMove verifyMove) {
        VerifyMoveDone verifyDone = new ChineseTenLgoic().verify(verifyMove);
        assertEquals(new VerifyMoveDone(verifyMove.getLastMovePlayerId(),
                "Hacker found"), verifyDone);
    }

    private VerifyMove move(int lastMovePlayerId,
            Map<String, Object> lastState, List<Operation> lastMove) {
        return new VerifyMove(wId, playersInfo,
        // in ChineseTen we never need to check the resulting state (the server makes
        // it, and the game doesn't have any hidden decisions such in Battleships)
                emptyState, lastState, lastMove, lastMovePlayerId);
    }

    private List<Integer> getIndicesInRange(int fromInclusive, int toInclusive) {
        return chineseTenLgoic.getIndicesInRange(fromInclusive, toInclusive);
      }

      
    @Test
    public void testGetIndicesInRange() {
        assertEquals(ImmutableList.of(3, 4),
                chineseTenLgoic.getIndicesInRange(3, 4));
    }

    private List<String> getCardsInRange(int fromInclusive, int toInclusive) {
        return chineseTenLgoic.getCardsInRange(fromInclusive, toInclusive);
    }
    
    @Test
    public void testCardsInRange() {
        assertEquals(ImmutableList.of("C3", "C4"), chineseTenLgoic.getCardsInRange(3, 4));
    }

    @Test
    public void testCardIdToString() {
        assertEquals("2c", chineseTenLgoic.cardIdToString(0));
        assertEquals("2d", chineseTenLgoic.cardIdToString(1));
        assertEquals("2h", chineseTenLgoic.cardIdToString(2));
        assertEquals("2s", chineseTenLgoic.cardIdToString(3));
        assertEquals("As", chineseTenLgoic.cardIdToString(51));
    }

    private List<Operation> getInitialOperations() {
        return chineseTenLgoic.getInitialMove(wId, bId);
    }

    // Initial Move Test, one OK, three hackers
    @Test
    public void testInitialMove() {
        assertMoveOk(move(wId, emptyState, getInitialOperations()));
    }
    
    @Test
    public void testInitialIllegalMoveByWrongPlayer() {
        assertHacker(move(bId, emptyState, getInitialOperations()));
    }

    @Test
    public void testInitialIllegalMoveFromNonEmptyState() {
        assertHacker(move(wId, nonEmptyState, getInitialOperations()));
    }

    @Test
    public void testInitialIllegalMoveWithExtraOperation() {
        List<Operation> initialOperations = getInitialOperations();
        initialOperations.add(new Set(M, ImmutableList.of()));
        assertHacker(move(wId, emptyState, initialOperations));
    }
    
    //Test for stage1
    @Test
    public void testStage1LegalMovebyW() {
        assertMoveOk(move(wId, initialStateOfStage0CanCollect, claimWithWAndD));

    }

    @Test
    public void testStage1IllegalMoveOfWrongCollectCards() {
        assertHacker(move(wId, initialStateOfStage0CanNotCollect, claimWithWAndD));
    }
    
    @Test
    public void testStage1IllegalMoveOfWrongCollectPlace() {
        assertHacker(move(wId, initialStateOfStage0CanCollect, claimWithTwoW));
    }
    
    // test for stage2   
    @Test
    public void testStage2LegalMovebyW() {
        assertMoveOk(move(wId, initialStateOfStage1CanFlip, shouldFlipOneCard));
    }
    
    @Test
    public void testStage2IllegalMovebyWrongPlayer() {
        assertHacker(move(bId, initialStateOfStage1CanFlip, shouldFlipOneCard));
    }
    
    @Test
    public void testStage2IllegalMovebyFlipTwoCards() {
        assertHacker(move(wId, initialStateOfStage1CanFlip, shouldNotFlipTwoCards));
    }
    
    //test for stage3
    @Test
    public void testStage3LegalMovebyW() {
        assertMoveOk(move(wId, initialStateOfStage2CanCollect, claimWithTwoDs));
    }
    
    @Test
    public void testStage3IllegalMoveOfWrongCollectCards() {
        assertHacker(move(wId, initialStateOfStage2CanNotCollect, claimWithTwoDs));
    }
    
    @Test
    public void testStage3IllegalMoveOfWrongTurn() {
        assertHacker(move(wId, initialStateOfStage2CanCollect, claimWithWrongTurn));
    }
    
    // test for special case
    @Test
    public void testSpecialLegalMovebyW() {
        assertMoveOk(move(wId, specialInitialState, claimWithFourCards));
    }
    
    @Test
    public void testSpecialIllegalMovebyOnlyCollectTwoCards() {
        assertHacker(move(wId, specialInitialState, claimWithWAndD));
    }
    
    public void testSpecialLegalMovebyW2() {
        assertMoveOk(move(wId, specialInitialState2, claimWithFourCards));
    }
    
    @Test
    public void testSpecialIllegalMovebyOnlyCollectTwoCards2() {
        assertHacker(move(wId, specialInitialState2, claimWithWAndD));
    }
    
    // test for endGame
    @Test
    public void testEndGame1() {
      Map<String, Object> state = ImmutableMap.<String, Object>builder()
          .put(TURN, W)
          .put(STAGE, stage1)
          .put(W, ImmutableList.<Integer>of(26))
          .put(B, ImmutableList.<Integer>of())
          .put(WC, getIndicesInRange(27, 50))
          .put(BC, getIndicesInRange(0, 25))
          .put(D, ImmutableList.<Integer>of(51))
          .put(M, ImmutableList.<Integer>of())
          .put("C26", "5s")
          .put("C51", "5h")
          .build();
      // The order of operations: turn, isCheater, W, B, M, claim, C0...C51
      List<Operation> operations = ImmutableList.<Operation>of(
          new Set(TURN, W),
          new Set(CLAIM, match),
          new Set(W, ImmutableList.<Integer>of()),
          new Set(WC, getIndicesInRange(26, 51)),
          new Set(D, ImmutableList.of()),
          new SetVisibility("C26"),
          new Shuffle(getCardsInRange(0, 51)),
          new EndGame(bId));

      assertMoveOk(move(wId, state, operations));
    }
    
    @Test
    public void testEndGame2() {
        Map<String, Object> state = ImmutableMap.<String, Object>builder()
                .put(TURN, W)
                .put(STAGE, stage0)
                .put(W, getIndicesInRange(0, 11))
                .put(B, getIndicesInRange(12, 23))
                .put(WC, ImmutableList.<Integer>of())
                .put(BC, ImmutableList.<Integer>of())
                .put(D, getIndicesInRange(48, 51))
                .put(M, getIndicesInRange(24, 47))
                .put("C48", "Ks")
                .put("C49", "Kh")
                .put("C50", "Kd")
                .put("C11", "Kc")
                .build();
      // The order of operations: turn, isCheater, W, B, M, claim, C0...C51
      List<Operation> operations = ImmutableList.<Operation>of(
          //new Set(key, value)
          new Set(CLAIM, reset),
          new Shuffle(getCardsInRange(0, 51)),
          new EndGame(bId));

      assertMoveOk(move(wId, state, operations));
    }
    
    
    


}
