package com.example.boardmaster.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.boardmaster.CurrentUser;
import com.example.boardmaster.FragmentListener;
import com.example.boardmaster.R;
import com.example.boardmaster.activity.MainActivity;
import com.example.boardmaster.activity.RegisterProfileActivity;
import com.example.boardmaster.retrofit.ApiClient;
import com.example.boardmaster.retrofit.JsonPlaceHolderApi;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {
    private JsonPlaceHolderApi api = ApiClient.getClient().create(JsonPlaceHolderApi.class);
    private EditText mUsername, mPassword;
    private Button mLoginButton;
    private FragmentListener mFragmentListener;

    private TextView mLoginText, mRegisterText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        mUsername = view.findViewById(R.id.loginUsername);
        mPassword = view.findViewById(R.id.loginPassword);

        mLoginButton = view.findViewById(R.id.loginButton);
        mLoginText = view.findViewById(R.id.loginText);
        mRegisterText = view.findViewById(R.id.loginRegisterText);

        if(CurrentUser.getInstance().isUserLogedIn()){
            mRegisterText.setVisibility(View.GONE);
        }
        else{
            mRegisterText.setVisibility(View.VISIBLE);

        }

        mRegisterText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity().getApplicationContext(), RegisterProfileActivity.class));
                getActivity().finish();
            }
        });

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uname = mUsername.getText().toString();
                String pwd = mPassword.getText().toString();
                loginUser(uname, pwd);
            }
        });
        return view;
    }
    public void loginUser(String uname, String password){
        Call<ResponseBody> call = api.loginUser(uname, password);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(!response.isSuccessful()){
                    mLoginText.setText("Login failed");
                    System.out.println("code: "+ response.code());
                }
                if(response.isSuccessful()){
                    try {
                        CurrentUser.getInstance().setUserLogedIn(true);
                        String jwtoken = "Bearer "+response.body().string();
                        CurrentUser.getInstance().setToken(jwtoken);

                        Toast.makeText(getActivity(),"Login Successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getActivity().getApplicationContext(), MainActivity.class));
                        getActivity().finish();


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


            }


            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });
    }
}
