package com.example.boardmaster.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.boardmaster.CurrentUser;
import com.example.boardmaster.R;
import com.example.boardmaster.User;
import com.example.boardmaster.retrofit.ApiClient;
import com.example.boardmaster.retrofit.JsonPlaceHolderApi;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    JsonPlaceHolderApi api = ApiClient.getClient().create(JsonPlaceHolderApi.class);
    TextView mEmail, mUsername;
    ImageView mProfileImage;
    View hview;
    private AppBarConfiguration mAppBarConfiguration;
    private StorageReference mStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        FloatingActionButton addBoardGame = findViewById(R.id.addBoardButton);

        hview = navigationView.getHeaderView(0);
        mUsername = (TextView) hview.findViewById(R.id.navUsername);
        mEmail = (TextView) hview.findViewById(R.id.navEmail);
        mProfileImage = (ImageView) hview.findViewById(R.id.profileImageView);

        mStorageRef = FirebaseStorage.getInstance().getReference();

        setHeader();



        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), AddGameActivity.class));
                finish();
            }
        });

        if(CurrentUser.getInstance().getGroup()!=null){
            System.out.println(CurrentUser.getInstance().getGroup());
            if("admin".compareToIgnoreCase(CurrentUser.getInstance().getGroup())==0){
                addBoardGame.setVisibility(View.VISIBLE);
                addBoardGame.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(getApplicationContext(), AddBoardGameActivity.class));
                        finish();
                    }
                });
            }
            else{
                addBoardGame.setVisibility(View.GONE);
            }
        }
        else{
            addBoardGame.setVisibility(View.GONE);
        }






        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_profile, R.id.nav_login, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }
    private void setHeader(){
        if(CurrentUser.getInstance().isUserLogedIn()){
            currentUser();
        }
        else {
            mUsername.setText("Username");
            mEmail.setText("Email");
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
                    User user = new User();
                    JSONObject jsonObject=new JSONObject(json);
                    String fname = jsonObject.getString("firstname");
                    String lname = jsonObject.getString("lastname");
                    String uname = jsonObject.getString("username");
                    String email = jsonObject.getString("email");
                    String group = jsonObject.getJSONArray("groups").getJSONObject(0).getString("name");


                    if(jsonObject.getJSONArray("profileImages").length() > 0){
                        String imageId = jsonObject.getJSONArray("profileImages").getJSONObject(0).getString("id");
                        getFirebasePhoto(imageId);
                    }

                    user.setEmail(email);
                    user.setFirstname(fname);
                    user.setLastname(lname);
                    user.setUsername(uname);
                    //user.setPhotos(photoList);

                    CurrentUser.getInstance().setUser(user);
                    System.out.println("group is: "+group);
                    CurrentUser.getInstance().setGroup(group);

                    mUsername.setText(uname);
                    mEmail.setText(email);
                }catch(JSONException e){
                    e.printStackTrace();
                }


            }

            @Override
            public void onFailure(Call<Object>call,Throwable t){
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void getFirebasePhoto(String id){
        File localFile = null;
        try {
            localFile = File.createTempFile("images", "jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
        StorageReference image = mStorageRef.child("images/" + id);

        image.getBytes(1024*1024).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                mProfileImage.setImageBitmap(bitmap);
            }
        });
    }
}