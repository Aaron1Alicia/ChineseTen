package org.chineseten.client;

import static com.google.common.base.Preconditions.checkNotNull;
import java_cup.internal_error;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

/**
 * Representation of the cheat game state.
 * The game state uses these keys: stage, claim, W, B, M,  C0....C51
 * which are mapped to these fields: turn, white, black, middle, claim, cards
 */
public class ChineseTenState {
  private final Color turn;
  private final ImmutableList<Integer> playerIds;

  /**
   * Note that some of the entries will have null, meaning the card is not visible to us.
   */
  private final ImmutableList<Optional<Card>> cards;

  /**
   * Index of the white cards, each integer is in the range [0-54).
   */
  private final ImmutableList<Integer> white;
  private final ImmutableList<Integer> black;
  private final ImmutableList<Integer> whiteCollect;
  private final ImmutableList<Integer> blackCollect;
  private final ImmutableList<Integer> deck;
  private final ImmutableList<Integer> middle;
  private final Optional<Claim> claim;
  private final int stage;

  public ChineseTenState(Color turn,
      int stage, Optional<Claim> claim, ImmutableList<Integer> white,
      ImmutableList<Integer> black, ImmutableList<Integer> whiteCollect,  
      ImmutableList<Integer> blackCollect, ImmutableList<Integer> deck, 
      ImmutableList<Integer> middle, ImmutableList<Optional<Card>> cards
      , ImmutableList<Integer> playerIds) {
    super();
    this.turn = checkNotNull(turn);
    this.stage = stage;
    this.claim = claim;
    this.playerIds = checkNotNull(playerIds);    
    this.white = checkNotNull(white);
    this.black = checkNotNull(black);
    this.whiteCollect = checkNotNull(whiteCollect);
    this.blackCollect = checkNotNull(blackCollect);
    this.deck = checkNotNull(deck);
    this.middle = checkNotNull(middle);  
    this.cards = checkNotNull(cards);
  }

  public Color getTurn() {
    return turn;
  }

  public ImmutableList<Integer> getPlayerIds() {
    return playerIds;
  }

  public int getPlayerId(Color color) {
    return playerIds.get(color.ordinal());
  }

  public ImmutableList<Optional<Card>> getCards() {
    return cards;
  }

  public ImmutableList<Integer> getWhite() {
    return white;
  }

  public ImmutableList<Integer> getBlack() {
    return black;
  }
  
  public ImmutableList<Integer> getWhiteCollect() {
      return whiteCollect;
  }

  public ImmutableList<Integer> getBlackCollect() {
      return blackCollect;
  }
  
  public ImmutableList<Integer> getDeck() {
      return deck;
  }

  public ImmutableList<Integer> getWhiteOrBlack(Color color) {
    return color.isWhite() ? white : black;
  }
  
  public ImmutableList<Integer> getWCOrBC(Color color) {
      return color.isWhite() ? whiteCollect : blackCollect;
  }
  
  public ImmutableList<Integer> getOppositeCollection(Color color) {
      return color.isWhite() ? blackCollect : whiteCollect;
  }

  public ImmutableList<Integer> getMiddle() {
    return middle;
  }

  public Optional<Claim> getClaim() {
    return claim;
  }
  
  public int getStage() {
    return stage;
  }
}
