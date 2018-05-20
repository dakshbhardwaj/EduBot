package com.dakshbhardwaj.bot;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;


import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import ai.api.AIListener;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIDataService;
import ai.api.android.AIService;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;
import com.google.gson.JsonElement;
import java.util.Map;

import android.Manifest;
public class MainActivity extends AppCompatActivity implements AIListener {

    RecyclerView recyclerView;
    EditText editText;
    RelativeLayout addBtn;
    DatabaseReference ref;
    FirebaseRecyclerAdapter<ChatMessage, chat_rec> adapter;
    Boolean flagFab = true;
    private FirebaseAuth firebaseAuth;

    private AIService aiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);


        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        editText = (EditText) findViewById(R.id.editText);
        addBtn = (RelativeLayout) findViewById(R.id.addBtn);

        recyclerView.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        ref = FirebaseDatabase.getInstance().getReference();
        ref.keepSynced(true);


        final AIConfiguration config = new AIConfiguration("9c7331f6c08a412aa8d275af28e5ae86",
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        aiService = AIService.getService(this, config);
        aiService.setListener((AIListener) this);

        final AIDataService aiDataService = new AIDataService(this, config);

        final AIRequest aiRequest = new AIRequest();


        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message = editText.getText().toString().trim();

                if (!message.equals("")) {

                    ChatMessage chatMessage = new ChatMessage(message, "user");
                    ref.child("chat").push().setValue(chatMessage);

                    aiRequest.setQuery(message);
                    new AsyncTask<AIRequest, Void, AIResponse>() {

                        @Override
                        protected AIResponse doInBackground(AIRequest... aiRequests) {
                            final AIRequest request = aiRequests[0];
                            try {
                                final AIResponse response = aiDataService.request(aiRequest);
                                return response;
                            } catch (AIServiceException e) {
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(AIResponse response) {
                            if (response != null) {

                                Result result = response.getResult();
                                String reply = result.getFulfillment().getSpeech();
                                ChatMessage chatMessage = new ChatMessage(reply, "bot");
                                ref.child("chat").push().setValue(chatMessage);
                            }
                        }
                    }.execute(aiRequest);
                } else {
                    aiService.startListening();
                }

                editText.setText("");

            }
        });


        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ImageView fab_img = (ImageView) findViewById(R.id.fab_img);
                Bitmap img = BitmapFactory.decodeResource(getResources(), R.drawable.ic_send_white_24dp);
                Bitmap img1 = BitmapFactory.decodeResource(getResources(), R.drawable.ic_mic_white_24dp);

                if (s.toString().trim().length() != 0 && flagFab) {
                    ImageViewAnimatedChange(MainActivity.this, fab_img, img);
                    flagFab = false;
                } else if (s.toString().trim().length() == 0) {
                    ImageViewAnimatedChange(MainActivity.this, fab_img, img1);
                    flagFab = true;
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        adapter = new FirebaseRecyclerAdapter<ChatMessage, chat_rec>(ChatMessage.class, R.layout.msglist, chat_rec.class, ref.child("chat")) {
            @Override
            protected void populateViewHolder(chat_rec viewHolder, ChatMessage model, int position) {

                if (model.getMsgUser().equals("user")) {
                    viewHolder.rightText.setText(model.getMsgText());
                    viewHolder.rightText.setVisibility(View.VISIBLE);
                    viewHolder.leftText.setVisibility(View.GONE);
                } else {
                    viewHolder.leftText.setText(model.getMsgText());
                    viewHolder.rightText.setVisibility(View.GONE);
                    viewHolder.leftText.setVisibility(View.VISIBLE);
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

    public void ImageViewAnimatedChange(Context c, final ImageView v, final Bitmap new_image) {
        final Animation anim_out = AnimationUtils.loadAnimation(c, R.anim.zoom_out);
        final Animation anim_in = AnimationUtils.loadAnimation(c, R.anim.zoom_in);
        anim_out.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationRepeat(Animation animation) { }
            @Override
            public void onAnimationEnd(Animation animation) {
                v.setImageBitmap(new_image);
                anim_in.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) { }
                    @Override
                    public void onAnimationRepeat(Animation animation) { }
                    @Override
                    public void onAnimationEnd(Animation animation) {   }
                });
                v.startAnimation(anim_in);
            }
        });
        v.startAnimation(anim_out);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){

        getMenuInflater().inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);


    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        switch (item.getItemId()){

            case R.id.sign_out:
                firebaseAuth=FirebaseAuth.getInstance();
                firebaseAuth.signOut();
                //closing activity
                finish();
                //starting login activity
                startActivity(new Intent(this, LoginActivity.class));

            case R.id.about_us:
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setCancelable(false);
                dialog.setTitle("About us");
                dialog.setMessage("Edu-Bot is an Android Application which provides answer to the query of students. Students can enter their queries in natural language either in" +
                        " text format or with voice-to-text feature provided." +
                        " \n \n" + "Daksh(151276)\n" + "Devansh Srivastava (151281)\n" + "Siddhant Mudgal(151412)");
                dialog.setPositiveButton("Ok Got it!!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                        .setNegativeButton(" ", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });

                final AlertDialog alert = dialog.create();
                alert.show();
                return true;
            case R.id.contact_us:
                AlertDialog.Builder dialogs = new AlertDialog.Builder(MainActivity.this);
                dialogs.setCancelable(false);
                dialogs.setTitle("Contact us");
                dialogs.setMessage("Daksh -" +"dakshbhardwaj2@gmail.com "+
                        "Devansh -"+ "devanshsri1998@gmail.com " +
                            "Siddhant -" + "sidmudgal21@gmail.com ");
                dialogs.setPositiveButton("Ok Got it!!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                        .setNegativeButton(" ", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });

                final AlertDialog alerts = dialogs.create();
                alerts.show();
                return true;


        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onResult(ai.api.model.AIResponse response) {


        Result result = response.getResult();

        String message = result.getResolvedQuery();
        ChatMessage chatMessage0 = new ChatMessage(message, "user");
        ref.child("chat").push().setValue(chatMessage0);


        String reply = result.getFulfillment().getSpeech();
        ChatMessage chatMessage = new ChatMessage(reply, "bot");
        ref.child("chat").push().setValue(chatMessage);

    }

    @Override
    public void onError(ai.api.model.AIError error) {
    }

    @Override
    public void onAudioLevel(float level) {
    }

    @Override
    public void onListeningStarted() {}

    @Override
    public void onListeningCanceled() {}

    @Override
    public void onListeningFinished() {}

}