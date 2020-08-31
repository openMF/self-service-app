package org.mifos.mobile.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import org.mifos.mobile.R
import org.mifos.mobile.models.beneficiary.Beneficiary
import javax.inject.Inject

/**
 * Created by dilpreet on 15/6/17.
 */
class BeneficiaryListAdapter @Inject constructor() :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var beneficiaryList: List<Beneficiary>? = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.row_beneficiary, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (beneficiaryList?.get(position) != null) {
            val (_, name, officeName, _, _, accountNumber, transferLimit) =
                    beneficiaryList?.get(position)!!
            (holder as ViewHolder).tvAccountNumber?.text = accountNumber
            holder.tvName?.text = name
            holder.tvOfficeName?.text = officeName
        }
    }

    override fun getItemCount(): Int {
        return if (beneficiaryList != null) beneficiaryList!!.size
        else 0
    }

    fun setBeneficiaryList(beneficiaryList: List<Beneficiary>?) {
        this.beneficiaryList = beneficiaryList
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        @JvmField
        @BindView(R.id.tv_beneficiary_name)
        var tvName: TextView? = null

        @JvmField
        @BindView(R.id.tv_account_number)
        var tvAccountNumber: TextView? = null

        @JvmField
        @BindView(R.id.tv_office_name)
        var tvOfficeName: TextView? = null

        init {
            ButterKnife.bind(this, itemView!!)
        }
    }
}