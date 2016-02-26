package pt.ulisboa.tecnico.meic.sirs;

import javax.crypto.Cipher;
import java.io.IOException;

/**
 * Xor a character of a file with a value
 */
public class FileXorByte {
    public static void main(String[] args) throws IOException {

        if(args.length != 3) {
            System.err.println("This program will XOR a character of a file with a value.");
            System.err.println("Usage: FileXorByte [inputFile] [offset] [value]");
            return;
        }

        final String inputFile = args[0];
        final int offset = new Integer(args[1]);
        final int value = new Integer(args[2]);

        FileMixer.mix(inputFile, inputFile, new ByteArrayMixer() {
            @Override
            public byte[] mix(byte[] byteArray1, byte[] byteArray2) {
                byteArray1[offset] ^= value;
                return byteArray1;
            }
        });

    }
}
