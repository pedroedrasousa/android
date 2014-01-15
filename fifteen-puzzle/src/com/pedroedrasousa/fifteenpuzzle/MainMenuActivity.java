package com.pedroedrasousa.fifteenpuzzle;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.pedroedrasousa.engine.Timer;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;


public class MainMenuActivity extends Activity {

	private AdView mAdView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_menu_activity);

	    final Button button = (Button) findViewById(R.id.simple_game);
	    button.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
	        	newClassicGame();
	        }
	    });
		
	    final Button button2 = (Button) findViewById(R.id.puzzle_game);
	    button2.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
	        	newChallengeGame();
	        }
	    });
	    
	    final Button btnAbout = (Button) findViewById(R.id.about);
	    btnAbout.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
	        	showAboutDialog();
	        }
	    });	    

	    // No ads for <2.3 devices.
	    if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
			// Create the adView.
			mAdView = new AdView(this);
			mAdView.setAdUnitId(getString(R.string.ad_view_unit_id_main_menu));
			mAdView.setAdSize(AdSize.BANNER);
			RelativeLayout layout = (RelativeLayout)findViewById(R.id.main_menu_layout);
	
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			layout.addView(mAdView, params);
			
			AdRequest adRequest = new AdRequest.Builder().build();
			mAdView.loadAd(adRequest);
	    }
	}

	private void newClassicGame() {
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1) {
			@Override
		    public View getView(int position, View convertView, ViewGroup parent) {
		        TextView textView = (TextView) super.getView(position, convertView, parent);
		        textView.setTextColor(getResources().getColor(R.color.list_text));
		        return textView;
		    }
		};
		
		int time, moves;
		
		time	= Score.getValue(MainMenuActivity.this, "classic", "3x3", "time");
		moves	= Score.getValue(MainMenuActivity.this, "classic", "3x3", "moves");
		if (time == -1) {
			adapter.add("3x3");
		} else {
			adapter.add("3x3\t(" + Timer.msToStringMMSS(time * 1000) + " in " + moves + " moves)");
		}
		
		time	= Score.getValue(MainMenuActivity.this, "classic", "3x4", "time");
		moves	= Score.getValue(MainMenuActivity.this, "classic", "3x4", "moves");
		if (time == -1) {
			adapter.add("3x4");
		} else {
			adapter.add("3x4\t(" + Timer.msToStringMMSS(time * 1000) + " in " + moves + " moves)");
		}
		
		time	= Score.getValue(MainMenuActivity.this, "classic", "4x4", "time");
		moves	= Score.getValue(MainMenuActivity.this, "classic", "4x4", "moves");
		if (time == -1) {
			adapter.add("4x4");
		} else {
			adapter.add("4x4\t(" + Timer.msToStringMMSS(time * 1000) + " in " + moves + " moves)");
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle(R.string.classic_game_title);
	    
	    builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int which) {
	        	Intent intent = new Intent(MainMenuActivity.this, GameActivity.class);
	        	intent.putExtra("game_id1", "classic");
	    		switch (which) {
				case 0:
		        	intent.putExtra("game_id2", "3x3");
		        	break;
				case 1:
		        	intent.putExtra("game_id2", "3x4");
		        	break;
				case 2:
		        	intent.putExtra("game_id2", "4x4");
					break;
				default:
					break;
	    		}
	        	startActivity(intent);
	    	}
	    });
	    builder.create().show();
	    
	    // If this is the first time playing, show a message box.
		SharedPreferences sharedPref = getSharedPreferences("fifteen_shared", Context.MODE_PRIVATE);
		if ( sharedPref.getBoolean("first_classic", true) ) {
			new AlertDialog.Builder(this)
				.setMessage(R.string.msg_classic_mode_intro)
				.setNeutralButton(R.string.btn_ok, null).show();
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putBoolean("first_classic", false);
			editor.commit();
		}
	}
	
	private void newChallengeGame() {
				
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1) {
			
			@Override
		    public View getView(int position, View convertView, ViewGroup parent) {
		        TextView textView = (TextView) super.getView(position, convertView, parent);
		        if ( isEnabled(position) ) {
		        	textView.setTextColor(getResources().getColor(R.color.list_text));
		        } else {
		        	textView.setTextColor(getResources().getColor(R.color.list_text_disabled));
		        }
		        return textView;
		    }
			
			@Override
	        public boolean isEnabled(int position) {
				int time = Score.getValue(MainMenuActivity.this, "challenge", String.valueOf(position - 1), "time");
				if (time == -1 && position != 0)
					return false; 
				return true;
			}
		};
		
		int i = 0;
		// Get the first level name.
		String levelName = FiffteenPuzzleBoardLoader.getLevelName(this, FifteenPuzzle.LEVEL_FILE, i);
		
		do {
			int time = Score.getValue(MainMenuActivity.this, "challenge", String.valueOf(i), "time");
			if (time == -1) {
				adapter.add(levelName + "\t\t(unsolved)");
			} else {
				adapter.add(levelName + "\t\t(" + Timer.msToStringMMSS(time * 1000) + ")");
			}
			i++;
		} while (!(levelName = FiffteenPuzzleBoardLoader.getLevelName(this, "levels.txt", i)).equals(""));
	

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle(R.string.challenge_title);
	    builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int which) {
	        	Intent intent = new Intent(MainMenuActivity.this, GameActivity.class);
	        	intent.putExtra("game_id1", "challenge");
	        	intent.putExtra("game_id2", Integer.toString(which));
	        	startActivity(intent);
	    	}
	    });
	    builder.create().show();
	    
	    // If this is the first time playing, show a message box.
		SharedPreferences sharedPref = getSharedPreferences("fifteen_shared", Context.MODE_PRIVATE);
		if ( sharedPref.getBoolean("first_challenge", true) ) {
			new AlertDialog.Builder(this)
				.setMessage(R.string.msg_challenge_mode_intro)
				.setNeutralButton(R.string.btn_ok, null).show();
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putBoolean("first_challenge", false);
			editor.commit();
		}
	}
	
	private void showAboutDialog() {
		String versionName = new String();
		String year		= getResources().getString(R.string.year);
		String author	= getResources().getString(R.string.author);
		String website	= getResources().getString(R.string.website);
		String email	= getResources().getString(R.string.email);
		
		try {
			versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
		TextView aboutText1 	= new TextView(this);
		TextView aboutText2 	= new TextView(this);
		TextView aboutText4		= new TextView(this);
		TextView emailText		= new TextView(this);
		TextView websiteText	= new TextView(this);
		
		aboutText1.setText("Version " + versionName + "\n" + year + " " + author);
		aboutText2.setText(R.string.about);
		aboutText4.setMovementMethod(LinkMovementMethod.getInstance());
		websiteText.setAutoLinkMask(Linkify.WEB_URLS);
		websiteText.setText(website);		
		emailText.setAutoLinkMask(Linkify.EMAIL_ADDRESSES);
		emailText.setText(email);
		
		LinearLayout aboutLayout = new LinearLayout(this);
		aboutLayout.setOrientation(LinearLayout.VERTICAL);
		aboutLayout.setPadding(10, 5, 0, 10);
		aboutLayout.addView(aboutText1);
		aboutLayout.addView(websiteText);
		aboutLayout.addView(aboutText2);
		aboutLayout.addView(emailText);
		if (!aboutText4.getText().equals(""))
			aboutLayout.addView(aboutText4);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name)
        .setView(aboutLayout)
        .setNegativeButton("Close", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (mAdView != null)
			mAdView.resume();
	}

	@Override
	protected void onPause() {
		if (mAdView != null)
			mAdView.pause();
		super.onPause();
	}
	
	@Override
	public void onDestroy() {
		if (mAdView != null)
			mAdView.destroy();
		super.onDestroy();
	}
}
