package net.takoli.simpleruntracker;

import android.app.Fragment;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.takoli.simpleruntracker.R;

public class EnterRun extends Fragment {

	MyNumberPicker dist10, dist1, dist_1, dist_01;
	MyNumberPicker hour, min10, min1, sec10, sec1;
	TextView div_d, div_th, div_tm;
	TextView distance, time;
	DisplayMetrics dm;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.enter_run, container, false);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		dm = getResources().getDisplayMetrics();
		
		// Set up DISTANCE fields
		dist10 = (MyNumberPicker) getView().findViewById(R.id.dist10);
		dist1 = (MyNumberPicker) getView().findViewById(R.id.dist1);
		div_d = ((TextView) getView().findViewById(R.id.div_d));
			div_d.setTextSize(dist1.getTextSize());
		dist_1 = (MyNumberPicker) getView().findViewById(R.id.dist_1);
		dist_01 = (MyNumberPicker) getView().findViewById(R.id.dist_01);
		
		// Set up TIME fields
		hour = (MyNumberPicker) getView().findViewById(R.id.hour);
		div_th = ((TextView) getView().findViewById(R.id.div_th));
			div_th.setTextSize(hour.getTextSize());
		min10 = (MyNumberPicker) getView().findViewById(R.id.min10);
			min10.setMaxValue(5);
		min1 = (MyNumberPicker) getView().findViewById(R.id.min1);
		div_tm = ((TextView) getView().findViewById(R.id.div_tm));
			div_tm.setTextSize(hour.getTextSize());
		sec10 = (MyNumberPicker) getView().findViewById(R.id.sec10);
			sec10.setMaxValue(5);
		sec1 = (MyNumberPicker) getView().findViewById(R.id.sec1);
		
		// "Distance" and "Time" 
		distance = (VerticalTextView) getActivity().findViewById(R.id.distance);
		distance.setTextColor(0xaaFF0000);
		distance.setTextSize(dist1.getTextSize());
		time = (VerticalTextView) getActivity().findViewById(R.id.time);
		time.setTextColor(0xaaFF0000);
		time.setTextSize(dist1.getTextSize());
	}
}
