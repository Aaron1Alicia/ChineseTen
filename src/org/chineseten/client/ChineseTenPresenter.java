package org.chineseten.client;

import java.util.Arrays;
import java.util.List;

import java_cup.internal_error;

import org.game_api.GameApi.Container;
import org.game_api.GameApi.Operation;
import org.game_api.GameApi.Set;
import org.game_api.GameApi.SetTurn;
import org.game_api.GameApi.SetVisibility;
import org.game_api.GameApi.UpdateUI;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * The presenter that controls the ChineseTen game graphics.
 * We use the MVP pattern:
 * the model is {@link ChineseTenState},
 * the view will have the ChineseTen graphics and 
 * it will implement {@link ChineseTenPresenter.View},
 * and the presenter is {@link ChineseTenPresenter}.
 */
public class ChineseTenPresenter {
  /**
   * The possible ChineseTen messages.
   * The ChineseTen related messages are:
   * STAGE_ONE: The player collects one card from W/B and one card from D and
   * move them to WC/BC if rule allows
   * STAGE_TWO: The player needs to flip one card from M to D if there is still cards left in M
   * STAGE_THREE: the player collects two cards within the d and move them to WC/BC
   */
  public enum ChineseTenMessage {
    STAGE_ONE, STAGE_TWO, STAGE_THREE;
  }

  public interface View {
    /**
     * Sets the presenter. The viewer will call certain methods on the presenter, e.g.,
     * when a card is selected ({@link #cardSelected}),
     * when selection is done ({@link #finishedSelectingCards}), etc.
     *
     * The process of making a claim in stage1 looks as follows to the viewer:
     * 1) The viewer calls {@link #cardSelectedInHand} a couple of times
     * 2) The viewer calls {@link #finishedSelectingCardsInHand} to finalize his selection
     * 3) The viewer calls {@link #cardSelectedInDeck} a couple of times
     * 4) The viewer calls {@link #finishedSelectingCardsInDeckForStage1} to finalize his selection
     * The process of making a claim in stage1 looks as follows to the presenter:
     * 1) The presenter calls {@link #chooseNextCardInHand} and passes the current selection.
     * 2) The presenter calls {@link #chooseNextCardInDeck} and passes the current selection.
     * 
     * * The process of fliping a card in stage2 looks as follow to the viewer:
     * 1) The viewer calls {@link #cardSelectedInMiddle} and passes the selection
     * 2) The viewer calls {@link #finishedFlipCardsForStage2} to finalize his selection
     * The process of fliping a card in stage2 looks as follow to the presenter:
     * 1) The presenter calls {@link #flipOneCardIfThereisCardsLeftInMiddlePile}
     * 
     * * The process of making a claim in stage3 looks as follows to the viewer:
     * 1) The viewer calls {@link #cardSelectedInDeck} a couple of times
     * 2) The viewer calls {@link #finishedSelectingCardsInDeckForStage3} to finalize his selection
     * The process of making a claim in stage3 looks as follows to the presenter:
     * 2) The presenter calls {@link #chooseNextCardInDeck} and passes the current selection.
     */
    void setPresenter(ChineseTenPresenter chineseTenPresenter);

    /** Sets the state for a viewer, i.e., not one of the players. */
    void setViewerState(int numberOfWhiteCards, int numberOfBlackCards,
        int numberOfCardsInWhiteCollection, int numberOfCardsInBlackCollecion,
        int numberOfCardsInDeck, int numberOfCardsInMiddlePile,
        ChineseTenMessage chineseTenMessage);

    /**
     * Sets the state for a player (whether the player has the turn or not).
     */
    void setPlayerState(int numberOfOpponentCards, int numberOfCardsInMiddlePile,
        List<Card> myCardsInHand, List<Card> myCardsInCollection,
        List<Card> cardsInDeck, List<Card> cardsOfOpponentInCollection,
        ChineseTenMessage chineseTenMessage);

