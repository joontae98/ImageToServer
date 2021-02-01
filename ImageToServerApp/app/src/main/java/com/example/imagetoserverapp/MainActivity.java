package com.example.imagetoserverapp;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity {

    Bitmap bitmap;
    Button btnGet, btnUp, btnDown, btnSet;
    EditText etxMname, etxUname;
    ImageView viewImg;
    String image, mName, uName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        btnGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //갤러리 호출 메서드
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 1);
            }
        });
        btnUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mName = etxMname.getText().toString();
                if (!mName.isEmpty()){
                    imgUpload();
                }
            }
        });
        btnDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewImg.setImageBitmap(null);
                uName = etxUname.getText().toString();
                imgDownload();
            }
        });
        btnSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] data = Base64.decode(image, Base64.DEFAULT);
                viewImg.setImageBitmap(byteArrayToBitmap(data));
            }
        });
    }

    private void init() {
        btnGet = (Button) findViewById(R.id.btn_main_get);
        btnUp = (Button) findViewById(R.id.btn_main_up);
        btnDown = (Button) findViewById(R.id.btn_main_down);
        btnSet = (Button) findViewById(R.id.btn_main_set);
        viewImg = (ImageView) findViewById(R.id.view_main_img1);
        etxMname = (EditText) findViewById(R.id.etx_main_myName);
        etxUname = (EditText) findViewById(R.id.etx_main_uName);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // 갤러리 호출 종료 후 작동하는 메서드
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = data.getData();
                getBitmap(uri);
                viewImg.setImageBitmap(bitmap);
            }
        }
    }

    private void getBitmap(Uri uri) {
        // uri를 bitmap 으로 변환 메서드
        if (Build.VERSION.SDK_INT >= 29) {
            ImageDecoder.Source source = ImageDecoder.createSource(getApplicationContext().getContentResolver(), uri);
            try {
                bitmap = ImageDecoder.decodeBitmap(source);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String bitmapToBase64(Bitmap bitmap) {
        // bitmap -> byte[] -> String 으로 encoding
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return android.util.Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void imgUpload() {
        // 이미지 업로드 api 호출 메서드
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String url = "http://192.168.0.113:3000/process/up";
                StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String s) {
                                if(!s.isEmpty()){
                                    Log.e("err","이미있는 이름 입니다.");
                                    showMessage("이미있는 이름 입니다.");
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                            }
                        }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        String image = bitmapToBase64(bitmap);
                        Map<String, String> params = new HashMap<>();
                        params.put("image", image);
                        params.put("name", mName );
                        return params;
                    }
                };
                RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
                requestQueue.add(stringRequest);
            }
        });
        thread.start();
    }

    private void imgDownload() {
        //이미지 다운로드 api 호출 메서드
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String url = "http://192.168.0.113:3000/process/down";
                StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (!response.isEmpty()) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                Log.e("download image", jsonObject.getString("image"));
                                image = jsonObject.getString("image");
                                return;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        Log.e("err","없는 이름입니다.");
                        showMessage("없는 이름입니다.");
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }){
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("name", uName );
                        return params;
                    }};

                RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
                requestQueue.add(request);
            }
        });
        thread.start();
    }

    private Bitmap byteArrayToBitmap(byte[] byteArr) {
        //byte[] -> bitmap decoding 메서드
        return BitmapFactory.decodeByteArray(byteArr, 0, byteArr.length);
    }

    private void showMessage(String str) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("오류").setMessage(str);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}