package com.example.test3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.CustomViewHolder> {

    private Context mContext;
    private Cursor mCursor;
    private SQLiteDatabase mDb;


    public CustomAdapter(Context context, Cursor cursor) {
        this.mContext = context;
        mCursor = cursor;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_list_tmp, viewGroup, false);

        CustomViewHolder viewHolder = new CustomViewHolder(view);

        return viewHolder;
    }

    //포지션을 입력 받아서 해당 데이터를 UI에 출력하는 역할
    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder viewholder, int position) {
        //해당 포지션으로 이동
        //FALSE면 데이터 없다는 듯
        if (!mCursor.moveToPosition(position))
            return;
        String name = mCursor.getString(mCursor.getColumnIndex(DictionaryContract.DictionaryEntry.COLUMN_NAME));
        String phoneNum = mCursor.getString(mCursor.getColumnIndex(DictionaryContract.DictionaryEntry.COLUMN_phoneNum));
        long id = mCursor.getLong(mCursor.getColumnIndex(DictionaryContract.DictionaryEntry._ID));

        viewholder.mName.setText(name);
        viewholder.mPhoneNum.setText(phoneNum);
        viewholder.mCircle.setImageResource(R.drawable.ic_lightbulb_outline_black_24dp);
        viewholder.itemView.setTag(id);

    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        if (mCursor != null)
            mCursor.close();
        mCursor = newCursor;
        if (newCursor != null) {
            this.notifyDataSetChanged();
        }
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder { // 1. 리스너 추가

        protected TextView mName;
        protected TextView mPhoneNum;
        protected CircleImageView mCircle;

        public CustomViewHolder(View view) {
            super(view);
            this.mName = (TextView) view.findViewById(R.id.textview_recyclerview_name);
            this.mPhoneNum = (TextView) view.findViewById(R.id.textview_recyclerview_phoneNum);
            this.mCircle = (CircleImageView) view.findViewById(R.id.circleView);

            //view.setOnCreateContextMenuListener(this); //2. 리스너 등록
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
}
