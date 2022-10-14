package com.xh.http;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiTest {
    @POST("api/front/order/create")
    Call<Object> create(@Body Entity entity);

    public static class Entity {
        public String app_id = "mobybox";
        public String channel = "google";
        public int coin = 0;
        public String currency = "USD";
        public String order_ext = "{\"order_id\":\"20220921055432970052\"}";
        public String os = "android_1.0.0";
        public String pay_channel = "google";
        public int price = 5;
        public String product_desc = "product_desc";
        public String product_id = "com.mobybox.android.diamond1";
        public String token = "gmd:tk:ZjU2MmVlNmEtMzk2MS0xMWVkLWIxZDAtZmExNjNlYTU1Y2U4LjEwMDA1OC4xNjYzNzMyODQwOTg2NTg3MTMy";
        public int uid = 100058;
    }
}
