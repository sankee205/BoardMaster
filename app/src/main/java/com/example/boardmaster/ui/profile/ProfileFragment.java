package com.example.boardmaster.ui.profile;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import com.example.boardmaster.CurrentUser;
import com.example.boardmaster.Photo;
import com.example.boardmaster.R;
import com.example.boardmaster.activity.EditProfileActivity;
import com.example.boardmaster.retrofit.ApiClient;
import com.example.boardmaster.retrofit.JsonPlaceHolderApi;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class ProfileFragment extends Fragment {
    private JsonPlaceHolderApi api = ApiClient.getClient().create(JsonPlaceHolderApi.class);
    private TextView mUsername, mEmail, mName, mNoLogin, mProfileGroup;
    private ImageView mProfileImage;
    private Button mEditProfileButton;
    private boolean userloggedin = false;

    private StorageReference mStorageRef;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceStatee) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        mUsername = view.findViewById(R.id.profileUsername);
        mEmail = view.findViewById(R.id.profileEmail);
        mName = view.findViewById(R.id.profileName);
        mNoLogin = view.findViewById(R.id.noLogin);
        mProfileImage = view.findViewById(R.id.profileImage);
        mProfileGroup = view.findViewById(R.id.profilegroup);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        setHeader();

        mEditProfileButton = view.findViewById(R.id.editProfileButton);

        mEditProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(userloggedin==true){
                    startActivity(new Intent(getActivity().getApplicationContext(), EditProfileActivity.class));
                    getActivity().finish();
                }
                else{
                    Toast.makeText(getActivity(),"Please login first", Toast.LENGTH_SHORT).show();
                }


            }
        });
        return view;
    }
    private void setHeader(){
        if(CurrentUser.getInstance().isUserLogedIn()){
            currentUser();
            userloggedin = true;

        }
        else {
            mProfileImage.setImageResource(R.drawable.icon_profile_foreground);
            userloggedin = false;
            mNoLogin.setText("Please Login or Register");
            mNoLogin.setVisibility(View.VISIBLE);

        }
    }

    private void currentUser(){
        String userToken = CurrentUser.getInstance().getToken();
        Call<Object> call= api.currentUser(userToken);

        call.enqueue(new Callback<Object>(){
            @Override
            public void onResponse(Call<Object>call, Response<Object> response){
                if(!response.isSuccessful()){
                    System.out.println("code:"+response.code());
                    return;
                }
                String json=new Gson().toJson(response.body());
                try{
                    JSONObject jsonObject=new JSONObject(json);
                    String name = jsonObject.getString("firstname")+ " "+jsonObject.getString("lastname") ;
                    String uname = jsonObject.getString("username");
                    String email = jsonObject.getString("email");
                    String group = jsonObject.getJSONArray("groups").getJSONObject(0).getString("name");

                    CurrentUser.getInstance().setGroup(group);

                    JSONArray photoarray = jsonObject.getJSONArray("profileImages");
                    if(photoarray.length() > 0){
                        String photoid = photoarray.getJSONObject(0).getString("id");
                        getFirebasePhoto(photoid);
                    }
                    else{
                        mProfileImage.setImageResource(R.drawable.icon_profile_foreground);
                    }

                    mName.setText(name);
                    mUsername.setText(uname);
                    mEmail.setText(email);
                    mProfileGroup.setText(group);
                }catch(JSONException e){
                    e.printStackTrace();
                }


            }

            @Override
            public void onFailure(Call<Object>call,Throwable t){
            }
        });

    }

    public void getFirebasePhoto(String id){
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
                mProfileImage.setImageBitmap(bitmap);
            }
        });
    }

}