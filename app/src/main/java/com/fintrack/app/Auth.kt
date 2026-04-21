package com.fintrack.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FinTrackRoot(vm: AppVM, onLogin: () -> Unit = {}) {
    val T = if (vm.dark) DarkFT else LightFT
    CompositionLocalProvider(LocalFTTheme provides T) {
        Box(
            Modifier.fillMaxSize().background(T.bg).systemBarsPadding()
        ) {
            if (!vm.loggedIn) AuthScreen(vm, T, onLogin)
            else MainApp(vm, T)
        }
    }
}

@Composable
fun AuthScreen(vm: AppVM, T: FTTheme, onLogin: () -> Unit = {}) {
    Box(
        Modifier.fillMaxSize().background(T.bg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))

            // Logo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(T.accentBg)
                    .padding(horizontal = 22.dp, vertical = 12.dp)
            ) {
                Text("💹", fontSize = 26.sp)
                Spacer(Modifier.width(8.dp))
                Text("FinTrack", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = T.accent)
            }
            Spacer(Modifier.height(10.dp))
            Text("Your personal finance companion", color = T.text2, fontSize = 14.sp)
            Spacer(Modifier.height(36.dp))

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = T.surface,
                shadowElevation = if (T == DarkFT) 8.dp else 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when {
                        vm.authStage == "setup" && vm.setupStep == 1 -> SetupName(vm, T)
                        vm.authStage == "setup" && vm.setupStep == 2 -> SetupPin(vm, T)
                        vm.authStage == "setup" && vm.setupStep == 3 -> ConfirmPin(vm, T, onLogin)
                        vm.authStage == "pin" -> PinLogin(vm, T, onLogin)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            TextButton(onClick = { vm.toggleTheme() }) {
                Text(
                    if (vm.dark) "☀️  Light mode" else "🌙  Dark mode",
                    color = T.text2, fontSize = 13.sp
                )
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SetupName(vm: AppVM, T: FTTheme) {
    Text("What's your name?", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = T.text)
    Spacer(Modifier.height(8.dp))
    Text("We'll personalise the app for you", color = T.text2, fontSize = 14.sp, textAlign = TextAlign.Center)
    Spacer(Modifier.height(28.dp))
    OutlinedTextField(
        value = vm.setupName,
        onValueChange = { vm.setupName = it },
        placeholder = { Text("Your name", color = T.text2) },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = outlinedColors(T),
        textStyle = LocalTextStyle.current.copy(
            color = T.text, fontSize = 17.sp,
            fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { if (vm.setupName.isNotBlank()) vm.setupStep = 2 }),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(20.dp))
    PillButton("Continue →", T.accent, enabled = vm.setupName.isNotBlank()) {
        if (vm.setupName.isNotBlank()) vm.setupStep = 2
    }
}

@Composable
private fun SetupPin(vm: AppVM, T: FTTheme) {
    Text("Set a PIN", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = T.text)
    Spacer(Modifier.height(8.dp))
    Text("Choose a 4-digit PIN to lock your app", color = T.text2, fontSize = 14.sp, textAlign = TextAlign.Center)
    Spacer(Modifier.height(32.dp))
    PinDots(vm.setupPin, T)
    Spacer(Modifier.height(28.dp))
    PinKeypad(
        onDigit = { if (vm.setupPin.length < 4) vm.setupPin += it },
        onDelete = { if (vm.setupPin.isNotEmpty()) vm.setupPin = vm.setupPin.dropLast(1) },
        T = T
    )
    if (vm.setupPin.length == 4) {
        Spacer(Modifier.height(22.dp))
        PillButton("Confirm →", T.accent) { vm.setupStep = 3 }
    }
    Spacer(Modifier.height(12.dp))
    TextButton(onClick = { vm.setupStep = 1; vm.setupPin = "" }) {
        Text("← Back", color = T.text2, fontSize = 13.sp)
    }
}

@Composable
private fun ConfirmPin(vm: AppVM, T: FTTheme, onLogin: () -> Unit) {
    Text("Confirm PIN", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = T.text)
    Spacer(Modifier.height(8.dp))
    Text("Enter the same PIN again", color = T.text2, fontSize = 14.sp, textAlign = TextAlign.Center)
    Spacer(Modifier.height(32.dp))
    PinDots(vm.setupConfirm, T)
    Spacer(Modifier.height(10.dp))
    Box(Modifier.height(24.dp), contentAlignment = Alignment.Center) {
        if (vm.pinError.isNotEmpty())
            Text(vm.pinError, color = T.red, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
    Spacer(Modifier.height(14.dp))
    PinKeypad(
        onDigit = { d ->
            if (vm.setupConfirm.length < 4) {
                vm.setupConfirm += d
                if (vm.setupConfirm.length == 4) {
                    if (vm.setupPin != vm.setupConfirm) {
                        vm.pinError = "PINs don't match"
                        vm.setupConfirm = ""
                    } else {
                        vm.finishSetup()
                        onLogin()
                    }
                }
            }
        },
        onDelete = { if (vm.setupConfirm.isNotEmpty()) vm.setupConfirm = vm.setupConfirm.dropLast(1) },
        T = T
    )
    Spacer(Modifier.height(12.dp))
    TextButton(onClick = { vm.setupStep = 2; vm.setupPin = ""; vm.setupConfirm = ""; vm.pinError = "" }) {
        Text("← Change PIN", color = T.text2, fontSize = 13.sp)
    }
}

@Composable
private fun PinLogin(vm: AppVM, T: FTTheme, onLogin: () -> Unit) {
    Box(
        Modifier.size(68.dp).clip(CircleShape).background(T.accentBg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            vm.userName.firstOrNull()?.uppercase() ?: "?",
            fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = T.accent
        )
    }
    Spacer(Modifier.height(14.dp))
    Text("Welcome back", color = T.text2, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    Text(vm.userName, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = T.text)
    Spacer(Modifier.height(28.dp))
    PinDots(vm.enteredPin, T)
    Spacer(Modifier.height(10.dp))
    Box(Modifier.height(24.dp), contentAlignment = Alignment.Center) {
        if (vm.pinError.isNotEmpty())
            Text(vm.pinError, color = T.red, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
    Spacer(Modifier.height(14.dp))
    PinKeypad(
        onDigit = { d ->
            if (vm.enteredPin.length < 4) {
                vm.enteredPin += d
                if (vm.enteredPin.length == 4) {
                    vm.checkPin()
                    if (vm.loggedIn) onLogin()
                }
            }
        },
        onDelete = { if (vm.enteredPin.isNotEmpty()) vm.enteredPin = vm.enteredPin.dropLast(1) },
        T = T
    )
    Spacer(Modifier.height(20.dp))
    TextButton(onClick = { vm.resetApp() }) {
        Text("Reset app", color = T.text3, fontSize = 12.sp)
    }
}

// ── PIN components ────────────────────────────────────────────────────────────

@Composable
fun PinDots(value: String, T: FTTheme) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(4) { i ->
            val filled = i < value.length
            Box(
                Modifier
                    .size(if (filled) 15.dp else 13.dp)
                    .clip(CircleShape)
                    .background(if (filled) T.accent else T.surface3)
            )
        }
    }
}

@Composable
fun PinKeypad(onDigit: (String) -> Unit, onDelete: () -> Unit, T: FTTheme) {
    val keys = listOf("1","2","3","4","5","6","7","8","9","","0","⌫")
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        keys.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { k ->
                    if (k.isEmpty()) {
                        Spacer(Modifier.size(76.dp, 56.dp))
                    } else {
                        val isDelete = k == "⌫"
                        Surface(
                            onClick = { if (isDelete) onDelete() else onDigit(k) },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isDelete) T.redBg else T.surface2,
                            modifier = Modifier.size(76.dp, 56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    k,
                                    fontSize = if (isDelete) 18.sp else 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDelete) T.red else T.text
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Shared UI helpers ─────────────────────────────────────────────────────────

@Composable
fun PillButton(text: String, color: Color, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            disabledContainerColor = color.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth().height(50.dp)
    ) {
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
fun GhostPillButton(
    text: String, textColor: Color, bgColor: Color,
    modifier: Modifier = Modifier, onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = bgColor,
        modifier = modifier.height(46.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(text, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textColor)
        }
    }
}

@Composable
fun outlinedColors(T: FTTheme) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = T.accent,
    unfocusedBorderColor = T.surface3,
    focusedContainerColor = T.surface2,
    unfocusedContainerColor = T.surface2,
    cursorColor = T.accent,
)

@Composable
fun ftInputColors(T: FTTheme) = TextFieldDefaults.colors(
    focusedContainerColor = T.surface2,
    unfocusedContainerColor = T.surface2,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    cursorColor = T.accent,
    focusedTextColor = T.text,
    unfocusedTextColor = T.text,
)

fun fmt(n: Double): String {
    val abs = Math.abs(n.toLong())
    val s = abs.toString()
    if (s.length <= 3) return "₹$s"
    val last3 = s.takeLast(3)
    val rest = s.dropLast(3)
    val grouped = rest.reversed().chunked(2).joinToString(",").reversed()
    return "₹$grouped,$last3"
}
