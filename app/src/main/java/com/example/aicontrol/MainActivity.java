package com.example.aicontrol;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int SPEECH_REQ = 100;
    TextView tvStatus, tvTitle, tvSub;
    Button btnVoice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityCompat.requestPermissions(this,
            new String[]{Manifest.permission.RECORD_AUDIO}, 1);

        // Root layout — dark gradient style
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setBackgroundColor(Color.parseColor("#1A1A2E"));
        root.setPadding(60, 60, 60, 60);

        // Title
        tvTitle = new TextView(this);
        tvTitle.setText("AI CONTROL");
        tvTitle.setTextSize(32f);
        tvTitle.setTextColor(Color.parseColor("#6C63FF"));
        tvTitle.setGravity(Gravity.CENTER);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(tvTitle);

        // Subtitle
        tvSub = new TextView(this);
        tvSub.setText("Voice-powered phone assistant");
        tvSub.setTextSize(14f);
        tvSub.setTextColor(Color.parseColor("#B0B0C0"));
        tvSub.setGravity(Gravity.CENTER);
        tvSub.setPadding(0, 10, 0, 60);
        root.addView(tvSub);

        // Mic button
        btnVoice = new Button(this);
        btnVoice.setText("🎤  TAP TO SPEAK");
        btnVoice.setTextSize(18f);
        btnVoice.setTextColor(Color.WHITE);
        btnVoice.setBackgroundColor(Color.parseColor("#6C63FF"));
        btnVoice.setPadding(40, 30, 40, 30);
        btnVoice.setOnClickListener(v -> startListening());
        root.addView(btnVoice);

        // Status
        tvStatus = new TextView(this);
        tvStatus.setText("Say: \"WhatsApp kholo\" or \"Home jao\"");
        tvStatus.setTextSize(14f);
        tvStatus.setTextColor(Color.parseColor("#B0B0C0"));
        tvStatus.setGravity(Gravity.CENTER);
        tvStatus.setPadding(0, 60, 0, 20);
        root.addView(tvStatus);

        // Enable service hint
        TextView hint = new TextView(this);
        hint.setText("⚠ First enable: Settings → Accessibility → AI Control");
        hint.setTextSize(11f);
        hint.setTextColor(Color.parseColor("#FF6584"));
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(0, 30, 0, 0);
        root.addView(hint);

        setContentView(root);
    }

    private void startListening() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...");
        try {
            startActivityForResult(intent, SPEECH_REQ);
        } catch (Exception e) {
            Toast.makeText(this, "Voice not supported", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SPEECH_REQ && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                String text = results.get(0);
                tvStatus.setText("You said: " + text);
                processCommand(text);
            }
        }
    }

    private void processCommand(String text) {
        AIHelper.parseCommand(text, new AIHelper.Callback() {
            @Override public void onResult(String action, String appName) {
                runOnUiThread(() -> executeAction(action, appName));
            }
            @Override public void onError(String err) {
                runOnUiThread(() -> tvStatus.setText("Error: " + err));
            }
        });
    }

    private void executeAction(String action, String appName) {
        AIControlService svc = AIControlService.getInstance();
        if (svc == null) {
            tvStatus.setText("⚠ Enable Accessibility: Settings → Accessibility → AI Control");
            return;
        }

        switch (action) {
            case "open_app":
                String pkg = getPackageNameFromApp(appName);
                if (pkg != null) {
                    svc.openApp(pkg);
                    tvStatus.setText("✅ Opening: " + appName);
                } else {
                    tvStatus.setText("❌ App not found: " + appName);
                }
                break;
            case "go_home":
                svc.goHome();
                tvStatus.setText("🏠 Going home");
                break;
            case "back":
                svc.pressBack();
                tvStatus.setText("⬅ Back");
                break;
            case "recents":
                svc.showRecents();
                tvStatus.setText("📱 Recents");
                break;
            default:
                tvStatus.setText("🤔 Didn't understand");
        }
    }

    private String getPackageNameFromApp(String appName) {
        String l = appName.toLowerCase(Locale.ROOT);
        if (l.contains("whatsapp")) return "com.whatsapp";
        if (l.contains("youtube")) return "com.google.android.youtube";
        if (l.contains("instagram")) return "com.instagram.android";
        if (l.contains("facebook")) return "com.facebook.katana";
        if (l.contains("chrome")) return "com.android.chrome";
        if (l.contains("camera")) return "com.android.camera";
        if (l.contains("settings")) return "com.android.settings";
        if (l.contains("gallery")) return "com.android.gallery3d";
        if (l.contains("gmail")) return "com.google.android.gm";
        if (l.contains("telegram")) return "org.telegram.messenger";
        return null;
    }
          }
