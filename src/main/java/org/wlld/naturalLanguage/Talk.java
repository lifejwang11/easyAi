package org.wlld.naturalLanguage;


import java.util.ArrayList;
import java.util.List;

/**
 * @author lidapeng
 * @description 语句分类
 * @date 4:14 下午 2020/2/23
 */
public class Talk {
    private List<WorldBody> allWorld = WordTemple.get().getAllWorld();//所有词集合

    public void talk(String sentence) {
        String rgm = null;
        if (sentence.indexOf(",") > -1) {
            rgm = ",";
        } else if (sentence.indexOf("，") > -1) {
            rgm = "，";
        }
        String[] sens;
        if (rgm != null) {
            sens = sentence.split(rgm);
        } else {
            sens = new String[]{sentence};
        }
        //拆词
        List<Sentence> sentences = new ArrayList<>();
        for (int i = 0; i < sens.length; i++) {
            Sentence sentenceWords = new Sentence();
            catchSentence(sentence, sentenceWords);
            sentences.add(sentenceWords);
        }
        restructure(sentences);
        for (Sentence sentence1 : sentences) {
            System.out.println(sentence1.getKeyWords());
        }
    }

    private void catchSentence(String sentence, Sentence sentenceWords) {//把句子拆开
        int len = sentence.length();
        for (int i = 0; i < len; i++) {
            String word = sentence.substring(0, i + 1);
            sentenceWords.setWord(word);
        }

    }

    private void restructure(List<Sentence> sentences) {//对句子里面的Word进行词频统计
        for (Sentence words : sentences) {
            List<WorldBody> listWord = allWorld;
            List<Word> waitWorld = words.getWaitWords();
            for (Word word : waitWorld) {
                String myWord = word.getWord();
                WorldBody body = getBody(myWord, listWord);
                listWord = body.getWorldBodies();
                word.setWordFrequency(body.getWordFrequency());
            }
        }
        Tokenizer tokenizer = new Tokenizer();
        for (Sentence words : sentences) {
            tokenizer.radiation(words);
        }
    }

    private WorldBody getBody(String word, List<WorldBody> worlds) {
        WorldBody myBody = null;
        for (WorldBody body : worlds) {
            if (body.getWordName().hashCode() == word.hashCode() && body.getWordName().equals(word)) {
                myBody = body;
                break;
            }
        }
        return myBody;
    }
}