    void chooseNextCardInHand(List<Card> selectedCardsInHand, List<Card> remainingCards);
    
    void chooseNextCardInDeck(List<Card> selectedCardsInDeck, List<Card> remainingCards);
    
//    void flipOneCardIfThereisCardsLeftInMiddlePile(List<Card> selectedCardsInDeck, 
//            List<Card> remainingCards);   
    void flipOneCardIfThereisCardsLeftInMiddlePile(List<Integer> cardsInMiddle);   
  }
  
//  private final int wId = 41;
//  private final int bId = 42;
  private final int stage0 = 0;
  private final int stage1 = 1;
  private final int stage2 = 2;
  private final int stage3 = 3;
  //private final String reset = "reset";
  private final String match = "match";
  private final String noMatch = "noMatch";
  //private final String special = "special";
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
  private static final String C = "C"; // Card key (C1 .. C54)
  private static final String CLAIM = "claim"; 
  private final ChineseTenLgoic chineseTenLgoic = new ChineseTenLgoic();
  private final View view;
  private final Container container;
  /** A viewer doesn't have a color. */
  private Optional<Color> myColor;
  private Color opponentColor;
  private ChineseTenState chineseTenState;
  private List<String> playerIds;
  private String yourPlayerId;
  private List<Card> selectedCardsInHand;
  private List<Card> selectedCardsInDeck;
  //private List<Card> selectedCardsInMiddle;

  public ChineseTenPresenter(View view, Container container) {
    this.view = view;
    this.container = container;
    view.setPresenter(this);
  }

  /** Updates the presenter and the view with the state in updateUI. */
  public void updateUI(UpdateUI updateUI) {
    playerIds = updateUI.getPlayerIds();
    yourPlayerId = updateUI.getYourPlayerId();
    int yourPlayerIndex = updateUI.getPlayerIndex(yourPlayerId);
    myColor = yourPlayerIndex == 0 ? Optional.of(Color.W)
        : yourPlayerIndex == 1 ? Optional.of(Color.B) : Optional.<Color>absent();
        
    selectedCardsInHand = Lists.newArrayList();
    selectedCardsInDeck = Lists.newArrayList();
    //selectedCardsInMiddle = Lists.newArrayList();
    
    if (updateUI.getState().isEmpty()) {
      // The W player sends the initial setup move.
      if (myColor.isPresent() && myColor.get().isWhite()) {
        sendInitialMove(playerIds);
      }
      return;
    }
    
    Color turnOfColor = null;
    for (Operation operation : updateUI.getLastMove()) {
      if (operation instanceof SetTurn) {
        turnOfColor = Color.values()[playerIds.indexOf(((SetTurn) operation).getPlayerId())];
      }
    }
    
    chineseTenState = chineseTenLgoic.gameApiStateToChineseTenState(
            updateUI.getState(), turnOfColor, playerIds);

    if (updateUI.isViewer()) {
        // I will implement this later
//      view.setViewerState(chineseTenState.getWhite().size(), chineseTenState.getBlack().size(),
//          chineseTenState.getMiddle().size(), getCheaterMessage());
      return;
    }
    if (updateUI.isAiPlayer()) {
      // TODO: implement AI in a later HW!
        int stage = chineseTenState.getStage();
        if (stage0 == stage || stage3 == stage) {
            List<Card> aiCardsInhand = getMyCardsInHand();
            selectedCardsInHand.add(aiCardsInhand.get(0));
            finishedSelectingCardsInDeckForStage();
            
        } else if(stage1 == stage) {
            //List<Card> aiCardsInMiddle = getCardsInMiddle();
            finishedFlipCardsForStage2(0);
        } else if(stage2 == stage) {
            finishedSelectingCardsInDeckForStage();
        } else {
            throw new RuntimeException();
        }
      //container.sendMakeMove(..);
      return;
    }
    // Must be a player!
    Color myC = myColor.get();
    opponentColor = myC.getOppositeColor();
    int numberOfOpponentCards = chineseTenState.getWhiteOrBlack(opponentColor).size();
    int numberOfCardsInMiddlePile = chineseTenState.getMiddle().size();
    
    int stage = chineseTenState.getStage();
    if (stage0 == stage || stage3 == stage) {
        view.setPlayerState(numberOfOpponentCards, numberOfCardsInMiddlePile, getMyCardsInHand(), 
                getMyCardsInCollection(), getMyCardsInDeck(), getOpponentCardsInCollection(), 
                ChineseTenMessage.STAGE_ONE);
        if (isMyTurn()) {
            chooseNextCardInHand();
        }        
    } else if (stage1 == stage) {
        view.setPlayerState(numberOfOpponentCards, numberOfCardsInMiddlePile, getMyCardsInHand(), 
                getMyCardsInCollection(), getMyCardsInDeck(), getOpponentCardsInCollection(), 
                ChineseTenMessage.STAGE_TWO);
        if (isMyTurn()) {
            flipOneCardIfThereisCardsLeftInMiddlePile();            
        }
        
       
    } else if (stage2 == stage) {
        view.setPlayerState(numberOfOpponentCards, numberOfCardsInMiddlePile, getMyCardsInHand(), 
                getMyCardsInCollection(), getMyCardsInDeck(), getOpponentCardsInCollection(), 
                ChineseTenMessage.STAGE_THREE); 
        if (isMyTurn()) {
            chooseNextCardInDeck();           
        }
    } else {
        System.out.println("Unexpected stage!");
        return;
    }
  }

