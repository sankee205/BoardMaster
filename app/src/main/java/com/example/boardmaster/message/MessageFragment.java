package com.example.boardmaster.message;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.boardmaster.CurrentUser;
import com.example.boardmaster.R;
import com.example.boardmaster.retrofit.ApiClient;
import com.example.boardmaster.retrofit.JsonPlaceHolderApi;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageFragment extends BottomSheetDialogFragment {
    private JsonPlaceHolderApi api = ApiClient.getClient().create(JsonPlaceHolderApi.class);
    private CurrentUser currentUser = CurrentUser.getInstance();
    private TextView textToSend;
    private Button sendButton;

    private String id;
    private String game;
    private String date;
    private String time;


    public MessageFragment(){
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_message, container, false);


        return view;

    }

    public void setParameters(String id, String game, String date, String time){
        this.id = id;
        this.game = game;
        this.date = date;
        this.time = time;

    }
    public void sendMessage(){

    }
}
