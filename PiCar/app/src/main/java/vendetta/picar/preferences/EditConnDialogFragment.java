package vendetta.picar.preferences;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import vendetta.picar.R;
import vendetta.picar.connection.ConnectionConfig;
import vendetta.picar.connection.ConnectionTypeEn;

import static android.text.InputType.TYPE_CLASS_TEXT;
import static android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD;

public class EditConnDialogFragment extends DialogFragment {

    private IConnEditable configHolder;
    private EditText connectionNameET, connectionValueET, addrET, secretET;

    public interface IConnEditable {
        void onSaveButtonPress(ConnectionConfig connectionConfig);

        ConnectionConfig getActiveSelection();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the IConnEditable so we can send events to the host
            configHolder = (IConnEditable) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(getActivity().toString() + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_connection_edit, null);
        connectionNameET = v.findViewById(R.id.connectionNameET);
        connectionValueET = v.findViewById(R.id.connectionValueET);
        TextView connectionIdEditTV = v.findViewById(R.id.connectionIdEditTV);
        connectionNameET.setText(configHolder.getActiveSelection().getName());
        connectionValueET.setText(configHolder.getActiveSelection().getIdentifier());
        connectionIdEditTV.setText(configHolder.getActiveSelection().getConnType().getSpecific());

        if (configHolder.getActiveSelection().getConnType().equals(ConnectionTypeEn.WIFI_AP)) {
            LinearLayout editConnLinLayout = v.findViewById(R.id.editConnLinLayout);
            addrET = new EditText(getContext());
            LinearLayout lineIP = inflateNewPreferenceLine("IP:", configHolder.getActiveSelection().getAddrValue(), addrET);
            secretET = new EditText(getContext());
            LinearLayout linePW = inflateNewPreferenceLine("Password:", configHolder.getActiveSelection().getSecretValue(), secretET);

            editConnLinLayout.addView(lineIP);
            editConnLinLayout.addView(linePW);
        }


        builder.setView(v)
                .setTitle("Editing " + configHolder.getActiveSelection().getName())
                .setIcon(android.R.drawable.ic_menu_edit)
                .setPositiveButton("Save", (dialog, id) ->
                        configHolder.onSaveButtonPress(new ConnectionConfig(connectionNameET.getText().toString(), configHolder.getActiveSelection().getConnType(),
                                connectionValueET.getText().toString(),
                                configHolder.getActiveSelection().getConnType().equals(ConnectionTypeEn.WIFI_AP) ? addrET.getText().toString(): "",
                                configHolder.getActiveSelection().getConnType().equals(ConnectionTypeEn.WIFI_AP) ? secretET.getText().toString() : "")))
                .setNegativeButton("cancel", (dialog, id) -> {// User cancelled the dialog
                });
        return builder.create();
    }

    @NonNull
    private LinearLayout inflateNewPreferenceLine(String keyInfo, String keyText, EditText savedEditText) {
        LinearLayout line = new LinearLayout(getContext());
        LinearLayout.LayoutParams lineLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        line.setLayoutParams(lineLayoutParams);
        line.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams textViewLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        textViewLayoutParams.width = 220;
        TextView keyValTextView = new TextView(getContext());
        keyValTextView.setLayoutParams(textViewLayoutParams);
        keyValTextView.setText(keyInfo);
        line.addView(keyValTextView);

        LinearLayout.LayoutParams editTextLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        savedEditText.setLayoutParams(editTextLayoutParams);
        if (keyText.toLowerCase().contains("password"))
            savedEditText.setInputType(TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_PASSWORD);
        savedEditText.setText(keyText);
        line.addView(savedEditText);
        return line;
    }
}
