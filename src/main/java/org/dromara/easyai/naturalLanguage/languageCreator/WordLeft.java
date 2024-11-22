package org.dromara.easyai.naturalLanguage.languageCreator;

import org.dromara.easyai.gameRobot.Action;

import java.util.ArrayList;
import java.util.List;

public class WordLeft extends Action {
    private final List<String> keyWords;
    private final List<String> finishWords;//终结态词集合

    public WordLeft(List<String> keyWords, List<String> finishWords) {
        this.keyWords = keyWords;
        this.finishWords = finishWords;
    }

    private boolean isFinish(String myWord) {
        boolean isHere = false;
        for (String finishWord : finishWords) {
            if (finishWord.hashCode() == myWord.hashCode() && finishWord.equals(myWord)) {
                isHere = true;
                break;
            }
        }
        return isHere;
    }

    @Override
    public int getActionId() {
        return super.getActionId();
    }

    @Override
    public void setActionId(int actionId) {
        super.setActionId(actionId);
    }

    @Override
    public List<int[]> action(int[] stateId) {
        int id = stateId[0];
        String myWord = keyWords.get(id - 1);
        int len = myWord.length() + 1;
        int size = keyWords.size();
        List<int[]> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            String word = keyWords.get(i);
            if (word.length() == len && word.substring(1).equals(myWord)) {
                list.add(new int[]{i + 1});
            }
        }
        return list;
    }

    @Override
    protected int getProfit(int[] stateId) {
        int allProfit = 0;
        List<int[]> states = action(stateId);
        if (states.isEmpty()) {
            allProfit = -10;
        } else {
            for (int i = 0; i < states.size(); i++) {
                int nextID = states.get(i)[0];
                if (nextID > 0) {
                    String myWord = keyWords.get(nextID - 1);//查看是否为终结态
                    if (isFinish(myWord)) {//是终结态
                        allProfit = allProfit + 10;
                    }
                } else {
                    allProfit = allProfit - 10;
                }
            }
            allProfit = allProfit / states.size();
        }
        return allProfit;
    }
}
