package net.takoli.simpleruntracker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class RunUpdateDialog extends DialogFragment {
	
	AlertDialog runUpdateDialog;
	int position;
	String date, dd, _dd, h, mm, ss;
	Run run;
	
	public RunUpdateDialog() { }
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		position = getArguments().getInt("pos");
		run = ((MainActivity) getActivity()).getRunDB().getRunList().get(position);
		date = run.getDateString();
	}
	
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	View view = getActivity().getLayoutInflater().inflate(R.layout.update_run_dialog, null);
    	runUpdateDialog = new AlertDialog.Builder(getActivity())
    			.setView(view)
                .setTitle("Update " + date + "'s run details")
                .setPositiveButton("Delete Run", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        	removeRun(position);
                        	((MainActivity) getActivity()).updateGraph();
                        	return; }
                    }
                )
                .setNeutralButton("Update Run", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        	updateRun(position);
                        	((MainActivity) getActivity()).updateGraph();
                        	return; }
                    }
                )
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        	return; }
                    }
                ).create();
        return runUpdateDialog;
    }

	@Override
    public void onResume() {
    	super.onStart();
      	EditText dd = (EditText) runUpdateDialog.findViewById(R.id.update_distance);
      	EditText _dd = (EditText) runUpdateDialog.findViewById(R.id.update_distance_dec);
      	EditText h = (EditText) runUpdateDialog.findViewById(R.id.update_time_h);
      	EditText mm = (EditText) runUpdateDialog.findViewById(R.id.update_time_mm);
      	EditText ss = (EditText) runUpdateDialog.findViewById(R.id.update_time_ss);
    	dd.setText(run.dd);
    	_dd.setText(run._dd);
    	h.setText(run.h);
    	mm.setText(run.mm);
    	ss.setText(run.ss);
//    	dd.setSelection(2);
//    	_dd.setSelection(2);
//    	h.setSelection(1);
//    	mm.setSelection(2);
//    	ss.setSelection(2);
//    	dd.addTextChangedListener(new TextWatcher() {
//			@Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
//			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
//			@Override public void afterTextChanged(Editable s) {
//				int length = s.length();
//				if (length < 2)
//					s.replace(0, 0, "00");
//				if (length > 2)
//					s.delete(0, length - 2);
//				RunUpdateDialog.this.dd = toInt(s.toString());
//			}
//		});
//    	_dd.addTextChangedListener(new TextWatcher() {
//			@Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
//			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
//			@Override public void afterTextChanged(Editable s) {
//				int length = s.length();
//				if (length < 2)
//					s.replace(0, 0, "00");
//				if (length > 2)
//					s.delete(0, length - 2);
//				RunUpdateDialog.this._dd = s.toString();
//			}
//		});
//    	h.addTextChangedListener(new TextWatcher() {
//			@Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
//			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
//			@Override public void afterTextChanged(Editable s) {
//				int length = s.length();
//				if (length < 1)
//					s.replace(0, 0, "0");
//				if (length > 1)
//					s.delete(0, length - 1);
//				RunUpdateDialog.this.h = s.toString();
//			}
//		});
//    	mm.addTextChangedListener(new TextWatcher() {
//			@Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
//			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
//			@Override public void afterTextChanged(Editable s) {
//				int length = s.length();
//				if (length < 2)
//					s.replace(0, 0, "00");
//				if (length > 2)
//					s.delete(0, length - 2);
//				if (s.charAt(0) > '5')
//					s.replace(0, 1, "0");
//				RunUpdateDialog.this.mm = s.toString();
//			}
//		});
//    	ss.addTextChangedListener(new TextWatcher() {
//			@Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
//			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
//			@Override public void afterTextChanged(Editable s) {
//				int length = s.length();
//				if (length < 2)
//					s.replace(0, 0, "00");
//				if (length > 2)
//					s.delete(0, length - 2);
//				if (s.charAt(0) > '5')
//					s.replace(0, 1, "0");
//				RunUpdateDialog.this.ss = s.toString();
//			}
//		});
    }
    
    private void updateRun(int position) {
    	((MainActivity) getActivity()).getRunDB()
			.updateRun(position, new int[] {toInt(dd), toInt(_dd), toInt(h), toInt(mm), toInt(ss)});
    	((MainActivity) getActivity()).getRunAdapter().notifyDataSetChanged();
    }
    
    private void removeRun(int position) {
    	((MainActivity) getActivity()).getRunDB()
			.removeRun(position);
    	((MainActivity) getActivity()).getRunAdapter().notifyDataSetChanged();
    }
    
    private static String twoDigits(String st) {
    	int length = st.length();
    	if (length >= 2)
    		return st.substring(length - 2, length);
    	else if (length == 1)
    		return "0" + st;
    	return "00";
    }
    
    private static int toInt(String st) {
    	return Integer.parseInt(st);
    }

}
