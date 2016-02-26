package pt.ulisboa.tecnico.meic.sirs;

import javax.crypto.Cipher;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static java.nio.file.Files.readAllBytes;

/**
 * This is the solution to the problem of changing the grades
 */
public class SolutionToTheGradesProblem {

    /*
     * First, we will begin by changing Thomas S. Cook's grade
     * - It is located in row 4, column 97.
     * - We want to change the byte in row 4, column 98 (offset 3*112+98-1 = 433 = 0x1b1), from 1 to 6
     * - '1' ^ '6' = 49 ^ 54 = 7
     * Similarly, Josh's offset is at (11,97), 10*112+97-1 = 1216 = 0x4c0
     * - '9' ^ '1' = 57 ^ 49 = 8
     * - ' ' ^ '6' = 32 ^ 54 = 22
     * In ECB mode, I can't do it: any change in the block will randomize it on decryption
     * In CBC mode, we have to make the same change, but in the previous block. As a side-effect, that block will be garbled.
     * In OFB mode, it is trivial: we only need to XOR as if we were changing the original plaintext. No side-effects.
     */

    public static void main(String args[]) {
        final String originalCipher1 = "/tmp/sirs/grades/inputs/grades.cbc.des";
        final String originalCipher2 = "/tmp/sirs/grades/inputs/grades.ofb.des";

        final String tamperedCipher1 = "/tmp/sirs/grades/outputs/grades-new.cbc.des";
        final String tamperedCipher2 = "/tmp/sirs/grades/outputs/grades-new.ofb.des";

        try {
            // let's begin with grades.ofb.des:
            byte[] file2Bytes = Files.readAllBytes(new File(originalCipher2).toPath());
            // 11 -> 16, Thomas
            file2Bytes[433] = (byte) (file2Bytes[433] ^ 7);
            // 9 -> 16, Josh
            file2Bytes[1216] = (byte) (file2Bytes[1216] ^ 8);
            file2Bytes[1217] = (byte) (file2Bytes[1217] ^ 22);

            Files.write(new File(tamperedCipher2).toPath(), file2Bytes);

            // now, let's work on grades.cbc.des:
            byte[] file1Bytes = Files.readAllBytes(new File(originalCipher1).toPath());
            // 11 -> 16, Thomas, but on the previous block
            file1Bytes[433-8] = (byte) (file1Bytes[433-8] ^ 7);
            // 9 -> 16, Josh
            file1Bytes[1216-8] = (byte) (file1Bytes[1216-8] ^ 8);
            file1Bytes[1217-8] = (byte) (file1Bytes[1217-8] ^ 22);

            Files.write(new File(tamperedCipher1).toPath(), file1Bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
