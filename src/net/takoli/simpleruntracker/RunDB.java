package net.takoli.simpleruntracker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class RunDB {

	private ArrayList<Run> runList;
	private final String FILE_NAME = "SimpleRunTrackerDB.csv";
	private final String TEMP_FILE_NAME = "SimpleRunTrackerDB_temp.csv";
	private final int MAX = 100;
	File intDir, extDownloadsDir;

	public RunDB(Context context) {
		runList = new ArrayList<Run>();
		Run nRun;
		// /data/data/net.takoli.simpleruntracker/files/ - where OpenFileOutput saves
		try {
			FileOutputStream outputStream = context.openFileOutput(FILE_NAME, Context.MODE_APPEND);
			outputStream.close();     // we just made sure the file existed in a tricky way
			FileInputStream inputStream = context.openFileInput(FILE_NAME);
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			String line;
			while ((line = reader.readLine()) != null) {
				runList.add(new Run(line));
				Log.i("run", runList.size() + ": " + line);
			}
			inputStream.close();
		} catch (Exception e) {
			Toast.makeText(context,"Can't read SimpleRunTracker_runList.csv", Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
	}
	
	public ArrayList<Run> getRunList() {
		return runList;
	}
	
	public void updateAndSaveRunDB(Context context) {  // might consider serialization instaed of CSV in the future
		int size = runList.size();
		int start = size > MAX ? runList.size()-MAX : 0;
		try {
			FileOutputStream outputStream = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
			for (int i = start; i < size; i++)
				outputStream.write((runList.get(i).toString()+"\n").getBytes());
			outputStream.close();
		} catch (Exception e) {
			Toast.makeText(context,"SimpleRunTracker_runList.csv is not reachable to append",Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
	}

	public void addNewRun(Context context, String line) {
		runList.add(new Run(line));
		// to append the file with new runs
		try {
			FileOutputStream outputStream = context.openFileOutput(FILE_NAME, Context.MODE_APPEND);
			outputStream.write((line+"\n").getBytes());
			outputStream.close();
		} catch (Exception e) {
			Toast.makeText(context,"SimpleRunTracker_runList.csv is not reachable to append",Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
	}

	// NOT USED - will implement Share Intent instead. Also considering Google Docs sync in the future
	public void saveToExternal(Context context) {
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
        		
			Toast.makeText(context, "Saved on SD Card",Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			Toast.makeText(context, "File write error", Toast.LENGTH_LONG)
					.show();
		}
	}
}