package org.mifos.mobile.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.content_fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.*
import org.mifos.mobile.R
import org.mifos.mobile.ui.activities.NotificationActivity
import org.mifos.mobile.ui.fragments.base.BaseFragment

class NewHomeFragment : BaseFragment(), View.OnClickListener {

    private var currentSelectedButton = 1;

    companion object {
        fun newInstance(): NewHomeFragment = NewHomeFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        var rootView: View =  inflater.inflate(R.layout.fragment_home, container, false)

        val ft = childFragmentManager.beginTransaction()
        val rt = RecentTransactionsFragment()
        ft.replace(R.id.fl_recentTransactions, rt)
        ft.addToBackStack(null)
        ft.commit()

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        iv_notification.setOnClickListener(this)
        btn_all.setOnClickListener(this)
        btn_third_party.setOnClickListener(this)
        btn_self.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when(view?.id){
            R.id.iv_notification -> {
                startActivity(Intent(context, NotificationActivity::class.java))
            }

            R.id.btn_all -> {
                if (currentSelectedButton != 1){
                    currentSelectedButton = 1
                    updateUI()
                }
            }

            R.id.btn_third_party -> {
                if (currentSelectedButton != 2){
                    currentSelectedButton = 2
                    updateUI()
                }
            }

            R.id.btn_self -> {
                if (currentSelectedButton != 3){
                    currentSelectedButton = 3
                    updateUI()
                }
            }
        }
    }

    private fun updateUI(){
        when(currentSelectedButton){
            1 -> {
                btn_all.chipBackgroundColor = ContextCompat.getColorStateList(context!!, R.color.accent)
                btn_all.setTextColor(ContextCompat.getColor(context!!, R.color.white))
                btn_third_party.chipBackgroundColor = ContextCompat.getColorStateList(context!!, R.color.white)
                btn_third_party.setTextColor(ContextCompat.getColor(context!!, R.color.accent))
                btn_self.chipBackgroundColor = ContextCompat.getColorStateList(context!!, R.color.white)
                btn_self.setTextColor(ContextCompat.getColor(context!!, R.color.accent))
            }

            2 -> {
                btn_all.chipBackgroundColor = ContextCompat.getColorStateList(context!!, R.color.white)
                btn_all.setTextColor(ContextCompat.getColor(context!!, R.color.accent))
                btn_third_party.chipBackgroundColor = ContextCompat.getColorStateList(context!!, R.color.accent)
                btn_third_party.setTextColor(ContextCompat.getColor(context!!, R.color.white))
                btn_self.chipBackgroundColor = ContextCompat.getColorStateList(context!!, R.color.white)
                btn_self.setTextColor(ContextCompat.getColor(context!!, R.color.accent))
            }

            3 -> {
                btn_all.chipBackgroundColor = ContextCompat.getColorStateList(context!!, R.color.white)
                btn_all.setTextColor(ContextCompat.getColor(context!!, R.color.accent))
                btn_third_party.chipBackgroundColor = ContextCompat.getColorStateList(context!!, R.color.white)
                btn_third_party.setTextColor(ContextCompat.getColor(context!!, R.color.accent))
                btn_self.chipBackgroundColor = ContextCompat.getColorStateList(context!!, R.color.accent)
                btn_self.setTextColor(ContextCompat.getColor(context!!, R.color.white))
            }
        }
    }
}