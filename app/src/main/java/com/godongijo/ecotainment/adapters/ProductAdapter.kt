import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.godongijo.ecotainment.databinding.ProductSingleViewBinding
import com.godongijo.ecotainment.models.Product

class ProductAdapter(private val productList: List<Product>) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    // ViewHolder with binding
    inner class ProductViewHolder(val binding: ProductSingleViewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ProductSingleViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]

        // Bind data to views using ViewBinding
        holder.binding.apply {
            productImage.setImageResource(product.imageResource)
            productName.text = product.name
            productPrice.text = product.price
            discountBadge.text = product.discount
            productRating.text = product.rating.toString()
            productSales.text = "${product.sales} terjual"
        }
    }

    override fun getItemCount(): Int = productList.size
}
