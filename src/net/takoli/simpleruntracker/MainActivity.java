package net.takoli.simpleruntracker;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.example.simpleruntracker.R;

public class MainActivity extends Activity {
	
	Fragment enterRun;
	FragmentTransaction fragTrans;
	FrameLayout fragLayout;
	RelativeLayout mainLayout;
	DisplayMetrics dm;
	int screenHeight, screenWidth;
	
	GestureDetector gestDect;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Set up variables
		setContentView(R.layout.activity_main);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		dm = getResources().getDisplayMetrics();
		screenHeight = (int)(dm.heightPixels);
		screenWidth = (int)(dm.widthPixels);
		mainLayout = (RelativeLayout) findViewById(R.id.main);
		fragLayout = new FrameLayout(this);
		enterRun = new EnterRun();
		
		// "Enter Run" Fragment setup
		fragLayout.setLayoutParams(new FrameLayout.LayoutParams(screenWidth, screenHeight / 2));
		fragLayout.setId(R.id.enter_run_frame);
		fragLayout.setBackgroundColor(Color.WHITE);
		mainLayout.addView(fragLayout);
		
		fragTrans = getFragmentManager().beginTransaction();
		fragTrans.add(R.id.enter_run_frame, enterRun);
		fragTrans.commit();
		
		gestDect = new GestureDetector(this, new MyGestureListener());
		fragLayout.setOnTouchListener(new OnTouchListener() {
		    public boolean onTouch(View v, MotionEvent event) {
		        return gestDect.onTouchEvent(event);
		    }
		});
		
		// "My runs" mid-section setup
		findViewById(R.id.my_runs).setBackgroundColor(Color.LTGRAY); //for testing
		// CursorLoader for async load... change later??
		ListView myRuns = (ListView) findViewById(R.id.my_runs);
		ArrayList<String> justTest = new ArrayList<String>(Arrays.asList(
				new String[] {"firstrun", "secondrun", "thirdrun", "fourthrun", "fifthrun"}));  // just testing
		myRuns.setAdapter(new RunAdapter(this, R.layout.one_run, justTest));
		//myRuns.setOnItemClickListener...myRuns  -> expand details (eg two lines)
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return gestDect.onTouchEvent(event);
	}
	
	@Override
	protected void onResume() {		
		super.onResume();
		fragLayout.setY(screenHeight * -3/10);
		slideDown();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void slideUp() {
		fragLayout.animate().setDuration(700).translationY(screenHeight * -3/10);
	}
	public void slideDown() {
		fragLayout.animate().setDuration(700).translationY(0);
	}
	
	
	class MyGestureListener extends SimpleOnGestureListener {
		private static final int SWIPE_MIN_DISTANCE = 20;
		private static final int SWIPE_BAD_MAX_DIST = 200;
	    private static final int SWIPE_THRESHOLD_VELOCITY = 20;
		
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			float deltaY = e2.getY() - e1.getY();
			float absDeltaX = Math.abs(e2.getX() - e1.getX());
				if (absDeltaX > SWIPE_BAD_MAX_DIST || Math.abs(velocityY) < SWIPE_THRESHOLD_VELOCITY) {
					return false; }
				else if (deltaY < -(SWIPE_MIN_DISTANCE)) {
					Log.i("run", "onFling UP");
					slideUp();
					return true; }
				else if (deltaY > SWIPE_MIN_DISTANCE) {
					Log.i("run", "onFling DOWN");
					slideDown();
					return true; }
				return false;
		}
		@Override
		public boolean onDown(MotionEvent e) {
			return true; }
	}
}
