package com.example.test3;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.google.android.libraries.places.api.model.Place;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchList_Activity extends AppCompatActivity {

    private ArrayList<Search_Dictionary> mArrayList, realList;
    private Search_CustomAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private int count = -1;

    private Button home_btn, school_btn, work_btn, delete;

    public boolean list_trans = false;

    final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    final List<Place.Field> fields = Arrays.asList(Place.Field.LAT_LNG, Place.Field.NAME);

    private SQLiteDatabase mDb,mDb2;

    public String Location;
    public int Loc_state;

    protected void onCreate (Bundle savedInstanceState) {
        Log.d("넘어ㅇ옴","넘어옴");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_list);

        //툴바 설정
        getSupportActionBar().setTitle("최근 기록");

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_main_list1);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        Search_DictionaryDbHelper dbHelper2 = new Search_DictionaryDbHelper(this);
        mDb2 = dbHelper2.getWritableDatabase();
        Cursor cursor2 = getAllDictionary();

        mAdapter = new Search_CustomAdapter(this,cursor2);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), mRecyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                String selectQuery = "SELECT * FROM " + Search_DictionaryContract.DictionaryEntry.TABLE_NAME;
                Cursor cursor = mDb2.rawQuery(selectQuery, null);
                cursor.moveToPosition(position);
                Intent sendIntent = new Intent(getBaseContext(), naviActivity.class);
                sendIntent.putExtra("start_name",cursor.getString(1));
                sendIntent.putExtra("end_name",cursor.getString(2));
                sendIntent.putExtra("start_lat",Double.parseDouble(cursor.getString(3)));
                sendIntent.putExtra("start_lon",Double.parseDouble(cursor.getString(4)));
                sendIntent.putExtra("end_lat",Double.parseDouble(cursor.getString(5)));
                sendIntent.putExtra("end_lon",Double.parseDouble(cursor.getString(6)));
                sendIntent.putExtra("state",1);
                startActivity(sendIntent);

            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

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

    }

    private boolean removeDictionary(long id) {
        return mDb2.delete(Search_DictionaryContract.DictionaryEntry.TABLE_NAME, Search_DictionaryContract.DictionaryEntry._ID + "=" + id, null) >0;
    }

    private Cursor getAllDictionary() {
        return mDb2.query(
                Search_DictionaryContract.DictionaryEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                Search_DictionaryContract.DictionaryEntry._ID
        );
    }

    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {
        private GestureDetector gestureDetector;
        private SearchList_Activity.ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final SearchList_Activity.ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildAdapterPosition(child));
                    }
                }
            });
        }


        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildAdapterPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean b) {

        }
    }

}
