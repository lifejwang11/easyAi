package org.dromara.voice;

import javax.sound.sampled.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class WaveFile {
    public final int NOT_SPECIFIED = AudioSystem.NOT_SPECIFIED; // -1
    public final int INT_SIZE = 4;

    private int sampleSize = NOT_SPECIFIED;
    private long framesCount = NOT_SPECIFIED;
    private int sampleRate = NOT_SPECIFIED;
    private int channelsNum;
    private byte[] data;      // wav bytes
    private AudioInputStream ais;
    private AudioFormat af;

    private Clip clip;
    private boolean canPlay;

    public WaveFile(File file) throws UnsupportedAudioFileException, IOException {
        if (!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }

        ais = AudioSystem.getAudioInputStream(file);

        af = ais.getFormat();

        framesCount = ais.getFrameLength();

        sampleRate = (int) af.getSampleRate();

        sampleSize = af.getSampleSizeInBits() / 8;

        channelsNum = af.getChannels();

        long dataLength = framesCount * af.getSampleSizeInBits() * af.getChannels() / 8;

        data = new byte[(int) dataLength];
        ais.read(data);

        AudioInputStream aisForPlay = AudioSystem.getAudioInputStream(file);
        try {
            clip = AudioSystem.getClip();
            clip.open(aisForPlay);
            clip.setFramePosition(0);
            canPlay = true;
        } catch (LineUnavailableException e) {
            canPlay = false;
            System.out.println("I can play only 8bit and 16bit music.");
        }
    }

    public boolean isCanPlay() {
        return canPlay;
    }

    public void play() {
        clip.start();
    }

    public void stop() {
        clip.stop();
    }

    public AudioFormat getAudioFormat() {
        return af;
    }

    public int getSampleSize() {
        return sampleSize;
    }

    public double getDurationTime() {
        return getFramesCount() / getAudioFormat().getFrameRate();
    }

    public long getFramesCount() {
        return framesCount;
    }


    /**
     * Returns sample (amplitude value). Note that in case of stereo samples
     * go one after another. I.e. 0 - first sample of left channel, 1 - first
     * sample of the right channel, 2 - second sample of the left channel, 3 -
     * second sample of the rigth channel, etc.
     */
    public int getSampleInt(int sampleNumber) {

        if (sampleNumber < 0 || sampleNumber >= data.length / sampleSize) {
            throw new IllegalArgumentException(
                    "sample number can't be < 0 or >= data.length/"
                            + sampleSize);
        }

        byte[] sampleBytes = new byte[4]; //4byte = int

        for (int i = 0; i < sampleSize; i++) {
            sampleBytes[i] = data[sampleNumber * sampleSize * channelsNum + i];
        }

        int sample = ByteBuffer.wrap(sampleBytes)
                .order(ByteOrder.LITTLE_ENDIAN).getInt();
        return sample;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public Clip getClip() {
        return clip;
    }
}
