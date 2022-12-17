package org.wlld.naturalLanguage.languageCreator;

import org.wlld.gameRobot.Action;

import java.util.List;

public class WordLeft extends Action {
    private List<String> keyWords;
    private List<String> finishWords;//终结态词集合

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
    public int[] action(int[] stateId) {
        int id = stateId[0];
        String myWord = keyWords.get(id - 1);
        int len = myWord.length() + 1;
        int size = keyWords.size();
        int[] nextId = new int[]{0};
        for (int i = 0; i < size; i++) {
            String word = keyWords.get(i);
            if (word.length() == len && word.substring(1).equals(myWord)) {
                nextId[0] = i + 1;
                break;
            }
        }
        return nextId;
    }

    @Override
    protected int getProfit(int[] stateId) {
        int profit = 0;
        int nextID = action(stateId)[0];
        if (nextID > 0) {
            String myWord = keyWords.get(nextID - 1);//查看是否为终结态
            if (isFinish(myWord)) {//是终结态
                profit = 10;
            }
        } else {
            profit = -10;
        }
        return profit;
    }
}
