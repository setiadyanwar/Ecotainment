import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.godongijo.ecotainment.adapters.ProductItemAdapter
import com.godongijo.ecotainment.databinding.SingleViewManageTransactionBinding
import com.godongijo.ecotainment.models.Transaction
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class ManageTransactionAdapter(
    private var context: Context,
    private var transactionList: List<Transaction>,
    private val onCheckTransactionClick: (Transaction) -> Unit,
) : RecyclerView.Adapter<ManageTransactionAdapter.ManageTransactionViewHolder>() {

    // ViewHolder with binding
    inner class ManageTransactionViewHolder(val binding: SingleViewManageTransactionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ManageTransactionViewHolder {
        val binding = SingleViewManageTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ManageTransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ManageTransactionViewHolder, position: Int) {
        val transaction = transactionList[position]

        // Format harga total transaksi
        val totalPrice = transaction.totalAmount
        val formattedPrice = NumberFormat.getInstance(Locale("id", "ID")).format(totalPrice) // Format ke format ribuan

        val transactionStatus  = transaction.status

        holder.binding.apply {
            transactionDate.text = formatDateTime(transaction.createdAt)
            totalAmount.text = "Rp$formattedPrice"

            // Reset semua badge menjadi GONE
            val badges = listOf(
                badgeWaitingConfirmation,
                badgeOnProcessed,
                badgeOnShipment,
                badgeCompleted,
                badgeCanceled
            )
            badges.forEach { it.visibility = View.GONE }

            // Set badge yang sesuai menjadi VISIBLE
            when (transactionStatus) {
                "waiting_for_confirmation" -> badgeWaitingConfirmation.visibility = View.VISIBLE
                "processed" -> badgeOnProcessed.visibility = View.VISIBLE
                "on_shipment" -> badgeOnShipment.visibility = View.VISIBLE
                "completed" -> badgeCompleted.visibility = View.VISIBLE
                "canceled" -> badgeCanceled.visibility = View.VISIBLE
            }

            // Setup nested RecyclerView
            val productItemAdapter = ProductItemAdapter(context)
            productItemAdapter.updateData(transaction.items) // List produk dari transaksi
            productItemRecycler.adapter = productItemAdapter
            productItemRecycler.layoutManager = LinearLayoutManager(root.context, RecyclerView.VERTICAL, false)

            checkTransaction.setOnClickListener {
                onCheckTransactionClick(transaction)
            }
        }

    }

    override fun getItemCount(): Int = transactionList.size

    fun updateList(newTransactionList: List<Transaction>) {
        transactionList = newTransactionList
        notifyDataSetChanged()
    }

    private fun formatDateTime(isoDate: String): String {
        return try {
            // Format asli dari ISO 8601
            val originalFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            originalFormat.timeZone = java.util.TimeZone.getTimeZone("UTC") // ISO format biasanya di UTC

            // Format tujuan
            val targetFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

            // Parse dan format ulang
            val date = originalFormat.parse(isoDate)
            targetFormat.format(date ?: "")
        } catch (e: Exception) {
            "Invalid date"
        }
    }
}
