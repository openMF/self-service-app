package org.mifos.mobile.ui.fragments

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.github.therajanmaurya.sweeterror.SweetUIErrorHandler
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.mifos.mobile.R
import org.mifos.mobile.api.local.PreferencesHelper
import org.mifos.mobile.models.client.Client
import org.mifos.mobile.models.client.Group
import org.mifos.mobile.presenters.UserDetailsPresenter
import org.mifos.mobile.ui.activities.EditUserDetailActivity
import org.mifos.mobile.ui.activities.base.BaseActivity
import org.mifos.mobile.ui.fragments.base.BaseFragment
import org.mifos.mobile.ui.views.UserDetailsView
import org.mifos.mobile.utils.Constants
import org.mifos.mobile.utils.DateHelper
import org.mifos.mobile.utils.TextDrawable
import org.mifos.mobile.utils.Toaster
import javax.inject.Inject

/**
 * Created by dilpreet on 10/7/17.
 */
class UserProfileFragment : BaseFragment(), UserDetailsView {
    @kotlin.jvm.JvmField
    @BindView(R.id.iv_profile)
    var ivProfile: ImageView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.iv_text_drawable)
    var ivTextDrawable: ImageView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.app_bar_layout)
    var appBarLayout: AppBarLayout? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.collapsing_toolbar)
    var collapsingToolbarLayout: CollapsingToolbarLayout? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.tv_user_name)
    var tvUsername: TextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.tv_account_number)
    var tvAccountNumber: TextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.tv_activation_date)
    var tvActivationDate: TextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.tv_office_name)
    var tvOfficeName: TextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.tv_groups)
    var tvGroups: TextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.tv_client_type)
    var tvClientType: TextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.tv_client_classification)
    var tvClientClassification: TextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.tv_phone_number)
    var tvPhoneNumber: TextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.tv_dob)
    var tvDOB: TextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.tv_gender)
    var tvGender: TextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.toolbar)
    var toolbar: Toolbar? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.layout_error)
    var layoutError: View? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.ll_user_profile)
    var llUserProfile: LinearLayout? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.fab_edit)
    var fabEdit: FloatingActionButton? = null

    @kotlin.jvm.JvmField
    @Inject
    var presenter: UserDetailsPresenter? = null

    @kotlin.jvm.JvmField
    @Inject
    var preferencesHelper: PreferencesHelper? = null
    private lateinit var rootView: View
    private var userBitmap: Bitmap? = null
    private var client: Client? = null
    private var sweetUIErrorHandler: SweetUIErrorHandler? = null
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_user_profile, container, false)
        (activity as BaseActivity?)?.activityComponent?.inject(this)
        ButterKnife.bind(this, rootView)
        presenter?.attachView(this)
        (activity as BaseActivity?)?.setSupportActionBar(toolbar)
        (activity as BaseActivity?)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        collapsingToolbarLayout?.setCollapsedTitleTextColor(ContextCompat.getColor(activity!!,
                R.color.white))
        collapsingToolbarLayout?.setExpandedTitleColor(ContextCompat.getColor(activity!!,
                R.color.white))
        sweetUIErrorHandler = SweetUIErrorHandler(activity, rootView)
        if (savedInstanceState == null) {
            presenter?.userDetails
            presenter?.userImage
        }
        return rootView
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(Constants.USER_DETAILS, client)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            client = savedInstanceState.getParcelable(Constants.USER_DETAILS)
            presenter?.setUserProfile(preferencesHelper?.userProfileImage)
            showUserDetails(client)
        }
    }

    /**
     * Sets client basic details which are fetched from server
     *
     * @param client instance of [Client] which contains information about client
     */
    override fun showUserDetails(client: Client?) {
        this.client = client
        tvUsername?.text = nullFieldCheck(getString(R.string.username), client?.displayName)
        tvAccountNumber?.text = nullFieldCheck(getString(R.string.account_number),
                client?.accountNo)
        tvActivationDate?.text = nullFieldCheck(getString(R.string.activation_date),
                DateHelper.getDateAsString(client?.activationDate))
        tvOfficeName?.text = nullFieldCheck(getString(R.string.office_name),
                client?.officeName)
        tvClientType?.text = nullFieldCheck(getString(R.string.client_type),
                client?.clientType?.name)
        tvGroups?.text = nullFieldCheck(getString(R.string.groups),
                getGroups(client?.groups))
        tvClientClassification?.text = nullFieldCheck(getString(R.string.client_classification),
                client?.clientClassification?.name)
        tvPhoneNumber?.text = nullFieldCheck(getString(R.string.phone_number),
                client?.mobileNo)
        if (client?.dobDate?.size != 3) {  // no data entry in database for the client
            tvDOB?.text = getString(R.string.no_dob_found)
        } else {
            tvDOB?.text = DateHelper.getDateAsString(client.dobDate)
        }
        tvGender?.text = nullFieldCheck(getString(R.string.gender), client?.gender?.name)
    }

    private fun nullFieldCheck(field: String, value: String?): String {
        return value
                ?: getString(R.string.no) + getString(R.string.blank) + field +
                getString(R.string.blank) + getString(R.string.found)
    }

    /**
     * Generate a string for groups which the client is part of.
     *
     * @param groups [List] of [Group] which client is a part of.
     * @return Returns String of groups
     */
    private fun getGroups(groups: List<Group>?): String {
        if (groups?.isEmpty() == true) {
            return getString(
                    R.string.not_assigned_with_any_group) // no groups entry in database for the
            // client
        }
        val builder = StringBuilder()
        if (groups != null)
            for ((_, _, name) in groups) {
                builder.append(getString(R.string.string_and_string, name, " | "))
            }
        return builder.toString().substring(0, builder.toString().length - 2)
    }

    /**
     * Provides with client Image fetched from the server in `bitmap`
     *
     * @param bitmap User Image
     */
    override fun showUserImage(bitmap: Bitmap?) {
        activity?.runOnUiThread {
            userBitmap = bitmap
            if (userBitmap == null) {
                val textDrawable = TextDrawable.builder()
                        .beginConfig()
                        .toUpperCase()
                        .endConfig()
                        .buildRound(
                                (if (preferencesHelper?.clientName.isNullOrEmpty()) preferencesHelper
                                        ?.userName
                                else preferencesHelper
                                        ?.clientName)
                                        ?.substring(0, 1),
                                ContextCompat.getColor(context!!, R.color.primary_dark))
                ivProfile?.visibility = View.GONE
                ivTextDrawable?.visibility = View.VISIBLE
                ivTextDrawable?.setImageDrawable(textDrawable)
            } else {
                ivTextDrawable?.visibility = View.GONE
                ivProfile?.visibility = View.VISIBLE
                ivProfile?.setImageBitmap(bitmap)
            }
        }
    }

    @OnClick(R.id.btn_change_password)
    fun changePassword() {
        startActivity(Intent(context, EditUserDetailActivity::class.java))
    }

    /**
     * It is called whenever any error occurs while executing a request
     *
     * @param message Error message that tells the user about the problem.
     */
    override fun showError(message: String?) {
        Toaster.show(rootView, message)
        sweetUIErrorHandler?.showSweetCustomErrorUI(getString(R.string.error_fetching_user_profile),
                R.drawable.ic_error_black_24dp, appBarLayout,
                layoutError)
        fabEdit?.visibility = View.GONE
    }

    override fun showProgress() {
        showProgressBar()
    }

    override fun hideProgress() {
        hideProgressBar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideProgress()
        presenter?.detachView()
    }

    companion object {
        @kotlin.jvm.JvmStatic
        fun newInstance(): UserProfileFragment {
            return UserProfileFragment()
        }
    }
}