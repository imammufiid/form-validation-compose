package com.mufiid.composeformvalidation.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mufiid.composeformvalidation.domain.usecase.ValidateEmail
import com.mufiid.composeformvalidation.domain.usecase.ValidatePassword
import com.mufiid.composeformvalidation.domain.usecase.ValidateRepeatedPassword
import com.mufiid.composeformvalidation.domain.usecase.ValidateTerms
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val validateEmail: ValidateEmail = ValidateEmail(),
    private val validatePassword: ValidatePassword = ValidatePassword(),
    private val validateRepeatedPassword: ValidateRepeatedPassword = ValidateRepeatedPassword(),
    private val validateTerm: ValidateTerms = ValidateTerms(),
) : ViewModel() {

    var state by mutableStateOf(RegistrationFormState())

    private val validationEventChannel = Channel<ValidationEvent>()
    val validationEvents = validationEventChannel.receiveAsFlow()

    fun onEvent(event: RegistrationFormEvent) {
        when (event) {
            is RegistrationFormEvent.EmailChanged -> {
                state = state.copy(email = event.email)

                val emailResult = validateEmail.execute(state.email)
                state = state.copy(emailError = emailResult.errorMessage)
            }
            is RegistrationFormEvent.PasswordChanged -> {
                state = state.copy(password = event.password)

                val passwordResult = validatePassword.execute(state.password)
                state = state.copy(passwordError = passwordResult.errorMessage)
            }
            is RegistrationFormEvent.RepeatedPasswordChanged -> {
                state = state.copy(repeatedPassword = event.repeatedPassword)

                val repeatedPasswordResult =
                    validateRepeatedPassword.execute(state.password, state.repeatedPassword)
                state = state.copy(repeatedPasswordError = repeatedPasswordResult.errorMessage)
            }
            is RegistrationFormEvent.TermsChanged -> {
                state = state.copy(acceptedTerms = event.isAccepted)

                val termsResult = validateTerm.execute(state.acceptedTerms)
                state = state.copy(termsError = termsResult.errorMessage)
            }
            RegistrationFormEvent.Submit -> {
                submitData()
            }
        }
    }

    private fun submitData() {
        // TODO: Validate all field when click button
        val emailResult = validateEmail.execute(state.email)
        val passwordResult = validatePassword.execute(state.password)
        val repeatedPasswordResult =
            validateRepeatedPassword.execute(state.password, state.repeatedPassword)
        val termsResult = validateTerm.execute(state.acceptedTerms)

        val hasError = listOf(
            emailResult, passwordResult, repeatedPasswordResult, termsResult
        ).any { !it.successful}

        if (hasError) {
            state = state.copy(
                emailError = emailResult.errorMessage,
                passwordError = passwordResult.errorMessage,
                repeatedPasswordError = repeatedPasswordResult.errorMessage,
                termsError = termsResult.errorMessage
            )
            return
        }

        viewModelScope.launch {
            state = state.copy(
                emailError = null,
                passwordError = null,
                repeatedPasswordError = null,
                termsError = null
            )
            validationEventChannel.send(ValidationEvent.Success)
        }

    }

    sealed class ValidationEvent {
        object Success : ValidationEvent()
    }
}