package com.example.boardmaster.message;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.boardmaster.CurrentUser;
import com.example.boardmaster.R;
import com.example.boardmaster.activity.MainActivity;
import com.example.boardmaster.activity.RegisterProfileActivity;
import com.example.boardmaster.retrofit.ApiClient;
import com.example.boardmaster.retrofit.JsonPlaceHolderApi;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageFragment extends BottomSheetDialogFragment {
    private StorageReference mStorageRef;
    private JsonPlaceHolderApi api = ApiClient.getClient().create(JsonPlaceHolderApi.class);
    private CurrentUser currentUser = CurrentUser.getInstance();
    private TextView textToSend;
    private ImageButton sendButton;

    private Long id;
    private String game;
    private String date;
    private String time;


    public MessageFragment(){
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_message, container, false);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        textToSend = view.findViewById(R.id.messageToSend);
        sendButton = view.findViewById(R.id.sendMessageButton);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = textToSend.getText().toString();
                getconversationid(message);
            }
        });

        return view;

    }

    public void setParameters(Long id, String game, String date, String time){
        this.id = id;
        this.game = game;
        this.date = date;
        this.time = time;

    }
    public void sendMessage(String message, String conversationid){
        String userToken = CurrentUser.getInstance().getToken();
        String path = null;

        Call<ResponseBody> call;
        Map<String, RequestBody> itemsData = new HashMap<>();

        itemsData.put("conversationid", createPartFromString(conversationid));
        itemsData.put("message", createPartFromString(message));
        if (path == null) {
            call = api.sendMessage(currentUser.getToken(),itemsData, null);
        }
        else {
            File file = new File(path);
            RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), reqFile);

            call = api.sendMessage(currentUser.getToken(),itemsData, body);

            //contentUri = Uri.fromFile(file);
        }

        call.enqueue(new Callback<ResponseBody>(){
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response){
                if(!response.isSuccessful()){
                    System.out.println("code:"+response.code());
                    Toast.makeText(getContext(),"Sending message failed", Toast.LENGTH_SHORT).show();
                }
                if(response.isSuccessful()){
                    Object somResponse = response.body();
                    System.out.println("response: "+somResponse);
                    String json=new Gson().toJson(response.body());
                    try {
                        JSONObject jsonObject=new JSONObject(json);
                        JSONArray photoList = jsonObject.getJSONArray("profileImages");

                        if(photoList.length() > 0){
                            for(int i = 0; i < photoList.length(); i ++){
                                JSONObject photo = photoList.getJSONObject(i);
                                String id = photo.getString("id");
                                System.out.println("id: " +id);

                                //uploadImageToFirebase(contentUri, id);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Toast.makeText(getContext(),"Message successfully sendt", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getContext(), MainActivity.class));
                    getActivity().finish();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call,Throwable t){
            }
        });
    }

    private RequestBody createPartFromString (String partString) {
        return RequestBody.create(MultipartBody.FORM, partString);
    }
    private void uploadImageToFirebase(Uri file, String id){
        StorageReference image = mStorageRef.child("images/" + id);
        image.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        image.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                System.out.println("image uploaded to firebase");
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        System.out.println(exception.getMessage());
                    }
                });
    }

    public void getconversationid(String message){
        String userToken = currentUser.getToken();

        Call<Object> call = api.getConversation(userToken, id);

        call.enqueue(new Callback<Object>(){
            @Override
            public void onResponse(Call<Object> call, Response<Object> response){
                if(!response.isSuccessful()){
                    System.out.println("code:"+response.code());
                    Toast.makeText(getContext(),"Sending message failed", Toast.LENGTH_SHORT).show();
                }
                if(response.isSuccessful()){
                    Object somResponse = response.body();
                    System.out.println("response: "+somResponse);
                    String json=new Gson().toJson(response.body());
                    JSONObject jsonObject= null;
                    try {
                        jsonObject = new JSONObject(json);
                        String conversationid = jsonObject.getString("id");
                        sendMessage(message, conversationid);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

            }

            @Override
            public void onFailure(Call<Object> call,Throwable t){
            }
        });
    }
}
