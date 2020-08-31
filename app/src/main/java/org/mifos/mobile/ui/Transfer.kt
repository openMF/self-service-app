package org.mifos.mobile.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.mifos.mobile.R
import org.mifos.mobile.ui.fragments.base.BaseFragment

class Transfer : BaseFragment() {
    companion object {
        fun newInstance(): Transfer = Transfer()
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_transfer, container, false)
    }
}