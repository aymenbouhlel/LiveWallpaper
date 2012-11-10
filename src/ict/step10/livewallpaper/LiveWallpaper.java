package ict.step10.livewallpaper;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Environment;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;


public class LiveWallpaper extends WallpaperService {

	private final Handler handler = new Handler();
	public Integer interval;
	public Integer counter;
	public String strFolder;
	public SharedPreferences prefs;
	
	private class WallpeperEngine extends Engine implements SharedPreferences.OnSharedPreferenceChangeListener{

		public Integer[] resBitmap;
		public Integer w,h;
		public Boolean mVisible;
		
		
		private final Runnable mWallpaper = new Runnable() {
			
			@Override
			public void run() {
				// TODO 自動生成されたメソッド・スタブ
				showWallpaper();
				
			}
		};
		
		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			// TODO 自動生成されたメソッド・スタブ
			super.onCreate(surfaceHolder);
			this.resBitmap = new Integer[]{R.drawable.ic_launcher,R.drawable.ic_launcher2};
			
			prefs = getSharedPreferences("SlideshowWallpaperPrefs", MODE_PRIVATE);
			prefs.registerOnSharedPreferenceChangeListener(this);
			
			init();
		}

		@Override
		public void onDestroy() {
			// TODO 自動生成されたメソッド・スタブ
			super.onDestroy();
			handler.removeCallbacks(mWallpaper);
		}

		@Override
		public void onSurfaceChanged(SurfaceHolder holder, int format,
				int width, int height) {
			// TODO 自動生成されたメソッド・スタブ
			super.onSurfaceChanged(holder, format, width, height);
			this.w = width;
			this.h = height;
		}

		@Override
		public void onSurfaceCreated(SurfaceHolder holder) {
			// TODO 自動生成されたメソッド・スタブ
			super.onSurfaceCreated(holder);
		}

		//壁紙の状態が変化したときに呼ばれる
		@Override
		public void onVisibilityChanged(boolean visible) {
			// TODO 自動生成されたメソッド・スタブ
			super.onVisibilityChanged(visible);
			this.mVisible = visible;
			if(visible){
				this.showWallpaper();
			}else{
				handler.removeCallbacks(mWallpaper);
			}			
		}
		
		public void showWallpaper(){
			final SurfaceHolder mHolder = this.getSurfaceHolder();
			Bitmap bitmap;
			String fn = findImageFile();
			//何故#equalsを使わないのか
			if(fn == ""){
				bitmap = this.loadImageFromResource();
			}else{
				bitmap = loadImageFile(fn);
			}
			
			//壁紙を表示する
			Canvas canvas = mHolder.lockCanvas();
			if(canvas!=null){
				canvas.drawColor(Color.BLACK);
				canvas.drawBitmap(bitmap, 0,0, null);
				mHolder.unlockCanvasAndPost(canvas);
			}
			counter++;
			handler.removeCallbacks(mWallpaper);
			if(mVisible)//再実行
				handler.postDelayed(mWallpaper, interval);			
		}
		
		public Bitmap loadImageFromResource(){
			
			Bitmap bitmap;
			if(counter >= resBitmap.length)
				counter = 0;
			Resources res = getResources();
			bitmap = BitmapFactory.decodeResource(res, resBitmap[counter]);
			bitmap = Bitmap.createScaledBitmap(bitmap,w,h,false);
			return bitmap;
		}
		
		public Bitmap loadImageFile(String path){
			
			Bitmap bitmap;
			BitmapFactory.Options opt = new BitmapFactory.Options();
			opt.inJustDecodeBounds = true;
			
			//画像の大きさを調べる
			BitmapFactory.decodeFile(path,opt);
			Integer oh = opt.outHeight;
			Integer ow = opt.outWidth;
			
			//画像の縮小
			BitmapFactory.Options opt2 = new BitmapFactory.Options();
			if(oh<h || ow < w){
				opt2.inSampleSize = Math.min(w/ow, h/oh);
			}else{
				opt2.inSampleSize=Math.max(ow/w,oh/h);
			}
			bitmap = BitmapFactory.decodeFile(path,opt2);
			
			//ディスプレイの向きにあわせ拡大縮小
			if(h>w){
				Bitmap offbitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
				Canvas offCanvas = new Canvas(offbitmap);
				bitmap = Bitmap.createScaledBitmap(bitmap, (int)(w),(int)(w*(((double)oh)/((double)ow))),false);
				offCanvas.drawBitmap(bitmap, 0, (h-bitmap.getHeight())/2,null);
				bitmap = offbitmap;
			}else{
				Bitmap offbitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
				Canvas offCanvas = new Canvas(offbitmap);
				bitmap = Bitmap.createScaledBitmap(bitmap, (int)(h*((double)ow/(double)oh)),h,false);
				offCanvas.drawBitmap(bitmap, (w-bitmap.getWidth())/2,(h-bitmap.getHeight())/2,null);
				bitmap = offbitmap;
			}
			
			return bitmap;
			
		}
		
		public String findImageFile(){
			String fn = "";
			FileFilter fFilter = new FileFilter() {
				
				@Override
				public boolean accept(File file) {
					// TODO 自動生成されたメソッド・スタブ
					Pattern p = Pattern.compile("\\.png$|\\.jpg$|\\.gif$|\\.jpeg$|\\.bmp$|",Pattern.CASE_INSENSITIVE);
					Matcher m= p.matcher(file.getName());
					boolean result = m.find()&&!file.isHidden();
					return result;
				}
			};
			
			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
				File folder = new File(strFolder);
				File[] fc = folder.listFiles(fFilter);
				
				if(fc!=null){
					if(fc.length >0){
						if(counter >= fc.length)
							counter=0;
							fn=fc[counter].toString();
					}
				}
			}
			return fn;
		}

		//SharedPreferenceが変更されたときに呼ばれる
		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			// TODO 自動生成されたメソッド・スタブ
			
			init();
			showWallpaper();
			
		}
	}
	
	@Override
	public Engine onCreateEngine() {
		// TODO 自動生成されたメソッド・スタブ
		return new WallpeperEngine();
	}
	
	public void init(){
		//壁紙の番号
		this.counter = 0;
		/*
		//表示間隔
		this.interval = 600000;
		//ファイルパスの指定
		this.strFolder = "/sdcard/mypaint";
		*/
		
		this.interval = Integer.parseInt(this.prefs.getString("Interval", "600000"));
		this.strFolder = prefs.getString("Folder", "");
	}

}