  private boolean isMyTurn() {
    return myColor.isPresent() && myColor.get() == chineseTenState.getTurn();
  }

  private List<Card> getMyCardsInHand() {
    List<Card> myCards = Lists.newArrayList();
    ImmutableList<Optional<Card>> cards = chineseTenState.getCards();
    for (Integer cardIndex : chineseTenState.getWhiteOrBlack(myColor.get())) {
      myCards.add(cards.get(cardIndex).get());
    }
    return myCards;
  }
  
  private List<Card> getMyCardsInDeck() {
      List<Card> myCards = Lists.newArrayList();
      ImmutableList<Optional<Card>> cards = chineseTenState.getCards();
      for (Integer cardIndex : chineseTenState.getDeck()) {
        myCards.add(cards.get(cardIndex).get());
      }
      return myCards;
    }
  
  private List<Card> getMyCardsInCollection() {
      List<Card> myCards = Lists.newArrayList();
      ImmutableList<Optional<Card>> cards = chineseTenState.getCards();
      for (Integer cardIndex : chineseTenState.getWCOrBC(myColor.get())) {
        myCards.add(cards.get(cardIndex).get());
      }
      return myCards;
    }
  
  private List<Card> getCardsInMiddle() {
      List<Card> myCards = Lists.newArrayList();
      ImmutableList<Optional<Card>> cards = chineseTenState.getCards();
      for (Integer cardIndex : chineseTenState.getMiddle()) {
        myCards.add(cards.get(cardIndex).get());
      }
      return myCards;
    }
  
  
  private List<Card> getOpponentCardsInCollection() {
      List<Card> myCards = Lists.newArrayList();
      ImmutableList<Optional<Card>> cards = chineseTenState.getCards();
      for (Integer cardIndex : chineseTenState.getWCOrBC(opponentColor)) {
        myCards.add(cards.get(cardIndex).get());
      }
      return myCards;
    }

  private void chooseNextCardInHand() {
      check(getMyCardsInHand().containsAll(selectedCardsInHand),
              getMyCardsInHand(), selectedCardsInHand);
    view.chooseNextCardInHand(
        ImmutableList.copyOf(selectedCardsInHand), 
        chineseTenLgoic.subtract(getMyCardsInHand(), selectedCardsInHand));
  }
  
