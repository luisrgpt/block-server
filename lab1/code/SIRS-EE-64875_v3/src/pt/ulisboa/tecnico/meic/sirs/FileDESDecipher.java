package pt.ulisboa.tecnico.meic.sirs;

import javax.crypto.Cipher;
import java.io.IOException;

/**
 * Decrypts a file with the DES algorithm in multiple modes, with a given, appropriate DES key
 */
public class FileDESDecipher {

    public static void main(String[] args) throws IOException {

        if(args.length != 4) {
            System.err.println("This program decrypts a file with 56-bit DES.");
            System.err.println("Usage: FileDESDecipher [inputFile] [DESKeyFile] [ECB|CBC|OFB] [outputFile]");
            return;
        }

        final String inputFile = args[0];
        final String keyFile = args[1];
        final String mode = args[2].toUpperCase();
        final String outputFile = args[3];

        if( !(mode.equals("ECB") || mode.equals("CBC") || mode.equals("OFB")) ) {
            System.err.println("The modes of operation must be ECB, CBC or OFB.");
            return;
        }

        DESCipherByteArrayMixer cipher = new DESCipherByteArrayMixer(Cipher.DECRYPT_MODE);
        cipher.setParameters(keyFile, mode);
        FileMixer.mix(inputFile, outputFile, cipher);

    }
}
