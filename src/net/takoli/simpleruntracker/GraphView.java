package net.takoli.simpleruntracker;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class GraphView extends View {
	
	private ArrayList<Run> runList;
	private int plotSize;
	private long[] dists, speeds;
	private long distMin, distMax, speedMin, speedMax;
	private boolean inMiles;
	private String dUnit, sUnit;
	private final double KM_TO_M = 1.60934;
	
	private final int MAX_PLOTS = 15;
	private float[] dX, dY, sX, sY;
	private int width, height;
	private int sPad, tPad, bPad;
	private Paint coordPaint, dPaint, sPaint, labelUnitPaint, dTextPaint, sTextPaint;
	
	// set up the view
	public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // initialize fields
        dists = new long[MAX_PLOTS];
        speeds = new long[MAX_PLOTS];
        dX = new float[MAX_PLOTS];
        dY = new float[MAX_PLOTS];
        sX = new float[MAX_PLOTS];
        sY = new float[MAX_PLOTS];
        inMiles = true;
        dUnit = "m";
        dUnit = "mph";
        coordPaint = new Paint();
        dPaint = new Paint();
        sPaint = new Paint();
        labelUnitPaint = new Paint();
        dTextPaint = new Paint();
        sTextPaint = new Paint();
        coordPaint.setStyle(Style.STROKE);
        coordPaint.setColor(Color.BLACK);
        coordPaint.setStrokeWidth(2);
        dPaint.setStyle(Style.STROKE);
        dPaint.setStrokeWidth(4);
        dPaint.setColor(0xFFFFA4A4);  //red for distance
        dPaint.setAntiAlias(true);
        dPaint.setShadowLayer(5, 3, 3, 0x88000000);
        sPaint.setStyle(Style.STROKE);
        sPaint.setStrokeWidth(4);
        sPaint.setColor(0xFFCCE5FF);   //blue for speed
        sPaint.setAntiAlias(true);
        sPaint.setShadowLayer(5, 3, 3, 0x88000000);
        dTextPaint.setColor(0xFFCC8383);
        sTextPaint.setColor(0xFF93A5B8);}
	
	public void setRunList(ArrayList<Run> runList, String unit) {
		this.runList = runList; 
		inMiles = (unit.compareTo("m") == 0);
		if (inMiles)	{ dUnit = "m"; sUnit = "mph";}
		else			{ dUnit = "km"; sUnit = "km/h"; }
		updateData();
	}
	
	public void updateData() {
		int fullSize = runList.size();
		plotSize = fullSize > MAX_PLOTS ? MAX_PLOTS : fullSize;
		Log.i("run", "updateData "+plotSize + " / " + fullSize);
		if (plotSize == 0) {
			Log.i("run", "nothing to chart");
			return; }
		int j = 0;
		for (int i = fullSize - plotSize; i < fullSize; i++) {
			dists[j] = runList.get(i).getDistDecInM();
			Log.i("run", i + ": " + runList.get(i).getDistDecInM());
			speeds[j] = runList.get(i).getSpeedDecInMPH(dists[j]);
			j++;
		}
		// in case we want to see the graph in KM:
		if (!inMiles) for (int i = 0; i < plotSize; i++) {
			dists[i] = Math.round(dists[i] * KM_TO_M);
			speeds[i] = Math.round(speeds[i] * KM_TO_M);
		}
		// check for min and max values:
		distMax = distMin = dists[0];
		speedMax = speedMin = speeds[0];
		for (int i = 0; i < plotSize; i++) {
			if (dists[i] < distMin)	distMin = dists[i];
			if (dists[i] > distMax)	distMax = dists[i];
			if (speeds[i] < speedMin)	speedMin = speeds[i];
			if (speeds[i] > speedMax)	speedMax = speeds[i];
		}
	}
	
	@Override
    protected void onDraw(Canvas canvas) {
		sPad = this.getPaddingLeft();
		bPad = this.getPaddingBottom();
		tPad = this.getPaddingTop();
		width = this.getWidth() - sPad * 2;
		height = this.getHeight() - tPad - bPad;
		drawCoordSystem(canvas, distMin, distMax, speedMin, speedMax);
	    drawChart(canvas);
	}

	private void drawCoordSystem(Canvas canvas, long distMin, long distMax,
			long speedMin, long speedMax) {
		// left, bottom, right lines
		canvas.drawLine((float)sPad, (float)tPad, (float)sPad, (float)(tPad+height), coordPaint);
		canvas.drawLine((float)sPad, (float)(tPad+height), (float)(sPad+width), (float)(tPad+height), coordPaint);
		canvas.drawLine((float)(sPad+width), (float)(tPad+height), (float)(sPad+width), (float)tPad, coordPaint);
		// miles or km and mph or km/h
		labelUnitPaint.setTextSize(sPad * 0.5f);
		canvas.drawText(dUnit, sPad * 1.2f, 			tPad * 2.5f, 	labelUnitPaint);
		canvas.drawText(sUnit, width - sPad * 0.25f, 	tPad * 2.5f, 	labelUnitPaint);
		// distance indicators
		dTextPaint.setTextSize(sPad * 0.3f);
		canvas.drawText(form(distMax), 				sPad * 0.2f, tPad + height*0.1f, dTextPaint);
		canvas.drawText(form((distMin+distMax)/2), 	sPad * 0.2f, tPad + height*0.5f, dTextPaint);
		canvas.drawText(form(distMin), 				sPad * 0.2f, tPad + height*0.9f, dTextPaint);
		// speed indicators
		sTextPaint.setTextSize(sPad * 0.3f);
		canvas.drawText(form(speedMax), 			width + sPad * 1.15f, tPad + height*0.2f, sTextPaint);
		canvas.drawText(form((speedMin+speedMax)/2),width + sPad * 1.15f, tPad + height*0.5f, sTextPaint);
		canvas.drawText(form(speedMin), 			width + sPad * 1.15f, tPad + height*0.8f, sTextPaint);
	}
	
	private void drawChart(Canvas canvas) {
		if (plotSize == 0)
			return;
		setPlotCoordinates();
		Path dPath = new Path();
		Path sPath = new Path();
		dPath.moveTo(dX[0], dY[0]);
		sPath.moveTo(sX[0], sY[0]);
        for (int i = 1; i < plotSize; i++) {
        	dPath.lineTo(dX[i], dY[i]); }
        for (int i = 1; i < plotSize; i++) {
        	sPath.lineTo(sX[i], sY[i]); }
        canvas.drawPath(sPath, sPaint);
        canvas.drawPath(dPath, dPaint);
	}
	
	private void setPlotCoordinates() {
		float wUnit = width / (MAX_PLOTS - 1);  //divide horizontally
		float dHeight = height * 0.8f;			//distance range will be 80% of chart
		float sHeight = height * 0.5f;			//speed range will be 50%
		float dTop = tPad + height * 0.1f;
		float sTop = tPad + height * 0.25f;
		float dRange = distMax - distMin;
		float sRange = speedMax - speedMin;
		for (int i = 0; i < plotSize; i++) {
			dX[i] = sPad + wUnit * i + 2;
			dY[i] = dTop + dHeight * (distMax - dists[i]) / dRange; 
			sX[i] = sPad + wUnit * i + 2;
			sY[i] = sTop + sHeight * (speedMax - speeds[i]) / sRange;
		}
	}
	
	private String form(long XXxx) {
		String formatted = "" + XXxx / 100;
		formatted += "." + (XXxx % 100) / 10;
		return formatted;
	}
}
