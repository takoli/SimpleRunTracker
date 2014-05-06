package net.takoli.simpleruntracker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class RunDB {

	private ArrayList<Run> runList;
	private long sumDistDec, sumTimeSec;
	
	private final String FILE_NAME = "SimpleRunTrackerDB.csv";
	private final int MAX = 100;
	private File intDir, extDownloadsDir;

	// This wil run every time the app starts up (or OnCreate is called...)
	public RunDB(Context context) {
		runList = new ArrayList<Run>();
		sumDistDec = sumTimeSec = 0;
		Run nRun;
		// /data/data/net.takoli.simpleruntracker/files/ - where OpenFileOutput saves
		try {
			FileOutputStream outputStream = context.openFileOutput(FILE_NAME, Context.MODE_APPEND);
			outputStream.close();     // we just made sure the file existed in a tricky way
			FileInputStream inputStream = context.openFileInput(FILE_NAME);
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			String line;
			while ((line = reader.readLine()) != null) {
				nRun = new Run(line);
				runList.add(nRun);
				// for statistics:
				sumDistDec += nRun.getDistDec();
				sumTimeSec += nRun.getTimeSec();
			}
			inputStream.close();
		} catch (Exception e) {
			Toast.makeText(context,"Can't read SimpleRunTrackerDB.csv", Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
	}
		
	public void addNewRun(Context context, Run newRun) {
		runList.add(newRun);
		// for statistics:
		sumDistDec += newRun.getDistDec();
		sumTimeSec += newRun.getTimeSec();
		//Log.i("run", "sumDist: " + sumDistDec);
	}
	
	public void updateRun(int pos, int[] updates) {
		Run toUpdate = runList.get(pos);
		sumDistDec -= toUpdate.getDistDec();
		sumTimeSec -= toUpdate.getTimeSec();
		
		toUpdate.dd = updates[0];
		toUpdate._dd = updates[1];
		toUpdate.h = updates[2];
		toUpdate.mm = updates[3];
		toUpdate.ss = updates[4];
		sumDistDec += toUpdate.getDistDec();
		sumTimeSec += toUpdate.getTimeSec();
	}
	
	public void removeRun(int pos) {
		Run toUpdate = runList.get(pos);
		sumDistDec -= toUpdate.getDistDec();
		sumTimeSec -= toUpdate.getTimeSec();
		runList.remove(pos);
	}
	
	public Run getLastRun() {
		int lastIndex = runList.size() - 1;
		if (lastIndex == -1)
			return null;
		return runList.get(lastIndex);
	}
	
	public int[] getLastValues() {
		int lastIndex = runList.size() - 1;
		if (lastIndex == -1)
			return new int[] {0, 0, 0, 0, 0};
		Run lastRun = runList.get(lastIndex);
		return new int[] {lastRun.dd, lastRun._dd, lastRun.h, lastRun.mm, lastRun.ss};
	}
	
	public ArrayList<Run> getRunList() {
		return runList;
	}
	
	// for STATISTICS:
	public int getAvgDistDec() {
		if (runList.size() == 0) return 0;
		else return (int) (1.0 * sumDistDec / runList.size());
	}
	public int getAvgPaceSec() {
		if (sumDistDec == 0) return 0;
		else return (int) (sumTimeSec * 100 / sumDistDec);
	}
	
	// save all changes
	public void updateAndSaveRunDB(Context context) {  // might consider serialization instead of CSV in the future
		int size = runList.size();
		Collections.sort(runList, new Comparator<Run>() {
			public int compare(Run a, Run b) {
		        return a.date.compareTo(b.date);
		    }
		});
		try {
			FileOutputStream outputStream = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
			int start = size > MAX ? runList.size()-MAX : 0;
			for (int i = start; i < size; i++)
				outputStream.write((runList.get(i).toString()+"\n").getBytes());
			outputStream.close();
		} catch (Exception e) {
			Toast.makeText(context, FILE_NAME +" is not reachable to append",Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
	}
	
	// delete ALL records
	public void deleteDB(Context context) {
		sumDistDec = 0;
		sumTimeSec = 0;
		try {
			context.deleteFile(FILE_NAME);
			runList.clear();
			sumTimeSec = sumDistDec = 0;
		} catch (Exception e) {
			Toast.makeText(context,"Can't delete " + FILE_NAME, Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
	}
	

	// Save DB to SD Card. Also considering Google Docs sync in the future
	public void saveToExternal(Context context) {
		updateAndSaveRunDB(context);
		if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			Toast.makeText(context, "SD Card is not available",Toast.LENGTH_LONG).show();
			return;
		}
		try {
			intDir = context.getFilesDir();
			extDownloadsDir =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
			File src = new File(intDir, FILE_NAME);
			File dst = new File(extDownloadsDir, FILE_NAME);			
			InputStream is = new FileInputStream(src);
        	OutputStream os = new FileOutputStream(dst);
        	byte[] data = new byte[is.available()];
			is.read(data);
        	os.write(data);
        	is.close();
        	os.close();
        		
			Toast.makeText(context, "Saved on SD Card's Donwloads folder",Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			Toast.makeText(context, "File write error", Toast.LENGTH_LONG)
					.show();
		}
	}
}
