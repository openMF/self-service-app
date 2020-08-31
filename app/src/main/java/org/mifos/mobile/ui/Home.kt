package org.mifos.mobile.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.fragment_account.*
import org.mifos.mobile.R
import org.mifos.mobile.ui.fragments.RecentTransactionsFragment
import org.mifos.mobile.ui.fragments.base.BaseFragment

class Home : BaseFragment(), View.OnClickListener {

    private lateinit var rootView: View
    private lateinit var sheetBehavior: BottomSheetBehavior<*>

    companion object {
        fun newInstance(): Home = Home()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_home_ui, container, false)
        val ft = childFragmentManager.beginTransaction()
        val rt = RecentTransactionsFragment()
        ft.replace(R.id.rv_recent_transactions, rt)
        ft.addToBackStack(null)
        ft.commit()
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sheetBehavior = BottomSheetBehavior.from(nsv_profile_bottom_sheet_dialog)
        sheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                // React to state change
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                    }
                    BottomSheetBehavior.STATE_SETTLING -> {
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // React to dragging events
            }
        })
    }

    override fun onClick(view: View) {

        when (view.id) {
        }
    }

}