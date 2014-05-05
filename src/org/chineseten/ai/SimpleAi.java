package org.chineseten.ai;

import java.util.List;

//import java_cup.internal_error;


import org.chineseten.client.ChineseTenState;
import org.chineseten.client.Color;
import org.game_api.GameApi.Operation;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

public class SimpleAi {
    
    private ChineseTenState lasState;
    private Optional<Color> myColor;
    
    public SimpleAi(ChineseTenState lastState, Optional<Color> myColor) {
        this.lasState=lastState;
        this.myColor=myColor;
        // TODO Auto-generated constructor stub
    }
    
//    public List<Operation> getAiOperations() {
//        
//        List<Operation> aiOperations = Lists.newArrayList();
//        return aiOperations;
//        
//    }
    
    public List<Integer> getAiOperationsForStage1() {
        List<Integer> res = Lists.newArrayList();
        List<Integer> myCardIndices = lasState.getWhiteOrBlack(myColor.get());
        List<Integer> cardsInDeckIndices = lasState.getDeck();
        //int i = myCardIndices.size();
        //int j = cardsInDeckIndices.size();
        
        for (int i = 0; i < myCardIndices.size(); i++) {
            Integer w = myCardIndices.get(i);
            for (int j = 0; j < cardsInDeckIndices.size(); j++) {
               Integer d = cardsInDeckIndices.get(j);
               if(checkWhetherSumIsTen(lasState, (int)d, (int)w)){
                   res.add(w);
                   res.add(d);
                   return res;
               };
               
            }
        }
        return res;  
    }
    
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

}
