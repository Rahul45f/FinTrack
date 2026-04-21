package com.fintrack.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

// ── Responsive sizing helper ──────────────────────────────────────────────────
// Scales sizes proportionally based on screen width.
// Base target is 360dp (small Android phone). Larger screens get bigger text/padding.

@Composable
fun rememberScale(): Float {
    val config = LocalConfiguration.current
    val screenW = config.screenWidthDp
    return (screenW / 360f).coerceIn(0.85f, 1.5f)
}

fun Float.sp(scale: Float): TextUnit = (this * scale).sp
fun Float.dp(scale: Float): Dp = (this * scale).dp
fun Int.rsp(scale: Float): TextUnit = (this * scale).sp
fun Int.rdp(scale: Float): Dp = (this * scale).dp

// ── Main shell ────────────────────────────────────────────────────────────────

@Composable
fun MainApp(vm: AppVM, T: FTTheme) {
    val scale = rememberScale()
    val tabId = when (vm.screen) {
        "people", "person"      -> "people"
        "portfolio", "category" -> "portfolio"
        else                    -> "dashboard"
    }

    BackHandler(enabled = vm.screen != "dashboard") {
        when (vm.screen) {
            "person"   -> { vm.screen = "people";    vm.selPerson = null }
            "category" -> { vm.screen = "portfolio"; vm.selCatId  = ""   }
            else       -> vm.screen = "dashboard"
        }
    }

    Scaffold(
        containerColor = T.bg,
        bottomBar = {
            NavigationBar(containerColor = T.surface, tonalElevation = 0.dp) {
                listOf(
                    Triple("dashboard", "🏠", "Home"),
                    Triple("people",    "👥", "People"),
                    Triple("portfolio", "📊", "Portfolio"),
                ).forEach { (id, icon, label) ->
                    val active = tabId == id
                    NavigationBarItem(
                        selected = active, onClick = { vm.screen = id },
                        icon = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(icon, fontSize = 20.rsp(scale))
                                if (active) Box(Modifier.size(4.rdp(scale)).clip(CircleShape).background(T.accent))
                            }
                        },
                        label = { Text(label, fontSize = 10.rsp(scale),
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedTextColor = T.accent, unselectedTextColor = T.text2,
                            indicatorColor = Color.Transparent)
                    )
                }
            }
        }
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            when (vm.screen) {
                "dashboard" -> DashboardScreen(vm, T)
                "people"    -> PeopleScreen(vm, T)
                "person"    -> PersonScreen(vm, T)
                "portfolio" -> PortfolioScreen(vm, T)
                "category"  -> CategoryScreen(vm, T)
            }
            ModalLayer(vm, T)
            PostAddDialog(vm, T, scale)
            SettleDialog(vm, T, scale)
        }
    }
}

// ── Post-add dialog ───────────────────────────────────────────────────────────

@Composable
fun PostAddDialog(vm: AppVM, T: FTTheme, scale: Float) {
    if (!vm.showPostAddDialog || vm.recentlyAddedPerson == null) return
    Dialog(onDismissRequest = { vm.showPostAddDialog = false; vm.recentlyAddedPerson = null }) {
        Surface(shape = RoundedCornerShape(24.rdp(scale)), color = T.surface,
            modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(24.rdp(scale)), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("✅", fontSize = 36.rsp(scale))
                Spacer(Modifier.height(12.rdp(scale)))
                Text("${vm.recentlyAddedPerson!!.name} added!", fontSize = 18.rsp(scale),
                    fontWeight = FontWeight.ExtraBold, color = T.text, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.rdp(scale)))
                Text("Add a transaction for them?", fontSize = 14.rsp(scale),
                    color = T.text2, textAlign = TextAlign.Center)
                Spacer(Modifier.height(20.rdp(scale)))
                Surface(onClick = { vm.showPostAddDialog = false; vm.openModal("transaction_lent") },
                    shape = RoundedCornerShape(999.dp), color = T.green,
                    modifier = Modifier.fillMaxWidth().height(48.rdp(scale))) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("💰 I Lent Money", fontSize = 15.rsp(scale),
                            fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                Spacer(Modifier.height(10.rdp(scale)))
                Surface(onClick = { vm.showPostAddDialog = false; vm.openModal("transaction_borrowed") },
                    shape = RoundedCornerShape(999.dp), color = T.red,
                    modifier = Modifier.fillMaxWidth().height(48.rdp(scale))) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("💸 I Borrowed", fontSize = 15.rsp(scale),
                            fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                Spacer(Modifier.height(8.rdp(scale)))
                TextButton(onClick = { vm.showPostAddDialog = false; vm.recentlyAddedPerson = null }) {
                    Text("Not now", color = T.text2, fontSize = 14.rsp(scale))
                }
            }
        }
    }
}

// ── Settle dialog ─────────────────────────────────────────────────────────────

