package com.Scan_Lv;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class scan extends Activity {
	private TextView textView;
	private EditText editText1; //tracking number, part 1
	private EditText editText2; //tracking number, part 2
	private EditText editText3; //tracking number, part 3
	private EditText editRange; //range
	private Spinner spinner;
	private float mSpinnerStartTime;
	String[] mTrackingNumArray;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Button scanButton = (Button) findViewById(R.id.scan);
        spinner = (Spinner) findViewById(R.id.caseTypeSpinner);
		textView = (TextView) findViewById(R.id.TextView01);
		editText1 = (EditText) findViewById(R.id.editText1);
		editText2 = (EditText) findViewById(R.id.editText2);
		editText3 = (EditText) findViewById(R.id.editText3);

		editRange = (EditText) findViewById(R.id.editRange);
		
		//Setup the spinner
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
		            this, R.array.caseType, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		this.mSpinnerStartTime = System.currentTimeMillis();
		spinner.setAdapter(adapter);
	    spinner.setOnItemSelectedListener(new MyOnItemSelectedListener());

		//editText1.setInputType(InputType.TYPE_CLASS_NUMBER);
        scanButton.setOnClickListener(new View.OnClickListener() {
        	// check the status of a LIN
            public void onClick(View view) {
            	scan(); 	
            }
        });
    }
    
    private void scan(){
    	String trackingNum = spinner.getSelectedItem().toString() + editText1.getText().toString()+editText2.getText().toString()+editText3.getText().toString();
    	int range = Integer.parseInt(editRange.getText().toString());
    	String base = trackingNum.substring(0, 3);
    	int number = Integer.parseInt(editText3.getText().toString());
    	mTrackingNumArray = new String[2*range+1];
    	// Generate all the tracking numbers to be scanned
    	for (int i = 0; i <= 2 * range; i++)
    		mTrackingNumArray[i] = base + editText1.getText().toString()+editText2.getText().toString()+Integer.toString(number - range + i);
    	
    	Intent trackingIntent = new Intent(this, ScanResult.class);
    	trackingIntent.putExtra("trackingNumArray", mTrackingNumArray);
    	startActivity(trackingIntent);
    	//mDialog = ProgressDialog.show(Scan.this, mTrackingNumArray[0], PROGRESS_TEXT);

        //ScanTest st = new ScanTest();
        //st.execute();
    	
    }
    
    public class MyOnItemSelectedListener implements OnItemSelectedListener {
    	int count = 0;
    
        public void onItemSelected(AdapterView<?> parent,
            View view, int pos, long id) {
        	String location = "";
        	String prefix = parent.getItemAtPosition(pos).toString();
        	Log.d("time", Float.toString(System.currentTimeMillis() - mSpinnerStartTime));
        	if(count >= 1){
        		if (prefix.equals("EAC")) location += "Vermont Service Center" ;
        		else if (prefix.equals("LIN")) location += "Nebraska Service Center";
        		else if (prefix.equals("SRC")) location += "Texas Service Center";
        		else if (prefix.equals("WAC")) location += "California Service Center";
        		location += " is working on your case." + "\n" + "Please input the tracking number and the range you want to scan.";    	
            	Toast.makeText(parent.getContext(), location, Toast.LENGTH_SHORT).show();
        	}else count++;
        	
        	
        }

        public void onNothingSelected(AdapterView parent) {
          // Do nothing.
        }
    }
    

}
