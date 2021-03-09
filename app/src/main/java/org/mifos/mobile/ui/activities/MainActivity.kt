package org.mifos.mobile.ui.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.mifos.mobile.R
import org.mifos.mobile.ui.activities.base.BaseActivity
import org.mifos.mobile.ui.fragments.AccountFragment
import org.mifos.mobile.ui.fragments.NewHomeFragment
import org.mifos.mobile.ui.fragments.ProfileFragment
import org.mifos.mobile.ui.fragments.TransferFragment

class MainActivity : BaseActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    private var bottomNavigationView: BottomNavigationView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigationView = findViewById(R.id.bottom_navigation)
        setupNavigationBar()
        bottomNavigationView?.run {
            setSelectedItemId(R.id.action_home)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        clearFragmentBackStack()
        setToolbarElevation()
        navigateFragment(item.itemId,false)
        return true
    }

    private fun setupNavigationBar() {
        bottomNavigationView?.setOnNavigationItemSelectedListener { item ->
            navigateFragment(item.itemId, false)
            true
        }
    }

    override fun onBackPressed() {
        val fragment: Fragment? = supportFragmentManager
                .findFragmentById(R.id.bottom_navigation_fragment_container)
        if (fragment != null && fragment !is NewHomeFragment && fragment.isVisible()) {
            navigateFragment(R.id.action_home, true)
            return
        }
        val count = supportFragmentManager.backStackEntryCount
        if (count == 0) {
            super.onBackPressed()
            //additional code
        } else {
            supportFragmentManager.popBackStack()
        }
    }

    private fun navigateFragment(id: Int, shouldSelect: Boolean) {
        if (shouldSelect) {
            bottomNavigationView?.setSelectedItemId(id)
        } else {
            when (id) {
                R.id.action_home -> replaceFragment(NewHomeFragment.newInstance(), false,
                        R.id.bottom_navigation_fragment_container)
                R.id.action_acounts -> replaceFragment(AccountFragment.newInstance(), false,
                        R.id.bottom_navigation_fragment_container)
                R.id.action_transfer -> replaceFragment(TransferFragment.newInstance(), false,
                        R.id.bottom_navigation_fragment_container)
                R.id.action_profile -> replaceFragment(ProfileFragment.newInstance(), false,
                        R.id.bottom_navigation_fragment_container)
            }
        }
    }
}