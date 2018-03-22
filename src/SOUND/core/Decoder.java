package SOUND.core;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.LinkedList;

import static java.lang.System.exit;


public class Decoder {

    private static float SAMPLE_RATE = 44100;
    private static int FRAME_SIZE = 92;
    private static float CARRIER1_FREQ = 10000;
    private static float CARRIER0_FREQ = 5000;
    private static int PREAMBLE_SIZE = 440;
    private static double CUTOFF_1 = 2e3;
    private static double CUTOFF_2 = 10e3;
    private static double AMP_PREAMPLE = 127;
    private static int BIT_SAMPLE = 44;
    private static byte[] FRAME0 = new byte[BIT_SAMPLE];
    private static byte[] FRAME1 = new byte[BIT_SAMPLE];

    public Decoder() {
    }

    private static byte[] generateWave(int sample, float carrier) {
        byte[] wave = new byte[sample];
        for (int i = 0; i < sample; i++) {
            wave[i] = (byte) (127 * Math.sin(2 * Math.PI * carrier * i / SAMPLE_RATE));
        }
        return wave;
    }

    private static LinkedList getFile(String filePath) {
        LinkedList linkedList = new LinkedList();
        try {
            Reader r = new FileReader(filePath);
            char c = 0;
            while ((c = (char) (r.read())) != 65535) {
                if (c == (char) 10 || c == (char) 32 || c == (char) 9) {
                    continue;
                }
                assert ((c == '1') || (c == '0')) : "=> Cannot put 2 or more base";
                linkedList.add((int) c - 48);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return linkedList;
    }

    private static byte[] getPreamble() {
        byte[] preamble = new byte[PREAMBLE_SIZE];
        double phaseIncre = (CUTOFF_2 - CUTOFF_1 )/ PREAMBLE_SIZE / 2;

        for (int i = 0; i < PREAMBLE_SIZE >> 1 ; i++){
            double phase = ((double) i / SAMPLE_RATE ) * (i * phaseIncre + CUTOFF_1);
            double signal = AMP_PREAMPLE * (Math.sin(2 * Math.PI * phase ));
            preamble[i] = (byte) signal;
            preamble[PREAMBLE_SIZE - i - 1] = preamble[i];
        }
        return preamble;
    }


    private static LinkedList packUp(LinkedList msg) {
        int totalSize = msg.size();
        int numPkg = (int) Math.ceil( totalSize / FRAME_SIZE);
        if (numPkg > 255) {
            exit(-4);
        }
        int skip = numPkg * FRAME_SIZE - msg.size();
        for (int i = 0; i < numPkg ; i ++){
            byte[] pkg = new byte[FRAME_SIZE + 8];
            for (int j = 0; j < FRAME_SIZE; j++){
                if ((i*FRAME_SIZE + j) < totalSize){
                    pkg[j] = (byte) msg.pop();
                    // TODO: decode as wav
                }
            }
        }
        return msg;
    }

    public static void main(String args[]) {
        System.out.println("=> Setup carriers!!!!.....");
        FRAME0 = generateWave(BIT_SAMPLE, CARRIER0_FREQ);
        FRAME1 = generateWave(BIT_SAMPLE, CARRIER1_FREQ);
        LinkedList dataList = getFile("/Users/tony/IdeaProjects/CS120-Toy/text/input.txt");
        int len = dataList.size();
        // TODO: Handle 10Mbit file
        System.out.println(len);
        utils UTIL = new utils();
        byte soundTrack[] = getPreamble();
        while (dataList.size() != 0) {
            int sig = (int) dataList.pop();
            if (sig == 0) {
                soundTrack = UTIL.addArray(soundTrack, FRAME0);
            } else {
                soundTrack = UTIL.addArray(soundTrack, FRAME1);
            }
        }
        System.out.println("=> end debug");
//        System.out.println((byte) 213.111);
//        System.out.println(Arrays.toString(getPreamble()));

//        System.out.println(Arrays.toString(FRAME0));
//        System.out.println(Arrays.toString(FRAME1));
        System.out.println(Arrays.toString(soundTrack));
    }


}
