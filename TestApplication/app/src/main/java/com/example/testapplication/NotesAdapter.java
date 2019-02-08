package com.example.testapplication;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.noteViewHolder> {

    Context context;
    ArrayList<Notes> notes;
    /*
     * An on-click handler that we've defined to make it easy for an Activity to interface with
     * our RecyclerView
     */
    private final NotesAdapterOnClickHandler clickHandler;

    /**
     * The interface that receives onClick messages.
     */
    public interface NotesAdapterOnClickHandler {
        void onClick(int position);
    }

    /**
     * Creates a MovieAdapter.
     *
     * @param clickHandler The on-click handler for this adapter. This single handler is called
     *                     when an item is clicked.
     */
    public NotesAdapter(Context context, ArrayList<Notes> notes, NotesAdapterOnClickHandler clickHandler) {
        this.context = context;
        this.notes = notes;
        this.clickHandler = clickHandler;
    }

    @NonNull
    @Override
    public NotesAdapter.noteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.note, parent, false);
        return new noteViewHolder(view);    }

    @Override
    public void onBindViewHolder(@NonNull NotesAdapter.noteViewHolder holder, int position) {
        holder.setData(notes.get(position));

    }

    @Override
    public int getItemCount() {
        if (notes == null || notes.size() == 0)
            return 0;
        return notes.size();
    }

    public class noteViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView titleText;

        public noteViewHolder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.tvnote);
            itemView.setOnClickListener(this);
        }

        public void setData(Notes note) {
             titleText.setText(note.getNote());
        }

        @Override
        public void onClick(View view) {
            if (clickHandler != null) {
                int position = getAdapterPosition();
                clickHandler.onClick(position);
            }
        }
    }

    public void setNotesData(ArrayList<Notes> notes) {
        this.notes = notes;
        notifyDataSetChanged();
    }

    public ArrayList<Notes> getNotes() {
        return notes;
    }
}
