package com.roundG0929.hibike;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;
import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.roundG0929.hibike.activities.auth.BasicProfileActivity;
import com.roundG0929.hibike.activities.auth.MyPostActivity;
import com.roundG0929.hibike.activities.auth.SigninActivity;
import com.roundG0929.hibike.activities.information.InformationWriteActivity;
import com.roundG0929.hibike.activities.map_route.FindPathActivity;
import com.roundG0929.hibike.activities.map_route.RidingActivity;
import com.roundG0929.hibike.activities.riding_record.RidingRecordListActivity;
import com.roundG0929.hibike.api.server.ApiInterface;
import com.roundG0929.hibike.api.server.RetrofitClient;
import com.roundG0929.hibike.api.server.dto.BasicProfile;
import com.roundG0929.hibike.api.server.dto.GetRidingTotal;
import com.roundG0929.hibike.api.server.fuction.ImageApi;
import com.roundG0929.hibike.activities.board.ListViewActivity;
import com.roundG0929.hibike.api.weather.AirconditionApi;
import com.roundG0929.hibike.api.weather.WeatherApi;
import com.roundG0929.hibike.api.weather.WeatherAnalysis;
import com.roundG0929.hibike.api.weather.airconditionDto.Aircondition;
import com.roundG0929.hibike.api.weather.airconditionDto.AirconditionItem;
import com.roundG0929.hibike.api.weather.wheatherDto.Item;
import com.roundG0929.hibike.api.weather.wheatherDto.RealTimeWeather;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    //hibike server api
    ApiInterface api;
    //Navigation drawer ?????? ??????
    CardView btn_open;
    //Navigation ?????? ?????? ?????????

    TextView btnSigninOrNickname; // ????????????, ?????????, ???????????????
    TextView tvMyRecord;
    FrameLayout postFrameLayout;
    FrameLayout dangerFrameLayout;
    ImageView ivProfileImage;
    String id;
    ImageApi imageApi;
    LinearLayout ll;
    TextView tvMainId;
    TextView tvMainRidingTotal;
    ProgressBar mainProgressBar;
    TextView mainRidingGoal;
    TextView mainRidingAchievement;
    LinearLayout llProfile;
    TextView myPost;


    //?????? activity?????? main component ????????? ?????????
    public static Context context_main;


    //Navigation drawer
//    private DrawerLayout drawerLayout;
//    private View drawerView;

    //???????????? ?????? ?????????, ??? ??????
    private long backKeyPressedTime = 0;
    Toast toast;
    boolean isDrawerOpened;

    //???????????? ??????
    WeatherAnalysis weatherAnalysis = new WeatherAnalysis();


    //?????? ?????? ??? ??????, ?????? ?????????
    PermissionListener permissionlistener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
        }

        @Override
        public void onPermissionDenied(List<String> deniedPermissions) {
            Toast.makeText(MainActivity.this, "????????? ???????????? ????????? ????????? ??? ????????????.", Toast.LENGTH_SHORT).show();
            finish();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_2);
        context_main = this;

        Handler mainAnimHandler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mainAnimHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        FrameLayout frameLayout = findViewById(R.id.loadingLayout);
                        frameLayout.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.animation_main));
                        frameLayout.setVisibility(View.GONE);
                        LinearLayout linearLayout = findViewById(R.id.mainLayout);
                    }
                });
            }
        }).start();
//        getWindow().setNavigationBarColor(Color.WHITE);//?????????????????? ??????

        //????????? ?????????, ?????? ????????? ???????????? ??????
        SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        id = pref.getString("id", "");

        api = RetrofitClient.getRetrofit().create(ApiInterface.class);

//        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
//        drawerView = (View) findViewById(R.id.drawer);
//
//        //Navigation drawer ?????? ??????
//        btn_open = findViewById(R.id.btn_profile);
//        btn_open.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                drawerLayout.openDrawer(drawerView);
//                isDrawerOpened = true;
//            }
//        });
//
//        // drawer ?????????
//        drawerLayout.setDrawerListener(listener);
//        drawerView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                return true;
//            }
//        });

        btnSigninOrNickname = (TextView) findViewById(R.id.btn_signin_or_nickname);

        //??? ?????? ??????
        tvMyRecord = findViewById(R.id.tv_my_record);
        tvMyRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RidingRecordListActivity.class);
                startActivity(intent);
            }
        });
        if (id == "") {
            btnSigninOrNickname.setText("????????? ??? ??????????????????!");
            ll = findViewById(R.id.layout_profile);
            ll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), SigninActivity.class);
                    startActivity(intent);
                }
            });
