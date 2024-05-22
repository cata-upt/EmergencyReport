package com.example.emergencyapp.api;

import com.example.emergencyapp.api.utils.NotificationRequestApi;
import com.example.emergencyapp.api.utils.TokenRequestApi;
import com.example.emergencyapp.entities.FriendRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("registerToken")
    Call<Void> registerToken(@Body TokenRequestApi token);

    @POST("sendNotification")
    Call<Void> sendNotification(@Body NotificationRequestApi notificationRequestApi);

    @POST("sendFriendRequest")
    Call<Void> sendFriendRequest(@Body FriendRequest friendRequest);
}
