package com.example.ethan.share01;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.ethan.share01.adapter.ChattingRoomAdapter;
import com.example.ethan.share01.model.ChattingRoom;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Lai.OH on 2016-07-27.
 */

public class ChatListActivity extends AppCompatActivity {

    private ChattingRoomAdapter mChatListAdapter;
    private ListView chatting_room_lv;
    private ArrayList<ChattingRoom> mChatRooms = null;

    private TextView recv_id_tv;
    private TextView msg_tv;
    private TextView time_tv;

    private RbPreference mPref = new RbPreference(ChatListActivity.this);

    private final String SERVER_URL = "https://toycom96.iptime.org:1443/chat_list";

    private int getChatroomId;
    private int getRecvId;
    private String getRecvName;
    private String getMsg;
    private String getTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);
        init();
    }

    private void init(){
        chatting_room_lv = (ListView) findViewById(R.id.chattingroom_listview);


        mChatRooms = new ArrayList<>();

        ChatListLoadThread chatlist = new ChatListLoadThread();
        chatlist.execute(SERVER_URL, mPref.getValue("auth", ""));

        chatting_room_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ChattingRoom chat_room = mChatRooms.get(position);
                int chat_room_id = chat_room.getChatRoomID();
                int chat_room_user_id = chat_room.getRecv_id();
                Intent intent = new Intent(ChatListActivity.this, ChatActivity.class);
                intent.putExtra("room_id", chat_room_id);
                intent.putExtra("sender_id", chat_room_user_id);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ChatListActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    /*
     * auth 값을 http header로 보내 사용자 정보를 받아오는 Thread
     */
    class ChatListLoadThread extends AsyncTask<String, Void, Void> {

        ProgressDialog loading;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading = new ProgressDialog(ChatListActivity.this);
            loading.setTitle("채팅방 리스트");
            loading.setMessage("채팅방 리스트를 받는 중이에요...");
            loading.setCancelable(false);
            loading.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            //Toast.makeText(UserInfoEditActivity.this, "정보 확인", Toast.LENGTH_SHORT).show();

            /*user_id.setText(mPref.getValue("user_id", ""));
            user_nick.setText(getUserNick);
            user_age.setText(getUserAge);
            user_coment.setText(getUserComent);*/

            mChatListAdapter = new ChattingRoomAdapter(ChatListActivity.this, mChatRooms);

            chatting_room_lv.setAdapter(mChatListAdapter);
            loading.dismiss();
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
            String user_auth = value[1];

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
                conn.addRequestProperty("Cookie", user_auth);
                //Cookie값 설정(auth)
                conn.setDoOutput(true);
                conn.setDoInput(true);


                os = conn.getOutputStream();
                //Output Stream 생성
                os.flush();
                //Buffer에 있는 모든 정보를 보냄

                int responseCode = conn.getResponseCode();

                //int responseCode = conn.getResponseCode();
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

                    JSONArray ja = new JSONArray(response);
                    for (int i = 0; i < ja.length(); i++) {
                        JSONObject order = ja.getJSONObject(i);

                        getChatroomId = Integer.parseInt(order.get("Id").toString());
                        getRecvId = Integer.parseInt(order.get("Recv_id").toString());
                        getRecvName = order.get("User_name").toString();
                        getMsg = order.get("Msg").toString();
                        getTime = order.get("Sended").toString();

                        mChatRooms.add(new ChattingRoom(getChatroomId,getRecvId, getRecvName, getMsg, getTime));
                    }
                    Log.i("Response Data", response);
                    //JSONObject responseJSON = new JSONObject(response);
                    //JSONObject를 생성해 key값 설정으로 result값을 받음.



                }else {
                    Log.e("HTTP_ERROR", "NOT CONNECTED HTTP");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}