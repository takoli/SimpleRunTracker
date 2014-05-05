package net.takoli.simpleruntracker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class ConfirmDeleteDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
        		.setMessage("This will remove all run records and can not be undone.")
                .setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            return; }
                    }
                )
                .setPositiveButton("Delete",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        	((MainActivity) getActivity()).getRunDB().deleteDB(getActivity());
                            ((MainActivity) getActivity()).getRunAdapter().notifyDataSetChanged(); }
                    }
                ).create();
    }
}