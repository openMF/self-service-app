package org.mifos.mobile.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

import butterknife.BindView
import butterknife.ButterKnife

import org.mifos.mobile.R
import org.mifos.mobile.models.accounts.savings.TransactionType
import org.mifos.mobile.models.accounts.savings.Transactions
import org.mifos.mobile.utils.CurrencyUtil.formatCurrency
import org.mifos.mobile.utils.DateHelper.getDateAsString

import java.util.*
import javax.inject.Inject

/**
 * Created by dilpreet on 6/3/17.
 */
class SavingAccountsTransactionListAdapter @Inject constructor() :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var savingAccountsTransactionList: List<Transactions?>?
    private var context: Context? = null
    fun setContext(context: Context?) {
        this.context = context
    }

    fun getItem(position: Int?): Transactions? {
        return position?.let { savingAccountsTransactionList?.get(it) }
    }

    fun setSavingAccountsTransactionList(savingAccountsTransactionList: List<Transactions?>?) {
        if (savingAccountsTransactionList != null) this.savingAccountsTransactionList = savingAccountsTransactionList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val vh: RecyclerView.ViewHolder
        val v = LayoutInflater.from(parent.context).inflate(
                R.layout.row_saving_account_transaction, parent, false)
        vh = ViewHolder(v)
        return vh
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val (_, transactionType, _, _, date, currency, paymentDetailData, amount, runningBalance) = getItem(position)!!
        (holder as ViewHolder).tvSavingAccountAmount?.text = context?.getString(R.string.string_and_string, currency?.displaySymbol, formatCurrency(context, amount!!))
        holder.tvSavingAccountRunningBalance?.text = context?.getString(R.string.string_and_string, currency?.displaySymbol, formatCurrency(context, runningBalance!!))
        holder.tvTransactionType?.text = transactionType?.value
        if (paymentDetailData != null) {
            holder.tvTransactionDetailData?.visibility = View.VISIBLE
            holder.tvTransactionDetailData?.text = paymentDetailData.paymentType.name
        } else {
            holder.tvTransactionDetailData?.visibility = View.GONE
        }
        holder.tvTransactionDate?.text = getDateAsString(date)
        val color = getColor(transactionType)
        if (color == ColorSelect.RED) {
            holder.vIndicator?.rotation = 180f
            holder.vIndicator?.background = ContextCompat.getDrawable(context!!, R.drawable.triangular_red_view)
        } else {
            holder.vIndicator?.rotation = 0f
            holder.vIndicator?.background = ContextCompat.getDrawable(context!!, R.drawable.triangular_green_view)
        }
    }

    private enum class ColorSelect {
        RED, GREEN
    }

    override fun getItemCount(): Int {
        return if (savingAccountsTransactionList != null) savingAccountsTransactionList!!.size
        else 0
    }

    private fun getColor(transactionType: TransactionType?): ColorSelect {
        if (transactionType?.deposit == true) {
            return ColorSelect.GREEN
        }
        if (transactionType?.dividendPayout == true) {
            return ColorSelect.RED
        }
        if (transactionType?.withdrawal == true) {
            return ColorSelect.RED
        }
        if (transactionType?.interestPosting == true) {
            return ColorSelect.GREEN
        }
        if (transactionType?.feeDeduction == true) {
            return ColorSelect.RED
        }
        if (transactionType?.initiateTransfer == true) {
            return ColorSelect.RED
        }
        if (transactionType?.approveTransfer == true) {
            return ColorSelect.RED
        }
        if (transactionType?.withdrawTransfer == true) {
            return ColorSelect.RED
        }
        if (transactionType?.rejectTransfer == true) {
            return ColorSelect.GREEN
        }
        return if (transactionType?.overdraftFee == true) {
            ColorSelect.RED
        } else ColorSelect.GREEN
    }

    class ViewHolder(v: View?) : RecyclerView.ViewHolder(v!!) {

        @JvmField
        @BindView(R.id.tv_transaction_date)
        var tvTransactionDate: TextView? = null

        @JvmField
        @BindView(R.id.tv_transaction_type)
        var tvTransactionType: TextView? = null

        @JvmField
        @BindView(R.id.tv_transaction_detail_data)
        var tvTransactionDetailData: TextView? = null

        @JvmField
        @BindView(R.id.tv_savingAccountAmount)
        var tvSavingAccountAmount: TextView? = null

        @JvmField
        @BindView(R.id.tv_saving_account_running_balance)
        var tvSavingAccountRunningBalance: TextView? = null

        @JvmField
        @BindView(R.id.v_indicator)
        var vIndicator: View? = null

        init {
            ButterKnife.bind(this, v!!)
        }
    }

    init {
        savingAccountsTransactionList = ArrayList()
    }
}