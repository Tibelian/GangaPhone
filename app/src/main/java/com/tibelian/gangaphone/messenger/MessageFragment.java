package com.tibelian.gangaphone.messenger;

import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.tibelian.gangaphone.R;
import com.tibelian.gangaphone.Session;
import com.tibelian.gangaphone.api.RestApi;
import com.tibelian.gangaphone.database.model.Message;
import com.tibelian.gangaphone.database.model.User;
import com.tibelian.gangaphone.socket.SocketClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MessageFragment extends Fragment {

    private static final String ARG_USER_ID = "user_id";
    private int userId;
    private User mTarget;
    private boolean isNewTarget = false;

    private TextView mUsername;
    private TextView mStatus;
    private RecyclerView mMsgRecyclerView;
    private MsgListAdapter mMessagesAdapter;
    private EditText mInputEditText;
    private Button mSendButton;

    public static MessageFragment newInstance(int uid) {
        Bundle args = new Bundle();
        args.putInt(ARG_USER_ID, uid);
        MessageFragment fragment = new MessageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userId = getArguments().getInt(ARG_USER_ID, 0);

        try {
            mTarget = Session.get().getUser().getChatFrom(userId).getUser();
        } catch(NullPointerException ne) {

            // if user is not found on the previous chats
            // then obtain him from the database
            try {
                mTarget = new RestApi().findUserById(userId);
                isNewTarget = true;
            } catch (IOException e) {
                Log.e("MessageFragment", "onCreate error RestApi --> " + e);
            }
        }

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_messages, container,false);

        // load components
        mInputEditText = view.findViewById(R.id.chatInput);
        mSendButton = view.findViewById(R.id.chatSend);
        mUsername = view.findViewById(R.id.message_username);
        mStatus = view.findViewById(R.id.message_online);
        mMsgRecyclerView = view.findViewById(R.id.message_recycler);

        // init messages list
        mMsgRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mMessagesAdapter = new MsgListAdapter();
        mMsgRecyclerView.setAdapter(mMessagesAdapter);

        // event on click send button
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        // show username and status
        mUsername.setText(mTarget.getUsername());
        mStatus.setText(mTarget.isOnline() ? R.string.online : R.string.offline);

        // load messages
        loadMessages(false);

        return view;
    }

    public void loadMessages(boolean fromDatabase) {
        try {
            // get messages from database if necessary
            if (fromDatabase)
                Session.get().getUser().getChatFrom(
                        mTarget.getId()).setMessages(
                                new RestApi().findMessages(mTarget.getId()));

            // update adapter using user's session only if its not a new conversation
            if (isNewTarget == false)
                mMessagesAdapter.setMessages(
                        Session.get().getUser().getChatFrom(mTarget.getId()).getMessages());

            // refresh adapter
            mMessagesAdapter.notifyDataSetChanged();

        } catch (IOException e) {
            Log.e("loadMessages", "error --> " + e);
        }
    }

    private void sendMessage() {

        // obtain the text
        String content = mInputEditText.getText().toString();

        // validate message
        if (content.length() == 0)
            return;

        Message newMsg = new Message();
        newMsg.setDate(new Date());
        newMsg.setContent(content);

        // the API needs only user's id
        User from = new User();
        from.setId(Session.get().getUser().getId());
        newMsg.setFrom(from);

        newMsg.setTo(mTarget);

        int mId = -1;
        try {
            mId = new RestApi().createMessage(newMsg);
        } catch (IOException io) {
            Log.e("MessageFragment", "createMessage error --> " + io);
        }

        if (mId != -1) {
            // clear input text
            mInputEditText.setText("");
            newMsg.setId(mId);
            Session.get().getUser()
                    .getChatFrom(mTarget.getId()).getMessages().add(newMsg);
            loadMessages(false);

            // notify the socket server
            SocketClient.get().send(
                    "{\"operation\":\"new_message\", \"target_id\":"+mTarget.getId()+"}");

            // hide keyboard
            mInputEditText.onEditorAction(EditorInfo.IME_ACTION_DONE);
        } else {
            Toast.makeText(getContext(), "An unexpected error occurred", Toast.LENGTH_SHORT).show();
        }

    }

    private class MsgListAdapter extends RecyclerView.Adapter<MsgListAdapter.ViewHolder> {

        private List<Message> mMessasges = new ArrayList<>();
        public void setMessages(List<Message> posts) {
            mMessasges.clear();
            for (Message m:posts)
                mMessasges.add(m);
        }

        @NonNull
        @Override
        public MsgListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MsgListAdapter.ViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.list_item_message, parent, false)
            );
        }

        @Override
        public void onBindViewHolder(MsgListAdapter.ViewHolder viewHolder, final int position) {
            viewHolder.msg = ( (Message) (mMessasges.get(position)) );
            viewHolder.bind();
        }

        @Override
        public int getItemCount() {
            return mMessasges.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public Message msg;
            private TextView mDate;
            private CheckBox mIsRead;
            private TextView mContent;
            private MaterialCardView mContainer;

            public ViewHolder(View v) {
                super(v);
                // bind xml textview
                mDate = v.findViewById(R.id.msgDate);
                mIsRead = v.findViewById(R.id.msgIsRead);
                mContent = v.findViewById(R.id.msgContent);
                mContainer = v.findViewById(R.id.msgContainer);

            }

            public void bind()
            {
                String timeAgo = DateUtils.getRelativeTimeSpanString(
                        msg.getDate().getTime()).toString();
                mDate.setText(timeAgo);
                mContent.setText(msg.getContent());

                if (msg.getFrom().getId() == mTarget.getId()) {
                    // sent msg
                }
                else {
                    // received msg
                    mIsRead.setVisibility(msg.isRead() ? View.VISIBLE : View.INVISIBLE);
                    mContainer.setCardBackgroundColor(Color.BLACK);
                    ViewGroup.LayoutParams layoutParams = mContainer.getLayoutParams();
                    LinearLayout.LayoutParams castLayoutParams = (LinearLayout.LayoutParams) layoutParams;
                    castLayoutParams.gravity = Gravity.END;
                    mDate.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                }
            }

        }

    }

}
