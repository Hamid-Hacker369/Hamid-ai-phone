package com.example.aicontrol;

import okhttp3.*;
import org.json.JSONObject;
import java.io.IOException;

public class AIHelper {

    private static final String BACKEND_URL =
        "https://ai-phone-backend-5qxx.vercel.app/api/parse";

    public interface Callback {
        void onResult(String action, String appName);
        void onError(String err);
    }

    public static void parseCommand(String userText, Callback cb) {
        OkHttpClient client = new OkHttpClient();

        JSONObject body = new JSONObject();
        try {
            body.put("text", userText);
        } catch (Exception e) { cb.onError(e.getMessage()); return; }

        Request req = new Request.Builder()
            .url(BACKEND_URL)
            .post(RequestBody.create(body.toString(),
                MediaType.parse("application/json")))
            .build();

        client.newCall(req).enqueue(new okhttp3.Callback() {
            @Override public void onFailure(Call call, IOException e) {
                cb.onError(e.getMessage());
            }
            @Override public void onResponse(Call call, Response response) throws IOException {
                try {
                    String res = response.body().string();
                    JSONObject json = new JSONObject(res);
                    cb.onResult(json.optString("action"), json.optString("app"));
                } catch (Exception e) { cb.onError(e.getMessage()); }
            }
        });
    }
          }
