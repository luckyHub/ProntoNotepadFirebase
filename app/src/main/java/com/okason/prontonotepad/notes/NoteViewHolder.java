package com.okason.prontonotepad.notes;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.okason.prontonotepad.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by vokafor on 1/18/2017.
 */

public class NoteViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.text_view_note_title)
    TextView title;
    @BindView(R.id.text_view_note_date)
    TextView noteDate;
    @BindView(R.id.image_view_delete)
    ImageView delete;


    public NoteViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

}
