package com.example.boardmaster.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.boardmaster.Game;
import com.example.boardmaster.JoinBottomDialogFragment;
import com.example.boardmaster.R;
import com.example.boardmaster.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.AppViewHolder>{
    private Context mContext;
    private ArrayList<Game> games = new ArrayList<>();
    private LayoutInflater layoutInflater;
    private OnItemListener mOnItemListener;


    ItemAdapter(ArrayList data, OnItemListener onItemListener){
        games = data;
        this.mOnItemListener = onItemListener;
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.list_game_layout, parent, false);
        return new AppViewHolder(view, mOnItemListener);
    }


    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        String title = games.get(position).getTitle();
        holder.title.setText(title);

        String game = games.get(position).getGameName();
        holder.game.setText(game);

        String description = games.get(position).getDescription();

        String id = games.get(position).getId();

        ArrayList<User> playerlist = games.get(position).getPlayers();
        String players = "";
        for(int i = 0; i< playerlist.size(); i++){
            User user = playerlist.get(i);
            players += user.getUsername()+ " ";

        }
        String finalPlayers = players;
        holder.players.setText(finalPlayers);



        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppCompatActivity activity = (AppCompatActivity)view.getContext();
                JoinBottomDialogFragment fragment = new JoinBottomDialogFragment();
                fragment.setParameters(id,game,title, description, finalPlayers);
                fragment.show(activity.getSupportFragmentManager(), "PurchaseBottomDialogFragment");
                //activity.getSupportFragmentManager().beginTransaction().replace(R.id.itemsRecyclerView, fragment).addToBackStack(null).commit();
            }
        });

    }

    @Override
    public int getItemCount() {
        if (games == null) {
            return 0;
        }
        return games.size();
    }

    public class AppViewHolder extends RecyclerView.ViewHolder{
        ImageView image;
        CardView cardView;
        TextView game, title, description, players;

        OnItemListener onItemListener;

        public AppViewHolder(@NonNull View itemView, OnItemListener onItemListener) {
            super(itemView);
            game = itemView.findViewById(R.id.listGameName);
            image = itemView.findViewById(R.id.listGameImage);
            title = itemView.findViewById(R.id.listGameTitle);
            players = itemView.findViewById(R.id.listGamePlayers);
            cardView = itemView.findViewById(R.id.gameListCard);
            this.onItemListener = onItemListener;

        }

    }

    public interface OnItemListener{
        void onItemClick(int position);
    }

}