  private void chooseNextCardInDeck() {
      view.chooseNextCardInDeck(ImmutableList.copyOf(selectedCardsInDeck), 
              chineseTenLgoic.subtract(getMyCardsInDeck(), selectedCardsInDeck));
    }
  
  private void flipOneCardIfThereisCardsLeftInMiddlePile() {
      view.flipOneCardIfThereisCardsLeftInMiddlePile(chineseTenState.getMiddle());    
  }

  private void check(boolean val) {
    if (!val) {
      throw new IllegalArgumentException();
    }
  }

  /**
   * Adds/remove the card from the {@link #selectedCards}.
   * The view can only call this method if the presenter called {@link View#chooseNextCard}.
   */
  public void cardSelectedInHand(Card card) {
    check(isMyTurn());
    if (selectedCardsInHand.contains(card)) {
      selectedCardsInHand.remove(card);
    } else if (selectedCardsInHand.size() == 0) {
      selectedCardsInHand.add(card);
    }
    chooseNextCardInHand();
  }
  
  public void cardSelectedInDeck(Card card) {
      check(isMyTurn());
      if (selectedCardsInDeck.contains(card)) {
        selectedCardsInDeck.remove(card);
      } else if (!selectedCardsInDeck.contains(card) && selectedCardsInDeck.size() < 3) {
        selectedCardsInDeck.add(card);
      }
      chooseNextCardInDeck();
    }
  

  /**
   * Finishes the card selection process.
   * The view can only call this method if the presenter called {@link View#chooseNextCard}
   * and more than one card was selected by calling {@link #cardSelected}.
   */
  public void finishedSelectingCardsInHand() {
    check(isMyTurn() && selectedCardsInHand.size() < 2);
    view.chooseNextCardInDeck(selectedCardsInDeck, 
            chineseTenLgoic.subtract(getMyCardsInDeck(), selectedCardsInDeck));
  }
  
  public void finishedSelectingCardsInDeckForStage() {
      check(isMyTurn() && selectedCardsInDeck.size() < 3);
      if (chineseTenState.getStage() == stage2) {
          
          check(selectedCardsInDeck.size() == 2 || selectedCardsInDeck.size() == 0,
                  chineseTenState.getStage());
          finishedSelectingCardsInDeckForStage3();  
          return;
      }
         
      check(chineseTenState.getStage() == stage0 || chineseTenState.getStage() == stage3, 
              chineseTenState.getStage());
      if (selectedCardsInDeck.size() == 1) {
          sendMatchMoveForStageOne();       
      } else if (selectedCardsInDeck.size() == 0 && selectedCardsInHand.size() == 1) {
          sendNoMatchMoveForStageOne();
      } else {        
           throw new RuntimeException("The operations in stage1 is not right!"
                          + "selectedCardsInDeck.size() = " + selectedCardsInDeck.size() + ","
                          + "selectedCardsInHand.size() = " + selectedCardsInHand.size() 
                          + "\n");    
      }
     
    }
  
  public void sendMatchMoveForStageOne() {
      
      List<Integer> myCardIndices = chineseTenState.getWhiteOrBlack(myColor.get());
      //List<Integer> myCollections = chineseTenState.getWCOrBC(myColor.get());
      Card wToRomove = selectedCardsInHand.get(0);
      Card dToRomove = selectedCardsInDeck.get(0);
      
      List<Integer> newWC = Lists.newArrayList();
      
      Integer w = myCardIndices.get(getMyCardsInHand().indexOf(wToRomove));
      Integer d = chineseTenState.getDeck().get(getMyCardsInDeck().indexOf(dToRomove));
      
      if (!checkWhetherSumIsTen(chineseTenState, (int) d, (int) w)) {
          fallBack();
          return;
      }
      
      
      
      List<Integer> myNewCardIndices = Lists.newArrayList();
      myNewCardIndices.addAll(myCardIndices);
      myNewCardIndices.remove(w);
      
      
      List<Integer> newDeck = Lists.newArrayList();
      newDeck.addAll(chineseTenState.getDeck());
      newDeck.remove(d);
      
      newWC.add(w);
      newWC.add(d);  
      
      List<Operation> operationsToSend = ImmutableList.<Operation>of(
              new SetTurn(yourPlayerId),
              new Set(STAGE, stage1),
              new Set(CLAIM, match),
              new Set(myColor.get().name(), myNewCardIndices),
              new Set(myColor.get().name() + "C", 
                      concat(newWC, chineseTenState.getWCOrBC(myColor.get()))),
              new Set(D, newDeck),
              new SetVisibility(new String(C + w))); 
      
      //container.sendMakeMove(operationsToSend);
      container.sendMakeMove(chineseTenLgoic.doClaimMoveOnStage1(
              chineseTenState, operationsToSend, playerIds));
  }
  
