package com.exhxx78.ping;

import android.app.Activity;
import android.graphics.Color;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        ScrollView scroll = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 60, 40, 40);
        layout.setBackgroundColor(Color.parseColor("#121212"));

        TextView title = new TextView(this);
        title.setText("Exhxx Ping Master 🌐\nالمطور: حيدر عادل | @exhxx78");
        title.setTextColor(Color.CYAN);
        title.setTextSize(20);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 40);

        targetInput = new EditText(this);
        targetInput.setHint("اكتب IP أو الدومين (مثال: google.com)");
        targetInput.setHintTextColor(Color.GRAY);
        targetInput.setTextColor(Color.WHITE);
        targetInput.setBackgroundColor(Color.parseColor("#1E1E1E"));
        targetInput.setPadding(30, 30, 30, 30);
        targetInput.setTextSize(16);

        LinearLayout quickBtnLayout = new LinearLayout(this);
        quickBtnLayout.setOrientation(LinearLayout.HORIZONTAL);
        quickBtnLayout.setPadding(0, 20, 0, 20);
        
        Button btnGoogle = new Button(this);
        btnGoogle.setText("Google DNS");
        btnGoogle.setBackgroundColor(Color.parseColor("#1E88E5"));
        btnGoogle.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        param.setMargins(0, 0, 10, 0);
        btnGoogle.setLayoutParams(param);

        Button btnCloudflare = new Button(this);
        btnCloudflare.setText("Cloudflare");
        btnCloudflare.setBackgroundColor(Color.parseColor("#F57C00"));
        btnCloudflare.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams param2 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        param2.setMargins(10, 0, 0, 0);
        btnCloudflare.setLayoutParams(param2);

        quickBtnLayout.addView(btnGoogle);
        quickBtnLayout.addView(btnCloudflare);

        btnStart = new Button(this);
        btnStart.setText("▶️ بـدء الفـحـص");
        btnStart.setBackgroundColor(Color.parseColor("#43A047"));
        btnStart.setTextColor(Color.WHITE);
        btnStart.setPadding(0, 30, 0, 30);

        btnStop = new Button(this);
        btnStop.setText("⏹️ إيقـاف الفـحـص");
        btnStop.setBackgroundColor(Color.parseColor("#E53935"));
        btnStop.setTextColor(Color.WHITE);
        btnStop.setPadding(0, 30, 0, 30);
        btnStop.setEnabled(false);
        btnStop.setAlpha(0.5f);

        livePingText = new TextView(this);
        livePingText.setText("0 ms");
        livePingText.setTextColor(Color.GRAY);
        livePingText.setTextSize(45);
        livePingText.setGravity(Gravity.CENTER);
        livePingText.setPadding(0, 40, 0, 20);

        statsText = new TextView(this);
        statsText.setText("جاهز للفحص...\nMin: 0 | Max: 0 | Avg: 0\nLoss: 0%");
        statsText.setTextColor(Color.WHITE);
        statsText.setTextSize(16);
        statsText.setGravity(Gravity.CENTER);
        statsText.setBackgroundColor(Color.parseColor("#1E1E1E"));
        statsText.setPadding(30, 30, 30, 30);

        layout.addView(title);
        layout.addView(targetInput);
        layout.addView(quickBtnLayout);
        
        TextView space = new TextView(this); space.setHeight(20); layout.addView(space);
        
        layout.addView(btnStart);
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
            Toast.makeText(this, "يرجى كتابة IP أو دومين!", Toast.LENGTH_SHORT).show();
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
                        
                        if (finalPing <= 80) livePingText.setTextColor(Color.parseColor("#43A047"));
                        else if (finalPing <= 150) livePingText.setTextColor(Color.parseColor("#FDD835"));
                        else livePingText.setTextColor(Color.parseColor("#E53935"));
                        
                    } else {
                        lostCount++;
                        livePingText.setText("Request Timeout");
                        livePingText.setTextColor(Color.parseColor("#E53935"));
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
        livePingText.setTextColor(Color.GRAY);
    }

    private void updateStatsDisplay() {
        double avgPing = (pingCount - lostCount > 0) ? (totalPing / (pingCount - lostCount)) : 0;
        int lossPercentage = (int) (((double) lostCount / pingCount) * 100);
        
        String minStr = (minPing == Double.MAX_VALUE) ? "0" : String.format("%.1f", minPing);
        
        String stats = String.format("الطلبات: %d | المفقود: %d\nMin: %s | Max: %.1f | Avg: %.1f\nLoss: %d%%", 
                pingCount, lostCount, minStr, maxPing, avgPing, lossPercentage);
                
        statsText.setText(stats);
        if (lossPercentage > 0) statsText.setTextColor(Color.parseColor("#EF5350"));
        else statsText.setTextColor(Color.WHITE);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPing();
    }
}
