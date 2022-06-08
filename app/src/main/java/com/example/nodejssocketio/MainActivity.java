package com.example.nodejssocketio;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {
    private ListView lvContent, lvUser;
    private EditText etContent;
    private ImageView ivAddUser, ivAddChat;
    private ArrayList<String> listUser, listChat;
    private ArrayAdapter adapterUser, adapterChat;
    private Socket mSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        anhXa();
        adapterUser = new ArrayAdapter(this, android.R.layout.simple_list_item_1, listUser);
        adapterChat = new ArrayAdapter(this, android.R.layout.simple_list_item_1, listChat);
        lvUser.setAdapter(adapterUser);
        lvContent.setAdapter(adapterChat);

        // Kết nối socket với địa chỉ ip
        try {
            mSocket = IO.socket("http://192.168.1.10:3000/");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        mSocket.connect();

        // Thêm user lên server
        ivAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = etContent.getText().toString().trim();
                // Nếu etContent có dữ liệu
                if (content.length() > 0) {
                    mSocket.emit("client-send-user", content);
                }
            }
        });

        ivAddChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String content = etContent.getText().toString().trim();
                if (content.length() > 0) {
                    mSocket.emit("client-send-chat", content);
                }
            }
        });
        // trả về xem user đã tồn tại hay chưa
        mSocket.on("server-send-result", onRetrieveData);
        // trả về array user để đưa lên list view user
        mSocket.on("server-send-listUser", onListUser);
        // Trả về content chat đưa lên listview
        mSocket.on("server-send-chat", onContentChat);
    }

    // Lấy content chat được trả về từ server và đưa lên mọi user
    private Emitter.Listener onContentChat = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject object = (JSONObject) args[0];
                    try {
                        String content = object.getString("noidung");
                        listChat.add(content);
                        adapterChat.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    // lấy array đc trả về từ server và add vào list
    private Emitter.Listener onListUser = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject object = (JSONObject) args[0];
                    listUser.clear();
                    try {
                        JSONArray array = object.getJSONArray("array");
                        for (int i = 0; i < array.length(); i++) {
                            listUser.add(array.getString(i));
                        }
                        adapterUser.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        }
    };

    // Mảng object
    // kiểm tra xem đã tồn tại user hay chưa
    private Emitter.Listener onRetrieveData = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            // tương tự như asyntask, có tác dụng tác động lên màn hình hiện tại
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject object = (JSONObject) args[0];
                    try {
                        boolean check = object.getBoolean("tontai");
                        if (check ) {
                            Toast.makeText(getApplicationContext(), "Đã có tài khoản", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Đăng kí thành công", Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };


    private void anhXa() {
        etContent = findViewById(R.id.et_content);
        ivAddUser = findViewById(R.id.iv_add_user);
        ivAddChat = findViewById(R.id.iv_add_chat);
        lvUser = findViewById(R.id.lv_list_user);
        lvContent = findViewById(R.id.lv_content);
        listUser = new ArrayList<>();
        listChat = new ArrayList<>();
    }

}