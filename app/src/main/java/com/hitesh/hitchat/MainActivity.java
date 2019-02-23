package com.hitesh.hitchat;


import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import ai.api.AIDataService;
import ai.api.AIListener;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;


public class MainActivity extends AppCompatActivity implements AIListener {

    private RecyclerView recyclerView;
    private EditText editText;
    private ImageView send;
    private DatabaseReference reference;
    private FirebaseRecyclerAdapter<ChatMessage, Chat_rec> adapter;
    private Boolean flagFab = true;
    private AIService aiService;
    private AIDataService aiDataService;
    private AIRequest aiRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpView();
        setUpAPI();
    }

    private void setUpAPI() {
        final AIConfiguration configuration = new AIConfiguration(Constants.API_CLIENT_ACCESS_TOKEN,
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        aiService = AIService.getService(this, configuration);
        aiService.setListener(this);

        aiDataService = new AIDataService(configuration);
        aiRequest = new AIRequest();


    }

    private void setUpView() {
        recyclerView = findViewById(R.id.rv_chat);

        editText = findViewById(R.id.et_msg);

        send = findViewById(R.id.iv_send);
        send.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onClick(View v) {
                String msg = editText.getText().toString().trim();
                if (!msg.isEmpty()) {
                    Log.d("@ Count", String.valueOf(adapter.getItemCount()));
                    Log.d("@ChatMSG", msg);
                    final AIRequest request = aiRequest;
                    ChatMessage chatMessage = new ChatMessage(msg, Constants.USER);
                    reference.child("chat").push().setValue(chatMessage);
                    aiRequest.setQuery(msg);
                    new AsyncTask<AIRequest, Void, AIResponse>() {
                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                        }

                        @Override
                        protected AIResponse doInBackground(AIRequest... aiRequests) {
                            final AIRequest request = aiRequests[0];
                            try {
                                final AIResponse aiResponse = aiDataService.request(request);
                                return aiResponse;

                            } catch (AIServiceException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(AIResponse aiResponse) {
                            if (aiResponse != null) {
                                Result result = aiResponse.getResult();
                                String reply = result.getFulfillment().getSpeech();
                                ChatMessage replyMsg = new ChatMessage(reply, Constants.BOT);
                                Log.d("@ChatReply", reply);
                                reference.child("chat").push().setValue(replyMsg);
                            }
                        }
                    }.execute(request);
                } else
                    Toast.makeText(MainActivity.this, "Please type something", Toast.LENGTH_SHORT).show();

                editText.setText("");
            }
        });

        recyclerView.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        reference = FirebaseDatabase.getInstance().getReference();
        reference.keepSynced(true);

        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("chat");
        FirebaseRecyclerOptions<ChatMessage> options =
                new FirebaseRecyclerOptions.Builder<ChatMessage>()
                        .setQuery(query, ChatMessage.class)
                        .build();
        adapter = new FirebaseRecyclerAdapter<ChatMessage, Chat_rec>(options) {

            @NonNull
            @Override
            public Chat_rec onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.item_msg, viewGroup, false);

                return new Chat_rec(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull Chat_rec holder, int position, @NonNull ChatMessage model) {
                Log.d("@ RV", String.valueOf(position));
                if (model.getMsgUser().equals(Constants.USER)) {


                    holder.user_msg.setText(model.getMsgText());

                    holder.user_msg.setVisibility(View.VISIBLE);
                    holder.bot_ans.setVisibility(View.GONE);
                } else {
                    holder.bot_ans.setText(model.getMsgText());

                    holder.user_msg.setVisibility(View.GONE);
                    holder.bot_ans.setVisibility(View.VISIBLE);
                }


            }


        };


        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);

                int msgCount = adapter.getItemCount();
                int lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();

                if (lastVisiblePosition == -1 ||
                        (positionStart >= (msgCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    recyclerView.scrollToPosition(positionStart);

                }

            }
        });


        recyclerView.setAdapter(adapter);


    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onResult(AIResponse result) {

    }

    @Override
    public void onError(AIError error) {

    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {

    }

    @Override
    public void onListeningCanceled() {

    }

    @Override
    public void onListeningFinished() {

    }
}
