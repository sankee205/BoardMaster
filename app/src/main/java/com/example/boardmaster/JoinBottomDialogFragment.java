package com.example.boardmaster;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.boardmaster.retrofit.ApiClient;
import com.example.boardmaster.retrofit.JsonPlaceHolderApi;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JoinBottomDialogFragment extends BottomSheetDialogFragment {
    private JsonPlaceHolderApi api = ApiClient.getClient().create(JsonPlaceHolderApi.class);
    private CurrentUser currentUser = CurrentUser.getInstance();
    private TextView mGame, mPlayers, mTitle, mDescription;
    private Button mBuyButton;

    private String id;
    private String game;
    private String title;
    private String description;
    private String players;


    public JoinBottomDialogFragment(){
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_join_game, container, false);

        mGame = view.findViewById(R.id.popUpName);
        mTitle = view.findViewById(R.id.popUpGameTitle);
        mPlayers = view.findViewById(R.id.popUpGamePlayers);
        mDescription = view.findViewById(R.id.popUpGameDescription);
        mBuyButton = view.findViewById(R.id.joinButton);

        mGame.setText(game);
        mTitle.setText(title);
        mDescription.setText(description);
        mPlayers.setText(players);

        mBuyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentUser.isUserLogedIn()){
                    joinGame(id);
                }
                else{
                    Toast.makeText(getActivity(),"Please login or register", Toast.LENGTH_SHORT).show();
                }

            }
        });

        return view;

    }
    public void setParameters(String id, String game, String title, String description, String players){
        this.id = id;
        this.game = game;
        this.title = title;
        this.description = description;
        this.players = players;
    }
    public boolean joinGame(String itemid){
        String userToken = CurrentUser.getInstance().getToken();
        Call<ResponseBody> call = api.joinGame(userToken,itemid);

        call.enqueue(new Callback<ResponseBody>(){
            @Override
            public void onResponse(Call<ResponseBody>call, Response<ResponseBody> response){
                if(!response.isSuccessful()){
                    System.out.println("code:"+response.code());
                    return;
                }
                if(response.isSuccessful()){
                    Toast.makeText(getActivity(),"Game Joined", Toast.LENGTH_SHORT).show();
                    mBuyButton.setVisibility(View.GONE);
                }

            }

            @Override
            public void onFailure(Call<ResponseBody>call,Throwable t){
            }
        });

        return true;
    }

}
