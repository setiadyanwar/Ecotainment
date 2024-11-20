import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.godongijo.ecotainment.R
import com.godongijo.ecotainment.databinding.SingleViewProductBinding
import com.godongijo.ecotainment.models.Product
import com.godongijo.ecotainment.services.product.ProductService
import com.godongijo.ecotainment.ui.activities.DetailProductActivity
import com.godongijo.ecotainment.utilities.Glide
import java.text.NumberFormat
import java.util.Locale

class ProductAdapter(
    private var context: Context,
    private var productList: List<Product>,
    private val productService: ProductService
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    // ViewHolder with binding
    inner class ProductViewHolder(val binding: SingleViewProductBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = SingleViewProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]

        val imageProduct = context.getString(R.string.base_url) + product.imageUrl
        Glide().loadImageFromUrl(holder.binding.productImage, imageProduct)

        // Format harga
        val price = product.price.toLongOrNull() ?: 0 // Menggunakan toLongOrNull untuk menghindari error jika tidak valid
        val formattedPrice = NumberFormat.getInstance(Locale("id", "ID")).format(price) // Format ke format ribuan

        holder.binding.apply {
            productName.text = product.name
            productPrice.text = "Rp$formattedPrice"
            productRating.text = product.rating.toString()
            productSales.text = "${product.totalSales} terjual"
        }


        // Click listener untuk item
        holder.itemView.setOnClickListener {
            val intent = Intent(context, DetailProductActivity::class.java).apply {
                putExtra("product_id", product.id) // Kirimkan id produk ke aktivitas
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = productList.size

    fun updateList(newProductList: List<Product>) {
        productList = newProductList
        notifyDataSetChanged()
    }
}