//            btnSigninOrNickname.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//
//                }
//            });
        } else {
            getProfile();
            //????????? ????????? ??????

            tvMainId = findViewById(R.id.tv_main_id);
            tvMainId.setText(id+" \uD83D\uDC4B");

            tvMainRidingTotal = findViewById(R.id.tv_main_riding_total);
            getTotalInfo();

            ivProfileImage = (ImageView) findViewById(R.id.iv_profile_image);
            imageApi = new ImageApi();
            imageApi.setImageOnImageView(this, ivProfileImage, imageApi.getProfileImageUrl(id));


            postFrameLayout = findViewById(R.id.postFrameLayout);
            postFrameLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), ListViewActivity.class);
                    startActivity(intent);
                }
            });
            ll = findViewById(R.id.layout_profile);
            ll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), BasicProfileActivity.class);
                    startActivity(intent);
                }
            });
        }

        //????????????, ??????
        TedPermission.create()
                .setPermissionListener(permissionlistener)
                .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA)
                .setDeniedMessage("?????? ????????? ???????????????.\n???????????? ????????? ?????? ???????????????.")
                .check();


        //???????????????
        CardView routebutton = findViewById(R.id.routeButton);
        routebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent findPathIntent = new Intent(getApplicationContext(), FindPathActivity.class);

                startActivity(findPathIntent);
                overridePendingTransition(0, 0);

            }
        });

        //????????????
        CardView ridingButton = findViewById(R.id.ridingButton);
        ridingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent ridingIntent = new Intent(getApplicationContext(), RidingActivity.class);
                ridingIntent.putExtra("from","main");
                startActivity(ridingIntent);
            }
        });

        //main ?????? ?????????
        mainProgressBar = findViewById(R.id.mainProgressBar);
        mainProgressBar.getProgressDrawable().setColorFilter(Color.parseColor("#54A2FF"), PorterDuff.Mode.SRC_IN);
        mainRidingGoal = findViewById(R.id.mainRidingGoal);
        mainRidingAchievement = findViewById(R.id.mainRidingAchievement);

        int ridingGoal = pref.getInt("ridingGoal", 0);
        mainRidingGoal.setText((ridingGoal/1000)+"km");

        if (ridingGoal != 0) {
            int nowRidingAchievement = pref.getInt("ridingAchievement", 0);
            mainRidingAchievement.setText(Double.parseDouble(String.format("%.1f", (double) nowRidingAchievement / 1000))+"km");

            int percentRiding = (int) ((double)nowRidingAchievement/(double) ridingGoal * 100);
            mainProgressBar.setProgress(percentRiding);
        }

        myPost = findViewById(R.id.myPost);
        myPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MyPostActivity.class);
                startActivity(intent);
            }
        });