  public void sendNoMatchMoveForStageOne() {
      
      List<Integer> myCardIndices = chineseTenState.getWhiteOrBlack(myColor.get());
      Card wToRomove = selectedCardsInHand.get(0);
      Integer w = myCardIndices.get(getMyCardsInHand().indexOf(wToRomove));
      List<Integer> newDeck = Lists.newArrayList();
      newDeck.add(w);
      check(newDeck.size() == 1, newDeck.size());
      List<Integer> myNewCardIndices = Lists.newArrayList();
      myNewCardIndices.addAll(myCardIndices);
      myNewCardIndices.remove(w);
     
      
      List<Operation> operationsToSend = ImmutableList.<Operation>of(
              new SetTurn(yourPlayerId),
              new Set(STAGE, stage1),
              new Set(CLAIM, noMatch),
              new Set(myColor.get().name(), myNewCardIndices),
              new Set(D, concat(newDeck, chineseTenState.getDeck())),
              new SetVisibility(new String(C + w)));   
      //container.sendMakeMove(operationsToSend);
      container.sendMakeMove(
              chineseTenLgoic.doNoClaimMoveOnStage1(chineseTenState, operationsToSend, playerIds));
  }
  
  
  public void finishedSelectingCardsInDeckForStage3() {
      check(isMyTurn() && selectedCardsInDeck.size() == 2 || selectedCardsInDeck.size() == 0);
           
      
      if (selectedCardsInDeck.size() == 2) {
          sendMatchMoveForStageThree();
      } else if (selectedCardsInDeck.size() == 0) {
          sendNoMatchMoveForStageThree();
      } else {
          throw new RuntimeException("The operations in stage3 is not right!"
                  + "selectedCardsInDeck.size() = " + selectedCardsInDeck.size() 
                  + "\n");  
      }
      
    }
  
  public void sendMatchMoveForStageThree() {
      Card dToRomoveOne = selectedCardsInDeck.get(0);
      Card dToRomoveTwo = selectedCardsInDeck.get(1);
      
      
      List<Integer> newD = Lists.newArrayList();
      
      Integer d1 = chineseTenState.getDeck().get(getMyCardsInDeck().indexOf(dToRomoveOne));
      Integer d2 = chineseTenState.getDeck().get(getMyCardsInDeck().indexOf(dToRomoveTwo));
      
      newD.add(d1);
      newD.add(d2);    
      if (!checkWhetherSumIsTen(chineseTenState, (int) d1, (int) d2)) {
          fallBackForStage3();
          return;
      }
      
      List<Operation> operationsToSend = ImmutableList.<Operation>of(
              new SetTurn(chineseTenState.getPlayerId(myColor.get().getOppositeColor())),
              new Set(STAGE, stage3),
              new Set(CLAIM, match),
              new Set(myColor.get().name() + "C", 
                      chineseTenLgoic.concat(newD, chineseTenState.getWCOrBC(myColor.get()))),
              new Set(D, chineseTenLgoic.subtract(chineseTenState.getDeck(), newD)));
      
      //container.sendMakeMove(claimWithWAndD);
      container.sendMakeMove(chineseTenLgoic.doClaimMoveOnStage3(
              chineseTenState, operationsToSend, playerIds));
      
  }
  
