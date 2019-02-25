package com.rajatgoyal.puzzle15.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import timber.log.Timber;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.rajatgoyal.puzzle15.R;

public abstract class BaseActivity extends AppCompatActivity {

	private GoogleSignInClient googleSignInClient = null;
	private static final int RC_SIGN_IN = 9001;

	GoogleSignInAccount signedInAccount;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
				.requestServerAuthCode(getString(R.string.default_web_client_id))
				.build();

		googleSignInClient = GoogleSignIn.getClient(this, gso);
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (isSignedIn()) return;
		signInSilently();
	}

	protected boolean isSignedIn() {
		return GoogleSignIn.getLastSignedInAccount(this) != null;
	}

	protected void signInSilently() {
		Timber.d("signInSilently()");

		googleSignInClient.silentSignIn().addOnCompleteListener(this,
				new OnCompleteListener<GoogleSignInAccount>() {
					@Override
					public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
						if (task.isSuccessful()) {
							Timber.d("signInSilently(): success");
							onConnected(task.getResult());
						} else {
							Timber.d("signInSilently(): failure");
							onDisconnected();
						}
					}
				});
	}

	protected void startSignInIntent() {
		startActivityForResult(googleSignInClient.getSignInIntent(), RC_SIGN_IN);
	}

	protected void signOut() {
		Timber.d("signOut()");

		if (!isSignedIn()) {
			Timber.d("signOut() called, but was not signed in!");
			return;
		}

		googleSignInClient.signOut().addOnCompleteListener(this,
				new OnCompleteListener<Void>() {
					@Override
					public void onComplete(@NonNull Task<Void> task) {
						boolean successful = task.isSuccessful();
						Timber.d("signOut(): %s", (successful ? "success" : "failed"));

						onDisconnected();
					}
				});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (requestCode == RC_SIGN_IN) {
			Task<GoogleSignInAccount> task =
					GoogleSignIn.getSignedInAccountFromIntent(intent);

			try {
				GoogleSignInAccount account = task.getResult(ApiException.class);
				onConnected(account);
			} catch (ApiException apiException) {
				String message = apiException.getMessage();
				if (message == null || message.isEmpty()) {
					message = "Sign In Failed Message";
				}
				Timber.d(message);
				onDisconnected();
			}
		}
	}

	protected void onConnected(GoogleSignInAccount googleSignInAccount) {
		Timber.d("onConnected(): connected to Google APIs");
		if (signedInAccount != googleSignInAccount) {
			signedInAccount = googleSignInAccount;
		}
	}

	public void onDisconnected() {
		Timber.d("onDisconnected()");
	}
}
