package com.example.boardmaster.activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.boardmaster.CurrentUser;
import com.example.boardmaster.R;
import com.example.boardmaster.User;
import com.example.boardmaster.game.Game;
import com.example.boardmaster.retrofit.ApiClient;
import com.example.boardmaster.retrofit.ExifUtil;
import com.example.boardmaster.retrofit.JsonPlaceHolderApi;
import com.example.boardmaster.retrofit.Utility;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import java.io.UnsupportedEncodingException;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
 * this class adds a game to the database
 */
public class AddGameActivity extends AppCompatActivity {
    JsonPlaceHolderApi api = ApiClient.getClient().create(JsonPlaceHolderApi.class);
    TextView backButton, mDate, mTime;
    EditText mTitle, mDescription, mPlayers;

    Spinner spinner;

    ImageView imageView;
    Button addBook;
    ImageButton takePhoto;

    private static final int REQUEST_CAMERA = 1;
    private static final int SELECT_FILE = 2;

    String userChoosenTask;

    private static Uri imageUri;

    List<File> photoFiles = new ArrayList<>();
    File currentPhoto;

    private CurrentUser currentUser = CurrentUser.getInstance();

    private ArrayList gameList;
    private String game;
    private String boardGameId;
    private String photoId;

    private StorageReference mStorageRef;


