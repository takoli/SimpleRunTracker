package net.takoli.simpleruntracker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class FirstRunDialog extends DialogFragment {

	MainActivity main;
	TextView unitTV;
	String unit;
	boolean updated = false;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	main = (MainActivity) getActivity();
    	unit = main.getUnit();
        return new AlertDialog.Builder(getActivity())
        		.setTitle("Choose unit of distance")
        		.setMessage("Do you want to enter distance in miles or kilometers")
                .setNegativeButton("Miles",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        	updated = unit.compareTo("ni") != 0;
                        	unit = "mi"; }
                    }
                )
                .setPositiveButton("Kilometers",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        	updated = unit.compareTo("km") != 0;
                        	unit = "km"; }
                    }
                ).create();
    }
    
    @Override
    public void onStop() {
    	super.onStop();
    	if (updated) {
    	main.setUnit(unit);
	    	Toast.makeText(main, "New runs will be entered in " + main.getUnitInFull(), Toast.LENGTH_SHORT).show();
	    	unitTV = ((TextView) main.findViewById(R.id.dist_unit));
	    	unitTV.setText(unit);
	    	main.getGraphView().setRunList(main.getRunDB(), main.getUnit());
	    	main.updateGraph();}
    }
}