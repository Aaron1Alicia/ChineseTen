package org.chineseten.client;


import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;
import java.util.Map;

//import org.chineseten.client.GameApi;
import org.chineseten.client.Card.Rank;
import org.chineseten.client.Card.Suit;
import org.chineseten.client.ChineseTenPresenter.ChineseTenMessage;
//import org.chineseten.client.ChinPresenter.CheaterMessage;
import org.chineseten.client.ChineseTenPresenter.View;
import org.game_api.GameApi;
import org.game_api.GameApi.Container;
import org.game_api.GameApi.Operation;
import org.game_api.GameApi.Set;
import org.game_api.GameApi.SetTurn;
import org.game_api.GameApi.SetVisibility;
import org.game_api.GameApi.UpdateUI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/** Tests for {@link CheatPresenter}.
 * Test plan:
 * There are several interesting states:
 * 1) empty state
 * 2) stage1: you need to collect one card from W/B and one card from D, move to WC/BC
 * 3) stage2: you need to flip one card from M to D and make it faced up
 * 4) stage3: you need to collect two cards from D and move to WC/BC
 * There are several interesting yourPlayerId:
 * 1) white player
 * 2) black player
 * 3) viewer
 * For each one of these states and for each yourPlayerId,
 * I will test what methods the presenters calls on the view and container.
 * In addition I will also test the interactions between the presenter and view, i.e.,
 * the view can call one of these methods:
 * 1) cardSelected
 * 2) finishedSelectingCards
 * 3) rankSelected
 * 4) declaredCheater
 */
@RunWith(JUnit4.class)
public class ChineseTenPresenterTest {
  /** The class under test. */
  private ChineseTenPresenter chineseTenPresenter;
  private final ChineseTenLgoic chineseTenLgoic = new ChineseTenLgoic();
  private View mockView;
  private Container mockContainer;

  private static final String PLAYER_ID = "playerId";
  private final String wId = "42";
  private final String bId = "43";
  private final int stage0 = 0;
  private final int stage1 = 1;
  private final int stage2 = 2;
  private final int stage3 = 3;
//  private final String reset = "reset";
  private final String match = "match";
  private final String noMatch = "noMatch";
  //private final String special = "special";
  //private final String noMatch = "nomatch";
  //private final String flip = "flip";
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
  private static final String C = "C"; // Card key (C1 .. C54)
  private static final String CLAIM = "claim"; 
  private final String viewerId = GameApi.VIEWER_ID;
  
  
  private final ImmutableMap<String, Object> wInfo =
      ImmutableMap.<String, Object>of(PLAYER_ID, wId);
  private final ImmutableMap<String, Object> bInfo =
      ImmutableMap.<String, Object>of(PLAYER_ID, bId);
  private final ImmutableList<Map<String, Object>> playersInfo =
      ImmutableList.<Map<String, Object>>of(wInfo, bInfo);

  /* The interesting states that I'll test. */
  private final ImmutableMap<String, Object> emptyState = ImmutableMap.<String, Object>of();
  private final ImmutableMap<String, Object> stateForOne =
      createState(12, 12, 0, 0, 4, 24, stage0);
  private final ImmutableMap<String, Object> stateForTwo =
          createState(12, 12, 0, 0, 4, 24, stage1);
  private final ImmutableMap<String, Object> stateForThree =
          createState(12, 12, 0, 0, 4, 24, stage2);


  @Before
  public void runBefore() {
    mockView = Mockito.mock(View.class);
    mockContainer = Mockito.mock(Container.class);
    chineseTenPresenter = new ChineseTenPresenter(mockView, mockContainer);
    verify(mockView).setPresenter(chineseTenPresenter);
  }

  @After
  public void runAfter() {
    // This will ensure I didn't forget to declare any extra interaction the mocks have.
    verifyNoMoreInteractions(mockContainer);
    verifyNoMoreInteractions(mockView);
  }

  @Test
  public void testEmptyStateForW() {
    chineseTenPresenter.updateUI(createUpdateUI(wId, wId, emptyState));
    verify(mockContainer).sendMakeMove(chineseTenLgoic.getInitialMove(wId, bId));
  }

  @Test
  public void testEmptyStateForB() {
    chineseTenPresenter.updateUI(createUpdateUI(bId, wId, emptyState));
  }

  @Test
  public void testEmptyStateForViewer() {
    chineseTenPresenter.updateUI(createUpdateUI(viewerId, wId, emptyState));
  }