    private DatePickerDialog.OnDateSetListener onDateSetListener;
    private TimePickerDialog.OnTimeSetListener onTimeSetListener;


    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_game);

        mTitle = findViewById(R.id.editProfileFirstName);
        mDescription = findViewById(R.id.addGameDescription);
        mPlayers = findViewById(R.id.editProfileEmail);
        imageView = findViewById(R.id.editProfileImage);

        addBook = findViewById(R.id.editProfileSaveButton);
        takePhoto = findViewById(R.id.editProfileImageButton);
        backButton = findViewById(R.id.editProfileCancelButton);

        spinner = findViewById(R.id.searchableSpinner);

        gameList = currentUser.getGameList();

        mDate = findViewById(R.id.addGameDate);
        mTime = findViewById(R.id.addGameTime);

        mStorageRef = FirebaseStorage.getInstance().getReference();



        mTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int hour = 12;
                int minute = 0;
                TimePickerDialog timePickerDialog = new TimePickerDialog(AddGameActivity.this, android.R.style.Theme_Material_Light_Dialog_MinWidth,onTimeSetListener, hour, minute,true);
                timePickerDialog.show();
            }
        });


        onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                String time;
                if(i1 ==0){
                    time = i + ":00";
                }
                else{
                    time = i + ":"+ i1;
                }
                mTime.setText(time);
            }
        };

        mDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH) ;
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(AddGameActivity.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth,onDateSetListener, year, month, day);
                dialog.getWindow().setBackgroundDrawable( new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        onDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                int d = day;
                int m = month + 1;
                int y = year;
                String date = d + "/" + m + "/"+ y;
                mDate.setText(date);
            }
        };


        spinner.setAdapter(new ArrayAdapter<>(AddGameActivity.this, R.layout.support_simple_spinner_dropdown_item, gameList));

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                game = gameList.get(position).toString();
                String boardGame = gameList.get(position).toString();
                getPhotoId(boardGame);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

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
                if(currentUser.isUserLogedIn()){

                    String title = mTitle.getText().toString();
                    String description = mDescription.getText().toString();
                    String price = mPlayers.getText().toString();
                    String timestring = mTime.getText().toString();
                    String dates = mDate.getText().toString();

                    Game thisgame = new Game();
                    thisgame.setDate(dates);
                    thisgame.setDescription(description);
                    thisgame.setGameName(title);
                    thisgame.setGameOwner(currentUser.getUser());
                    thisgame.setMaxPlayers(Integer.parseInt(price));
                    thisgame.setTime(timestring);

                    addItem(game, title, description, price, dates, timestring);

                }
                else{
                    Toast.makeText(AddGameActivity.this,"Please Login First", Toast.LENGTH_SHORT).show();
                }


            }
        });


    }

    /**
     *
     * @param game
     * @param title
     * @param description
     * @param players
     * @param date
     * @param time
     */
    public void addItem(String game, String title,String description,String players, String date, String time) {
        boolean picture;
        String imagePath = getPath(imageUri);
        Map<String, RequestBody> itemsData = new HashMap<>();
        String token = currentUser.getToken();

        itemsData.put("title", createPartFromString(title));
        itemsData.put("game", createPartFromString(game));
        itemsData.put("desc", createPartFromString(description));
        itemsData.put("players", createPartFromString(players));
        itemsData.put("date", createPartFromString(date));
        itemsData.put("time", createPartFromString(time));
        Call<Object> call;
        if (imagePath == null) {
            if(photoId != null){
                call = api.addGame(token, itemsData, null);
                itemsData.put("photoId", createPartFromString(photoId));
                picture = false;
            }
            else{
                call = api.addGame(token, itemsData, null);
                picture = false;
            }

        }

        else{
            File file = new File(imagePath);
            RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), reqFile);

            call = api.addGame(token,itemsData, body);
            picture = true;
        }

        call.enqueue(new Callback<Object>(){
            @Override
            public void onResponse(Call<Object> call, Response<Object> response){
                if(!response.isSuccessful()){
                    System.out.println("code:"+response.code());
                    Toast.makeText(AddGameActivity.this,"Register failed", Toast.LENGTH_SHORT).show();
                }
                if(response.isSuccessful()){
                    Object somResponse = response.body();
                    String json=new Gson().toJson(response.body());
                    try {
                        JSONObject jsonObject=new JSONObject(json);
                        JSONArray photoList = jsonObject.getJSONArray("profileImages");
                        createConversation(jsonObject.getLong("id"));
                        if(picture){
                            if(photoList.length() > 0){
                                for(int i = 0; i < photoList.length(); i ++){
                                    JSONObject photo = photoList.getJSONObject(i);
                                    String id = photo.getString("id");
                                    uploadImageToFirebase(imageUri, id);
                                }
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Toast.makeText(AddGameActivity.this,"Registered successfully, please login", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                }

            }

            @Override
            public void onFailure(Call<Object> call,Throwable t){
                t.printStackTrace();
            }
        });
    }

    /**
     *
     * @param partString
     * @return
     */
    private RequestBody createPartFromString (String partString) {
        return RequestBody.create(MultipartBody.FORM, partString);
    }


    /**
     *
     */
    private void selectImagesMethod() {
        final CharSequence[] items = {"Take Photo", "Choose from Library", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result = Utility.checkPermission(AddGameActivity.this);
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
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
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
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        imageView.setImageBitmap(bm);
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

        imageView.setImageBitmap(thumbnail);
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

    /**
     * gets the path of a uri file
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


    /**
     * uploads a image to firebase
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
                                Toast.makeText(AddGameActivity.this,"Image Uploaded", Toast.LENGTH_SHORT).show();
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
     * creates a conversation through request to the server
     * @param thisgameid
     */
    private void createConversation(Long thisgameid){
        Call<Object> call = api.createConversation(currentUser.getToken(),thisgameid);


        call.enqueue(new Callback<Object>(){
            @Override
            public void onResponse(Call<Object> call, Response<Object> response){
                if(!response.isSuccessful()){
                    System.out.println("code:"+response.code());
                    Toast.makeText(AddGameActivity.this,"Register failed", Toast.LENGTH_SHORT).show();
                }
                if(response.isSuccessful()){
                    Object somResponse = response.body();

                }

            }

            @Override
            public void onFailure(Call<Object> call,Throwable t){
                t.printStackTrace();
            }
        });
    }

    /**
     * gets the boardgame photo from firebase by id
     * @param id
     */
    private void getBoardGamePhoto(String id){
        photoId = id;
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

    /**
     * gets the photo id of a boardgame based on the name
     * @param name
     */
    private void getPhotoId(String name){
        Call<ResponseBody> call= api.getPhoto(name);

        call.enqueue(new Callback<ResponseBody>(){
            @Override
            public void onResponse(Call<ResponseBody>call, Response<ResponseBody> response){
                if(!response.isSuccessful()){
                    System.out.println("code:"+response.code());
                    return;
                }
                if(response.isSuccessful()){
                    String id = null;
                    try {
                        id = response.body().string();
                        getBoardGamePhoto(id);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }


            }

            @Override
            public void onFailure(Call<ResponseBody>call,Throwable t){
            }
        });
    }

}