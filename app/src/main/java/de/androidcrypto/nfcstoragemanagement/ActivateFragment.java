package de.androidcrypto.nfcstoragemanagement;

import static android.content.Context.MODE_PRIVATE;
import static de.androidcrypto.nfcstoragemanagement.Utils.doVibrate;
import static de.androidcrypto.nfcstoragemanagement.Utils.playSinglePing;
import static de.androidcrypto.nfcstoragemanagement.Utils.testBit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.TagLostException;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.IOException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ActivateFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ActivateFragment extends Fragment implements NfcAdapter.ReaderCallback {

    private static final String TAG = ActivateFragment.class.getName();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    com.google.android.material.textfield.TextInputEditText resultNfcWriting;
    RadioButton rbActivateGetStatus, rbActivateOn, rbActivateOff;

    private NfcAdapter mNfcAdapter;
    private NfcA nfcA;

    private int identifiedNtagConfigurationPage; // this  is the stating point for any work on configuration

    public ActivateFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SendFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ActivateFragment newInstance(String param1, String param2) {
        ActivateFragment fragment = new ActivateFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    // AID is setup in apduservice.xml
    // original AID: F0394148148100

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_activate, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        resultNfcWriting = getView().findViewById(R.id.etActivateResult);
        rbActivateGetStatus = getView().findViewById(R.id.rbActivateShowStatus);
        rbActivateOn = getView().findViewById(R.id.rbActivateOn);
        rbActivateOff = getView().findViewById(R.id.rbActivateOff);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(getView().getContext());
    }

    /**
     * section for NFC
     */

    @Override
    public void onTagDiscovered(Tag tag) {
// Read and or write to Tag here to the appropriate Tag Technology type class
        // in this example the card should be an Ndef Technology Type

        nfcA = NfcA.get(tag);
        try {
            nfcA = NfcA.get(tag);

            if (nfcA != null) {
                writeToUiAppend(resultNfcWriting,"NFC tag is Nfca compatible");
                nfcA.connect();
                // check that the tag is a NTAG213/215/216 manufactured by NXP - stop if not
                String ntagVersion = NfcIdentifyNtag.checkNtagType(nfcA, tag.getId());
                if ((!ntagVersion.equals("213")) && (!ntagVersion.equals("215")) && (!ntagVersion.equals("216"))) {
                    writeToUiAppend(resultNfcWriting,"NFC tag is NOT of type NXP NTAG213/215/216, aborted");
                    return;
                }

                int nfcaMaxTranceiveLength = nfcA.getMaxTransceiveLength(); // important for the readFast command
                Log.d(TAG, "nfcaMaxTranceiveLength: " + nfcaMaxTranceiveLength);
                int ntagPages = NfcIdentifyNtag.getIdentifiedNtagPages();
                identifiedNtagConfigurationPage = NfcIdentifyNtag.getIdentifiedNtagConfigurationPage();
                writeToUiAppend(resultNfcWriting, "The configuration is starting in page " + identifiedNtagConfigurationPage);
                int ntagMemoryBytes = NfcIdentifyNtag.getIdentifiedNtagMemoryBytes();
                String tagIdString = Utils.getDec(tag.getId());
                String nfcaContent = "raw data of " + NfcIdentifyNtag.getIdentifiedNtagType() + "\n" +
                        "number of pages: " + ntagPages +
                        " total memory: " + ntagMemoryBytes +
                        " bytes\n" +
                        "tag ID: " + Utils.bytesToHexNpe(NfcIdentifyNtag.getIdentifiedNtagId()) + "\n" +
                        "tag ID: " + tagIdString + "\n";
                nfcaContent = nfcaContent + "maxTranceiveLength: " + nfcaMaxTranceiveLength + " bytes\n";
                writeToUiAppend(resultNfcWriting, nfcaContent);
                // read the complete memory depending on ntag type
                byte[] ntagMemory = new byte[ntagMemoryBytes];
                // read the content of the tag in several runs
                byte[] response = new byte[0];

                boolean isGetActivateStatus = rbActivateGetStatus.isChecked();
                boolean isActivateOn = rbActivateOn.isChecked();
                boolean isActivateOff = rbActivateOff.isChecked();

                try {
                    if (isGetActivateStatus) {
                        response = getStatusUidMirrorNdef(nfcA);

                        // read the complete ndef message and find the placeholder
                        // as I'm limiting the maximum ndef message we can read all data in one run
                        // build the matching strings for ud and mac

                        // read the placeholder names from the shared preferences
                        SharedPreferences prefs = getContext().getSharedPreferences(NdefSettingsFragment.PREFS_NAME, MODE_PRIVATE);
                        String UID_NAME = prefs.getString(NdefSettingsFragment.PREFS_UID_NAME, null);
                        String MAC_NAME = prefs.getString(NdefSettingsFragment.PREFS_MAC_NAME, null);
                        if ((UID_NAME == null) || (UID_NAME.length() < 1) || (MAC_NAME == null) || (MAC_NAME.length() < 1)) {
                            writeToUiAppend(resultNfcWriting, "Please setup the NDEF settings, aborted");
                            return;
                        }
                        StringBuilder sb = new StringBuilder();
                        // uid
                        sb.append(NdefSettingsFragment.UID_HEADER);
                        sb.append(NdefSettingsFragment.UID_NAME);
                        sb.append(NdefSettingsFragment.UID_FOOTER);
                        String uidMatchString = sb.toString();
                        // mac
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append(NdefSettingsFragment.MAC_HEADER);
                        sb2.append(NdefSettingsFragment.MAC_NAME);
                        sb2.append(NdefSettingsFragment.MAC_FOOTER);
                        String macMatchString = sb.toString();

                        // read the content of the the tag to find the match strings




                        // todo read all user memory pages and search for math strings

                        if (response == null) {
                            writeToUiAppend(resultNfcWriting, "status of the UID mirror: FAILURE");
                            return;
                        } else {
                            writeToUiAppend(resultNfcWriting, "status of the UID mirror: SUCCESS - code: " + Utils.bytesToHexNpe(response));
                        }

                    }
                    if (isActivateOn) {
                        response = writeEnableUidMirrorNdef(nfcA);
                        if (response == null) {
                            writeToUiAppend(resultNfcWriting, "Enabling the UID mirror: FAILURE");
                            return;
                        } else {
                            writeToUiAppend(resultNfcWriting, "Enabling the UID mirror: SUCCESS - code: " + Utils.bytesToHexNpe(response));
                        }
                    }
                    if (isActivateOn) {
                        //response = writeDisableUidMirrorNdef(nfcA);
                        if (response == null) {
                            writeToUiAppend(resultNfcWriting, "Disabling the UID mirror: FAILURE");
                            return;
                        } else {
                            writeToUiAppend(resultNfcWriting, "Disabling the UID mirror: SUCCESS - code: " + Utils.bytesToHexNpe(response));
                        }
                    }


                } finally {
                    try {
                        nfcA.close();
                    } catch (IOException e) {
                        writeToUiAppend(resultNfcWriting, "ERROR: IOException " + e.toString());
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            writeToUiAppend(resultNfcWriting, "ERROR: IOException " + e.toString());
            e.printStackTrace();
        }

            doVibrate(getActivity());

    }

    private byte[] getStatusUidMirrorNdef(NfcA nfcA) {
        /**
         * WARNING: this command is hardcoded to work with a NTAG216
         * the bit for enabling or disabling the uid mirror is in pages 41/131/227 (0x29 / 0x83 / 0xE3)
         * depending on the tag type
         *
         * byte 0 of this pages holds the MIRROR byte
         * byte 2 of this pages holds the MIRROR_PAGE byte
         *
         * Mirror byte has these flags
         * bits 6+7 define which mirror shall be used:
         *   00b = no ASCII mirror
         *   01b = Uid ASCII mirror
         *   10b = NFC counter ASCII mirror
         *   11b = Uid and NFC counter ASCII mirror
         * bits 4+5 define the byte position within the page defined in MIRROR_PAGE byte
         *
         * MIRROR_PAGE byte defines the start of mirror.
         *
         * It is import that the end of mirror is within the user memory. These lengths apply:
         * Uid mirror: 14 bytes
         * NFC counter mirror: 6 bytes
         * Uid + NFC counter mirror: 21 bytes (14 bytes for Uid and 1 byte separation + 6 bytes counter value
         * Separator is x (0x78)
         *
         * This function writes the MIRROR_PAGE and MIRROR_BYTE to the place where the WRITE NDEF MESSAGE needs it
         *
         */

        boolean isUidMirror, isCounterMirror, isMirrorBit4, isMirrorBit5;

        writeToUiAppend(resultNfcWriting, "* Start enabling the Counter mirror *");
        // read page 227 = Configuration page 0
        byte[] readPageResponse = getTagDataResponse(nfcA, identifiedNtagConfigurationPage);
        if (readPageResponse != null) {
            // get byte 0 = MIRROR
            byte mirrorByte = readPageResponse[0];
            // get byte 2 = MIRROR_PAGE
            byte mirrorPageByte = readPageResponse[2];
            writeToUiAppend(resultNfcWriting, "mirrorPageByte: " + Utils.byteToHex(mirrorPageByte) + " (= " + (int) mirrorPageByte + " dec)");
            writeToUiAppend(resultNfcWriting, "MIRROR content old: " + Utils.printByteBinary(mirrorByte));

            isUidMirror = testBit(mirrorByte, 6);
            isCounterMirror = testBit(mirrorByte, 7);
            isMirrorBit4 = testBit(mirrorByte, 4);
            isMirrorBit5 = testBit(mirrorByte, 5);
            writeToUiAppend(resultNfcWriting, "isUidMirror: " + isUidMirror + " || isCounterMirror: " + isCounterMirror);
            writeToUiAppend(resultNfcWriting, "isMirrorBit4: " + isMirrorBit4 + " || isMirrorBit5: " + isMirrorBit5);

            byte mirrorByteNew;
            // unsetting bit 7 = counter, we are doing UID mirroring only
            mirrorByteNew = Utils.unsetBitInByte(mirrorByte, 7);
            // setting bit 6
            mirrorByteNew = Utils.setBitInByte(mirrorByteNew, 6);
            // fix: start the mirror from byte 1 of the designated page, so bits are set as follows
            mirrorByteNew = Utils.unsetBitInByte(mirrorByteNew, 5);
            mirrorByteNew = Utils.setBitInByte(mirrorByteNew, 4);
            writeToUiAppend(resultNfcWriting, "MIRROR content new: " + Utils.printByteBinary(mirrorByteNew));
            // set the page where the mirror is starting, we use a fixed page here:
            int setMirrorPage = 15; // 0x0F
            byte mirrorPageNew = (byte) (setMirrorPage & 0x0ff);
            // rebuild the page data
            readPageResponse[0] = mirrorByteNew;
            readPageResponse[2] = mirrorPageNew;
            return readPageResponse;
/*
            // write the page back to the tag
            byte[] writePageResponse = writeTagDataResponse(nfcA, 227, readPageResponse); // this is for NTAG216 only
            writeToUiAppend(resultNfcWriting, "write page to tag: " + Utils.bytesToHexNpe(readPageResponse));
            //byte[] writePageResponse = writeTagDataResponse(nfcA, 5, readPageResponse); // this is for NTAG216 only

            if (writePageResponse != null) {
                writeToUiAppend(resultNfcWriting, "SUCCESS: writing with response: " + Utils.bytesToHexNpe(writePageResponse));
                return readPageResponse;
            } else {
                writeToUiAppend(resultNfcWriting, "FAILURE: no writing on the tag");
            }

             */
        }
        return null;
    }

    private byte[] writeEnableUidMirrorNdef(NfcA nfcA) {
        /**
         * The bit for enabling or disabling the uid mirror is in pages 41/131/227 (0x29 / 0x83 / 0xE3)
         * depending on the tag type
         *
         * byte 0 of this pages holds the MIRROR byte
         * byte 2 of this pages holds the MIRROR_PAGE byte
         *
         * Mirror byte has these flags
         * bits 6+7 define which mirror shall be used:
         *   00b = no ASCII mirror
         *   01b = Uid ASCII mirror
         *   10b = NFC counter ASCII mirror
         *   11b = Uid and NFC counter ASCII mirror
         * bits 4+5 define the byte position within the page defined in MIRROR_PAGE byte
         *
         * MIRROR_PAGE byte defines the start of mirror.
         *
         * It is import that the end of mirror is within the user memory. These lengths apply:
         * Uid mirror: 14 bytes
         * NFC counter mirror: 6 bytes
         * Uid + NFC counter mirror: 21 bytes (14 bytes for Uid and 1 byte separation + 6 bytes counter value
         * Separator is x (0x78)
         *
         * This function writes the MIRROR_PAGE and MIRROR_BYTE to the place where the WRITE NDEF MESSAGE needs it
         *
         */


        writeToUiAppend(resultNfcWriting, "* Start enabling the Counter mirror *");
        // read page 227 on NTAG226 = Configuration page 0
        byte[] readPageResponse = getTagDataResponse(nfcA, identifiedNtagConfigurationPage);
        if (readPageResponse != null) {
            // get byte 0 = MIRROR
            byte mirrorByte = readPageResponse[0];
            // get byte 2 = MIRROR_PAGE
            byte mirrorPageByte = readPageResponse[2];
            writeToUiAppend(resultNfcWriting, "MIRROR content old: " + Utils.printByteBinary(mirrorByte));
            byte mirrorByteNew;
            // unsetting bit 7 = counter, we are doing UID mirroring only
            mirrorByteNew = Utils.unsetBitInByte(mirrorByte, 7);
            // setting bit 6
            mirrorByteNew = Utils.setBitInByte(mirrorByteNew, 6);
            // fix: start the mirror from byte 1 of the designated page, so bits are set as follows
            mirrorByteNew = Utils.unsetBitInByte(mirrorByteNew, 5);
            mirrorByteNew = Utils.setBitInByte(mirrorByteNew, 4);
            writeToUiAppend(resultNfcWriting, "MIRROR content new: " + Utils.printByteBinary(mirrorByteNew));
            // set the page where the mirror is starting, we use a fixed page here:
            int setMirrorPage = 15; // 0x0F
            byte mirrorPageNew = (byte) (setMirrorPage & 0x0ff);
            // rebuild the page data
            readPageResponse[0] = mirrorByteNew;
            readPageResponse[2] = mirrorPageNew;
            // write the page back to the tag
            byte[] writePageResponse = writeTagDataResponse(nfcA, identifiedNtagConfigurationPage, readPageResponse); // this is for NTAG216 only
            writeToUiAppend(resultNfcWriting, "write page to tag: " + Utils.bytesToHexNpe(readPageResponse));
            //byte[] writePageResponse = writeTagDataResponse(nfcA, 5, readPageResponse); // this is for NTAG216 only
            if (writePageResponse != null) {
                writeToUiAppend(resultNfcWriting, "SUCCESS: writing with response: " + Utils.bytesToHexNpe(writePageResponse));
                return readPageResponse;
            } else {
                writeToUiAppend(resultNfcWriting, "FAILURE: no writing on the tag");
            }
        }
        return null;
    }

    private byte[] getTagDataResponse(NfcA nfcA, int page) {
        byte[] response;
        byte[] command = new byte[]{
                (byte) 0x30,  // READ
                (byte) (page & 0x0ff), // page 0
        };
        try {
            response = nfcA.transceive(command); // response should be 16 bytes = 4 pages
            if (response == null) {
                // either communication to the tag was lost or a NACK was received
                writeToUiAppend(resultNfcWriting, "Error on reading page " + page);
                return null;
            } else if ((response.length == 1) && ((response[0] & 0x00A) != 0x00A)) {
                // NACK response according to Digital Protocol/T2TOP
                // Log and return
                writeToUiAppend(resultNfcWriting, "Error (NACK) on reading page " + page);
                return null;
            } else {
                // success: response contains ACK or actual data
                writeToUiAppend(resultNfcWriting, "SUCCESS on reading page " + page + " response: " + Utils.bytesToHexNpe(response));
                System.out.println("reading page " + page + ": " + Utils.bytesToHexNpe(response));
            }
        } catch (TagLostException e) {
            // Log and return
            writeToUiAppend(resultNfcWriting, "ERROR: Tag lost exception on reading");
            return null;
        } catch (IOException e) {
            writeToUiAppend(resultNfcWriting, "ERROR: IOEexception: " + e);
            e.printStackTrace();
            return null;
        }
        return response;
    }

    private byte[] writeTagDataResponse(NfcA nfcA, int page, byte[] dataByte) {
        byte[] response;
        byte[] command = new byte[]{
                (byte) 0xA2,  // WRITE
                (byte) (page & 0x0ff),
                dataByte[0],
                dataByte[1],
                dataByte[2],
                dataByte[3]
        };
        try {
            response = nfcA.transceive(command); // response should be 16 bytes = 4 pages
            if (response == null) {
                // either communication to the tag was lost or a NACK was received
                writeToUiAppend(resultNfcWriting, "Error on writing page " + page);
                return null;
            } else if ((response.length == 1) && ((response[0] & 0x00A) != 0x00A)) {
                // NACK response according to Digital Protocol/T2TOP
                // Log and return
                writeToUiAppend(resultNfcWriting, "Error (NACK) on writing page " + page);
                return null;
            } else {
                // success: response contains ACK or actual data
                writeToUiAppend(resultNfcWriting, "SUCCESS on writing page " + page + " response: " + Utils.bytesToHexNpe(response));
                System.out.println("response page " + page + ": " + Utils.bytesToHexNpe(response));
                return response;
            }
        } catch (TagLostException e) {
            // Log and return
            writeToUiAppend(resultNfcWriting, "ERROR: Tag lost exception");
            return null;
        } catch (IOException e) {
            writeToUiAppend(resultNfcWriting, "ERROR: IOEexception: " + e);
            e.printStackTrace();
            return null;
        }
    }

    private void formatNdef(Tag tag) {
        // trying to format the tag
        NdefFormatable format = NdefFormatable.get(tag);
        if(format != null){
            try {
                format.connect();
                format.format(new NdefMessage(new NdefRecord(NdefRecord.TNF_EMPTY, null, null, null)));
                format.close();
                showMessage("Tag formatted, try again to write on tag");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                showMessage("Failed to connect");
                e.printStackTrace();
            } catch (FormatException e) {
                // TODO Auto-generated catch block
                showMessage("Failed Format");
                e.printStackTrace();
            }
        }
        else {
            showMessage("Tag not formattable or already formatted to Ndef");
        }
    }

    private void writeToUiAppend(TextView textView, String message) {
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

    private void showMessage(String message) {
        getActivity().runOnUiThread(() -> {
            Toast.makeText(getContext(),
                    message,
                    Toast.LENGTH_SHORT).show();
            resultNfcWriting.setText(message);
        });
    }

    private void showWirelessSettings() {
        Toast.makeText(getView().getContext(), "You need to enable NFC", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mNfcAdapter != null) {

            if (!mNfcAdapter.isEnabled())
                showWirelessSettings();

            Bundle options = new Bundle();
            // Work around for some broken Nfc firmware implementations that poll the card too fast
            options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250);

            // Enable ReaderMode for all types of card and disable platform sounds
            // the option NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK is NOT set
            // to get the data of the tag afer reading
            mNfcAdapter.enableReaderMode(getActivity(),
                    this,
                    NfcAdapter.FLAG_READER_NFC_A |
                            NfcAdapter.FLAG_READER_NFC_B |
                            NfcAdapter.FLAG_READER_NFC_F |
                            NfcAdapter.FLAG_READER_NFC_V |
                            NfcAdapter.FLAG_READER_NFC_BARCODE |
                            NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
                    options);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mNfcAdapter != null)
            mNfcAdapter.disableReaderMode(getActivity());
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}