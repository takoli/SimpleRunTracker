package net.takoli.simpleruntracker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class ChartFullScreenDialog extends DialogFragment {
	
	private MainActivity main;
	private AlertDialog chartFullScreenView;
	private RunDB runDB;
	private String unit;
	private GraphViewFull graph;
	private int height;
	private int width;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	main = (MainActivity) getActivity();
        chartFullScreenView =  new AlertDialog.Builder(getActivity())
        		.setView(getActivity().getLayoutInflater().inflate(R.layout.chart_full_screen_dialog, null))
        		.setTitle("")
                .setPositiveButton("Done",null)
                .create();
        return chartFullScreenView;
	}
    
    @Override
    public void onResume() {
    	super.onResume();
    	// set sizes based on orientation
    	DisplayMetrics dm = getResources().getDisplayMetrics();
    	width = (int) (dm.widthPixels * 0.98);
    	height = (int) (dm.heightPixels * 0.4);
    	Display display = ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        if (display.getRotation() == Configuration.ORIENTATION_PORTRAIT)
        	height = (int) (height * 1.8);
    	getDialog().getWindow().setLayout(width, height);
    	// get resources and listeners
    	graph = (GraphViewFull) chartFullScreenView.findViewById(R.id.chart_full_screen);
    	SeekBar seekbar = (SeekBar) chartFullScreenView.findViewById(R.id.seekBar);
    	seekbar.setMax(45);
    	seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) { }
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) { }
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				int n = 5 + progress;
				showCurrentNumber(n);
				graph.updateData(n);
				graph.invalidate();
			}
		});
	seekbar.setProgress(20);
    	MainActivity main = (MainActivity) getActivity();
    	graph.setRunList(main.getRunDB(), main.getUnit());
    }
    
    private void showCurrentNumber(int n) {
    	TextView currentNumber = (TextView) chartFullScreenView.findViewById(R.id.chart_run_number);
    	if (currentNumber == null)
    	    return;
    	currentNumber.setTextSize(width / 55);
    	currentNumber.setText("last " + n + " workouts");
    	currentNumber.setVisibility(View.VISIBLE);
    	currentNumber.setAlpha(1);
    	currentNumber.animate().alpha(0).setDuration(1000);
    }
}
