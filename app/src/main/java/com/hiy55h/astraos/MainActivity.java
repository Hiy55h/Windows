package com.hiy55h.astraos;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends Activity {
    private SharedPreferences prefs;
    private FrameLayout root, desktop, windowLayer;
    private String userName = "User";
    private int wallpaper = 0;
    private boolean dark = false;
    private final Set<String> shortcuts = new HashSet<>();
    private final ArrayList<View> openWindows = new ArrayList<>();
    private int windowCount = 0;
    private static final int PICK_FILE = 71;
    private final String[] apps = {"ファイルマネージャー", "Astraの設定", "Google TVの設定", "メモ帳", "電卓", "写真", "タスクマネージャー"};

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        prefs = getSharedPreferences("astra", MODE_PRIVATE);
        userName = prefs.getString("user", "User");
        wallpaper = prefs.getInt("wallpaper", 0);
        dark = prefs.getBoolean("dark", false);
        shortcuts.addAll(prefs.getStringSet("shortcuts", new HashSet<>()));
        if (!prefs.getBoolean("setup_done", false)) showSetup(0);
        else showLock();
    }

    private int bg() { return new int[]{Color.rgb(20,45,80), Color.rgb(25,75,70), Color.rgb(65,42,85), Color.rgb(35,38,48)}[wallpaper % 4]; }
    private int panel() { return dark ? Color.rgb(35,39,48) : Color.rgb(245,247,250); }
    private int ink() { return dark ? Color.WHITE : Color.rgb(25,30,40); }

    private TextView label(String s, int size) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(ink());
        t.setGravity(Gravity.CENTER); t.setPadding(12, 8, 12, 8); return t;
    }
    private Button button(String s, Runnable action) {
        Button b = new Button(this); b.setText(s); b.setOnClickListener(v -> action.run()); return b;
    }
    private LinearLayout column() {
        LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); l.setPadding(24,18,24,18); return l;
    }
    private void base(View v) { setContentView(v); }

    private void showSetup(int step) {
        LinearLayout page = column(); page.setGravity(Gravity.CENTER); page.setBackgroundColor(bg());
        TextView title = label("AstraOS セットアップ", 28); title.setTextColor(Color.WHITE); page.addView(title);
        TextView hint = label(step == 0 ? "ようこそ！AstraOSへ" : step == 1 ? "ユーザー名を決めよう" : step == 2 ? "パスワードを設定（任意）" : step == 3 ? "壁紙を選ぼう" : "テーマを選ぼう", 18);
        hint.setTextColor(Color.WHITE); page.addView(hint);
        if (step == 0) page.addView(button("開始する", () -> showSetup(1)));
        if (step == 1) {
            EditText input = new EditText(this); input.setSingleLine(true); input.setHint("ユーザー名"); input.setText(userName.equals("User") ? "" : userName); page.addView(input);
            page.addView(button("次へ", () -> { String n=input.getText().toString().trim(); if(n.isEmpty()){input.setError("ユーザー名を入力してね");return;} userName=n; prefs.edit().putString("user",n).apply(); showSetup(2); }));
        }
        if (step == 2) {
            EditText input = new EditText(this); input.setSingleLine(true); input.setHint("パスワード（空欄でもOK）"); input.setInputType(129); page.addView(input);
            page.addView(button("次へ", () -> { prefs.edit().putString("password",input.getText().toString()).apply(); showSetup(3); }));
        }
        if (step == 3) {
            LinearLayout row = new LinearLayout(this); row.setGravity(Gravity.CENTER);
            for(int i=0;i<4;i++){ final int x=i; Button b=new Button(this); b.setText("壁紙 "+(i+1)); b.setOnClickListener(v->{wallpaper=x;prefs.edit().putInt("wallpaper",x).apply();}); row.addView(b); } page.addView(row);
            page.addView(button("次へ", () -> showSetup(4)));
        }
        if (step == 4) {
            page.addView(button("ライトテーマ", () -> {dark=false;prefs.edit().putBoolean("dark",false).apply();}));
            page.addView(button("ダークテーマ", () -> {dark=true;prefs.edit().putBoolean("dark",true).apply();}));
            page.addView(button("セットアップ完了！", () -> {prefs.edit().putBoolean("setup_done",true).apply();showDesktop();}));
        }
        base(page);
    }

    private void showLock() {
        LinearLayout page=column(); page.setGravity(Gravity.CENTER); page.setBackgroundColor(bg());
        TextView clock=label("AstraOS",34); clock.setTextColor(Color.WHITE); page.addView(clock);
        TextView who=label(userName,20); who.setTextColor(Color.WHITE); page.addView(who);
        EditText pass=new EditText(this); pass.setSingleLine(true); pass.setGravity(Gravity.CENTER); pass.setHint("パスワード"); pass.setInputType(129); page.addView(pass);
        page.addView(button("ログイン", () -> {String expected=prefs.getString("password",""); if(expected.equals(pass.getText().toString())) showDesktop(); else pass.setError("パスワードが違います");}));
        if(prefs.getString("password","").isEmpty()) { pass.setVisibility(View.GONE); page.getChildAt(3).setVisibility(View.GONE); page.addView(button("デスクトップへ",this::showDesktop)); }
        base(page);
    }

    private void showDesktop() {
        root=new FrameLayout(this); root.setBackgroundColor(bg());
        desktop=new FrameLayout(this); root.addView(desktop,new FrameLayout.LayoutParams(-1,-1));
        windowLayer=new FrameLayout(this); root.addView(windowLayer,new FrameLayout.LayoutParams(-1,-1));
        TextView greeting=label("AstraOS  •  "+userName,18); greeting.setTextColor(Color.WHITE); greeting.setGravity(Gravity.TOP|Gravity.LEFT); FrameLayout.LayoutParams gp=new FrameLayout.LayoutParams(-1,55); desktop.addView(greeting,gp);
        int y=75;
        for(String app:shortcuts){ addDesktopShortcut(app,18,y); y+=78; }
        LinearLayout bar=new LinearLayout(this); bar.setGravity(Gravity.CENTER_VERTICAL); bar.setPadding(10,4,10,4); bar.setBackgroundColor(0xDD111827);
        TextView menu=label("✦ AstraMenu",16); menu.setTextColor(Color.WHITE); menu.setBackgroundColor(0xFF263449); menu.setOnClickListener(v->showAppList()); bar.addView(menu);
        TextView user=label(userName,14); user.setTextColor(Color.WHITE); bar.addView(user);
        FrameLayout.LayoutParams bp=new FrameLayout.LayoutParams(-1,58,Gravity.BOTTOM); root.addView(bar,bp);
        base(root);
    }

    private void addDesktopShortcut(String app,int x,int y) {
        TextView icon=label("▣\n"+app,14); icon.setTextColor(Color.WHITE); icon.setBackgroundColor(0x55334455);
        FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(145,68); p.leftMargin=x;p.topMargin=y; desktop.addView(icon,p);
        icon.setOnClickListener(v->openApp(app));
    }

    private void showMenu(View anchor) { showAppList(); }

    private void showAppList() {
        LinearLayout list = column();
        list.setPadding(12, 8, 12, 8);
        for (String app : apps) {
            Button item = new Button(this);
            item.setText("▣  " + app);
            item.setOnClickListener(v -> openApp(app));
            item.setOnLongClickListener(v -> { showAppContext(item, app); return true; });
            item.setOnContextClickListener(v -> { showAppContext(item, app); return true; });
            list.addView(item, new LinearLayout.LayoutParams(-1, 58));
        }
        ScrollView scroll = new ScrollView(this);
        scroll.addView(list);
        new AlertDialog.Builder(this).setTitle("AstraMenu • アプリ").setView(scroll)
                .setNegativeButton("閉じる", null).show();
    }

    private void showAppContext(View anchor, String app) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add("ホームに追加").setOnMenuItemClickListener(item -> {
            shortcuts.add(app);
            prefs.edit().putStringSet("shortcuts", new HashSet<>(shortcuts)).apply();
            if (desktop != null) {
                desktop.removeAllViews();
                TextView greeting = label("AstraOS  •  " + userName, 18);
                greeting.setTextColor(Color.WHITE);
                greeting.setGravity(Gravity.TOP | Gravity.LEFT);
                desktop.addView(greeting, new FrameLayout.LayoutParams(-1, 55));
                int y = 75;
                for (String shortcut : shortcuts) { addDesktopShortcut(shortcut, 18, y); y += 78; }
            }
            toast(app + " をホームに追加しました");
            return true;
        });
        popup.getMenu().add("起動").setOnMenuItemClickListener(item -> { openApp(app); return true; });
        popup.getMenu().add("アプリ情報").setOnMenuItemClickListener(item -> {
            new AlertDialog.Builder(this).setTitle(app).setMessage("AstraOS内蔵アプリ").setPositiveButton("OK", null).show();
            return true;
        });
        popup.show();
    }

    private void openApp(String app) {
        if ("Google TVの設定".equals(app)) {
            try { startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS)); }
            catch(Exception e){ toast("Google TVの設定を開けませんでした"); }
            return;
        }
        FrameLayout win=new FrameLayout(this); win.setBackgroundColor(panel());
        LinearLayout shell=new LinearLayout(this); shell.setOrientation(LinearLayout.VERTICAL); shell.setBackgroundColor(panel());
        LinearLayout titlebar=new LinearLayout(this); titlebar.setGravity(Gravity.CENTER_VERTICAL); titlebar.setBackgroundColor(0xFF263449);
        TextView title=label(app,15); title.setTextColor(Color.WHITE); title.setGravity(Gravity.CENTER_VERTICAL|Gravity.LEFT); titlebar.addView(title,new LinearLayout.LayoutParams(0,48,1));
        Button min=new Button(this); min.setText("—"); min.setOnClickListener(v->win.setVisibility(View.GONE)); titlebar.addView(min);
        Button max=new Button(this); max.setText("□"); max.setOnClickListener(v->{FrameLayout.LayoutParams p=(FrameLayout.LayoutParams)win.getLayoutParams(); if(p.width==-1){p.width=getResources().getDisplayMetrics().widthPixels*3/4;p.height=getResources().getDisplayMetrics().heightPixels*3/4;p.gravity=Gravity.CENTER;}else{p.width=-1;p.height=-1;p.leftMargin=0;p.topMargin=0;}win.setLayoutParams(p);}); titlebar.addView(max);
        Button close=new Button(this); close.setText("×"); close.setOnClickListener(v->{windowLayer.removeView(win);openWindows.remove(win);}); titlebar.addView(close);
        shell.addView(titlebar);
        FrameLayout body=new FrameLayout(this); shell.addView(body,new LinearLayout.LayoutParams(-1,0,1));
        if(app.equals("Astraの設定")) buildSettings(body);
        else if(app.equals("ファイルマネージャー")) buildFiles(body);
        else if(app.equals("メモ帳")) { EditText note=new EditText(this);note.setGravity(Gravity.TOP|Gravity.LEFT);note.setHint("メモを入力");body.addView(note,new FrameLayout.LayoutParams(-1,-1)); }
        else if(app.equals("電卓")) buildCalculator(body);
        else if(app.equals("写真")) body.addView(label("写真を選ぶにはファイルマネージャーを使ってね",16));
        else if(app.equals("タスクマネージャー")) body.addView(label("起動中のウィンドウ: "+openWindows.size(),18));
        else body.addView(label(app,18));
        shell.addView(body);
        win.addView(shell);
        FrameLayout.LayoutParams wp=new FrameLayout.LayoutParams(Math.min(760,getResources().getDisplayMetrics().widthPixels-40),Math.min(470,getResources().getDisplayMetrics().heightPixels-100),Gravity.CENTER);
        windowLayer.addView(win,wp); openWindows.add(win); win.setOnTouchListener((v,e)->{if(e.getAction()==MotionEvent.ACTION_DOWN)v.bringToFront();return false;});
        titlebar.setOnTouchListener(new View.OnTouchListener(){float sx,sy;int ox,oy; public boolean onTouch(View v,MotionEvent e){FrameLayout.LayoutParams p=(FrameLayout.LayoutParams)win.getLayoutParams(); if(e.getAction()==0){sx=e.getRawX();sy=e.getRawY();ox=p.leftMargin;oy=p.topMargin;return true;} if(e.getAction()==1)return true; if(e.getAction()==2){p.gravity=Gravity.TOP|Gravity.LEFT;p.leftMargin=ox+(int)(e.getRawX()-sx);p.topMargin=oy+(int)(e.getRawY()-sy);win.setLayoutParams(p);return true;}return false;}});
    }

    private void buildSettings(FrameLayout body) {
        LinearLayout list=column();
        list.addView(label("Astraの設定",20));
        list.addView(button("壁紙：ネイビー",()->setWallpaper(0)));
        list.addView(button("壁紙：グリーン",()->setWallpaper(1)));
        list.addView(button("壁紙：パープル",()->setWallpaper(2)));
        list.addView(button("壁紙：ダーク",()->setWallpaper(3)));
        list.addView(button("ライト / ダーク切替",()->{dark=!dark;prefs.edit().putBoolean("dark",dark).apply();toast("テーマを変更したよ。再起動後に全体へ反映されます");}));
        EditText name=new EditText(this);name.setHint("新しいユーザー名");list.addView(name);
        list.addView(button("ユーザー名を保存",()->{if(!name.getText().toString().trim().isEmpty()){userName=name.getText().toString().trim();prefs.edit().putString("user",userName).apply();toast("ユーザー名を保存しました");}}));
        ScrollView scroll=new ScrollView(this);scroll.addView(list);body.addView(scroll,new FrameLayout.LayoutParams(-1,-1));
    }
    private void setWallpaper(int n){wallpaper=n;prefs.edit().putInt("wallpaper",n).apply();if(desktop!=null)desktop.setBackgroundColor(bg());if(root!=null)root.setBackgroundColor(bg());toast("壁紙を変更しました");}
    private void buildFiles(FrameLayout body) {
        LinearLayout list=column();list.addView(label("ファイルマネージャー",20));
        list.addView(button("ファイルを開く",()->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("*/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,PICK_FILE);}));
        list.addView(label("TXT / HTML / PNG / JPG / ZIP に対応予定\nEXEはAndroidで直接実行できません",14));
        body.addView(list);
    }
    private void buildCalculator(FrameLayout body) {
        LinearLayout l=column(); EditText a=new EditText(this);a.setHint("数値 A");a.setInputType(8194);EditText b=new EditText(this);b.setHint("数値 B");b.setInputType(8194);TextView result=label("結果",20);
        l.addView(a);l.addView(b);l.addView(button("A + B",()->calculate(a,b,result,"+")));l.addView(button("A − B",()->calculate(a,b,result,"-")));l.addView(button("A × B",()->calculate(a,b,result,"*")));l.addView(button("A ÷ B",()->calculate(a,b,result,"/")));l.addView(result);body.addView(l);
    }
    private void calculate(EditText a,EditText b,TextView out,String op){try{double x=Double.parseDouble(a.getText().toString()),y=Double.parseDouble(b.getText().toString());if(op.equals("/")&&y==0){out.setText("0では割れません");return;}double z=op.equals("+")?x+y:op.equals("-")?x-y:op.equals("*")?x*y:x/y;out.setText("結果: "+z);}catch(Exception e){out.setText("数値を入力してください");}}
    @Override protected void onActivityResult(int req,int res,Intent data){super.onActivityResult(req,res,data);if(req==PICK_FILE&&res==RESULT_OK&&data!=null){Uri uri=data.getData();String name=uri.getLastPathSegment();String lower=name==null?"":name.toLowerCase();if(lower.endsWith(".exe")){toast("Windows専用のEXEファイルはAndroidでは実行できません");return;}if(lower.endsWith(".txt")){try{BufferedReader r=new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(uri)));StringBuilder s=new StringBuilder();String line;while((line=r.readLine())!=null&&s.length()<30000)s.append(line).append("\n");new AlertDialog.Builder(this).setTitle("テキスト").setMessage(s.toString()).setPositiveButton("閉じる",null).show();}catch(Exception e){toast("ファイルを読み込めませんでした");}}else toast("選択したファイル: "+name);}}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
}
