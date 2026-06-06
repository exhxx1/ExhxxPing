package com.exhxx78.ping;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MainActivity extends Activity {
    private EditText targetInput;
    private TextView livePingText, statsText;
    private Button btnStart, btnStop;
    
    private boolean isPinging = false;
    private Thread pingThread;
    
    private int pingCount = 0;
    private int lostCount = 0;
    private double minPing = Double.MAX_VALUE;
    private double maxPing = 0;
    private double totalPing = 0;

    // دالة لصنع أزرار بحواف دائرية احترافية
    private GradientDrawable getRoundedBg(String colorStr, float radius) {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(radius);
        shape.setColor(Color.parseColor(colorStr));
        return shape;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        ScrollView scroll = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 80, 50, 50);
        layout.setBackgroundColor(Color.parseColor("#0F172A")); // لون ليلي عصري (Slate)

        Typeface modernFont = Typeface.create("sans-serif-medium", Typeface.NORMAL);
        Typeface boldFont = Typeface.create("sans-serif-black", Typeface.NORMAL);

        // العنوان والحقوق
        TextView title = new TextView(this);
        title.setText("فحص البنج\nالمطور: محمد عدنان | @exhxx78");
        title.setTextColor(Color.parseColor("#38BDF8")); // أزرق سماوي
        title.setTextSize(22);
        title.setTypeface(boldFont);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 60);

        // حقل الإدخال
        targetInput = new EditText(this);
        targetInput.setHint("الهدف (مثال: 8.8.8.8)");
        targetInput.setHintTextColor(Color.parseColor("#64748B"));
        targetInput.setTextColor(Color.WHITE);
        targetInput.setBackground(getRoundedBg("#1E293B", 25f));
        targetInput.setPadding(40, 40, 40, 40);
        targetInput.setTextSize(16);
        targetInput.setTypeface(modernFont);

        // أزرار سريعة
        LinearLayout quickBtnLayout = new LinearLayout(this);
        quickBtnLayout.setOrientation(LinearLayout.HORIZONTAL);
        quickBtnLayout.setPadding(0, 30, 0, 30);
        
        Button btnGoogle = new Button(this);
        btnGoogle.setText("Google");
        btnGoogle.setBackground(getRoundedBg("#0284C7", 20f));
        btnGoogle.setTextColor(Color.WHITE);
        btnGoogle.setTypeface(modernFont);
        btnGoogle.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_search, 0, 0, 0);
        btnGoogle.setPadding(20, 20, 20, 20);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        param.setMargins(0, 0, 15, 0);
        btnGoogle.setLayoutParams(param);

        Button btnCloudflare = new Button(this);
        btnCloudflare.setText("Cloudflare");
        btnCloudflare.setBackground(getRoundedBg("#EA580C", 20f));
        btnCloudflare.setTextColor(Color.WHITE);
        btnCloudflare.setTypeface(modernFont);
        btnCloudflare.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_compass, 0, 0, 0);
        btnCloudflare.setPadding(20, 20, 20, 20);
        LinearLayout.LayoutParams param2 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        param2.setMargins(15, 0, 0, 0);
        btnCloudflare.setLayoutParams(param2);

        quickBtnLayout.addView(btnGoogle);
        quickBtnLayout.addView(btnCloudflare);

        // أزرار التحكم
        btnStart = new Button(this);
        btnStart.setText(" بدء الفحص");
        btnStart.setBackground(getRoundedBg("#16A34A", 25f));
        btnStart.setTextColor(Color.WHITE);
        btnStart.setTypeface(boldFont);
        btnStart.setTextSize(16);
        // إضافة أيقونة Play حقيقية من النظام
        btnStart.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_media_play, 0, 0, 0);
        btnStart.setPadding(40, 30, 40, 30);

        TextView spaceBtn = new TextView(this); spaceBtn.setHeight(20);

        btnStop = new Button(this);
        btnStop.setText(" إيقاف الفحص");
        btnStop.setBackground(getRoundedBg("#DC2626", 25f));
        btnStop.setTextColor(Color.WHITE);
        btnStop.setTypeface(boldFont);
        btnStop.setTextSize(16);
        // إضافة أيقونة Pause حقيقية من النظام
        btnStop.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_media_pause, 0, 0, 0);
        btnStop.setPadding(40, 30, 40, 30);
        btnStop.setEnabled(false);
        btnStop.setAlpha(0.5f);

        // شاشة البنج المباشر
        livePingText = new TextView(this);
        livePingText.setText("0 ms");
        livePingText.setTextColor(Color.parseColor("#475569"));
        livePingText.setTextSize(55);
        livePingText.setTypeface(boldFont);
        livePingText.setGravity(Gravity.CENTER);
        livePingText.setPadding(0, 60, 0, 40);

        // لوحة الإحصائيات الدقيقة
        statsText = new TextView(this);
        statsText.setText("جاهز للفحص...\nMin: 0 | Max: 0 | Avg: 0\nLoss: 0%");
        statsText.setTextColor(Color.parseColor("#94A3B8"));
        statsText.setTextSize(16);
        statsText.setTypeface(modernFont);
        statsText.setGravity(Gravity.CENTER);
        statsText.setBackground(getRoundedBg("#1E293B", 30f));
        statsText.setPadding(40, 50, 40, 50);

        layout.addView(title);
        layout.addView(targetInput);
        layout.addView(quickBtnLayout);
        
        TextView space = new TextView(this); space.setHeight(20); layout.addView(space);
        
        layout.addView(btnStart);
        layout.addView(spaceBtn);
        layout.addView(btnStop);
        layout.addView(livePingText);
        layout.addView(statsText);
        
        scroll.addView(layout);
        setContentView(scroll);

        btnGoogle.setOnClickListener(v -> targetInput.setText("8.8.8.8"));
        btnCloudflare.setOnClickListener(v -> targetInput.setText("1.1.1.1"));
        
        btnStart.setOnClickListener(v -> startPing());
        btnStop.setOnClickListener(v -> stopPing());
    }

    private void startPing() {
        String target = targetInput.getText().toString().trim();
        if (target.isEmpty()) {
            Toast.makeText(this, "يرجى تحديد الهدف أولاً!", Toast.LENGTH_SHORT).show();
            return;
        }

        pingCount = 0; lostCount = 0;
        minPing = Double.MAX_VALUE; maxPing = 0; totalPing = 0;
        
        isPinging = true;
        btnStart.setEnabled(false); btnStart.setAlpha(0.5f);
        btnStop.setEnabled(true); btnStop.setAlpha(1.0f);
        targetInput.setEnabled(false);

        pingThread = new Thread(() -> {
            while (isPinging) {
                pingCount++;
                double currentPing = -1;
                
                try {
                    Process p = Runtime.getRuntime().exec("ping -c 1 -W 1 " + target);
                    BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.contains("time=")) {
                            int startIndex = line.indexOf("time=") + 5;
                            int endIndex = line.indexOf(" ms", startIndex);
                            currentPing = Double.parseDouble(line.substring(startIndex, endIndex));
                            break;
                        }
                    }
                    p.waitFor();
                } catch (Exception e) { currentPing = -1; }

                final double finalPing = currentPing;
                
                runOnUiThread(() -> {
                    if (finalPing != -1) {
                        if (finalPing < minPing) minPing = finalPing;
                        if (finalPing > maxPing) maxPing = finalPing;
                        totalPing += finalPing;
                        
                        livePingText.setText(String.format("%.1f ms", finalPing));
                        
                        // ألوان نيون عصرية للبنج
                        if (finalPing <= 80) livePingText.setTextColor(Color.parseColor("#22C55E")); // أخضر
                        else if (finalPing <= 150) livePingText.setTextColor(Color.parseColor("#EAB308")); // أصفر
                        else livePingText.setTextColor(Color.parseColor("#EF4444")); // أحمر
                        
                    } else {
                        lostCount++;
                        livePingText.setText("Timeout");
                        livePingText.setTextColor(Color.parseColor("#EF4444"));
                    }
                    updateStatsDisplay();
                });

                try { Thread.sleep(1000); } catch (InterruptedException e) { break; }
            }
        });
        pingThread.start();
    }

    private void stopPing() {
        isPinging = false;
        if (pingThread != null) pingThread.interrupt();
        
        btnStart.setEnabled(true); btnStart.setAlpha(1.0f);
        btnStop.setEnabled(false); btnStop.setAlpha(0.5f);
        targetInput.setEnabled(true);
        livePingText.setTextColor(Color.parseColor("#475569"));
    }

    private void updateStatsDisplay() {
        double avgPing = (pingCount - lostCount > 0) ? (totalPing / (pingCount - lostCount)) : 0;
        int lossPercentage = (int) (((double) lostCount / pingCount) * 100);
        
        String minStr = (minPing == Double.MAX_VALUE) ? "0" : String.format("%.1f", minPing);
        
        String stats = String.format("الطلبات: %d  |  المفقود: %d\nMin: %s  |  Max: %.1f  |  Avg: %.1f\nLoss: %d%%", 
                pingCount, lostCount, minStr, maxPing, avgPing, lossPercentage);
                
        statsText.setText(stats);
        if (lossPercentage > 0) statsText.setTextColor(Color.parseColor("#F87171"));
        else statsText.setTextColor(Color.parseColor("#94A3B8"));
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPing();
    }
}
