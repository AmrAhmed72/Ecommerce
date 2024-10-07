
package com.example.ecommerce.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import androidx.hilt.navigation.compose.hiltViewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil.compose.rememberAsyncImagePainter
import com.example.ecommerce.data.model.ProductItemSmall
import com.example.ecommerce.data.model.Products
import com.example.ecommerce.presentation.viewModel.CategoryProductsViewModel
import com.example.ecommerce.presentation.viewModel.SearchViewModel

class FavoritesScreen(
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel: CategoryProductsViewModel = hiltViewModel()
        val searchViewModel: SearchViewModel = hiltViewModel()
        val navigator = LocalNavigator.currentOrThrow
        var searchText by remember { mutableStateOf("") }

        LaunchedEffect(Unit) {
            viewModel.getAllProduct()
        }

        // تنفيذ البحث عند تغيير searchText باستخدام LaunchedEffect
        LaunchedEffect(searchText) {
            searchViewModel.getProductsSearch(searchText)
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "Products") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back to Home")
                        }
                    },
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { newText ->
                        searchText = newText
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("Search Products") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (searchText.isBlank()) {
                    // عرض منتجات الفئة
                    val productsState by viewModel.allProducts.collectAsState()
                    FavoritesGridState(
                        productsState = productsState,
                        onProductClick = { productId ->
                            navigator.push(ProductDetailsScreen(productId))
                        }
                    )
                } else {
                    // عرض نتائج البحث
                    val searchState by searchViewModel.productSearch.collectAsState()
                    FavoritesGridState(
                        productsState = searchState,
                        onProductClick = { productId ->
                            navigator.push(ProductDetailsScreen(productId))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FavoritesGridState(
    productsState: Products?,
    onProductClick: (Int) -> Unit
) {
    when {
        productsState?.data?.data != null && productsState.status -> {
            FavoritesGrid(
                products = productsState.data.data,
                onProductClick = onProductClick
            )
        }

        productsState?.status == false -> {
            FavoriteErrorState(message = "Failed to load products.")
        }

        productsState == null -> {
            FavoriteLoadingState()
        }

        else -> {
            FavoriteLoadingState()
        }
    }
}

@Composable
fun FavoritesGrid(
    products: List<ProductItemSmall>,
    onProductClick: (Int) -> Unit
) {
    LazyColumn (
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(products.filter { it.in_favorites }) { product ->
            FavoriteProductBox(product = product, onClick = { onProductClick(product.id) })
        }
    }
}

@Composable
fun FavoriteProductBox(product: ProductItemSmall, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Image(
                painter = rememberAsyncImagePainter(product.image),
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Price: \$${product.price}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun FavoriteLoadingState() {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun FavoriteErrorState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )
    }
}