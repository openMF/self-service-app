package org.mifos.mobile.ui

import android.accounts.Account
import android.os.Bundle
import android.provider.ContactsContract
import android.view.MenuItem
import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.mifos.mobile.R
import org.mifos.mobile.ui.activities.base.BaseActivity
import org.mifos.mobile.ui.enums.AccountType

class Main : BaseActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    private var bottomNavigationView: BottomNavigationView? = null
    private var menuItem = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        bottomNavigationView = findViewById(R.id.bottom_navigation)
        setupNavigationBar()
        bottomNavigationView?.run { setSelectedItemId(R.id.action_home)
        }


    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        clearFragmentBackStack()
        setToolbarElevation()
        menuItem = item.itemId
        navigateFragment(item.itemId,false)
        return true
    }
    private fun setupNavigationBar() {

        bottomNavigationView?.setOnNavigationItemSelectedListener(object : BottomNavigationView.OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(@NonNull item: android.view.MenuItem): Boolean {
                navigateFragment(item.itemId, false)
                return true
            }
        })

    }
    override fun onBackPressed() {
        val fragment: Fragment? = supportFragmentManager
                .findFragmentById(R.id.bottom_navigation_fragment_container)
        if (fragment != null && fragment !is Home && fragment.isVisible()) {
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
                R.id.action_home -> replaceFragment(Home.newInstance(), false,
                        R.id.bottom_navigation_fragment_container)
                R.id.action_transfer -> replaceFragment(Transfer.newInstance(), false,
                        R.id.bottom_navigation_fragment_container)
            }
        }
    }
}