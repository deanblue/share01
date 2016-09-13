package com.example.ethan.share01;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

public class DetailContents extends AppCompatActivity {

    public ViewPager pager;
    private Context mContext = null;
    private int board_id;
    private final String join_url = "https://toycom96.iptime.org:1443/bbs_view";
    private RbPreference mPref = new RbPreference(DetailContents.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_contents);

        ListSearch http = new ListSearch();

        pager = (ViewPager) findViewById(R.id.pager);
        //pager 생성 ( 이미지가 여러개일 경우 슬라이드 이미지 전환가능한 부분 )
        Intent intent = getIntent();
        board_id = intent.getExtras().getInt("id");
        // 클릭한 사진의 id값을 받아옴

        Log.e("Detail View ", Integer.toString(board_id));

        Log.e("Detail auth ", mPref.getValue("auth", ""));

        http.execute(join_url, Integer.toString(board_id), mPref.getValue("auth", ""));
        // 서버 주소와 게시판 id값을 함께 넘겨주어 id값 활용
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(DetailContents.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    class ListSearch extends AsyncTask<String, Void, Void> {

        int id;
        int user_id;
        String title;
        String msg;
        String media;
        String option;
        double glat;
        double glong;
        int gdist;
        int glidx;
        int term;
        // JSON통신 후, response값들 사용하든 안하든 일단 모든 값 받아서 저장하기 위한 변수(g는 중복이 있어 받아오는 get의 약자)
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            DetailPagerAdapter adapter = new DetailPagerAdapter(getLayoutInflater(), media, mContext);
            // 통신이 완료된 후, media값을 넘겨줌, 추후에 사진의 갯수를 확인 하고 그에따라 media 배열로 변경
            pager.setAdapter(adapter);
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


            try {
                IgnoreHttpSertification.ignoreSertificationHttps();
                //String url = "https://toycom96.iptime.org:1443/user_join";
                URL obj = new URL(connUrl);
                //접속 Server URL 설정
                conn = (HttpURLConnection) obj.openConnection();
                //Http 접속
                conn.setConnectTimeout(5000);
                //접속 timeuot시간 설정
                conn.setReadTimeout(5000);
                //read timeout 시간 설정
                conn.setRequestMethod("POST");
                //통신 방식 : POST

                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Cookie", value[2]);
                //데이터 주고 받는 형식 : json 설정
                //conn.setRequestProperty("Cookie", "auth=NtUMVRdHf/RNbYut82ALI4Jznf2w3gRPkCyl0w7GCoxqeIi0kDLV83XvDN3zARhFI6wcww==");
                //Cookie값 설정(auth)
                conn.setDoOutput(true);
                conn.setDoInput(true);


                JSONObject job = new JSONObject();
                //JSONObject 생성 후 input
                job.put("id", Integer.parseInt(value[1]));
                job.put("long", 126.7459979);
                job.put("lat", 37.259485);

                os = conn.getOutputStream();
                //Output Stream 생성
                os.write(job.toString().getBytes("utf-8"));
                os.flush();
                //Buffer에 있는 모든 정보를 보냄

                int responseCode = conn.getResponseCode();
                //request code를 받음

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.e("HTTP_OK", "HTTP OK RESULT");
                    is = conn.getInputStream();
                    baos = new ByteArrayOutputStream();
                    byte[] byteBuffer = new byte[1024];
                    byte[] byteData = null;
                    int nLength = 0;
                    while ((nLength = is.read(byteBuffer, 0, byteBuffer.length)) != -1) {
                        baos.write(byteBuffer, 0, nLength);
                    }
                    byteData = baos.toByteArray();

                    response = new String(byteData);
                    JSONObject responseJSON = new JSONObject(response);
                    Log.i("Response ", response);
                    // 받아온 값이 배열에 어떻게 들어있는지에 대한 1차적인 확인
                    id = (int) responseJSON.get("Id");
                    Log.i("response id", responseJSON.getString("Id"));
                    user_id = (int) responseJSON.get("User_id");
                    title = (String) responseJSON.get("Title");
                    msg = (String) responseJSON.get("Msg");
                    Log.i("media value", media = responseJSON.getString("Media"));
                    // 현재는 media만 사용하였으므로 media값 확인하기 위한 Log
                    option = (String) responseJSON.get("Option");
                    glat = (double) responseJSON.get("Lat");
                    glong = (double) responseJSON.get("Long");
                    gdist = (int) responseJSON.get("Dist");
                    glidx = (int) responseJSON.get("Lidx");
                    term = (int) responseJSON.get("Term");
                    Log.i("response value", "DATA response = " + response);
                    //넘어오는 값들의 변수형 확인 후 변경 필요
                } else {
                    Log.e("HTTP_ERROR", "NOT CONNECTED HTTP");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
