package com.tibelian.gangaphone.messenger;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateUtils;
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
import com.tibelian.gangaphone.database.DatabaseManager;
import com.tibelian.gangaphone.database.model.Chat;

import java.util.List;

public class ChatListActivity extends AppCompatActivity {

    private RecyclerView mPostsRecyclerView;
    private PostListAdapter mPostAdapter;

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

        // load messages form db
        loadChats();
    }


    public void loadChats() {
        // obtain msg
        List<Chat> posts = DatabaseManager.get(this).getChats();
        mPostAdapter.setPosts(posts);
    }

    private class PostListAdapter extends RecyclerView.Adapter<PostListAdapter.ViewHolder> {

        private List<Chat> mPosts;
        public void setPosts(List<Chat> posts) {
            mPosts = posts;
        }

        @NonNull
        @Override
        public PostListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new PostListAdapter.ViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.list_item_chat, parent, false)
            );
        }

        @Override
        public void onBindViewHolder(PostListAdapter.ViewHolder viewHolder, final int position) {
            viewHolder.chat = ( (Chat) (mPosts.get(position)) );
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

                // on click item show product details
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(ChatActivity.newIntent(
                                ChatListActivity.this, chat.getUser().getUsername()));
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
                String timeAgo = DateUtils.getRelativeTimeSpanString(
                        chat.getLastDate().getTime()).toString();

                // bind data
                mDate.setText(timeAgo);
                mRecipient.setText(chat.getUser().getUsername());
                mLastMsg.setText(chat.getLastMessage());
                mLastMsg.setTextColor(chat.isRead() ? Color.GRAY : Color.BLACK);
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
                // @todo user's product list
                //startActivity(new Intent(getContext(), UserProductListActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
