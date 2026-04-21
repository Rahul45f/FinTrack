package com.fintrack.app

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate

class AppVM(private val st: AppStorage) : ViewModel() {

    var dark by mutableStateOf(st.dark)
    var loggedIn by mutableStateOf(false)
    var userName by mutableStateOf(st.profile?.name ?: "")
    var authStage by mutableStateOf(if (st.profile?.pin != null) "pin" else "setup")

    var setupName by mutableStateOf("")
    var setupPin by mutableStateOf("")
    var setupConfirm by mutableStateOf("")
    var setupStep by mutableStateOf(1)

    var enteredPin by mutableStateOf("")
    var pinError by mutableStateOf("")

    var data by mutableStateOf(UserData())

    var screen by mutableStateOf("dashboard")
    var selPerson by mutableStateOf<Person?>(null)
    var selCatId by mutableStateOf("")

    var modal by mutableStateOf<String?>(null)
    var fName by mutableStateOf("")
    var fPhone by mutableStateOf("")
    var fType by mutableStateOf("lent")
    var fAmount by mutableStateOf("")
    var fDesc by mutableStateOf("")
    var fDate by mutableStateOf(todayStr())
    var fDueDate by mutableStateOf("")
    var fCat by mutableStateOf("stocks")
    var fInvested by mutableStateOf("")
    var fCurrent by mutableStateOf("")
    var fNotes by mutableStateOf("")
    var fBalance by mutableStateOf("")

    var contacts by mutableStateOf<List<PhoneContact>>(emptyList())
    var contactSearch by mutableStateOf("")
    var showContactPicker by mutableStateOf(false)

    // Raw search input — typed by user
    var peopleSearchRaw by mutableStateOf("")
    // Debounced version — used for actual filtering (prevents lag)
    var peopleSearch by mutableStateOf("")
    private var debounceJob: Job? = null

    var showPostAddDialog by mutableStateOf(false)
    var recentlyAddedPerson by mutableStateOf<Person?>(null)

    // Settle dialog
    var settleDialog by mutableStateOf<Pair<String, String>?>(null)
    var partialAmount by mutableStateOf("")

    val selCat: FTCategory? get() = CATS.find { it.id == selCatId }

    // ── Cached filtered results (only recomputed when data or search changes) ──
    // These are computed properties backed by the state — Compose will only
    // recompose what reads them, not the whole screen.
    val cachedFilteredPeople: List<Person> get() {
        val q = peopleSearch.trim().lowercase()
        return data.people
            .filter { Math.abs(net(it)) > 0.001 }
            .filter { q.isEmpty() || it.name.lowercase().contains(q) || it.phone.contains(q) }
            .sortedByDescending { Math.abs(net(it)) }
    }

    val cachedContactSuggestions: List<PhoneContact> get() {
        val q = peopleSearch.trim()
        if (q.isEmpty()) return emptyList()
        val existing = data.people.map { it.name.lowercase() }.toSet()
        return contacts
            .filter { contactMatches(it, q) && !existing.contains(it.name.lowercase()) }
            .sortedBy { score(it, q) }
    }

    fun todayStr(): String = LocalDate.now().toString()

    // ── Auth ───────────────────────────────────────────────────────────────

    fun toggleTheme() { dark = !dark; st.dark = dark }
    fun lock() { loggedIn = false; enteredPin = ""; pinError = "" }

    fun resetApp() {
        st.profile = null
        authStage = "setup"; setupStep = 1
        setupName = ""; setupPin = ""; setupConfirm = ""
        enteredPin = ""; pinError = ""; loggedIn = false
    }

    fun checkPin() {
        val profile = st.profile ?: return
        if (enteredPin == profile.pin) {
            data = st.load(profile.name); loggedIn = true; pinError = ""
        } else {
            pinError = "Incorrect PIN"; enteredPin = ""
        }
    }

    fun finishSetup() {
        st.profile = UserProfile(setupName.trim(), setupPin)
        val d = UserData(); st.save(setupName.trim(), d)
        userName = setupName.trim(); data = d; loggedIn = true
    }

    // ── Search with debounce ───────────────────────────────────────────────
    // When user types, update raw immediately (shows in TextField instantly)
    // but only update the actual search query after 200ms of no typing.
    // This prevents filtering 500+ contacts on every keystroke.