  public void sendNoMatchMoveForStageThree() {
      List<Operation> operationsToSend = ImmutableList.<Operation>of(
              new SetTurn(chineseTenState.getPlayerId(myColor.get().getOppositeColor())),
              new Set(STAGE, stage3),
              new Set(CLAIM, noMatch));
      
      //container.sendMakeMove(claimWithWAndD);
      container.sendMakeMove(chineseTenLgoic.doNoClaimMoveOnStage3(
              chineseTenState, operationsToSend, playerIds));      
  }
  
  public void finishedFlipCardsForStage2(int index) {
      check(isMyTurn());    
      Integer m = chineseTenState.getMiddle().get(index);
     // List<Integer> myCardIndices = chineseTenState.getMiddle();
      List<Integer> newDeck = Lists.newArrayList();    
      //m = myCardIndices.get(getCardsInMiddle().indexOf(flip));
      newDeck.add(m);
      
      List<Operation> flipOperation = ImmutableList.<Operation>of(
              new SetTurn(yourPlayerId),
              new Set(STAGE, stage2),
              new Set(CLAIM, "flip"),
              new Set(D, chineseTenLgoic.concat(newDeck, chineseTenState.getDeck())),
              new Set(M, chineseTenLgoic.subtract(chineseTenState.getMiddle(), newDeck)),
              new SetVisibility(new String(C + m)));
      
      //container.sendMakeMove(flipOperation);
      container.sendMakeMove(chineseTenLgoic.doClaimMoveOnStage2(
              chineseTenState, flipOperation, playerIds));
    }

  private void sendInitialMove(List<String> playerIds) {
    String whitePlayerId = playerIds.get(0);
    String blackPlayerId = playerIds.get(1);
    container.sendMakeMove(chineseTenLgoic.getInitialMove(whitePlayerId, blackPlayerId));
  }
  
  
  // Below are some helper functions
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
  
  private void check(boolean val, Object... debugArguments) {
      if (!val) {
          throw new RuntimeException("We have a hacker! debugArguments="
                  + Arrays.toString(debugArguments));
      }
  }
  
  /** Check whether the sum of collected cards is ten or other cases that the rule allows.*/
  boolean checkWhetherSumIsTen(ChineseTenState state, int a, int b) {
//      check(a.size() == 1, a);
//      check(b.size() == 1, b);
      //int aValue = a.get(0);
      //int bValue = b.get(0);
      
      int aValue = state.getCards().get(a).get().getRank().getNumberfromRank();
      int bValue = state.getCards().get(b).get().getRank().getNumberfromRank();
      
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
  
  public void fallBack() {
      int numberOfOpponentCards = chineseTenState.getWhiteOrBlack(opponentColor).size();
      int numberOfCardsInMiddlePile = chineseTenState.getMiddle().size();
      
      view.setPlayerState(numberOfOpponentCards, numberOfCardsInMiddlePile, getMyCardsInHand(), 
              getMyCardsInCollection(), getMyCardsInDeck(), getOpponentCardsInCollection(), 
              ChineseTenMessage.STAGE_ONE);
      chooseNextCardInHand();
  }
  
  public void fallBackForStage3() {
      int numberOfOpponentCards = chineseTenState.getWhiteOrBlack(opponentColor).size();
      int numberOfCardsInMiddlePile = chineseTenState.getMiddle().size();
      
      view.setPlayerState(numberOfOpponentCards, numberOfCardsInMiddlePile, getMyCardsInHand(), 
              getMyCardsInCollection(), getMyCardsInDeck(), getOpponentCardsInCollection(), 
              ChineseTenMessage.STAGE_THREE);
      chooseNextCardInDeck();    
  }
  
}
