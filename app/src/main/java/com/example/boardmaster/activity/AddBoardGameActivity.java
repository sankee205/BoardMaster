package com.example.boardmaster.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.boardmaster.CurrentUser;
import com.example.boardmaster.R;
import com.example.boardmaster.retrofit.ApiClient;
import com.example.boardmaster.retrofit.ExifUtil;
import com.example.boardmaster.retrofit.JsonPlaceHolderApi;
import com.example.boardmaster.retrofit.Utility;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

/**
 * This class is an activity that adds a boardgame to the list of boardgames
 */
public class AddBoardGameActivity extends AppCompatActivity {
    JsonPlaceHolderApi api = ApiClient.getClient().create(JsonPlaceHolderApi.class);

    //firebase Image storage
    private StorageReference mStorageRef;

    //back button
    TextView homeButton;

    //parametic inputs for a boardgame
    EditText mName, mPlayers;

    //photo
    ImageView imageView;

    //button to add
    Button addBoardGame;

    //add image button
    ImageButton takePhoto;

    private static final int REQUEST_CAMERA = 1;
    private static final int SELECT_FILE = 2;

    //choose photo from library or new photo
    String userChoosenTask;

    //variable for image
    private Uri imagePath;

    // list for photo
    List<File> photoFiles = new ArrayList<>();

    //file for boardgame photo
    File currentPhoto;

    //variabel for current user
    private CurrentUser currentUser = CurrentUser.getInstance();

    //arraylist for the list of games
    private ArrayList gameList = currentUser.getGameList();

    //game name
    private String game;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_boardgame);

        // attaches variables to the specific objects
        homeButton = findViewById(R.id.editProfileCancelButton);
        mName = findViewById(R.id.editProfileFirstName);
        mPlayers = findViewById(R.id.editProfileEmail);
        imageView = findViewById(R.id.editProfileImage);
        addBoardGame = findViewById(R.id.editProfileSaveButton);
        takePhoto = findViewById(R.id.editProfileImageButton);
        mStorageRef = FirebaseStorage.getInstance().getReference();


        //sets a listener to the homebutton
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }
        });


        //sets a listener to the photo button
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImagesMethod();
            }
        });

        //sets a listener to the add boardgame button
        addBoardGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentUser.isUserLogedIn()){
                    String name = mName.getText().toString();
                    String players = mPlayers.getText().toString();


                    addBoard(name, players);
                }
                else{
                    Toast.makeText(AddBoardGameActivity.this,"Please Login First", Toast.LENGTH_SHORT).show();
                }


            }
        });
    }

    /**
     * adds a boardgame to the boardgamelist, sends a boardgame to the server
     * @param name
     * @param players
     */
    public void addBoard(String name ,String players) {
        String path = getPath(imagePath);

        Map<String, RequestBody> itemsData = new HashMap<>();

        itemsData.put("name", createPartFromString(name));
        itemsData.put("players", createPartFromString(players));
        Call<Object> call;
        if (path == null) {
            call = api.addBoardGame(currentUser.getToken(), itemsData);
        }
        else{
            File file = new File(path);
            RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), reqFile);

            call = api.addBoardGames(currentUser.getToken(), itemsData, body);
        }
        call.enqueue(new Callback<Object>(){
            @Override
            public void onResponse(Call<Object> call, Response<Object> response){
                if(!response.isSuccessful()){
                    System.out.println("code:"+response.code());
                    Toast.makeText(AddBoardGameActivity.this,"BoardGame adding failed", Toast.LENGTH_SHORT).show();

                }
                if(response.isSuccessful()) {
                    Object somResponse = response.body();
                    String json=new Gson().toJson(response.body());
                    try {
                        JSONObject jsonObject=new JSONObject(json);
                        JSONArray photoList = jsonObject.getJSONArray("boardImages");
                        if(photoList.length() > 0){
                            for(int i = 0; i < photoList.length(); i ++){
                                JSONObject photo = photoList.getJSONObject(i);
                                String id = photo.getString("id");
                                uploadImageToFirebase(imagePath, id);
                            }
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(AddBoardGameActivity.this,"Board successfully added", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                }

            }

            @Override
            public void onFailure(Call<Object> call,Throwable t){
            }
        });
    }

    /**
     * This i s a help function, creates a part from a string
     * @param partString
     * @return
     */
    private RequestBody createPartFromString (String partString) {
        return RequestBody.create(MultipartBody.FORM, partString);
    }

    /**
     * pop up window that lets you choose between taking a photo and choosing from
     * the library of the phone
     */
    private void selectImagesMethod() {
        final CharSequence[] items = {"Take Photo", "Choose from Library", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result = Utility.checkPermission(AddBoardGameActivity.this);
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

    /**
     * function run after photo is taken/choosen
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imagePath = data.getData();
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE){
                onSelectFromGalleryResult(data);
            }
            else if (requestCode == REQUEST_CAMERA){
                onCaptureImageResult(data);

            }
        }
    }

    /**
     * sets the imageview to the coosen photo
     * @param data
     */
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

    /**
     * sets the imageview to the coosen photo
     * @param data
     */
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

        imageView.setImageBitmap(thumbnail);
    }

    /**
     * starts a new window for the image gallery
     */
    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    /**
     * start a new window for the camera
     */
    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }


    /**
     * uploads the image to firbase
     * @param file
     * @param id
     */
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
                                Toast.makeText(AddBoardGameActivity.this,"Image Uploaded", Toast.LENGTH_SHORT).show();
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

    /**
     * returns the path of a uri file
     * @param uri
     * @return
     */
    public String getPath(Uri uri) {
            if(uri == null){
                return null;
            }
            else{
                Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                cursor.moveToFirst();
                String document_id = cursor.getString(0);
                document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
                cursor.close();

                cursor = getContentResolver().query(
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
                cursor.moveToFirst();
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                cursor.close();

                return path;
            }
        }
}