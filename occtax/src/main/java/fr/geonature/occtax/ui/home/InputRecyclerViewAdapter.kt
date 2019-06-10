package fr.geonature.occtax.ui.home

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fr.geonature.occtax.R
import fr.geonature.occtax.input.Input

/**
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class InputRecyclerViewAdapter(private val listener: OnInputRecyclerViewAdapterListener) : RecyclerView.Adapter<InputRecyclerViewAdapter.ViewHolder>() {
    private val inputs: MutableList<Input> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ViewHolder {
        return ViewHolder(parent)
    }

    override fun getItemCount(): Int {
        return inputs.size
    }

    override fun onBindViewHolder(holder: ViewHolder,
                                  position: Int) {
        holder.bind(position,
                    inputs[position])
    }

    fun setInputs(inputs: List<Input>) {
        this.inputs.clear()
        this.inputs.addAll(inputs)

        notifyDataSetChanged()
    }

    fun addInput(input: Input,
                 index: Int? = null) {
        if (index == null) {
            this.inputs.add(input)
            notifyItemInserted(this.inputs.size - 1)
        }
        else {
            this.inputs.add(index,
                            input)
            notifyItemRangeChanged(index,
                                   this.inputs.size - index)
        }
    }

    fun removeAt(index: Int) {
        this.inputs.removeAt(index)
        notifyItemRemoved(index)
    }

    inner class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_2,
                                                                                                                    parent,
                                                                                                                    false)) {

        private val text1: TextView = itemView.findViewById(android.R.id.text1)

        fun bind(position: Int,
                 input: Input) {
            text1.text = itemView.context.getString(R.string.home_input_created_at,
                                                    DateFormat.format(itemView.context.getString(R.string.home_input_date),
                                                                      input.date))
            itemView.setOnClickListener { listener.onInputClicked(input) }
            itemView.setOnLongClickListener {
                listener.onInputLongClicked(position,
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
        fun onInputLongClicked(index: Int,
                               input: Input)
    }
}