@Composable
fun SettleDialog(vm: AppVM, T: FTTheme, scale: Float) {
    if (vm.settleDialog == null) return
    val (pid, tid) = vm.settleDialog!!
    val person = vm.data.people.find { it.id == pid }
    val tx     = person?.transactions?.find { it.id == tid }
    if (tx == null) { vm.closeSettleDialog(); return }

    Dialog(onDismissRequest = { vm.closeSettleDialog() }) {
        Surface(shape = RoundedCornerShape(24.rdp(scale)), color = T.surface,
            modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(22.rdp(scale))) {
                Text("Settle Transaction", fontSize = 19.rsp(scale),
                    fontWeight = FontWeight.ExtraBold, color = T.text)
                Spacer(Modifier.height(5.rdp(scale)))
                Text(tx.description, fontSize = 13.rsp(scale), color = T.text2)
                Spacer(Modifier.height(4.rdp(scale)))
                Text(fmt(tx.amount), fontSize = 18.rsp(scale), fontWeight = FontWeight.Bold,
                    color = if (tx.type == "lent") T.green else T.red)
                Spacer(Modifier.height(18.rdp(scale)))

                // Full settle
                Surface(onClick = { vm.settleFull() },
                    shape = RoundedCornerShape(999.dp), color = T.green,
                    modifier = Modifier.fillMaxWidth().height(50.rdp(scale))) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("✅  Settle Full ${fmt(tx.amount)}", fontSize = 14.rsp(scale),
                            fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Spacer(Modifier.height(14.rdp(scale)))
                Divider(color = T.divider)
                Spacer(Modifier.height(14.rdp(scale)))

                // Partial settle
                Text("PARTIAL SETTLEMENT", fontSize = 10.rsp(scale), fontWeight = FontWeight.Bold,
                    color = T.text2, letterSpacing = 0.7.sp)
                Spacer(Modifier.height(8.rdp(scale)))
                Row(horizontalArrangement = Arrangement.spacedBy(10.rdp(scale)),
                    verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = vm.partialAmount, onValueChange = { vm.partialAmount = it },
                        placeholder = { Text("Amount paid (₹)", color = T.text2,
                            fontSize = 13.rsp(scale)) },
                        singleLine = true, colors = ftInputColors(T),
                        shape = RoundedCornerShape(12.rdp(scale)),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = LocalTextStyle.current.copy(color = T.text, fontSize = 14.rsp(scale)),
                        modifier = Modifier.weight(1f)
                    )
                    val canPartial = vm.partialAmount.toDoubleOrNull().let { it != null && it > 0 }
                    Surface(onClick = { if (canPartial) vm.settlePartial() },
                        shape = RoundedCornerShape(12.rdp(scale)),
                        color = if (canPartial) T.accent else T.surface3,
                        modifier = Modifier.height(50.rdp(scale))) {
                        Box(contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 16.rdp(scale))) {
                            Text("Settle", fontSize = 13.rsp(scale),
                                fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
                Spacer(Modifier.height(5.rdp(scale)))
                Text("Remaining kept as new unsettled transaction.",
                    fontSize = 10.rsp(scale), color = T.text2)

                Spacer(Modifier.height(14.rdp(scale)))
                Divider(color = T.divider)
                Spacer(Modifier.height(12.rdp(scale)))

                // Delete transaction
                Surface(
                    onClick = { vm.deleteTransaction(pid, tid) },
                    shape = RoundedCornerShape(999.dp),
                    color = T.redBg,
                    modifier = Modifier.fillMaxWidth().height(46.rdp(scale))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🗑  Delete This Transaction", fontSize = 14.rsp(scale),
                            fontWeight = FontWeight.Bold, color = T.red)
                    }
                }

                Spacer(Modifier.height(6.rdp(scale)))
                TextButton(onClick = { vm.closeSettleDialog() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Cancel", color = T.text2, fontSize = 14.rsp(scale))
                }
            }
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
fun AppHeader(vm: AppVM, T: FTTheme) {
    val scale = rememberScale()
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.rdp(scale), vertical = 12.rdp(scale)),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.rdp(scale))) {
            Box(Modifier.size(34.rdp(scale)).clip(RoundedCornerShape(11.rdp(scale)))
                .background(T.accentBg), contentAlignment = Alignment.Center) {
                Text("💹", fontSize = 16.rsp(scale))
            }
            Text("FinTrack", fontSize = 18.rsp(scale), fontWeight = FontWeight.ExtraBold, color = T.accent)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.rdp(scale))) {
            Surface(onClick = { vm.toggleTheme() }, shape = RoundedCornerShape(12.rdp(scale)),
                color = T.surface, shadowElevation = 2.dp, modifier = Modifier.size(38.rdp(scale))) {
                Box(contentAlignment = Alignment.Center) {
                    Text(if (vm.dark) "☀️" else "🌙", fontSize = 15.rsp(scale))
                }
            }
            Surface(onClick = { vm.lock() }, shape = RoundedCornerShape(12.rdp(scale)),
                color = T.surface, shadowElevation = 2.dp, modifier = Modifier.height(38.rdp(scale))) {
                Box(contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 12.rdp(scale))) {
                    Text("🔒 Lock", fontSize = 12.rsp(scale), fontWeight = FontWeight.Bold, color = T.text2)
                }
            }
        }
    }
}

// ── Dashboard ─────────────────────────────────────────────────────────────────

