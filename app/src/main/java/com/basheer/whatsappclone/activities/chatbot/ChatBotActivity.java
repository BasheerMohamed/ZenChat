package com.basheer.whatsappclone.activities.chatbot;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.basheer.whatsappclone.adapter.ChatBotMessageAdapter;
import com.basheer.whatsappclone.databinding.ActivityChatBotBinding;
import com.basheer.whatsappclone.model.ChatBotMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatBotActivity extends AppCompatActivity {

    ActivityChatBotBinding binding;
    List<ChatBotMessage> messageList;
    ChatBotMessageAdapter messageAdapter;
    public static final okhttp3.MediaType JSON = MediaType.get("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBotBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        messageList = new ArrayList<>();

        messageAdapter = new ChatBotMessageAdapter(messageList);
        binding.messageContent.setAdapter(messageAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        binding.messageContent.setLayoutManager(llm);

        binding.sendButton.setOnClickListener((v)->{

            String question = binding.messageBox.getText().toString().trim();
            addToChat(question,ChatBotMessage.SENT_BY_ME);
            binding.messageBox.setText("");
            callAPI(question);

        });

        binding.backButton.setOnClickListener(v -> finish());

    }

    @SuppressLint("NotifyDataSetChanged")
    void addToChat(String message, String sentBy){

        runOnUiThread(() -> {

            messageList.add(new ChatBotMessage(message,sentBy));
            messageAdapter.notifyDataSetChanged();
            binding.messageContent.smoothScrollToPosition(messageAdapter.getItemCount());

        });

    }

    void addResponse(String response){
        messageList.remove(messageList.size()-1);
        addToChat(response,ChatBotMessage.SENT_BY_BOT);
    }

    void callAPI(String question){

        messageList.add(new ChatBotMessage("Typing... ",ChatBotMessage.SENT_BY_BOT));
        JSONObject jsonBody = new JSONObject();

        try {

            jsonBody.put("model","gpt-3.5-turbo-instruct");
            jsonBody.put("prompt",question);
            jsonBody.put("max_tokens",4000);
            jsonBody.put("temperature",0);

        } catch (JSONException ignored) { }

        RequestBody body = RequestBody.create(jsonBody.toString(),JSON);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/completions")
                .header("Authorization","Bearer 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")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

                addResponse("Failed to load response due to "+e.getMessage());

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                if(response.isSuccessful()){

                    JSONObject  jsonObject = null;
                    try {

                        assert response.body() != null;
                        jsonObject = new JSONObject(response.body().string());
                        JSONArray jsonArray = jsonObject.getJSONArray("choices");
                        String result = jsonArray.getJSONObject(0).getString("text");
                        addResponse(result.trim());

                    } catch(JSONException ignored) { }

                }else{

                    assert response.body() != null;
                    addResponse("Failed to load response due to "+ response.body());

                }

            }

        });

    }

}