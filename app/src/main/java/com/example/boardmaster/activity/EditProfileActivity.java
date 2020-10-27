package com.example.boardmaster.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.boardmaster.CurrentUser;
import com.example.boardmaster.R;
import com.example.boardmaster.retrofit.ApiClient;
import com.example.boardmaster.retrofit.ExifUtil;
import com.example.boardmaster.retrofit.JsonPlaceHolderApi;
import com.example.boardmaster.retrofit.Utility;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
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

public class EditProfileActivity extends AppCompatActivity {
    JsonPlaceHolderApi api = ApiClient.getClient().create(JsonPlaceHolderApi.class);
    TextView backButton;
    EditText mFirstname, mLastname, mUsername, mEmail;

    ImageView imageView;
    Button addBook;
    ImageButton takePhoto;

    private static final int REQUEST_CAMERA = 1;
    private static final int SELECT_FILE = 2;

    String userChoosenTask;

    private static String imagePath;

    List<File> photoFiles = new ArrayList<>();
    File currentPhoto;

    private CurrentUser currentUser = CurrentUser.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile_activity);

        mFirstname = findViewById(R.id.editProfileFirstName);
        mLastname = findViewById(R.id.editProfileLastName);
        mUsername = findViewById(R.id.editProfileUserName);
        mEmail = findViewById(R.id.editProfileEmail);
        imageView = findViewById(R.id.editProfileImage);

        addBook = findViewById(R.id.editProfileSaveButton);
        takePhoto = findViewById(R.id.editProfileImageButton);
        backButton = findViewById(R.id.editProfileCancelButton);

        getInfo();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }
        });


        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImagesMethod();
            }
        });

        addBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = mUsername.getText().toString();
                String firstname = mFirstname.getText().toString();
                String lastname = mLastname.getText().toString();
                String email = mEmail.getText().toString();


                editProfile(username, firstname, lastname, email, null);

            }
        });


    }

    public void getInfo(){
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
                    String firstname = jsonObject.getString("firstname");
                    String lastname = jsonObject.getString("lastname") ;
                    String uname = jsonObject.getString("username");
                    String email = jsonObject.getString("email");
                    System.out.println(jsonObject);
                    imageView.setImageResource(R.drawable.icon_profile_foreground);
                    mFirstname.setText(firstname);
                    mLastname.setText(lastname);
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



    public void editProfile(String username,String firstname, String lastname, String email, String imagePath) {
        Call<ResponseBody> call;
        Map<String, RequestBody> itemsData = new HashMap<>();

        itemsData.put("firstname", createPartFromString(firstname));
        itemsData.put("lastname", createPartFromString(lastname));
        itemsData.put("username", createPartFromString(username));
        itemsData.put("email", createPartFromString(email));
        if (imagePath == null) {
            call = api.editUser(currentUser.getToken(), itemsData);
        }
        else {
            File file = new File(imagePath);
            RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), reqFile);

            call = api.editUserAndPhoto(currentUser.getToken(), itemsData, body);
        }

        call.enqueue(new Callback<ResponseBody>(){
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response){
                if(!response.isSuccessful()){
                    System.out.println("code:"+response.code());
                    Toast.makeText(EditProfileActivity.this,"Saving changes failed", Toast.LENGTH_SHORT).show();

                }
                if(response.isSuccessful()){
                    String somResponse = response.body().toString();
                    Toast.makeText(EditProfileActivity.this,"Changes successfully saved", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
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



    private void selectImagesMethod() {
        final CharSequence[] items = {"Take Photo", "Choose from Library", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result = Utility.checkPermission(EditProfileActivity.this);
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
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        imageView.setImageBitmap(bm);
    }
    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");

        Uri tempUri = getImageUri(getApplicationContext(), thumbnail);

        imagePath = getRealPathFromURI(tempUri);
        File finalFile = new File(imagePath);

        Bitmap finalimg = ExifUtil.rotateBitmap(finalFile.toString(), thumbnail);
        imageView.setImageBitmap(thumbnail);
    }







    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }


    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
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

}