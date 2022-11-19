package edu.bluejack22_1.JoCards

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import org.w3c.dom.Text

class CardAdapter(private val cardList:ArrayList<Card>) : RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    var onItemClick : ((Card) -> Unit)? = null
    private lateinit var userEmail : String

    class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardTopic : TextView =  itemView.findViewById(R.id.card_topic)
        val cardQuestion : TextView =  itemView.findViewById(R.id.card_question)
        val cardCreated : TextView =  itemView.findViewById(R.id.card_created)
        val cardMineIndicator : TextView = itemView.findViewById(R.id.card_mine_indicator)
        val eachCardItem : LinearLayout = itemView.findViewById(R.id.each_card_item)
        val isLoginGoogle = GoogleSignIn.getLastSignedInAccount(itemView.context)
        val isLoginAuth = FirebaseAuth.getInstance().currentUser
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.each_card, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = cardList[position]
        holder.cardTopic.setText(card.topic)
        holder.cardQuestion.setText(card.question)
        holder.cardCreated.setText(card.created)

        if(holder.isLoginGoogle != null){
            userEmail = holder.isLoginGoogle.email.toString()
        }
        if(holder.isLoginAuth != null){
            userEmail = holder.isLoginAuth.email.toString()
        }
        if(userEmail != null && userEmail != card.creator) {
            holder.cardMineIndicator.setText("")
        }

        holder.eachCardItem.setOnClickListener {
            onItemClick?.invoke(card)
        }
    }

    override fun getItemCount(): Int {
        return cardList.size
    }
}