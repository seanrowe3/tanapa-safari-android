package edu.clemson.tanapasafari;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import edu.clemson.tanapasafari.db.TanapaDbHelper;
import edu.clemson.tanapasafari.model.SafariPointOfInterest;
import edu.clemson.tanapasafari.model.SafariWayPoint;
import edu.clemson.tanapasafari.service.GPSTracker;
import edu.clemson.tanapasafari.service.GPSTrackerSingleton;

public class GuideActivity extends Activity {

	
	private List<SafariPointOfInterest> safariPointsOfInterest;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);

        Intent intent = getIntent();
        int safariId = intent.getIntExtra("safariId", -1);
 
        //set your starting location
        GPSTracker gpsTracker = GPSTrackerSingleton.getInstance(this);
        if(!gpsTracker.canGetLocation()){
        	gpsTracker.showSettingsAlert();
        }
        GoogleMap map = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
        if(gpsTracker.canGetLocation()){
        	Location location = gpsTracker.getLocation();
        	
            map.setMyLocationEnabled(true);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude()), 15));
            map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        	
        }
        
        //query internal database for waypoints
        List<SafariWayPoint> wayPoints = new ArrayList<SafariWayPoint>();
        wayPoints = TanapaDbHelper.getInstance(getBaseContext()).getWayPoints(safariId);
        Log.d("WayPoints", wayPoints.toString());
        
        //Set polyline options
        PolylineOptions line = new PolylineOptions();
        line.width(5);
        line.color(Color.GREEN);
        
        //add the waypoints
        for(SafariWayPoint point:wayPoints){
        	line.add(new LatLng(point.getLatitude(), point.getLongitude()));
        }
        
        //add the line
        map.addPolyline(line);
        
        
        safariPointsOfInterest = TanapaDbHelper.getInstance(getBaseContext()).getSafariPointsOfInterest(safariId);
        gpsTracker.registerLocationListener(new LocationListener() {

			@Override
			public void onLocationChanged(Location location) {
				checkPointsOfInterest(location);
				
			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onProviderEnabled(String provider) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onProviderDisabled(String provider) {
				// TODO Auto-generated method stub
				
			}
    	});
        
	}	
	
	private void checkPointsOfInterest(Location l) {
		for ( SafariPointOfInterest poi : safariPointsOfInterest) {
			if (l.distanceTo(poi.getLocation()) < poi.getRadius()) {
				if (!poi.isInGeofence()) {
					poi.setInGeofence(true);
					TextView title, text;
					
					LayoutInflater li = LayoutInflater.from(this);
					AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
					View poiView = li.inflate(R.layout.activity_poi, null);
					
					alertDialogBuilder.setView(poiView);
					title = (TextView) poiView.findViewById(R.id.poiTitle);
					text = (TextView) poiView.findViewById(R.id.poiText);
					title.setText(poi.getName());
					text.setText(poi.getDescription());
					
					// Create Dialog box for user input 
					alertDialogBuilder
					.setCancelable(false)
					.setPositiveButton("OK",
					  new DialogInterface.OnClickListener() {
					    @Override
						public void onClick(DialogInterface dialog,int id) {
						dialog.dismiss();
					    }
					  })
					.setNegativeButton("Cancel",
					  new DialogInterface.OnClickListener() {
					    @Override
						public void onClick(DialogInterface dialog,int id) {
						dialog.cancel();
					    }
					  });

				AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.show();

				}
			} else {
				if (poi.isInGeofence()) {
					showToastText("Exited POI Geofence: " + poi.getName());
				}
				poi.setInGeofence(false);
			}
		}
	}
	
	
	public void captureImage(View view){
		captureMedia(1);
	}
	
	public void captureVideo(View view){
		captureMedia(2);
	}
	
	public void captureMedia(int x){
		Intent captureMediaIntent;
		if(x == 1){
			captureMediaIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		}
		else{
			captureMediaIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
			captureMediaIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 15);
		}
	    if (captureMediaIntent.resolveActivity(getPackageManager()) != null) {
	        startActivityForResult(captureMediaIntent, 1);
	    }
	}
	
	public void loadReportActivity(View view) {
		Intent reportIntent = new Intent(this, ReportActivity.class);
		startActivity(reportIntent);
	}
	
	private void showToastText(String msg) {
		Toast.makeText(getApplicationContext(), msg,
				Toast.LENGTH_LONG).show();
	}
	
}
