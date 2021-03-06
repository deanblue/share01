package com.example.ethan.share01;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by ethan on 16. 6. 17..
 */
public class ContentsListLoad {

    static List<ContentsListObject> mContentItem = new ArrayList<>();
    static ContentsListAdapter mAdapter;
    private String mDistFlag = "0";
    private final Lock _mutex = new ReentrantLock(true);


    public ContentsListLoad (List<ContentsListObject> ContentItem, ContentsListAdapter ListAdapter) {
        this.mContentItem = ContentItem;
        this.mAdapter = ListAdapter;
    }

    public int loadFromApi(int ListIndex, int dist_flag, String auth, RecyclerView recyclerView, Context context) {

        getBbsList BbsList = new getBbsList(mContentItem, mAdapter,recyclerView, context);

        String IdxStr = String.valueOf(ListIndex);
        String DistStr = String.valueOf(dist_flag);

        BbsList.execute(IdxStr, DistStr, auth);

        return 0;
    }


    class getBbsList extends AsyncTask<String, Void, Void> {
        private List<ContentsListObject> mContentItem;
        public ContentsListAdapter mAdapter;
        private RecyclerView mRecyclerView;
        private  Context mContext;

        public getBbsList (List<ContentsListObject> ContentItem, ContentsListAdapter ListAdapter,RecyclerView recyclerView, Context context) {
            this.mContentItem = ContentItem;
            this.mAdapter = ListAdapter;
            this.mRecyclerView = recyclerView;
            this.mContext = context;
        }

        protected void onPostExecute(Void aVoid) {

            /*
             * doInBackground에서 모든 데이터를 add한 뒤 adapter에 연결 후 recyclerView에 뿌려준다.
             */
            mAdapter = new ContentsListAdapter(mContext, mContentItem);
            mRecyclerView.setAdapter(mAdapter);

        }

        @Override
        protected Void doInBackground(String... value) {
            HttpURLConnection conn = null;
            OutputStream os = null;
            InputStream is = null;
            ByteArrayOutputStream baos = null;
            String response = null;

            try {

                IgnoreHttpSertification.ignoreSertificationHttps();
                URL obj = new URL("https://toycom96.iptime.org:1443/bbs_list");
                conn = (HttpURLConnection) obj.openConnection();
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(5000);
                conn.setRequestMethod("POST");

                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Cookie", value[2]);
                conn.setDoOutput(true);
                conn.setDoInput(true);

                JSONObject job = new JSONObject();
                job.put("long", 126.7459979);
                job.put("lat", 37.259485);
                job.put("dist", Integer.parseInt(value[1]));
                job.put("lidx", Integer.parseInt(value[0]));

                os = conn.getOutputStream();
                os.write(job.toString().getBytes("utf-8"));
                os.flush();

                int responseCode = conn.getResponseCode();
                //request code를 받음

                if (responseCode == HttpURLConnection.HTTP_OK) {
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
                    Log.e("ItemResponse", response.toString());
                } else {
                    return null;
                }

                String result = "";
                if (response.isEmpty() || response.equals("null") ) {
                    /*
                     * response가 null인 경우, 즉 반경내의 게시글이 없을경우
                     * item을 clear 해준다
                     * 안해줄시 그전의 내용이 남아있음.
                     */
                    mContentItem.clear();
                    return null;
                }
                JSONArray ja = new JSONArray(response);
                /*
                 * 반경에 따른 response값을 adding 해주기전 clear를 해야 item들이 겹치지 않는다.
                 */
                mContentItem.clear();

                for (int i = 0; i < ja.length(); i++) {
                    JSONObject order = ja.getJSONObject(i);
                    this.mContentItem.add(new ContentsListObject(order.getInt("Id"), order.getInt("User_id"), order.getString("Media"), order.getString("Title")));

                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
            }

            return null;
        }
    }
}
