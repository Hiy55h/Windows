package com.hiy55h.astraos;

import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.graphics.*;
import android.net.Uri;
import android.os.*;
import android.provider.OpenableColumns;
import android.view.*;
import android.webkit.*;
import android.widget.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

public class MainActivity extends Activity {
    private SharedPreferences prefs;
    private FrameLayout root, desktop, windowLayer;
    private String userName="User", userIcon="👤";
    private int wallpaper=0;
    private boolean dark=false, showClock=true, sounds=false, showDesktopIcons=true;
    private final Set<String> shortcuts=new LinkedHashSet<>();
    private final ArrayList<FrameLayout> openWindows=new ArrayList<>();
    private final ArrayList<String> windowNames=new ArrayList<>();
    private static final int PICK_FILE=71, PICK_PHOTO=72;
    private final Handler handler=new Handler();
    private final String[] apps={"ファイルマネージャー","Astraの設定","Google TVの設定","メモ帳","電卓","写真","タスクマネージャー","ブラウザー"};
    private final String[] icons={"📁","⚙️","📺","📝","🧮","🖼️","📊","🌐"};
    private TextView clockView;

    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        prefs=getSharedPreferences("astra",MODE_PRIVATE);
        userName=prefs.getString("user","User"); userIcon=prefs.getString("icon","👤");
        wallpaper=prefs.getInt("wallpaper",0); dark=prefs.getBoolean("dark",false);
        showClock=prefs.getBoolean("clock",true); sounds=prefs.getBoolean("sounds",false);
        showDesktopIcons=prefs.getBoolean("desktop_icons",true);
        shortcuts.addAll(prefs.getStringSet("shortcuts",new HashSet<>()));
        if(!prefs.getBoolean("setup_done",false)) showSetup(0); else showLock();
    }
    private int bg(){return new int[]{Color.rgb(20,45,80),Color.rgb(25,75,70),Color.rgb(65,42,85),Color.rgb(35,38,48)}[Math.floorMod(wallpaper,4)];}
    private int panel(){return dark?Color.rgb(35,39,48):Color.rgb(245,247,250);}
    private int ink(){return dark?Color.WHITE:Color.rgb(25,30,40);}
    private TextView label(String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(ink());t.setGravity(Gravity.CENTER);t.setPadding(10,7,10,7);return t;}
    private Button button(String s,Runnable r){Button b=new Button(this);b.setText(s);b.setOnClickListener(v->r.run());return b;}
    private LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);l.setPadding(16,12,16,12);return l;}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
    private void savePrefs(){prefs.edit().putString("user",userName).putString("icon",userIcon).putInt("wallpaper",wallpaper).putBoolean("dark",dark).putBoolean("clock",showClock).putBoolean("sounds",sounds).putBoolean("desktop_icons",showDesktopIcons).putStringSet("shortcuts",new HashSet<>(shortcuts)).apply();}

    private void showSetup(int step){
        LinearLayout page=column();page.setGravity(Gravity.CENTER);page.setBackgroundColor(bg());
        TextView title=label("AstraOS セットアップ",28);title.setTextColor(Color.WHITE);page.addView(title);
        String[] hints={"ようこそ！AstraOSへ","ユーザー名を決めよう","パスワードを設定（任意）","ユーザーアイコンを選ぼう","壁紙を選ぼう","テーマを選ぼう","日時表示を設定","セットアップ完了！"};
        TextView hint=label(hints[step],18);hint.setTextColor(Color.WHITE);page.addView(hint);
        if(step==0)page.addView(button("開始する",()->showSetup(1)));
        if(step==1){EditText e=new EditText(this);e.setSingleLine(true);e.setHint("ユーザー名");page.addView(e);page.addView(button("次へ",()->{String n=e.getText().toString().trim();if(n.isEmpty()){e.setError("入力してね");return;}userName=n;showSetup(2);}));}
        if(step==2){EditText e=new EditText(this);e.setSingleLine(true);e.setHint("パスワード（空欄でもOK）");e.setInputType(129);page.addView(e);page.addView(button("次へ",()->{prefs.edit().putString("password",e.getText().toString()).apply();showSetup(3);}));}
        if(step==3){LinearLayout row=new LinearLayout(this);for(String ic:new String[]{"👤","😀","🚀","🌟","🐱","🎮"})row.addView(button(ic,()->{userIcon=ic;toast("アイコン: "+ic);}));page.addView(row);page.addView(button("次へ",()->showSetup(4)));}
        if(step==4){for(int i=0;i<4;i++){final int x=i;page.addView(button("壁紙 "+(i+1),()->{wallpaper=x;toast("壁紙を選択");}));}page.addView(button("次へ",()->showSetup(5)));}
        if(step==5){page.addView(button("ライトテーマ",()->{dark=false;toast("ライトテーマを選択");}));page.addView(button("ダークテーマ",()->{dark=true;toast("ダークテーマを選択");}));page.addView(button("次へ",()->showSetup(6)));}
        if(step==6){page.addView(button("時計を表示",()->showClock=true));page.addView(button("時計を非表示",()->showClock=false));page.addView(button("次へ",()->showSetup(7)));}
        if(step==7)page.addView(button("AstraOSを開始！",()->{savePrefs();prefs.edit().putBoolean("setup_done",true).apply();showDesktop();}));
        base(page);
    }
    private void base(View v){setContentView(v);}
    private void showLock(){
        LinearLayout p=column();p.setGravity(Gravity.CENTER);p.setBackgroundColor(bg());
        TextView t=label(userIcon+"  AstraOS",34);t.setTextColor(Color.WHITE);p.addView(t);
        TextView who=label(userName,20);who.setTextColor(Color.WHITE);p.addView(who);
        EditText pass=new EditText(this);pass.setSingleLine(true);pass.setGravity(Gravity.CENTER);pass.setHint("パスワード");pass.setInputType(129);p.addView(pass);
        String expected=prefs.getString("password","");
        if(expected.isEmpty()){pass.setVisibility(View.GONE);p.addView(button("デスクトップへ",this::showDesktop));}
        else p.addView(button("ログイン",()->{if(expected.equals(pass.getText().toString()))showDesktop();else pass.setError("パスワードが違います");}));
        base(p);
    }

    private void showDesktop(){
        root=new FrameLayout(this);root.setBackgroundColor(bg());
        desktop=new FrameLayout(this);root.addView(desktop,new FrameLayout.LayoutParams(-1,-1));
        windowLayer=new FrameLayout(this);root.addView(windowLayer,new FrameLayout.LayoutParams(-1,-1));
        TextView greeting=label("AstraOS  •  "+userIcon+" "+userName,19);greeting.setTextColor(Color.WHITE);greeting.setGravity(Gravity.TOP|Gravity.LEFT);
        desktop.addView(greeting,new FrameLayout.LayoutParams(-1,55));
        if(showDesktopIcons){int y=72;for(String a:shortcuts){addDesktopShortcut(a,18,y);y+=78;}}
        LinearLayout bar=new LinearLayout(this);bar.setGravity(Gravity.CENTER_VERTICAL);bar.setPadding(8,3,8,3);bar.setBackgroundColor(0xEE111827);
        TextView menu=label("✦ AstraMenu",16);menu.setTextColor(Color.WHITE);menu.setBackgroundColor(0xFF263449);menu.setOnClickListener(v->showAppList());bar.addView(menu);
        TextView user=label(userIcon+" "+userName,14);user.setTextColor(Color.WHITE);bar.addView(user);
        if(showClock){clockView=label("",14);clockView.setTextColor(Color.WHITE);bar.addView(clockView,new LinearLayout.LayoutParams(0,48,1));updateClock();}
        FrameLayout.LayoutParams bp=new FrameLayout.LayoutParams(-1,56,Gravity.BOTTOM);root.addView(bar,bp);
        base(root);
    }
    private void updateClock(){if(clockView!=null){clockView.setText(new java.text.SimpleDateFormat("yyyy/MM/dd  HH:mm",java.util.Locale.JAPAN).format(new Date()));handler.postDelayed(this::updateClock,1000);}}
    private void addDesktopShortcut(String app,int x,int y){TextView icon=label(iconFor(app)+"\n"+app,14);icon.setTextColor(Color.WHITE);icon.setBackgroundColor(0x55334455);FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(150,68);p.leftMargin=x;p.topMargin=y;desktop.addView(icon,p);icon.setOnClickListener(v->openApp(app));}
    private String iconFor(String app){for(int i=0;i<apps.length;i++)if(apps[i].equals(app))return icons[i];return "▣";}
    private void showAppList(){
        LinearLayout list=column();ScrollView scroll=new ScrollView(this);
        for(String app:apps){Button item=new Button(this);item.setText(iconFor(app)+"  "+app);item.setOnClickListener(v->openApp(app));item.setOnLongClickListener(v->{showAppContext(item,app);return true;});item.setOnContextClickListener(v->{showAppContext(item,app);return true;});list.addView(item,new LinearLayout.LayoutParams(-1,56));}
        scroll.addView(list);new AlertDialog.Builder(this).setTitle("AstraMenu • アプリ").setView(scroll).setNegativeButton("閉じる",null).show();
    }
    private void showAppContext(View anchor,String app){
        PopupMenu pop=new PopupMenu(this,anchor);
        pop.getMenu().add("ホームに追加").setOnMenuItemClickListener(i->{shortcuts.add(app);savePrefs();if(desktop!=null){desktop.removeAllViews();TextView g=label("AstraOS  •  "+userIcon+" "+userName,19);g.setTextColor(Color.WHITE);g.setGravity(Gravity.TOP|Gravity.LEFT);desktop.addView(g,new FrameLayout.LayoutParams(-1,55));if(showDesktopIcons){int y=72;for(String a:shortcuts){addDesktopShortcut(a,18,y);y+=78;}}}toast("ホームに追加しました");return true;});
        pop.getMenu().add("起動").setOnMenuItemClickListener(i->{openApp(app);return true;});
        pop.getMenu().add("アプリ情報").setOnMenuItemClickListener(i->{new AlertDialog.Builder(this).setTitle(app).setMessage("AstraOS内蔵アプリ").setPositiveButton("OK",null).show();return true;});pop.show();
    }

    private void openApp(String app){
        if(app.equals("Google TVの設定")){try{startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));}catch(Exception e){toast("設定を開けませんでした");}return;}
        FrameLayout win=new FrameLayout(this);win.setBackgroundColor(panel());
        LinearLayout shell=new LinearLayout(this);shell.setOrientation(LinearLayout.VERTICAL);shell.setBackgroundColor(panel());
        LinearLayout titlebar=new LinearLayout(this);titlebar.setGravity(Gravity.CENTER_VERTICAL);titlebar.setBackgroundColor(0xFF263449);
        TextView title=label(app,15);title.setTextColor(Color.WHITE);title.setGravity(Gravity.CENTER_VERTICAL|Gravity.LEFT);titlebar.addView(title,new LinearLayout.LayoutParams(0,44,1));
        Button min=new Button(this);min.setText("—");min.setOnClickListener(v->win.setVisibility(View.GONE));titlebar.addView(min);
        Button max=new Button(this);max.setText("□");max.setOnClickListener(v->{if(win.getTag()==null){win.setTag("max");win.setTranslationX(0);win.setTranslationY(0);FrameLayout.LayoutParams p=(FrameLayout.LayoutParams)win.getLayoutParams();p.width=-1;p.height=-1;p.setMargins(0,0,0,0);p.gravity=Gravity.TOP|Gravity.LEFT;win.setLayoutParams(p);}else{win.setTag(null);FrameLayout.LayoutParams p=(FrameLayout.LayoutParams)win.getLayoutParams();p.width=Math.min(760,getResources().getDisplayMetrics().widthPixels-40);p.height=Math.min(470,getResources().getDisplayMetrics().heightPixels-100);p.gravity=Gravity.CENTER;win.setLayoutParams(p);}});titlebar.addView(max);
        Button close=new Button(this);close.setText("×");close.setOnClickListener(v->{windowLayer.removeView(win);openWindows.remove(win);windowNames.remove(app);});titlebar.addView(close);
        shell.addView(titlebar);FrameLayout body=new FrameLayout(this);shell.addView(body,new LinearLayout.LayoutParams(-1,0,1));
        switch(app){
            case "Astraの設定":buildSettings(body);break;
            case "ファイルマネージャー":buildFiles(body);break;
            case "メモ帳":buildNotepad(body);break;
            case "電卓":buildCalculator(body);break;
            case "写真":buildPhotos(body);break;
            case "タスクマネージャー":buildTaskManager(body);break;
            case "ブラウザー":buildBrowser(body);break;
            default:body.addView(label(app,18));
        }
        win.addView(shell);FrameLayout.LayoutParams wp=new FrameLayout.LayoutParams(Math.min(760,getResources().getDisplayMetrics().widthPixels-40),Math.min(470,getResources().getDisplayMetrics().heightPixels-100),Gravity.CENTER);
        windowLayer.addView(win,wp);openWindows.add(win);windowNames.add(app);
        win.setOnTouchListener((v,e)->{if(e.getAction()==MotionEvent.ACTION_DOWN)v.bringToFront();return false;});
        titlebar.setOnTouchListener(new View.OnTouchListener(){float sx,sy,tx,ty;public boolean onTouch(View v,MotionEvent e){if(e.getAction()==0){sx=e.getRawX();sy=e.getRawY();tx=win.getTranslationX();ty=win.getTranslationY();win.bringToFront();return true;}if(e.getAction()==2){win.setTranslationX(tx+e.getRawX()-sx);win.setTranslationY(ty+e.getRawY()-sy);return true;}return e.getAction()==1;}});
    }

    private void buildSettings(FrameLayout body){
        LinearLayout l=column();l.addView(label("Astraの設定",20));
        for(int i=0;i<4;i++){final int x=i;l.addView(button("壁紙 "+(i+1),()->setWallpaper(x)));}
        l.addView(button("ライト / ダーク切替",()->{dark=!dark;savePrefs();toast("テーマを保存しました（新しいウィンドウから反映）");}));
        EditText name=new EditText(this);name.setHint("新しいユーザー名");name.setText(userName);l.addView(name);
        l.addView(button("ユーザー名を保存",()->{if(!name.getText().toString().trim().isEmpty()){userName=name.getText().toString().trim();savePrefs();toast("保存しました");}}));
        LinearLayout iconsRow=new LinearLayout(this);for(String ic:new String[]{"👤","😀","🚀","🌟","🐱","🎮"})iconsRow.addView(button(ic,()->{userIcon=ic;savePrefs();toast("アイコンを変更しました");}));l.addView(label("ユーザーアイコン",16));l.addView(iconsRow);
        l.addView(button("パスワードを変更",()->passwordDialog()));
        l.addView(button("時計表示 ON / OFF",()->{showClock=!showClock;savePrefs();toast("保存しました。デスクトップ再表示で反映");}));
        l.addView(button("デスクトップアイコン ON / OFF",()->{showDesktopIcons=!showDesktopIcons;savePrefs();toast("保存しました。デスクトップ再表示で反映");}));
        l.addView(button("システムサウンド ON / OFF",()->{sounds=!sounds;savePrefs();toast("サウンド設定を保存しました");}));
        l.addView(button("初期セットアップをやり直す",()->new AlertDialog.Builder(this).setMessage("設定ウィザードを次回起動時に表示しますか？").setPositiveButton("はい",(d,w)->{prefs.edit().putBoolean("setup_done",false).apply();showSetup(0);}).setNegativeButton("キャンセル",null).show()));
        ScrollView s=new ScrollView(this);s.addView(l);body.addView(s,new FrameLayout.LayoutParams(-1,-1));
    }
    private void passwordDialog(){LinearLayout l=column();EditText p=new EditText(this);p.setHint("新しいパスワード（空欄で解除）");p.setInputType(129);l.addView(p);new AlertDialog.Builder(this).setTitle("パスワード変更").setView(l).setPositiveButton("保存",(d,w)->{prefs.edit().putString("password",p.getText().toString()).apply();toast("パスワードを保存しました");}).setNegativeButton("キャンセル",null).show();}
    private void setWallpaper(int n){wallpaper=n;savePrefs();if(desktop!=null)desktop.setBackgroundColor(bg());if(root!=null)root.setBackgroundColor(bg());toast("壁紙を変更しました");}

    private void buildFiles(FrameLayout body){LinearLayout l=column();l.addView(label("ファイルマネージャー",20));l.addView(button("ファイルを開く",()->pick("*/*",PICK_FILE)));l.addView(button("画像を選ぶ",()->pick("image/*",PICK_PHOTO)));l.addView(label("TXT / HTML / PNG / JPG / ZIP / APK に対応。EXEは実行できません。",14));body.addView(l);}
    private void pick(String type,int request){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType(type);i.addCategory(Intent.CATEGORY_OPENABLE);i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivityForResult(i,request);}
    private String displayName(Uri uri){try(Cursor c=getContentResolver().query(uri,null,null,null,null)){if(c!=null&&c.moveToFirst()){int n=c.getColumnIndex(OpenableColumns.DISPLAY_NAME);if(n>=0)return c.getString(n);}}catch(Exception ignored){}return uri.getLastPathSegment()==null?"file":uri.getLastPathSegment();}
    private void openUri(Uri uri){
        String name=displayName(uri),low=name.toLowerCase(Locale.ROOT),mime=getContentResolver().getType(uri);
        if(low.endsWith(".exe")){toast("Windows用EXEはAndroidで直接実行できません");return;}
        if(low.endsWith(".zip")||"application/zip".equals(mime)){extractZip(uri);return;}
        if(low.endsWith(".txt")||"text/plain".equals(mime)){readText(uri,name);return;}
        if(low.endsWith(".html")||low.endsWith(".htm")||"text/html".equals(mime)){openHtml(uri,name);return;}
        if((mime!=null&&mime.startsWith("image/"))||low.matches(".*\\.(png|jpg|jpeg|gif|webp)$")){showImage(uri,name);return;}
        if(low.endsWith(".apk")){try{Intent i=new Intent(Intent.ACTION_VIEW);i.setDataAndType(uri,"application/vnd.android.package-archive");i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(i);}catch(Exception e){toast("APKを開けませんでした");}return;}
        try{Intent i=new Intent(Intent.ACTION_VIEW);i.setDataAndType(uri,mime==null?"*/*":mime);i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(i);}catch(Exception e){toast("このファイル形式は開けません");}
    }
    private void readText(Uri uri,String name){try(BufferedReader r=new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(uri)))){StringBuilder b=new StringBuilder();String line;while((line=r.readLine())!=null&&b.length()<200000)b.append(line).append("\n");showTextWindow(name,b.toString());}catch(Exception e){toast("読み込めませんでした");}}
    private void showTextWindow(String name,String text){openApp("メモ帳");EditText e=findLastEditText();if(e!=null)e.setText(text);toast(name);}
    private EditText findLastEditText(){if(openWindows.isEmpty())return null;FrameLayout w=openWindows.get(openWindows.size()-1);return findEdit((ViewGroup)w);}
    private EditText findEdit(ViewGroup g){for(int i=0;i<g.getChildCount();i++){View v=g.getChildAt(i);if(v instanceof EditText)return (EditText)v;if(v instanceof ViewGroup){EditText e=findEdit((ViewGroup)v);if(e!=null)return e;}}return null;}
    private void buildNotepad(FrameLayout body){LinearLayout l=column();EditText note=new EditText(this);note.setGravity(Gravity.TOP|Gravity.LEFT);note.setHint("メモを入力");note.setText(prefs.getString("note",""));l.addView(note,new LinearLayout.LayoutParams(-1,0,1));l.addView(button("保存",()->{prefs.edit().putString("note",note.getText().toString()).apply();toast("メモを保存しました");}));body.addView(l,new FrameLayout.LayoutParams(-1,-1));}
    private void openHtml(Uri uri,String name){try{String html;try(InputStream in=getContentResolver().openInputStream(uri)){ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buf=new byte[4096];int n;while((n=in.read(buf))>0&&out.size()<1000000)out.write(buf,0,n);html=out.toString("UTF-8");}openApp("ブラウザー");}catch(Exception e){toast("HTMLを開けませんでした");}}
    private void showImage(Uri uri,String name){openApp("写真");if(openWindows.isEmpty())return;ImageView img=new ImageView(this);img.setImageURI(uri);img.setAdjustViewBounds(true);img.setScaleType(ImageView.ScaleType.FIT_CENTER);FrameLayout w=openWindows.get(openWindows.size()-1);ViewGroup shell=(ViewGroup)w.getChildAt(0);ViewGroup body=(ViewGroup)shell.getChildAt(1);body.removeAllViews();body.addView(img,new FrameLayout.LayoutParams(-1,-1));}
    private void buildPhotos(FrameLayout body){LinearLayout l=column();l.addView(label("写真",20));l.addView(button("画像を開く",()->pick("image/*",PICK_PHOTO)));body.addView(l);}
    private void extractZip(Uri uri){File dir=new File(getExternalFilesDir(null),"Extracted");if(!dir.exists())dir.mkdirs();int count=0;try(ZipInputStream zis=new ZipInputStream(getContentResolver().openInputStream(uri))){ZipEntry entry;byte[] buf=new byte[8192];while((entry=zis.getNextEntry())!=null){File out=new File(dir,entry.getName());String base=dir.getCanonicalPath()+File.separator;if(!out.getCanonicalPath().startsWith(base))continue;if(entry.isDirectory()){out.mkdirs();continue;}File parent=out.getParentFile();if(parent!=null)parent.mkdirs();try(FileOutputStream fos=new FileOutputStream(out)){int n;while((n=zis.read(buf))>0)fos.write(buf,0,n);}count++;}}catch(Exception e){toast("ZIPを展開できませんでした");return;}new AlertDialog.Builder(this).setTitle("ZIP展開完了").setMessage(count+" 個のファイルを展開しました。保存先: "+dir.getAbsolutePath()).setPositiveButton("OK",null).show();}
    private void buildBrowser(FrameLayout body){LinearLayout l=column();EditText address=new EditText(this);address.setSingleLine(true);address.setText("https://www.youtube.com/");l.addView(address);WebView web=new WebView(this);web.getSettings().setJavaScriptEnabled(true);web.getSettings().setDomStorageEnabled(true);web.setWebViewClient(new WebViewClient());l.addView(button("開く",()->{String u=address.getText().toString().trim();if(!u.startsWith("http://")&&!u.startsWith("https://"))u="https://"+u;web.loadUrl(u);}));l.addView(web,new LinearLayout.LayoutParams(-1,0,1));web.loadUrl("https://www.youtube.com/");body.addView(l,new FrameLayout.LayoutParams(-1,-1));}
    private void buildCalculator(FrameLayout body){LinearLayout l=column();EditText a=new EditText(this);a.setHint("数値 A");a.setInputType(8194);EditText b=new EditText(this);b.setHint("数値 B");b.setInputType(8194);TextView result=label("結果",20);l.addView(a);l.addView(b);for(String op:new String[]{"+","−","×","÷"})l.addView(button("A "+op+" B",()->calculate(a,b,result,op)));l.addView(result);body.addView(l);}
    private void calculate(EditText a,EditText b,TextView out,String op){try{double x=Double.parseDouble(a.getText().toString()),y=Double.parseDouble(b.getText().toString()),z;if(op.equals("+"))z=x+y;else if(op.equals("−"))z=x-y;else if(op.equals("×"))z=x*y;else{if(y==0){out.setText("0では割れません");return;}z=x/y;}out.setText("結果: "+z);}catch(Exception e){out.setText("数値を入力してください");}}
    private void buildTaskManager(FrameLayout body){LinearLayout l=column();l.addView(label("Overlays • タスクマネージャー",18));TextView list=label("",16);StringBuilder b=new StringBuilder();for(int i=0;i<windowNames.size();i++)b.append("• ").append(windowNames.get(i)).append("\n");list.setText(b.toString());l.addView(list);l.addView(button("ウィンドウ数: "+openWindows.size(),()->toast("起動中: "+openWindows.size())));l.addView(button("すべて最小化",()->{for(FrameLayout w:openWindows)w.setVisibility(View.GONE);}));l.addView(button("すべて閉じる",()->{for(FrameLayout w:new ArrayList<>(openWindows))windowLayer.removeView(w);openWindows.clear();windowNames.clear();}));body.addView(l);}
    @Override protected void onActivityResult(int req,int res,Intent data){super.onActivityResult(req,res,data);if(res==RESULT_OK&&data!=null&&data.getData()!=null){if(req==PICK_FILE)openUri(data.getData());else if(req==PICK_PHOTO)showImage(data.getData(),displayName(data.getData()));}}
    @Override public void onBackPressed(){if(windowLayer!=null){for(FrameLayout w:openWindows)w.setVisibility(View.GONE);toast("Overlaysのウィンドウを最小化しました");}else super.onBackPressed();}
}