  @Test
  public void testStage1ForWTurnOfW() {
    chineseTenPresenter.updateUI(createUpdateUI(wId, wId, stateForOne));
    verify(mockView).setPlayerState(12, 24, getCards(0, 12), getCards(0, 0), 
            getCards(24, 28), getCards(0, 0), ChineseTenMessage.STAGE_ONE);
    verify(mockView).chooseNextCardInHand(ImmutableList.<Card>of(), getCards(0, 12));
  }
  
  
  @Test
  public void testStage1ForWTurnOfWClaim() {
    chineseTenPresenter.updateUI(createUpdateUI(wId, wId, stateForOne));
    verify(mockView).setPlayerState(12, 24, getCards(0, 12), getCards(0, 0), 
            getCards(24, 28), getCards(0, 0), ChineseTenMessage.STAGE_ONE);
    verify(mockView).chooseNextCardInHand(ImmutableList.<Card>of(), getCards(0, 12));
    chineseTenPresenter.cardSelectedInHand(getCards(0, 1).get(0));
    verify(mockView).chooseNextCardInHand(ImmutableList.<Card>of(getCards(0, 1).get(0)), 
            chineseTenLgoic.subtract(getCards(0, 12), getCards(0, 1)));
    chineseTenPresenter.finishedSelectingCardsInHand();
    verify(mockView).chooseNextCardInDeck(getCards(24, 24), getCards(24, 28));
    chineseTenPresenter.cardSelectedInDeck(getCards(24, 25).get(0));
    verify(mockView).chooseNextCardInDeck(getCards(24, 25), 
            chineseTenLgoic.subtract(getCards(24, 28), getCards(24, 25)));
    chineseTenPresenter.finishedSelectingCardsInDeckForStage();
    
    List<Operation> claimWithWAndD = ImmutableList.<Operation>of(
            new SetTurn(wId),
            new Set(STAGE, stage1),
            new Set(CLAIM, match),
            new Set("W", chineseTenLgoic.getIndicesInRange(1, 11)),
            new Set("WC", ImmutableList.<Integer>of(0, 24)),
            new Set("D", chineseTenLgoic.getIndicesInRange(25, 27)),
            new SetVisibility("C0"));
    verify(mockContainer).sendMakeMove(claimWithWAndD);
    
  } 
  
  @Test
  public void testStage1ForWTurnOfWNoClaim() {
    chineseTenPresenter.updateUI(createUpdateUI(wId, wId, stateForOne));
    verify(mockView).setPlayerState(12, 24, getCards(0, 12), getCards(0, 0), 
            getCards(24, 28), getCards(0, 0), ChineseTenMessage.STAGE_ONE);
    verify(mockView).chooseNextCardInHand(ImmutableList.<Card>of(), getCards(0, 12));
    chineseTenPresenter.cardSelectedInHand(getCards(0, 1).get(0));
    verify(mockView).chooseNextCardInHand(ImmutableList.<Card>of(getCards(0, 1).get(0)), 
            chineseTenLgoic.subtract(getCards(0, 12), getCards(0, 1)));
    chineseTenPresenter.finishedSelectingCardsInHand();
    //verify(mockView).chooseNextCardInDeck(getCards(24, 24), getCards(24, 28));
    chineseTenPresenter.cardSelectedInDeck(getCards(24, 25).get(0));
    verify(mockView).chooseNextCardInDeck(getCards(24, 25), 
            chineseTenLgoic.subtract(getCards(24, 28), getCards(24, 25)));
    chineseTenPresenter.cardSelectedInDeck(getCards(24, 25).get(0));
    verify(mockView, times(2)).chooseNextCardInDeck(ImmutableList.<Card>of(), getCards(24, 28));
    chineseTenPresenter.finishedSelectingCardsInDeckForStage();
    
    List<Operation> expected = ImmutableList.<Operation>of(
            new SetTurn(wId),
            new Set(STAGE, stage1),
            new Set(CLAIM, noMatch),
            new Set("W", chineseTenLgoic.getIndicesInRange(1, 11)),
            new Set("D", concat(chineseTenLgoic.getIndicesInRange(0, 0), 
                    chineseTenLgoic.getIndicesInRange(24, 27))),
            new SetVisibility("C0"));
    verify(mockContainer).sendMakeMove(expected);
    
  } 

