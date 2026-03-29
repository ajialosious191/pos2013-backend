package com.pos2013.offline.data.remote 

import com.pos2013.offline.data.local.InvoiceItem

data class BatchTransactionDto( 
    val localId: String, 
    val code: String, 
    val amountMinor: Long, 
    val currency: String, 
    val createdAt: Long,
    val items: List<InvoiceItem> = emptyList(),
    val subtotalMinor: Long = 0,
    val taxMinor: Long = 0,
    val discountMinor: Long = 0
) 

data class BatchRequestDto( 
    val protocolVersion: String = "201.3", 
    val merchantId: String, 
    val terminalId: String, 
    val batchId: String, 
    val timestamp: Long, 
    val nonce: String, 
    val transactions: List<BatchTransactionDto> 
) 

data class BatchResultItemDto( 
    val localId: String, 
    val status: String, 
    val transactionId: String 
) 

data class BatchResponseDto( 
    val success: Boolean, 
    val batchId: String?, 
    val results: List<BatchResultItemDto>? 
) 
