package com.example.test3;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class Search_CustomAdapter extends RecyclerView.Adapter<Search_CustomAdapter.CustomViewHolder> {

    //private ArrayList<Search_Dictionary> mList;
    private Context mContext;
    private Cursor mCursor;
    private SQLiteDatabase mDb;

    public Search_CustomAdapter(Context context, Cursor cursor) {
        this.mContext = context;
        mCursor = cursor;

    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
        View view = LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.item_searchlist_, viewGroup, false);
        CustomViewHolder viewHolder = new CustomViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder viewHolder, int position) {
        viewHolder.start_name.setTextSize(TypedValue.COMPLEX_UNIT_SP,15);
        viewHolder.end_name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        viewHolder.start_name.setGravity(Gravity.CENTER);
        viewHolder.end_name.setGravity(Gravity.CENTER);

       // viewHolder.start_name.setText(mCursor.get(position).getStart_name());
        //viewHolder.end_name.setText(mList.get(position).getEnd_name());

        if (!mCursor.moveToPosition(position))
            return;
        String start_name = mCursor.getString(mCursor.getColumnIndex(Search_DictionaryContract.DictionaryEntry.START_NAME));
        String end_name = mCursor.getString(mCursor.getColumnIndex(Search_DictionaryContract.DictionaryEntry.END_NAME));
        long id = mCursor.getLong(mCursor.getColumnIndex(Search_DictionaryContract.DictionaryEntry._ID));


        viewHolder.start_name.setText(start_name);
        viewHolder.end_name.setText(end_name);
        viewHolder.search1.setImageResource(R.drawable.search1);
        viewHolder.itemView.setTag(id);
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

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        protected  TextView start_name;
        protected  TextView end_name;
        protected ImageView search1;

        public CustomViewHolder(View view) {
            super(view);
            this.start_name = (TextView) view.findViewById(R.id.recyclerview_start);
            this.end_name = (TextView) view.findViewById(R.id.recyclerview_end);
            this.search1 = (ImageView) view.findViewById(R.id.recyclerview_search1);

        }
        private Cursor getAllDictionary() {
            return mDb.query(
                    Search_DictionaryContract.DictionaryEntry.TABLE_NAME,
                    null,
                    null,
                    null,
                    null,
                    null,
                    Search_DictionaryContract.DictionaryEntry.COLUMN_TIMESTAMP
            );
        }
    }
}
