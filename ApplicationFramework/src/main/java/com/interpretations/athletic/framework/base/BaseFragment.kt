package com.interpretations.athletic.framework.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.interpretations.athletic.framework.R

open class BaseFragment : Fragment() {

    // context and activity
    // the [fragmentActivity] and [fragmentContext] are non-null [Activity] and [Context] objects
    protected lateinit var fragmentActivity: Activity
    protected lateinit var fragmentContext: Context

    // onRemoveFragment listener
    private var onRemoveFragmentListener: OnRemoveFragment? = null

    /**
     * Set onRemoveListener used for inheritance
     *
     * @param fragment The Fragment to be removed
     */
    fun setOnRemoveListener(fragment: OnRemoveFragment) {
        onRemoveFragmentListener = fragment
    }

    /**
     * Method is used to pop the top state off the back stack. Returns true if there
     * was one to pop, else false. This function is asynchronous -- it enqueues the
     * request to pop, but the action will not be performed until the application
     * returns to its event loop.
     */
    fun popBackStack() {
        if (activity == null) {
            return
        }

        try {
            activity?.supportFragmentManager?.popBackStack()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Method is used to add fragment to the current stack
     *
     * @param containerViewId Optional identifier of the container this fragment
     * is to be placed in. If 0, it will not be placed in a container.
     * @param fragment The new Fragment that is going to replace the container.
     */
    fun addFragment(containerViewId: Int, fragment: Fragment) {
        if (activity == null) {
            return
        }

        // check if the fragment has been added already
        val temp = activity?.supportFragmentManager?.findFragmentByTag(
            fragment.javaClass.simpleName
        )
        if (temp != null && temp.isAdded) {
            return
        }
        // replace fragment and transition
        if (topFragment != null && topFragment?.tag?.isNotEmpty() == true &&
            topFragment?.isAdded == true
        ) {
            return
        }
        // add fragment and transition with animation
        val transaction = activity?.supportFragmentManager?.beginTransaction()
        transaction?.setCustomAnimations(
            R.anim.ui_slide_in_from_bottom,
            R.anim.ui_slide_out_to_bottom, R.anim.ui_slide_in_from_bottom,
            R.anim.ui_slide_out_to_bottom
        )?.add(
            containerViewId, fragment,
            fragment.javaClass.simpleName
        )?.addToBackStack(fragment.javaClass.simpleName)

        try {
            transaction?.commit()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            // used as last resort
            transaction?.commitAllowingStateLoss()
        }
    }

    /**
     * Method is used to add fragment to the current stack without animation
     *
     * @param containerViewId Optional identifier of the container this fragment
     * is to be placed in. If 0, it will not be placed in a container.
     * @param fragment The new Fragment that is going to replace the container.
     */
    fun addFragmentNoAnim(containerViewId: Int, fragment: Fragment) {
        if (activity == null) {
            return
        }

        // check if the fragment has been added already
        val temp = activity?.supportFragmentManager?.findFragmentByTag(
            fragment.javaClass.simpleName
        )
        if (temp != null && temp.isAdded) {
            return
        }
        // replace fragment and transition
        if (topFragment != null && topFragment?.tag?.isNotEmpty() == true &&
            topFragment?.isAdded == true
        ) {
            return
        }
        // add fragment and transition with animation
        val transaction = activity?.supportFragmentManager?.beginTransaction()
        transaction?.add(
            containerViewId, fragment,
            fragment.javaClass.simpleName
        )?.addToBackStack(fragment.javaClass.simpleName)

        try {
            transaction?.commit()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            // used as last resort
            transaction?.commitAllowingStateLoss()
        }
    }

    /**
     * Method is used to add fragment with replace to stack without animation.
     * When Fragment is replaced all current fragments on the backstack are removed.
     *
     * @param fragment The Fragment to be added
     */
    fun addFragmentReplaceNoAnim(fragment: Fragment) {
        if (activity == null) {
            return
        }

        // check if the fragment has been added already
        val temp = activity?.supportFragmentManager?.findFragmentByTag(
            fragment.javaClass.simpleName
        )
        if (temp != null && temp.isAdded) {
            return
        }

        // replace fragment and transition
        if (topFragment != null && topFragment?.tag?.isNotEmpty() == true &&
            topFragment?.isAdded == true
        ) {
            // pop back stack
            popBackStack()
        }

        // add fragment and transition without animation
        val transaction = activity?.supportFragmentManager?.beginTransaction()

        try {
            transaction?.commit()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            // used as last resort
            transaction?.commitAllowingStateLoss()
        }
    }

    /**
     * Method for removing the Fragment view
     */
    fun remove() {
        if (activity == null) {
            return
        }

        // remove fragment with animation
        val transaction = activity?.supportFragmentManager?.beginTransaction()
        transaction?.setCustomAnimations(
            R.anim.ui_slide_in_from_bottom,
            R.anim.ui_slide_out_to_bottom
        )
        transaction?.remove(this)

        try {
            transaction?.commit()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            // used as last resort
            transaction?.commitAllowingStateLoss()
        }
        activity?.supportFragmentManager?.popBackStack()
    }

    /**
     * Method for removing the Fragment view with no animation
     */
    fun removeNoAnim() {
        if (activity == null) {
            return
        }

        // remove fragment without animation
        val transaction = activity?.supportFragmentManager?.beginTransaction()
        transaction?.remove(this)

        try {
            transaction?.commit()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            // used as last resort
            transaction?.commitAllowingStateLoss()
        }
    }

    /**
     * Method is used to retrieve the current fragment the user is on
     *
     * @return Returns the TopFragment if there is one, otherwise returns null
     */
    private val topFragment: Fragment?
        get() {
            if (activity != null) {
                val backStackEntryCount = activity?.supportFragmentManager?.backStackEntryCount ?: 0
                if (backStackEntryCount > 0) {
                    var i = backStackEntryCount
                    while (i >= 0) {
                        i--
                        val topFragment = activity?.supportFragmentManager?.fragments?.get(i)
                        if (topFragment != null) {
                            return topFragment
                        }
                    }
                }
            }
            return null
        }

    /**
     * Method is used to re-direct to a different Activity with no transition
     *
     * @param clazz         The in-memory representation of a Java class
     * @param intent           An intent is an abstract description of an operation to be performed
     * @param isClearBackStack If set in an Intent passed to Context.startActivity(),
     * this flag will cause any existing task that would be associated
     * with the activity to be cleared before the activity is started
     */
    fun goToActivity(
        clazz: Class<*>,
        intent: Intent?,
        isClearBackStack: Boolean? = true
    ) {
        // set intent
        val i = intent ?: Intent(fragmentContext, clazz)
        if (isClearBackStack == true) {
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        } else {
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        if (!fragmentActivity.isFinishing) {
            // start activity
            startActivity(i)
        }
    }

    /**
     * Method is used to re-direct to different Activity from a fragment with a
     * transition animation slide in from bottom of screen
     *
     * @param clazz         The in-memory representation of a Java class
     * @param intent           An intent is an abstract description of an operation to be performed
     * @param isClearBackStack If set in an Intent passed to Context.startActivity(),
     * this flag will cause any existing task that would be associated
     * with the activity to be cleared before the activity is started
     */
    fun goToActivityAnimInFromBottom(
        clazz: Class<*>,
        intent: Intent?,
        isClearBackStack: Boolean? = true
    ) {
        // set intent
        val i = intent ?: Intent(fragmentContext, clazz)
        if (isClearBackStack == true) {
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        } else {
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        if (!fragmentActivity.isFinishing) {
            // start activity
            startActivity(i)
            // transition animation
            fragmentActivity.overridePendingTransition(
                R.anim.ui_slide_in_from_bottom,
                R.anim.ui_slide_out_to_bottom
            )
        }
    }

    /**
     * Method is used to re-direct to different Activity from a fragment with a
     * transition animation slide in from bottom of screen
     *
     * @param clazz         The in-memory representation of a Java class
     * @param intent           An intent is an abstract description of an operation to be performed
     * @param isClearBackStack If set in an Intent passed to Context.startActivity(),
     * this flag will cause any existing task that would be associated
     * with the activity to be cleared before the activity is started
     */
    fun goToActivityAnimInFromTop(
        clazz: Class<*>,
        intent: Intent?,
        isClearBackStack: Boolean? = true
    ) {
        // set intent
        val i = intent ?: Intent(fragmentContext, clazz)
        if (isClearBackStack == true) {
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        } else {
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        if (!fragmentActivity.isFinishing) {
            // start activity
            startActivity(i)
            // transition animation
            fragmentActivity.overridePendingTransition(
                R.anim.ui_slide_in_from_top,
                R.anim.ui_slide_out_to_top
            )
        }
    }

    override fun onDetach() {
        super.onDetach()
        if (onRemoveFragmentListener != null) {
            onRemoveFragmentListener?.onRemove()
            onRemoveFragmentListener = null
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
        if (fragmentContext is Activity) {
            fragmentActivity = fragmentContext as Activity
        }
    }

    /**
     * Method for removing a fragment
     */
    interface OnRemoveFragment {
        fun onRemove()
    }
}