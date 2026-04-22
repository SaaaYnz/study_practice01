package com.example.sp01.api;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    @POST("api/collections/users/request-otp")
    Call<JsonObject> authOtp(@Body JsonObject body);

    @POST("api/collections/users/auth-with-otp")
    Call<JsonObject> verifyOtp(@Body JsonObject body);

    @POST("api/collections/users/records")
    Call<JsonObject> registerUser(@Body JsonObject body);

    @POST("api/collections/users/auth-with-password")
    Call<JsonObject> authWithPassword(@Body JsonObject body);

    @GET("api/collections/products/records")
    Call<JsonObject> getProducts(
            @Header("Authorization") String authorization,
            @Query("perPage") int perPage,
            @Query("sort") String sort
    );
}
