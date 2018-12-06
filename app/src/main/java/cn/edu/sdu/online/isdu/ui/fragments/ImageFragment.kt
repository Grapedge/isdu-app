package cn.edu.sdu.online.isdu.ui.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.ui.activity.GuideActivity
import com.bumptech.glide.Glide

class ImageFragment : Fragment() {
    private val ARG_PARAM_IMG_SRC = "src"
    private val ARG_PARAM_INDEX = "index"

    private var param1: Int? = null
    private var imageView: ImageView? = null
    private var index: Int? = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getInt(ARG_PARAM_IMG_SRC)
            index = it.getInt(ARG_PARAM_INDEX)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_image, container, false)
        imageView = view.findViewById(R.id.image_view)
        Glide.with(context!!)
                .load(param1 ?: 0)
                .into(imageView!!)

//        imageView!!.setImageResource(param1 ?: 0)
        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @return A new instance of fragment ImageFragment.
         */
        @JvmStatic
        fun newInstance(param1: Int, param2: Int) =
                ImageFragment().apply {
                    arguments = Bundle().apply {
                        putInt(ARG_PARAM_IMG_SRC, param1)
                        putInt(ARG_PARAM_INDEX, param2)
                    }
                }
    }
}
