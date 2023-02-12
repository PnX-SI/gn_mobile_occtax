package fr.geonature.occtax.ui.input.counting

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import fr.geonature.commons.util.ThemeUtils
import fr.geonature.occtax.R

/**
 * Custom dialog fragment to show a bottom sheet to let the user to choose how to add photo.
 *
 * @author S. Grimault
 */
class AddPhotoBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private var listener: OnAddPhotoBottomSheetDialogFragmentListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.bottom_sheet_photo,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<RecyclerView>(android.R.id.list).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = MenuItemAdapter(
                listOf(
                    MenuItem(
                        R.drawable.ic_add_photo,
                        R.string.counting_bottom_sheet_photo_camera
                    ),
                    MenuItem(
                        R.drawable.ic_image,
                        R.string.counting_bottom_sheet_photo_gallery
                    )
                )
            )
        }.also {
            it.addItemDecoration(
                DividerItemDecoration(
                    context,
                    (it.layoutManager as LinearLayoutManager).orientation
                )
            )
        }
    }

    fun setOnAddPhotoBottomSheetDialogFragmentListener(listener: OnAddPhotoBottomSheetDialogFragmentListener) {
        this.listener = listener
    }

    data class MenuItem(
        @DrawableRes val iconResourceId: Int,
        @StringRes val labelResourceId: Int
    )

    /**
     * Callback used by [AddPhotoBottomSheetDialogFragment].
     */
    interface OnAddPhotoBottomSheetDialogFragmentListener {
        fun onSelectMenuItem(menuItem: MenuItem)
    }

    inner class MenuItemAdapter(private val items: List<MenuItem>) :
        RecyclerView.Adapter<MenuItemAdapter.MenuItemHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuItemHolder {
            return MenuItemHolder(parent)
        }

        override fun onBindViewHolder(holder: MenuItemHolder, position: Int) {
            holder.onBind(items[position])
        }

        override fun getItemCount(): Int {
            return items.size
        }

        inner class MenuItemHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.list_item_1,
                parent,
                false
            )
        ) {
            fun onBind(menuItem: MenuItem) {
                itemView.findViewById<TextView>(android.R.id.text1).apply {
                    setText(menuItem.labelResourceId)
                    setCompoundDrawablesWithIntrinsicBounds(
                        menuItem.iconResourceId,
                        0,
                        0,
                        0
                    )
                    compoundDrawablePadding =
                        itemView.resources.getDimensionPixelSize(R.dimen.text_margin)

                    TextViewCompat.setCompoundDrawableTintList(
                        this,
                        ColorStateList.valueOf(
                            ThemeUtils.getColor(
                                itemView.context,
                                android.R.attr.textColorPrimary
                            )
                        )
                    )
                }
                itemView.setOnClickListener {
                    listener?.onSelectMenuItem(menuItem)
                }
            }
        }
    }
}