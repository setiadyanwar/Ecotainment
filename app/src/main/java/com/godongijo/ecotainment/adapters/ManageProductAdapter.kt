import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.godongijo.ecotainment.R
import com.godongijo.ecotainment.databinding.SingleViewManageProductBinding
import com.godongijo.ecotainment.models.Product
import com.godongijo.ecotainment.utilities.Glide
import java.text.NumberFormat
import java.util.Locale

class ManageProductAdapter(
    private var context: Context,
    private var productList: List<Product>,
    private val onEditClick: (Product) -> Unit, // Callback untuk tombol edit
    private val onDeleteClick: (Product) -> Unit // Callback untuk tombol delete
) : RecyclerView.Adapter<ManageProductAdapter.ManageProductViewHolder>() {

    // ViewHolder with binding
    inner class ManageProductViewHolder(val binding: SingleViewManageProductBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ManageProductViewHolder {
        val binding = SingleViewManageProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ManageProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ManageProductViewHolder, position: Int) {
        val product = productList[position]

        val imageProduct = context.getString(R.string.base_url) + product.imageUrl
        Glide().loadImageFromUrl(holder.binding.productImage, imageProduct)

        // Format harga
        val price = product.price
        val formattedPrice = NumberFormat.getInstance(Locale("id", "ID")).format(price) // Format ke format ribuan

        holder.binding.apply {
            productTitle.text = product.name
            productPrice.text = "Rp$formattedPrice"

            // Listener untuk tombol edit
            editProduct.setOnClickListener {
                onEditClick(product)
            }

            // Listener untuk tombol delete
            deleteProduct.setOnClickListener {
                onDeleteClick(product)
            }
        }
    }

    override fun getItemCount(): Int = productList.size

    fun updateList(newProductList: List<Product>) {
        productList = newProductList
        notifyDataSetChanged()
    }
}
