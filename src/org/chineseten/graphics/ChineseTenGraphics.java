package org.chineseten.graphics;

import java.util.Collections;
import java.util.List;

import java_cup.internal_error;

import org.chineseten.client.Card;
import org.chineseten.client.Card.Rank;
import org.chineseten.client.ChineseTenPresenter;
import org.chineseten.client.ChineseTenPresenter.ChineseTenMessage;
//import org.chineseten.client.ChineseTenPresenter;
//import org.cheat.client.CheatPresenter.CheaterMessage;
import org.chineseten.client.Claim;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 * Graphics for the game of cheat.
 */
public class ChineseTenGraphics extends Composite implements ChineseTenPresenter.View {
  public interface ChineseTenGraphicsUiBinder extends UiBinder<Widget, ChineseTenGraphics> {
  }

  @UiField
  HorizontalPanel opponentArea;
  @UiField
  HorizontalPanel playerArea;
  @UiField
  HorizontalPanel selectedArea;
  @UiField
  HorizontalPanel middleArea;
  @UiField
  HorizontalPanel opponentCollectionArea;
  @UiField
  HorizontalPanel deckArea;
  @UiField
  HorizontalPanel playerCollecionArea;
  @UiField
  Button claimBtn;
  @UiField
  Button claimBtnOfDeck;
  
  //private boolean enableClicks = false;
  private boolean enableClicksForHand = false;
  private boolean enableClicksForDeck = false;
  
  private final CardImageSupplier cardImageSupplier;
  private ChineseTenPresenter presenter;

  public ChineseTenGraphics() {
    CardImages cardImages = GWT.create(CardImages.class);
    this.cardImageSupplier = new CardImageSupplier(cardImages);
    ChineseTenGraphicsUiBinder uiBinder = GWT.create(ChineseTenGraphicsUiBinder.class);
    initWidget(uiBinder.createAndBindUi(this));
  }

  private List<Image> createBackCards(int numOfCards) {
    List<CardImage> images = Lists.newArrayList();
    for (int i = 0; i < numOfCards; i++) {
      images.add(CardImage.Factory.getBackOfCardImage());
    }
    return createImages(images, false);
  }

  private List<Image> createCardImages(List<Card> cards, boolean withClick) {
    List<CardImage> images = Lists.newArrayList();
    for (Card card : cards) {
      images.add(CardImage.Factory.getCardImage(card));
    }
    return createImages(images, withClick);
  }
  
  private List<Image> createCardImagesInHand(List<Card> cards, boolean withClick) {
      List<CardImage> images = Lists.newArrayList();
      for (Card card : cards) {
        images.add(CardImage.Factory.getCardImage(card));
      }
      return createImagesInHand(images, withClick);
    }
  
  private List<Image> createCardImagesInDeck(List<Card> cards, boolean withClick) {
      List<CardImage> images = Lists.newArrayList();
      for (Card card : cards) {
        images.add(CardImage.Factory.getCardImage(card));
      }
      return createImagesInDeck(images, withClick);
    }

  private List<Image> createImages(List<CardImage> images, boolean withClick) {
    List<Image> res = Lists.newArrayList();
    for (CardImage img : images) {
     // final CardImage imgFinal = img;
      Image image = new Image(cardImageSupplier.getResource(img));
//      if (withClick) {
//        image.addClickHandler(new ClickHandler() {
//          @Override
//          public void onClick(ClickEvent event) {
//            if (enableClicks) {
//              presenter.cardSelected(imgFinal.card);
//            }
//          }
//        });
//      }
      res.add(image);
    }
    return res;
  }
  
