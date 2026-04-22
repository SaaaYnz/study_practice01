package com.example.sp01.api;

import com.google.gson.JsonObject;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.Part;
import retrofit2.http.POST;
import retrofit2.http.Path;
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

    @GET("api/collections/promotions_and_news/records")
    Call<JsonObject> getPromotionsAndNews(
            @Header("Authorization") String authorization,
            @Query("page") int page,
            @Query("perPage") int perPage
    );

    @GET("api/collections/projects/records")
    Call<JsonObject> getProjects(
            @Header("Authorization") String authorization,
            @Query("page") int page,
            @Query("perPage") int perPage
    );

    @Multipart
    @POST("api/collections/projects/records")
    Call<JsonObject> createProject(
            @Header("Authorization") String authorization,
            @Part("title") RequestBody title,
            @Part("type") RequestBody type,
            @Part("date_start") RequestBody dateStart,
            @Part("date_end") RequestBody dateEnd,
            @Part("size") RequestBody size,
            @Part("description_source") RequestBody descriptionSource,
            @Part MultipartBody.Part technicalDrawing,
            @Part("user_id") RequestBody userId
    );

    @GET("api/collections/basket/records")
    Call<JsonObject> getBasket(@Header("Authorization") String authorization);

    @POST("api/collections/basket/records")
    Call<JsonObject> createBasket(
            @Header("Authorization") String authorization,
            @Body JsonObject body
    );

    @PATCH("api/collections/basket/records/{basketId}")
    Call<JsonObject> updateBasket(
            @Header("Authorization") String authorization,
            @Path("basketId") String basketId,
            @Body JsonObject body
    );

    @DELETE("api/collections/basket/records/{basketId}")
    Call<Void> deleteBasket(
            @Header("Authorization") String authorization,
            @Path("basketId") String basketId
    );

    @GET("api/collections/orders/records")
    Call<JsonObject> getOrders(
            @Header("Authorization") String authorization,
            @Query("page") int page,
            @Query("perPage") int perPage
    );

    @POST("api/collections/orders/records")
    Call<JsonObject> createOrder(
            @Header("Authorization") String authorization,
            @Body JsonObject body
    );
}
