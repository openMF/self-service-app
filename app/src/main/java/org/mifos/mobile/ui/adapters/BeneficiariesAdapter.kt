package org.mifos.mobile.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.mifos.mobile.R
import org.mifos.mobile.models.beneficiary.Beneficiary

class BeneficiariesAdapter(val beneficiariesList: ArrayList<Beneficiary>) : RecyclerView.Adapter<BeneficiariesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_beneficiaries, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return beneficiariesList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val list: Beneficiary = beneficiariesList[position]
        holder.textViewName?.text = list.name
        holder.textViewDescription?.text = list.name
        holder.textViewPrice?.text = list.accountNumber
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewName = itemView.findViewById<TextView>(R.id.tv_name)
        val textViewDescription = itemView.findViewById<TextView>(R.id.tv_beneficiaries_decription)
        val textViewPrice = itemView.findViewById<TextView>(R.id.tv_price)
    }
}