package org.mifos.mobile.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.mifos.mobile.R
import org.mifos.mobile.ui.fragments.base.BaseFragment

/**
 * This Fragment will be implemented in a future PR related to redesigning of ui.
 */

class TransferFragment : BaseFragment() {

    companion object {
        fun newInstance(): TransferFragment = TransferFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_transfer, container, false)
    }
}