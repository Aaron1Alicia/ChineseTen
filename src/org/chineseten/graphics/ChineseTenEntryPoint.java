package org.chineseten.graphics;

import org.chineseten.client.ChineseTenLgoic;
import org.chineseten.client.ChineseTenPresenter;
import org.game_api.GameApi;
import org.game_api.GameApi.Game;
import org.game_api.GameApi.IteratingPlayerContainer;
import org.game_api.GameApi.UpdateUI;
import org.game_api.GameApi.VerifyMove;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class ChineseTenEntryPoint implements EntryPoint {
    
  // For web version
  //IteratingPlayerContainer container;
  // For emulator version
  GameApi.ContainerConnector container;
  ChineseTenPresenter chineseTenPresenter;

  @Override
  public void onModuleLoad() {
      Window.enableScrolling(false);
    Game game = new Game() {
      @Override
      public void sendVerifyMove(VerifyMove verifyMove) {
        container.sendVerifyMoveDone(new ChineseTenLgoic().verify(verifyMove));
      }

      @Override
      public void sendUpdateUI(UpdateUI updateUI) {
        chineseTenPresenter.updateUI(updateUI);
      }
    };
    
    // For web version
//    container = new IteratingPlayerContainer(game, 2);
//    ChineseTenGraphics chineseTenGraphics = new ChineseTenGraphics();
//    chineseTenPresenter =
//        new ChineseTenPresenter(chineseTenGraphics, container);
//    final ListBox playerSelect = new ListBox();
//    playerSelect.addItem("WhitePlayer");
//    playerSelect.addItem("BlackPlayer");
//    playerSelect.addItem("Viewer");
//    playerSelect.addChangeHandler(new ChangeHandler() {
//      @Override
//      public void onChange(ChangeEvent event) {
//        int selectedIndex = playerSelect.getSelectedIndex();
//        String playerId = selectedIndex == 2 ? GameApi.VIEWER_ID
//            : container.getPlayerIds().get(selectedIndex);
//        container.updateUi(playerId);
//      }
//    });
//    FlowPanel flowPanel = new FlowPanel();
//    flowPanel.add(chineseTenGraphics);
//    flowPanel.add(playerSelect);
//    RootPanel.get("mainDiv").add(flowPanel);
//    container.sendGameReady();
//    container.updateUi(container.getPlayerIds().get(0));
    // For web version
    
    // For emulator version
    container = new GameApi.ContainerConnector(game);
    ChineseTenGraphics chineseTenGraphics = new ChineseTenGraphics();
    chineseTenPresenter =
        new ChineseTenPresenter(chineseTenGraphics, container);
    
    RootPanel.get("mainDiv").add(chineseTenGraphics);
    container.sendGameReady();
    // For emulator version
    
  }
}