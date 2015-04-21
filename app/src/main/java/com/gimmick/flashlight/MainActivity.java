package com.gimmick.flashlight;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

public class MainActivity extends Activity implements OnItemClickListener,
		OnClickListener {
	private static final String TAG = "MainActivity";
	private Activity currentActivity;
	public static boolean check = false;
	private AudioManager audioManager = null;
	private BluetoothAdapter bt;
	private WifiManager wifi;
	float BackLightValue = 0.5f; // dummy default value
	PreferenceManager pm;
	private static boolean isLightOn = false;
	private static Camera camera;

	private LogWriter logWriter;
	private ImageButton imageButton;
	CarouselGallery mCarouselGallery;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.home);
		currentActivity = this;
		logWriter = new LogWriter();
		imageButton = (ImageButton) findViewById(R.id.FlashLight);
		imageButton.setOnClickListener(this);
		pm = new PreferenceManager(currentActivity);
		mCarouselGallery = (CarouselGallery) findViewById(R.id.main_carousel);
		mCarouselGallery.setAdapter(new ImageAdapter(this));
		mCarouselGallery.setSelection(Integer.MAX_VALUE / 2);
		mCarouselGallery.setOnItemClickListener(this);
		// Suggests an audio stream whose volume should be changed by the
		// hardware volume control
		this.setVolumeControlStream(AudioManager.STREAM_RING);
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		// BluetoothAdapter
		bt = BluetoothAdapter.getDefaultAdapter();

		// BluetoothAdapter
		wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		System.out.println(pm.getTorchStatus());
		if(pm.getTorchStatus().equalsIgnoreCase("yes")){
			imageButton.setImageDrawable(currentActivity.getResources()
					.getDrawable(R.drawable.led_on));
			isLightOn = true;
			((ImageButton) findViewById(R.id.FlashLight)).setBackgroundColor(getResources().getColor(R.color.backgroud_color));
			
			((LinearLayout) findViewById(R.id.HomeLayout)).setBackgroundColor(getResources().getColor(R.color.backgroud_color));
			
		}

	}

	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		String tag = (String) arg1.getTag();
		vibrateOnButtonClick(currentActivity);
		if (tag.equalsIgnoreCase("Bluetooth")) {
			setBluetoothActions();
		} else if (tag.equalsIgnoreCase("Brightness")) {
			setBrightnessAction();
		} else if (tag.equalsIgnoreCase("Ringing Volume")) {
			setRingerVolumeActions();

		} else if (tag.equalsIgnoreCase("Wifi")) {
			setWifiActions();
		} else if (tag.equalsIgnoreCase("Settings")) {
			Intent intent = new Intent(
					android.provider.Settings.ACTION_SETTINGS);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			currentActivity.startActivity(intent);
		}
	}

	private void setFlashlightActions() {
		try {
			if (isLightOn) {
				if (camera != null) {
					camera.stopPreview();
					camera.release();
					camera = null;
				}
				isLightOn = false;
				imageButton.setImageDrawable(currentActivity.getResources()
						.getDrawable(R.drawable.led_off));
				pm.saveTorchStatus("No");
				((LinearLayout) findViewById(R.id.HomeLayout)).setBackgroundColor(getResources().getColor(R.color.backgroud_color_off));
				((ImageButton) findViewById(R.id.FlashLight)).setBackgroundColor(getResources().getColor(R.color.backgroud_color_off));
					
			} else {
				// Open the default i.e. the first rear facing camera.
				camera = Camera.open();

				if (camera == null) {
					Toast.makeText(currentActivity,
							"Sorry! you don't have camera.", Toast.LENGTH_SHORT)
							.show();
				} else {
					// Set the torch flash mode
					Parameters param = camera.getParameters();
					param.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
					try {
						camera.setParameters(param);
						camera.startPreview();
						isLightOn = true;
						imageButton.setImageDrawable(currentActivity
								.getResources().getDrawable(R.drawable.led_on));
						pm.saveTorchStatus("Yes");
						((LinearLayout) findViewById(R.id.HomeLayout)).setBackgroundColor(getResources().getColor(R.color.backgroud_color));
						((ImageButton) findViewById(R.id.FlashLight)).setBackgroundColor(getResources().getColor(R.color.backgroud_color));
						
					} catch (Exception e) {
						Toast.makeText(currentActivity,
								"Sorry! Your flash is not supported.",
								Toast.LENGTH_SHORT).show();
					}
				}
			}
		} catch (Exception e) {
			Toast.makeText(currentActivity,
					"Sorry! Your flash is not supported.", Toast.LENGTH_SHORT)
					.show();
			logWriter.appendLog(TAG + " setFlashlightActions Error :"
					+ e.getMessage());
		}

	}

	private void setBluetoothActions() {
		try {
			String enable = "";
			if (bt.isEnabled()) {
				bt.disable();
				enable = "Off";
			} else {
				bt.enable();
				enable = "On";

			}
			Toast.makeText(currentActivity, "Please wait Bluetooth is turning "+enable, Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			logWriter.appendLog(TAG + " setBluetoothActions Error :"
					+ e.getMessage());
		}

	}

	private void setBrightnessAction() {
		try {
			showBrightNessSeekBar();
		} catch (Exception e) {
			logWriter.appendLog(TAG + " showBrightNessSeekBar Error :"
					+ e.getMessage());
		}
	}

	private void setRingerVolumeActions() {
		try {
			showVolumSeekBar();
		} catch (Exception e) {
			logWriter.appendLog(TAG + " showVolumSeekBar Error :"
					+ e.getMessage());
		}

	}

	private void setWifiActions() {
		try {
			String enable = "";
			if (wifi.isWifiEnabled()) {
				wifi.setWifiEnabled(false);
				enable = "Off";
			} else {
				wifi.setWifiEnabled(true);
				enable = "On";
			}
			Toast.makeText(currentActivity, "Please wait Wifi is turning "+enable, Toast.LENGTH_SHORT).show();
			
		} catch (Exception e) {
			logWriter.appendLog(TAG + " setWifiActions Error :"
					+ e.getMessage());
		}

	}

	private void showVolumSeekBar() {
		AlertDialog.Builder builder;
		AlertDialog alertDialog;
		SeekBar ringerVlmSeekBar = null;

		LayoutInflater inflater = (LayoutInflater) currentActivity
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.custom_dialog,
				(ViewGroup) findViewById(R.id.layout_root));
		try {
			ringerVlmSeekBar = (SeekBar) layout.findViewById(R.id.seekBar1);
			// for Phone ringer volume
			ringerVlmSeekBar.setMax(audioManager
					.getStreamMaxVolume(AudioManager.STREAM_RING));
			// Set the progress with current Ringer Volume
			ringerVlmSeekBar.setProgress(audioManager
					.getStreamVolume(AudioManager.STREAM_RING));

			ringerVlmSeekBar
					.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
						public void onStopTrackingTouch(SeekBar arg0) {
						}

						public void onStartTrackingTouch(SeekBar arg0) {
						}

						public void onProgressChanged(SeekBar arg0,
								int progress, boolean arg2) {
							audioManager.setStreamVolume(
									AudioManager.STREAM_RING, progress, 0);
						}
					});
		} catch (Exception e) {
			e.printStackTrace();
		}
		builder = new AlertDialog.Builder(MainActivity.this);
		builder.setView(layout);

		alertDialog = builder.create();
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

			}
		});

		alertDialog.show();
	}

	private void showBrightNessSeekBar() {
		AlertDialog.Builder builder;
		AlertDialog alertDialog;
		SeekBar ringerVlmSeekBar = null;

		LayoutInflater inflater = (LayoutInflater) currentActivity
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.custom_dialog,
				(ViewGroup) findViewById(R.id.layout_root));
		try {
			ringerVlmSeekBar = (SeekBar) layout.findViewById(R.id.seekBar1);
			ringerVlmSeekBar
					.setProgress(Math.round(100 * Settings.System.getInt(
							getContentResolver(), "screen_brightness") / 255.0F - 5.0F));
			ringerVlmSeekBar.setMax(95);
			ringerVlmSeekBar
					.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
						public void onStopTrackingTouch(SeekBar arg0) {
						}

						public void onStartTrackingTouch(SeekBar arg0) {
						}

						public void onProgressChanged(SeekBar arg0,
								int progress, boolean arg2) {
							if (progress > 15) {
								BackLightValue = (float) progress / 100;
								WindowManager.LayoutParams layoutParams = getWindow()
										.getAttributes();
								layoutParams.screenBrightness = BackLightValue + 5 / 100.0F;
								getWindow().setAttributes(layoutParams);
								Settings.System.putInt(
										currentActivity.getContentResolver(),
										"screen_brightness",
										Math.round(255 * (BackLightValue + 5) / 100.0F));
							}
						}
					});
		} catch (Exception e) {
			e.printStackTrace();
		}
		builder = new AlertDialog.Builder(MainActivity.this);
		builder.setView(layout);

		alertDialog = builder.create();
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				int SysBackLightValue = (int) (BackLightValue * 255);

				android.provider.Settings.System.putInt(getContentResolver(),
						android.provider.Settings.System.SCREEN_BRIGHTNESS,
						SysBackLightValue);

			}
		});
		alertDialog.setButton2("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

			}
		});

		alertDialog.show();
	}

	public void onClick(View v) {
		vibrateOnButtonClick(currentActivity);
		setFlashlightActions();
	}

	final static class ImageAdapter extends BaseAdapter {

		private Context mContext;
		private final static String TAG = "ImageAdapter";
		final int thumbs[] = { R.drawable.bluetooh, R.drawable.brightness,
				R.drawable.ringinvolum, R.drawable.wifi, R.drawable.icon };

		final String tags[] = { "Bluetooth", "Brightness", "Ringing Volume",
				"Wifi", "Settings", };

		public ImageAdapter(Context context) {
			// TODO Auto-generated constructor stub
			mContext = context;
		}

		/**
		 * calculate and store as static the multiplicity of the position i.e
		 * position / thumbs.length. to actually measure how many times the 5
		 * has been passed.
		 */
		private static int multiplicity = 0;

		public int getCount() {
			// TODO Auto-generated method stub
			return Integer.MAX_VALUE;
		}

		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			// Log.i(TAG,"GetItem called");
			return arg0;
		}

		static int itemId = 0;

		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ImageView i = new ImageView(mContext);
			i.setLayoutParams(new Gallery.LayoutParams(200, 200));
			multiplicity = position / 5;
			int desirePos = (position - (5 * multiplicity));
			i.setImageResource(thumbs[desirePos]);
			i.setTag(tags[desirePos]);

			return i;

		}

	}
	public  void vibrateOnButtonClick(Context context) {
		Vibrator vibe = (Vibrator) context
				.getSystemService(Context.VIBRATOR_SERVICE);
		vibe.vibrate(50);
	}
}
