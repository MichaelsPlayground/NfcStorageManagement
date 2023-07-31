package de.androidcrypto.nfcstoragemanagement;

import static de.androidcrypto.nfcstoragemanagement.Utils.doVibrate;
import static de.androidcrypto.nfcstoragemanagement.Utils.playSinglePing;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.IOException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PersonalizeTagFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PersonalizeTagFragment extends Fragment implements NfcAdapter.ReaderCallback {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private static final String TAG = PersonalizeTagFragment.class.getName();
    private com.google.android.material.textfield.TextInputEditText resultNfcWriting;

    private Ntag21xMethods ntagMethods;
    private NfcAdapter mNfcAdapter;
    private Tag nTag;
    private NfcA nfcA;
    private Ndef ndef;

    private int identifiedNtagConfigurationPage; // this  is the stating point for any work on configuration
    private byte[] tagUid; // written by onDiscovered

    public PersonalizeTagFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ReceiveFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PersonalizeTagFragment newInstance(String param1, String param2) {
        PersonalizeTagFragment fragment = new PersonalizeTagFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this.getContext());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        resultNfcWriting = getView().findViewById(R.id.etPersonalizeResult);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(getView().getContext());
        ntagMethods = new Ntag21xMethods(getActivity(), resultNfcWriting);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_personalize_tag, container, false);
    }

    private boolean runCompletePersonalize() {
        /*
        This are the steps that will run when a tag is tapped:
        1. write the NDEF template to the tag
        2. disable all existing mirror
        3. enable UID mirroring
        4. write the UID-based MAC to the tag
         */

        // step 1 a: connect to NDEF and write the NDEF template to the tag
        boolean success = connectNfca(nfcA, ndef);
        if (!success) {
            writeToUiAppend(resultNfcWriting, "could not connect with NDEF, aborted");
            return false;
        }
        // step 1 b:

        return false;
    }

    private boolean connectNfca(NfcA nfcA, Ndef ndef) {
        if ((nfcA == null) || (ndef == null)) {
            writeToUiAppend(resultNfcWriting, "nfcA or ndef is NULL, aborted");
            return false;
        }
        try {
            if (ndef.isConnected()) {
                Log.d(TAG, "ndef was connected, trying to close ndef");
                ndef.close();
                Log.d(TAG, "ndef is closed");
            }
            nfcA.connect();
            writeToUiAppend(resultNfcWriting, "nfcA is connected");
            return true;
        } catch (IOException e) {
            writeToUiAppend(resultNfcWriting, "ERROR: IOException " + e.toString());
            e.printStackTrace();
            return false;
        }
    }

    private boolean connectNdef(NfcA nfcA, Ndef ndef) {
        if ((nfcA == null) || (ndef == null)) {
            writeToUiAppend(resultNfcWriting, "nfcA or ndef is NULL, aborted");
            return false;
        }
        try {
            if (nfcA.isConnected()) {
                Log.d(TAG, "nfcA was connected, trying to close nfcA");
                ndef.close();
                Log.d(TAG, "nfcA is closed");
            }
            ndef.connect();
            writeToUiAppend(resultNfcWriting, "ndef is connected");
            return true;
        } catch (IOException e) {
            writeToUiAppend(resultNfcWriting, "ERROR: IOException " + e.toString());
            e.printStackTrace();
            return false;
        }
    }

    // This method is running in another thread when a card is discovered
    // !!!! This method cannot cannot direct interact with the UI Thread
    // Use `runOnUiThread` method to change the UI from this method
    @Override
    public void onTagDiscovered(Tag tag) {
        // Read and or write to Tag here to the appropriate Tag Technology type class
        // in this example the card should be an Ndef Technology Type

        nfcA = NfcA.get(tag);
        ndef = Ndef.get(tag);

        writeToUiAppend(resultNfcWriting, "NFC tag discovered");

        boolean personalizeResult = runCompletePersonalize();


        /*
        Ndef mNdef = Ndef.get(tag);

        if (mNdef == null) {
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(),
                        "mNdef is null",
                        Toast.LENGTH_SHORT).show();
            });
        }

        // Check that it is an Ndef capable card
        if (mNdef != null) {

        }
        */
        doVibrate(getActivity());
        playSinglePing(getContext());
    }

    /**
     * section for UI service methods
     */

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

    private void showWirelessSettings() {
        Toast.makeText(this.getContext(), "You need to enable NFC", Toast.LENGTH_SHORT).show();
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
            mNfcAdapter.enableReaderMode(this.getActivity(),
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
            mNfcAdapter.disableReaderMode(this.getActivity());
    }

}