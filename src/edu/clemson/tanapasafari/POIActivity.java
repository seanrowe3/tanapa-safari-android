package edu.clemson.tanapasafari;

import java.util.ArrayList;
import java.util.List;

import edu.clemson.tanapasafari.db.TanapaDbHelper;
import edu.clemson.tanapasafari.model.SafariPointOfInterest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class POIActivity extends Activity{

	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_poi);
		
		Intent intent = getIntent();
		int safariId = intent.getIntExtra("safariId", -1);
		int poiId = intent.getIntExtra("poiId", -1);
		List<SafariPointOfInterest> pois = new ArrayList<SafariPointOfInterest>();
		SafariPointOfInterest POI = new SafariPointOfInterest();
		
		pois = TanapaDbHelper.getInstance(getBaseContext()).getSafariPointsOfInterest(safariId);
		
		for ( SafariPointOfInterest poi : pois) {
			if (poi.getId() == poiId)  POI = poi;
		}
		
		TextView title = (TextView) findViewById(R.id.poiTitle);
		TextView info = (TextView) findViewById(R.id.poiText);
		
		
		title.setText(POI.getName());
		info.setText(POI.getDescription());
			
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.report, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
