package com.example.nexusbusiness

import com.example.nexusbusiness.data.model.Product
import org.junit.Assert.assertEquals
import org.junit.Test

class ProductPhotoPersistenceTest {
    private val product = Product(
        id = 7,
        nome = "Produto com foto",
        qtd = 10,
        custo = "R$ 10,00",
        margem = 25.0,
        precoVenda = "R$ 12,50",
        rateio = "R$ 1,00",
        photoPath = "/data/user/0/com.example.nexusbusiness/files/product_images/photo.jpg"
    )

    @Test
    fun quantityUpdatePreservesPhotoPath() {
        assertEquals(product.photoPath, product.copy(qtd = 4).photoPath)
    }

    @Test
    fun priceUpdatePreservesPhotoPath() {
        assertEquals(
            product.photoPath,
            product.copy(precoVenda = "R$ 15,00", margem = 40.0).photoPath
        )
    }

    @Test
    fun calculatedValuesPreservePhotoPath() {
        assertEquals(
            product.photoPath,
            product.copy(rateio = "R$ 2,00", precoVenda = "R$ 16,00").photoPath
        )
    }
}
