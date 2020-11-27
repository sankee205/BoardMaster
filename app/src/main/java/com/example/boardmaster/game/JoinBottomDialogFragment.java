package com.example.boardmaster.game;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.boardmaster.CurrentUser;
import com.example.boardmaster.R;
import com.example.boardmaster.retrofit.ApiClient;
import com.example.boardmaster.retrofit.JsonPlaceHolderApi;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.sql.Time;
import java.util.Date;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JoinBottomDialogFragment extends BottomSheetDialogFragment {
    private JsonPlaceHolderApi api = ApiClient.getClient().create(JsonPlaceHolderApi.class);
    private CurrentUser currentUser = CurrentUser.getInstance();
    private TextView mGame, mPlayers, mTitle, mDescription, mGameMembers, mDate, mTime;
    private Button mBuyButton;
    private ImageView imageView;

    private Long id;
    private String game;
    private String title;
    private String description;
    private String players;
    private String date;
    private String time;
    private String photoID;

    private StorageReference mStorageRef;

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
        mGameMembers = view.findViewById(R.id.popUpGamePlayerText);
        mDate = view.findViewById(R.id.popUpGameDate);
        mTime = view.findViewById(R.id.popUpGameTime);
        imageView = view.findViewById(R.id.popUpGameImage);

        mGame.setText(game);
        mTitle.setText(title);
        mDescription.setText(description);
        mPlayers.setText(players);
        mDate.setText(date.toString());
        mTime.setText(time.toString());
        mGameMembers.setText("GameMembers: ");

        mStorageRef = FirebaseStorage.getInstance().getReference();

        getBoardGamePhoto(photoID);


        if(CurrentUser.getInstance().isUserLogedIn()){
            if (players.contains(CurrentUser.getInstance().getUser().getUsername())){
                mBuyButton.setVisibility(View.GONE);
            }
            else{
                mBuyButton.setVisibility(View.VISIBLE);
                mBuyButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        joinGame(id);

                    }
                });
            }
        }
        else{
            mBuyButton.setVisibility(View.VISIBLE);
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
        }


        return view;

    }
    public void setParameters(Long id, String game, String title, String description, String players, String date, String time, String photoId){
        this.id = id;
        this.game = game;
        this.title = title;
        this.description = description;
        this.players = players;
        this.date = date;
        this.time = time;
        this.photoID = photoId;

    }
    public boolean joinGame(Long itemid){
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

    private void getBoardGamePhoto(String id){
        File localFile = null;
        try {
            localFile = File.createTempFile("images", "jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
        StorageReference image = mStorageRef.child("images/" + id);

        image.getBytes(1024*1024*5).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                imageView.setImageBitmap(bitmap);

            }
        });
    }
}
