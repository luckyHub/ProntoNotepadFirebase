package com.okason.prontonotepad.notes;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.okason.prontonotepad.R;
import com.okason.prontonotepad.listeners.NoteItemListener;
import com.okason.prontonotepad.model.Note;
import com.okason.prontonotepad.util.TimeUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;



/**
 * Created by Valentine on 2/6/2016.
 */
public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.ViewHolder> {
    private List<Note> mNotes;
    private NoteItemListener mItemListener;
    private View noteView;


    public NotesAdapter(List<Note> notes) {
        mNotes = notes;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        noteView = inflater.inflate(R.layout.row_note_list, parent, false);
        return new ViewHolder(noteView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Note note = mNotes.get(position);

        holder.title.setText(note.getTitle());
        holder.noteDate.setText(TimeUtils.getTimeAgo(note.getDateModified()));

        if (note.getColor() != 0) {
            noteView.setBackgroundColor(note.getColor());

        }

        holder.title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mItemListener.onNoteClick(note);
            }
        });

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mItemListener.onDeleteButtonClicked(note);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mNotes.size();
    }

    public Note getItem(int position) {
        return mNotes.get(position);
    }

    public void replaceData(List<Note> notes) {
        setList(notes);
        notifyDataSetChanged();
    }

    private void setList(List<Note> notes) {
        mNotes = notes;
    }

    public void setNoteItemListener(NoteItemListener listener) {
        mItemListener = listener;
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.text_view_note_title)
        TextView title;
        @BindView(R.id.text_view_note_date)
        TextView noteDate;
        @BindView(R.id.image_view_delete)
        ImageView delete;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            Note note = getItem(position);
            mItemListener.onNoteClick(note);

        }
    }





    public Note removeItem(int position) {
        final Note note = mNotes.remove(position);
        notifyItemRemoved(position);
        return note;
    }

    public void addItem(int position, Note note) {
        mNotes.add(position, note);
        notifyItemInserted(position);
    }


    public void animateTo(List<Note> notes) {
        applyAndAnimateRemovals(notes);
        applyAndAnimateAdditions(notes);
        applyAndAnimateMovedItems(notes);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final Note note = mNotes.remove(fromPosition);
        mNotes.add(toPosition, note);
        notifyItemMoved(fromPosition, toPosition);
    }

    private void applyAndAnimateRemovals(List<Note> notes) {
        for (int i = mNotes.size() - 1; i >= 0; i--) {
            final Note note = mNotes.get(i);
            if (!notes.contains(note)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<Note> notes) {
        for (int i = 0, count = notes.size(); i < count; i++) {
            final Note note = notes.get(i);
            if (!mNotes.contains(note)) {
                addItem(i, note);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<Note> notes) {
        for (int toPosition = notes.size() - 1; toPosition >= 0; toPosition--) {
            final Note note = notes.get(toPosition);
            final int fromPosition = mNotes.indexOf(note);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

}
