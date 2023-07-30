package de.androidcrypto.nfcstoragemanagement;

import android.nfc.tech.NfcA;
import android.text.TextUtils;
import android.widget.TextView;

import java.io.IOException;

/**
 * The class takes all interactions with a NTAG21x tag
 */

public class Ntag21xMethods {

    private TextView textView; // used for displaying informations from the functions

    public Ntag21xMethods(TextView textView) {
        this.textView = textView;
    }

    /**
     * read the content of the user memory upto 'numberOfBytes' - this is because the maximum NDEF length
     * got defined in NdefSettingsFragment
     * The content is used to find matching strings with UID and/or MAC
     * Note: if any mirror is enabled on the tag the returned content is the VIRTUAL content including
     * the mirror content, not the REAL content written in pages !
     *
     * @param nfcA
     * @param numberOfBytesToRead
     * @return
     */
    public byte[] readNdefContent(NfcA nfcA, int numberOfBytesToRead, int nfcaMaxTranceiveLength ) {
        int nfcaMaxTranceive4ByteTrunc = nfcaMaxTranceiveLength / 4; // 63
        int nfcaMaxTranceive4ByteLength = nfcaMaxTranceive4ByteTrunc * 4; // 252 bytes
        int nfcaNrOfFullReadings = numberOfBytesToRead / nfcaMaxTranceive4ByteLength;
        int nfcaTotalFullReadingBytes = nfcaNrOfFullReadings * nfcaMaxTranceive4ByteLength; // 3 * 252 = 756
        int nfcaMaxTranceiveModuloLength = numberOfBytesToRead - nfcaTotalFullReadingBytes; // 888 bytes - 756 bytes = 132 bytes
        String nfcaContent = "";
        nfcaContent = nfcaContent + "nfcaMaxTranceive4ByteTrunc: " + nfcaMaxTranceive4ByteTrunc + "\n";
        nfcaContent = nfcaContent + "nfcaMaxTranceive4ByteLength: " + nfcaMaxTranceive4ByteLength + "\n";
        nfcaContent = nfcaContent + "nfcaNrOfFullReadings: " + nfcaNrOfFullReadings + "\n";
        nfcaContent = nfcaContent + "nfcaTotalFullReadingBytes: " + nfcaTotalFullReadingBytes + "\n";
        nfcaContent = nfcaContent + "nfcaMaxTranceiveModuloLength: " + nfcaMaxTranceiveModuloLength + "\n";

        byte[] response;
        byte[] ntagMemory = new byte[numberOfBytesToRead];
        try {
            // first round
            for (int i = 0; i < nfcaNrOfFullReadings; i++) {
                //nfcaContent = nfcaContent + "starting round: " + i + "\n";
                System.out.println("starting round: " + i);
                byte[] commandF = new byte[]{
                        (byte) 0x3A,  // FAST_READ
                        (byte) ((4 + (nfcaMaxTranceive4ByteTrunc * i)) & 0x0ff), // page 4 is the first user memory page
                        (byte) ((4 + (nfcaMaxTranceive4ByteTrunc * (i + 1)) - 1) & 0x0ff)
                };
                //nfcaContent = nfcaContent + "i: " + i + " commandF: " + bytesToHex(commandF) + "\n";
                response = nfcA.transceive(commandF);
                if (response == null) {
                    // either communication to the tag was lost or a NACK was received
                    // Log and return
                    nfcaContent = nfcaContent + "ERROR: null response";
                    String finalNfcaText = nfcaContent;
                    writeToUiAppend(textView, finalNfcaText);
                    System.out.println(finalNfcaText);
                    return null;
                } else if ((response.length == 1) && ((response[0] & 0x00A) != 0x00A)) {
                    // NACK response according to Digital Protocol/T2TOP
                    // Log and return
                    nfcaContent = nfcaContent + "ERROR: NACK response: " + Utils.bytesToHexNpe(response);
                    String finalNfcaText = nfcaContent;
                    writeToUiAppend(textView, finalNfcaText);
                    System.out.println(finalNfcaText);
                    return null;
                } else {
                    // success: response contains ACK or actual data
                    System.arraycopy(response, 0, ntagMemory, (nfcaMaxTranceive4ByteLength * i), nfcaMaxTranceive4ByteLength);
                }
            } // for
            // now we read the nfcaMaxTranceiveModuloLength bytes, for a NTAG216 = 132 bytes
            //nfcaContent = nfcaContent + "starting last round: " + "\n";
            //System.out.println("starting last round: ");
            byte[] commandF = new byte[]{
                    (byte) 0x3A,  // FAST_READ
                    (byte) ((4 + (nfcaMaxTranceive4ByteTrunc * nfcaNrOfFullReadings)) & 0x0ff), // page 4 is the first user memory page
                    (byte) ((4 + (nfcaMaxTranceive4ByteTrunc * nfcaNrOfFullReadings) + (nfcaMaxTranceiveModuloLength / 4) & 0x0ff))
            };
            //nfcaContent = nfcaContent + "last: " + " commandF: " + bytesToHex(commandF) + "\n";
            response = nfcA.transceive(commandF);
            if (response == null) {
                // either communication to the tag was lost or a NACK was received
                // Log and return
                nfcaContent = nfcaContent + "ERROR: null response";
                String finalNfcaText = nfcaContent;
                writeToUiAppend(textView, finalNfcaText);
                System.out.println(finalNfcaText);
                return null;
            } else if ((response.length == 1) && ((response[0] & 0x00A) != 0x00A)) {
                // NACK response according to Digital Protocol/T2TOP
                // Log and return
                nfcaContent = nfcaContent + "ERROR: NACK response: " + Utils.bytesToHexNpe(response);
                String finalNfcaText = nfcaContent;
                //writeToUiAppend(resultNfcWriting, finalNfcaText);
                System.out.println(finalNfcaText);
                return null;
            } else {
                // success: response contains ACK or actual data
                System.arraycopy(response, 0, ntagMemory, (nfcaMaxTranceive4ByteLength * nfcaNrOfFullReadings), nfcaMaxTranceiveModuloLength);
            }
        } catch (IOException e) {
            writeToUiAppend(textView, "ERROR: IOException " + e.toString());
            e.printStackTrace();
            return null;
        }
        return ntagMemory;
    }


    /**
     * service methods
     */

    /*
    private void writeToUiAppendOld(TextView textView, String message) {
        getActivity().runOnUiThread(() -> {
            String oldString = textView.getText().toString();
            if (TextUtils.isEmpty(oldString)) {
                textView.setText(message);
            } else {
                String newString = message + "\n" + oldString;
                textView.setText(newString);
                System.out.println(message);
            }
        });
    }
     */

    private void writeToUiAppend(TextView textView, String message) {
        String oldString = textView.getText().toString();
        if (TextUtils.isEmpty(oldString)) {
            textView.setText(message);
        } else {
            String newString = message + "\n" + oldString;
            textView.setText(newString);
            System.out.println(message);
        }
    }
}