    fun onPeopleSearchChange(value: String) {
        peopleSearchRaw = value
        debounceJob?.cancel()
        if (value.isEmpty()) {
            // Clear immediately — no lag when deleting
            peopleSearch = ""
        } else {
            debounceJob = viewModelScope.launch {
                delay(200)
                peopleSearch = value
            }
        }
    }

    // ── Contacts ───────────────────────────────────────────────────────────

    fun loadContacts(ctx: Context) {
        viewModelScope.launch {
            // Load contacts off the main thread to avoid blocking UI
            val loaded = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                fetchPhoneContacts(ctx)
            }
            contacts = loaded
        }
    }

    fun selectContact(c: PhoneContact) {
        fName = c.name; fPhone = c.phone
        showContactPicker = false; modal = "person"
    }

    private fun score(c: PhoneContact, q: String): Int {
        val n  = c.name.lowercase()
        val ph = c.phone.replace(" ","").replace("-","").replace("+","")
        val ql = q.lowercase()
        val qd = q.replace(" ","").replace("-","").replace("+","")
        return when {
            n == ql || ph == qd   -> 0
            n.startsWith(ql)      -> 1
            ph.startsWith(qd)     -> 2
            n.contains(ql)        -> 3
            ph.contains(qd)       -> 4
            else                  -> 99
        }
    }

    private fun contactMatches(c: PhoneContact, q: String): Boolean {
        val n  = c.name.lowercase()
        val ph = c.phone.replace(" ","").replace("-","").replace("+","")
        val ql = q.lowercase()
        val qd = q.replace(" ","").replace("-","").replace("+","")
        return n.contains(ql) || ph.contains(qd)
    }

    fun filteredContacts(): List<PhoneContact> {
        val q = contactSearch.trim()
        return if (q.isEmpty()) contacts.sortedBy { it.name }
        else contacts.filter { contactMatches(it, q) }.sortedBy { score(it, q) }
    }

    fun addPersonFromContactAndTransact(c: PhoneContact, type: String) {
        val existing = data.people.find { it.name.lowercase() == c.name.lowercase() }
        val person = existing ?: run {
            val p = Person(name = c.name.trim(), phone = c.phone)
            save(data.copy(people = data.people + p)); p
        }
        selPerson = person; screen = "person"; peopleSearchRaw = ""; peopleSearch = ""
        openModal(if (type == "lent") "transaction_lent" else "transaction_borrowed")
    }

    fun triggerNotifications(ctx: Context) {
        NotificationHelper(ctx).checkAndNotify(data.people)
    }

    // ── Settle dialog ──────────────────────────────────────────────────────

    fun openSettleDialog(personId: String, txId: String) {
        settleDialog = personId to txId; partialAmount = ""
    }

    fun closeSettleDialog() { settleDialog = null; partialAmount = "" }

    fun settleFull() {
        val (pid, tid) = settleDialog ?: return
        val nd = data.copy(people = data.people.map { p ->
            if (p.id == pid) p.copy(
                transactions = p.transactions.map { t ->
                    if (t.id == tid) t.copy(settled = true) else t
                }
            ) else p
        })
        save(nd); selPerson = nd.people.find { it.id == pid }
        closeSettleDialog()
    }

    fun settlePartial() {
        val (pid, tid) = settleDialog ?: return
        val partial = partialAmount.trim().toDoubleOrNull() ?: return
        if (partial <= 0) return
        val person = data.people.find { it.id == pid } ?: return
        val tx = person.transactions.find { it.id == tid } ?: return
        if (partial >= tx.amount) { settleFull(); return }

        val remaining = tx.amount - partial
        val settledTx   = tx.copy(settled = true, amount = partial)
        val remainingTx = tx.copy(
            id = java.util.UUID.randomUUID().toString(),
            amount = remaining,
            description = "${tx.description} (remaining)",
            settled = false
        )
        val newTxs = person.transactions.map { t -> if (t.id == tid) settledTx else t } + remainingTx
        val nd = data.copy(people = data.people.map { p ->
            if (p.id == pid) p.copy(transactions = newTxs) else p
        })
        save(nd); selPerson = nd.people.find { it.id == pid }
        closeSettleDialog()
    }

    // Delete a single transaction completely
    fun deleteTransaction(personId: String, txId: String) {
        val nd = data.copy(people = data.people.map { p ->
            if (p.id == personId) p.copy(transactions = p.transactions.filter { it.id != txId })
            else p
        })
        save(nd); selPerson = nd.people.find { it.id == personId }
        closeSettleDialog()
    }

    // ── Modal helpers ──────────────────────────────────────────────────────

    fun openModal(type: String) {
        fName = ""; fPhone = ""; fAmount = ""; fDesc = ""
        fDate = todayStr(); fDueDate = ""
        fInvested = ""; fCurrent = ""; fNotes = ""
        fBalance = data.accountBalance.toLong().toString()
        fType = when (type) {
            "transaction_lent"     -> "lent"
            "transaction_borrowed" -> "borrowed"
            else -> "lent"
        }
        if (type != "investment") fCat = "stocks"
        modal = type
    }

    fun closeModal() { modal = null }

    // ── Data mutations ─────────────────────────────────────────────────────

    private fun save(d: UserData) { st.save(userName, d); data = d }

    fun submitBalance() {
        save(data.copy(accountBalance = fBalance.toDoubleOrNull() ?: 0.0)); closeModal()
    }

    fun submitPerson() {
        if (fName.isBlank()) return
        val newPerson = Person(name = fName.trim(), phone = fPhone)
        save(data.copy(people = data.people + newPerson))
        closeModal()
        selPerson = newPerson; screen = "person"
        showPostAddDialog = true; recentlyAddedPerson = newPerson
    }

    fun deletePerson(id: String) {
        save(data.copy(people = data.people.filter { it.id != id }))
        screen = "people"; selPerson = null
    }

    fun submitTransaction() {
        val person = selPerson ?: return
        val amt = fAmount.trim().toDoubleOrNull() ?: return
        if (amt <= 0) return
        val tx = Transaction(
            type        = fType,
            amount      = amt,
            description = fDesc.trim().ifBlank {
                if (fType == "lent") "Lent to ${person.name}" else "Borrowed from ${person.name}"
            },
            date    = fDate.ifBlank { todayStr() },
            dueDate = fDueDate.ifBlank { null }
        )
        val nd = data.copy(people = data.people.map { p ->
            if (p.id == person.id) p.copy(transactions = p.transactions + tx) else p
        })
        save(nd); selPerson = nd.people.find { it.id == person.id }
        closeModal()
    }

    fun submitInvestment() {
        if (fName.isBlank() || fInvested.isBlank() || fCurrent.isBlank()) return
        val inv = Investment(
            category       = fCat, name = fName,
            investedAmount = fInvested.toDoubleOrNull() ?: return,
            currentValue   = fCurrent.toDoubleOrNull() ?: return,
            date           = fDate.ifBlank { todayStr() }, notes = fNotes
        )
        save(data.copy(investments = data.investments + inv)); closeModal()
    }

    fun deleteInv(id: String) {
        save(data.copy(investments = data.investments.filter { it.id != id }))
    }

    // ── Stats ──────────────────────────────────────────────────────────────

    val toReceive get() = data.people.sumOf { p ->
        p.transactions.filter { !it.settled && it.type == "lent" }.sumOf { it.amount }
    }
    val toGive get() = data.people.sumOf { p ->
        p.transactions.filter { !it.settled && it.type == "borrowed" }.sumOf { it.amount }
    }
    val totInvested get() = data.investments.sumOf { it.investedAmount }
    val totCurrent  get() = data.investments.sumOf { it.currentValue }
    val totPL       get() = totCurrent - totInvested

    fun net(p: Person) = p.transactions
        .filter { !it.settled }
        .sumOf { if (it.type == "lent") it.amount else -it.amount }

    data class CS(val invs: List<Investment>, val invested: Double, val current: Double) {
        val pl  get() = current - invested
        val pct get() = if (invested > 0) (pl / invested) * 100 else 0.0
    }

    fun cs(catId: String): CS {
        val list = data.investments.filter { it.category == catId }
        return CS(list, list.sumOf { it.investedAmount }, list.sumOf { it.currentValue })
    }

    class Fac(private val s: AppStorage) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(c: Class<T>) = AppVM(s) as T
    }
}