//        ???????????? ??????????????????
//        ???????????? ????????????
        TextView temperature = findViewById(R.id.temperatureText);
        TextView moisture = findViewById(R.id.moistureText);
        TextView pm10Text = findViewById(R.id.pm10Text);
        TextView pm25Text = findViewById(R.id.pm25Text);
        TextView weatherComment = findViewById(R.id.weatherText);
        FusedLocationProviderClient fusedLocationClient;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        int x = (int) convertGRID_GPS(0,location.getLatitude(),location.getLongitude()).x;
                        int y= (int) convertGRID_GPS(0,location.getLatitude(),location.getLongitude()).y;

                        Log.d("TAG", "mainLocation: ");
                        //?????????????????? ????????? ?????? ????????????


                        //??????
                        new WeatherApi(x,y,System.currentTimeMillis()).getApi().enqueue(new Callback<RealTimeWeather>() {
                            @Override
                            public void onResponse(Call<RealTimeWeather> call, Response<RealTimeWeather> response) {
                                Log.d("TAG", "realTimeWeathercode: " + response.message());
                                if (response.isSuccessful()) {
                                    Log.d("TAG", "realTimeWeathercode: " + response.message());
                                    weatherAnalysis.setWeatherData(response.body().response.body.items.item);

                                    //??????
                                    temperature.setText("\uD83C\uDF21"+ weatherAnalysis.getTemperature() + " ???");

                                    //??????
                                    LottieAnimationView weatherImage = findViewById(R.id.weatherImage);
                                    if (weatherAnalysis.getCloud() <= 1) {
                                        weatherImage.setAnimation(R.raw.animation_sunny);
                                    } else if (weatherAnalysis.getCloud() <= 3) {
                                        weatherImage.setAnimation(R.raw.animation_cloudy);
                                    } else {
                                        weatherImage.setAnimation(R.raw.animation_overcast);
                                    }
                                    weatherImage.playAnimation();
                                    weatherImage.setRepeatCount(LottieDrawable.INFINITE);

                                    //??????
                                    moisture.setText("????"+weatherAnalysis.getMoisture() + " %");

                                    //???
                                    if(weatherAnalysis.getNow_RainSnowType() == 1 ||weatherAnalysis.getNow_RainSnowType() == 2 ||
                                            weatherAnalysis.getNow_RainSnowType() == 5||weatherAnalysis.getNow_RainSnowType() == 6){
                                        weatherImage.setAnimation(R.raw.animation_rain);
                                    }


                                    //????????????
                                    new AirconditionApi().getApi().enqueue(new Callback<Aircondition>() {
                                        @Override
                                        public void onResponse(Call<Aircondition> call, Response<Aircondition> response) {
                                            weatherAnalysis.setAirconditionData(response.body().response.body.items);
                                            Log.d("TAG", "aircondition: " + weatherAnalysis.getPm10Average() + " " + weatherAnalysis.getPm25Average());

                                            pm10Text.setText(weatherAnalysis.getPm10Condition());
                                            if(weatherAnalysis.getPm10Condition().equals("??????")){pm10Text.setTextColor(Color.parseColor("#549cf8"));}
                                            else if(weatherAnalysis.getPm10Condition().equals("??????")){pm10Text.setTextColor(Color.parseColor("#5ac451"));}
                                            else if(weatherAnalysis.getPm10Condition().equals("??????")){pm10Text.setTextColor(Color.parseColor("#efa066"));}
                                            else if(weatherAnalysis.getPm10Condition().equals("??????")){pm10Text.setTextColor(Color.parseColor("#ec655f"));}

                                            pm25Text.setText(weatherAnalysis.getPm25Condition());
                                            if(weatherAnalysis.getPm25Condition().equals("??????")){pm25Text.setTextColor(Color.parseColor("#549cf8"));}
                                            else if(weatherAnalysis.getPm25Condition().equals("??????")){pm25Text.setTextColor(Color.parseColor("#5ac451"));}
                                            else if(weatherAnalysis.getPm25Condition().equals("??????")){pm25Text.setTextColor(Color.parseColor("#efa066"));}
                                            else if(weatherAnalysis.getPm25Condition().equals("??????")){pm25Text.setTextColor(Color.parseColor("#ec655f"));}

                                            weatherComment.setText(weatherAnalysis.makeComment());
                                        }

                                        @Override
                                        public void onFailure(Call<Aircondition> call, Throwable t) {

                                        }
                                    });

                                } else {
                                    Log.e("api_error", response.message());
                                }

                            }

                            @Override
                            public void onFailure(Call<RealTimeWeather> call, Throwable t) {
                                Log.e("weather_call_error", t.toString());
                            }
                        });


                    }
                });


        dangerFrameLayout = findViewById(R.id.dangerFrameLayout);
        dangerFrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent inforIntent = new Intent(getApplicationContext(), InformationWriteActivity.class);
                startActivity(inforIntent);
                overridePendingTransition(0, 0);
            }
        });

    }//onCreate()


    //?????????????????? ?????????(??????????????? ??????) tedpermission??? ??????
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

