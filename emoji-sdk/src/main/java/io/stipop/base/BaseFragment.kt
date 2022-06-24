package io.stipop.base

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment

internal abstract class BaseFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyTheme()
    }

    abstract fun applyTheme()


}