  @Test
  public void testEmptyMiddleStateForWTurnOfB() {
      chineseTenPresenter.updateUI(createUpdateUI(bId, wId, stateForOne));
      verify(mockView).setPlayerState(12, 24, getCards(12, 24), getCards(0, 0), 
              getCards(24, 28), getCards(0, 0), ChineseTenMessage.STAGE_ONE);
   //   verify(mockView).chooseNextCardInHand(ImmutableList.<Card>of(), getCards(0, 12));
  }
  
  
  // Below are for stage2
  @Test
  public void testStage2ForWturnOfW() {
      chineseTenPresenter.updateUI(createUpdateUI(wId, wId, stateForTwo));
      verify(mockView).setPlayerState(12, 24, getCards(0, 12), getCards(0, 0), 
              getCards(24, 28), getCards(0, 0), ChineseTenMessage.STAGE_TWO);
      verify(mockView).flipOneCardIfThereisCardsLeftInMiddlePile(
              chineseTenLgoic.getIndicesInRange(28, 51));
              
      
  }
  
  @Test
  public void testStage2ForBturnOfW() {
      chineseTenPresenter.updateUI(createUpdateUI(bId, wId, stateForTwo));
      verify(mockView).setPlayerState(12, 24, getCards(12, 24), getCards(0, 0), 
              getCards(24, 28), getCards(0, 0), ChineseTenMessage.STAGE_TWO);
      
  }
  
//  @Test
//  public void testStage2ForWturnOfWFlip() {
//      chineseTenPresenter.updateUI(createUpdateUI(wId, wId, stateForTwo));
//      verify(mockView).setPlayerState(12, 24, getCards(0, 12), getCards(0, 0), 
//              getCards(24, 28), getCards(0, 0), ChineseTenMessage.STAGE_TWO);
//      verify(mockView).flipOneCardIfThereisCardsLeftInMiddlePile(
//              ImmutableList.<Card>of(), getCards(28, 52));
//      chineseTenPresenter.cardSelectedInMiddle(getCards(28, 29).get(0));
//      verify(mockView).flipOneCardIfThereisCardsLeftInMiddlePile(getCards(28, 29), 
//              chineseTenLgoic.subtract(getCards(28, 52), getCards(28, 29)));
//      chineseTenPresenter.finishedFlipCardsForStage2();
//      
//      List<Operation> flipOperation = ImmutableList.<Operation>of(
//              new SetTurn(wId),
//              new Set(STAGE, stage2),
//              new Set(CLAIM, flip),
//              new Set(D, chineseTenLgoic.concat(chineseTenLgoic.getIndicesInRange(28, 28), 
//                      chineseTenLgoic.getIndicesInRange(24, 27))),                      
//              new Set(M, chineseTenLgoic.getIndicesInRange(29, 51)),
//              new SetVisibility("C28"));
//      
//      verify(mockContainer).sendMakeMove(flipOperation);
//  }
  
  // Below are for stage3
  @Test
  public void testStage3ForWturnOfW() {
      chineseTenPresenter.updateUI(createUpdateUI(wId, wId, stateForThree));
      verify(mockView).setPlayerState(12, 24, getCards(0, 12), getCards(0, 0), 
              getCards(24, 28), getCards(0, 0), ChineseTenMessage.STAGE_THREE);
      verify(mockView).chooseNextCardInDeck(ImmutableList.<Card>of(), getCards(24, 28));
      
  }
  
  @Test
  public void testStage3ForBturnOfW() {
      chineseTenPresenter.updateUI(createUpdateUI(bId, wId, stateForThree));
      verify(mockView).setPlayerState(12, 24, getCards(12, 24), getCards(0, 0), 
              getCards(24, 28), getCards(0, 0), ChineseTenMessage.STAGE_THREE);
      //verify(mockView).chooseNextCardInDeck(ImmutableList.<Card>of(), getCards(24, 28));
      
  }
  
  // This test will run throuth if we delete the restraition of sum is ten
  @Test
  public void testStage3ForWTurnOfWClaim() {
      chineseTenPresenter.updateUI(createUpdateUI(wId, wId, stateForThree));
      verify(mockView).setPlayerState(12, 24, getCards(0, 12), getCards(0, 0), 
              getCards(24, 28), getCards(0, 0), ChineseTenMessage.STAGE_THREE);
      verify(mockView).chooseNextCardInDeck(ImmutableList.<Card>of(), getCards(24, 28));
      chineseTenPresenter.cardSelectedInDeck(getCards(24, 25).get(0));
      verify(mockView).chooseNextCardInDeck(getCards(24, 25), 
              chineseTenLgoic.subtract(getCards(24, 28), getCards(24, 25)));
      chineseTenPresenter.cardSelectedInDeck(getCards(25, 26).get(0));
      verify(mockView).chooseNextCardInDeck(getCards(24, 26), 
              chineseTenLgoic.subtract(getCards(24, 28), getCards(24, 26)));
      chineseTenPresenter.finishedSelectingCardsInDeckForStage3();
      
      List<Operation> claimWithTwoDs = ImmutableList.<Operation>of(
              new SetTurn(bId),
              new Set(STAGE, stage3),
              new Set(CLAIM, match),
              new Set("WC", ImmutableList.<Integer>of(24, 25)),
              new Set("D", ImmutableList.<Integer>of(26, 27)));
      verify(mockContainer).sendMakeMove(claimWithTwoDs);      
    } 
  
