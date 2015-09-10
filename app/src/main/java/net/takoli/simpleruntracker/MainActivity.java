package net.takoli.simpleruntracker;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import net.takoli.simpleruntracker.adapter.RunAdapter;
import net.takoli.simpleruntracker.adapter.RunAdapterObserver;
import net.takoli.simpleruntracker.adapter.animator.FadeInUpAnimator;
import net.takoli.simpleruntracker.graph.GraphViewFull;
import net.takoli.simpleruntracker.graph.GraphViewSmall;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences settings;
    private FragmentTransaction fragTrans;
    private FragmentManager fragMngr;
    private DisplayMetrics dm;
    private int screenHeight, screenWidth;
    protected GestureDetector gestDect;

    // Enter run and stats fragment
    private FrameLayout runFragLayout;
	protected Fragment enterRun;
	private StatsFragment statsFragment;
    private VerticalTextView distance, time;
    private RadioGroup dateRadioGroup;
    private Button enterRunButton;
    private boolean runFragOpen;
    private int enterRunBottom;
    private static final AnticipateOvershootInterpolator slideUpInterpolator =
                                                new AnticipateOvershootInterpolator(0.8f);
    private static final AnticipateOvershootInterpolator slideDownInterpolator =
                                                new AnticipateOvershootInterpolator(0.92f);

    // Run list view
    private RunDB runDB;
    private RecyclerView runListView;
    private RunAdapter runAdapter;
    private RecyclerView.LayoutManager runListLM;
    private int listTop;
    private int listBottom;
    private int shiftedDown = 0;
    private int cardHeight = 0;
    private LinearLayout.LayoutParams runListParams;


    // Graphs
	private GraphViewSmall graphSmall;
	private GraphViewFull graphFull;
	private ChartFullScreenDialog graphFullFragment;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

		//getActionBar().setDisplayShowTitleEnabled(false);
		settings = getPreferences(MODE_PRIVATE);
		
		// Set up variables and fields
        dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
		screenHeight = (dm.heightPixels);
		screenWidth = (dm.widthPixels);
		enterRun = new EnterRun();

		// "Enter Run" top fragment setup:
        runFragLayout = (FrameLayout) findViewById(R.id.enter_run_fragment_frame);
		runFragLayout.setLayoutParams(new RelativeLayout.LayoutParams(screenWidth, screenHeight / 2));
		runFragLayout.setId(R.id.enter_run_frame);
		runFragLayout.setBackgroundColor(Color.WHITE);
		fragMngr = getFragmentManager();
		fragTrans = fragMngr.beginTransaction();
		fragTrans.replace(R.id.enter_run_frame, enterRun);
		fragTrans.commit();
		// enable fling up and down to open/close the top panel
		gestDect = new GestureDetector(this, new MainGestureListener());
		runFragLayout.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestDect.onTouchEvent(event);
            }
        });
			
		// List of Runs setup:
		runDB = new RunDB(this);
		runDB.setDBLimit(getDBLimit());

        runAdapter = new RunAdapter(this, runDB);
        runAdapter.registerAdapterDataObserver(new RunAdapterObserver(runAdapter, runDB));
        runListLM = new LinearLayoutManager(MainActivity.this);
		runListView = (RecyclerView) findViewById(R.id.my_runs);
        runListView.setLayoutManager(runListLM);
        runListView.setAdapter(runAdapter);
        runListView.setItemAnimator(new FadeInUpAnimator());
        runListView.getItemAnimator().setAddDuration(500);
        runListView.getItemAnimator().setRemoveDuration(500);
		runListView.setHasFixedSize(true);

        // Graph initial setup
		graphSmall = (GraphViewSmall) findViewById(R.id.graph);
		graphSmall.setRunList(runDB, getUnit());
		
		// check for first run
		if (runDB.isEmpty())
			(new FirstRunDialog()).show(fragMngr, "FirstRunDialog");
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            listTop = runListView.getTop();
            listBottom = runListView.getBottom();
            enterRunBottom = screenHeight / 2;
            runListParams = (LinearLayout.LayoutParams) runListView.getLayoutParams();
            Log.i("run", "M: " + findViewById(R.id.main_space).getBottom() + ", " + listTop + ", " + listBottom + "; " + graphSmall.getTop());
        }
    }

    @Override
	protected void onResume() {
		super.onResume();
		if (runFragLayout != null)
			runFragLayout.setY(screenHeight * -3/10);
        distance = (VerticalTextView) findViewById(R.id.distance);
        time = (VerticalTextView) findViewById(R.id.time);
        dateRadioGroup = (RadioGroup) findViewById(R.id.date_radiobuttons);
        enterRunButton = (Button) findViewById(R.id.enter_run_button);
		slideDown();
    }

    @Override
    protected void onPause() {
        super.onPause();
        slideUp();
    }

    @Override
	protected void onStop() {
        super.onStop();
        runDB.saveRunDB(this);
	}
	
	@Override
	public void onBackPressed() {
		statsFragment = (StatsFragment) fragMngr.findFragmentByTag("statsFragment");
		if (statsFragment != null && statsFragment.isActive())
			statsFragment.animateOut();
		else
			super.onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    	case R.id.stats_icon:
	    	case R.id.statistics:
	    		statsFragment = (StatsFragment) fragMngr.findFragmentByTag("statsFragment");
	    		if (statsFragment == null) {
	    			statsFragment = new StatsFragment();
	    			statsFragment.setRetainInstance(true);
		    		fragTrans = fragMngr.beginTransaction();
		    		fragTrans.add(R.id.main, statsFragment, "statsFragment");
		    		fragTrans.commit();
		    	}
	    		else {
	    			if (statsFragment.isActive())
	    				statsFragment.animateOut();
	    			else
	    				statsFragment.animateIn();
	    		}
	            return true;
	    	case R.id.settings:
	    		(new SettingsDialog()).show(fragMngr, "SettingsDialog");
	            return true; 
	        case R.id.graph_it:
	    		graphFullFragment = (ChartFullScreenDialog) getFragmentManager().findFragmentByTag("ChartFullScreen");
			if (graphFullFragment == null) {
				graphFullFragment = new ChartFullScreenDialog();
				graphFullFragment.show(fragMngr, "ChartFullScreen");
			}
	            return true; 
	    	case R.id.export_list_of_runs:
	        	runDB.saveToExternalMemory(this);
	        	Intent emailIntent = runDB.emailIntent(this);
	        	if (emailIntent != null)
	        		startActivity(emailIntent);
	            return true;
	        case R.id.delete_db:
	        	(new ConfirmDeleteDialog()).show(fragMngr, "confirmDeleteDB");
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getY() / screenHeight > 0.8) {
			graphFullFragment = (ChartFullScreenDialog) getFragmentManager().findFragmentByTag("ChartFullScreen");
			if (graphFullFragment == null) {
				graphFullFragment = new ChartFullScreenDialog();
				graphFullFragment.show(fragMngr, "ChartFullScreen");
			}
			return true;
		}
		else
			return gestDect.onTouchEvent(event);
	}
	
	public GraphViewSmall getGraphView() {
		return graphSmall;
	}
	
	public RunDB getRunDB() {
		return runDB;
    }

	public RecyclerView getRunList() {
		return runListView;
	}
	
	public RunAdapter getRunAdapter() {
		return runAdapter;
    }
	
	public void setUnit(String unit) {
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("unit", unit);
		editor.commit();
	}
	public String getUnit() {
		return settings.getString("unit", "mi");
	}
	public String getUnitInFull() {
		String u = settings.getString("unit", "mi");
		if (u.compareTo("mi") == 0)
			return "miles";
		else if (u.compareTo("km") == 0)
			return "kilometers";
		else return "";
	}
	public void setDBLimit(String limit) {
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("limit", limit);
		editor.commit();
		runDB.setDBLimit(limit);
		runDB.ensureDBLimit();
	}
	public String getDBLimit() {
		return settings.getString("limit", "300");
	}
	public void updateGraph() {
		if (graphSmall != null) {
			graphSmall.updateData();
			graphSmall.invalidate(); }
	}

    private void slideUp() {
		distance.animate().translationX(0).setDuration(1000);
		time.animate().translationX(0).setDuration(1000);
		// make the date radio buttons disappear
		dateRadioGroup.animate().setDuration(700).alpha(0);
		// change the button text
		enterRunButton.setText("Open");
		enterRunButton.setTextColor(0xFFFFFFFF);
		enterRunButton.animate().setDuration(700).alpha(0.5f);
		enterRunButton.setClickable(false);
		// slide the fragment up
		runFragLayout.animate()
                .setDuration(700)
                .setInterpolator(slideUpInterpolator)
                .translationY(screenHeight * -35 / 100);
		runFragOpen = false;
        // adjust runList visibility
        shiftBackRunList();
	}
    private void slideDown() {
		float moveTextBy = dm.widthPixels / 5.5f - ((BigNumberPicker) findViewById(R.id.dist10)).getTextSize()*dm.density*2;
		distance.animate().translationXBy(moveTextBy).setDuration(1000);
		time.animate().translationXBy(-moveTextBy).setDuration(1000);
		// make the date radio buttons reappear
		dateRadioGroup.animate().setDuration(700).alpha(1);
		// change the button text
		enterRunButton.setText("Enter Run");
		enterRunButton.setTextColor(0xFF000000);
		enterRunButton.animate().setDuration(700).alpha(1);
		enterRunButton.setClickable(true);
		// slide the fragment down
		runFragLayout.animate()
                .setDuration(700)
                .setInterpolator(slideDownInterpolator)
                .translationY(0);
		runFragOpen = true;
        // adjust runList visibility
        shiftDownRunListIfNeeded();
	}

    private void shiftBackRunList() {
        shiftedDown = 0;
        runListView.animate().translationY(0).setDuration(700);
    }

    public void shiftBackRunListByOneIfNeeded() {
        final int noOfCards = runListLM.getChildCount();
        if (shiftedDown == 0 || noOfCards < 2)
            return;
        if (cardHeight == 0)
            cardHeight = runListLM.getChildAt(1).getBottom() - runListLM.getChildAt(0).getBottom();
        final int listCurrentBottom = listTop + shiftedDown + (int)((noOfCards + 1.2) * cardHeight);
        Log.i("run", "shift0? " + listTop + "; " + shiftedDown + " :: " + listCurrentBottom + ";" + listBottom);
        int overLap = listCurrentBottom - listBottom;
        Log.i("run", "overlap: " + overLap);
        if (overLap > 0) {
            Log.i("run", "overlapping bottoms");
            int shiftUp = overLap < shiftedDown ? overLap : shiftedDown;
            Log.i("run", "translate");
            shiftedDown -= shiftUp;
            runListView.animate().translationYBy(-shiftUp).setDuration(10);
        }
    }

    private void shiftDownRunListIfNeeded() {
        final int TOLERATE = 100;
        runListView.postDelayed(new Runnable() {
            @Override
            public void run() {
                final int noOfCards = runListLM.getChildCount();
                if (noOfCards < 1)
                    return;
                final int lastCardBottom = listTop + runListLM.getChildAt(noOfCards - 1).getBottom();
                Log.i("run", "measures: " + listTop + ", " + lastCardBottom + ", " + listBottom + "; " + enterRunBottom);
                if ((lastCardBottom - listTop) < (listBottom - enterRunBottom)) {
                    Log.i("run", "fits in window, move by: " + (enterRunBottom - listTop));
                    shiftedDown = enterRunBottom - listTop;
                    runListView.animate().translationY(shiftedDown).setDuration(700);
                } else if (listBottom - lastCardBottom > TOLERATE) {
                    Log.i("run", "bigger than window, move by: " + (listBottom - lastCardBottom));
                    shiftedDown = listBottom - lastCardBottom;
                    runListView.animate().translationY(shiftedDown).setDuration(700);
                } else {
                    //Log.i("run", "no move, reset");
                    shiftedDown = 0;
                    runListView.animate().translationY(0).setDuration(700);
                }
            }
        }, 300);
    }
	
	
	
	// TO OPEN AND CLOSE TOP PANEL GestureListener
	class MainGestureListener extends SimpleOnGestureListener {
		private static final int SWIPE_MIN_DISTANCE = 20;
		private static final int SWIPE_BAD_MAX_DIST = 200;
	    private static final int SWIPE_THRESHOLD_VELOCITY = 20;
		
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			if (e1 == null || e2 == null)
				return false;
			float deltaY = e2.getY() - e1.getY();
			float deltaX = e2.getX() - e1.getX();
			if ((e1.getX() / screenWidth > 0.15 &&  e1.getX() / screenWidth < 0.85) 
					&&  e1.getY() / screenHeight < 0.33) {
				//Log.i("run", "onFling out of area");
				return false; }
			if (Math.abs(deltaX) > SWIPE_BAD_MAX_DIST || Math.abs(velocityY) < SWIPE_THRESHOLD_VELOCITY) {
				//Log.i("run", "onFling invalid");
				return false; }
			else if (deltaY < -(SWIPE_MIN_DISTANCE)) {
				//Log.i("run", "onFling UP");
				slideUp();
				return true; }
			else if (deltaY > SWIPE_MIN_DISTANCE) {
				//Log.i("run", "onFling DOWN");
				if (!runFragOpen) slideDown();
					return true; }
			return false;
		}
		@Override
		public boolean onDown(MotionEvent e) {
			if (!runFragOpen)
				slideDown();
			return true; }
	}
}