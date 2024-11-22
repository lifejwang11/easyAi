package org.dromara.easyai.voice;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public class VoiceTest {
    public static void main(String[] args) throws UnsupportedAudioFileException, IOException {
        File file = new File("D:\\sondTest\\wo.wav");
        WaveFile wav = new WaveFile(file);
        int amplitudeExample = wav.getSampleInt(140); // 140th amplitude value.
        System.out.println("帧数:"+wav.getFramesCount());
        System.out.println("140帧的幅度："+amplitudeExample);
    }
}