  @Test
  public void testStage3ForWTurnOfWNoClaim() {
      chineseTenPresenter.updateUI(createUpdateUI(wId, wId, stateForThree));
      verify(mockView).setPlayerState(12, 24, getCards(0, 12), getCards(0, 0), 
              getCards(24, 28), getCards(0, 0), ChineseTenMessage.STAGE_THREE);
      verify(mockView).chooseNextCardInDeck(ImmutableList.<Card>of(), getCards(24, 28));
//      chineseTenPresenter.cardSelectedInDeck(getCards(24, 25).get(0));
//      verify(mockView).chooseNextCardInDeck(getCards(24, 25), 
//              chineseTenLgoic.subtract(getCards(24, 28), getCards(24, 25)));
//      chineseTenPresenter.cardSelectedInDeck(getCards(25, 26).get(0));
//      verify(mockView).chooseNextCardInDeck(getCards(24, 26), 
//              chineseTenLgoic.subtract(getCards(24, 28), getCards(24, 26)));
      chineseTenPresenter.finishedSelectingCardsInDeckForStage3();
      
      List<Operation> claimWithTwoDs = ImmutableList.<Operation>of(
              new SetTurn(bId),
              new Set(STAGE, stage3),
              new Set(CLAIM, noMatch));
      verify(mockContainer).sendMakeMove(claimWithTwoDs);      
    } 
  
  

  private List<Card> getCards(int fromInclusive, int toExclusive) {
    List<Card> cards = Lists.newArrayList();
    for (int i = fromInclusive; i < toExclusive; i++) {
      Rank rank = Rank.values()[i / 4];
      Suit suit = Suit.values()[i % 4];
      cards.add(new Card(suit, rank));
    }
    return cards;
  }

  private ImmutableMap<String, Object> createState(
      int numberOfWhiteCards, int numberOfBlackCards, 
      int numberOfWhiteCollection, int numberOfBlackCollection,
      int numberOfDeck, int numberOfMiddle,
      int stage) {
    Map<String, Object> state = Maps.newHashMap();
    
    state.put(STAGE, stage);
    int offset = 0;
    
    state.put(W, chineseTenLgoic.getIndicesInRange(0, numberOfWhiteCards - 1));
    offset += numberOfWhiteCards;
    
    state.put(B, chineseTenLgoic.getIndicesInRange(offset,
        offset + numberOfBlackCards - 1));
    offset += numberOfBlackCards;
    
    state.put(WC, chineseTenLgoic.getIndicesInRange(offset, offset + numberOfWhiteCollection - 1));
    offset += numberOfWhiteCollection;
    
    state.put(BC, chineseTenLgoic.getIndicesInRange(offset, offset + numberOfBlackCollection - 1));
    offset += numberOfBlackCollection;
    
    
    state.put(D, chineseTenLgoic.getIndicesInRange(offset, offset + numberOfDeck - 1));
    offset += numberOfDeck;
    
    state.put(M, chineseTenLgoic.getIndicesInRange(offset, 51));
      
    // We just reveal all the cards (hidden cards are not relevant for our testing).
    int i = 0;
    for (Card card : getCards(0, 52)) {
      state.put(C + (i++),
          card.getRank().getFirstLetter() + card.getSuit().getFirstLetterLowerCase());
    }
    return ImmutableMap.copyOf(state);
  }

  private UpdateUI createUpdateUI(
      String yourPlayerId, String turnOfPlayerId, Map<String, Object> state) {
    // Our UI only looks at the current state
    // (we ignore: lastState, lastMovePlayerId, playerIdToNumberOfTokensInPot)
    return new UpdateUI(yourPlayerId, playersInfo, state,
        emptyState, // we ignore lastState
        ImmutableList.<Operation>of(new SetTurn(turnOfPlayerId)),
        null,
        ImmutableMap.<String, Integer>of());
  }
  
  <T> List<T> concat(List<T> a, List<T> b) {
      return Lists.newArrayList(Iterables.concat(a, b));
  }
}
