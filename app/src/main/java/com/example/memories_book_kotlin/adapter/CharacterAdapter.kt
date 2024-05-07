package com.example.memories_book_kotlin.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.memories_book_kotlin.databinding.RecyclerRowBinding
import com.example.memories_book_kotlin.model.Relation_Character
import com.example.memories_book_kotlin.view.MemoriesActivity

class CharacterAdapter (val characterList : List<Relation_Character>): RecyclerView.Adapter<CharacterAdapter.CharacterHolder>(){
      class CharacterHolder(val binding : RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root){

      }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacterHolder {
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return CharacterHolder(binding)
    }

    override fun getItemCount(): Int {
        return characterList.size
    }

    override fun onBindViewHolder(holder: CharacterHolder, position: Int) {
        holder.binding.recyclerViewTextView.text=characterList.get(position).fullName
        //Android Geliştiriciler Intent'in burada yapılmasını tercih etmezler, Interface ile yaparlar
        holder.itemView.setOnClickListener{
            val intent = Intent(holder.itemView.context,MemoriesActivity::class.java)
            intent.putExtra("selectedCharacter",characterList.get(position))
            intent.putExtra("info", "old")
            holder.itemView.context.startActivity(intent)
        }
    }
}