@Composable
fun DashboardScreen(vm: AppVM, T: FTTheme) {
    val scale = rememberScale()
    val hp = 20.rdp(scale)
    Column(Modifier.fillMaxSize()) {
        AppHeader(vm, T)
        LazyColumn(contentPadding = PaddingValues(horizontal = hp, vertical = 8.rdp(scale)),
            verticalArrangement = Arrangement.spacedBy(14.rdp(scale))) {
            item {
                Column {
                    Text("Good to see you,", fontSize = 13.rsp(scale),
                        fontWeight = FontWeight.SemiBold, color = T.text2)
                    Text("${vm.userName} 👋", fontSize = 28.rsp(scale),
                        fontWeight = FontWeight.ExtraBold, color = T.text)
                }
            }
            item {
                Surface(onClick = { vm.openModal("balance") }, shape = RoundedCornerShape(24.rdp(scale)),
                    color = T.accent, shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(24.rdp(scale))) {
                        Text("ACCOUNT BALANCE", fontSize = 11.rsp(scale), fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.7f), letterSpacing = 0.8.sp)
                        Spacer(Modifier.height(8.rdp(scale)))
                        Text(fmt(vm.data.accountBalance), fontSize = 36.rsp(scale),
                            fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Spacer(Modifier.height(4.rdp(scale)))
                        Text("Tap to update", fontSize = 12.rsp(scale),
                            color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Medium)
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.rdp(scale))) {
                    listOf(
                        Triple("To Receive", vm.toReceive, T.green) to Pair(T.greenBg, { vm.screen = "people" }),
                        Triple("To Give",    vm.toGive,    T.red)   to Pair(T.redBg,   { vm.screen = "people" }),
                        Triple("P&L", Math.abs(vm.totPL), if (vm.totPL >= 0) T.green else T.red) to
                                Pair(if (vm.totPL >= 0) T.greenBg else T.redBg, { vm.screen = "portfolio" }),
                    ).forEach { (info, action) ->
                        val (label, value, color) = info
                        val (bg, onClick) = action
                        Surface(onClick = onClick, shape = RoundedCornerShape(18.rdp(scale)),
                            color = T.surface, shadowElevation = 3.dp, modifier = Modifier.weight(1f)) {
                            Column(Modifier.padding(12.rdp(scale))) {
                                Box(Modifier.size(28.rdp(scale)).clip(RoundedCornerShape(10.rdp(scale)))
                                    .background(bg), contentAlignment = Alignment.Center) {
                                    Box(Modifier.size(8.rdp(scale)).clip(CircleShape).background(color))
                                }
                                Spacer(Modifier.height(10.rdp(scale)))
                                Text(label, fontSize = 9.rsp(scale), fontWeight = FontWeight.Bold,
                                    color = T.text2, letterSpacing = 0.4.sp)
                                Spacer(Modifier.height(3.rdp(scale)))
                                Text((if (label == "P&L" && vm.totPL >= 0) "+"
                                else if (label == "P&L") "-" else "") + fmt(value),
                                    fontSize = 13.rsp(scale), fontWeight = FontWeight.ExtraBold, color = color)
                            }
                        }
                    }
                }
            }
            item {
                Surface(shape = RoundedCornerShape(24.rdp(scale)), color = T.surface, shadowElevation = 3.dp) {
                    Column(Modifier.padding(20.rdp(scale))) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically) {
                            Text("People", fontSize = 16.rsp(scale), fontWeight = FontWeight.ExtraBold, color = T.text)
                            Surface(onClick = { vm.screen = "people" },
                                shape = RoundedCornerShape(999.dp), color = T.accentBg) {
                                Text("See all", modifier = Modifier.padding(
                                    horizontal = 14.rdp(scale), vertical = 5.rdp(scale)),
                                    fontSize = 12.rsp(scale), fontWeight = FontWeight.Bold, color = T.accent)
                            }
                        }
                        Spacer(Modifier.height(14.rdp(scale)))
                        val active = vm.data.people.filter { Math.abs(vm.net(it)) > 0.001 }
                            .sortedByDescending { Math.abs(vm.net(it)) }
                        if (active.isEmpty()) {
                            EmptyPreview("👥", "No pending settlements") { vm.screen = "people" }
                        } else {
                            active.take(3).forEachIndexed { i, p ->
                                PersonRow(p, vm.net(p), T, scale) { vm.selPerson = p; vm.screen = "person" }
                                if (i < minOf(active.size, 3) - 1) Divider(color = T.divider, thickness = 1.dp)
                            }
                        }
                    }
                }
            }
            item {
                Surface(shape = RoundedCornerShape(24.rdp(scale)), color = T.surface, shadowElevation = 3.dp) {
                    Column(Modifier.padding(20.rdp(scale))) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically) {
                            Text("Portfolio", fontSize = 16.rsp(scale), fontWeight = FontWeight.ExtraBold, color = T.text)
                            Surface(onClick = { vm.screen = "portfolio" },
                                shape = RoundedCornerShape(999.dp), color = T.accentBg) {
                                Text("See all", modifier = Modifier.padding(
                                    horizontal = 14.rdp(scale), vertical = 5.rdp(scale)),
                                    fontSize = 12.rsp(scale), fontWeight = FontWeight.Bold, color = T.accent)
                            }
                        }
                        Spacer(Modifier.height(14.rdp(scale)))
                        val activeCats = CATS.filter { vm.cs(it.id).invested > 0 }
                        if (activeCats.isEmpty()) {
                            EmptyPreview("📊", "No investments yet") { vm.openModal("investment") }
                        } else {
                            activeCats.take(4).forEachIndexed { i, cat ->
                                val catId = cat.id
                                CatRow(cat, vm.cs(catId), T, scale) {
                                    vm.selCatId = catId; vm.screen = "category"
                                }
                                if (i < minOf(activeCats.size, 4) - 1) Divider(color = T.divider, thickness = 1.dp)
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(8.rdp(scale))) }
        }
    }
}

// ── People ────────────────────────────────────────────────────────────────────

