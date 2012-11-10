package ict.step10.livewallpaper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class Preferences extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener{

	public SharedPreferences prefs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO 自動生成されたメソッド・スタブ
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		this.getPreferenceManager().setSharedPreferencesName("SlideshowWallpaperPrefs");
		this.addPreferencesFromResource(R.xml.preferences);
		this.prefs = this.getSharedPreferences("SlideshowWallpaperPrefs", MODE_PRIVATE);
		
		((EditText)this.findViewById(R.id.editText1)).setText(prefs.getString("Folder", ""));
		prefs.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onStop() {
		// TODO 自動生成されたメソッド・スタブ
		super.onStop();
		Editor editor = prefs.edit();
		editor.putString("Folder", ((EditText)this.findViewById(R.id.editText1)).getText().toString());
		editor.commit();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
		// TODO 自動生成されたメソッド・スタブ
		//Prefsからフォルダー名の情報を読み取りEditTextに表示
		((EditText)this.findViewById(R.id.editText1)).setText(prefs.getString("Folder", ""));
		
	}
	
	public void onClick(View v){
		Intent intent = new Intent(this,FilePicker.class);
		this.startActivity(intent);
	}

}
