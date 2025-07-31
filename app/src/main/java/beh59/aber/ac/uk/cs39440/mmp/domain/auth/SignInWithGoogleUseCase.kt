package beh59.aber.ac.uk.cs39440.mmp.domain.auth

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import beh59.aber.ac.uk.cs39440.mmp.R
import beh59.aber.ac.uk.cs39440.mmp.data.repository.IAuthRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.perf.metrics.AddTrace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject

/**
 * SignInWithGoogleUseCase
 * Encapsulates and coordinates logic related to Google authentication
 * @param authRepository Contains business logic and communication with AuthDataSource related to
 * authentication within the application
 */
class SignInWithGoogleUseCase @Inject constructor(
    private val authRepository: IAuthRepository
) {
    /**
     * invoke
     * Called when SignInWithGoogleUseCase is called. Manages the process of Google authentication
     * @param context The Android context needed to access CredentialManager
     * @return A result containing a FirebaseUser if successful and an exception if not
     */
    @AddTrace(name = "signInWithGoogleUseCase")
    suspend operator fun invoke(context: Context): Result<FirebaseUser> {
        return try {
            val credentialManager = CredentialManager.create(context)

            val hashedNonce = withContext(Dispatchers.IO) {
                val rawNonce = UUID.randomUUID().toString()
                val bytes = rawNonce.toByteArray()
                val md = MessageDigest.getInstance("SHA-256")
                val digest = md.digest(bytes)
                digest.fold("") { str, it -> str + "%02x".format(it) }
            }

            val googleIDOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .setAutoSelectEnabled(false)
                .setNonce(hashedNonce)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIDOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = context
            )

            val credential = result.credential

            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {

                val googleIDTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIDTokenCredential.idToken

                authRepository.signInWithGoogle(idToken)
            } else {
                val error = "Invalid credential type: ${credential.javaClass.simpleName}"
                Log.e("SignInWithGoogleUseCase", error)
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e("SignInWithGoogleUseCase", "Error during Google sign-in", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show()
            }
            Result.failure(e)
        }
    }
} 