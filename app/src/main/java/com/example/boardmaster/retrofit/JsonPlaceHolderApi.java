package com.example.boardmaster.retrofit;

import com.example.boardmaster.BoardGame;
import com.example.boardmaster.Game;
import com.example.boardmaster.Photo;
import com.example.boardmaster.User;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Query;

public interface JsonPlaceHolderApi {

    //-------------------------------AUTHENTICATION-------------------------------------------------

    @Multipart
    @POST("auth/create")
    Call<Object> createUser(@PartMap Map<String, RequestBody> data,
                                @Part MultipartBody.Part image);
    @Multipart
    @POST("auth/create")
    Call<Object> createUsers(@PartMap Map<String, RequestBody> data);

    @Multipart
    @PUT("auth/editprofile")
    Call<ResponseBody> editUser(@Header("Authorization") String token,
                                    @PartMap Map<String, RequestBody> data
    );

    @Multipart
    @PUT("auth/editprofile")
    Call<ResponseBody> editUserAndPhoto(@Header("Authorization") String token,
                                     @PartMap Map<String, RequestBody> data,
                                     @Part MultipartBody.Part image);

    @Headers("Content-Type: application/json")
    @GET("auth/login")
    Call<ResponseBody> loginUser(@Query("username") String username,
                                 @Query("password") String password);


    @Headers("Content-Type:application/json")
    @GET("auth/currentuser")
    Call<Object>currentUser(@Header("Authorization") String token);


    @Headers("Content-Type:application/json")
    @PUT("auth/addrole")
    Call<ResponseBody>addRole(@Header("Authorization") String token,
                              @Query("uid") String username,
                              @Query("role") String role);

    @Headers("Content-Type:application/json")
    @PUT("auth/removerole")
    Call<ResponseBody>removeRole(@Header("Authorization") String token,
                                 @Query("uid") String username,
                                 @Query("role") String role);

    @Headers("Content-Type:application/json")
    @PUT("auth/changepassword")
    Call<ResponseBody>changePassword(@Header("Authorization") String token,
                                     @Query("uid") String username,
                                     @Query("pwd") String newPassword);




    //---------------------------MobileApp----------------------------------------------------------


    @Headers("Content-Type:application/json")
    @GET("boardmaster/games")
    Call<ArrayList<Game>>listGames();


    @Headers("Content-Type:application/json")
    @GET("boardmaster/boardgames")
    Call<ArrayList<BoardGame>> listBoardGames();



    @Headers("Content-Type:application/json")
    @PUT("boardmaster/joingame")
    Call<ResponseBody>joinGame(@Header("Authorization") String token,
                                   @Query("gameid") String gameid);

    @Headers("Content-Type:application/json")
    @DELETE("boardmaster/exitgame")
    Call<ResponseBody>exitGame(@Header("Authorization") String token,
                                 @Query("gameid") String gameid);


    @Multipart
    @POST("boardmaster/add-game")
    Call<ResponseBody> addGame(@Header("Authorization") String token,
                               @PartMap Map<String, RequestBody> data,
                               @Part MultipartBody.Part image

    );


    @Multipart
    @POST("boardmaster/add-boardgames")
    Call<ResponseBody> addBoardGames(@Header("Authorization") String token,
                                @PartMap Map<String, RequestBody> data,
                                @Part MultipartBody.Part image
                                     /**
                                      * if(imagePath == null){
                                      *             Map<String, RequestBody> itemsData = new HashMap<>();
                                      *
                                      *             itemsData.put("title", createPartFromString(title));
                                      *             itemsData.put("desc", createPartFromString(description));
                                      *             itemsData.put("price", createPartFromString(price));
                                      *             call = jsonPlaceHolderApi.addGame(token,itemsData);
                                      *         }
                                      *         else{
                                      *             File file = new File(imagePath);
                                      *             RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
                                      *             MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), reqFile);
                                      *
                                      *             Map<String, RequestBody> itemsData = new HashMap<>();
                                      *
                                      *             itemsData.put("title", createPartFromString(title));
                                      *             itemsData.put("desc", createPartFromString(description));
                                      *             itemsData.put("price", createPartFromString(price));
                                      *             call = jsonPlaceHolderApi.addGames(token,itemsData, body);
                                      *         }
                                      */
    );
    @Multipart
    @POST("boardmaster/add-boardgame")
    Call<ResponseBody> addBoardGame(@Header("Authorization") String token,
                               @PartMap Map<String, RequestBody> data
    );


    @Multipart
    @POST("boardmaster/image{name}")
    Call<Photo>getPhoto(@Query("name") String name);

}
