package org.mifos.mobile.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick

import org.mifos.mobile.R
import org.mifos.mobile.injection.ActivityContext
import org.mifos.mobile.models.FAQ
import org.mifos.mobile.utils.FaqDiffUtil

import javax.inject.Inject

/**
 * Created by dilpreet on 12/8/17.
 */
class FAQAdapter @Inject constructor(@ActivityContext context: Context) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var alreadySelectedPosition = 0
    private var faqArrayList: ArrayList<FAQ?>?
    private val context: Context

    fun setFaqArrayList(faqArrayList: ArrayList<FAQ?>?) {
        this.faqArrayList = faqArrayList
        alreadySelectedPosition = -1
    }

    fun updateList(faqArrayList: java.util.ArrayList<FAQ?>?) {
        val diffResult = DiffUtil.calculateDiff(FaqDiffUtil(this.faqArrayList,
                faqArrayList))
        diffResult.dispatchUpdatesTo(this)
        setFaqArrayList(faqArrayList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.row_faq, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val (question, answer, isSelected) = faqArrayList?.get(position)!!
        (holder as ViewHolder).tvFaqQs?.text = question
        holder.tvFaqAns?.text = answer
        if (isSelected) {
            holder.tvFaqAns?.visibility = View.VISIBLE
            holder.ivArrow?.setImageDrawable(ContextCompat.getDrawable(context,
                    R.drawable.ic_arrow_up))
        } else {
            holder.tvFaqAns?.visibility = View.GONE
            holder.ivArrow?.setImageDrawable(ContextCompat.getDrawable(context,
                    R.drawable.ic_arrow_drop_down))
        }
    }

    fun updateView(position: Int) {
        if (alreadySelectedPosition != -1) {
            faqArrayList?.get(alreadySelectedPosition)?.isSelected = false
            notifyItemChanged(alreadySelectedPosition)
        }
        if (alreadySelectedPosition == position) {
            faqArrayList?.get(alreadySelectedPosition)?.isSelected = false
            notifyItemChanged(alreadySelectedPosition)
            alreadySelectedPosition = -1
            return
        }
        faqArrayList?.get(position)?.isSelected = true
        notifyItemChanged(position)
        alreadySelectedPosition = position
    }

    override fun getItemCount(): Int {
        return if (faqArrayList != null) faqArrayList!!.size
        else 0
    }

    inner class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        @JvmField
        @BindView(R.id.tv_qs)
        var tvFaqQs: TextView? = null

        @JvmField
        @BindView(R.id.tv_ans)
        var tvFaqAns: TextView? = null

        @JvmField
        @BindView(R.id.ll_faq)
        var llFaq: LinearLayout? = null

        @JvmField
        @BindView(R.id.iv_arrow)
        var ivArrow: ImageView? = null

        @OnClick(R.id.ll_faq)
        fun faqHeaderClicked() {
            updateView(adapterPosition)
        }

        init {
            ButterKnife.bind(this, itemView!!)
        }
    }

    init {
        faqArrayList = ArrayList()
        this.context = context
    }
}