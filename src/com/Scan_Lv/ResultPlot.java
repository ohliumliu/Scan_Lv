package com.Scan_Lv;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.jjoe64.graphview.BarGraphView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphView.GraphViewSeries;
import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;

public class ResultPlot extends Activity {
	private static final String[] STEPS = {"Error", 
									"Acceptance", 
									"Initial Review", 
									"Request for Evidence",
									"Request for Evidence Response Reivew",
									"Testing and Interview",
									"Decision",
									"Post Decision Activity",
									"Oath Ceremony",
									"Card/Document Production"
		                            };
	private static final String[] VERTICAL_LABEL = {"Card/Document Production",
									"Oath Cermony",
									"Post Decision Activity",
									"Decision",
									"Testing and Interview",
									"Request for Evidence Response Review",
									"Request for Evidence",
									"Initial Review",
									"Acceptance",
									"Error"};
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.resultplot);

		String[] trackingNum = getIntent().getStringArrayExtra("TrackingNum");
		String[] trackingType = getIntent().getStringArrayExtra("TrackingType");
		String[] trackingResult = getIntent().getStringArrayExtra("TrackingResult");
		int length = trackingNum.length;
		double step = 0; // current status of the case
		GraphViewData[] resultData = new GraphViewData[length];
		int i = 0;
		for (i = 0; i < length; i++){
			
			//get current step of the process
			if (trackingResult[i].equalsIgnoreCase(STEPS[1]))
				step = 1;
			else if (trackingResult[i].equalsIgnoreCase(STEPS[2]))
				step = 2;
			else if (trackingResult[i].equalsIgnoreCase(STEPS[3]))
				step = 3;
			else if (trackingResult[i].equalsIgnoreCase(STEPS[4]))
				step = 4;
			else if (trackingResult[i].equalsIgnoreCase(STEPS[5]))
				step = 5;
			else if (trackingResult[i].equalsIgnoreCase(STEPS[6]))
				step = 6;
			else if (trackingResult[i].equalsIgnoreCase(STEPS[7]))
				step = 7;
			else if (trackingResult[i].equalsIgnoreCase(STEPS[8]))
				step = 8;
			else if (trackingResult[i].equalsIgnoreCase(STEPS[9]))
				step = 9;
			else step = 0;
			
			//step = (step - 1) * 1.5; // this is to scale it for correct plotting
			resultData[i] = new GraphViewData(i+1, step);
		}
		
		// graph with dynamically generated horizontal and vertical labels
		GraphView graphView;
		graphView = new BarGraphView(
					this // context
					, "Scan Result" // heading
			);
		
		graphView.addSeries(new GraphViewSeries(resultData)); // data
		
		//graphView.setVerticalLabels(VERTICAL_LABEL);
		String[] horizontalLabel = new String[length];
		for (i = 0; i < length; i++){
			horizontalLabel[i] = trackingNum[i].substring(11);
		}
		//graphView.setHorizontalLabels(horizontalLabel);
		LinearLayout layout = (LinearLayout) findViewById(R.id.resultplot);
		
		layout.addView(graphView);

		// custom static labels
		//graphView.setHorizontalLabels(new String[] {"2 days ago", "yesterday", "today", "tomorrow"});
		//graphView.setVerticalLabels(new String[] {"high", "middle", "low"});
		//graphView.addSeries(exampleSeries); // data

		
	}
}
