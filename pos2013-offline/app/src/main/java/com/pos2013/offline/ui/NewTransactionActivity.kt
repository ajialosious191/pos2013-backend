package com.pos2013.offline.ui

import android.content.Context
import android.os.Bundle
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.pos2013.offline.R
import com.pos2013.offline.data.local.InvoiceItem

class NewTransactionActivity : ComponentActivity() {

    private val viewModel: TransactionViewModel by viewModels()
    private val cartItems = mutableListOf<InvoiceItem>()
    private lateinit var cartAdapter: CartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_transaction)

        val codeInput = findViewById<TextInputEditText>(R.id.codeInput)
        val itemNameInput = findViewById<TextInputEditText>(R.id.itemNameInput)
        val itemQtyInput = findViewById<TextInputEditText>(R.id.itemQtyInput)
        val itemPriceInput = findViewById<TextInputEditText>(R.id.itemPriceInput)
        val totalAmountText = findViewById<TextView>(R.id.totalAmountText)
        val subtotalText = findViewById<TextView>(R.id.subtotalText)
        val taxText = findViewById<TextView>(R.id.taxText)
        val currencySpinner = findViewById<Spinner>(R.id.currencySpinner)
        
        val cartRecyclerView = findViewById<RecyclerView>(R.id.cartRecyclerView)
        cartAdapter = CartAdapter(cartItems)
        cartRecyclerView.layoutManager = LinearLayoutManager(this)
        cartRecyclerView.adapter = cartAdapter

        // Currencies
        val currencies = listOf(
            "USD", "EUR", "GBP", "JPY", "CHF", "CAD", "AUD", "NZD", "CNY", "HKD", "SGD", "INR", 
            "AED", "SAR", "QAR", "KWD", "BHD", "OMR", "EGP", "SEK", "NOK", "DKK", "RUB", "TRY", 
            "THB", "PHP", "MYR", "KRW", "IDR", "ARS", "BRL", "MXN", "CLP", "COP", "PEN", "ZAR", 
            "NGN", "KES"
        )
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        currencySpinner.adapter = spinnerAdapter
        
        // Default to AED
        val aedIndex = currencies.indexOf("AED")
        if (aedIndex != -1) currencySpinner.setSelection(aedIndex)

        findViewById<Button>(R.id.addItemBtn).setOnClickListener {
            val name = itemNameInput.text.toString().trim()
            val qtyStr = itemQtyInput.text.toString().trim()
            val priceStr = itemPriceInput.text.toString().trim()

            if (name.isEmpty() || qtyStr.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(this, "Fill item details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val qty = qtyStr.toInt()
                val priceMinor = (priceStr.toDouble() * 100).toLong()
                
                cartItems.add(InvoiceItem(name, qty, priceMinor.toInt()))
                cartAdapter.notifyItemInserted(cartItems.size - 1)
                
                updateTotal(subtotalText, taxText, totalAmountText)
                
                // Clear inputs
                itemNameInput.text?.clear()
                itemQtyInput.setText("1")
                itemPriceInput.text?.clear()
                itemNameInput.requestFocus()
                
            } catch (e: Exception) {
                Toast.makeText(this, "Invalid quantity or price", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.submitBtn).setOnClickListener {
            val code = codeInput.text.toString().trim()
            val currency = currencySpinner.selectedItem.toString()

            if (code.length != 6) {
                Toast.makeText(this, "Enter 6-digit code", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val subtotalMinor = cartItems.sumOf { it.qty.toLong() * it.price.toLong() }
            val taxMinor = (subtotalMinor * 0.05).toLong()
            val totalMinor = subtotalMinor + taxMinor
            
            val prefs = getSharedPreferences("pos2013", Context.MODE_PRIVATE)
            val merchantId = prefs.getString("merchantId", "AL_RKN_AL_RAQY") ?: "AL_RKN_AL_RAQY"
            val terminalId = prefs.getString("terminalId", "T001") ?: "T001"

            viewModel.saveTransaction(
                code = code,
                amount = totalMinor,
                currency = currency,
                merchantId = merchantId,
                terminalId = terminalId,
                items = cartItems,
                subtotal = subtotalMinor,
                tax = taxMinor,
                discount = 0
            ) {
                Toast.makeText(this, "Invoice Saved Offline", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun updateTotal(subtotalText: TextView, taxText: TextView, totalAmountText: TextView) {
        val subtotalMinor = cartItems.sumOf { it.qty.toLong() * it.price.toLong() }
        val taxMinor = (subtotalMinor * 0.05).toLong()
        val totalMinor = subtotalMinor + taxMinor

        subtotalText.text = "%.2f".format(subtotalMinor / 100.0)
        taxText.text = "%.2f".format(taxMinor / 100.0)
        totalAmountText.text = "%.2f".format(totalMinor / 100.0)
    }
}
