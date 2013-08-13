package com.Scan_Lv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.achartengine.ChartFactory;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer.Orientation;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


import android.R.color;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ScanResult extends ListActivity {
	private static final String[] STEPS = {"Error", 
		"Acceptance", 
		"Initial Review", 
		"Request for Evidence",
		"Request for Evidence Response Review",
		"Testing and Interview",
		"Decision",
		"Post Decision Activity",
		"Oath Ceremony",
		"Card/ Document Production"
        };
	private static final int EMAIL_ID = 0;
	private static final int PLOT_ID = EMAIL_ID + 1;
	private static final int THREAD_BEFORE = -1; //thread not started yet
	private static final int THREAD_RUNNING = 0; //thread running
	private static final int THREAD_DONE = 1; //thread done
	private static final int THREAD_MAX = 10; //maximum number of threads at the same time
	String[] mTrackingNumArray; // array to store all the tracking numbers to be scanned. 
	ArrayList<Map<String, String>> mTrackingResultList; //array to store the short tracking number/scan result pairs.
	String[] mTrackingDetailsArray; //array to store the detailed result
	String[] mTrackingResultArray; //array to store the short result
	String[] mTrackingTypeArray; //array to store the form type
	TextView mAllThreadStatusText;
	List<Integer> mThreadStatusList; //save tracking thread status
	int mFinishedThread = 0;
	ProgressBar mAllThreadStatus;
	private final String PROGRESS_TEXT = "Checking egov.uscis.gov ...";
	private final String HELP_TEXT = "Tap on each item to get detailed case status.\nPush Menu for further action.\nPush Home to leave the job in the background";
	//ProgressDialog[] mDialog;
	SimpleAdapter mResultAdapter;
	@Override
	public void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);
	  setContentView(R.layout.scanresultall);
	  
	  mAllThreadStatus = (ProgressBar) findViewById(R.id.allthreadstatus);
	  mAllThreadStatusText = (TextView) findViewById(R.id.alltheadstatustext);
	  
	  //Get extra from intent
	  mTrackingNumArray = getIntent().getStringArrayExtra("trackingNumArray");
	  int length = mTrackingNumArray.length;
	  int index = 0;
	  String[] from = {"trackingnum", "result"};
	  int[] to = {R.id.trackingnum, R.id.result};
	  
	  //build initial result list, result is null
	  mTrackingResultList = new ArrayList<Map<String, String>>(length);
	  mTrackingResultArray = new String[length];
	  mTrackingDetailsArray = new String[length];
	  mTrackingTypeArray = new String[length];
	  
	  mThreadStatusList = new ArrayList<Integer>(length);
	  for (index = 0; index<length; index++){
		  mThreadStatusList.add(THREAD_BEFORE);
	  }
	  
	  for (index = 0; index<length; index++){
		  mTrackingResultList.add(putData(mTrackingNumArray[index], null));
	  }
	  
	  mResultAdapter = new SimpleAdapter(this, mTrackingResultList, R.layout.scanresult, from, to);
	  setListAdapter(mResultAdapter);
	  
	  Toast.makeText(getBaseContext(), this.HELP_TEXT , Toast.LENGTH_LONG).show();
	  
	  mAllThreadStatus.setMax(length);
	  mAllThreadStatus.setProgress(0);
	  
	  //mDialog = new ProgressDialog[this.mTrackingNumArray.length];

	  
  	  for (index = 0; index < ((length>THREAD_MAX)?THREAD_MAX:length); index ++){
  	    //mDialog[index] = ProgressDialog.show(ScanResult.this, mTrackingNumArray[index], PROGRESS_TEXT);
  	  	ScanTest st = new ScanTest(index);
  	    mThreadStatusList.set(index, THREAD_RUNNING);
  	    st.execute();
  	  }
	  
     
	}
	
	
	private class ScanTest extends AsyncTask<Void, Void, String> {
    	
    	int mIndex;
    	
    	public ScanTest(int index){
    		super();
    		this.mIndex = index;
    	}
    	
		protected String doInBackground(Void... input) {
			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			schemeRegistry.register(new Scheme("https", new EasySSLSocketFactory(), 443));
			 
			HttpParams params = new BasicHttpParams();
			params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 30);
			params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean(30));
			params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			 
			ClientConnectionManager cm = new SingleClientConnManager(params, schemeRegistry);
			DefaultHttpClient client = new DefaultHttpClient(cm, params);
			
			
			String responseText = "";
			StringBuilder sb=new StringBuilder("https://egov.uscis.gov/cris/Dashboard/CaseStatus.do");
			HttpPost post = new HttpPost(sb.toString());
			List<BasicNameValuePair> nameValuepairs = new ArrayList<BasicNameValuePair>();
			nameValuepairs.add(new BasicNameValuePair("appReceiptNum", mTrackingNumArray[mIndex]));
			
			try {
				post.setEntity(new UrlEncodedFormEntity(nameValuepairs));
				HttpResponse response = client.execute(post);
				HttpEntity entity = response.getEntity();

				
				//Get HTML text
				InputStream content = entity.getContent();
				BufferedReader buffer = new BufferedReader(
						new InputStreamReader(content));	
				StringBuilder s = new StringBuilder();
				String line = null;
				while ((line = buffer.readLine()) != null) {
					s.append(line);
				}
				responseText = s.toString();
				
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//Parsing using jsoup
			Document doc = Jsoup.parse(responseText);			
			Element caseStatus;
            Element ec = doc.getElementsByClass("errorContainer").first();
            if (ec == null) {
            		caseStatus = doc.getElementById("caseStatus");
            		String caseType = caseStatus.child(0).text();
            		int formNameIdx = caseType.indexOf("Form ");
            		caseType = caseType.substring(formNameIdx+5, formNameIdx+9);
            		
            		String caseResult = caseStatus.child(1).child(1).text().replace("Your Case Status: ", "");
                    responseText = caseType + ":\n" + caseResult;
                    mTrackingDetailsArray[mIndex] = doc.getElementsByClass("caseStatus").first().text();
                    mTrackingResultArray[mIndex] = caseResult;
                    mTrackingTypeArray[mIndex] = caseType;
            }
            else {
            	responseText = ec.text();
            	mTrackingDetailsArray[mIndex] = responseText;
            	mTrackingResultArray[mIndex] = "Case Not Found";
            	mTrackingTypeArray[mIndex] = "NA";
            }
		    mTrackingResultList.set(mIndex, putData(mTrackingNumArray[mIndex], responseText));
		    mThreadStatusList.set(mIndex, THREAD_DONE);
            return responseText;
		}

		protected void onPostExecute(String result) {
			mFinishedThread++;
			mAllThreadStatusText.setText(Integer.toString(mFinishedThread) + " of " + Integer.toString(mTrackingNumArray.length));
			mAllThreadStatus.setProgress(mFinishedThread);
		    mResultAdapter.notifyDataSetChanged();
		    
		    //fire additional thread if needed
		    int nextIdx = mThreadStatusList.indexOf(THREAD_BEFORE);
		    if (nextIdx >=0 ){
		    	ScanTest nextST = new ScanTest(nextIdx);
		    	mThreadStatusList.set(nextIdx, THREAD_RUNNING);
		    	nextST.execute();
		    	Log.d("thread", Integer.toString(nextIdx));
		    }
		    
		}

		

		
	}

	public Map<String, String> putData(String trackingnum, String result) {
		// TODO Auto-generated method stub
    	HashMap<String, String> hmap = new HashMap<String, String>();
    	hmap.put("trackingnum", trackingnum);
    	hmap.put("result", result);
		return hmap;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		menu.add(0, EMAIL_ID, 0, "E-Mail Result");
		menu.add(0, PLOT_ID, 0, "Plot Result");
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item){
		switch(item.getItemId()){
		case EMAIL_ID:{
			emailResult();
			return true;
		}
		case PLOT_ID:{
			plotResult();
			return true;
		}
		}
		return super.onMenuItemSelected(featureId, item);
		
	}

	

	private void emailResult() {
		// get detailed result
		StringBuilder fullReport = new StringBuilder();
		int length = mTrackingNumArray.length;

		for (int i = 0; i<length; i++){
			fullReport.append(mTrackingNumArray[i] + "; " + mTrackingTypeArray[i] + "; " + mTrackingResultArray[i] +
					"; " + mTrackingDetailsArray[i] + "\n");
		}
		
		// email
		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		emailIntent.setType("plain/text");
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, "ScanLv Result from " + mTrackingNumArray[0] + " to " + mTrackingNumArray[length-1]);
		emailIntent.putExtra(Intent.EXTRA_TEXT, fullReport.toString());

		startActivity(Intent.createChooser(emailIntent, "Send result using"));
	}
	
	private void plotResult() {
		// TODO Auto-generated method stub
		/* The following is for GraphView
		Intent intent = new Intent(ScanResult.this, ResultPlot.class);
		intent.putExtra("TrackingNum", mTrackingNumArray);
		intent.putExtra("TrackingType", mTrackingTypeArray);
		intent.putExtra("TrackingResult", mTrackingResultArray);
		startActivity(intent);
		*/
		
		/* The following is for achartengine
		 */
		
		 XYMultipleSeriesRenderer renderer = getResultPlotRenderer();
	     setChartSettings(renderer);
	     Intent intent = ChartFactory.getBarChartIntent(this, getResultPlotDataset(), renderer, Type.DEFAULT);
	     startActivity(intent);
		
	}
	
	private XYMultipleSeriesDataset getResultPlotDataset() {
		 XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		 final int length = mTrackingNumArray.length;
		 
		 int step;
		 CategorySeries series = new CategorySeries("Scan Result");
		 series.clear();
		 for (int i = 0; i < length; i++) {
			 if (mTrackingResultArray[i].equalsIgnoreCase(STEPS[1]))
					step = 1;
				else if (mTrackingResultArray[i].equalsIgnoreCase(STEPS[2]))
					step = 2;
				else if (mTrackingResultArray[i].equalsIgnoreCase(STEPS[3]))
					step = 3;
				else if (mTrackingResultArray[i].equalsIgnoreCase(STEPS[4]))
					step = 4;
				else if (mTrackingResultArray[i].equalsIgnoreCase(STEPS[5]))
					step = 5;
				else if (mTrackingResultArray[i].equalsIgnoreCase(STEPS[6]))
					step = 6;
				else if (mTrackingResultArray[i].equalsIgnoreCase(STEPS[7]))
					step = 7;
				else if (mTrackingResultArray[i].equalsIgnoreCase(STEPS[8]))
					step = 8;
				else if (mTrackingResultArray[i].equalsIgnoreCase(STEPS[9]))
					step = 9;
				else step = 0;
			 
		        series.add(step);
		 }
		 dataset.addSeries(series.toXYSeries());
		
		    return dataset;
	}

	private void setChartSettings(XYMultipleSeriesRenderer renderer) {
		int length = mTrackingNumArray.length;
		 


		 //x axis property
		 renderer.setXTitle("Case Number");
		 renderer.setXAxisMin(0.5);
		 renderer.setXAxisMax(((length>20)?20:length) + 0.5);
		//set xlable
		 int i = 0;
		 for (i = 0; i < length; i++){
			 renderer.addXTextLabel(i+1, mTrackingNumArray[i].substring(10));
		 }
		 renderer.setXLabelsAlign(android.graphics.Paint.Align.LEFT);
		 renderer.setXLabelsAngle(-90);
		 //renderer.setMargins(new int[]{5, 3, 5, 3});
		 //renderer.setMarginsColor(Color.YELLOW);
		 
		 //y axis property
		 renderer.setYTitle("Status");
		 renderer.setYAxisMin(-0.5);
		 renderer.setYAxisMax(STEPS.length-0.5);
		 //set ylable
		 for (i = 0; i < STEPS.length; i ++){
			 renderer.addYTextLabel(i, StringUtils.center(STEPS[i], STEPS[4].length()+10));
		 }
		 renderer.setYLabelsAngle(0);
		 renderer.setYLabelsAlign(android.graphics.Paint.Align.LEFT);
		 renderer.setYLabelsColor(0, Color.GREEN);
		 
		 
		//chart property 
		 renderer.setChartTitle("Scan Result");
		 renderer.setShowAxes(false);
		 renderer.setOrientation(Orientation.HORIZONTAL);
		 renderer.setBarSpacing(0.2);
		 renderer.setShowLegend(false);
		 renderer.setPanEnabled(true, false);	
		 double[] panLimits = {0.5, length + 0.5, -.5, STEPS.length-0.5};
		 renderer.setPanLimits(panLimits);
		 renderer.setShowGrid(false);
		
		}

	private XYMultipleSeriesRenderer getResultPlotRenderer() {
		// TODO Auto-generated method stub
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
	    renderer.setAxisTitleTextSize(10);
	    renderer.setChartTitleTextSize(15);
	    renderer.setLabelsTextSize(10);
	    SimpleSeriesRenderer r = new SimpleSeriesRenderer();
	    r.setColor(Color.BLUE);
	    renderer.addSeriesRenderer(r);
	    r = new SimpleSeriesRenderer();
	    
	    return renderer;
	}

	public void onListItemClick(ListView l, View v, int position, long id){
		super.onListItemClick(l, v, position, id);
		StringBuilder resultFlash = new StringBuilder();
		resultFlash.append(mTrackingNumArray[position]);
		resultFlash.append((mTrackingTypeArray[position].equals(null))?"":" (" + mTrackingTypeArray[position] + "):\n");
		resultFlash.append((mTrackingDetailsArray[position].equals(null))?"":mTrackingDetailsArray[position]);
		Toast.makeText(getBaseContext(), resultFlash, Toast.LENGTH_LONG).show();
	}
}
