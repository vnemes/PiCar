// Copyright © 2016-2017 Shawn Baker using the MIT License.
package vendetta.blecar.camera;

import android.app.Activity;
import android.os.Bundle;
import android.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import vendetta.blecar.R;
import vendetta.blecar.camera.dependencies.Camera;

public class VideoActivity extends Activity
{
	// public constants
	public final static String CAMERA = "camera";

	// instance variables
	private Camera camera;
	private FrameLayout frameLayout;
	private VideoFragment videoFragment;

	//******************************************************************************
	// onCreate
	//******************************************************************************
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// configure the activity
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video);

		// get the camera object
		Bundle data = getIntent().getExtras();
		camera = data.getParcelable(CAMERA);
		Log.d(getClass().getSimpleName(),"camera: " + camera.toString());

		// get the frame layout, handle system visibility changes
		frameLayout = (FrameLayout) findViewById(R.id.video);

		// set full screen layout
		int visibility = frameLayout.getSystemUiVisibility();
		visibility |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
		frameLayout.setSystemUiVisibility(visibility);

		// create the video fragment
		videoFragment = videoFragment.newInstance(camera, true);
		FragmentTransaction fragTran = getFragmentManager().beginTransaction();
		fragTran.add(R.id.video, videoFragment);
		fragTran.commit();
	}


	//******************************************************************************
	// onBackPressed
	//******************************************************************************
	@Override
	public void onBackPressed()
	{
		videoFragment.stop();
		super.onBackPressed();
	}
}