  private List<Image> createImagesInHand(List<CardImage> images, boolean withClick) {
      List<Image> res = Lists.newArrayList();
      for (CardImage img : images) {
        final CardImage imgFinal = img;
        Image image = new Image(cardImageSupplier.getResource(img));
        if (withClick) {
          image.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              if (enableClicksForHand) {
                presenter.cardSelectedInHand(imgFinal.card);
              }
            }
          });
        }
        res.add(image);
      }
      return res;
    }
  
  private List<Image> createImagesInDeck(List<CardImage> images, boolean withClick) {
      List<Image> res = Lists.newArrayList();
      for (CardImage img : images) {
        final CardImage imgFinal = img;
        Image image = new Image(cardImageSupplier.getResource(img));
        if (withClick) {
          image.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              if (enableClicksForDeck) {
                presenter.cardSelectedInDeck(imgFinal.card);
              }
            }
          });
        }
        res.add(image);
      }
      return res;
    }

  private void placeImages(HorizontalPanel panel, List<Image> images) {
    panel.clear();
    Image last = images.isEmpty() ? null : images.get(images.size() - 1);
    for (Image image : images) {
      FlowPanel imageContainer = new FlowPanel();
      imageContainer.setStyleName(image != last ? "imgShortContainer" : "imgContainer");
      imageContainer.add(image);
      panel.add(imageContainer);
    }
  }

  private void alertStageMessage(ChineseTenMessage chineseTenMessage) {
    String message = "";
    List<String> options = Lists.newArrayList();

    switch (chineseTenMessage) {
      case STAGE_ONE:
        message += "This is stage1, please collect one card from yourarea and "
                + "one card from the deck for which the sum of the two cards "
                + "are ten";
        break;
      case STAGE_TWO:
        message += "This is stage2, please flip one card from the middle and put"
                + "it faced up in the deck area .";
        break;
      case STAGE_THREE:
        message += "This is stage3, please collect two cards from the deck,"
                + " for which the sume of the two cards are ten";
//        options.add("Probably told the truth");
//        options.add(callCheatOption);
        break;
      default:
          message += "stage0 or endgame!";
        break;
    }
    if (message.isEmpty()) {
      options.add("Initializing...");
    }
    if (options.isEmpty()) {
      options.add("OK");
    }
    new PopupChoices(message, options,
        new PopupChoices.OptionChosen() {
      @Override
      public void optionChosen(String option) {
//        if (option.equals(callCheatOption)) {
//          presenter.declaredCheater();
//        }
      }
    }).center();
  }

  private void disableClicks() {
    claimBtn.setEnabled(false);
    claimBtnOfDeck.setEnabled(false);
    enableClicksForHand = false;
    enableClicksForDeck = false;
  }

  @UiHandler("claimBtn")
  void onClickClaimBtn(ClickEvent e) {
    disableClicks();
    presenter.finishedSelectingCardsInHand();
  }
  
  @UiHandler("claimBtnOfDeck")
  void onClickClaimBtnOfDeck(ClickEvent e) {
    disableClicks();
    presenter.finishedSelectingCardsInDeckForStage1();
  }

  @Override
  public void setPresenter(ChineseTenPresenter chineseTenPresenter) {
    this.presenter = chineseTenPresenter;
  }
  
  @Override
  public void setViewerState(int numberOfWhiteCards, int numberOfBlackCards,
          int numberOfCardsInWhiteCollection, int numberOfCardsInBlackCollecion,
          int numberOfCardsInDeck, int numberOfCardsInMiddlePile,
          ChineseTenMessage chineseTenMessage) {
      
  }
  
  @Override
  public void setPlayerState(int numberOfOpponentCards, int numberOfCardsInMiddlePile,
          List<Card> myCardsInHand, List<Card> myCardsInCollection,
          List<Card> cardsInDeck, List<Card> cardsOfOpponentInCollection,
          ChineseTenMessage chineseTenMessage) {    
      Collections.sort(myCardsInHand);
      Collections.sort(myCardsInCollection);
      Collections.sort(cardsInDeck);
      Collections.sort(cardsOfOpponentInCollection);
      placeImages(playerArea, createCardImagesInHand(myCardsInHand, false));
      placeImages(selectedArea, ImmutableList.<Image>of());
      placeImages(opponentArea, createBackCards(numberOfOpponentCards));
      placeImages(middleArea, createBackCards(numberOfCardsInMiddlePile));
      placeImages(deckArea, createCardImagesInDeck(cardsInDeck, false));
      placeImages(opponentCollectionArea, createCardImages(cardsOfOpponentInCollection, false));
      placeImages(playerCollecionArea, createCardImages(myCardsInCollection, false));
      alertStageMessage(chineseTenMessage);
      disableClicks();     
  }

  @Override
  public void chooseNextCardInHand(List<Card> selectedCardsInHand, List<Card> remainingCards) {
    Collections.sort(selectedCardsInHand);
    Collections.sort(remainingCards);
    enableClicksForHand = true;
    placeImages(playerArea, createCardImagesInHand(remainingCards, true));
    placeImages(selectedArea, createCardImagesInHand(selectedCardsInHand, true));
    claimBtn.setEnabled(!selectedCardsInHand.isEmpty());
  }
  
  
  //void chooseNextCardInHand(List<Card> selectedCardsInHand, List<Card> remainingCards);
  
  public void chooseNextCardInDeck(List<Card> selectedCardsInDeck, List<Card> remainingCards) {
      Collections.sort(selectedCardsInDeck);
      Collections.sort(remainingCards);
      enableClicksForDeck = true;
      placeImages(playerArea, createCardImagesInDeck(remainingCards, true));
      placeImages(selectedArea, createCardImagesInDeck(selectedCardsInDeck, true));
      claimBtnOfDeck.setEnabled(!selectedCardsInDeck.isEmpty());  
  }
  
  public void flipOneCardIfThereisCardsLeftInMiddlePile(List<Card> selectedCardsInDeck, 
          List<Card> remainingCards) {
      
  }

}
