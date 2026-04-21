package com.fintrack.app

import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Divider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun ModalLayer(vm: AppVM, T: FTTheme) {
    if (vm.showContactPicker) { ContactPickerSheet(vm, T); return }
    if (vm.modal == null) return

    Dialog(onDismissRequest = { vm.closeModal() }) {
        Surface(shape = RoundedCornerShape(24.dp), color = T.surface,
            modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
            Column(Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        when (vm.modal) {
                            "person"               -> "Add Person"
                            "transaction_lent"     -> "💰 I Lent Money"
                            "transaction_borrowed" -> "💸 I Borrowed Money"
                            "investment"           -> "Add Investment"
                            "balance"              -> "Update Balance"
                            else -> ""
                        },
                        fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = T.text)
                    Surface(onClick = { vm.closeModal() }, shape = RoundedCornerShape(999.dp),
                        color = T.surface2, modifier = Modifier.size(34.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("✕", color = T.text2, fontSize = 16.sp)
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
                when (vm.modal) {
                    "person"               -> PersonForm(vm, T)
                    "transaction_lent"     -> TransactionForm(vm, T)
                    "transaction_borrowed" -> TransactionForm(vm, T)
                    "investment"           -> InvestmentForm(vm, T)
                    "balance"              -> BalanceForm(vm, T)
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ContactPickerSheet(vm: AppVM, T: FTTheme) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.55f))) {
        Surface(onClick = { vm.showContactPicker = false; vm.contactSearch = "" },
            color = Color.Transparent, modifier = Modifier.fillMaxSize()) {}
        Surface(shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = T.surface,
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().fillMaxHeight(0.85f)) {
            Column(Modifier.fillMaxSize().navigationBarsPadding()) {
                Box(Modifier.padding(top = 12.dp, bottom = 16.dp).size(40.dp, 5.dp)
                    .background(T.surface3, RoundedCornerShape(3.dp))
                    .align(Alignment.CenterHorizontally))
                Text("Select Contact", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold,
                    color = T.text, modifier = Modifier.padding(horizontal = 24.dp))
                Spacer(Modifier.height(14.dp))
                TextField(value = vm.contactSearch, onValueChange = { vm.contactSearch = it },
                    placeholder = { Text("Search by name or phone...", color = T.text2) },
                    singleLine = true, colors = ftInputColors(T), shape = RoundedCornerShape(16.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(color = T.text, fontSize = 15.sp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp))
                Spacer(Modifier.height(12.dp))
                val filtered = vm.filteredContacts()
                if (filtered.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(if (vm.contacts.isEmpty()) "No contacts found.\nGrant permission in Settings."
                        else "No contacts match.",
                            color = T.text2, fontSize = 14.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(24.dp))
                    }
                } else {
                    LazyColumn(Modifier.fillMaxSize()) {
                        items(filtered) { contact ->
                            Surface(onClick = { vm.contactSearch = ""; vm.selectContact(contact) },
                                color = Color.Transparent) {
                                Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                    Box(Modifier.size(42.dp).clip(CircleShape).background(T.accentBg),
                                        contentAlignment = Alignment.Center) {
                                        Text(contact.name.firstOrNull()?.uppercase() ?: "?",
                                            fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = T.accent)
                                    }
                                    Column {
                                        Text(contact.name, fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold, color = T.text)
                                        if (contact.phone.isNotEmpty())
                                            Text(contact.phone, fontSize = 12.sp, color = T.text2)
                                    }
                                }
                            }
                            Divider(Modifier.padding(horizontal = 24.dp), color = T.divider, thickness = 1.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PersonForm(vm: AppVM, T: FTTheme) {
    Surface(onClick = { vm.closeModal(); vm.showContactPicker = true },
        shape = RoundedCornerShape(14.dp), color = T.accentBg,
        modifier = Modifier.fillMaxWidth().height(48.dp)) {
        Box(contentAlignment = Alignment.Center) {
            Text("📱  Import from Contacts", fontSize = 14.sp,
                fontWeight = FontWeight.Bold, color = T.accent)
        }
    }
    Spacer(Modifier.height(16.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Divider(Modifier.weight(1f), color = T.divider)
        Text("  OR  ", fontSize = 12.sp, color = T.text2, fontWeight = FontWeight.Medium)
        Divider(Modifier.weight(1f), color = T.divider)
    }
    Spacer(Modifier.height(16.dp))
    FTField("Full Name *", "Enter name", vm.fName, T) { vm.fName = it }
    Spacer(Modifier.height(14.dp))
    FTField("Phone (optional)", "Phone number", vm.fPhone, T,
        keyboardType = KeyboardType.Phone) { vm.fPhone = it }
    Spacer(Modifier.height(20.dp))
    PillButton("Add Person", T.accent) { vm.submitPerson() }
}

@Composable
private fun TransactionForm(vm: AppVM, T: FTTheme) {
    Row(Modifier.fillMaxWidth().background(T.surface2, RoundedCornerShape(16.dp)).padding(4.dp)) {
        listOf("lent" to "⬆️ I Lent", "borrowed" to "⬇️ I Borrowed").forEach { (type, label) ->
            val active = vm.fType == type
            Surface(onClick = { vm.fType = type }, shape = RoundedCornerShape(13.dp),
                color = if (active) (if (type == "lent") T.green else T.red) else Color.Transparent,
                modifier = Modifier.weight(1f).height(44.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                        color = if (active) Color.White else T.text2)
                }
            }
        }
    }
    Spacer(Modifier.height(14.dp))
    FTField("Amount (₹) *", "0", vm.fAmount, T, keyboardType = KeyboardType.Number) { vm.fAmount = it }
    Spacer(Modifier.height(14.dp))
    FTField("Description (optional)", "e.g. Lunch, rent", vm.fDesc, T) { vm.fDesc = it }
    Spacer(Modifier.height(14.dp))
    FTField("Date (YYYY-MM-DD)", vm.todayStr(), vm.fDate, T) { vm.fDate = it }
    Spacer(Modifier.height(14.dp))
    FTField("Due Date (YYYY-MM-DD)", "Optional reminder date", vm.fDueDate, T) { vm.fDueDate = it }
    Spacer(Modifier.height(6.dp))
    Text("📅 Set a due date to get a notification reminder", fontSize = 11.sp, color = T.text2)
    Spacer(Modifier.height(20.dp))
    PillButton("Save Transaction", if (vm.fType == "lent") T.green else T.red,
        enabled = vm.fAmount.trim().toDoubleOrNull().let { it != null && it > 0 }) {
        vm.submitTransaction()
    }
}

@Composable
private fun InvestmentForm(vm: AppVM, T: FTTheme) {
    Text("CATEGORY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = T.text2, letterSpacing = 0.7.sp)
    Spacer(Modifier.height(8.dp))
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CATS.forEach { cat ->
            val active = vm.fCat == cat.id
            Surface(onClick = { vm.fCat = cat.id }, shape = RoundedCornerShape(12.dp),
                color = if (active) Color(cat.colorHex).copy(alpha = 0.2f) else T.surface2) {
                Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(cat.icon, fontSize = 14.sp)
                    Text(cat.label, fontSize = 13.sp,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                        color = if (active) Color(cat.colorHex) else T.text2)
                }
            }
        }
    }
    Spacer(Modifier.height(14.dp))
    FTField("Name *", "e.g. Reliance, Bitcoin, SBI FD", vm.fName, T) { vm.fName = it }
    Spacer(Modifier.height(14.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.weight(1f)) {
            FTField("Invested (₹) *", "0", vm.fInvested, T, keyboardType = KeyboardType.Number) { vm.fInvested = it }
        }
        Column(Modifier.weight(1f)) {
            FTField("Current (₹) *", "0", vm.fCurrent, T, keyboardType = KeyboardType.Number) { vm.fCurrent = it }
        }
    }
    Spacer(Modifier.height(14.dp))
    FTField("Date (YYYY-MM-DD)", vm.todayStr(), vm.fDate, T) { vm.fDate = it }
    Spacer(Modifier.height(14.dp))
    FTField("Notes (optional)", "Any notes", vm.fNotes, T) { vm.fNotes = it }
    Spacer(Modifier.height(20.dp))
    PillButton("Add Investment", T.accent) { vm.submitInvestment() }
}

@Composable
private fun BalanceForm(vm: AppVM, T: FTTheme) {
    Text("ACCOUNT BALANCE (₹)", fontSize = 11.sp, fontWeight = FontWeight.Bold,
        color = T.text2, letterSpacing = 0.7.sp)
    Spacer(Modifier.height(8.dp))
    TextField(value = vm.fBalance, onValueChange = { vm.fBalance = it },
        placeholder = { Text("0", color = T.text2, fontSize = 22.sp) },
        singleLine = true, colors = ftInputColors(T), shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        textStyle = LocalTextStyle.current.copy(
            color = T.text, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold),
        modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(20.dp))
    PillButton("Update Balance", T.accent) { vm.submitBalance() }
}

@Composable
fun FTField(label: String, placeholder: String, value: String, T: FTTheme,
            keyboardType: KeyboardType = KeyboardType.Text, onValue: (String) -> Unit) {
    Text(label.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold,
        color = T.text2, letterSpacing = 0.7.sp)
    Spacer(Modifier.height(7.dp))
    TextField(value = value, onValueChange = onValue,
        placeholder = { Text(placeholder, color = T.text2, fontSize = 15.sp) },
        singleLine = true, colors = ftInputColors(T), shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        textStyle = LocalTextStyle.current.copy(
            color = T.text, fontSize = 15.sp, fontWeight = FontWeight.Medium),
        modifier = Modifier.fillMaxWidth())
}