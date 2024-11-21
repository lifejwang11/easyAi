package org.dromara.naturalLanguage;


import java.util.ArrayList;
import java.util.List;

/**
 * @author lidapeng
 * @description 语句分类
 * @date 4:14 下午 2020/2/23
 */
public class Talk {
    private final List<WorldBody> allWorld;//所有词集合
    private final WordTemple wordTemple;

    public Talk(WordTemple wordTemple) {
        this.wordTemple = wordTemple;
        allWorld = wordTemple.getAllWorld();//所有词集合
    }

    public List<List<String>> getSplitWord(String sentence) {//单纯进行拆词
        List<Sentence> sentences = splitSentence(sentence);
        List<List<String>> words = new ArrayList<>();
        for (Sentence sentence1 : sentences) {
            words.add(sentence1.getKeyWords());
        }
        return words;
    }

    private List<Sentence> splitSentence(String sentence) {
        String[] sens = sentence.replace(" ", "").split("，|。|？|！|；|、|：");
        //拆词
        List<Sentence> sentences = new ArrayList<>();
        for (String mySentence : sens) {
            List<Sentence> sentenceList = catchSentence(mySentence);
            int key = 0;
            int nub = 0;
            for (int j = 0; j < sentenceList.size(); j++) {
                Sentence sentence1 = sentenceList.get(j);
                restructure(sentence1);
                int size = sentence1.getKeyWords().size();
                if (size > nub) {
                    key = j;
                    nub = size;
                }
            }
            //System.out.println(sentenceList.get(key).getKeyWords());
            sentences.add(sentenceList.get(key));
        }
        return sentences;
    }

    private List<Sentence> catchSentence(String sentence) {//把句子拆开
        int len = sentence.length();
        List<Sentence> sentences = new ArrayList<>();
        if (len > 1) {
            for (int j = 0; j < len - 1; j++) {
                Sentence sentenceWords = new Sentence();
                for (int i = j; i < len; i++) {
                    String word = sentence.substring(j, i + 1);
                    sentenceWords.setWord(word);
                }
                sentences.add(sentenceWords);
            }
        } else {
            Sentence sentenceWords = new Sentence();
            sentenceWords.setWord(sentence);
            sentences.add(sentenceWords);
        }
        return sentences;
    }

    private void restructure(Sentence words) {//对句子里面的Word进行词频统计
        List<WorldBody> listWord = allWorld;
        List<Word> waitWorld = words.getWaitWords();
        for (Word word : waitWorld) {
            String myWord = word.getWord();
            WorldBody body = getBody(myWord, listWord);
            if (body == null) {//已经无法查找到对应的词汇了
                word.setWordFrequency(1);
                break;
            }
            listWord = body.getWorldBodies();//这个body报了一次空指针
            word.setWordFrequency(body.getWordFrequency());
        }
        Tokenizer tokenizer = new Tokenizer(wordTemple);
        tokenizer.radiation(words);

    }

    private WorldBody getBody(String word, List<WorldBody> worlds) {
        //TODO 这里有个BUG 当myBody出现空的时候断词已经找不到了
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
