package com.example.test3;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;


public class MessageSettingActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private CustomAdapter mAdapter;
    private SQLiteDatabase mDb;
    DictionaryDbHelper dbHelper;
    private EditText mNewNameText;
    private EditText mNewPhoneText;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_setting);

        //툴바 설정
        getSupportActionBar().setTitle("최근 기록");

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_main_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        DictionaryDbHelper dbHelper = new DictionaryDbHelper(this);
        mDb = dbHelper.getWritableDatabase();
        Cursor cursor = getAllDictionary();

        mAdapter = new CustomAdapter(this, cursor); //데이터를 표시할 커서를 위한 어댑터 생성
        mRecyclerView.setAdapter(mAdapter); //리사이클뷰에 어탭터 추가

        // 목록에서 항목을 왼쪽, 오른쪽 방향으로 스와이프 하는 항목을 처리
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            // 사용하지 않는다.
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                // 스와이프된 아이템의 아이디를 가져온다.
                long id = (long) viewHolder.itemView.getTag();
                // DB 에서 해당 아이디를 가진 레코드를 삭제한다.
                removeDictionary(id);
                // 리스트를 갱신한다.
                mAdapter.swapCursor(getAllDictionary());
            }
        }).attachToRecyclerView(mRecyclerView);  //리사이클러뷰에 itemTouchHelper 를 붙인다.


        Button buttonInsert = (Button) findViewById(R.id.button_main_insert);
        buttonInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectQuery = "SELECT * FROM " + DictionaryContract.DictionaryEntry.TABLE_NAME;
                Cursor cursor2 = mDb.rawQuery(selectQuery, null);
                while(cursor2.moveToNext()) {
                    Log.d("값은 : ",cursor2.getString(2));
                    try {
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(cursor2.getString(2), null, sms, null, null);
                        Toast.makeText(getApplicationContext(), "전송 완료!", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "SMS faild, please try again later!", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.account_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.account_plus:
                AlertDialog.Builder builder = new AlertDialog.Builder(MessageSettingActivity.this);
                View view = LayoutInflater.from(MessageSettingActivity.this)
                        .inflate(R.layout.edit_box, null, false);
                builder.setView(view); //alertDialog의 builder를 사용해 알림창을 띄운다.
                final Button ButtonSubmit = (Button) view.findViewById(R.id.button_dialog_submit);
                final EditText mNewNameText = (EditText) view.findViewById(R.id.edittext_dialog_name);
                final EditText mNewPhoneText = (EditText) view.findViewById(R.id.edittext_dialog_phoneNum);
                ButtonSubmit.setText("삽입");

                final AlertDialog dialog = builder.create(); //알림창 객체 설정
                ButtonSubmit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("tag :",mNewNameText.getText().toString());
                        if (mNewNameText.getText().length() == 0 ||
                                mNewPhoneText.getText().length() == 0) {
                            return;
                        }
                        addNewData(mNewNameText.getText().toString(), mNewPhoneText.getText().toString());
                        mAdapter.swapCursor(getAllDictionary());

                        dialog.dismiss();
                    }
                });
                dialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    private long addNewData(String name, String phoneNum) {
        ContentValues cv = new ContentValues();
        cv.put(DictionaryContract.DictionaryEntry.COLUMN_NAME, name);
        cv.put(DictionaryContract.DictionaryEntry.COLUMN_phoneNum, phoneNum);

        return mDb.insert(DictionaryContract.DictionaryEntry.TABLE_NAME,null,cv);
    }

    private boolean removeDictionary(long id) {
        return mDb.delete(DictionaryContract.DictionaryEntry.TABLE_NAME, DictionaryContract.DictionaryEntry._ID + "=" + id, null) >0;
    }


    private Cursor getAllDictionary() {
        return mDb.query(
                DictionaryContract.DictionaryEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                DictionaryContract.DictionaryEntry.COLUMN_TIMESTAMP
        );
    }
}