@Composable
fun PeopleScreen(vm: AppVM, T: FTTheme) {
    val scale = rememberScale()
    val hp    = 20.rdp(scale)

    // Read filtered results — these are computed properties, Compose tracks them efficiently
    val people   = vm.cachedFilteredPeople
    val contacts = vm.cachedContactSuggestions

    Column(Modifier.fillMaxSize()) {
        AppHeader(vm, T)
        Column(Modifier.fillMaxWidth().padding(horizontal = hp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom) {
                Column {
                    Text("Ledger", fontSize = 13.rsp(scale), fontWeight = FontWeight.SemiBold, color = T.text2)
                    Text("People", fontSize = 26.rsp(scale), fontWeight = FontWeight.ExtraBold, color = T.text)
                }
                Surface(onClick = { vm.openModal("person") }, shape = RoundedCornerShape(999.dp),
                    color = T.accent, shadowElevation = 4.dp, modifier = Modifier.height(44.rdp(scale))) {
                    Box(contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 20.rdp(scale))) {
                        Text("+ Add", fontSize = 14.rsp(scale), fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
            Spacer(Modifier.height(14.rdp(scale)))

            // Search uses peopleSearchRaw for instant visual feedback
            // but filtering is debounced via onPeopleSearchChange
            TextField(
                value = vm.peopleSearchRaw,
                onValueChange = { vm.onPeopleSearchChange(it) },
                placeholder = { Text("🔍  Name or phone number...", color = T.text2,
                    fontSize = 13.rsp(scale)) },
                singleLine = true, colors = ftInputColors(T),
                shape = RoundedCornerShape(16.rdp(scale)),
                textStyle = androidx.compose.ui.text.TextStyle(color = T.text, fontSize = 14.rsp(scale)),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(14.rdp(scale)))
            Row(horizontalArrangement = Arrangement.spacedBy(12.rdp(scale))) {
                listOf("To Receive" to Pair(vm.toReceive, T.green),
                    "To Give"    to Pair(vm.toGive,    T.red)).forEach { (label, vp) ->
                    val (value, color) = vp
                    Surface(shape = RoundedCornerShape(20.rdp(scale)), color = T.surface,
                        shadowElevation = 3.dp, modifier = Modifier.weight(1f)) {
                        Column(Modifier.padding(16.rdp(scale))) {
                            Box(Modifier.fillMaxWidth().height(4.rdp(scale))
                                .clip(RoundedCornerShape(2.rdp(scale))).background(color))
                            Spacer(Modifier.height(10.rdp(scale)))
                            Text(label, fontSize = 10.rsp(scale), fontWeight = FontWeight.Bold,
                                color = T.text2, letterSpacing = 0.5.sp)
                            Spacer(Modifier.height(4.rdp(scale)))
                            Text(fmt(value), fontSize = 20.rsp(scale),
                                fontWeight = FontWeight.ExtraBold, color = color)
                        }
                    }
                }
            }
            Spacer(Modifier.height(14.rdp(scale)))
        }

        LazyColumn(contentPadding = PaddingValues(horizontal = hp, vertical = 4.rdp(scale))) {
            if (people.isNotEmpty()) {
                item {
                    Text("PEOPLE", fontSize = 10.rsp(scale), fontWeight = FontWeight.Bold,
                        color = T.text2, letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(bottom = 8.rdp(scale)))
                }
                item {
                    Surface(shape = RoundedCornerShape(24.rdp(scale)), color = T.surface,
                        shadowElevation = 3.dp) {
                        Column {
                            people.forEachIndexed { i, p ->
                                PersonRow(p, vm.net(p), T, scale) {
                                    vm.selPerson = p; vm.screen = "person"
                                }
                                if (i < people.size - 1)
                                    Divider(Modifier.padding(horizontal = 20.rdp(scale)),
                                        color = T.divider, thickness = 1.dp)
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(16.rdp(scale))) }
            }

            if (contacts.isNotEmpty()) {
                item {
                    Text("ADD FROM CONTACTS", fontSize = 10.rsp(scale), fontWeight = FontWeight.Bold,
                        color = T.text2, letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(bottom = 8.rdp(scale)))
                }
                item {
                    Surface(shape = RoundedCornerShape(24.rdp(scale)), color = T.surface,
                        shadowElevation = 3.dp) {
                        Column {
                            contacts.forEachIndexed { i, contact ->
                                Row(Modifier.fillMaxWidth().padding(14.rdp(scale)),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.rdp(scale))) {
                                    Box(Modifier.size(42.rdp(scale)).clip(CircleShape)
                                        .background(T.accentBg), contentAlignment = Alignment.Center) {
                                        Text(contact.name.firstOrNull()?.uppercase() ?: "?",
                                            fontSize = 17.rsp(scale), fontWeight = FontWeight.ExtraBold,
                                            color = T.accent)
                                    }
                                    Column(Modifier.weight(1f)) {
                                        Text(contact.name, fontSize = 14.rsp(scale),
                                            fontWeight = FontWeight.Bold, color = T.text)
                                        if (contact.phone.isNotEmpty())
                                            Text(contact.phone, fontSize = 11.rsp(scale), color = T.text2)
                                    }
                                    val cName = contact.name; val cPhone = contact.phone
                                    Surface(onClick = {
                                        vm.addPersonFromContactAndTransact(PhoneContact(cName, cPhone), "lent")
                                    }, shape = RoundedCornerShape(999.dp), color = T.greenBg,
                                        modifier = Modifier.height(32.rdp(scale))) {
                                        Box(contentAlignment = Alignment.Center,
                                            modifier = Modifier.padding(horizontal = 10.rdp(scale))) {
                                            Text("Lent", fontSize = 11.rsp(scale),
                                                fontWeight = FontWeight.Bold, color = T.green)
                                        }
                                    }
                                    Surface(onClick = {
                                        vm.addPersonFromContactAndTransact(PhoneContact(cName, cPhone), "borrowed")
                                    }, shape = RoundedCornerShape(999.dp), color = T.redBg,
                                        modifier = Modifier.height(32.rdp(scale))) {
                                        Box(contentAlignment = Alignment.Center,
                                            modifier = Modifier.padding(horizontal = 10.rdp(scale))) {
                                            Text("Borrow", fontSize = 11.rsp(scale),
                                                fontWeight = FontWeight.Bold, color = T.red)
                                        }
                                    }
                                }
                                if (i < contacts.size - 1)
                                    Divider(Modifier.padding(horizontal = 14.rdp(scale)),
                                        color = T.divider, thickness = 1.dp)
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(16.rdp(scale))) }
            }

            if (people.isEmpty() && contacts.isEmpty()) {
                item {
                    when {
                        vm.data.people.isEmpty() && vm.peopleSearch.isEmpty() ->
                            Surface(shape = RoundedCornerShape(24.rdp(scale)), color = T.surface,
                                shadowElevation = 3.dp, modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(40.rdp(scale)),
                                    horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("👥", fontSize = 52.rsp(scale))
                                    Spacer(Modifier.height(12.rdp(scale)))
                                    Text("No people yet", fontSize = 18.rsp(scale),
                                        fontWeight = FontWeight.ExtraBold, color = T.text)
                                    Spacer(Modifier.height(8.rdp(scale)))
                                    Text("Type a name or number to find contacts",
                                        fontSize = 14.rsp(scale), color = T.text2,
                                        textAlign = TextAlign.Center)
                                    Spacer(Modifier.height(22.rdp(scale)))
                                    PillButton("+ Add Person", T.accent) { vm.openModal("person") }
                                }
                            }
                        vm.peopleSearch.isNotEmpty() ->
                            Box(Modifier.fillMaxWidth().padding(top = 40.rdp(scale)),
                                contentAlignment = Alignment.Center) {
                                Text("No results for \"${vm.peopleSearch}\"",
                                    color = T.text2, fontSize = 14.rsp(scale))
                            }
                        else ->
                            Box(Modifier.fillMaxWidth().padding(top = 40.rdp(scale)),
                                contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🎉", fontSize = 40.rsp(scale))
                                    Spacer(Modifier.height(10.rdp(scale)))
                                    Text("All settled up!", fontSize = 16.rsp(scale),
                                        fontWeight = FontWeight.ExtraBold, color = T.text)
                                    Spacer(Modifier.height(6.rdp(scale)))
                                    Text("No pending settlements", fontSize = 13.rsp(scale), color = T.text2)
                                }
                            }
                    }
                }
            }
        }
    }
}

// ── Person Detail ─────────────────────────────────────────────────────────────

@Composable
fun PersonScreen(vm: AppVM, T: FTTheme) {
    val scale = rememberScale()
    val hp    = 20.rdp(scale)
    val p = vm.selPerson ?: return
    val net = vm.net(p)
    val lentTotal     = p.transactions.filter { !it.settled && it.type == "lent"     }.sumOf { it.amount }
    val borrowedTotal = p.transactions.filter { !it.settled && it.type == "borrowed" }.sumOf { it.amount }

    Column(Modifier.fillMaxSize()) {
        AppHeader(vm, T)
        LazyColumn(contentPadding = PaddingValues(horizontal = hp, vertical = 8.rdp(scale)),
            verticalArrangement = Arrangement.spacedBy(14.rdp(scale))) {
            item {
                TextButton(onClick = { vm.screen = "people"; vm.selPerson = null }) {
                    Text("← People", color = T.text2, fontSize = 14.rsp(scale), fontWeight = FontWeight.Bold)
                }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.rdp(scale))) {
                    Box(Modifier.size(62.rdp(scale)).clip(CircleShape)
                        .background(if (net >= 0) T.greenBg else T.redBg),
                        contentAlignment = Alignment.Center) {
                        Text(p.name.first().uppercase(), fontSize = 24.rsp(scale),
                            fontWeight = FontWeight.ExtraBold,
                            color = if (net >= 0) T.green else T.red)
                    }
                    Column {
                        Text(p.name, fontSize = 22.rsp(scale), fontWeight = FontWeight.ExtraBold, color = T.text)
                        if (p.phone.isNotEmpty()) Text(p.phone, fontSize = 14.rsp(scale), color = T.text2)
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.rdp(scale))) {
                    listOf(
                        Triple("They Owe", lentTotal,     T.green to T.greenBg),
                        Triple("You Owe",  borrowedTotal, T.red   to T.redBg),
                        Triple("Net", Math.abs(net),
                            (if (net >= 0) T.green else T.red) to
                                    (if (net >= 0) T.greenBg else T.redBg))
                    ).forEach { (label, value, cp) ->
                        val (color, bg) = cp
                        Box(Modifier.weight(1f).clip(RoundedCornerShape(18.rdp(scale))).background(bg)) {
                            Column(Modifier.padding(12.rdp(scale)),
                                horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(label, fontSize = 9.rsp(scale), fontWeight = FontWeight.Bold,
                                    color = T.text2, letterSpacing = 0.4.sp)
                                Spacer(Modifier.height(4.rdp(scale)))
                                Text(fmt(value), fontSize = 14.rsp(scale),
                                    fontWeight = FontWeight.ExtraBold, color = color)
                            }
                        }
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.rdp(scale))) {
                    Surface(onClick = { vm.openModal("transaction_lent") },
                        shape = RoundedCornerShape(999.dp), color = T.green,
                        shadowElevation = 4.dp, modifier = Modifier.weight(1f).height(48.rdp(scale))) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("💰 I Lent", fontSize = 14.rsp(scale),
                                fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    Surface(onClick = { vm.openModal("transaction_borrowed") },
                        shape = RoundedCornerShape(999.dp), color = T.red,
                        shadowElevation = 4.dp, modifier = Modifier.weight(1f).height(48.rdp(scale))) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("💸 Borrowed", fontSize = 14.rsp(scale),
                                fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
            item {
                Text("Transaction History", fontSize = 16.rsp(scale),
                    fontWeight = FontWeight.ExtraBold, color = T.text)
                Spacer(Modifier.height(2.rdp(scale)))
                Text("Tap a transaction to settle or delete it",
                    fontSize = 11.rsp(scale), color = T.text2)
            }
            if (p.transactions.isEmpty()) {
                item {
                    Surface(shape = RoundedCornerShape(20.rdp(scale)), color = T.surface,
                        shadowElevation = 3.dp, modifier = Modifier.fillMaxWidth()) {
                        Box(Modifier.padding(36.rdp(scale)), contentAlignment = Alignment.Center) {
                            Text("No transactions yet", fontSize = 14.rsp(scale),
                                fontWeight = FontWeight.SemiBold, color = T.text2)
                        }
                    }
                }
            } else {
                item {
                    Surface(shape = RoundedCornerShape(24.rdp(scale)), color = T.surface,
                        shadowElevation = 3.dp) {
                        Column {
                            p.transactions.reversed().forEachIndexed { i, tx ->
                                Surface(
                                    onClick = { if (!tx.settled) vm.openSettleDialog(p.id, tx.id) },
                                    color = Color.Transparent
                                ) {
                                    Row(Modifier.fillMaxWidth().padding(14.rdp(scale)),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.rdp(scale))) {
                                        Box(Modifier.size(40.rdp(scale))
                                            .clip(RoundedCornerShape(13.rdp(scale)))
                                            .background(if (tx.type == "lent") T.greenBg else T.redBg),
                                            contentAlignment = Alignment.Center) {
                                            Text(if (tx.type == "lent") "⬆️" else "⬇️",
                                                fontSize = 17.rsp(scale))
                                        }
                                        Column(Modifier.weight(1f)) {
                                            Text(tx.description, fontSize = 14.rsp(scale),
                                                fontWeight = FontWeight.Bold,
                                                color = if (tx.settled) T.text2 else T.text)
                                            Text(tx.date, fontSize = 12.rsp(scale), color = T.text2)
                                            if (!tx.dueDate.isNullOrBlank())
                                                Text("Due: ${tx.dueDate}", fontSize = 11.rsp(scale),
                                                    color = if (tx.dueDate <=
                                                        java.time.LocalDate.now().toString()
                                                        && !tx.settled) T.red else T.text2,
                                                    fontWeight = FontWeight.SemiBold)
                                            if (tx.settled)
                                                Text("✓ Settled", fontSize = 11.rsp(scale),
                                                    color = T.green, fontWeight = FontWeight.Bold)
                                            else
                                                Text("Tap to settle or delete",
                                                    fontSize = 10.rsp(scale), color = T.text3)
                                        }
                                        Text((if (tx.type == "lent") "+" else "-") + fmt(tx.amount),
                                            fontSize = 14.rsp(scale), fontWeight = FontWeight.ExtraBold,
                                            color = if (tx.settled) T.text2
                                            else if (tx.type == "lent") T.green else T.red)
                                    }
                                }
                                if (i < p.transactions.size - 1)
                                    Divider(Modifier.padding(horizontal = 14.rdp(scale)),
                                        color = T.divider, thickness = 1.dp)
                            }
                        }
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    TextButton(onClick = { vm.deletePerson(p.id) }) {
                        Text("Remove ${p.name} from list", color = T.text3, fontSize = 12.rsp(scale))
                    }
                }
            }
            item { Spacer(Modifier.height(8.rdp(scale))) }
        }
    }
}

// ── Portfolio ─────────────────────────────────────────────────────────────────

@Composable
fun PortfolioScreen(vm: AppVM, T: FTTheme) {
    val scale = rememberScale()
    val hp    = 20.rdp(scale)
    Column(Modifier.fillMaxSize()) {
        AppHeader(vm, T)
        Column(Modifier.fillMaxWidth().padding(horizontal = hp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom) {
                Column {
                    Text("Wealth", fontSize = 13.rsp(scale), fontWeight = FontWeight.SemiBold, color = T.text2)
                    Text("Portfolio", fontSize = 26.rsp(scale), fontWeight = FontWeight.ExtraBold, color = T.text)
                }
                Surface(onClick = { vm.openModal("investment") }, shape = RoundedCornerShape(999.dp),
                    color = T.accent, shadowElevation = 4.dp, modifier = Modifier.height(44.rdp(scale))) {
                    Box(contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 20.rdp(scale))) {
                        Text("+ Add", fontSize = 14.rsp(scale), fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
            Spacer(Modifier.height(16.rdp(scale)))
            Surface(shape = RoundedCornerShape(24.rdp(scale)), color = T.surface,
                shadowElevation = 3.dp, modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(22.rdp(scale)), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf(
                        Triple("Invested", fmt(vm.totInvested), T.accent),
                        Triple("Current",  fmt(vm.totCurrent),  T.text),
                        Triple("P&L", (if (vm.totPL >= 0) "+" else "") + fmt(vm.totPL),
                            if (vm.totPL >= 0) T.green else T.red)
                    ).forEach { (label, value, color) ->
                        Column {
                            Text(label.uppercase(), fontSize = 10.rsp(scale), fontWeight = FontWeight.Bold,
                                color = T.text2, letterSpacing = 0.6.sp)
                            Spacer(Modifier.height(4.rdp(scale)))
                            Text(value, fontSize = 16.rsp(scale), fontWeight = FontWeight.ExtraBold, color = color)
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.rdp(scale)))
            Text("BY CATEGORY", fontSize = 11.rsp(scale), fontWeight = FontWeight.Bold,
                color = T.text2, letterSpacing = 0.6.sp)
            Spacer(Modifier.height(10.rdp(scale)))
        }
        LazyColumn(contentPadding = PaddingValues(horizontal = hp, vertical = 4.rdp(scale))) {
            item(key = "row0") { CatCardRow(vm, T, scale, CATS[0].id, CATS[1].id) }
            item { Spacer(Modifier.height(12.rdp(scale))) }
            item(key = "row1") { CatCardRow(vm, T, scale, CATS[2].id, CATS[3].id) }
            item { Spacer(Modifier.height(12.rdp(scale))) }
            item(key = "row2") { CatCardRow(vm, T, scale, CATS[4].id, CATS[5].id) }
            item { Spacer(Modifier.height(16.rdp(scale))) }
        }
    }
}

@Composable
fun CatCardRow(vm: AppVM, T: FTTheme, scale: Float, leftId: String, rightId: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.rdp(scale))) {
        CatCard(CATS.first { it.id == leftId },  vm.cs(leftId),  T, scale, Modifier.weight(1f)) {
            vm.selCatId = leftId;  vm.screen = "category"
        }
        CatCard(CATS.first { it.id == rightId }, vm.cs(rightId), T, scale, Modifier.weight(1f)) {
            vm.selCatId = rightId; vm.screen = "category"
        }
    }
}

// ── Category Detail ───────────────────────────────────────────────────────────

@Composable
fun CategoryScreen(vm: AppVM, T: FTTheme) {
    val scale = rememberScale()
    val hp    = 20.rdp(scale)
    val cat   = vm.selCat ?: return
    val cs    = vm.cs(cat.id)

    Column(Modifier.fillMaxSize()) {
        AppHeader(vm, T)
        LazyColumn(contentPadding = PaddingValues(horizontal = hp, vertical = 8.rdp(scale)),
            verticalArrangement = Arrangement.spacedBy(14.rdp(scale))) {
            item {
                TextButton(onClick = { vm.screen = "portfolio"; vm.selCatId = "" }) {
                    Text("← Portfolio", color = T.text2, fontSize = 14.rsp(scale),
                        fontWeight = FontWeight.Bold)
                }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.rdp(scale))) {
                    Box(Modifier.size(58.rdp(scale)).clip(RoundedCornerShape(20.rdp(scale)))
                        .background(Color(cat.colorHex).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center) { Text(cat.icon, fontSize = 26.rsp(scale)) }
                    Column {
                        Text(cat.label, fontSize = 22.rsp(scale), fontWeight = FontWeight.ExtraBold, color = T.text)
                        Text("${cs.invs.size} investments", fontSize = 13.rsp(scale), color = T.text2)
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.rdp(scale))) {
                    listOf(
                        Triple("Invested", fmt(cs.invested),
                            Color(cat.colorHex) to Color(cat.colorHex).copy(0.15f)),
                        Triple("Current", fmt(cs.current), T.text to T.surface2),
                        Triple("P&L", (if (cs.pl >= 0) "+" else "") + fmt(cs.pl),
                            (if (cs.pl >= 0) T.green else T.red) to
                                    (if (cs.pl >= 0) T.greenBg else T.redBg))
                    ).forEach { (label, value, cp) ->
                        val (color, bg) = cp
                        Box(Modifier.weight(1f).clip(RoundedCornerShape(18.rdp(scale))).background(bg)) {
                            Column(Modifier.padding(12.rdp(scale)),
                                horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(label, fontSize = 9.rsp(scale), fontWeight = FontWeight.Bold,
                                    color = T.text2, letterSpacing = 0.4.sp)
                                Spacer(Modifier.height(4.rdp(scale)))
                                Text(value, fontSize = 14.rsp(scale),
                                    fontWeight = FontWeight.ExtraBold, color = color)
                            }
                        }
                    }
                }
            }
            item {
                GhostPillButton("+ Add ${cat.label}", Color(cat.colorHex),
                    Color(cat.colorHex).copy(alpha = 0.15f), Modifier.fillMaxWidth()) {
                    vm.fCat = cat.id; vm.openModal("investment")
                }
            }
            if (cs.invs.isEmpty()) {
                item {
                    Surface(shape = RoundedCornerShape(20.rdp(scale)), color = T.surface,
                        shadowElevation = 3.dp, modifier = Modifier.fillMaxWidth()) {
                        Box(Modifier.padding(36.rdp(scale)), contentAlignment = Alignment.Center) {
                            Text("No ${cat.label} investments yet", fontSize = 14.rsp(scale),
                                fontWeight = FontWeight.SemiBold, color = T.text2)
                        }
                    }
                }
            } else {
                item {
                    Surface(shape = RoundedCornerShape(24.rdp(scale)), color = T.surface,
                        shadowElevation = 3.dp) {
                        Column {
                            cs.invs.forEachIndexed { i, inv ->
                                val pl  = inv.currentValue - inv.investedAmount
                                val pct = if (inv.investedAmount > 0)
                                    (pl / inv.investedAmount) * 100 else 0.0
                                Column(Modifier.padding(16.rdp(scale))) {
                                    Row(Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top) {
                                        Column(Modifier.weight(1f)) {
                                            Text(inv.name, fontSize = 15.rsp(scale),
                                                fontWeight = FontWeight.ExtraBold, color = T.text)
                                            Text(inv.date, fontSize = 12.rsp(scale), color = T.text2)
                                            if (inv.notes.isNotEmpty())
                                                Text(inv.notes, fontSize = 12.rsp(scale), color = T.text2)
                                        }
                                        val invId = inv.id
                                        Surface(onClick = { vm.deleteInv(invId) }, shape = CircleShape,
                                            color = T.redBg, modifier = Modifier.size(30.rdp(scale))) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text("✕", fontSize = 13.rsp(scale), color = T.red)
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(10.rdp(scale)))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.rdp(scale))) {
                                        listOf(
                                            Triple("Invested", fmt(inv.investedAmount),
                                                Color(cat.colorHex) to Color(cat.colorHex).copy(0.15f)),
                                            Triple("Current", fmt(inv.currentValue), T.text to T.surface2),
                                            Triple((if (pl >= 0) "▲ " else "▼ ") +
                                                    String.format("%.1f%%", Math.abs(pct)),
                                                (if (pl >= 0) "+" else "") + fmt(pl),
                                                (if (pl >= 0) T.green else T.red) to
                                                        (if (pl >= 0) T.greenBg else T.redBg))
                                        ).forEach { (label, value, cp) ->
                                            val (color, bg) = cp
                                            Box(Modifier.weight(1f).clip(RoundedCornerShape(12.rdp(scale)))
                                                .background(bg)) {
                                                Column(Modifier.padding(8.rdp(scale)),
                                                    horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text(label, fontSize = 9.rsp(scale),
                                                        fontWeight = FontWeight.Bold, color = T.text2, maxLines = 1)
                                                    Spacer(Modifier.height(3.rdp(scale)))
                                                    Text(value, fontSize = 12.rsp(scale),
                                                        fontWeight = FontWeight.ExtraBold, color = color)
                                                }
                                            }
                                        }
                                    }
                                }
                                if (i < cs.invs.size - 1)
                                    Divider(Modifier.padding(horizontal = 16.rdp(scale)),
                                        color = T.divider, thickness = 1.dp)
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(8.rdp(scale))) }
        }
    }
}

// ── Shared components ─────────────────────────────────────────────────────────

@Composable
fun PersonRow(p: Person, net: Double, T: FTTheme, scale: Float, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Color.Transparent) {
        Row(Modifier.fillMaxWidth().padding(14.rdp(scale)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.rdp(scale))) {
                Box(Modifier.size(38.rdp(scale)).clip(CircleShape)
                    .background(if (net >= 0) T.greenBg else T.redBg),
                    contentAlignment = Alignment.Center) {
                    Text(p.name.first().uppercase(), fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.rsp(scale), color = if (net >= 0) T.green else T.red)
                }
                Column {
                    Text(p.name, fontWeight = FontWeight.Bold, fontSize = 14.rsp(scale), color = T.text)
                    Text("${p.transactions.filter { !it.settled }.size} pending",
                        fontSize = 11.rsp(scale), color = T.text2)
                }
            }
            Text((if (net >= 0) "+" else "") + fmt(net), fontSize = 14.rsp(scale),
                fontWeight = FontWeight.ExtraBold,
                color = if (net >= 0) T.green else T.red)
        }
    }
}

@Composable
fun CatRow(cat: FTCategory, cs: AppVM.CS, T: FTTheme, scale: Float, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Color.Transparent) {
        Row(Modifier.fillMaxWidth().padding(10.rdp(scale)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.rdp(scale))) {
                Box(Modifier.size(38.rdp(scale)).clip(RoundedCornerShape(12.rdp(scale)))
                    .background(Color(cat.colorHex).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center) { Text(cat.icon, fontSize = 18.rsp(scale)) }
                Column {
                    Text(cat.label, fontWeight = FontWeight.Bold, fontSize = 14.rsp(scale), color = T.text)
                    Text("${cs.invs.size} holdings", fontSize = 11.rsp(scale), color = T.text2)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(fmt(cs.invested), fontSize = 13.rsp(scale), fontWeight = FontWeight.ExtraBold, color = T.text)
                Text((if (cs.pl >= 0) "▲" else "▼") + String.format("%.1f%%", Math.abs(cs.pct)),
                    fontSize = 11.rsp(scale), fontWeight = FontWeight.Bold,
                    color = if (cs.pl >= 0) T.green else T.red)
            }
        }
    }
}

@Composable
fun CatCard(cat: FTCategory, cs: AppVM.CS, T: FTTheme, scale: Float,
            modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(22.rdp(scale)), color = T.surface,
        shadowElevation = if (cs.invested > 0) 5.dp else 3.dp, modifier = modifier) {
        Column(Modifier.padding(18.rdp(scale))) {
            Box(Modifier.size(48.rdp(scale)).clip(RoundedCornerShape(16.rdp(scale)))
                .background(Color(cat.colorHex).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center) { Text(cat.icon, fontSize = 22.rsp(scale)) }
            Spacer(Modifier.height(12.rdp(scale)))
            Text(cat.label, fontWeight = FontWeight.ExtraBold, fontSize = 14.rsp(scale), color = T.text)
            Spacer(Modifier.height(3.rdp(scale)))
            Text(fmt(cs.invested), fontSize = 17.rsp(scale), fontWeight = FontWeight.ExtraBold,
                color = if (cs.invested > 0) Color(cat.colorHex) else T.text2)
            if (cs.invested > 0) {
                Spacer(Modifier.height(3.rdp(scale)))
                Text((if (cs.pl >= 0) "▲ " else "▼ ") + String.format("%.1f%%", Math.abs(cs.pct)),
                    fontSize = 12.rsp(scale), fontWeight = FontWeight.Bold,
                    color = if (cs.pl >= 0) T.green else T.red)
            }
            Text("${cs.invs.size} holdings", fontSize = 11.rsp(scale), color = T.text2,
                fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun EmptyPreview(emoji: String, message: String, onAdd: () -> Unit) {
    val T = LocalFTTheme.current
    val scale = rememberScale()
    Column(Modifier.fillMaxWidth().padding(vertical = 18.rdp(scale)),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 34.rsp(scale))
        Spacer(Modifier.height(8.rdp(scale)))
        Text(message, fontSize = 13.rsp(scale), fontWeight = FontWeight.Bold, color = T.text2)
        Spacer(Modifier.height(12.rdp(scale)))
        Surface(onClick = onAdd, shape = RoundedCornerShape(999.dp), color = T.accentBg) {
            Text("+ Add", modifier = Modifier.padding(horizontal = 16.rdp(scale), vertical = 6.rdp(scale)),
                fontSize = 13.rsp(scale), fontWeight = FontWeight.Bold, color = T.accent)
        }
    }
}