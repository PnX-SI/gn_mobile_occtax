package fr.geonature.occtax.ui.home

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import fr.geonature.occtax.R
import fr.geonature.occtax.input.Input

/**
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class InputRecyclerViewAdapter(private val listener: OnInputRecyclerViewAdapterListener) : RecyclerView.Adapter<InputRecyclerViewAdapter.ViewHolder>() {
    private val inputs: MutableList<Input> = mutableListOf()

    init {
        registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()

                listener.showEmptyTextView(itemCount == 0)
            }

            override fun onItemRangeChanged(positionStart: Int,
                                            itemCount: Int) {
                super.onItemRangeChanged(positionStart,
                                         itemCount)

                listener.showEmptyTextView(itemCount == 0)
            }

            override fun onItemRangeInserted(positionStart: Int,
                                             itemCount: Int) {
                super.onItemRangeInserted(positionStart,
                                          itemCount)

                listener.showEmptyTextView(false)
            }

            override fun onItemRangeRemoved(positionStart: Int,
                                            itemCount: Int) {
                super.onItemRangeRemoved(positionStart,
                                         itemCount)

                listener.showEmptyTextView(itemCount == 0)
            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ViewHolder {
        return ViewHolder(parent)
    }

    override fun getItemCount(): Int {
        return inputs.size
    }

    override fun onBindViewHolder(holder: ViewHolder,
                                  position: Int) {
        holder.bind(inputs[position])
    }

    fun setInputs(inputs: List<Input>) {
        if (this.inputs.isEmpty()) {
            this.inputs.addAll(inputs)

            if (inputs.isNotEmpty()) {
                notifyItemRangeInserted(0,
                                        inputs.size)
            }

            return
        }

        if (inputs.isEmpty()) {
            clear()

            return
        }

        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {

            override fun getOldListSize(): Int {
                return this@InputRecyclerViewAdapter.inputs.size
            }

            override fun getNewListSize(): Int {
                return inputs.size
            }

            override fun areItemsTheSame(oldItemPosition: Int,
                                         newItemPosition: Int): Boolean {
                return this@InputRecyclerViewAdapter.inputs[oldItemPosition].id == inputs[newItemPosition].id
            }

            override fun areContentsTheSame(oldItemPosition: Int,
                                            newItemPosition: Int): Boolean {
                return this@InputRecyclerViewAdapter.inputs[oldItemPosition] == inputs[newItemPosition]
            }
        })
        this.inputs.clear()
        this.inputs.addAll(inputs)
        diffResult.dispatchUpdatesTo(this)
    }

    fun addInput(input: Input,
                 index: Int? = null) {
        if (index == null) {
            this.inputs.add(input)
            notifyItemRangeInserted(this.inputs.size - 1,
                                    1)
        }
        else {
            this.inputs.add(index,
                            input)
            notifyItemRangeChanged(index,
                                   this.inputs.size - index)
        }
    }

    fun clear() {
        this.inputs.clear()
        notifyDataSetChanged()
    }

    fun remove(input: Input) {
        val inputPosition = this.inputs.indexOf(input)
        this.inputs.remove(input)
        notifyItemRemoved(inputPosition)

        if (this.inputs.isEmpty()) {
            notifyDataSetChanged()
        }
    }

    inner class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_2,
                                                                                                                    parent,
                                                                                                                    false)) {

        private val text1: TextView = itemView.findViewById(android.R.id.text1)

        fun bind(input: Input) {
            text1.text = itemView.context.getString(R.string.home_input_created_at,
                                                    DateFormat.format(itemView.context.getString(R.string.home_input_date),
                                                                      input.date))
            itemView.setOnClickListener { listener.onInputClicked(input) }
            itemView.setOnLongClickListener {
                listener.onInputLongClicked(inputs.indexOf(input),
                                            input)
                true
            }
        }
    }

    /**
     * Callback used by [InputRecyclerViewAdapter].
     */
    interface OnInputRecyclerViewAdapterListener {

        /**
         * Called when a [Input] has been clicked.
         *
         * @param input the selected [Input]
         */
        fun onInputClicked(input: Input)

        /**
         * Called when a [Input] has been clicked and held.
         *
         * @param input the selected [Input]
         */
        fun onInputLongClicked(position: Int,
                               input: Input)

        /**
         * Whether to show an empty text view when data changed.
         */
        fun showEmptyTextView(show: Boolean)
    }
}