//
//    //Drawer ?????????
//    DrawerLayout.DrawerListener listener = new DrawerLayout.DrawerListener() {
//        @Override
//        public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {}
//        @Override
//        public void onDrawerOpened(@NonNull View drawerView) {}
//        @Override
//        public void onDrawerClosed(@NonNull View drawerView) {}
//        @Override
//        public void onDrawerStateChanged(int newState) {}
//    };
//
//    @Override
//    public void onBackPressed() {
//        if(isDrawerOpened){
//            drawerLayout.closeDrawer(Gravity.LEFT);
//            isDrawerOpened = false;
//        }
//        else if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
//            backKeyPressedTime = System.currentTimeMillis();
//            toast = Toast.makeText(this, "\'??????\' ????????? ?????? ??? ???????????? ???????????????.", Toast.LENGTH_SHORT);
//            toast.show();
//            return;
//        }
//        else if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
//            finish();
//            toast.cancel();
//        }
//    }

    private void getProfile(){
        api.getProfile(id).enqueue(new Callback<BasicProfile>() {
            @Override
            public void onResponse(Call<BasicProfile> call, Response<BasicProfile> response) {
                if(response.isSuccessful()){
                    String nickname = response.body().getNickname();
                    btnSigninOrNickname.setText(nickname);
                }else{
                    btnSigninOrNickname.setText(id);
                }
            }
            @Override
            public void onFailure(Call<BasicProfile> call, Throwable t) {
                btnSigninOrNickname.setText(id);
            }
        });
    }
    public void getTotalInfo(){
        api.getRidingTotal(id).enqueue(new Callback<GetRidingTotal>() {
            @Override
            public void onResponse(Call<GetRidingTotal> call, Response<GetRidingTotal> response) {
                if (response.isSuccessful()) {
                    String totalDistance = response.body().getTotalDistance();
                    int distance = (int) Math.round(Double.parseDouble(totalDistance));
                    String[] totalTime = response.body().getTotalTime().split(" : ");
                    String time="";
                    try {
                        time = totalTime[0] + "??? " + totalTime[1] + "???";
                        tvMainRidingTotal.setText("\uD83D\uDEB5 ??? ??????: " + distance +"m  "+"\n\uD83D\uDD51 ??? ??????: " + time);
                    } catch (Exception e) {
                        tvMainRidingTotal.setText("\uD83D\uDEB5 ??? ??????: 0m"+"  "+"\n\uD83D\uDD51 ??? ??????: 0 : 0");
                    }

                } else {
                    Toast.makeText(getApplicationContext(), response.message(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<GetRidingTotal> call, Throwable t) {}
        });
    }


    //????????? ?????? ?????? ?????? ?????? ????????? (????????? -> ????????? ?????? ?????????)
    private LatXLngY convertGRID_GPS(int mode, double lat_X, double lng_Y )
    {
        double RE = 6371.00877; // ?????? ??????(km)
        double GRID = 5.0; // ?????? ??????(km)
        double SLAT1 = 30.0; // ?????? ??????1(degree)
        double SLAT2 = 60.0; // ?????? ??????2(degree)
        double OLON = 126.0; // ????????? ??????(degree)
        double OLAT = 38.0; // ????????? ??????(degree)
        double XO = 43; // ????????? X??????(GRID)
        double YO = 136; // ???1?????? Y??????(GRID)

        //
        // LCC DFS ???????????? ( code : "TO_GRID"(?????????->??????, lat_X:??????,  lng_Y:??????), "TO_GPS"(??????->?????????,  lat_X:x, lng_Y:y) )
        //


        double DEGRAD = Math.PI / 180.0;
        double RADDEG = 180.0 / Math.PI;

        double re = RE / GRID;
        double slat1 = SLAT1 * DEGRAD;
        double slat2 = SLAT2 * DEGRAD;
        double olon = OLON * DEGRAD;
        double olat = OLAT * DEGRAD;

        double sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);
        double sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;
        double ro = Math.tan(Math.PI * 0.25 + olat * 0.5);
        ro = re * sf / Math.pow(ro, sn);
        LatXLngY rs = new LatXLngY();

        if (mode == 0) {
            rs.lat = lat_X;
            rs.lng = lng_Y;
            double ra = Math.tan(Math.PI * 0.25 + (lat_X) * DEGRAD * 0.5);
            ra = re * sf / Math.pow(ra, sn);
            double theta = lng_Y * DEGRAD - olon;
            if (theta > Math.PI) theta -= 2.0 * Math.PI;
            if (theta < -Math.PI) theta += 2.0 * Math.PI;
            theta *= sn;
            rs.x = Math.floor(ra * Math.sin(theta) + XO + 0.5);
            rs.y = Math.floor(ro - ra * Math.cos(theta) + YO + 0.5);
        }
        else {
            rs.x = lat_X;
            rs.y = lng_Y;
            double xn = lat_X - XO;
            double yn = ro - lng_Y + YO;
            double ra = Math.sqrt(xn * xn + yn * yn);
            if (sn < 0.0) {
                ra = -ra;
            }
            double alat = Math.pow((re * sf / ra), (1.0 / sn));
            alat = 2.0 * Math.atan(alat) - Math.PI * 0.5;

            double theta = 0.0;
            if (Math.abs(xn) <= 0.0) {
                theta = 0.0;
            }
            else {
                if (Math.abs(yn) <= 0.0) {
                    theta = Math.PI * 0.5;
                    if (xn < 0.0) {
                        theta = -theta;
                    }
                }
                else theta = Math.atan2(xn, yn);
            }
            double alon = theta / sn + olon;
            rs.lat = alat * RADDEG;
            rs.lng = alon * RADDEG;
        }
        return rs;
    }



    class LatXLngY
    {
        public double lat;
        public double lng;

        public double x;
        public double y;

    }
}