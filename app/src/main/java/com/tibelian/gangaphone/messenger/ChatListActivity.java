package com.tibelian.gangaphone.messenger;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tibelian.gangaphone.R;
import com.tibelian.gangaphone.Session;
import com.tibelian.gangaphone.api.JsonMapper;
import com.tibelian.gangaphone.api.RestApi;
import com.tibelian.gangaphone.database.model.Chat;
import com.tibelian.gangaphone.database.model.Message;
import com.tibelian.gangaphone.socket.SocketClient;
import com.tibelian.gangaphone.user.profile.ProductListActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChatListActivity extends AppCompatActivity {

    private static boolean active = false;

    private static RecyclerView mPostsRecyclerView;
    private static PostListAdapter mPostAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messenger);

        // bind xml elements
        mPostsRecyclerView = findViewById(R.id.posts_recycler);

        // init recycler
        mPostsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mPostAdapter = new PostListAdapter();
        mPostsRecyclerView.setAdapter(mPostAdapter);

        // load messages
        loadChats(false);
        checkWhoIsOnline();
    }

    public static void checkWhoIsOnline() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String ids = "";
                ArrayList<Chat> chats = Session.get().getUser().getChats();
                for (int i = 0; i < chats.size(); i++) {
                    if (i > 0) ids += ",";
                    ids += ""+chats.get(i).getUser().getId();
                }
                SocketClient.get().send("is_online\n"+ids);
            }
        }).start();
    }

    public static void loadChats(boolean fromDatabase) {
        try {
            if (fromDatabase)
                Session.get().getUser().setChats(JsonMapper.mapChats(
                        new RestApi().findMessages(Session.get().getUser().getId()),
                        Session.get().getUser()
                ));
            mPostAdapter.setPosts(Session.get().getUser().getChats());
            mPostAdapter.notifyDataSetChanged();
        } catch (IOException io) {
            Log.e("ChatListActivity", "loadChats error --> " + io);
        }
    }

    private class PostListAdapter extends RecyclerView.Adapter<PostListAdapter.ViewHolder> {

        private ArrayList<Chat> mPosts = new ArrayList<>();
        public void setPosts(List<Chat> posts) {
            mPosts.clear();
            for(Chat c:posts) mPosts.add(c);
        }

        @NonNull
        @Override
        public PostListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Log.d("ChatListActivity", "executing onCreateViewHolder");
            return new PostListAdapter.ViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.list_item_chat, parent, false)
            );
        }

        @Override
        public void onBindViewHolder(PostListAdapter.ViewHolder viewHolder, final int position) {
            viewHolder.chat = ( (Chat) (mPosts.get(position)) );
            Log.d("ChatListActivity", "onBindViewHolder num chats --> " + mPosts.size());
            viewHolder.bind();
        }

        @Override
        public int getItemCount() {
            return mPosts.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public Chat chat;
            private TextView mRecipient;
            private TextView mLastMsg;
            private TextView mDate;
            private TextView mOnline;

            public ViewHolder(View v) {
                super(v);
                Log.d("ChatListActivity", "executing ViewHolder constructor");
                // on click item show product details
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(ChatActivity.newIntent(
                                ChatListActivity.this, chat.getUser().getId()));
                    }
                });

                // bind xml textview
                mRecipient = v.findViewById(R.id.chatRecipient);
                mDate = v.findViewById(R.id.chatDate);
                mLastMsg = v.findViewById(R.id.chatLastMsg);
                mOnline = v.findViewById(R.id.chatOnline);

            }

            public void bind()
            {

                Message lastMsg = chat.getMessages().get(chat.getMessages().size() - 1);
                String timeAgo = DateUtils.getRelativeTimeSpanString(
                        lastMsg.getDate().getTime()).toString();

                // bind data
                mDate.setText(timeAgo);
                mRecipient.setText(chat.getUser().getUsername());
                mLastMsg.setText(lastMsg.getContent());
                mLastMsg.setTextColor(lastMsg.isRead() ? Color.GRAY : Color.BLACK);
                mOnline.setText(chat.getUser().isOnline() ? R.string.online : R.string.offline);
                mOnline.setAllCaps(false);
            }

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_main, menu);
        menu.findItem(R.id.menu_msg).setVisible(false);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_user:
                startActivity(new Intent(ChatListActivity.this, ProductListActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }




    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }
    @Override
    public void onStop() {
        super.onStop();
        active = false;
    }
    public static boolean isActive() {
        return active;
    }

}
