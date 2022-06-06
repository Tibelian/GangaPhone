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
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tibelian.gangaphone.R;
import com.tibelian.gangaphone.Session;
import com.tibelian.gangaphone.utils.JsonMapper;
import com.tibelian.gangaphone.api.RestApi;
import com.tibelian.gangaphone.database.model.Chat;
import com.tibelian.gangaphone.database.model.Message;
import com.tibelian.gangaphone.messenger.socket.SocketClient;
import com.tibelian.gangaphone.user.profile.ProductListActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Create messenger list of chats
 */
public class ChatListActivity extends AppCompatActivity {

    // control var to detect if current activity is active
    private static boolean active = false;

    // save this context
    private static ChatListActivity context = null;

    // fragment
    private static View mMessageFrame;

    // const to attach intent data
    private static final String EXTRA_USER_ID = "current_user_id";

    // memeber variables, xml elements
    private static RecyclerView mPostsRecyclerView;
    private static PostListAdapter mPostAdapter;

    /**
     * init activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messenger);

        // bind xml elements
        mPostsRecyclerView = findViewById(R.id.posts_recycler);
        mMessageFrame = findViewById(R.id.messenger_large_currentChat);

        // init recycler
        mPostsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mPostAdapter = new PostListAdapter();
        mPostsRecyclerView.setAdapter(mPostAdapter);

        // if screen size is large
        if (mMessageFrame != null) {
            Log.i("ChatListActivity", "onCreate large screen detected");
            int uid = getIntent().getIntExtra(EXTRA_USER_ID, 0);
            if (uid == 0) uid = Session.get().getUser().getChats().get(0).getUser().getId();
            loadMessageFragment(uid);
        }

        // load messages
        loadChats(false);
        checkWhoIsOnline();

        getSupportActionBar().setSubtitle(R.string.messenger_subtitle);
    }

    /**
     * show selected chat
     * @param uid
     */
    private void loadMessageFragment(int uid) {
        MessageFragment msgFragment = MessageFragment.newInstance(uid);
        FragmentManager fManager = getSupportFragmentManager();
        fManager.beginTransaction()
                .replace(mMessageFrame.getId(), msgFragment)
                .commit()
        ;
    }

    /**
     * detect who is online
     * ask tcp server on new thread
     */
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

    /**
     * Obtain chats from database or session
     * @param fromDatabase
     */
    public void loadChats(boolean fromDatabase) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    // ask api for messages
                    if (fromDatabase)
                        Session.get().getUser().setChats(JsonMapper.mapChats(
                                new RestApi().findMessages(Session.get().getUser().getId()),
                                Session.get().getUser()
                        ));
                    // update the adapater
                    mPostAdapter.setPosts(Session.get().getUser().getChats());
                    mPostAdapter.notifyDataSetChanged();
                } catch (IOException io) {
                    Log.e("ChatListActivity", "loadChats error --> " + io);
                }
            }
        });
    }

    /**
     * custom list of chats
     */
    private class PostListAdapter extends RecyclerView.Adapter<PostListAdapter.ViewHolder> {

        // full list to show
        private ArrayList<Chat> mPosts = new ArrayList<>();

        // update list
        public void setPosts(List<Chat> posts) {
            mPosts.clear();
            for(Chat c:posts) mPosts.add(c);
        }

        /**
         * load layout
         * @param parent
         * @param viewType
         * @return
         */
        @NonNull
        @Override
        public PostListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Log.d("ChatListActivity", "executing onCreateViewHolder");
            return new PostListAdapter.ViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.list_item_chat, parent, false)
            );
        }

        /**
         * set data
         * @param viewHolder
         * @param position
         */
        @Override
        public void onBindViewHolder(PostListAdapter.ViewHolder viewHolder, final int position) {
            viewHolder.chat = ( (Chat) (mPosts.get(position)) );
            Log.d("ChatListActivity", "onBindViewHolder num chats --> " + mPosts.size());
            viewHolder.bind();
        }

        /**
         * number of chats
         * @return int
         */
        @Override
        public int getItemCount() {
            return mPosts.size();
        }

        /**
         * custom view
         */
        public class ViewHolder extends RecyclerView.ViewHolder {

            // current chat
            public Chat chat;
            private TextView mRecipient;
            private TextView mLastMsg;
            private TextView mDate;
            private TextView mOnline;

            /**
             * constructor
             * @param v
             */
            public ViewHolder(View v) {
                super(v);
                // on click item show product details
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // if fragment doesn't exits
                        // that mean's we need to call the activity
                        if (mMessageFrame == null )
                            startActivity(ChatActivity.newIntent(
                                    ChatListActivity.this, chat.getUser().getId()));
                        else
                            loadMessageFragment(chat.getUser().getId());
                    }
                });

                // bind xml textview
                mRecipient = v.findViewById(R.id.chatRecipient);
                mDate = v.findViewById(R.id.chatDate);
                mLastMsg = v.findViewById(R.id.chatLastMsg);
                mOnline = v.findViewById(R.id.chatOnline);

            }

            /**
             * set data to the visual elements
             */
            public void bind()
            {
                // obtain last message content
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

    /**
     * modify menu options layout
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_main, menu);
        menu.findItem(R.id.menu_msg).setVisible(false);
        return true;
    }

    /**
     * Event on click menu's item
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            // open profile
            case R.id.menu_user:
                startActivity(new Intent(ChatListActivity.this, ProductListActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * set current activity as active
     * and also set the context
     */
    @Override
    public void onStart() {
        super.onStart();
        active = true;
        context = this;
    }

    /**
     * unset the active and context variables
     */
    @Override
    public void onStop() {
        super.onStop();
        active = false;
        context = null;
    }

    /**
     * getter static context
     * @return ChatListActivity
     */
    public static ChatListActivity getContext() {
        return context;
    }

    /**
     * getter static active boolean
     * @return boolean
     */
    public static boolean isActive() {
        return active;
    }

}
