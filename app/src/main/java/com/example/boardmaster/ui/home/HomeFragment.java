package com.example.boardmaster.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.boardmaster.game.BoardGame;
import com.example.boardmaster.CurrentUser;
import com.example.boardmaster.R;
import com.example.boardmaster.retrofit.ApiClient;
import com.example.boardmaster.game.Game;
import com.example.boardmaster.retrofit.JsonPlaceHolderApi;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * this is the opening fragment, it lists all the games that other people
 * have posted so its available for everyone
 */
public class HomeFragment extends Fragment{

    private JsonPlaceHolderApi api = ApiClient.getClient().create(JsonPlaceHolderApi.class);
    private ArrayList<Game> items = new ArrayList<>();


    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private View listView;
    private ItemAdapter.OnItemListener onItemListener;

    /**
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        listView = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = listView.findViewById(R.id.gamesRecyclerView);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        setItemsList();
        getGameList();
        adapter = new ItemAdapter(items, onItemListener);
        recyclerView.setAdapter(adapter);
        return listView;
    }

    /**
     * requests the list of all games, adapts it and lists it
     */
    public void setItemsList()
    {
        Call<ArrayList<Game>> call = api.listGames();

        call.enqueue(new Callback<ArrayList<Game>>(){
            @Override
            public void onResponse(Call<ArrayList<Game>> call, Response<ArrayList<Game>> response){
                if(!response.isSuccessful()){
                    System.out.println("code:"+response.code());
                    return;
                }
                items = response.body();
                adapter = new ItemAdapter(items, onItemListener);
                recyclerView.setAdapter(adapter);

            }

            @Override
            public void onFailure(Call<ArrayList<Game>> call,Throwable t){
            }
        });
    }

    /**
     * gets the list of boardgames
     */
    private void getGameList(){
        Call<ArrayList<BoardGame>>call = api.listBoardGames();

        call.enqueue(new Callback<ArrayList<BoardGame>>(){
            @Override
            public void onResponse(Call<ArrayList<BoardGame>> call, Response<ArrayList<BoardGame>> response){
                if(!response.isSuccessful()){
                    System.out.println("code:"+response.code());
                    return;
                }
                ArrayList<BoardGame> boardgames = response.body();
                ArrayList<String> boardgamelist = new ArrayList<>();

                for(int i = 0; i< boardgames.size();i++){
                    boardgamelist.add(boardgames.get(i).getName());
                }
                CurrentUser.getInstance().setGameList(boardgamelist);


            }

            @Override
            public void onFailure(Call<ArrayList<BoardGame>> call,Throwable t){
            }
        });
    }

}