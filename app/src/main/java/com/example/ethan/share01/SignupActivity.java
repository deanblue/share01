package com.example.ethan.share01;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText user_id_edt;
    private EditText user_age_edt;
    private EditText user_password_edt;
    private EditText user_nick_edt;
    private EditText user_phone_edt;

    private Button sign_up_btn;

    private RadioButton user_sex_male_rb;
    private RadioButton user_sex_female_rb;

    private String getUserId;
    private String getUserAge;
    private String getUserPass;
    private String getUserSex;
    private String getUserNick;

    private String getUserPhone;
    private String getUserDeviceId;


    public static RbPreference mPref;
    private final String join_url = "https://toycom96.iptime.org:1443/user_join";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        init();
        sign_up_btn.setOnClickListener(this);
    }

    private void signupModule(String userId, String userPass, String userNick, String userAge, String userSex, String userDeviceId, String userPhone){
        mPref = new RbPreference(SignupActivity.this);
        if (userId != null && userPass != null && userNick != null){
            mPref.put("user_name", userId);

            HttpUtil http = new HttpUtil();
            http.execute(join_url,userId,userPass,userNick,userAge,userSex,userDeviceId,userPhone);

        } else {
            Toast.makeText(SignupActivity.this, "입력 실패", Toast.LENGTH_SHORT);
        }


    }
    private void init(){
        user_id_edt = (EditText) findViewById(R.id.signup_user_id);
        user_age_edt = (EditText) findViewById(R.id.signup_user_age);
        user_password_edt = (EditText) findViewById(R.id.signup_user_password);
        user_nick_edt = (EditText) findViewById(R.id.signup_user_nick);
        user_phone_edt = (EditText) findViewById(R.id.signup_user_phone);

        sign_up_btn = (Button) findViewById(R.id.signup_button);
        user_sex_female_rb = (RadioButton) findViewById(R.id.signup_user_sex_femail);
        user_sex_male_rb = (RadioButton) findViewById(R.id.signup_user_sex_male);
        phoneInfo();
    }

    private void phoneInfo() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_PHONE_STATE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        1122);
            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        1122);
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1122: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    TelephonyManager manager=(TelephonyManager) getSystemService(getApplicationContext().TELEPHONY_SERVICE);
                    getUserDeviceId=manager.getDeviceId();
                    getUserPhone =manager.getLine1Number();
                    user_phone_edt.setText(getUserPhone);

                } else {
                    Toast.makeText(this, "폰의 정보를 얻을 수 있어야 회원 가입이 가능합니다.", Toast.LENGTH_SHORT).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(SignupActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signup_button :
                if(user_id_edt.getText().toString().length() != 0 && user_password_edt.getText().toString().length() != 0 && user_age_edt.getText().toString().length() != 0 && user_nick_edt.getText().toString().length() != 0){
                    getUserId = user_id_edt.getText().toString();
                    getUserPass = user_password_edt.getText().toString();
                    getUserAge = user_age_edt.getText().toString();
                    getUserNick = user_nick_edt.getText().toString();

                    if(user_sex_male_rb.isChecked()){
                        getUserSex = "M";
                    }
                    if(user_sex_female_rb.isChecked()){
                        getUserSex = "F";
                    }


                    signupModule(getUserId,getUserPass,getUserNick,getUserAge,getUserSex,getUserDeviceId,getUserPhone);
                } else {
                    Toast.makeText(SignupActivity.this, "모든정보를 입력해주세요", Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    class HttpUtil extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(SignupActivity.this, "회원가입 완료", Toast.LENGTH_SHORT).show();
            mPref.put("is_login",true);
            CreateAuthUtil auth = new CreateAuthUtil(getApplicationContext());
            auth.execute(mPref.getValue("user_num",""),getUserDeviceId);

            Intent intent = new Intent(SignupActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        @Override
        protected Void doInBackground(String... value) {
            HttpURLConnection conn = null;
            OutputStream os = null;
            InputStream is = null;
            ByteArrayOutputStream baos = null;
            String response = null;
            /*
            http통신 부분 설정 변수들
             */
            String connUrl = value[0];
            String join_id = value[1];
            String join_pass = getMD5Hash(value[2]);
            String join_nick = value[3];
            int join_age = Integer.parseInt(value[4].toString());
            String join_sex = value[5];
            String join_device_id = value[6];
            String  join_phone = value[7];

            String join_photo = "";
            String join_coment = "";

            Log.e("user_id", join_id);
            Log.e("userpass", join_pass);
            Log.e("userDevice", join_device_id);
            mPref.put("user_id",join_id);

            try {
                IgnoreHttpSertification.ignoreSertificationHttps();
                //String url = "https://toycom96.iptime.org:1443/user_join";
                URL obj = new URL(connUrl);
                //접속 Server URL 설정
                conn = (HttpURLConnection) obj.openConnection();
                //Http 접속
                conn.setConnectTimeout(10000);
                //접속 timeuot시간 설정
                conn.setReadTimeout(10000);
                //read timeout 시간 설정
                conn.setRequestMethod("POST");
                //통신 방식 : POST

                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                //데이터 주고 받는 형식 : json 설정
                //conn.setRequestProperty("Cookie", "auth=NtUMVRdHefRNbYut82ALIz0hFKOyRM4D13krg/xdxWfrhThgkmDJTAbs7A3fbhd4lu4cIg==");
                //Cookie값 설정(auth)
                conn.setDoOutput(true);
                conn.setDoInput(true);


                JSONObject job = new JSONObject();
                //JSONObject 생성 후 input
                job.put("email", join_id);
                job.put("passwd", join_pass);
                job.put("name", join_nick);
                job.put("age", join_age);
                job.put("sex", join_sex);
                job.put("photo", join_photo);
                job.put("msg", join_coment);
                job.put("device_id", join_device_id);
                job.put("phone_num", join_phone);

                os = conn.getOutputStream();
                //Output Stream 생성
                os.write(job.toString().getBytes("utf-8"));
                os.flush();
                //Buffer에 있는 모든 정보를 보냄

                int responseCode = conn.getResponseCode();
                //request code를 받음

                if(responseCode == HttpURLConnection.HTTP_OK) {

                    Log.e("HTTP_OK", "HTTP OK RESULT");
                    is = conn.getInputStream();
                    baos = new ByteArrayOutputStream();
                    byte[] byteBuffer = new byte[1024];
                    byte[] byteData = null;
                    int nLength = 0;
                    while((nLength = is.read(byteBuffer, 0, byteBuffer.length)) != -1) {
                        baos.write(byteBuffer, 0, nLength);
                    }
                    byteData = baos.toByteArray();

                    response = new String(byteData);
                    //Json 문자열로 온 데이터값을 저장함( ex.> {"key":value} )
                    Log.i("Response Data", response);
                    JSONObject responseJSON = new JSONObject(response);
                    //JSONObject를 생성해 key값 설정으로 result값을 받음.
                    Log.i("Response ID Value", responseJSON.get("id").toString());
                    String result = responseJSON.get("id").toString();
                    //Toast.makeText(this, "Your id value : : " + result, Toast.LENGTH_SHORT);
                    Log.i("responese value", "DATA response = " + result);
                    mPref.put("user_num", result);
                    mPref.put("device_id", join_device_id);
                }else {
                    Log.e("HTTP_ERROR", "NOT CONNECTED HTTP");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }



    public static String getMD5Hash(String s) {
        MessageDigest m = null;
        String hash = null;

        try {
            m = MessageDigest.getInstance("MD5");
            m.update(s.getBytes(),0,s.length());
            hash = new BigInteger(1, m.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return hash;
    }

}
