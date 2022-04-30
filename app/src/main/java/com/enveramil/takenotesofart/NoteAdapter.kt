package com.enveramil.takenotesofart

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.view.menu.MenuView
import androidx.recyclerview.widget.RecyclerView
import com.enveramil.takenotesofart.databinding.RecyclerRowBinding

class NoteAdapter(val noteList : ArrayList<NotesModel>) : RecyclerView.Adapter<NoteAdapter.NoteHolder>() {
    class NoteHolder(val binding : RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root){}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteHolder {
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return NoteHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteHolder, position: Int) {
        holder.binding.recyclerRowTextView.text = noteList.get(position).name
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context,DetailsActivity::class.java)
            intent.putExtra("info","old") // kayıtlı sanat eserini gösterir.
            intent.putExtra("id",noteList.get(position).id)
            holder.itemView.context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return noteList.size
    }
}