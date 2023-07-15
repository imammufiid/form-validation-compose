package com.mufiid.composeformvalidation.domain.usecase

class ValidatePassword {

    fun execute(password: String): ValidationResult {
        if (password.length < 8) {
            return ValidationResult(
                successful = false,
                errorMessage = "The password need to consist of at least 8 characters"
            )
        }

        val containsLettersAndDigits =
            password.any { it.isDigit() } && password.any { it.isLetter() }

        if (!containsLettersAndDigits) {
            return ValidationResult(
                successful = false,
                errorMessage = "That password needs to contain at least letter and digit"
            )
        }

        return ValidationResult(
            successful = true,
            errorMessage = null
        )
    }

}