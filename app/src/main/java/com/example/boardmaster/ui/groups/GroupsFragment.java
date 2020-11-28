package com.example.boardmaster.ui.groups;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.boardmaster.CurrentUser;
import com.example.boardmaster.R;
import com.example.boardmaster.game.BoardGame;
import com.example.boardmaster.game.Game;
import com.example.boardmaster.retrofit.ApiClient;
import com.example.boardmaster.retrofit.JsonPlaceHolderApi;
import com.example.boardmaster.ui.home.ItemAdapter;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Group fragment is the fragment that lists al the groups the user is attached to
 */
public class GroupsFragment extends Fragment {

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
        listView = inflater.inflate(R.layout.fragment_groups, container, false);

        recyclerView = listView.findViewById(R.id.groupsRecyclerView);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        setItemsList();
        adapter = new GroupAdapter(items, onItemListener);
        recyclerView.setAdapter(adapter);

        return listView;
    }

    /**
     * sends a request for the games and lists it throught the group adapter
     */
    public void setItemsList()
    {
        Call<ArrayList<Game>> call = api.listUsersGames(CurrentUser.getInstance().getToken());

        call.enqueue(new Callback<ArrayList<Game>>(){
            @Override
            public void onResponse(Call<ArrayList<Game>> call, Response<ArrayList<Game>> response){
                if(!response.isSuccessful()){
                    System.out.println("code:"+response.code());
                    return;
                }
                items = response.body();
                adapter = new GroupAdapter(items, onItemListener);
                recyclerView.setAdapter(adapter);

            }

            @Override
            public void onFailure(Call<ArrayList<Game>> call,Throwable t){
            }
        });
    }

}