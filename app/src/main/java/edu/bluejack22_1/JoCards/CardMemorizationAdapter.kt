package edu.bluejack22_1.JoCards

import android.opengl.Visibility
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth

class CardMemorizationAdapter(private var cardList:ArrayList<CardMemorization>) : RecyclerView.Adapter<CardMemorizationAdapter.CardViewHolder>() {

    var onItemClick : ((CardMemorization) -> Unit)? = null
    private lateinit var userEmail : String

    public fun setCardList(filteredList : ArrayList<CardMemorization>) {
        this.cardList = filteredList
        notifyDataSetChanged()
    }

    class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val cardTopic : TextView =  itemView.findViewById(R.id.card_topic)
        val cardQuestion : TextView =  itemView.findViewById(R.id.card_question)
        val cardCreated : TextView =  itemView.findViewById(R.id.card_created)
        val cardMineIndicator : TextView = itemView.findViewById(R.id.card_mine_indicator)
        val isLoginGoogle = GoogleSignIn.getLastSignedInAccount(itemView.context)
        val isLoginAuth = FirebaseAuth.getInstance().currentUser
        val eachCardItem : LinearLayout = itemView.findViewById(R.id.each_card_item)
        var addCardIndicator : ImageView = itemView.findViewById(R.id.add_card_indicator)
        var removeCardIndicator : ImageView = itemView.findViewById(R.id.remove_card_indicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.each_card_memorization, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = cardList[position]
        holder.cardTopic.setText(card.topic)
        holder.cardQuestion.setText(card.question)
        holder.cardCreated.setText(card.created)

        if(card.isAdded) {
            holder.addCardIndicator.visibility = View.GONE
            holder.removeCardIndicator.visibility = View.VISIBLE
        } else {
            holder.addCardIndicator.visibility = View.VISIBLE
            holder.removeCardIndicator.visibility = View.GONE
        }

        if (holder.isLoginGoogle != null) {
            userEmail = holder.isLoginGoogle.email.toString()
        }
        if (holder.isLoginAuth != null) {
            userEmail = holder.isLoginAuth.email.toString()
        }
        if (userEmail != null && userEmail != card.creator) {
            holder.cardMineIndicator.setText("")
        }

        holder.eachCardItem.setOnClickListener {
            if(Memorization.memorizationArr.size == 5) {
                card.isAdded = true
            }
            if(!card.isAdded) {
                holder.addCardIndicator.visibility = View.GONE
                holder.removeCardIndicator.visibility = View.VISIBLE
            } else {
                holder.addCardIndicator.visibility = View.VISIBLE
                holder.removeCardIndicator.visibility = View.GONE
            }
            card.isAdded = !card.isAdded
            Log.v("jojojo", "card.isAdded $card.isAdded")
            onItemClick?.invoke(card)
        }
    }
    override fun getItemCount(): Int {
        return cardList.size
    }
}