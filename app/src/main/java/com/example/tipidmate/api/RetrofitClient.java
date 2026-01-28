package com.example.tipidmate.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // IMPORTANT: Change this URL based on your testing device
    // For Android Emulator: use "http://10.0.2.2/tipidmate_api/"
    // For Real Phone: use "http://YOUR_COMPUTER_IP/tipidmate_api/"
    private static final String BASE_URL = "http://10.0.2.2/tipidmate_api/";
        //"http://10.0.2.2/tipidmate_api/";

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {

            // Create logging interceptor to see API requests and responses
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Create OkHttp client with logging
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            // Create Retrofit instance
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}