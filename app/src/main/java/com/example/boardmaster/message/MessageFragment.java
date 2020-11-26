package com.example.boardmaster.message;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.boardmaster.CurrentUser;
import com.example.boardmaster.R;
import com.example.boardmaster.activity.AddGameActivity;
import com.example.boardmaster.activity.MainActivity;
import com.example.boardmaster.activity.RegisterProfileActivity;
import com.example.boardmaster.retrofit.ApiClient;
import com.example.boardmaster.retrofit.JsonPlaceHolderApi;
import com.example.boardmaster.retrofit.Utility;
import com.example.boardmaster.ui.home.ItemAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private RecyclerView.Adapter adapter;
    private List<Message> messages = new ArrayList<>();

    private ImageView imageToSend;
    private ImageButton cameraButton;


    private Long id;
    private Long conversationId;
    private String game;
    private String date;
    private String time;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;


    private static final int REQUEST_CAMERA = 1;
    private static final int SELECT_FILE = 2;
    String userChoosenTask;

    private static Uri imageUri;

    public MessageFragment(){
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_message, container, false);

        recyclerView = view.findViewById(R.id.messageRecyclerView);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // do your stuff
        } else {
            mAuth.signInAnonymously().addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    System.out.println("login success");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    System.out.println(e.getMessage());
                }
            });
        }

        imageToSend = view.findViewById(R.id.imageToSend);
        cameraButton = view.findViewById(R.id.photoMessageButton);
        textToSend = view.findViewById(R.id.messageToSend);
        sendButton = view.findViewById(R.id.sendMessageButton);
        adapter = new MessageAdapter(messages);
        getconversationid();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = textToSend.getText().toString();
                textToSend.setText("");
                imageToSend.setVisibility(View.GONE);
                sendMessage(message, conversationId.toString());
            }
        });

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImagesMethod();
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
        String imagePath = getPath(imageUri);
        String userToken = CurrentUser.getInstance().getToken();
        String path = null;

        Call<Message> call;
        Map<String, RequestBody> itemsData = new HashMap<>();

        itemsData.put("conversationid", createPartFromString(conversationid));
        itemsData.put("message", createPartFromString(message));
        if (path == null) {
            call = api.sendMessage(currentUser.getToken(),itemsData, null);
        }
        else {
            File file = new File(imagePath);
            RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), reqFile);

            call = api.sendMessage(currentUser.getToken(),itemsData, body);

            //contentUri = Uri.fromFile(file);
        }

        call.enqueue(new Callback<Message>(){
            @Override
            public void onResponse(Call<Message> call, Response<Message> response){
                if(!response.isSuccessful()){
                    System.out.println("code:"+response.code());
                    Toast.makeText(getContext(),"Sending message failed", Toast.LENGTH_SHORT).show();
                }
                if(response.isSuccessful()){
                    Message somResponse = response.body();
                    if(somResponse.getPhotos().size() > 0){
                        uploadImageToFirebase(imageUri,somResponse.getPhotos().get(0).getId());
                    }
                    updateConversation(conversationId);

                }

            }

            @Override
            public void onFailure(Call<Message> call,Throwable t){
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

    public void getconversationid(){
        String userToken = currentUser.getToken();

        Call<Object> call = api.getConversation(userToken, id);

        call.enqueue(new Callback<Object>(){
            @Override
            public void onResponse(Call<Object> call, Response<Object> response){
                if(!response.isSuccessful()){
                    System.out.println("code:"+response.code());
                    Toast.makeText(getContext(),"getting conversation id failed", Toast.LENGTH_SHORT).show();
                }
                if(response.isSuccessful()){
                    Object somResponse = response.body();
                    String json=new Gson().toJson(response.body());
                    JSONArray jsonArray= null;
                    try {
                        jsonArray = new JSONArray(json);
                        String id = jsonArray.get(0).toString();
                        String newId = id.trim().replace(".0", "");
                        conversationId = Long.parseLong(newId);
                        updateConversation(conversationId);

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

    public void updateConversation(Long convId){
        String userToken = currentUser.getToken();

        if(convId != null){
            Call<List<Message>> call = api.getMessages(userToken, conversationId);

            call.enqueue(new Callback<List<Message>>(){
                @Override
                public void onResponse(Call<List<Message>> call, Response<List<Message>> response){
                    if(!response.isSuccessful()){
                        System.out.println("code:"+response.code());
                        Toast.makeText(getContext(),"updating conversation failed", Toast.LENGTH_SHORT).show();
                    }
                    if(response.isSuccessful()){
                        messages= response.body();
                        if(!messages.isEmpty()) {
                            adapter = new MessageAdapter(messages);
                            recyclerView.setAdapter(adapter);
                        }
                    }
                }
                @Override
                public void onFailure(Call<List<Message>> call,Throwable t){
                }
            });
        }
    }

    private void selectImagesMethod() {
        final CharSequence[] items = {"Take Photo", "Choose from Library", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result = Utility.checkPermission(getContext());
                if (items[item].equals("Take Photo")) {
                    userChoosenTask = "Take Photo";
                    if (result)
                        cameraIntent();
                } else if (items[item].equals("Choose from Library")) {
                    userChoosenTask = "Choose from Library";
                    if (result)
                        galleryIntent();
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imageUri = data.getData();
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE){
                onSelectFromGalleryResult(data);
            }
            else if (requestCode == REQUEST_CAMERA){
                onCaptureImageResult(data);

            }
        }
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {
        Bitmap bm = null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        imageToSend.setImageBitmap(bm);
        imageToSend.setVisibility(View.VISIBLE);
    }
    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");
        if (!destination.exists()) {
            destination.mkdirs();
        }

        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        imageToSend.setImageBitmap(thumbnail);
        imageToSend.setVisibility(View.VISIBLE);

    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    public String getPath(Uri uri) {
        if(uri == null){
            return null;
        }
        else{
            Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
            cursor.moveToFirst();
            String document_id = cursor.getString(0);
            document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
            cursor.close();

            cursor = getActivity().getContentResolver().query(
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
            cursor.moveToFirst();
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            cursor.close();

            return path;
        }
    }

}
