package com.example.boardmaster.message;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.boardmaster.CurrentUser;
import com.example.boardmaster.Photo;
import com.example.boardmaster.R;
import com.example.boardmaster.User;
import com.example.boardmaster.game.Game;
import com.example.boardmaster.game.JoinBottomDialogFragment;
import com.example.boardmaster.ui.groups.GroupAdapter;
import com.example.boardmaster.ui.home.ItemAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * this is a message adapter. it will adapt the message to the messagefragment
 * so it looks like a chat
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.AppViewHolder>{

    private Context mContext;
    private List<Message> messages = new ArrayList<>();
    private LayoutInflater layoutInflater;
    private StorageReference mStorageRef;
    private CurrentUser currentUser = CurrentUser.getInstance();


    MessageAdapter(List<Message> data){
        messages = data;
    }


    /**
     *
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        String sender = message.getSender().getUsername();
        String current = currentUser.getUser().getUsername();
        if(current.compareTo(sender) == 0){
            return 0;
        }
        else{
            return 1;
        }
    }


    /**
     *
     * @param parent
     * @param viewType
     * @return
     */
    @NonNull
    @Override
    public MessageAdapter.AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(viewType == 0 ? R.layout.message_right : R.layout.message_left,
                parent,false);
        return new MessageAdapter.AppViewHolder(view);
    }


    /**
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.AppViewHolder holder, int position) {
        DateFormat dateFormat = new SimpleDateFormat();
        Message message = messages.get(position);

        String username = message.getSender().getFirstname();
        String text = message.getText();
        String date = "";

        Date created = message.getCreated();
        if(created != null) {
            date = dateFormat.format(created);

        }

        holder.username.setText(username);
        holder.text.setText(text);
        holder.date.setText(date);

        if(message.getPhotos().size() > 0) {
            String photoid = message.getPhotos().get(0).getId();

            File localFile = null;
            try {
                localFile = File.createTempFile("images", "jpg");
            } catch (IOException e) {
                e.printStackTrace();
            }
            StorageReference image = mStorageRef.child("images/" + photoid);

            image.getBytes(1024*1024*5).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    holder.image.setImageBitmap(bitmap);

                }
            });

        }

        if(message.getSender().getProfileImages().size() > 0) {
            String photoid = message.getSender().getProfileImages().get(0).getId();

            File localFile = null;
            try {
                localFile = File.createTempFile("images", "jpg");
            } catch (IOException e) {
                e.printStackTrace();
            }
            StorageReference image = mStorageRef.child("images/" + photoid);

            image.getBytes(1024*1024*5).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    holder.avatar.setImageBitmap(bitmap);

                }
            });

        } else {
            holder.avatar.setImageResource(R.drawable.icon_profile_foreground);
        }

    }

    /**
     *
     * @return
     */
    @Override
    public int getItemCount() {
        if (messages == null) {
            return 0;
        }
        return messages.size();
    }

    public class AppViewHolder extends RecyclerView.ViewHolder{
        TextView username,text,date;
        ImageView image, avatar;


        public AppViewHolder(@NonNull View view) {
            super(view);
            this.username = view.findViewById(R.id.username);
            this.text = view.findViewById(R.id.text);
            this.date = view.findViewById(R.id.date);
            this.image = view.findViewById(R.id.image);
            this.avatar = view.findViewById(R.id.avatar);
            mStorageRef = FirebaseStorage.getInstance().getReference();


        }

    }
}
